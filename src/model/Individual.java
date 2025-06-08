package model;

import util.ComfortCalculator;

import java.util.List;
import java.util.Random;

/**
 * Represents an individual in the simulation with a path and comfort value.
 */
public class Individual {
    private Path path;
    private double comfort;
    private boolean isDead;
    private final double creationTime;
    private final double deathTime;

    /**
     * Creates a new individual with a starting point at the given creation time.
     *
     * @param initialPoint The initial point
     * @param creationTime The time of creation
     * @param deathMean The mean value for the death event (μ)
     * @param comfortSensitivity The comfort sensitivity parameter (k)
     * @param random The random number generator
     */
    public Individual(Point initialPoint, double creationTime, double deathMean, double comfortSensitivity, Random random) {
        this.path = new Path();
        this.path.addPoint(initialPoint, null); // No cost for the first point
        this.comfort = 0.0; // Initial comfort as per PDF
        this.isDead = false;
        this.creationTime = creationTime;

        // Calculate death time - protect against NaN with safe defaults
        double mean;
        try {
            mean = (1 - Math.log(1 - comfort)) * deathMean;
            if (Double.isNaN(mean) || Double.isInfinite(mean) || mean <= 0) {
                mean = deathMean; // Default to deathMean if calculation fails
            }
        } catch (Exception e) {
            mean = deathMean; // Default to deathMean if calculation fails
        }

        // Calculate exponential random variable safely
        double randomValue = Math.max(0.00001, random.nextDouble()); // Avoid log(0)
        this.deathTime = creationTime + (-mean * Math.log(randomValue));
    }

    /**
     * Creates a new individual as a child of a parent individual.
     *
     * @param parent The parent individual
     * @param creationTime The time of creation
     * @param deathMean The mean value for the death event (μ)
     * @param comfortSensitivity The comfort sensitivity parameter (k)
     * @param grid The grid for path cost calculation
     * @param random The random number generator
     */
    public Individual(Individual parent, double creationTime, double deathMean, double comfortSensitivity, Grid grid, Random random) {
        this.isDead = false;
        this.creationTime = creationTime;

        // Create a prefix of the parent's path
        List<Point> parentPoints = parent.getPath().getPoints();
        int parentPathLength = parentPoints.size();

        // Calculate prefix length: 90% + (parent comfort * 10%)
        int prefixLength = Math.min(
                (int) Math.ceil(0.9 * parentPathLength + (parent.getComfort() * 0.1 * parentPathLength)),
                parentPathLength
        );

        // Ensure prefixLength is at least 1
        prefixLength = Math.max(1, prefixLength);

        this.path = new Path();
        for (int i = 0; i < prefixLength; i++) {
            if (i == 0) {
                this.path.addPoint(parentPoints.get(i), null); // First point has no cost
            } else {
                this.path.addPoint(parentPoints.get(i), grid);
            }
        }

        // Calculate comfort
        updateComfort(grid, comfortSensitivity);

        // Calculate death time - protect against NaN with safe defaults
        double mean;
        try {
            mean = (1 - Math.log(1 - comfort)) * deathMean;
            if (Double.isNaN(mean) || Double.isInfinite(mean) || mean <= 0) {
                mean = deathMean; // Default to deathMean if calculation fails
            }
        } catch (Exception e) {
            mean = deathMean; // Default to deathMean if calculation fails
        }

        // Calculate exponential random variable safely
        double randomValue = Math.max(0.00001, random.nextDouble()); // Avoid log(0)
        this.deathTime = creationTime + (-mean * Math.log(randomValue));
    }

    /**
     * Gets the path of this individual.
     *
     * @return The path
     */
    public Path getPath() {
        return path;
    }

    /**
     * Gets the comfort value of this individual.
     *
     * @return The comfort value
     */
    public double getComfort() {
        return comfort;
    }

    /**
     * Checks if this individual is dead.
     *
     * @return True if dead, false otherwise
     */
    public boolean isDead() {
        return isDead;
    }

    /**
     * Marks this individual as dead.
     */
    public void kill() {
        this.isDead = true;
    }

    /**
     * Gets the creation time of this individual.
     *
     * @return The creation time
     */
    public double getCreationTime() {
        return creationTime;
    }

    /**
     * Gets the death time of this individual.
     *
     * @return The death time
     */
    public double getDeathTime() {
        return deathTime;
    }

    /**
     * Moves this individual to a random adjacent point.
     *
     * @param grid The grid
     * @param comfortSensitivity The comfort sensitivity parameter (k)
     * @param random The random number generator
     */
    public void move(Grid grid, double comfortSensitivity, Random random) {
        if (isDead) return;

        Point currentPosition = path.getLastPoint();
        List<Point> validMoves = grid.getValidAdjacentPoints(currentPosition);

        if (!validMoves.isEmpty()) {
            // Choose a random direction
            int direction = random.nextInt(validMoves.size());
            Point nextPosition = validMoves.get(direction);

            // Add the point to the path
            path.addPoint(nextPosition, grid);

            // Update comfort
            updateComfort(grid, comfortSensitivity);
        }
    }

    /**
     * Updates the comfort value of this individual.
     *
     * @param comfortSensitivity The comfort sensitivity parameter (k)
     */
    private void updateComfort(double comfortSensitivity) {
        // Used for initial comfort calculation (no path length yet)
        this.comfort = 0.0;
    }

    /**
     * Updates the comfort value of this individual based on its path.
     *
     * @param grid The grid
     * @param comfortSensitivity The comfort sensitivity parameter (k)
     */
    private void updateComfort(Grid grid, double comfortSensitivity) {
        Point lastPoint = path.getLastPoint();
        Point finalPoint = grid.getFinalPoint();

        // Calculate comfort only if the path has more than one point
        if (path.getLength() > 0) {
            this.comfort = ComfortCalculator.calculateComfort(
                    path,
                    lastPoint,
                    finalPoint,
                    grid.getMaxCost(),
                    grid.getWidth() + grid.getHeight(),
                    comfortSensitivity
            );
        } else {
            this.comfort = 0.0;
        }
    }

    /**
     * Calculates the next move time based on comfort and move mean.
     *
     * @param currentTime The current time
     * @param moveMean The mean value for the move event (δ)
     * @param random The random number generator
     * @return The next move time
     */
    public double calculateNextMoveTime(double currentTime, double moveMean, Random random) {
        // Calculate mean safely
        double mean;
        try {
            mean = (1 - Math.log(comfort)) * moveMean;
            if (Double.isNaN(mean) || Double.isInfinite(mean) || mean <= 0) {
                mean = moveMean; // Default to moveMean if calculation fails
            }
        } catch (Exception e) {
            mean = moveMean; // Default to moveMean if calculation fails
        }

        // Calculate exponential random variable safely
        double randomValue = Math.max(0.00001, random.nextDouble()); // Avoid log(0)
        return currentTime + (-mean * Math.log(randomValue));
    }

    /**
     * Calculates the next reproduction time based on comfort and reproduction mean.
     *
     * @param currentTime The current time
     * @param reproductionMean The mean value for the reproduction event (ρ)
     * @param random The random number generator
     * @return The next reproduction time
     */
    public double calculateNextReproductionTime(double currentTime, double reproductionMean, Random random) {
        // Calculate mean safely
        double mean;
        try {
            mean = (1 - Math.log(comfort)) * reproductionMean;
            if (Double.isNaN(mean) || Double.isInfinite(mean) || mean <= 0) {
                mean = reproductionMean; // Default to reproductionMean if calculation fails
            }
        } catch (Exception e) {
            mean = reproductionMean; // Default to reproductionMean if calculation fails
        }

        // Calculate exponential random variable safely
        double randomValue = Math.max(0.00001, random.nextDouble()); // Avoid log(0)
        return currentTime + (-mean * Math.log(randomValue));
    }

    /**
     * Checks if this individual has reached the final point.
     *
     * @param finalPoint The final point
     * @return True if the path has reached the final point, false otherwise
     */
    public boolean hasReachedFinalPoint(Point finalPoint) {
        return path.containsPoint(finalPoint);
    }

    @Override
    public String toString() {
        return "Individual{path=" + path + ", comfort=" + comfort + ", creationTime=" + creationTime + ", deathTime=" + deathTime + "}";
    }
}