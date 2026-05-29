package axl.itmo.server.command;

import axl.itmo.common.CommandNames;
import axl.itmo.common.model.Person;
import axl.itmo.server.collection.CollectionManager;
import axl.itmo.server.logging.ServerLogger;
import axl.itmo.server.utils.Console;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.LinkedList;

/**
 * Manages server-side console commands.
 * Processes commands locally and outputs results to console.
 */
public class ServerCommandManager {
    private final CollectionManager collectionManager;
    private final Console console;
    private final ServerLogger logger;

    public ServerCommandManager(CollectionManager collectionManager, Console console) {
        this.collectionManager = collectionManager;
        this.console = console;
        this.logger = ServerLogger.getInstance();
    }

    /**
     * Executes a command from server console.
     *
     * @param commandName the command name
     * @param argument the command argument (can be null)
     * @return true if command succeeded
     */
    public boolean executeCommand(String commandName, Object argument) {
        try {
            CommandNames cmd = CommandNames.fromValue(commandName);
            if (cmd == null) {
                console.printError("Unknown command: " + commandName);
                return false;
            }

            switch (cmd) {
                case ADD:
                    return handleAdd();
                case UPDATE:
                    return handleUpdate(argument != null ? (Integer) argument : null);
                case REMOVE_BY_ID:
                    return handleRemoveById(argument != null ? (Integer) argument : null);
                case CLEAR:
                    return handleClear();
                case SHOW:
                    return handleShow();
                case INFO:
                    return handleInfo();
                case REMOVE_FIRST:
                    return handleRemoveFirst();
                case REORDER:
                    return handleReorder();
                case COUNT_LESS_THAN_HEIGHT:
                    return handleCountLessThanHeight(argument != null ? (Float) argument : null);
                case COUNT_GREATER_THAN_PASSPORT_ID:
                    return handleCountGreaterThanPassportID(argument != null ? (String) argument : null);
                case PRINT_FIELD_DESCENDING_PASSPORT_ID:
                    return handlePrintFieldDescendingPassportID();
                case SAVE:
                    return handleSave();
                case HELP:
                    return handleHelp();
                case HISTORY:
                    console.printError("History not available on server console");
                    return false;
                case EXIT:
                    console.printSuccess("Server shutdown initiated...");
                    return true;
                case EXECUTE_SCRIPT:
                    console.printError("Execute script not supported on server console");
                    return false;
                default:
                    console.printError("Unknown command");
                    return false;
            }
        } catch (Exception e) {
            console.printError("Error executing command: " + e.getMessage());
            logger.logError("Command execution error", e);
            return false;
        }
    }

    private boolean handleAdd() {
        int id = collectionManager.generateId();
        Person person = console.readPerson(id);
        if (person != null) {
            person.setId(id);
            person.setCreationDate(LocalDateTime.now());
            try {
                collectionManager.add(person, 0L); // Use 0 as default owner for console
                console.printSuccess("Person added successfully with ID: " + id);
                logger.logCollectionSaved(); // Log as collection change
                return true;
            } catch (SQLException e) {
                console.printError("Error saving to database: " + e.getMessage());
            }
        }
        return false;
    }

    private boolean handleUpdate(Integer id) {
        if (id == null) {
            console.printError("update requires an ID argument");
            return false;
        }

        Person existing = collectionManager.getById(id);
        if (existing == null) {
            console.printError("Person with id " + id + " not found.");
            return false;
        }

        Person person = console.readPerson(id);
        if (person != null) {
            person.setCreationDate(LocalDateTime.now());
            try {
                collectionManager.update(id, person, 0L);
                console.printSuccess("Person updated successfully.");
                logger.logCollectionSaved();
                return true;
            } catch (SQLException e) {
                console.printError("Error updating database: " + e.getMessage());
            }
        }
        return false;
    }

    private boolean handleRemoveById(Integer id) {
        if (id == null) {
            console.printError("remove_by_id requires an ID argument");
            return false;
        }

        if (collectionManager.getById(id) == null) {
            console.printError("Person with id " + id + " not found.");
            return false;
        }

        try {
            collectionManager.removeById(id, 0L);
            console.printSuccess("Person removed successfully.");
            logger.logCollectionSaved();
            return true;
        } catch (SQLException e) {
            console.printError("Error removing from database: " + e.getMessage());
            return false;
        }
    }

    private boolean handleClear() {
        try {
            collectionManager.clear(0L);
            console.printSuccess("Collection cleared.");
            logger.logCollectionSaved();
            return true;
        } catch (SQLException e) {
            console.printError("Error clearing database: " + e.getMessage());
            return false;
        }
    }

    private boolean handleShow() {
        LinkedList<Person> collection = collectionManager.getCollection();
        if (collection.isEmpty()) {
            console.printInfo("Collection is empty.");
        } else {
            console.println(Console.CYAN + "Collection Elements:" + Console.RESET);
            for (Person person : collection) {
                console.println(person.toString());
            }
        }
        return true;
    }

    private boolean handleInfo() {
        int size = collectionManager.getCollection().size();
        LocalDateTime creationDate = collectionManager.getCreationDate();
        console.println("Collection type: LinkedList");
        console.println("Elements count: " + size);
        console.println("Creation date: " + creationDate);
        return true;
    }

    private boolean handleRemoveFirst() {
        if (collectionManager.getCollection().isEmpty()) {
            console.printError("Collection is empty");
            return false;
        }
        try {
            collectionManager.removeFirst(0L);
            console.printSuccess("First element removed.");
            logger.logCollectionSaved();
            return true;
        } catch (SQLException e) {
            console.printError("Error removing from database: " + e.getMessage());
            return false;
        }
    }

    private boolean handleReorder() {
        collectionManager.reorder();
        console.printSuccess("Collection reordered.");
        logger.logCollectionSaved();
        return true;
    }

    private boolean handleCountLessThanHeight(Float height) {
        if (height == null) {
            console.printError("count_less_than_height requires a height argument");
            return false;
        }
        long count = collectionManager.countLessThanHeight(height);
        console.println("Count: " + count);
        return true;
    }

    private boolean handleCountGreaterThanPassportID(String passportID) {
        if (passportID == null) {
            console.printError("count_greater_than_passport_id requires a passport ID argument");
            return false;
        }
        long count = collectionManager.countGreaterThanPassportID(passportID);
        console.println("Count: " + count);
        return true;
    }

    private boolean handlePrintFieldDescendingPassportID() {
        String result = collectionManager.printFieldDescendingPassportID();
        if (result.isEmpty()) {
            console.printInfo("No passport IDs to display");
        } else {
            console.println(result);
        }
        return true;
    }

    private boolean handleSave() {
        collectionManager.save();
        console.printSuccess("Collection saved to file.");
        logger.logCollectionSaved();
        return true;
    }

    private boolean handleHelp() {
        console.printInfo("Available server commands:");
        console.println("  add - Add new element to collection");
        console.println("  update <id> - Update element by ID");
        console.println("  remove_by_id <id> - Remove element by ID");
        console.println("  clear - Clear collection");
        console.println("  show - Show all elements");
        console.println("  info - Show collection info");
        console.println("  remove_first - Remove first element");
        console.println("  reorder - Reorder collection");
        console.println("  count_less_than_height <h> - Count by height");
        console.println("  count_greater_than_passport_id <id> - Count by passport ID");
        console.println("  print_field_descending_passport_id - Print passport IDs");
        console.println("  save - Save collection to file");
        console.println("  help - Show this help");
        console.println("  exit - Shutdown server");
        return true;
    }
}