package axl.itmo.common.dto;

import java.io.Serial;
import java.io.Serializable;

/**
 * Data Transfer Object for command requests.
 * Contains command name and argument(s) to be sent from client to server.
 */
public class CommandRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 195435L;

    /**
     * Name of the command to execute.
     */
    private String commandName;

    /**
     * The argument for the command (can be a Person object, String, primitive type, etc.).
     */
    private Object argument;

    /**
     * Default constructor for deserialization.
     */
    public CommandRequest() {
    }

    /**
     * Constructs a CommandRequest.
     *
     * @param commandName name of the command
     * @param argument command argument
     */
    public CommandRequest(String commandName, Object argument) {
        this.commandName = commandName;
        this.argument = argument;
    }

    public String getCommandName() {
        return commandName;
    }

    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }

    public Object getArgument() {
        return argument;
    }

    public void setArgument(Object argument) {
        this.argument = argument;
    }

    @Override
    public String toString() {
        return "CommandRequest{" +
                "commandName='" + commandName + '\'' +
                ", argument=" + argument +
                '}';
    }
}
