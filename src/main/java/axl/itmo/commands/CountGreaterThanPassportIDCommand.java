package axl.itmo.commands;

import axl.itmo.utils.CollectionManager;
import axl.itmo.utils.Console;

/**
 * Command to count elements with passportID greater than the specified value.
 */
public class CountGreaterThanPassportIDCommand extends Command {
    private final CollectionManager collectionManager;
    private final Console console;

    public CountGreaterThanPassportIDCommand(CollectionManager collectionManager, Console console) {
        super("count_greater_than_passport_i_d", "вывести количество элементов, значение поля passportID которых больше заданного");
        this.collectionManager = collectionManager;
        this.console = console;
    }

    @Override
    /** {@inheritDoc} */
    public boolean execute(String argument) {
        if (argument.isEmpty()) {
            console.printError("Command 'count_greater_than_passport_i_d' requires a passportID argument.");
            return false;
        }
        long count = collectionManager.countGreaterThanPassportID(argument);
        console.println("Count: " + Console.GREEN + count + Console.RESET);
        return true;
    }
}
