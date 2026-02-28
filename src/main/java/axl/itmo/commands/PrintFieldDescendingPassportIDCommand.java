package axl.itmo.commands;

import axl.itmo.utils.CollectionManager;
import axl.itmo.utils.Console;

/**
 * Command to print passportID fields in descending order.
 */
public class PrintFieldDescendingPassportIDCommand extends Command {
    private final CollectionManager collectionManager;
    private final Console console;

    public PrintFieldDescendingPassportIDCommand(CollectionManager collectionManager, Console console) {
        super("print_field_descending_passport_i_d", "вывести значения поля passportID всех элементов в порядке убывания");
        this.collectionManager = collectionManager;
        this.console = console;
    }

    @Override
    /** {@inheritDoc} */
    public boolean execute(String argument) {
        if (!argument.isEmpty()) {
            console.printError("Command 'print_field_descending_passport_i_d' does not take arguments.");
            return false;
        }
        console.println(Console.CYAN + "Passport IDs (Descending):" + Console.RESET);
        collectionManager.printFieldDescendingPassportID();
        return true;
    }
}
