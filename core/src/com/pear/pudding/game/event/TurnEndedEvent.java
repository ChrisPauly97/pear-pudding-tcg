package com.pear.pudding.game.event;

import com.pear.pudding.game.state.GameEvent;

/**
 * Event fired when a turn ends.
 */
public class TurnEndedEvent implements GameEvent {
    public final String playerId;
    public final int turnNumber;

    public TurnEndedEvent(String playerId, int turnNumber) {
        this.playerId = playerId;
        this.turnNumber = turnNumber;
    }

    @Override
    public String getType() {
        return "TURN_ENDED";
    }
}
