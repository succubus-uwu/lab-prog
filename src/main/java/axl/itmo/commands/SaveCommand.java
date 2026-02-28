package axl.itmo.commands;

import axl.itmo.utils.CollectionManager;
import axl.itmo.utils.Console;

/**
 * Command to save the collection to a file.
 */
public class SaveCommand extends Command {
    private final CollectionManager collectionManager;
    private final Console console;

    public SaveCommand(CollectionManager collectionManager, Console console) {
        super("save", "сохранить коллекцию в файл");
        this.collectionManager = collectionManager;
        this.console = console;
    }

    @Override
    /** {@inheritDoc} */
    public boolean execute(String argument) {
        if (!argument.isEmpty()) {
            console.printError("Command 'save' does not take arguments.");
            return false;
        }
        collectionManager.save();
        console.printSuccess("Collection saved successfully.");
        return true;
    }
}
