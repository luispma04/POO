package simulation;

import model.Grid;
import model.Individual;

import java.util.Random;

/**
 * Represents a move event for an individual.
 */
public class MoveEvent extends Event {
    private final Individual individual;
    private final int moveNumber;

    /**
     * Creates a new move event for the given individual at the given time.
     *
     * @param time The time at which the event is scheduled
     * @param individual The individual that will move
     * @param moveNumber The sequence number of this move
     */
    public MoveEvent(double time, Individual individual, int moveNumber) {
        super(time);
        this.individual = individual;
        this.moveNumber = moveNumber;
    }

    @Override
    public void process(Simulator simulator) {
        if (individual.isDead()) {
            return; // Don't process events for dead individuals
        }

        // Move the individual
        individual.move(simulator.getGrid(), simulator.getComfortSensitivity(), simulator.getRandom());

        // Schedule the next move
        double nextMoveTime = individual.calculateNextMoveTime(
                getTime(),
                simulator.getMoveMean(),
                simulator.getRandom()
        );

        // Check if the next move time is within the simulation time
        if (nextMoveTime <= simulator.getSimulationTime()) {
            MoveEvent nextMove = new MoveEvent(nextMoveTime, individual, moveNumber + 1);
            simulator.getPEC().addEvent(nextMove);
        }
    }

    /**
     * Gets the individual associated with this move event.
     *
     * @return The individual
     */
    public Individual getIndividual() {
        return individual;
    }

    /**
     * Gets the move number.
     *
     * @return The move number
     */
    public int getMoveNumber() {
        return moveNumber;
    }

    @Override
    public String toString() {
        return "MoveEvent{time=" + getTime() + ", individual=" + individual + ", moveNumber=" + moveNumber + "}";
    }
}
