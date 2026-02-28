package axl.itmo.model;

/**
 * Represents a location with x, y, z coordinates and a name.
 */
public class Location {
    /**
     * X coordinate value.
     */
    private float x;
    /**
     * Y coordinate value.
     */
    private float y;
    /**
     * Z coordinate value.
     */
    private float z;
    /**
     * Optional name of the location.
     */
    private String name;

    /**
     * Constructs a new location.
     *
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     * @param name location name
     */
    public Location(float x, float y, float z, String name) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.name = name;
    }

    /**
     * Returns x coordinate.
     * @return x value
     */
    public float getX() {
        return x;
    }

    /**
     * Returns y coordinate.
     * @return y value
     */
    public float getY() {
        return y;
    }

    /**
     * Returns z coordinate.
     * @return z value
     */
    public float getZ() {
        return z;
    }

    /**
     * Returns location name.
     * @return name or null
     */
    public String getName() {
        return name;
    }

    @Override
    /**
     * Returns a string representation of this location.
     * @return string with coordinates and name
     */
    public String toString() {
        return "Location{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", name='" + name + '\'' +
                '}';
    }
}
