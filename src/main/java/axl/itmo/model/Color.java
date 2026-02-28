package axl.itmo.model;

/**
 * Enum representing eye color.
 */
public enum Color {
    /** Green eyes. */
    GREEN,
    /** Black eyes. */
    BLACK,
    /** Orange eyes. */
    ORANGE,
    /** Brown eyes. */
    BROWN;

    /**
     * Returns a comma-separated list of all colors.
     *
     * @return String containing all enum values
     */
    public static String nameList() {
        StringBuilder nameList = new StringBuilder();
        for (Color color : values()) {
            nameList.append(color.name()).append(", ");
        }
        return nameList.substring(0, nameList.length() - 2);
    }
}
