package simulation;

import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Pending Event Container (PEC) for managing events in the simulation.
 */
public class PEC {
    private final Queue<Event> events;
    private int processedEvents;

    /**
     * Creates a new empty PEC.
     */
    public PEC() {
        this.events = new PriorityQueue<>();
        this.processedEvents = 0;
    }

    /**
     * Adds an event to the PEC.
     *
     * @param event The event to add
     */
    public void addEvent(Event event) {
        events.add(event);
    }

    /**
     * Gets the next event from the PEC without removing it.
     *
     * @return The next event, or null if the PEC is empty
     */
    public Event peekEvent() {
        return events.peek();
    }

    /**
     * Gets and removes the next event from the PEC.
     *
     * @return The next event, or null if the PEC is empty
     */
    public Event nextEvent() {
        Event event = events.poll();
        if (event != null) {
            processedEvents++;
        }
        return event;
    }

    /**
     * Checks if the PEC is empty.
     *
     * @return True if the PEC is empty, false otherwise
     */
    public boolean isEmpty() {
        return events.isEmpty();
    }

    /**
     * Gets the number of events currently in the PEC.
     *
     * @return The number of events
     */
    public int size() {
        return events.size();
    }

    /**
     * Gets the number of processed events.
     *
     * @return The number of processed events
     */
    public int getProcessedEvents() {
        return processedEvents;
    }
}
