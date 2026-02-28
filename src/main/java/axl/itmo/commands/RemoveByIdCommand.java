package axl.itmo.commands;

import axl.itmo.utils.CollectionManager;
import axl.itmo.utils.Console;

/**
 * Command to remove an element from the collection by its ID.
 */
public class RemoveByIdCommand extends Command {
    /** Collection manager used to access and modify elements. */
    private final CollectionManager collectionManager;
    /** Console for user interaction. */
    private final Console console;

    /**
     * Creates a new RemoveByIdCommand.
     * @param collectionManager collection manager dependency
     * @param console console for messages
     */
    public RemoveByIdCommand(CollectionManager collectionManager, Console console) {
        super("remove_by_id", "удалить элемент из коллекции по его id");
        this.collectionManager = collectionManager;
        this.console = console;
    }

    @Override
    /** {@inheritDoc} */
    public boolean execute(String argument) {
        if (argument.isEmpty()) {
            console.printError("Command 'remove_by_id' requires an ID argument.");
            return false;
        }
        try {
            int id = Integer.parseInt(argument);
            if (collectionManager.getById(id) == null) {
                console.printError("Person with id " + id + " not found.");
                return false;
            }
            collectionManager.removeById(id);
            console.printSuccess("Person removed successfully.");
            return true;
        } catch (NumberFormatException e) {
            console.printError("Invalid ID format. Please enter a valid integer.");
        }
        return false;
    }
}
