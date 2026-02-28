package axl.itmo.commands;

import axl.itmo.utils.CollectionManager;
import axl.itmo.utils.Console;

/**
 * Command to remove the first element from the collection.
 */
public class RemoveFirstCommand extends Command {
    private final CollectionManager collectionManager;
    private final Console console;

    public RemoveFirstCommand(CollectionManager collectionManager, Console console) {
        super("remove_first", "удалить первый элемент из коллекции");
        this.collectionManager = collectionManager;
        this.console = console;
    }

    @Override
    /** {@inheritDoc} */
    public boolean execute(String argument) {
        if (!argument.isEmpty()) {
            console.printError("Command 'remove_first' does not take arguments.");
            return false;
        }
        if (collectionManager.getCollection().isEmpty()) {
            console.printInfo("Collection is empty.");
            return false;
        }
        collectionManager.removeFirst();
        console.printSuccess("First element removed successfully.");
        return true;
    }
}
