package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a path as a sequence of points.
 */
public class Path {
    private final List<Point> points;
    private int cost;
    private Grid grid; // Added grid reference

    /**
     * Creates a new empty path.
     */
    public Path() {
        this.points = new ArrayList<>();
        this.cost = 0;
        this.grid = null;
    }

    /**
     * Creates a new path as a copy of an existing path.
     *
     * @param other The path to copy
     */
    public Path(Path other) {
        this.points = new ArrayList<>(other.points);
        this.cost = other.cost;
        this.grid = other.grid;
    }

    /**
     * Creates a path from a prefix of an existing path.
     *
     * @param other        The original path
     * @param prefixLength The length of the prefix
     * @param grid         The grid for calculating costs
     */
    public Path(Path other, int prefixLength, Grid grid) {
        this.points = new ArrayList<>(other.points.subList(0, Math.min(prefixLength, other.points.size())));
        this.cost = calculateCost(grid);
        this.grid = grid;
    }

    /**
     * Adds a point to the path and updates the cost.
     *
     * @param point The point to add
     * @param grid  The grid for calculating the edge cost
     */
    public void addPoint(Point point, Grid grid) {
        // Store the grid reference
        if (this.grid == null && grid != null) {
            this.grid = grid;
        }

        if (!points.isEmpty()) {
            Point lastPoint = points.get(points.size() - 1);
            if (lastPoint.isAdjacent(point)) {
                points.add(point);
                cost += grid.getEdgeCost(lastPoint, point);

                // Check if the new point creates a cycle and remove it if it does
                int index = points.indexOf(point);
                if (index < points.size() - 1) {
                    // A cycle is found, remove it
                    List<Point> cyclePoints = new ArrayList<>(points.subList(index, points.size() - 1));
                    for (int i = 0; i < cyclePoints.size() - 1; i++) {
                        cost -= grid.getEdgeCost(cyclePoints.get(i), cyclePoints.get(i + 1));
                    }
                    points.subList(index, points.size() - 1).clear();
                }
            }
        } else {
            // First point in the path
            points.add(point);
        }
    }

    /**
     * Gets the list of points in the path.
     *
     * @return The list of points
     */
    public List<Point> getPoints() {
        return new ArrayList<>(points);
    }

    /**
     * Gets the last point in the path.
     *
     * @return The last point, or null if the path is empty
     */
    public Point getLastPoint() {
        return points.isEmpty() ? null : points.get(points.size() - 1);
    }

    /**
     * Gets the length of the path (number of edges).
     *
     * @return The length
     */
    public int getLength() {
        return points.size() - 1;
    }

    /**
     * Gets the cost of the path.
     *
     * @return The cost
     */
    public int getCost() {
        return cost;
    }

    /**
     * Gets the grid associated with this path.
     *
     * @return The grid
     */
    public Grid getGrid() {
        return grid;
    }

    /**
     * Calculates the cost of the path based on the grid.
     *
     * @param grid The grid
     * @return The calculated cost
     */
    private int calculateCost(Grid grid) {
        int totalCost = 0;
        for (int i = 0; i < points.size() - 1; i++) {
            totalCost += grid.getEdgeCost(points.get(i), points.get(i + 1));
        }
        return totalCost;
    }

    /**
     * Checks if this path contains the given point.
     *
     * @param point The point to check
     * @return True if the path contains the point, false otherwise
     */
    public boolean containsPoint(Point point) {
        return points.contains(point);
    }

    /**
     * Checks if this path has reached the final point.
     *
     * @param finalPoint The final point
     * @return True if the path has reached the final point, false otherwise
     */
    public boolean hasReachedFinalPoint(Point finalPoint) {
        return !points.isEmpty() && points.contains(finalPoint);
    }

    @Override
    public String toString() {
        return points.toString();
    }
}