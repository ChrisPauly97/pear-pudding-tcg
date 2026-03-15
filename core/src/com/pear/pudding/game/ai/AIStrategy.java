package com.pear.pudding.game.ai;

import com.pear.pudding.game.action.GameAction;
import com.pear.pudding.game.state.GameState;
import java.util.List;

/**
 * Interface for AI decision-making. Pure game logic, no LibGDX or rendering.
 */
public interface AIStrategy {
    /**
     * Decides what actions to take this turn.
     *
     * @param state The current game state.
     * @param playerId The ID of the AI player.
     * @return A list of actions to execute this turn, in order.
     */
    List<GameAction> decideTurn(GameState state, String playerId);
}
