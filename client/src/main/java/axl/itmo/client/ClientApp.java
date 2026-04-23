package axl.itmo.client;

import axl.itmo.client.command.ClientCommandManager;
import axl.itmo.client.ui.Console;
import axl.itmo.common.model.Person;

import java.util.Scanner;

/**
 * Main client application.
 * Reads commands interactively from console and communicates with server.
 */
public class ClientApp {
    private static final String DEFAULT_SERVER_HOST = "localhost";
    private static final int DEFAULT_SERVER_PORT = 9999;

    public static void main(String[] args) {
        String serverHost = DEFAULT_SERVER_HOST;
        int serverPort = DEFAULT_SERVER_PORT;

        if (args.length > 0) {
            serverHost = args[0];
        }

        if (args.length > 1) {
            try {
                serverPort = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number. Using default: " + DEFAULT_SERVER_PORT);
            }
        }

        ClientApp client = new ClientApp();
        client.start(serverHost, serverPort);
    }

    /**
     * Starts the client application.
     */
    public void start(String serverHost, int serverPort) {
        Console console = new Console(new Scanner(System.in), false);
        ClientCommandManager commandManager = new ClientCommandManager(serverHost, serverPort, console);

        console.println("Welcome to the Person Collection Manager Client!");
        console.println("Type 'help' to see available commands.");
        console.println("Server: " + serverHost + ":" + serverPort);

        boolean running = true;
        while (running) {
            console.print("> ");
            String commandLine = console.readLine();
            if (commandLine == null) {
                break;
            }

            commandLine = commandLine.trim();
            if (commandLine.isEmpty()) {
                continue;
            }

            // Parse command and argument
            String[] parts = commandLine.split(" ", 2);
            String command = parts[0].toLowerCase();
            Object argument = null;

            // Process different commands
            if (command.equals("exit")) {
                running = false;
                commandManager.executeCommand(command, argument);
            } else if (command.equals("help") || command.equals("history")) {
                commandManager.executeCommand(command, argument);
            } else if (command.equals("add")) {
                int id = 1; // Server will assign actual ID
                Person person = console.readPerson(id);
                if (person != null) {
                    commandManager.executeCommand(command, person);
                }
            } else if (command.equals("update")) {
                if (parts.length < 2) {
                    console.printError("update requires an ID argument");
                    continue;
                }
                try {
                    int id = Integer.parseInt(parts[1]);
                    Person person = console.readPerson(id);
                    if (person != null) {
                        person.setId(id);
                        commandManager.executeCommand(command, person);
                    }
                } catch (NumberFormatException e) {
                    console.printError("Invalid ID format");
                }
            } else if (command.equals("remove_by_id")) {
                if (parts.length < 2) {
                    console.printError("remove_by_id requires an ID argument");
                    continue;
                }
                try {
                    int id = Integer.parseInt(parts[1]);
                    commandManager.executeCommand(command, id);
                } catch (NumberFormatException e) {
                    console.printError("Invalid ID format");
                }
            } else if (command.equals("count_less_than_height")) {
                if (parts.length < 2) {
                    console.printError("count_less_than_height requires a height argument");
                    continue;
                }
                try {
                    float height = Float.parseFloat(parts[1]);
                    commandManager.executeCommand(command, height);
                } catch (NumberFormatException e) {
                    console.printError("Invalid height format");
                }
            } else if (command.equals("count_greater_than_passport_id")) {
                if (parts.length < 2) {
                    console.printError("count_greater_than_passport_id requires a passport ID argument");
                    continue;
                }
                String passportId = parts[1];
                commandManager.executeCommand(command, passportId);
            } else if (command.equals("show") || command.equals("clear") ||
                       command.equals("remove_first") || command.equals("reorder") ||
                       command.equals("print_field_descending_passport_id") ||
                       command.equals("info")) {
                commandManager.executeCommand(command, argument);
            } else if (command.equals("save")) {
                console.printError("'save' command is only available on server");
            } else if (command.equals("execute_script")) {
                if (parts.length < 2) {
                    console.printError("execute_script requires a file name argument");
                    continue;
                }
                String fileName = parts[1];
                commandManager.executeCommand(command, fileName);
            } else {
                console.printError("Unknown command: " + command);
            }
        }
        
        commandManager.disconnect();
    }
}
