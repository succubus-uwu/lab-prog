package axl.itmo.server.command;

import axl.itmo.common.CommandNames;
import axl.itmo.common.dto.CommandRequest;
import axl.itmo.common.dto.CommandResponse;
import axl.itmo.common.model.Person;
import axl.itmo.common.model.User;
import axl.itmo.server.collection.CollectionManager;
import axl.itmo.server.net.PushNotificationService;
import axl.itmo.server.utils.AuthUtils;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.LinkedList;

public class CommandProcessor {
    private final CollectionManager collectionManager;
    private final AuthUtils authUtils;
    private PushNotificationService pushService;

    public CommandProcessor(CollectionManager collectionManager, AuthUtils authUtils) {
        this.collectionManager = collectionManager;
        this.authUtils = authUtils;
    }

    public void setPushService(PushNotificationService pushService) {
        this.pushService = pushService;
    }

    public CommandResponse process(CommandRequest request) {
        String commandName = request.getCommandName();
        Object argument = request.getArgument();

        try {
            CommandNames cmd = CommandNames.fromValue(commandName);
            if (cmd == null) {
                return new CommandResponse(false, "Unknown command: " + commandName);
            }

            User user = null;
            if (cmd != CommandNames.REGISTER && cmd != CommandNames.LOGIN && cmd != CommandNames.SUBSCRIBE) {
                user = authenticateUser(request);
                if (user == null) {
                    return new CommandResponse(false, "Authentication failed");
                }
            }

            switch (cmd) {
                case REGISTER:
                    return handleRegister(request.getLogin(), request.getPassword());
                case LOGIN:
                    return handleLogin(request.getLogin(), request.getPassword());
                case SUBSCRIBE:
                    return handleSubscribe(request.getLogin(), request.getPassword());
                case ADD:
                    return handleAdd((Person) argument, user);
                case UPDATE:
                    return handleUpdate((Person) argument, user);
                case REMOVE_BY_ID:
                    return handleRemoveById((Integer) argument, user);
                case CLEAR:
                    return handleClear(user);
                case SHOW:
                    return handleShow();
                case INFO:
                    return handleInfo();
                case REMOVE_FIRST:
                    return handleRemoveFirst(user);
                case REORDER:
                    return handleReorder();
                case SAVE:
                    collectionManager.save();
                    return new CommandResponse(true, "Collection saved");
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

    private User authenticateUser(CommandRequest request) {
        try {
            return authUtils.authenticateUser(request.getLogin(), request.getPassword());
        } catch (Exception e) {
            return null;
        }
    }

    private void broadcastUpdate() {
        if (pushService != null) {
            pushService.broadcast(collectionManager.getCollection());
        }
    }

    private CommandResponse handleRegister(String login, String password) {
        if (login == null || login.isEmpty() || password == null || password.isEmpty()) {
            return new CommandResponse(false, "Invalid login or password");
        }
        try {
            boolean success = authUtils.registerUser(login, password);
            if (success) {
                // Also authenticate to return userId immediately
                try {
                    User created = authUtils.authenticateUser(login, password);
                    return new CommandResponse(true, "User registered successfully:" + (created != null ? created.getId() : 0));
                } catch (Exception ignored) {}
                return new CommandResponse(true, "User registered successfully:0");
            } else {
                return new CommandResponse(false, "Login already exists");
            }
        } catch (Exception e) {
            return new CommandResponse(false, "Registration failed: " + e.getMessage());
        }
    }

    private CommandResponse handleLogin(String login, String password) {
        if (login == null || login.isEmpty() || password == null || password.isEmpty()) {
            return new CommandResponse(false, "Invalid login or password");
        }
        try {
            User user = authUtils.authenticateUser(login, password);
            if (user != null) {
                // Encode userId in message so GUI client can obtain it without extra round-trip
                return new CommandResponse(true, "Login successful:" + user.getId());
            } else {
                return new CommandResponse(false, "Invalid login or password");
            }
        } catch (Exception e) {
            return new CommandResponse(false, "Login failed: " + e.getMessage());
        }
    }

    private CommandResponse handleSubscribe(String login, String password) {
        if (login == null || login.isEmpty() || password == null || password.isEmpty()) {
            return new CommandResponse(false, "Invalid credentials for subscription");
        }
        try {
            User user = authUtils.authenticateUser(login, password);
            if (user == null) {
                return new CommandResponse(false, "Authentication failed");
            }
            // ACK — ServerApp will add the channel to PushNotificationService after sending this response
            return new CommandResponse(true, "subscribed");
        } catch (Exception e) {
            return new CommandResponse(false, "Subscription failed: " + e.getMessage());
        }
    }

    private CommandResponse handleAdd(Person person, User user) {
        if (person == null) {
            return new CommandResponse(false, "Invalid person data");
        }
        int newId = collectionManager.generateId();
        person.setId(newId);
        person.setCreationDate(LocalDateTime.now());
        person.setOwnerId(user.getId());
        try {
            collectionManager.add(person, user.getId());
            broadcastUpdate();
            return new CommandResponse(true, "Person added successfully with ID: " + newId);
        } catch (SQLException e) {
            return new CommandResponse(false, "Failed to add person: " + e.getMessage());
        }
    }

    private CommandResponse handleUpdate(Person person, User user) {
        if (person == null) {
            return new CommandResponse(false, "Invalid person data");
        }
        int id = person.getId();
        if (collectionManager.getById(id) == null) {
            return new CommandResponse(false, "Person with ID " + id + " not found");
        }
        person.setCreationDate(LocalDateTime.now());
        try {
            collectionManager.update(id, person, user.getId());
            broadcastUpdate();
            return new CommandResponse(true, "Person updated successfully");
        } catch (SQLException e) {
            return new CommandResponse(false, "Failed to update person: " + e.getMessage());
        }
    }

    private CommandResponse handleRemoveById(Integer id, User user) {
        if (id == null || id <= 0) {
            return new CommandResponse(false, "Invalid ID");
        }
        if (collectionManager.getById(id) == null) {
            return new CommandResponse(false, "Person with ID " + id + " not found");
        }
        try {
            collectionManager.removeById(id, user.getId());
            broadcastUpdate();
            return new CommandResponse(true, "Person removed successfully");
        } catch (SQLException e) {
            return new CommandResponse(false, "Failed to remove person: " + e.getMessage());
        }
    }

    private CommandResponse handleClear(User user) {
        try {
            collectionManager.clear(user.getId());
            broadcastUpdate();
            return new CommandResponse(true, "Your objects in the collection were cleared");
        } catch (SQLException e) {
            return new CommandResponse(false, "Failed to clear collection: " + e.getMessage());
        }
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

    private CommandResponse handleRemoveFirst(User user) {
        if (collectionManager.getCollection().isEmpty()) {
            return new CommandResponse(false, "Collection is empty");
        }
        try {
            collectionManager.removeFirst(user.getId());
            broadcastUpdate();
            return new CommandResponse(true, "First element removed");
        } catch (SQLException e) {
            return new CommandResponse(false, "Failed to remove first element: " + e.getMessage());
        }
    }

    private CommandResponse handleReorder() {
        collectionManager.reorder();
        broadcastUpdate();
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
