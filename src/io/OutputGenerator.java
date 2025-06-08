package io;

import model.Grid;
import model.Individual;
import model.Point;
import visualization.GridVisualizer;

import java.util.List;
import java.util.Map;

/**
 * Handles generating formatted output for the simulation.
 */
public class OutputGenerator {
    // Flag to toggle grid visualization
    private static boolean pathVisualizationEnabled = false;

    /**
     * Enables or disables path visualization.
     *
     * @param enabled True to enable visualization, false to disable
     */
    public static void setPathVisualizationEnabled(boolean enabled) {
        pathVisualizationEnabled = enabled;
    }

    /**
     * Checks if path visualization is enabled.
     *
     * @return True if visualization is enabled, false otherwise
     */
    public static boolean isPathVisualizationEnabled() {
        return pathVisualizationEnabled;
    }

    /**
     * Prints the input parameters to the terminal.
     *
     * @param parameters The map of parameters
     */
    public static void printInputParameters(Map<String, Object> parameters) {
        int width = (int) parameters.get("width");
        int height = (int) parameters.get("height");
        Point initialPoint = (Point) parameters.get("initialPoint");
        Point finalPoint = (Point) parameters.get("finalPoint");
        int numSpecialCostZones = (int) parameters.get("numSpecialCostZones");
        int numObstacles = (int) parameters.get("numObstacles");
        double simulationTime = (double) parameters.get("simulationTime");
        int initialPopulation = (int) parameters.get("initialPopulation");
        int maxPopulation = (int) parameters.get("maxPopulation");
        double comfortSensitivity = (double) parameters.get("comfortSensitivity");
        double deathMean = (double) parameters.get("deathMean");
        double moveMean = (double) parameters.get("moveMean");
        double reproductionMean = (double) parameters.get("reproductionMean");

        // Print first line
        System.out.println(width + " " + height + " " +
                initialPoint.getX() + " " + initialPoint.getY() + " " +
                finalPoint.getX() + " " + finalPoint.getY() + " " +
                numSpecialCostZones + " " + numObstacles + " " +
                (int) simulationTime + " " + initialPopulation + " " + maxPopulation + " " +
                (int) comfortSensitivity + " " + (int) deathMean + " " + (int) moveMean + " " + (int) reproductionMean);

        // Print special cost zones
        if (numSpecialCostZones > 0) {
            System.out.println("special cost zones:");
            @SuppressWarnings("unchecked")
            List<Grid.SpecialCostZone> specialCostZones = (List<Grid.SpecialCostZone>) parameters.get("specialCostZones");

            for (Grid.SpecialCostZone zone : specialCostZones) {
                Point bottomLeft = zone.getBottomLeft();
                Point topRight = zone.getTopRight();
                int cost = zone.getCost();

                System.out.println(bottomLeft.getX() + " " + bottomLeft.getY() + " " +
                        topRight.getX() + " " + topRight.getY() + " " + cost);
            }
        }

        // Print obstacles
        if (numObstacles > 0) {
            System.out.println("obstacles:");
            @SuppressWarnings("unchecked")
            List<Point> obstacles = (List<Point>) parameters.get("obstacles");

            for (Point obstacle : obstacles) {
                System.out.println(obstacle.getX() + " " + obstacle.getY());
            }
        }

        // Add two line breaks
        System.out.println();
        System.out.println();
    }

    /**
     * Prints an observation of the population.
     *
     * @param observationNumber The observation number
     * @param time The current time
     * @param events The number of processed events
     * @param populationSize The size of the population
     * @param finalPointHit Whether the final point has been hit
     * @param bestIndividual The best individual
     * @param finalPoint The final point
     */
    public static void printObservation(int observationNumber, double time, int events,
                                        int populationSize, boolean finalPointHit,
                                        Individual bestIndividual, Point finalPoint) {
        System.out.println("Observation number: " + observationNumber);
        System.out.println("Present time: " + time);
        System.out.println("Number of realized events: " + events);
        System.out.println("Population size: " + populationSize);
        System.out.println("Final point has been hit: " + (finalPointHit ? "yes" : "no"));

        if (bestIndividual != null) {
            System.out.println("Path of the best fit individual: " + bestIndividual.getPath().getPoints());

            if (finalPointHit) {
                System.out.println("Cost: " + bestIndividual.getPath().getCost());
            } else {
                System.out.println("Comfort: " + bestIndividual.getComfort());
            }

            // Display grid visualization if enabled
            if (pathVisualizationEnabled && bestIndividual.getPath().getPoints().size() > 1) {
                Grid grid = bestIndividual.getPath().getGrid();
                if (grid != null) {
                    GridVisualizer.visualizeGrid(grid, bestIndividual);
                }
            }
        } else {
            System.out.println("Path of the best fit individual: []");
            System.out.println("Comfort: 0.0");
        }

        System.out.println();
    }

    /**
     * Prints the final result of the simulation.
     *
     * @param bestIndividual The best individual found during the simulation
     */
    public static void printFinalResult(Individual bestIndividual) {
        if (bestIndividual != null) {
            System.out.println("Best fit individual: " + bestIndividual.getPath().getPoints() +
                    " with cost " + bestIndividual.getPath().getCost());

            // Display final grid visualization if enabled
            if (pathVisualizationEnabled) {
                Grid grid = bestIndividual.getPath().getGrid();
                if (grid != null) {
                    GridVisualizer.visualizeGrid(grid, bestIndividual);
                }
            }
        } else {
            System.out.println("No solution found");
        }
    }
}