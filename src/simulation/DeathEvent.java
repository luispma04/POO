package simulation;

import model.Individual;

/**
 * Represents a death event for an individual.
 */
public class DeathEvent extends Event {
    private final Individual individual;

    /**
     * Creates a new death event for the given individual at the given time.
     *
     * @param time The time at which the event is scheduled
     * @param individual The individual that will die
     */
    public DeathEvent(double time, Individual individual) {
        super(time);
        this.individual = individual;
    }

    @Override
    public void process(Simulator simulator) {
        // Mark the individual as dead
        individual.kill();
    }

    /**
     * Gets the individual associated with this death event.
     *
     * @return The individual
     */
    public Individual getIndividual() {
        return individual;
    }

    @Override
    public String toString() {
        return "DeathEvent{time=" + getTime() + ", individual=" + individual + "}";
    }
}
