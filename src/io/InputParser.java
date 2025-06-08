package io;

import model.Grid;
import model.Point;
import util.RandomGenerator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Handles parsing input from command line arguments or files.
 */
public class InputParser {
    /**
     * Parses command line arguments for random parameter generation.
     *
     * @param args The command line arguments
     * @return A map of parsed parameters
     */
    public static Map<String, Object> parseRandomParameters(String[] args) {
        if (args.length < 14) {
            throw new IllegalArgumentException("Insufficient arguments for random generation");
        }

        Map<String, Object> parameters = new HashMap<>();
        Random random = new Random();

        try {
            // Parse grid dimensions and points
            int width = Integer.parseInt(args[1]);
            int height = Integer.parseInt(args[2]);
            int xi = Integer.parseInt(args[3]);
            int yi = Integer.parseInt(args[4]);
            int xf = Integer.parseInt(args[5]);
            int yf = Integer.parseInt(args[6]);
            int numSpecialCostZones = Integer.parseInt(args[7]);
            int numObstacles = Integer.parseInt(args[8]);
            double simulationTime = Double.parseDouble(args[9]);
            int initialPopulation = Integer.parseInt(args[10]);
            int maxPopulation = Integer.parseInt(args[11]);
            double comfortSensitivity = Double.parseDouble(args[12]);
            double deathMean = Double.parseDouble(args[13]);
            double moveMean = Double.parseDouble(args[14]);
            double reproductionMean = Double.parseDouble(args[15]);

            // Validate parameters
            validateParameters(width, height, xi, yi, xf, yf, numSpecialCostZones, numObstacles,
                    simulationTime, initialPopulation, maxPopulation, comfortSensitivity,
                    deathMean, moveMean, reproductionMean);

            // Create points
            Point initialPoint = new Point(xi, yi);
            Point finalPoint = new Point(xf, yf);

            // Generate random obstacles and special cost zones
            List<Point> obstacles = RandomGenerator.generateRandomObstacles(
                    numObstacles, width, height, initialPoint, finalPoint, random
            );

            List<Grid.SpecialCostZone> specialCostZones = RandomGenerator.generateRandomSpecialCostZones(
                    numSpecialCostZones, width, height, random
            );

            // Create grid
            Grid grid = new Grid(width, height, initialPoint, finalPoint, obstacles, specialCostZones);

            // Store parameters
            parameters.put("width", width);
            parameters.put("height", height);
            parameters.put("initialPoint", initialPoint);
            parameters.put("finalPoint", finalPoint);
            parameters.put("numSpecialCostZones", numSpecialCostZones);
            parameters.put("numObstacles", numObstacles);
            parameters.put("simulationTime", simulationTime);
            parameters.put("initialPopulation", initialPopulation);
            parameters.put("maxPopulation", maxPopulation);
            parameters.put("comfortSensitivity", comfortSensitivity);
            parameters.put("deathMean", deathMean);
            parameters.put("moveMean", moveMean);
            parameters.put("reproductionMean", reproductionMean);
            parameters.put("obstacles", obstacles);
            parameters.put("specialCostZones", specialCostZones);
            parameters.put("grid", grid);

            return parameters;

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number format in command line arguments", e);
        }
    }

    /**
     * Parses a file for simulation parameters.
     *
     * @param filePath The path to the file
     * @return A map of parsed parameters
     * @throws IOException If an I/O error occurs
     */
    public static Map<String, Object> parseFile(String filePath) throws IOException {
        Map<String, Object> parameters = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line = reader.readLine().trim();
            String[] tokens = line.split("\\s+");

            if (tokens.length < 15) {
                throw new IllegalArgumentException("Insufficient parameters in file");
            }

            // Parse basic parameters
            int width = Integer.parseInt(tokens[0]);
            int height = Integer.parseInt(tokens[1]);
            int xi = Integer.parseInt(tokens[2]);
            int yi = Integer.parseInt(tokens[3]);
            int xf = Integer.parseInt(tokens[4]);
            int yf = Integer.parseInt(tokens[5]);
            int numSpecialCostZones = Integer.parseInt(tokens[6]);
            int numObstacles = Integer.parseInt(tokens[7]);
            double simulationTime = Double.parseDouble(tokens[8]);
            int initialPopulation = Integer.parseInt(tokens[9]);
            int maxPopulation = Integer.parseInt(tokens[10]);
            double comfortSensitivity = Double.parseDouble(tokens[11]);
            double deathMean = Double.parseDouble(tokens[12]);
            double moveMean = Double.parseDouble(tokens[13]);
            double reproductionMean = Double.parseDouble(tokens[14]);

            // Validate parameters
            validateParameters(width, height, xi, yi, xf, yf, numSpecialCostZones, numObstacles,
                    simulationTime, initialPopulation, maxPopulation, comfortSensitivity,
                    deathMean, moveMean, reproductionMean);

            // Create points
            Point initialPoint = new Point(xi, yi);
            Point finalPoint = new Point(xf, yf);

            // Parse special cost zones
            List<Grid.SpecialCostZone> specialCostZones = new ArrayList<>();
            if (numSpecialCostZones > 0) {
                // Skip the header line
                line = reader.readLine().trim();
                if (!line.equals("special cost zones:")) {
                    throw new IllegalArgumentException("Expected 'special cost zones:' but found: " + line);
                }

                for (int i = 0; i < numSpecialCostZones; i++) {
                    line = reader.readLine().trim();
                    tokens = line.split("\\s+");

                    if (tokens.length < 5) {
                        throw new IllegalArgumentException("Invalid special cost zone format");
                    }

                    int x1 = Integer.parseInt(tokens[0]);
                    int y1 = Integer.parseInt(tokens[1]);
                    int x2 = Integer.parseInt(tokens[2]);
                    int y2 = Integer.parseInt(tokens[3]);
                    int cost = Integer.parseInt(tokens[4]);

                    Point bottomLeft = new Point(x1, y1);
                    Point topRight = new Point(x2, y2);

                    specialCostZones.add(new Grid.SpecialCostZone(bottomLeft, topRight, cost));
                }
            }

            // Parse obstacles
            List<Point> obstacles = new ArrayList<>();
            if (numObstacles > 0) {
                // Skip the header line
                line = reader.readLine().trim();
                if (!line.equals("obstacles:")) {
                    throw new IllegalArgumentException("Expected 'obstacles:' but found: " + line);
                }

                for (int i = 0; i < numObstacles; i++) {
                    line = reader.readLine().trim();
                    tokens = line.split("\\s+");

                    if (tokens.length < 2) {
                        throw new IllegalArgumentException("Invalid obstacle format");
                    }

                    int x = Integer.parseInt(tokens[0]);
                    int y = Integer.parseInt(tokens[1]);

                    obstacles.add(new Point(x, y));
                }
            }

            // Create grid
            Grid grid = new Grid(width, height, initialPoint, finalPoint, obstacles, specialCostZones);

            // Store parameters
            parameters.put("width", width);
            parameters.put("height", height);
            parameters.put("initialPoint", initialPoint);
            parameters.put("finalPoint", finalPoint);
            parameters.put("numSpecialCostZones", numSpecialCostZones);
            parameters.put("numObstacles", numObstacles);
            parameters.put("simulationTime", simulationTime);
            parameters.put("initialPopulation", initialPopulation);
            parameters.put("maxPopulation", maxPopulation);
            parameters.put("comfortSensitivity", comfortSensitivity);
            parameters.put("deathMean", deathMean);
            parameters.put("moveMean", moveMean);
            parameters.put("reproductionMean", reproductionMean);
            parameters.put("obstacles", obstacles);
            parameters.put("specialCostZones", specialCostZones);
            parameters.put("grid", grid);
            parameters.put("filePath", filePath);

            return parameters;

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number format in file", e);
        }
    }

    /**
     * Validates the input parameters.
     *
     * @param width The grid width
     * @param height The grid height
     * @param xi The initial point x-coordinate
     * @param yi The initial point y-coordinate
     * @param xf The final point x-coordinate
     * @param yf The final point y-coordinate
     * @param numSpecialCostZones The number of special cost zones
     * @param numObstacles The number of obstacles
     * @param simulationTime The simulation time
     * @param initialPopulation The initial population size
     * @param maxPopulation The maximum population size
     * @param comfortSensitivity The comfort sensitivity parameter
     * @param deathMean The mean value for the death event
     * @param moveMean The mean value for the move event
     * @param reproductionMean The mean value for the reproduction event
     */
    private static void validateParameters(int width, int height, int xi, int yi, int xf, int yf,
                                           int numSpecialCostZones, int numObstacles, double simulationTime,
                                           int initialPopulation, int maxPopulation, double comfortSensitivity,
                                           double deathMean, double moveMean, double reproductionMean) {
        // Check basic constraints
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Grid dimensions must be positive");
        }

        if (xi < 1 || xi > width || yi < 1 || yi > height) {
            throw new IllegalArgumentException("Initial point must be within grid boundaries");
        }

        if (xf < 1 || xf > width || yf < 1 || yf > height) {
            throw new IllegalArgumentException("Final point must be within grid boundaries");
        }

        if (numSpecialCostZones < 0 || numObstacles < 0) {
            throw new IllegalArgumentException("Number of special cost zones and obstacles must be non-negative");
        }

        if (simulationTime <= 0) {
            throw new IllegalArgumentException("Simulation time must be positive");
        }

        if (initialPopulation <= 0 || maxPopulation <= 0 || initialPopulation > maxPopulation) {
            throw new IllegalArgumentException("Invalid population parameters");
        }

        if (comfortSensitivity <= 0 || deathMean <= 0 || moveMean <= 0 || reproductionMean <= 0) {
            throw new IllegalArgumentException("Event mean values and comfort sensitivity must be positive");
        }
    }
}
