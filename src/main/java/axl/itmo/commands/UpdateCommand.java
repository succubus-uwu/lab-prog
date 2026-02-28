package axl.itmo.commands;

import axl.itmo.utils.CollectionManager;
import axl.itmo.utils.Console;
import axl.itmo.model.Person;

/**
 * Command to update an element in the collection by its ID.
 */
public class UpdateCommand extends Command {
    private final CollectionManager collectionManager;
    private final Console console;

    public UpdateCommand(CollectionManager collectionManager, Console console) {
        super("update", "обновить значение элемента коллекции, id которого равен заданному");
        this.collectionManager = collectionManager;
        this.console = console;
    }

    @Override
    /** {@inheritDoc} */
    public boolean execute(String argument) {
        if (argument.isEmpty()) {
            console.printError("Command 'update' requires an ID argument.");
            return false;
        }
        try {
            int id = Integer.parseInt(argument);
            Person existing = collectionManager.getById(id);
            if (existing == null) {
                console.printError("Person with id " + id + " not found.");
                return false;
            }
            Person person = console.readPerson(id);
            if (person != null) {
                collectionManager.update(id, person);
                console.printSuccess("Person updated successfully.");
                return true;
            }
        } catch (NumberFormatException e) {
            console.printError("Invalid ID format. Please enter a valid integer.");
        }
        return false;
    }
}
