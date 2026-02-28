package axl.itmo.commands;

import axl.itmo.utils.CommandManager;
import axl.itmo.utils.Console;

/**
 * Command to display help information.
 */
public class HelpCommand extends Command {
    /** Command manager providing the registry of commands. */
    private final CommandManager commandManager;
    /** Console for output. */
    private final Console console;

    /**
     * Creates a new HelpCommand.
     * @param commandManager command manager dependency
     * @param console console for output
     */
    public HelpCommand(CommandManager commandManager, Console console) {
        super("help", "вывести справку по доступным командам");
        this.commandManager = commandManager;
        this.console = console;
    }

    @Override
    /** {@inheritDoc} */
    public boolean execute(String argument) {
        if (!argument.isEmpty()) {
            console.printError("Command 'help' does not take arguments.");
            return false;
        }
        console.printInfo("Available commands:");
        for (Command command : commandManager.getCommands().values()) {
            console.println("  " + Console.CYAN + command.getName() + Console.RESET + " : " + command.getDescription());
        }
        return true;
    }
}
