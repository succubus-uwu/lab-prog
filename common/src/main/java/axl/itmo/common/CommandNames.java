package axl.itmo.common;

/**
 * Enum containing all available command names.
 */
public enum CommandNames {
    ADD("add"),
    UPDATE("update"),
    REMOVE_BY_ID("remove_by_id"),
    CLEAR("clear"),
    SHOW("show"),
    INFO("info"),
    REMOVE_FIRST("remove_first"),
    REORDER("reorder"),
    EXECUTE_SCRIPT("execute_script"),
    EXIT("exit"),
    HELP("help"),
    HISTORY("history"),
    COUNT_LESS_THAN_HEIGHT("count_less_than_height"),
    COUNT_GREATER_THAN_PASSPORT_ID("count_greater_than_passport_id"),
    PRINT_FIELD_DESCENDING_PASSPORT_ID("print_field_descending_passport_id"),
    SAVE("save");

    private final String value;

    CommandNames(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Gets CommandNames by value string.
     *
     * @param value the command name string
     * @return the corresponding CommandNames or null if not found
     */
    public static CommandNames fromValue(String value) {
        for (CommandNames cmd : CommandNames.values()) {
            if (cmd.value.equals(value)) {
                return cmd;
            }
        }
        return null;
    }
}
