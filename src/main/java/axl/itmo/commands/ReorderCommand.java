package axl.itmo.commands;

import axl.itmo.utils.CollectionManager;
import axl.itmo.utils.Console;

/**
 * Command to reverse the order of elements in the collection.
 */
public class ReorderCommand extends Command {
    private final CollectionManager collectionManager;
    private final Console console;

    public ReorderCommand(CollectionManager collectionManager, Console console) {
        super("reorder", "отсортировать коллекцию в порядке, обратном нынешнему");
        this.collectionManager = collectionManager;
        this.console = console;
    }

    @Override
    /** {@inheritDoc} */
    public boolean execute(String argument) {
        if (!argument.isEmpty()) {
            console.printError("Command 'reorder' does not take arguments.");
            return false;
        }
        collectionManager.reorder();
        console.printSuccess("Collection reordered successfully.");
        return true;
    }
}
