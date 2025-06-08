package simulation;

import io.OutputGenerator;
import model.Grid;
import model.Individual;
import model.Point;
import model.Population;

import java.util.Random;

/**
 * Main simulation engine for the pathfinder problem.
 */
public class Simulator {
    private final Grid grid;
    private final Population population;
    private final PEC pec;
    private final Random random;
    private final double comfortSensitivity;
    private final double deathMean;
    private final double moveMean;
    private final double reproductionMean;
    private final double simulationTime;
    private double currentTime;
    private int observationCount;

    /**
     * Creates a new simulator with the given parameters.
     *
     * @param grid The grid
     * @param initialPopulation The initial population size (ν)
     * @param maxPopulation The maximum population size (νmax)
     * @param comfortSensitivity The comfort sensitivity parameter (k)
     * @param deathMean The mean value for the death event (μ)
     * @param moveMean The mean value for the move event (δ)
     * @param reproductionMean The mean value for the reproduction event (ρ)
     * @param simulationTime The final time of the simulation (τ)
     */
    public Simulator(Grid grid, int initialPopulation, int maxPopulation,
                     double comfortSensitivity, double deathMean, double moveMean,
                     double reproductionMean, double simulationTime) {
        this.grid = grid;
        this.random = new Random();
        this.comfortSensitivity = comfortSensitivity;
        this.deathMean = deathMean;
        this.moveMean = moveMean;
        this.reproductionMean = reproductionMean;
        this.simulationTime = simulationTime;
        this.currentTime = 0.0;
        this.observationCount = 0;

        // Initialize PEC
        this.pec = new PEC();

        // Initialize population
        this.population = new Population(grid, initialPopulation, maxPopulation, comfortSensitivity, deathMean, random, currentTime);

        // Schedule initial events for each individual
        for (Individual individual : population.getIndividuals()) {
            scheduleInitialEvents(individual);
        }
    }

    /**
     * Schedules the initial events (death, first move, first reproduction) for a new individual.
     *
     * @param individual The individual
     */
    private void scheduleInitialEvents(Individual individual) {
        // Schedule death event
        if (individual.getDeathTime() <= simulationTime) {
            DeathEvent deathEvent = new DeathEvent(individual.getDeathTime(), individual);
            pec.addEvent(deathEvent);
        }

        // Schedule first move
        double firstMoveTime = individual.calculateNextMoveTime(currentTime, moveMean, random);
        if (firstMoveTime <= simulationTime) {
            MoveEvent moveEvent = new MoveEvent(firstMoveTime, individual, 1);
            pec.addEvent(moveEvent);
        }

        // Schedule first reproduction
        double firstReproductionTime = individual.calculateNextReproductionTime(currentTime, reproductionMean, random);
        if (firstReproductionTime <= simulationTime) {
            ReproductionEvent reproductionEvent = new ReproductionEvent(firstReproductionTime, individual, 1);
            pec.addEvent(reproductionEvent);
        }
    }

    /**
     * Runs the simulation until the final time or until there are no more events.
     *
     * @return The best individual found during the simulation
     */
    public Individual simulate() {
        // Print initial observation (time 0) - this is observation 0
        observePopulation();

        // Main simulation loop
        while (!pec.isEmpty()) {
            Event event = pec.nextEvent();

            // If event time exceeds simulation time, exit loop
            if (event.getTime() > simulationTime) {
                break;
            }

            // Update current time
            currentTime = event.getTime();

            // Process the event
            event.process(this);

            // Remove dead individuals
            population.removeDeadIndividuals();

            // Check if it's time for an observation
            // We need observations at τ/20, 2τ/20, 3τ/20, ..., 19τ/20, 20τ/20
            if (observationCount <= 19 && currentTime >= observationCount * (simulationTime / 20)) {
                observePopulation();
            }
        }

        // Final observation at time τ (observation 20) - ensure we always get observation 20
        if (observationCount <= 20) {
            currentTime = simulationTime;
            observePopulation();
        }

        // Return the best individual found during the simulation
        return population.getBestIndividualOverall();
    }

    /**
     * Observes the current state of the population and prints it.
     */
    private void observePopulation() {
        Individual bestIndividual = population.getBestIndividual();
        boolean reachedFinal = bestIndividual != null &&
                bestIndividual.hasReachedFinalPoint(grid.getFinalPoint());

        OutputGenerator.printObservation(
                observationCount,
                currentTime,
                pec.getProcessedEvents(),
                population.getSize(),
                reachedFinal,
                bestIndividual,
                grid.getFinalPoint()
        );

        observationCount++;
    }

    /**
     * Gets the grid.
     *
     * @return The grid
     */
    public Grid getGrid() {
        return grid;
    }

    /**
     * Gets the population.
     *
     * @return The population
     */
    public Population getPopulation() {
        return population;
    }

    /**
     * Gets the PEC.
     *
     * @return The PEC
     */
    public PEC getPEC() {
        return pec;
    }

    /**
     * Gets the random number generator.
     *
     * @return The random number generator
     */
    public Random getRandom() {
        return random;
    }

    /**
     * Gets the comfort sensitivity parameter (k).
     *
     * @return The comfort sensitivity
     */
    public double getComfortSensitivity() {
        return comfortSensitivity;
    }

    /**
     * Gets the mean value for the death event (μ).
     *
     * @return The death mean
     */
    public double getDeathMean() {
        return deathMean;
    }

    /**
     * Gets the mean value for the move event (δ).
     *
     * @return The move mean
     */
    public double getMoveMean() {
        return moveMean;
    }

    /**
     * Gets the mean value for the reproduction event (ρ).
     *
     * @return The reproduction mean
     */
    public double getReproductionMean() {
        return reproductionMean;
    }

    /**
     * Gets the final time of the simulation (τ).
     *
     * @return The simulation time
     */
    public double getSimulationTime() {
        return simulationTime;
    }

    /**
     * Gets the current time of the simulation.
     *
     * @return The current time
     */
    public double getCurrentTime() {
        return currentTime;
    }
}
