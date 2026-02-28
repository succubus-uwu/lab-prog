package axl.itmo.commands;

/**
 * Abstract base class for all commands.
 * Holds common metadata (name, description) and defines the execute contract.
 */
public abstract class Command {
    /**
     * Command keyword used to invoke this command.
     */
    private final String name;
    /**
     * Human-readable command description.
     */
    private final String description;

    /**
     * Constructs a command definition.
     *
     * @param name command name (keyword)
     * @param description brief description of the command
     */
    public Command(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /**
     * Executes the command.
     * @param argument The argument passed to the command.
     * @return true if the command executed successfully, false otherwise.
     */
    public abstract boolean execute(String argument);

    /**
     * Returns the command name.
     * @return command keyword
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the command description.
     * @return description text
     */
    public String getDescription() {
        return description;
    }

    @Override
    /**
     * Returns a formatted string with name and description.
     * @return string representation
     */
    public String toString() {
        return name + " : " + description;
    }
}
