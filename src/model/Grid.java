package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Represents the grid with obstacles and special cost zones.
 */
public class Grid {
    private final int width;
    private final int height;
    private final Point initialPoint;
    private final Point finalPoint;
    private final List<Point> obstacles;
    private final Map<EdgeKey, Integer> specialCosts;
    private final int maxCost;

    /**
     * Creates a new grid with the given dimensions and points.
     *
     * @param width The width of the grid
     * @param height The height of the grid
     * @param initialPoint The initial point
     * @param finalPoint The final point
     */
    public Grid(int width, int height, Point initialPoint, Point finalPoint) {
        this.width = width;
        this.height = height;
        this.initialPoint = initialPoint;
        this.finalPoint = finalPoint;
        this.obstacles = new ArrayList<>();
        this.specialCosts = new HashMap<>();
        this.maxCost = 1; // Default max cost
    }

    /**
     * Creates a new grid with obstacles and special cost zones.
     *
     * @param width The width of the grid
     * @param height The height of the grid
     * @param initialPoint The initial point
     * @param finalPoint The final point
     * @param obstacles The list of obstacle points
     * @param specialCostZones The list of special cost zones
     */
    public Grid(int width, int height, Point initialPoint, Point finalPoint,
                List<Point> obstacles, List<SpecialCostZone> specialCostZones) {
        this.width = width;
        this.height = height;
        this.initialPoint = initialPoint;
        this.finalPoint = finalPoint;
        this.obstacles = new ArrayList<>(obstacles);
        this.specialCosts = new HashMap<>();

        // Process special cost zones
        for (SpecialCostZone zone : specialCostZones) {
            processSpecialCostZone(zone);
        }

        // Calculate the maximum cost
        int max = 1; // Default edge cost
        for (int cost : specialCosts.values()) {
            max = Math.max(max, cost);
        }
        this.maxCost = max;
    }

    /**
     * Process a special cost zone to add its edges to the specialCosts map.
     *
     * @param zone The special cost zone
     */
    private void processSpecialCostZone(SpecialCostZone zone) {
        Point bottomLeft = zone.getBottomLeft();
        Point topRight = zone.getTopRight();
        int cost = zone.getCost();

        // Add horizontal edges at the bottom and top of the rectangle
        for (int x = bottomLeft.getX(); x < topRight.getX(); x++) {
            // Bottom edge
            Point p1 = new Point(x, bottomLeft.getY());
            Point p2 = new Point(x + 1, bottomLeft.getY());
            EdgeKey key = new EdgeKey(p1, p2);
            specialCosts.put(key, Math.max(specialCosts.getOrDefault(key, 1), cost));

            // Top edge
            p1 = new Point(x, topRight.getY());
            p2 = new Point(x + 1, topRight.getY());
            key = new EdgeKey(p1, p2);
            specialCosts.put(key, Math.max(specialCosts.getOrDefault(key, 1), cost));
        }

        // Add vertical edges at the left and right of the rectangle
        for (int y = bottomLeft.getY(); y < topRight.getY(); y++) {
            // Left edge
            Point p1 = new Point(bottomLeft.getX(), y);
            Point p2 = new Point(bottomLeft.getX(), y + 1);
            EdgeKey key = new EdgeKey(p1, p2);
            specialCosts.put(key, Math.max(specialCosts.getOrDefault(key, 1), cost));

            // Right edge
            p1 = new Point(topRight.getX(), y);
            p2 = new Point(topRight.getX(), y + 1);
            key = new EdgeKey(p1, p2);
            specialCosts.put(key, Math.max(specialCosts.getOrDefault(key, 1), cost));
        }
    }

    /**
     * Gets the width of the grid.
     *
     * @return The width
     */
    public int getWidth() {
        return width;
    }

    /**
     * Gets the height of the grid.
     *
     * @return The height
     */
    public int getHeight() {
        return height;
    }

    /**
     * Gets the initial point.
     *
     * @return The initial point
     */
    public Point getInitialPoint() {
        return initialPoint;
    }

    /**
     * Gets the final point.
     *
     * @return The final point
     */
    public Point getFinalPoint() {
        return finalPoint;
    }

    /**
     * Gets the list of obstacles.
     *
     * @return The list of obstacles
     */
    public List<Point> getObstacles() {
        return new ArrayList<>(obstacles);
    }

    /**
     * Gets the maximum cost of any edge in the grid.
     *
     * @return The maximum cost
     */
    public int getMaxCost() {
        return maxCost;
    }

    /**
     * Checks if the given point is within the grid boundaries.
     *
     * @param point The point to check
     * @return True if the point is within the grid, false otherwise
     */
    public boolean isWithinBounds(Point point) {
        return point.getX() >= 1 && point.getX() <= width &&
                point.getY() >= 1 && point.getY() <= height;
    }

    /**
     * Checks if the given point is an obstacle.
     *
     * @param point The point to check
     * @return True if the point is an obstacle, false otherwise
     */
    public boolean isObstacle(Point point) {
        return obstacles.contains(point);
    }

    /**
     * Gets the cost of traversing the edge between two adjacent points.
     *
     * @param p1 The first point
     * @param p2 The second point
     * @return The cost of the edge
     */
    public int getEdgeCost(Point p1, Point p2) {
        if (!p1.isAdjacent(p2)) {
            throw new IllegalArgumentException("Points must be adjacent");
        }

        EdgeKey key = new EdgeKey(p1, p2);
        return specialCosts.getOrDefault(key, 1); // Default cost is 1
    }

    /**
     * Gets a list of valid adjacent points from the given point.
     *
     * @param point The starting point
     * @return A list of valid adjacent points
     */
    public List<Point> getValidAdjacentPoints(Point point) {
        List<Point> adjacentPoints = new ArrayList<>();

        // Check in all four directions (North, East, South, West)
        int[][] directions = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};

        for (int[] dir : directions) {
            Point adjacentPoint = new Point(point.getX() + dir[0], point.getY() + dir[1]);

            if (isWithinBounds(adjacentPoint) && !isObstacle(adjacentPoint)) {
                adjacentPoints.add(adjacentPoint);
            }
        }

        return adjacentPoints;
    }

    /**
     * Generates random obstacles.
     *
     * @param numObstacles The number of obstacles to generate
     * @param random The random number generator
     */
    public void generateRandomObstacles(int numObstacles, Random random) {
        obstacles.clear();

        while (obstacles.size() < numObstacles) {
            int x = random.nextInt(width) + 1;
            int y = random.nextInt(height) + 1;
            Point obstacle = new Point(x, y);

            // Don't place obstacles at the initial or final points
            if (!obstacle.equals(initialPoint) && !obstacle.equals(finalPoint) && !obstacles.contains(obstacle)) {
                obstacles.add(obstacle);
            }
        }
    }

    /**
     * Generates random special cost zones.
     *
     * @param numZones The number of zones to generate
     * @param random The random number generator
     */
    public void generateRandomSpecialCostZones(int numZones, Random random) {
        specialCosts.clear();

        for (int i = 0; i < numZones; i++) {
            // Generate random rectangle coordinates
            int x1 = random.nextInt(width - 1) + 1;
            int y1 = random.nextInt(height - 1) + 1;
            int x2 = random.nextInt(width - x1) + x1 + 1;
            int y2 = random.nextInt(height - y1) + y1 + 1;

            // Generate random cost (between 2 and 10)
            int cost = random.nextInt(9) + 2;

            SpecialCostZone zone = new SpecialCostZone(new Point(x1, y1), new Point(x2, y2), cost);
            processSpecialCostZone(zone);
        }
    }

    /**
     * Helper class to store an edge key for the specialCosts map.
     */
    private static class EdgeKey {
        private final Point p1;
        private final Point p2;

        public EdgeKey(Point p1, Point p2) {
            // Ensure consistent ordering of points
            if (p1.getX() < p2.getX() || (p1.getX() == p2.getX() && p1.getY() < p2.getY())) {
                this.p1 = p1;
                this.p2 = p2;
            } else {
                this.p1 = p2;
                this.p2 = p1;
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;

            EdgeKey other = (EdgeKey) obj;
            return p1.equals(other.p1) && p2.equals(other.p2);
        }

        @Override
        public int hashCode() {
            return 31 * p1.hashCode() + p2.hashCode();
        }
    }

    /**
     * Represents a special cost zone as a rectangle with a cost.
     */
    public static class SpecialCostZone {
        private final Point bottomLeft;
        private final Point topRight;
        private final int cost;

        public SpecialCostZone(Point bottomLeft, Point topRight, int cost) {
            this.bottomLeft = bottomLeft;
            this.topRight = topRight;
            this.cost = cost;
        }

        public Point getBottomLeft() {
            return bottomLeft;
        }

        public Point getTopRight() {
            return topRight;
        }

        public int getCost() {
            return cost;
        }
    }
}