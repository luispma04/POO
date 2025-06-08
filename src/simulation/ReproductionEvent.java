package simulation;

import model.Grid;
import model.Individual;
import model.Population;

/**
 * Represents a reproduction event for an individual.
 */
public class ReproductionEvent extends Event {
    private final Individual individual;
    private final int reproductionNumber;

    /**
     * Creates a new reproduction event for the given individual at the given time.
     *
     * @param time The time at which the event is scheduled
     * @param individual The individual that will reproduce
     * @param reproductionNumber The sequence number of this reproduction
     */
    public ReproductionEvent(double time, Individual individual, int reproductionNumber) {
        super(time);
        this.individual = individual;
        this.reproductionNumber = reproductionNumber;
    }

    @Override
    public void process(Simulator simulator) {
        if (individual.isDead()) {
            return; // Don't process events for dead individuals
        }

        // Create a new individual as a child of the current individual
        Individual child = new Individual(
                individual,
                getTime(),
                simulator.getDeathMean(),
                simulator.getComfortSensitivity(),
                simulator.getGrid(),
                simulator.getRandom()
        );

        // Add the child to the population
        boolean epidemicTriggered = simulator.getPopulation().addIndividual(child);

        // Handle epidemic if triggered
        if (epidemicTriggered) {
            simulator.getPopulation().simulateEpidemic(simulator.getRandom());
        }

        // Schedule death event for the child
        if (child.getDeathTime() <= simulator.getSimulationTime()) {
            DeathEvent deathEvent = new DeathEvent(child.getDeathTime(), child);
            simulator.getPEC().addEvent(deathEvent);
        }

        // Schedule first move for the child
        double firstMoveTime = child.calculateNextMoveTime(
                getTime(),
                simulator.getMoveMean(),
                simulator.getRandom()
        );

        if (firstMoveTime <= simulator.getSimulationTime()) {
            MoveEvent moveEvent = new MoveEvent(firstMoveTime, child, 1);
            simulator.getPEC().addEvent(moveEvent);
        }

        // Schedule first reproduction for the child
        double firstReproductionTime = child.calculateNextReproductionTime(
                getTime(),
                simulator.getReproductionMean(),
                simulator.getRandom()
        );

        if (firstReproductionTime <= simulator.getSimulationTime()) {
            ReproductionEvent reproductionEvent = new ReproductionEvent(firstReproductionTime, child, 1);
            simulator.getPEC().addEvent(reproductionEvent);
        }

        // Schedule next reproduction for the parent
        double nextReproductionTime = individual.calculateNextReproductionTime(
                getTime(),
                simulator.getReproductionMean(),
                simulator.getRandom()
        );

        if (nextReproductionTime <= simulator.getSimulationTime()) {
            ReproductionEvent nextReproduction = new ReproductionEvent(
                    nextReproductionTime,
                    individual,
                    reproductionNumber + 1
            );
            simulator.getPEC().addEvent(nextReproduction);
        }
    }

    /**
     * Gets the individual associated with this reproduction event.
     *
     * @return The individual
     */
    public Individual getIndividual() {
        return individual;
    }

    /**
     * Gets the reproduction number.
     *
     * @return The reproduction number
     */
    public int getReproductionNumber() {
        return reproductionNumber;
    }

    @Override
    public String toString() {
        return "ReproductionEvent{time=" + getTime() + ", individual=" + individual + ", reproductionNumber=" + reproductionNumber + "}";
    }
}
