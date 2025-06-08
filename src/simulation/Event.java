package simulation;

/**
 * Abstract class representing an event in the simulation.
 */
public abstract class Event implements Comparable<Event> {
    private final double time;

    /**
     * Creates a new event scheduled at the given time.
     *
     * @param time The time at which the event is scheduled
     */
    public Event(double time) {
        this.time = time;
    }

    /**
     * Gets the scheduled time of this event.
     *
     * @return The scheduled time
     */
    public double getTime() {
        return time;
    }

    /**
     * Processes this event in the simulation.
     *
     * @param simulator The simulator
     */
    public abstract void process(Simulator simulator);

    @Override
    public int compareTo(Event other) {
        return Double.compare(this.time, other.time);
    }
}
