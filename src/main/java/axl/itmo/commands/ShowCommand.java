package axl.itmo.commands;

import axl.itmo.utils.CollectionManager;
import axl.itmo.utils.Console;
import axl.itmo.model.Person;

/**
 * Command to display all elements of the collection.
 */
public class ShowCommand extends Command {
    /** Provides access to collection elements. */
    private final CollectionManager collectionManager;
    /** Console for output. */
    private final Console console;

    /**
     * Creates a new ShowCommand.
     * @param collectionManager collection manager dependency
     * @param console console for output
     */
    public ShowCommand(CollectionManager collectionManager, Console console) {
        super("show", "вывести в стандартный поток вывода все элементы коллекции в строковом представлении");
        this.collectionManager = collectionManager;
        this.console = console;
    }

    @Override
    /** {@inheritDoc} */
    public boolean execute(String argument) {
        if (!argument.isEmpty()) {
            console.printError("Command 'show' does not take arguments.");
            return false;
        }
        if (collectionManager.getCollection().isEmpty()) {
            console.printInfo("Collection is empty.");
        } else {
            console.println(Console.CYAN + "Collection Elements:" + Console.RESET);
            for (Person person : collectionManager.getCollection()) {
                console.println(person.toString());
            }
        }
        return true;
    }
}
