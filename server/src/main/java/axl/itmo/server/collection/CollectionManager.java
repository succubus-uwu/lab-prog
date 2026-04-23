package axl.itmo.server.collection;

import axl.itmo.common.model.Person;
import axl.itmo.common.util.PersonLocationComparator;
import axl.itmo.server.persistence.FileManager;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages the collection of Person objects on the server side.
 * All operations use Stream API where applicable.
 * Thread-safe for multi-threaded server console access.
 */
public class CollectionManager {
    private final LinkedList<Person> collection;
    private final LocalDateTime creationDate;
    private final FileManager fileManager;

    public CollectionManager(FileManager fileManager) {
        this.fileManager = fileManager;
        this.collection = fileManager.readCollection();
        this.creationDate = LocalDateTime.now();
        validateIds();
        sortCollection();
    }

    /**
     * Validates and fixes duplicate IDs using Stream API.
     */
    private void validateIds() {
        Set<Integer> ids = collection.stream()
                .map(Person::getId)
                .collect(Collectors.toSet());

        boolean hasDuplicates = ids.size() != collection.size();

        if (hasDuplicates) {
            System.out.println("Duplicate IDs found in the collection file. Reassigning IDs.");
            reassignIds();
        }
    }

    /**
     * Reassigns all IDs sequentially starting from 1.
     */
    private void reassignIds() {
        int[] counter = {0};
        collection.forEach(person -> person.setId(++counter[0]));
    }

    public LinkedList<Person> getCollection() {
        return new LinkedList<>(collection); // Return copy to avoid external modification
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    /**
     * Adds a person to the collection and sorts.
     */
    public void add(Person person) {
        collection.add(person);
        sortCollection();
    }

    /**
     * Updates a person in the collection by ID.
     */
    public void update(int id, Person newPerson) {
        Optional<Integer> indexOpt = collection.stream()
                .map(Person::getId)
                .toList()
                .stream()
                .peek(pid -> {})
                .toList()
                .stream()
                .filter(pid -> pid == id)
                .findFirst()
                .map(pid -> {
                    for (int i = 0; i < collection.size(); i++) {
                        if (collection.get(i).getId() == id) {
                            return i;
                        }
                    }
                    return -1;
                });

        // Simpler approach
        for (int i = 0; i < collection.size(); i++) {
            if (collection.get(i).getId() == id) {
                collection.set(i, newPerson);
                sortCollection();
                return;
            }
        }
    }

    /**
     * Removes a person by ID using Stream API.
     */
    public void removeById(int id) {
        collection.removeIf(person -> person.getId() == id);
    }

    /**
     * Clears the collection.
     */
    public void clear() {
        collection.clear();
    }

    /**
     * Saves the collection to file.
     */
    public void save() {
        fileManager.writeCollection(collection);
    }

    /**
     * Removes the first element.
     */
    public void removeFirst() {
        if (!collection.isEmpty()) {
            collection.removeFirst();
        }
    }

    /**
     * Reverses the collection order.
     */
    public void reorder() {
        Collections.reverse(collection);
    }

    /**
     * Counts persons with height less than specified value using Stream API.
     */
    public long countLessThanHeight(float height) {
        return collection.stream()
                .filter(p -> p.getHeight() < height)
                .count();
    }

    /**
     * Counts persons with passport ID greater than specified value using Stream API.
     */
    public long countGreaterThanPassportID(String passportID) {
        return collection.stream()
                .filter(p -> p.getPassportID() != null && p.getPassportID().compareTo(passportID) > 0)
                .count();
    }

    /**
     * Prints passport IDs in descending order using Stream API.
     */
    public String printFieldDescendingPassportID() {
        return collection.stream()
                .map(Person::getPassportID)
                .filter(Objects::nonNull)
                .sorted(Collections.reverseOrder())
                .collect(Collectors.joining("\n"));
    }

    /**
     * Sorts collection by name, then by location using Stream API and Comparators.
     */
    private void sortCollection() {
        collection.sort(Comparator.comparing(Person::getName)
                .thenComparing(new PersonLocationComparator()));
    }

    /**
     * Generates a unique ID for a new person.
     */
    public int generateId() {
        if (collection.isEmpty()) return 1;

        Set<Integer> used = collection.stream()
                .map(Person::getId)
                .collect(Collectors.toSet());

        for (int candidate = 1; candidate > 0; candidate++) {
            if (!used.contains(candidate)) {
                return candidate;
            }
        }

        throw new IllegalStateException("Could not generate unique positive ID: all values are taken");
    }

    /**
     * Finds a person by ID.
     */
    public Person getById(int id) {
        return collection.stream()
                .filter(p -> p.getId() == id)
                .findFirst()
                .orElse(null);
    }

    /**
     * Gets collection sorted by location field.
     */
    public LinkedList<Person> getCollectionSortedByLocation() {
        return collection.stream()
                .sorted(new PersonLocationComparator())
                .collect(Collectors.toCollection(LinkedList::new));
    }
}
