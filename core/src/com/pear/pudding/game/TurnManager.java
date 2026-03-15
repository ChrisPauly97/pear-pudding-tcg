package com.pear.pudding.game;

import com.pear.pudding.game.action.GameAction;
import com.pear.pudding.game.rules.GameRules;
import com.pear.pudding.game.state.*;

/**
 * Manages game turn flow and phase transitions.
 */
public class TurnManager {
    /**
     * Phases within a turn.
     */
    public enum Phase {
        DRAW,              // Draw phase
        PLAY_AND_ATTACK,   // Main phase
        END                // End phase
    }

    /**
     * Starts a new turn for the active player.
     */
    public void startTurn(GameState state) {
        PlayerState player = state.getActivePlayer();

        // Reset minions on board
        for (String cardId : player.getBoard().getSlots()) {
            if (cardId != null) {
                // TODO: Reset card state when CardRepository is added
            }
        }

        state.phase = GamePhase.DRAW;
    }

    /**
     * Ends the current turn.
     */
    public void endTurn(GameState state) {
        state.phase = GamePhase.END;
    }

    /**
     * Checks if an action is legal in the current game state.
     */
    public boolean isActionLegal(GameState state, GameAction action) {
        String actorId = action.getActorId();

        // Must be the actor's turn
        if (!GameRules.isPlayerTurn(state, actorId)) {
            return false;
        }

        // TODO: Check phase-specific rules based on action type

        return true;
    }

    /**
     * Transitions to the next phase.
     */
    public void nextPhase(GameState state) {
        switch (state.phase) {
            case DRAW:
                state.phase = GamePhase.PLAY_AND_ATTACK;
                break;
            case PLAY_AND_ATTACK:
                state.phase = GamePhase.END;
                break;
            case END:
                // End turn handled externally
                break;
        }
    }
}
