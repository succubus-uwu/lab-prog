package axl.itmo.commands;

import axl.itmo.utils.CollectionManager;
import axl.itmo.utils.Console;

/**
 * Command to count elements with height less than the specified value.
 */
public class CountLessThanHeightCommand extends Command {
    private final CollectionManager collectionManager;
    private final Console console;

    public CountLessThanHeightCommand(CollectionManager collectionManager, Console console) {
        super("count_less_than_height", "вывести количество элементов, значение поля height которых меньше заданного");
        this.collectionManager = collectionManager;
        this.console = console;
    }

    @Override
    /** {@inheritDoc} */
    public boolean execute(String argument) {
        if (argument.isEmpty()) {
            console.printError("Command 'count_less_than_height' requires a height argument.");
            return false;
        }
        try {
            float height = Float.parseFloat(argument);
            long count = collectionManager.countLessThanHeight(height);
            console.println("Count: " + Console.GREEN + count + Console.RESET);
            return true;
        } catch (NumberFormatException e) {
            console.printError("Invalid height format. Please enter a valid number.");
        }
        return false;
    }
}
