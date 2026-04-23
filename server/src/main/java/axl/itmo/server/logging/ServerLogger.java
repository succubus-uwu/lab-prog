package axl.itmo.server.logging;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Server logger for lifecycle events and diagnostics.
 * Uses java.util.logging.Logger for all logging.
 */
public class ServerLogger {
    private static final Logger logger = Logger.getLogger(ServerLogger.class.getName());
    private static ServerLogger instance;

    // ANSI Color Codes
    private static final String RESET = "\u001B[0m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RED = "\u001B[31m";
    private static final String BLUE = "\u001B[34m";
    private static final String CYAN = "\u001B[36m";

    static {
        configureLogger();
    }

    /**
     * Configures the logger with console and file handlers.
     */
    private static void configureLogger() {
        logger.setLevel(Level.ALL);
        
        // Prevent passing log records to parent handlers (which duplicate console output)
        logger.setUseParentHandlers(false);

        // Remove default handlers (if any are attached directly to this logger)
        for (Handler handler : logger.getHandlers()) {
            logger.removeHandler(handler);
        }

        // Custom Console Formatter with Colors
        Formatter consoleFormatter = new Formatter() {
            private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            @Override
            public String format(LogRecord record) {
                String color;
                Level level = record.getLevel();
                if (level == Level.SEVERE) {
                    color = RED;
                } else if (level == Level.WARNING) {
                    color = YELLOW;
                } else if (record.getMessage().contains("connected") || record.getMessage().contains("started") || record.getMessage().contains("Collection saved")) {
                    color = GREEN;
                } else if (record.getMessage().contains("Request")) {
                    color = BLUE;
                } else if (record.getMessage().contains("Response")) {
                    color = CYAN;
                } else {
                    color = RESET; // Default info
                }

                String time = dateFormat.format(new Date(record.getMillis()));
                String message = formatMessage(record);
                
                StringBuilder exception = new StringBuilder();
                if (record.getThrown() != null) {
                    exception.append("\n").append(RED).append(record.getThrown().toString()).append(RESET);
                    for (StackTraceElement element : record.getThrown().getStackTrace()) {
                        exception.append("\n\tat ").append(element.toString());
                    }
                }

                return String.format("%s[%s]%s %s%-7s%s : %s%s\n", 
                    CYAN, time, RESET, 
                    color, level.getName(), RESET, 
                    color + message, exception.toString());
            }
        };

        // Custom File Formatter (No Colors)
        Formatter fileFormatter = new Formatter() {
            private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            @Override
            public String format(LogRecord record) {
                String time = dateFormat.format(new Date(record.getMillis()));
                String message = formatMessage(record);
                
                StringBuilder exception = new StringBuilder();
                if (record.getThrown() != null) {
                    exception.append("\n").append(record.getThrown().toString());
                    for (StackTraceElement element : record.getThrown().getStackTrace()) {
                        exception.append("\n\tat ").append(element.toString());
                    }
                }

                return String.format("[%s] %-7s : %s\n%s", time, record.getLevel().getName(), message, exception.toString());
            }
        };

        // Console handler
        java.util.logging.ConsoleHandler consoleHandler = new java.util.logging.ConsoleHandler();
        consoleHandler.setLevel(Level.ALL);
        consoleHandler.setFormatter(consoleFormatter);
        logger.addHandler(consoleHandler);

        // File handler
        try {
            FileHandler fileHandler = new FileHandler("server.log", true);
            fileHandler.setLevel(Level.ALL);
            fileHandler.setFormatter(fileFormatter);
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Could not create file handler for logging", e);
        }
    }

    /**
     * Gets the singleton logger instance.
     */
    public static ServerLogger getInstance() {
        if (instance == null) {
            instance = new ServerLogger();
        }
        return instance;
    }

    /**
     * Logs server start event.
     */
    public void logServerStart(int port) {
        logger.log(Level.INFO, "Server started on port " + port);
    }

    /**
     * Logs new client connection.
     */
    public void logClientConnected(String clientAddress) {
        logger.log(Level.INFO, "New client connection from: " + clientAddress);
    }

    /**
     * Logs new request received.
     */
    public void logRequestReceived(String commandName, String clientAddress) {
        logger.log(Level.INFO, "Request received from " + clientAddress + " -> " + commandName);
    }

    /**
     * Logs response sent.
     */
    public void logResponseSent(String clientAddress, boolean success) {
        logger.log(Level.INFO, "Response sent to " + clientAddress + " (Success: " + success + ")");
    }

    /**
     * Logs client disconnection.
     */
    public void logClientDisconnected(String clientAddress) {
        logger.log(Level.INFO, "Client disconnected: " + clientAddress);
    }

    /**
     * Logs error or exception.
     */
    public void logError(String message, Throwable throwable) {
        logger.log(Level.SEVERE, message, throwable);
    }

    /**
     * Logs collection saved.
     */
    public void logCollectionSaved() {
        logger.log(Level.INFO, "Collection saved to file");
    }

    /**
     * Logs server shutdown.
     */
    public void logServerShutdown() {
        logger.log(Level.INFO, "Server shutdown initiated");
    }
}
