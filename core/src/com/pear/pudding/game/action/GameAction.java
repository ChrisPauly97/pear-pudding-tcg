package com.pear.pudding.game.action;

import com.pear.pudding.game.state.GameState;

/**
 * Base interface for all game actions. Actions are commands that mutate GameState.
 */
public interface GameAction {
    /**
     * Executes this action, mutating the game state and producing events.
     */
    ActionResult execute(GameState state);

    /**
     * Returns the ID of the player taking this action.
     */
    String getActorId();

    /**
     * Returns a human-readable description of the action.
     */
    String getDescription();
}
