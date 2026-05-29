package axl.itmo.client.command;

import axl.itmo.client.net.CommandSender;
import axl.itmo.client.net.ResponseReceiver;
import axl.itmo.client.ui.Console;
import axl.itmo.common.dto.CommandRequest;
import axl.itmo.common.dto.CommandResponse;
import axl.itmo.common.model.Person;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages client-side commands and server communication.
 */
public class ClientCommandManager {
    private final String serverHost;
    private final int serverPort;
    private final Console console;
    private final CommandSender sender;
    private final ResponseReceiver receiver;
    private final List<String> commandHistory;
    
    private SocketChannel socketChannel;
    private String userLogin;
    private String userPassword;

    public ClientCommandManager(String serverHost, int serverPort, Console console) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.console = console;
        this.sender = new CommandSender(serverHost, serverPort);
        this.receiver = new ResponseReceiver();
        this.commandHistory = new ArrayList<>();
    }
    
    /**
     * Ensures the client is connected to the server.
     */
    private boolean connect() {
        if (socketChannel != null && socketChannel.isOpen() && socketChannel.isConnected()) {
            return true;
        }
        
        try {
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            if (!socketChannel.connect(new InetSocketAddress(serverHost, serverPort))) {
                while (!socketChannel.finishConnect()) {
                    try { Thread.sleep(10); } catch (InterruptedException e) {}
                }
            }
            console.printSuccess("Connected to server at " + serverHost + ":" + serverPort);
            return true;
        } catch (ConnectException e) {
            console.printError("Cannot connect to server at " + serverHost + ":" + serverPort);
            console.printError("Please make sure the server is running.");
            return false;
        } catch (IOException e) {
            console.printError("Network error: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Closes the connection to the server.
     */
    public void disconnect() {
        if (socketChannel != null && socketChannel.isOpen()) {
            try {
                socketChannel.close();
                socketChannel = null;
            } catch (IOException e) {
                // Ignore
            }
        }
    }

    /**
     * Executes a command by sending it to the server and receiving response.
     *
     * @param commandName the command name
     * @param argument the command argument (can be null)
     * @return true if command succeeded
     */
    public boolean executeCommand(String commandName, Object argument) {
        commandHistory.add(commandName);

        // Don't allow save command on client
        if (commandName.equals("save")) {
            console.printError("'save' command is only available on server");
            return false;
        }

        // Handle exit locally
        if (commandName.equals("exit")) {
            console.printSuccess("Exiting...");
            disconnect();
            return true;
        }

        // Handle help locally
        if (commandName.equals("help")) {
            printHelp();
            return true;
        }

        // Handle history locally
        if (commandName.equals("history")) {
            printHistory();
            return true;
        }

        // Handle execute_script locally (reads file and sends commands to server)
        if (commandName.equals("execute_script")) {
            return executeScript((String) argument);
        }

        // Handle register and login locally
        if (commandName.equals("register")) {
            return handleRegister();
        }
        if (commandName.equals("login")) {
            return handleLogin();
        }

        if (!connect()) {
            return false;
        }

        try {
            CommandRequest request = new CommandRequest(commandName, argument);
            request.setLogin(userLogin);
            request.setPassword(userPassword);
            sender.sendCommand(request, socketChannel);

            CommandResponse response = receiver.receiveResponse(socketChannel);

            if (response.isSuccess()) {
                console.printSuccess(response.getMessage());

                // Display collection data if present
                if (response.getData() != null && !response.getData().isEmpty()) {
                    console.println(Console.CYAN + "Collection Elements:" + Console.RESET);
                    for (Person person : response.getData()) {
                        console.println(person.toString());
                    }
                }
            } else {
                console.printError(response.getMessage());
            }

            return response.isSuccess();
        } catch (IOException e) {
            console.printError("Network error or server disconnected: " + e.getMessage());
            disconnect();
            return false;
        } catch (ClassNotFoundException e) {
            console.printError("Protocol error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Prints available commands.
     */
    private void printHelp() {
        console.printInfo("Available commands:");
        console.println("  register - зарегистрироваться");
        console.println("  login - войти в систему");
        console.println("  add - добавить новый элемент в коллекцию");
        console.println("  update <id> - обновить значение элемента коллекции");
        console.println("  remove_by_id <id> - удалить элемент из коллекции по ID");
        console.println("  clear - очистить коллекцию");
        console.println("  show - показать все элементы коллекции");
        console.println("  info - вывести информацию о коллекции");
        console.println("  remove_first - удалить первый элемент коллекции");
        console.println("  reorder - изменить порядок сортировки коллекции");
        console.println("  count_less_than_height <height> - вывести количество элементов меньше по росту");
        console.println("  count_greater_than_passport_id <id> - вывести количество элементов с ID больше чем");
        console.println("  print_field_descending_passport_id - вывести значения поля passportID в обратном порядке");
        console.println("  execute_script <file> - выполнить скрипт из файла");
        console.println("  help - выводить справку по командам");
        console.println("  history - выводить последние команды");
        console.println("  exit - завершить программу");
    }

    /**
     * Prints command history.
     */
    private void printHistory() {
        if (commandHistory.isEmpty()) {
            console.printInfo("No commands in history");
            return;
        }
        console.printInfo("Command history:");
        for (int i = 0; i < commandHistory.size(); i++) {
            console.println((i + 1) + ". " + commandHistory.get(i));
        }
    }

    /**
     * Executes a script file containing commands.
     *
     * @param fileName the script file name
     * @return true if script executed successfully
     */
    private boolean executeScript(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            console.printError("execute_script requires a file name argument");
            return false;
        }

        try (java.io.BufferedReader reader = java.nio.file.Files.newBufferedReader(java.nio.file.Paths.get(fileName))) {
            String line;
            int lineNumber = 0;
            boolean allSuccessful = true;

            console.printInfo("Executing script: " + fileName);

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();

                if (line.isEmpty() || line.startsWith("#")) {
                    continue; // Skip empty lines and comments
                }

                console.println("Executing line " + lineNumber + ": " + line);

                // Parse command and argument
                String[] parts = line.split(" ", 2);
                String command = parts[0].toLowerCase();
                Object argument = null;

                // Handle different commands
                if (command.equals("add")) {
                    console.printError("add command not supported in scripts (requires interactive input)");
                    allSuccessful = false;
                    continue;
                } else if (command.equals("update")) {
                    if (parts.length < 2) {
                        console.printError("Line " + lineNumber + ": update requires an ID argument");
                        allSuccessful = false;
                        continue;
                    }
                    try {
                        argument = Integer.parseInt(parts[1]);
                        console.printError("update command not supported in scripts (requires interactive input)");
                        allSuccessful = false;
                        continue;
                    } catch (NumberFormatException e) {
                        console.printError("Line " + lineNumber + ": Invalid ID format");
                        allSuccessful = false;
                        continue;
                    }
                } else if (command.equals("remove_by_id")) {
                    if (parts.length < 2) {
                        console.printError("Line " + lineNumber + ": remove_by_id requires an ID argument");
                        allSuccessful = false;
                        continue;
                    }
                    try {
                        argument = Integer.parseInt(parts[1]);
                    } catch (NumberFormatException e) {
                        console.printError("Line " + lineNumber + ": Invalid ID format");
                        allSuccessful = false;
                        continue;
                    }
                } else if (command.equals("count_less_than_height")) {
                    if (parts.length < 2) {
                        console.printError("Line " + lineNumber + ": count_less_than_height requires a height argument");
                        allSuccessful = false;
                        continue;
                    }
                    try {
                        argument = Float.parseFloat(parts[1]);
                    } catch (NumberFormatException e) {
                        console.printError("Line " + lineNumber + ": Invalid height format");
                        allSuccessful = false;
                        continue;
                    }
                } else if (command.equals("count_greater_than_passport_id")) {
                    if (parts.length < 2) {
                        console.printError("Line " + lineNumber + ": count_greater_than_passport_id requires a passport ID argument");
                        allSuccessful = false;
                        continue;
                    }
                    argument = parts[1];
                } else if (command.equals("show") || command.equals("clear") || command.equals("remove_first") ||
                           command.equals("reorder") || command.equals("print_field_descending_passport_id") ||
                           command.equals("info")) {
                    // These commands don't need arguments
                } else {
                    console.printError("Line " + lineNumber + ": Unknown command: " + command);
                    allSuccessful = false;
                    continue;
                }

                if (!connect()) {
                    return false;
                }

                // Execute the command
                try {
                    CommandRequest request = new CommandRequest(command, argument);
                    request.setLogin(userLogin);
                    request.setPassword(userPassword);
                    sender.sendCommand(request, socketChannel);

                    CommandResponse response = receiver.receiveResponse(socketChannel);

                    if (response.isSuccess()) {
                        console.printSuccess("✓ " + response.getMessage());

                        // Display collection data if present
                        if (response.getData() != null && !response.getData().isEmpty()) {
                            console.println(Console.CYAN + "Collection Elements:" + Console.RESET);
                            for (Person person : response.getData()) {
                                console.println(person.toString());
                            }
                        }
                    } else {
                        console.printError("✗ " + response.getMessage());
                        allSuccessful = false;
                    }
                } catch (IOException e) {
                    console.printError("Network error or server disconnected: " + e.getMessage());
                    disconnect();
                    allSuccessful = false;
                } catch (ClassNotFoundException e) {
                    console.printError("Protocol error: " + e.getMessage());
                    allSuccessful = false;
                }
            }

            console.printInfo("Script execution completed. " + (allSuccessful ? "All commands successful." : "Some commands failed."));
            return allSuccessful;

        } catch (java.io.IOException e) {
            console.printError("Error reading script file '" + fileName + "': " + e.getMessage());
            return false;
        }
    }

    /**
     * Handles user registration.
     * @return true if registration successful
     */
    private boolean handleRegister() {
        console.printInfo("Registration");
        console.println("Enter login: ");
        String login = console.readLine();
        if (login == null || login.trim().isEmpty()) {
            console.printError("Login cannot be empty");
            return false;
        }
        console.println("Enter password: ");
        String password = console.readLine();
        if (password == null || password.trim().isEmpty()) {
            console.printError("Password cannot be empty");
            return false;
        }

        if (!connect()) {
            return false;
        }

        try {
            CommandRequest request = new CommandRequest("register", null);
            request.setLogin(login);
            request.setPassword(password);
            sender.sendCommand(request, socketChannel);

            CommandResponse response = receiver.receiveResponse(socketChannel);

            if (response.isSuccess()) {
                console.printSuccess(response.getMessage());
                userLogin = login;
                userPassword = password;
                return true;
            } else {
                console.printError(response.getMessage());
                return false;
            }
        } catch (IOException e) {
            console.printError("Network error: " + e.getMessage());
            disconnect();
            return false;
        } catch (ClassNotFoundException e) {
            console.printError("Protocol error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Handles user login.
     * @return true if login successful
     */
    private boolean handleLogin() {
        console.printInfo("Login");
        console.println("Enter login: ");
        String login = console.readLine();
        if (login == null || login.trim().isEmpty()) {
            console.printError("Login cannot be empty");
            return false;
        }
        console.println("Enter password: ");
        String password = console.readLine();
        if (password == null || password.trim().isEmpty()) {
            console.printError("Password cannot be empty");
            return false;
        }

        if (!connect()) {
            return false;
        }

        try {
            CommandRequest request = new CommandRequest("login", null);
            request.setLogin(login);
            request.setPassword(password);
            sender.sendCommand(request, socketChannel);

            CommandResponse response = receiver.receiveResponse(socketChannel);

            if (response.isSuccess()) {
                console.printSuccess(response.getMessage());
                userLogin = login;
                userPassword = password;
                return true;
            } else {
                console.printError(response.getMessage());
                return false;
            }
        } catch (IOException e) {
            console.printError("Network error: " + e.getMessage());
            disconnect();
            return false;
        } catch (ClassNotFoundException e) {
            console.printError("Protocol error: " + e.getMessage());
            return false;
        }
    }
}