package axl.itmo.common.dto;

import axl.itmo.common.model.Person;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Data Transfer Object for command responses.
 * Contains response status, message, and optional collection data to be sent from server to client.
 */
public class CommandResponse implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 195434L;

    /**
     * Whether the command executed successfully.
     */
    private boolean success;

    /**
     * Response message (result or error description).
     */
    private String message;

    /**
     * Optional collection data (for commands that return collections, e.g., show).
     * Can contain Person objects or null if command doesn't return collection data.
     */
    private List<Person> data;

    /**
     * Default constructor for deserialization.
     */
    public CommandResponse() {
    }

    /**
     * Constructs a CommandResponse with status and message.
     *
     * @param success whether command succeeded
     * @param message response message
     */
    public CommandResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.data = null;
    }

    /**
     * Constructs a CommandResponse with status, message, and data.
     *
     * @param success whether command succeeded
     * @param message response message
     * @param data optional collection data
     */
    public CommandResponse(boolean success, String message, List<Person> data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Person> getData() {
        return data;
    }

    public void setData(List<Person> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "CommandResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", dataSize=" + (data != null ? data.size() : 0) +
                '}';
    }
}

