package axl.itmo.server;

import axl.itmo.common.dto.CommandRequest;
import axl.itmo.common.dto.CommandResponse;
import axl.itmo.server.collection.CollectionManager;
import axl.itmo.server.command.CommandProcessor;
import axl.itmo.server.command.ServerCommandManager;
import axl.itmo.server.logging.ServerLogger;
import axl.itmo.server.net.RequestReader;
import axl.itmo.server.net.ResponseSender;
import axl.itmo.server.net.RateLimiter;
import axl.itmo.server.persistence.DatabaseHandler;
import axl.itmo.server.utils.AuthUtils;
import axl.itmo.server.utils.Console;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main server application with non-blocking NIO architecture.
 * Accepts connections, reads requests, processes commands, and sends responses.
 * Runs in a single-threaded mode where console input is non-blocking.
 */
public class ServerApp {
    private static final String ENV_VAR = "FILE";
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 9999;
    private static final ServerLogger logger = ServerLogger.getInstance();

    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private CollectionManager collectionManager;
    private CommandProcessor commandProcessor;
    private RequestReader requestReader;
    private ResponseSender responseSender;
    private ServerCommandManager serverCommandManager;
    private RateLimiter rateLimiter;
    private Console console;
    private boolean running = true;
    private ExecutorService readPool;
    private ExecutorService processPool;
    private ExecutorService sendPool;

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        String host = DEFAULT_HOST;

        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number. Using default: " + DEFAULT_PORT);
            }
        }

        if (args.length > 1) {
            host = args[1];
        }

        ServerApp server = new ServerApp();
        server.start(host, port);
    }

    /**
     * Starts the server on the specified host and port.
     */
    public void start(String host, int port) {
        try {
            initializeServer(host, port);
            registerShutdownHook();

            console.println("Welcome to the server console. Type 'help' for commands.");
            
            // Run everything in a single thread
            while (running) {
                // Non-blocking select to allow checking console
                selector.selectNow();
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectedKeys.iterator();

                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();

                    try {
                        if (key.isAcceptable()) {
                            handleAccept();
                        } else if (key.isReadable()) {
                            handleRead(key);
                        }
                    } catch (IOException e) {
                        logger.logError("Error handling connection", e);
                        key.cancel();
                    }
                }

                // Check console non-blocking
                processConsoleInput();
                
                // Prevent tight loop CPU spinning
                Thread.sleep(10);
            }

        } catch (IOException | InterruptedException e) {
            logger.logError("Server error", e);
            System.exit(1);
        } finally {
            shutdown();
        }
    }

    /**
     * Initializes server components.
     */
    private void initializeServer(String host, int port) throws IOException {
        // Initialize components
        DatabaseHandler dbHandler = new DatabaseHandler();
        collectionManager = new CollectionManager(dbHandler);
        AuthUtils authUtils = new AuthUtils(dbHandler);
        commandProcessor = new CommandProcessor(collectionManager, authUtils);
        requestReader = new RequestReader();
        responseSender = new ResponseSender();
        rateLimiter = new RateLimiter();
        console = new Console(new java.util.Scanner(System.in), false);
        serverCommandManager = new ServerCommandManager(collectionManager, console);

        // Create server socket channel
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress(host, port));

        // Create selector
        selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        // Initialize thread pools
        readPool = Executors.newFixedThreadPool(4);
        processPool = Executors.newFixedThreadPool(4);
        sendPool = Executors.newFixedThreadPool(4);

        logger.logServerStart(port);
    }

    /**
     * Registers a shutdown hook to save collection on exit.
     */
    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nServer shutting down...");
            collectionManager.save();
            logger.logCollectionSaved();
            logger.logServerShutdown();
        }));
    }

    /**
     * Handles new client connection.
     */
    private void handleAccept() throws IOException {
        SocketChannel clientChannel = serverSocketChannel.accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ);
        logger.logClientConnected(clientChannel.socket().getInetAddress().getHostAddress());
    }

    /**
     * Handles client request reading and processing.
     */
    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        String clientAddress = clientChannel.socket().getInetAddress().getHostAddress();

        readPool.execute(() -> {
            synchronized (clientChannel) {
                CommandRequest request = requestReader.readRequest(clientChannel);
                if (request == null) {
                    // Null might mean channel is closed or incomplete read (for now, assuming simple case where null means error/disconnect)
                    key.cancel();
                    try {
                        clientChannel.close();
                    } catch (IOException e) {
                        // Ignore
                    }
                    logger.logClientDisconnected(clientAddress);
                    return;
                }
                
                // Rate Limiting Check
                if (!rateLimiter.isAllowed(clientAddress)) {
                    sendPool.execute(() -> {
                        synchronized (clientChannel) {
                            responseSender.sendResponse(clientChannel, new CommandResponse(false, "Rate limit exceeded. Too many requests."));
                        }
                    });
                    return;
                }

                final CommandRequest finalRequest = request;
                processPool.execute(() -> {
                    CommandResponse response = commandProcessor.process(finalRequest);

                    sendPool.execute(() -> {
                        synchronized (clientChannel) {
                            responseSender.sendResponse(clientChannel, response);
                        }
                    });
                });
            }
        });
    }

    /**
     * Shuts down the server.
     */
    private void shutdown() {
        try {
            if (selector != null && selector.isOpen()) {
                selector.close();
            }
            if (serverSocketChannel != null && serverSocketChannel.isOpen()) {
                serverSocketChannel.close();
            }
            if (readPool != null && !readPool.isShutdown()) {
                readPool.shutdown();
            }
            if (processPool != null && !processPool.isShutdown()) {
                processPool.shutdown();
            }
            if (sendPool != null && !sendPool.isShutdown()) {
                sendPool.shutdown();
            }
        } catch (IOException e) {
            logger.logError("Error during shutdown", e);
        }
    }

    /**
     * Non-blocking console processing.
     */
    private void processConsoleInput() throws IOException {
        if (System.in.available() > 0) {
            String commandLine = console.readLine();
            if (commandLine == null) {
                running = false;
                return;
            }

            commandLine = commandLine.trim();
            if (commandLine.isEmpty()) {
                return;
            }

            // Parse command and argument
            String[] parts = commandLine.split(" ", 2);
            String command = parts[0].toLowerCase();
            Object argument = null;

            // Handle different commands
            switch (command) {
                case "exit":
                    console.printSuccess("Server shutdown initiated...");
                    running = false;
                    break;
                case "set_rate_limit":
                    if (parts.length < 2) {
                        console.printError("set_rate_limit requires a number argument");
                        return;
                    }
                    try {
                        int limit = Integer.parseInt(parts[1]);
                        rateLimiter.setMaxRequestsPerMinute(limit);
                        console.printSuccess("Rate limit set to " + limit + " requests per minute.");
                    } catch (NumberFormatException e) {
                        console.printError("Invalid number format for rate limit.");
                    }
                    break;
                case "add":
                    serverCommandManager.executeCommand(command, null);
                    break;
                case "update":
                    if (parts.length < 2) {
                        console.printError("update requires an ID argument");
                        return;
                    }
                    try {
                        argument = Integer.parseInt(parts[1]);
                        serverCommandManager.executeCommand(command, argument);
                    } catch (NumberFormatException e) {
                        console.printError("Invalid ID format");
                    }
                    break;
                case "remove_by_id":
                    if (parts.length < 2) {
                        console.printError("remove_by_id requires an ID argument");
                        return;
                    }
                    try {
                        argument = Integer.parseInt(parts[1]);
                        serverCommandManager.executeCommand(command, argument);
                    } catch (NumberFormatException e) {
                        console.printError("Invalid ID format");
                    }
                    break;
                case "count_less_than_height":
                    if (parts.length < 2) {
                        console.printError("count_less_than_height requires a height argument");
                        return;
                    }
                    try {
                        argument = Float.parseFloat(parts[1]);
                        serverCommandManager.executeCommand(command, argument);
                    } catch (NumberFormatException e) {
                        console.printError("Invalid height format");
                    }
                    break;
                case "count_greater_than_passport_id":
                    if (parts.length < 2) {
                        console.printError("count_greater_than_passport_id requires a passport ID argument");
                        return;
                    }
                    argument = parts[1];
                    serverCommandManager.executeCommand(command, argument);
                    break;
                case "show":
                case "clear":
                case "remove_first":
                case "reorder":
                case "print_field_descending_passport_id":
                case "info":
                case "save":
                case "help":
                    if (command.equals("help")) {
                        serverCommandManager.executeCommand(command, null);
                        console.println("  set_rate_limit <limit> - Set max requests per minute per client");
                    } else {
                        serverCommandManager.executeCommand(command, null);
                    }
                    break;
                default:
                    console.printError("Unknown command: " + command);
                    break;
            }
        }
    }
}