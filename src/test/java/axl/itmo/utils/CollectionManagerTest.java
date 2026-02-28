package axl.itmo.utils;

import axl.itmo.model.Coordinates;
import axl.itmo.model.Location;
import axl.itmo.model.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CollectionManagerTest {

    @Mock
    private FileManager fileManager;

    private CollectionManager collectionManager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(fileManager.readCollection()).thenReturn(new LinkedList<>());
        collectionManager = new CollectionManager(fileManager);
    }

    @Test
    void testAdd() {
        Person person = createTestPerson(1, "Alice");
        collectionManager.add(person);
        assertEquals(1, collectionManager.getCollection().size());
        assertEquals("Alice", collectionManager.getCollection().get(0).getName());
    }

    @Test
    void testUpdate() {
        Person person = createTestPerson(1, "Alice");
        collectionManager.add(person);

        Person updatedPerson = createTestPerson(1, "Bob");
        collectionManager.update(1, updatedPerson);

        assertEquals("Bob", collectionManager.getById(1).getName());
    }

    @Test
    void testRemoveById() {
        Person person = createTestPerson(1, "Alice");
        collectionManager.add(person);
        collectionManager.removeById(1);
        assertTrue(collectionManager.getCollection().isEmpty());
    }

    @Test
    void testClear() {
        collectionManager.add(createTestPerson(1, "Alice"));
        collectionManager.add(createTestPerson(2, "Bob"));
        collectionManager.clear();
        assertTrue(collectionManager.getCollection().isEmpty());
    }

    @Test
    void testRemoveFirst() {
        collectionManager.add(createTestPerson(1, "Alice"));
        collectionManager.add(createTestPerson(2, "Bob"));
        collectionManager.removeFirst();
        assertEquals(1, collectionManager.getCollection().size());
        assertEquals("Bob", collectionManager.getCollection().get(0).getName());
    }

    @Test
    void testReorder() {
        collectionManager.add(createTestPerson(1, "Alice"));
        collectionManager.add(createTestPerson(2, "Bob"));
        collectionManager.reorder();
        assertEquals("Bob", collectionManager.getCollection().get(0).getName());
        assertEquals("Alice", collectionManager.getCollection().get(1).getName());
    }

    @Test
    void testCountLessThanHeight() {
        Person p1 = createTestPerson(1, "Alice");
        p1.setHeight(160);
        Person p2 = createTestPerson(2, "Bob");
        p2.setHeight(180);
        collectionManager.add(p1);
        collectionManager.add(p2);

        assertEquals(1, collectionManager.countLessThanHeight(170));
    }

    @Test
    void testCountGreaterThanPassportID() {
        Person p1 = createTestPerson(1, "Alice");
        p1.setPassportID("A100");
        Person p2 = createTestPerson(2, "Bob");
        p2.setPassportID("B100");
        collectionManager.add(p1);
        collectionManager.add(p2);

        assertEquals(1, collectionManager.countGreaterThanPassportID("A500"));
    }

    @Test
    void testGenerateId() {
        assertEquals(1, collectionManager.generateId());
        collectionManager.add(createTestPerson(1, "Alice"));
        assertEquals(2, collectionManager.generateId());
    }

    private Person createTestPerson(int id, String name) {
        return new Person(id, name, new Coordinates(1, 1), LocalDateTime.now(), 170, new Date(), "P" + id, null, new Location(1, 1, 1, "Loc"));
    }
}
