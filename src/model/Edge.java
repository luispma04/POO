package model;

/**
 * Represents an edge between two adjacent points on the grid.
 */
public class Edge {
    private final Point start;
    private final Point end;
    private final int cost;

    /**
     * Creates a new edge between two points with the given cost.
     *
     * @param start The starting point
     * @param end The ending point
     * @param cost The cost of traversing this edge
     */
    public Edge(Point start, Point end, int cost) {
        this.start = start;
        this.end = end;
        this.cost = cost;
    }

    /**
     * Gets the starting point.
     *
     * @return The starting point
     */
    public Point getStart() {
        return start;
    }

    /**
     * Gets the ending point.
     *
     * @return The ending point
     */
    public Point getEnd() {
        return end;
    }

    /**
     * Gets the cost of traversing this edge.
     *
     * @return The cost
     */
    public int getCost() {
        return cost;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Edge edge = (Edge) obj;
        return cost == edge.cost &&
                ((start.equals(edge.start) && end.equals(edge.end)) ||
                        (start.equals(edge.end) && end.equals(edge.start)));
    }

    @Override
    public int hashCode() {
        return start.hashCode() + end.hashCode();
    }

    @Override
    public String toString() {
        return start + " -> " + end + " (cost: " + cost + ")";
    }
}
