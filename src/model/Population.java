package model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Manages a collection of individuals in the simulation.
 */
public class Population {
    private final List<Individual> individuals;
    private final Grid grid;
    private final int maxPopulation;
    private final double comfortSensitivity;
    private Individual bestIndividualOverall;

    /**
     * Creates a new population with the given grid and parameters.
     *
     * @param grid The grid
     * @param initialPopulation The initial population size (ν)
     * @param maxPopulation The maximum population size (νmax)
     * @param comfortSensitivity The comfort sensitivity parameter (k)
     * @param deathMean The mean value for the death event (μ)
     * @param random The random number generator
     * @param initialTime The initial time of the simulation
     */
    public Population(Grid grid, int initialPopulation, int maxPopulation,
                      double comfortSensitivity, double deathMean, Random random, double initialTime) {
        this.individuals = new ArrayList<>();
        this.grid = grid;
        this.maxPopulation = maxPopulation;
        this.comfortSensitivity = comfortSensitivity;
        this.bestIndividualOverall = null;

        // Create initial population
        Point initialPoint = grid.getInitialPoint();
        for (int i = 0; i < initialPopulation; i++) {
            Individual individual = new Individual(initialPoint, initialTime, deathMean, comfortSensitivity, random);
            individuals.add(individual);
        }
    }

    /**
     * Gets the list of individuals in the population.
     *
     * @return The list of individuals
     */
    public List<Individual> getIndividuals() {
        return new ArrayList<>(individuals);
    }

    /**
     * Gets the number of individuals in the population.
     *
     * @return The population size
     */
    public int getSize() {
        return individuals.size();
    }

    /**
     * Gets the best individual in the current population.
     *
     * @return The best individual, or null if the population is empty
     */
    public Individual getBestIndividual() {
        if (individuals.isEmpty()) {
            return bestIndividualOverall;
        }

        List<Individual> aliveIndividuals = getAliveIndividuals();
        if (aliveIndividuals.isEmpty()) {
            return bestIndividualOverall;
        }

        // Check if any individual has reached the final point
        Point finalPoint = grid.getFinalPoint();
        List<Individual> finishers = new ArrayList<>();

        for (Individual individual : aliveIndividuals) {
            if (individual.hasReachedFinalPoint(finalPoint)) {
                finishers.add(individual);
            }
        }

        Individual bestCurrent;
        if (!finishers.isEmpty()) {
            // Sort finishers by path cost (lowest first)
            bestCurrent = finishers.stream()
                    .min(Comparator.comparingInt(i -> i.getPath().getCost()))
                    .orElse(null);
        } else {
            // Sort by comfort (highest first)
            bestCurrent = aliveIndividuals.stream()
                    .max(Comparator.comparingDouble(Individual::getComfort))
                    .orElse(null);
        }

        // Update best individual overall if needed
        if (bestIndividualOverall == null) {
            bestIndividualOverall = bestCurrent;
        } else {
            boolean bestHasReachedFinal = bestIndividualOverall.hasReachedFinalPoint(finalPoint);
            boolean currentHasReachedFinal = bestCurrent.hasReachedFinalPoint(finalPoint);

            if (currentHasReachedFinal && !bestHasReachedFinal) {
                bestIndividualOverall = bestCurrent;
            } else if (currentHasReachedFinal && bestHasReachedFinal) {
                // Both reached final, compare costs
                if (bestCurrent.getPath().getCost() < bestIndividualOverall.getPath().getCost()) {
                    bestIndividualOverall = bestCurrent;
                }
            } else if (!currentHasReachedFinal && !bestHasReachedFinal) {
                // Neither reached final, compare comfort
                if (bestCurrent.getComfort() > bestIndividualOverall.getComfort()) {
                    bestIndividualOverall = bestCurrent;
                }
            }
            // If best reached final but current didn't, keep best
        }

        return bestCurrent;
    }

    /**
     * Gets the best individual over the entire simulation.
     *
     * @return The best overall individual
     */
    public Individual getBestIndividualOverall() {
        // Update with the current best
        getBestIndividual();
        return bestIndividualOverall;
    }

    /**
     * Adds a new individual to the population.
     *
     * @param individual The individual to add
     * @return True if an epidemic occurred, false otherwise
     */
    public boolean addIndividual(Individual individual) {
        individuals.add(individual);

        // Check if population exceeds maximum size
        if (individuals.size() > maxPopulation) {
            return true; // Trigger epidemic
        }

        return false;
    }

    /**
     * Gets the list of alive individuals in the population.
     *
     * @return The list of alive individuals
     */
    public List<Individual> getAliveIndividuals() {
        List<Individual> alive = new ArrayList<>();
        for (Individual individual : individuals) {
            if (!individual.isDead()) {
                alive.add(individual);
            }
        }
        return alive;
    }

    /**
     * Removes dead individuals from the population.
     */
    public void removeDeadIndividuals() {
        individuals.removeIf(Individual::isDead);
    }

    /**
     * Simulates an epidemic in the population.
     *
     * @param random The random number generator
     */
    public void simulateEpidemic(Random random) {
        if (individuals.size() <= 5) {
            return; // Not enough individuals for an epidemic
        }

        // Sort individuals by comfort (highest first)
        individuals.sort(Comparator.comparingDouble(Individual::getComfort).reversed());

        // The five individuals with the highest comfort always survive
        List<Individual> survivors = new ArrayList<>(individuals.subList(0, 5));

        // For the rest, survival depends on comfort
        for (int i = 5; i < individuals.size(); i++) {
            Individual individual = individuals.get(i);
            double survivalProbability = individual.getComfort();

            if (random.nextDouble() <= survivalProbability) {
                survivors.add(individual);
            } else {
                individual.kill();
            }
        }

        // Update the population
        individuals.clear();
        individuals.addAll(survivors);
    }
}
