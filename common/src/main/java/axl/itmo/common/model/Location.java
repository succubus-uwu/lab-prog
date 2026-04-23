package axl.itmo.common.model;

import java.io.Serial;
import java.io.Serializable;

/**
 * Represents a location with name, X, Y, and Z coordinates.
 */
public class Location implements Serializable {

    @Serial
    private static final long serialVersionUID = 165645343L;

    private String name;
    private double x;
    private long y;
    private double z;

    /**
     * Default constructor for deserialization.
     */
    public Location() {
    }

    /**
     * Creates a new Location.
     *
     * @param name location name
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     */
    public Location(String name, double x, long y, double z) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public long getY() {
        return y;
    }

    public void setY(long y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    @Override
    public String toString() {
        return "Location{" +
                "name='" + name + '\'' +
                ", x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return Double.compare(location.x, x) == 0 &&
                y == location.y &&
                Double.compare(location.z, z) == 0 &&
                java.util.Objects.equals(name, location.name);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(name, x, y, z);
    }
}

