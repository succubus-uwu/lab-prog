package axl.itmo;

import axl.itmo.utils.CollectionManager;
import axl.itmo.utils.CommandManager;
import axl.itmo.utils.Console;
import axl.itmo.utils.FileManager;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        String envVariable = "FILE";
        FileManager fileManager = new FileManager(envVariable);
        CollectionManager collectionManager = new CollectionManager(fileManager);
        Console console = new Console(new Scanner(System.in), false);
        CommandManager commandManager = new CommandManager(collectionManager, console);

        console.println("Welcome to the Person Collection Manager!");
        console.println("Type 'help' to see available commands.");

        while (true) {
            console.print("> ");
            String commandLine = console.readLine();
            if (commandLine == null) {
                break;
            }
            if (commandLine.trim().isEmpty()) continue;
            commandManager.execute(commandLine);
        }
    }
}
