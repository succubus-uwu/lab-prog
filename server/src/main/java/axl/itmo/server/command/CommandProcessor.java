package axl.itmo.server.command;

import axl.itmo.common.CommandNames;
import axl.itmo.common.dto.CommandRequest;
import axl.itmo.common.dto.CommandResponse;
import axl.itmo.common.model.Person;
import axl.itmo.server.collection.CollectionManager;

import java.time.LocalDateTime;
import java.util.LinkedList;

/**
 * Processes commands received from clients.
 * Executes commands against the server collection and returns responses.
 */
public class CommandProcessor {
    private final CollectionManager collectionManager;

    public CommandProcessor(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    /**
     * Processes a command request and returns a response.
     *
     * @param request the command request from client
     * @return command response
     */
    public CommandResponse process(CommandRequest request) {
        String commandName = request.getCommandName();
        Object argument = request.getArgument();

        try {
            CommandNames cmd = CommandNames.fromValue(commandName);
            if (cmd == null) {
                return new CommandResponse(false, "Unknown command: " + commandName);
            }

            switch (cmd) {
                case ADD:
                    return handleAdd((Person) argument);
                case UPDATE:
                    return handleUpdate((Person) argument);
                case REMOVE_BY_ID:
                    return handleRemoveById((Integer) argument);
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
                case EXECUTE_SCRIPT:
                    return new CommandResponse(false, "Execute script not supported on server");
                case EXIT:
                    return new CommandResponse(true, "Client exiting");
                case HELP:
                    return handleHelp();
                case HISTORY:
                    return new CommandResponse(false, "History not available on server");
                case COUNT_LESS_THAN_HEIGHT:
                    return handleCountLessThanHeight((Float) argument);
                case COUNT_GREATER_THAN_PASSPORT_ID:
                    return handleCountGreaterThanPassportID((String) argument);
                case PRINT_FIELD_DESCENDING_PASSPORT_ID:
                    return handlePrintFieldDescendingPassportID();
                default:
                    return new CommandResponse(false, "Unknown command");
            }
        } catch (Exception e) {
            return new CommandResponse(false, "Error processing command: " + e.getMessage());
        }
    }

    private CommandResponse handleAdd(Person person) {
        if (person == null) {
            return new CommandResponse(false, "Invalid person data");
        }

        int newId = collectionManager.generateId();
        person.setId(newId);
        person.setCreationDate(LocalDateTime.now());

        collectionManager.add(person);
        return new CommandResponse(true, "Person added successfully with ID: " + newId);
    }

    private CommandResponse handleUpdate(Person person) {
        if (person == null) {
            return new CommandResponse(false, "Invalid person data");
        }

        int id = person.getId();
        if (collectionManager.getById(id) == null) {
            return new CommandResponse(false, "Person with ID " + id + " not found");
        }

        person.setCreationDate(LocalDateTime.now());
        collectionManager.update(id, person);
        return new CommandResponse(true, "Person updated successfully");
    }

    private CommandResponse handleRemoveById(Integer id) {
        if (id == null || id <= 0) {
            return new CommandResponse(false, "Invalid ID");
        }

        if (collectionManager.getById(id) == null) {
            return new CommandResponse(false, "Person with ID " + id + " not found");
        }

        collectionManager.removeById(id);
        return new CommandResponse(true, "Person removed successfully");
    }

    private CommandResponse handleClear() {
        collectionManager.clear();
        return new CommandResponse(true, "Collection cleared");
    }

    private CommandResponse handleShow() {
        LinkedList<Person> sortedCollection = collectionManager.getCollectionSortedByLocation();
        if (sortedCollection.isEmpty()) {
            return new CommandResponse(true, "Collection is empty", new LinkedList<>());
        }
        return new CommandResponse(true, "Showing collection", sortedCollection);
    }

    private CommandResponse handleInfo() {
        int size = collectionManager.getCollection().size();
        LocalDateTime creationDate = collectionManager.getCreationDate();
        String info = "Collection type: LinkedList\n" +
                "Elements count: " + size + "\n" +
                "Creation date: " + creationDate;
        return new CommandResponse(true, info);
    }

    private CommandResponse handleRemoveFirst() {
        if (collectionManager.getCollection().isEmpty()) {
            return new CommandResponse(false, "Collection is empty");
        }
        collectionManager.removeFirst();
        return new CommandResponse(true, "First element removed");
    }

    private CommandResponse handleReorder() {
        collectionManager.reorder();
        return new CommandResponse(true, "Collection reordered");
    }

    private CommandResponse handleHelp() {
        String help = "Available commands:\n" +
                "add - Add a new element\n" +
                "update - Update an element\n" +
                "remove_by_id <id> - Remove element by ID\n" +
                "clear - Clear collection\n" +
                "show - Show all elements\n" +
                "info - Show collection info\n" +
                "remove_first - Remove first element\n" +
                "reorder - Reorder collection\n" +
                "count_less_than_height <height> - Count elements with height < value\n" +
                "count_greater_than_passport_id <id> - Count elements with passport ID > value\n" +
                "print_field_descending_passport_id - Print passport IDs in descending order\n" +
                "help - Show this help\n" +
                "exit - Exit application";
        return new CommandResponse(true, help);
    }

    private CommandResponse handleCountLessThanHeight(Float height) {
        if (height == null || height <= 0) {
            return new CommandResponse(false, "Invalid height value");
        }
        long count = collectionManager.countLessThanHeight(height);
        return new CommandResponse(true, "Count: " + count);
    }

    private CommandResponse handleCountGreaterThanPassportID(String passportID) {
        if (passportID == null || passportID.isEmpty()) {
            return new CommandResponse(false, "Invalid passport ID");
        }
        long count = collectionManager.countGreaterThanPassportID(passportID);
        return new CommandResponse(true, "Count: " + count);
    }

    private CommandResponse handlePrintFieldDescendingPassportID() {
        String result = collectionManager.printFieldDescendingPassportID();
        if (result.isEmpty()) {
            return new CommandResponse(true, "No passport IDs to display");
        }
        return new CommandResponse(true, result);
    }
}

