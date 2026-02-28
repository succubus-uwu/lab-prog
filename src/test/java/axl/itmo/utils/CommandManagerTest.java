package axl.itmo.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CommandManagerTest {

    @Mock
    private CollectionManager collectionManager;
    @Mock
    private Console console;

    private CommandManager commandManager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(collectionManager.getCollection()).thenReturn(new LinkedList<>());
        commandManager = new CommandManager(collectionManager, console);
    }

    @Test
    void testRegisterCommands() {
        assertFalse(commandManager.getCommands().isEmpty());
        assertTrue(commandManager.getCommands().containsKey("help"));
        assertTrue(commandManager.getCommands().containsKey("add"));
    }

    @Test
    void testExecuteUnknownCommand() {
        commandManager.execute("unknown_command");
        verify(console).printError(contains("Unknown command"));
    }

    @Test
    void testExecuteHelp() {
        commandManager.execute("help");
        verify(console).printInfo("Available commands:");
    }

    @Test
    void testHistory() {
        commandManager.execute("help");
        commandManager.execute("info");
        assertEquals(2, commandManager.getHistory().size());
        assertEquals("help", commandManager.getHistory().get(0));
        assertEquals("info", commandManager.getHistory().get(1));
    }

    @Test
    void testHistoryLimit() {
        for (int i = 0; i < 10; i++) {
            commandManager.execute("cmd" + i);
        }
        assertEquals(8, commandManager.getHistory().size());
        assertEquals("cmd2", commandManager.getHistory().get(0));
    }
}
