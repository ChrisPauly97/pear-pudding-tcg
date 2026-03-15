package com.pear.pudding.game.action;

import com.pear.pudding.game.state.GameEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Result of executing a GameAction. Contains success status, optional error, and events.
 */
public class ActionResult {
    public final boolean success;
    public final String errorMessage;
    public final List<GameEvent> events;

    private ActionResult(boolean success, String errorMessage, List<GameEvent> events) {
        this.success = success;
        this.errorMessage = errorMessage;
        this.events = events;
    }

    /**
     * Creates a successful result with events.
     */
    public static ActionResult success(GameEvent... events) {
        List<GameEvent> eventList = new ArrayList<>();
        for (GameEvent event : events) {
            eventList.add(event);
        }
        return new ActionResult(true, null, eventList);
    }

    /**
     * Creates a successful result with a list of events.
     */
    public static ActionResult success(List<GameEvent> events) {
        return new ActionResult(true, null, new ArrayList<>(events));
    }

    /**
     * Creates a failed result with an error message.
     */
    public static ActionResult failure(String errorMessage) {
        return new ActionResult(false, errorMessage, new ArrayList<>());
    }
}
