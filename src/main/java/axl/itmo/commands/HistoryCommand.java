package axl.itmo.commands;

import axl.itmo.utils.CommandManager;
import axl.itmo.utils.Console;

/**
 * Command to display the history of executed commands.
 */
public class HistoryCommand extends Command {
    private final CommandManager commandManager;
    private final Console console;

    public HistoryCommand(CommandManager commandManager, Console console) {
        super("history", "вывести последние 8 команд (без их аргументов)");
        this.commandManager = commandManager;
        this.console = console;
    }

    @Override
    /** {@inheritDoc} */
    public boolean execute(String argument) {
        if (!argument.isEmpty()) {
            console.printError("Command 'history' does not take arguments.");
            return false;
        }
        console.println(Console.CYAN + "Command History:" + Console.RESET);
        for (String command : commandManager.getHistory()) {
            console.println("  " + command);
        }
        return true;
    }
}
