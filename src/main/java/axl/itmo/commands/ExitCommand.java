package axl.itmo.commands;

import axl.itmo.utils.Console;

/**
 * Command to exit the application.
 */
public class ExitCommand extends Command {
    private final Console console;

    public ExitCommand(Console console) {
        super("exit", "завершить программу (без сохранения в файл)");
        this.console = console;
    }

    @Override
    /** {@inheritDoc} */
    public boolean execute(String argument) {
        if (!argument.isEmpty()) {
            console.printError("Command 'exit' does not take arguments.");
            return false;
        }
        console.printInfo("Exiting...");
        System.exit(0);
        return true;
    }
}
