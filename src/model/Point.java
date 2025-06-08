package model;

/**
 * Represents a point on the grid with x,y coordinates.
 */
public class Point {
    private final int x;
    private final int y;

    /**
     * Creates a new point with the given coordinates.
     *
     * @param x The x-coordinate
     * @param y The y-coordinate
     */
    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Gets the x-coordinate.
     *
     * @return The x-coordinate
     */
    public int getX() {
        return x;
    }

    /**
     * Gets the y-coordinate.
     *
     * @return The y-coordinate
     */
    public int getY() {
        return y;
    }

    /**
     * Calculates the Manhattan distance to another point.
     *
     * @param other The other point
     * @return The Manhattan distance
     */
    public int manhattanDistance(Point other) {
        return Math.abs(this.x - other.x) + Math.abs(this.y - other.y);
    }

    /**
     * Checks if this point is adjacent to another point.
     *
     * @param other The other point
     * @return True if adjacent, false otherwise
     */
    public boolean isAdjacent(Point other) {
        return manhattanDistance(other) == 1;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Point point = (Point) obj;
        return x == point.x && y == point.y;
    }

    @Override
    public int hashCode() {
        return 31 * x + y;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}

