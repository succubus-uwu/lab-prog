package axl.itmo.commands;

import axl.itmo.utils.CollectionManager;
import axl.itmo.utils.Console;

/**
 * Command to clear the collection.
 */
public class ClearCommand extends Command {
    /** Collection manager used to operate on the collection. */
    private final CollectionManager collectionManager;
    /** Console for printing results and errors. */
    private final Console console;

    /**
     * Creates a new ClearCommand instance.
     * @param collectionManager collection manager dependency
     * @param console console for user interaction
     */
    public ClearCommand(CollectionManager collectionManager, Console console) {
        super("clear", "очистить коллекцию");
        this.collectionManager = collectionManager;
        this.console = console;
    }

    @Override
    /** {@inheritDoc} */
    public boolean execute(String argument) {
        if (!argument.isEmpty()) {
            console.printError("Command 'clear' does not take arguments.");
            return false;
        }
        collectionManager.clear();
        console.printSuccess("Collection cleared successfully.");
        return true;
    }
}
