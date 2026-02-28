package axl.itmo.utils;

import axl.itmo.commands.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Manages command execution.
 */
public class CommandManager {
    private final CollectionManager collectionManager;
    private final Console console;
    private final LinkedList<String> history = new LinkedList<>();
    private final LinkedList<String> scriptStack = new LinkedList<>();
    private final Map<String, Command> commands = new HashMap<>();

    public CommandManager(CollectionManager collectionManager, Console console) {
        this.collectionManager = collectionManager;
        this.console = console;
        registerCommands();
    }

    private void registerCommands() {
        registerCommand(new HelpCommand(this, console));
        registerCommand(new InfoCommand(collectionManager, console));
        registerCommand(new ShowCommand(collectionManager, console));
        registerCommand(new AddCommand(collectionManager, console));
        registerCommand(new UpdateCommand(collectionManager, console));
        registerCommand(new RemoveByIdCommand(collectionManager, console));
        registerCommand(new ClearCommand(collectionManager, console));
        registerCommand(new SaveCommand(collectionManager, console));
        registerCommand(new ExecuteScriptCommand(this, console));
        registerCommand(new ExitCommand(console));
        registerCommand(new RemoveFirstCommand(collectionManager, console));
        registerCommand(new ReorderCommand(collectionManager, console));
        registerCommand(new HistoryCommand(this, console));
        registerCommand(new CountLessThanHeightCommand(collectionManager, console));
        registerCommand(new CountGreaterThanPassportIDCommand(collectionManager, console));
        registerCommand(new PrintFieldDescendingPassportIDCommand(collectionManager, console));
    }

    private void registerCommand(Command command) {
        commands.put(command.getName(), command);
    }

    public void execute(String commandLine) {
        String[] parts = commandLine.trim().split("\\s+", 2);
        String commandName = parts[0].toLowerCase();
        String argument = parts.length > 1 ? parts[1] : "";

        // Always record the attempted command in history (up to 8),
        // even if it is unknown. This matches tests expecting history
        // to track the last 8 entered commands regardless of validity.
        if (!commandName.isEmpty()) {
            addToHistory(commandName);
        }

        Command command = commands.get(commandName);
        if (command != null) {
            command.execute(argument);
        } else {
            console.printError("Unknown command: '" + commandName + "'. Type 'help' for available commands.");
        }
    }

    private void addToHistory(String command) {
        if (history.size() >= 8) {
            history.removeFirst();
        }
        history.add(command);
    }

    public Map<String, Command> getCommands() {
        return commands;
    }

    public LinkedList<String> getHistory() {
        return history;
    }

    public LinkedList<String> getScriptStack() {
        return scriptStack;
    }

    public CollectionManager getCollectionManager() {
        return collectionManager;
    }
}
