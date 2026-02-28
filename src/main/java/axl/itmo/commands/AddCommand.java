package axl.itmo.commands;

import axl.itmo.utils.CollectionManager;
import axl.itmo.utils.Console;
import axl.itmo.model.Person;

/**
 * Command to add a new element to the collection.
 */
public class AddCommand extends Command {
    /** Collection manager used to modify the collection. */
    private final CollectionManager collectionManager;
    /** Console for I/O messages. */
    private final Console console;

    /**
     * Creates a new AddCommand.
     * @param collectionManager collection manager dependency
     * @param console console for user interaction
     */
    public AddCommand(CollectionManager collectionManager, Console console) {
        super("add", "добавить новый элемент в коллекцию");
        this.collectionManager = collectionManager;
        this.console = console;
    }

    @Override
    /** {@inheritDoc} */
    public boolean execute(String argument) {
        if (!argument.isEmpty()) {
            console.printError("Command 'add' does not take arguments.");
            return false;
        }

        try {
            int id = collectionManager.generateId();
            Person person = console.readPerson(id);
            if (person != null) {
                collectionManager.add(person);
                console.printSuccess("Person added successfully.");
                return true;
            }
            return false;
        } catch (IllegalStateException e) {
            console.printError(e.getMessage());
            return false;
        }
    }
}
