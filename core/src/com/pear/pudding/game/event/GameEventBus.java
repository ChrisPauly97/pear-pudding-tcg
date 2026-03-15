package com.pear.pudding.game.event;

import com.pear.pudding.game.state.GameEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Central event bus for publishing and subscribing to game events.
 */
public class GameEventBus {
    private final List<GameEventListener> listeners = new ArrayList<>();

    /**
     * Subscribes a listener to all events.
     */
    public void subscribe(GameEventListener listener) {
        listeners.add(listener);
    }

    /**
     * Unsubscribes a listener.
     */
    public void unsubscribe(GameEventListener listener) {
        listeners.remove(listener);
    }

    /**
     * Publishes an event to all listeners.
     */
    public void publish(GameEvent event) {
        for (GameEventListener listener : listeners) {
            listener.onEvent(event);
        }
    }

    /**
     * Publishes multiple events.
     */
    public void publishAll(List<GameEvent> events) {
        for (GameEvent event : events) {
            publish(event);
        }
    }

    /**
     * Clears all listeners.
     */
    public void clear() {
        listeners.clear();
    }
}
