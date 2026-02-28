package axl.itmo.model;

/**
 * Represents 2D coordinates with X and Y components.
 */
public class Coordinates {
    /**
     * X coordinate value (must be less than or equal to 862 in input validation elsewhere).
     */
    private double x;
    /**
     * Y coordinate value.
     */
    private long y;

    /**
     * Creates a new immutable Coordinates instance.
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
}
