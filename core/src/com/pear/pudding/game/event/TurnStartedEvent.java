package com.pear.pudding.game.event;

import com.pear.pudding.game.state.GameEvent;

/**
 * Event fired when a new turn starts.
 */
public class TurnStartedEvent implements GameEvent {
    public final String activePlayerId;
    public final int turnNumber;

    public TurnStartedEvent(String activePlayerId, int turnNumber) {
        this.activePlayerId = activePlayerId;
        this.turnNumber = turnNumber;
    }

    @Override
    public String getType() {
        return "TURN_STARTED";
    }
}
