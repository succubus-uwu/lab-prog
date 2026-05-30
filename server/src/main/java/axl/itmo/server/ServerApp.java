package axl.itmo.server;

import axl.itmo.common.CommandNames;
import axl.itmo.common.dto.CommandRequest;
import axl.itmo.common.dto.CommandResponse;
import axl.itmo.server.collection.CollectionManager;
import axl.itmo.server.command.CommandProcessor;
import axl.itmo.server.command.ServerCommandManager;
import axl.itmo.server.logging.ServerLogger;
import axl.itmo.server.net.PushNotificationService;
import axl.itmo.server.net.RateLimiter;
import axl.itmo.server.net.RequestReader;
import axl.itmo.server.net.ResponseSender;
import axl.itmo.server.persistence.DatabaseHandler;
import axl.itmo.server.utils.AuthUtils;
import axl.itmo.server.utils.Console;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerApp {
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
    private PushNotificationService pushService;

    // Channels that have subscribed to push updates — deregistered from Selector
    private final Set<SocketChannel> pushChannels = ConcurrentHashMap.newKeySet();
    // Key cancellations requested from worker threads
    private final Queue<SelectionKey> keysToCancel = new ConcurrentLinkedQueue<>();

    private boolean running = true;
    private ExecutorService readPool;
    private ExecutorService processPool;
    private ExecutorService sendPool;

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        String host = DEFAULT_HOST;
        if (args.length > 0) {
            try { port = Integer.parseInt(args[0]); }
            catch (NumberFormatException e) { System.err.println("Invalid port. Using default: " + DEFAULT_PORT); }
        }
        if (args.length > 1) host = args[1];
        new ServerApp().start(host, port);
    }

    public void start(String host, int port) {
        try {
            initializeServer(host, port);
            registerShutdownHook();
            console.println("Welcome to the server console. Type 'help' for commands.");

            while (running) {
                // Cancel any keys scheduled by worker threads
                SelectionKey key;
                while ((key = keysToCancel.poll()) != null) {
                    key.cancel();
                }

                selector.selectNow();
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectedKeys.iterator();

                while (iterator.hasNext()) {
                    SelectionKey k = iterator.next();
                    iterator.remove();
                    try {
                        if (k.isAcceptable()) handleAccept();
                        else if (k.isReadable()) handleRead(k);
                    } catch (IOException e) {
                        logger.logError("Error handling connection", e);
                        k.cancel();
                    }
                }

                processConsoleInput();
                Thread.sleep(10);
            }
        } catch (IOException | InterruptedException e) {
            logger.logError("Server error", e);
            System.exit(1);
        } finally {
            shutdown();
        }
    }

    private void initializeServer(String host, int port) throws IOException {
        DatabaseHandler dbHandler = new DatabaseHandler();
        collectionManager = new CollectionManager(dbHandler);
        AuthUtils authUtils = new AuthUtils(dbHandler);
        pushService = new PushNotificationService();
        commandProcessor = new CommandProcessor(collectionManager, authUtils);
        commandProcessor.setPushService(pushService);
        requestReader = new RequestReader();
        responseSender = new ResponseSender();
        rateLimiter = new RateLimiter();
        console = new Console(new java.util.Scanner(System.in), false);
        serverCommandManager = new ServerCommandManager(collectionManager, console);

        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress(host, port));

        selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        readPool = Executors.newFixedThreadPool(4);
        processPool = Executors.newFixedThreadPool(4);
        sendPool = Executors.newFixedThreadPool(4);

        logger.logServerStart(port);
    }

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nServer shutting down...");
            collectionManager.save();
            logger.logCollectionSaved();
            logger.logServerShutdown();
        }));
    }

    private void handleAccept() throws IOException {
        SocketChannel clientChannel = serverSocketChannel.accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ);
        logger.logClientConnected(clientChannel.socket().getInetAddress().getHostAddress());
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();

        // Push channels: only check for disconnect
        if (pushChannels.contains(clientChannel)) {
            ByteBuffer probe = ByteBuffer.allocate(1);
            try {
                int r = clientChannel.read(probe);
                if (r == -1) disconnectPushChannel(clientChannel, key);
            } catch (IOException e) {
                disconnectPushChannel(clientChannel, key);
            }
            return;
        }

        String clientAddress = clientChannel.socket().getInetAddress().getHostAddress();

        readPool.execute(() -> {
            synchronized (clientChannel) {
                CommandRequest request = requestReader.readRequest(clientChannel);
                if (request == null) {
                    key.cancel();
                    try { clientChannel.close(); } catch (IOException ignored) {}
                    logger.logClientDisconnected(clientAddress);
                    return;
                }

                if (!rateLimiter.isAllowed(clientAddress)) {
                    sendPool.execute(() -> {
                        synchronized (clientChannel) {
                            responseSender.sendResponse(clientChannel,
                                new CommandResponse(false, "Rate limit exceeded."));
                        }
                    });
                    return;
                }

                final CommandRequest finalRequest = request;
                processPool.execute(() -> {
                    CommandResponse response = commandProcessor.process(finalRequest);
                    final boolean isSubscribe = CommandNames.SUBSCRIBE.getValue()
                            .equals(finalRequest.getCommandName());

                    sendPool.execute(() -> {
                        synchronized (clientChannel) {
                            responseSender.sendResponse(clientChannel, response);
                        }
                        // After ACK is sent, promote channel to push subscriber
                        if (isSubscribe && response.isSuccess()) {
                            pushChannels.add(clientChannel);
                            pushService.addSubscriber(clientChannel);
                            keysToCancel.add(key); // deregister from selector
                        }
                    });
                });
            }
        });
    }

    private void disconnectPushChannel(SocketChannel ch, SelectionKey key) {
        pushService.removeSubscriber(ch);
        pushChannels.remove(ch);
        key.cancel();
        try { ch.close(); } catch (IOException ignored) {}
        logger.logClientDisconnected(ch.socket().getInetAddress().getHostAddress());
    }

    private void shutdown() {
        try {
            if (selector != null && selector.isOpen()) selector.close();
            if (serverSocketChannel != null && serverSocketChannel.isOpen()) serverSocketChannel.close();
            if (readPool != null) readPool.shutdown();
            if (processPool != null) processPool.shutdown();
            if (sendPool != null) sendPool.shutdown();
        } catch (IOException e) {
            logger.logError("Error during shutdown", e);
        }
    }

    private void processConsoleInput() throws IOException {
        if (System.in.available() > 0) {
            String commandLine = console.readLine();
            if (commandLine == null) { running = false; return; }
            commandLine = commandLine.trim();
            if (commandLine.isEmpty()) return;

            String[] parts = commandLine.split(" ", 2);
            String command = parts[0].toLowerCase();
            Object argument = null;

            switch (command) {
                case "exit":
                    console.printSuccess("Server shutdown initiated...");
                    running = false;
                    break;
                case "set_rate_limit":
                    if (parts.length < 2) { console.printError("set_rate_limit requires a number"); return; }
                    try {
                        int limit = Integer.parseInt(parts[1]);
                        rateLimiter.setMaxRequestsPerMinute(limit);
                        console.printSuccess("Rate limit set to " + limit);
                    } catch (NumberFormatException e) { console.printError("Invalid number"); }
                    break;
                case "add":
                    serverCommandManager.executeCommand(command, null);
                    break;
                case "update":
                    if (parts.length < 2) { console.printError("update requires an ID"); return; }
                    try { argument = Integer.parseInt(parts[1]); serverCommandManager.executeCommand(command, argument); }
                    catch (NumberFormatException e) { console.printError("Invalid ID"); }
                    break;
                case "remove_by_id":
                    if (parts.length < 2) { console.printError("remove_by_id requires an ID"); return; }
                    try { argument = Integer.parseInt(parts[1]); serverCommandManager.executeCommand(command, argument); }
                    catch (NumberFormatException e) { console.printError("Invalid ID"); }
                    break;
                case "count_less_than_height":
                    if (parts.length < 2) { console.printError("count_less_than_height requires height"); return; }
                    try { argument = Float.parseFloat(parts[1]); serverCommandManager.executeCommand(command, argument); }
                    catch (NumberFormatException e) { console.printError("Invalid height"); }
                    break;
                case "count_greater_than_passport_id":
                    if (parts.length < 2) { console.printError("requires passport ID"); return; }
                    serverCommandManager.executeCommand(command, parts[1]);
                    break;
                case "show": case "clear": case "remove_first": case "reorder":
                case "print_field_descending_passport_id": case "info": case "save":
                    serverCommandManager.executeCommand(command, null);
                    break;
                case "help":
                    serverCommandManager.executeCommand(command, null);
                    console.println("  set_rate_limit <n> - Set max requests/min per client");
                    break;
                default:
                    console.printError("Unknown command: " + command);
            }
        }
    }
}
