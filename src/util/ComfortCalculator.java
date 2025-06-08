package util;

import model.Path;
import model.Point;

/**
 * Utility class for calculating comfort values.
 */
public class ComfortCalculator {
    /**
     * Calculates the comfort value for a path.
     *
     * @param path The path
     * @param currentPoint The current position
     * @param finalPoint The final point
     * @param maxCost The maximum edge cost
     * @param gridSize The sum of grid width and height
     * @param comfortSensitivity The comfort sensitivity parameter (k)
     * @return The calculated comfort value
     */
    public static double calculateComfort(Path path, Point currentPoint, Point finalPoint,
                                          int maxCost, int gridSize, double comfortSensitivity) {
        int pathLength = path.getLength();
        int pathCost = path.getCost();
        int distance = currentPoint.manhattanDistance(finalPoint);

        // Handle case where path length is 0 to avoid division by zero
        if (pathLength == 0) {
            return 0.0; // As per PDF, initial comfort is 0
        }

        // First component: cost efficiency
        double costComponent = 1 - (double) (pathCost - pathLength + 2) / ((maxCost - 1) * pathLength + 3);

        // Second component: distance to final point
        double distanceComponent = 1 - (double) distance / (gridSize + 1);

        // Final comfort calculation
        double comfort = Math.pow(costComponent, comfortSensitivity) * Math.pow(distanceComponent, comfortSensitivity);

        // Ensure comfort is between 0 and 1
        return Math.min(1.0, Math.max(0.0, comfort));
    }
}