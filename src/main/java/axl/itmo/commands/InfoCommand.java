package axl.itmo.commands;

import axl.itmo.utils.CollectionManager;
import axl.itmo.utils.Console;

/**
 * Command to display information about the collection.
 */
public class InfoCommand extends Command {
    /** Provides access to the collection and its metadata. */
    private final CollectionManager collectionManager;
    /** Console for output. */
    private final Console console;

    /**
     * Creates a new InfoCommand.
     * @param collectionManager collection manager dependency
     * @param console console for user interaction
     */
    public InfoCommand(CollectionManager collectionManager, Console console) {
        super("info", "вывести в стандартный поток вывода информацию о коллекции (тип, дата инициализации, количество элементов и т.д.)");
        this.collectionManager = collectionManager;
        this.console = console;
    }

    @Override
    /** {@inheritDoc} */
    public boolean execute(String argument) {
        if (!argument.isEmpty()) {
            console.printError("Command 'info' does not take arguments.");
            return false;
        }
        console.println(Console.CYAN + "Collection Info:" + Console.RESET);
        console.println("  Type: " + Console.GREEN + collectionManager.getCollection().getClass().getName() + Console.RESET);
        console.println("  Initialization Date: " + Console.GREEN + collectionManager.getCreationDate() + Console.RESET);
        console.println("  Element Count: " + Console.GREEN + collectionManager.getCollection().size() + Console.RESET);
        return true;
    }
}
