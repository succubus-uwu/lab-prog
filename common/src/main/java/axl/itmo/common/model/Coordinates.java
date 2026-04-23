package axl.itmo.common.model;

import java.io.Serial;
import java.io.Serializable;

/**
 * Represents 2D coordinates with X and Y components.
 */
public class Coordinates implements Serializable {

    @Serial
    private static final long serialVersionUID = 16754323235456L;

    /**
     * X coordinate value (must be less than or equal to 862 in input validation elsewhere).
     */
    private double x;
    /**
     * Y coordinate value.
     */
    private long y;

    /**
     * Creates a new Coordinates instance.
     *
     * @param x the X coordinate value
     * @param y the Y coordinate value
     */
    public Coordinates(double x, long y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Returns the X coordinate.
     *
     * @return X value
     */
    public double getX() {
        return x;
    }

    /**
     * Returns the Y coordinate.
     *
     * @return Y value
     */
    public long getY() {
        return y;
    }

    @Override
    public String toString() {
        return "Coordinates{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coordinates that = (Coordinates) o;
        return Double.compare(that.x, x) == 0 && y == that.y;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(x, y);
    }
}

