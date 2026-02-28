package axl.itmo.utils;

import axl.itmo.model.Person;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Manages the collection of Person objects.
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

    private void validateIds() {
        Set<Integer> ids = new HashSet<>();
        boolean hasDuplicates = false;
        for (Person person : collection) {
            if (ids.contains(person.getId())) {
                hasDuplicates = true;
                break;
            }
            ids.add(person.getId());
        }

        if (hasDuplicates) {
            System.out.println("\u001B[31mDuplicate IDs found in the collection file.\u001B[0m");
            System.out.println("Do you want to reassign IDs automatically? (yes/no)");
            Scanner scanner = new Scanner(System.in);
            if (scanner.hasNextLine()) {
                String input = scanner.nextLine().trim().toLowerCase();
                if ("yes".equals(input) || "y".equals(input)) {
                    reassignIds();
                    System.out.println("\u001B[32mIDs reassigned successfully.\u001B[0m");
                } else {
                    System.out.println("\u001B[31mExiting program due to duplicate IDs.\u001B[0m");
                    System.exit(1);
                }
            } else {
                System.out.println("\u001B[31mNo input available. Exiting program due to duplicate IDs.\u001B[0m");
                System.exit(1);
            }
        }
    }

    private void reassignIds() {
        int id = 1;
        for (Person person : collection) {
            person.setId(id++);
        }
    }

    public LinkedList<Person> getCollection() {
        return collection;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void add(Person person) {
        collection.add(person);
        sortCollection();
    }

    public void update(int id, Person newPerson) {
        for (int i = 0; i < collection.size(); i++) {
            if (collection.get(i).getId() == id) {
                collection.set(i, newPerson);
                sortCollection();
                return;
            }
        }
    }

    public void removeById(int id) {
        collection.removeIf(person -> person.getId() == id);
    }

    public void clear() {
        collection.clear();
    }

    public void save() {
        fileManager.writeCollection(collection);
    }

    public void removeFirst() {
        if (!collection.isEmpty()) {
            collection.removeFirst();
        }
    }

    public void reorder() {
        Collections.reverse(collection);
    }

    public long countLessThanHeight(float height) {
        return collection.stream().filter(p -> p.getHeight() < height).count();
    }

    public long countGreaterThanPassportID(String passportID) {
        return collection.stream()
                .filter(p -> p.getPassportID() != null && p.getPassportID().compareTo(passportID) > 0)
                .count();
    }

    public void printFieldDescendingPassportID() {
        collection.stream()
                .map(Person::getPassportID)
                .filter(Objects::nonNull)
                .sorted(Collections.reverseOrder())
                .forEach(System.out::println);
    }

    private void sortCollection() {
        Collections.sort(collection);
    }

    public int generateId() {
        if (collection.isEmpty()) return 1;

        HashSet<Integer> used = new HashSet<>();
        for (Person p : collection) {
            int id = p.getId();
            if (id > 0) used.add(id);
        }

        for (int candidate = 1; candidate > 0; candidate++) {
            if (!used.contains(candidate)) {
                return candidate;
            }
        }

        throw new IllegalStateException("Не удалось сгенерировать уникальный положительный ID: все значения заняты");
    }
    
    public Person getById(int id) {
        return collection.stream().filter(p -> p.getId() == id).findFirst().orElse(null);
    }
}
