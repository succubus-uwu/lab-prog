package axl.itmo.server.collection;

import axl.itmo.common.model.Person;
import axl.itmo.common.util.PersonLocationComparator;
import axl.itmo.server.persistence.DatabaseHandler;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * Manages the collection of Person objects on the server side.
 * All operations use Stream API where applicable.
 * Thread-safe for multi-threaded server console access.
 */
public class CollectionManager {
    private LinkedList<Person> collection;
    private final LocalDateTime creationDate;
    private final DatabaseHandler dbHandler;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public CollectionManager(DatabaseHandler dbHandler) {
        this.dbHandler = dbHandler;
        try {
            this.collection = dbHandler.loadPersons();
        } catch (SQLException e) {
            System.err.println("Error loading persons from database: " + e.getMessage());
            this.collection = new LinkedList<>();
        }
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
        lock.readLock().lock();
        try {
            return new LinkedList<>(collection); // Return copy to avoid external modification
        } finally {
            lock.readLock().unlock();
        }
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    /**
     * Adds a person to the collection and sorts.
     */
    public void add(Person person, long ownerId) throws SQLException {
        lock.writeLock().lock();
        try {
            dbHandler.savePerson(person, ownerId);
            person.setOwnerId(ownerId);
            collection.add(person);
            sortCollection();
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Updates a person in the collection by ID.
     */
    public void update(int id, Person newPerson, long ownerId) throws SQLException {
        lock.writeLock().lock();
        try {
            Person existing = getByIdUnsafe(id);
            if (existing == null) {
                throw new SQLException("Person not found");
            }
            if (existing.getOwnerId() != ownerId && ownerId != 0L) {
                throw new SQLException("Permission denied: You do not own this object.");
            }
            
            dbHandler.updatePerson(newPerson, ownerId);
            newPerson.setOwnerId(ownerId);
            
            for (int i = 0; i < collection.size(); i++) {
                if (collection.get(i).getId() == id) {
                    collection.set(i, newPerson);
                    sortCollection();
                    return;
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Removes a person by ID using Stream API.
     */
    public void removeById(int id, long ownerId) throws SQLException {
        lock.writeLock().lock();
        try {
            Person existing = getByIdUnsafe(id);
            if (existing == null) {
                throw new SQLException("Person not found");
            }
            if (existing.getOwnerId() != ownerId && ownerId != 0L) {
                throw new SQLException("Permission denied: You do not own this object.");
            }
            
            dbHandler.deletePerson(id, ownerId);
            collection.removeIf(person -> person.getId() == id);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Clears the collection for a specific user.
     */
    public void clear(long ownerId) throws SQLException {
        lock.writeLock().lock();
        try {
            dbHandler.deletePersonsByOwner(ownerId);
            collection.removeIf(person -> person.getOwnerId() == ownerId || ownerId == 0L);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Saves the collection to file.
     */
    public void save() {
        lock.writeLock().lock();
        try {
            dbHandler.savePersons(collection);
        } catch (SQLException e) {
            System.err.println("Error saving persons to database: " + e.getMessage());
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Removes the first element belonging to the user.
     */
    public void removeFirst(long ownerId) throws SQLException {
        lock.writeLock().lock();
        try {
            if (!collection.isEmpty()) {
                Person p = collection.getFirst();
                if (p.getOwnerId() == ownerId || ownerId == 0L) {
                    dbHandler.deletePerson(p.getId(), p.getOwnerId());
                    collection.removeFirst();
                } else {
                    throw new SQLException("Permission denied: You do not own the first object.");
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Reverses the collection order.
     */
    public void reorder() {
        lock.writeLock().lock();
        try {
            Collections.reverse(collection);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Counts persons with height less than specified value using Stream API.
     */
    public long countLessThanHeight(float height) {
        lock.readLock().lock();
        try {
            return collection.stream()
                    .filter(p -> p.getHeight() < height)
                    .count();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Counts persons with passport ID greater than specified value using Stream API.
     */
    public long countGreaterThanPassportID(String passportID) {
        lock.readLock().lock();
        try {
            return collection.stream()
                    .filter(p -> p.getPassportID() != null && p.getPassportID().compareTo(passportID) > 0)
                    .count();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Prints passport IDs in descending order using Stream API.
     */
    public String printFieldDescendingPassportID() {
        lock.readLock().lock();
        try {
            return collection.stream()
                    .map(Person::getPassportID)
                    .filter(Objects::nonNull)
                    .sorted(Collections.reverseOrder())
                    .collect(Collectors.joining("\n"));
        } finally {
            lock.readLock().unlock();
        }
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
        lock.readLock().lock();
        try {
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
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Finds a person by ID.
     */
    public Person getById(int id) {
        lock.readLock().lock();
        try {
            return getByIdUnsafe(id);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    private Person getByIdUnsafe(int id) {
        return collection.stream()
                .filter(p -> p.getId() == id)
                .findFirst()
                .orElse(null);
    }

    /**
     * Gets collection sorted by location field.
     */
    public LinkedList<Person> getCollectionSortedByLocation() {
        lock.readLock().lock();
        try {
            return collection.stream()
                    .sorted(new PersonLocationComparator())
                    .collect(Collectors.toCollection(LinkedList::new));
        } finally {
            lock.readLock().unlock();
        }
    }
}