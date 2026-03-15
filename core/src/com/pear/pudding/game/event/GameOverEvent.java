package com.pear.pudding.game.event;

import com.pear.pudding.game.state.GameEvent;

/**
 * Event fired when the game ends.
 */
public class GameOverEvent implements GameEvent {
    public final String winnerId;

    public GameOverEvent(String winnerId) {
        this.winnerId = winnerId;
    }

    @Override
    public String getType() {
        return "GAME_OVER";
    }
}
