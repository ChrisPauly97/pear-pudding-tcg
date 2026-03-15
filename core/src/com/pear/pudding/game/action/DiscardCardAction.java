package com.pear.pudding.game.action;

import com.pear.pudding.game.state.GameState;

/**
 * Action to discard a card from hand to discard pile.
 */
public class DiscardCardAction implements GameAction {
    private final String playerId;
    private final String cardInstanceId;

    public DiscardCardAction(String playerId, String cardInstanceId) {
        this.playerId = playerId;
        this.cardInstanceId = cardInstanceId;
    }

    @Override
    public ActionResult execute(GameState state) {
        // TODO: Implement discard logic
        return ActionResult.failure("Not yet implemented");
    }

    @Override
    public String getActorId() {
        return playerId;
    }

    @Override
    public String getDescription() {
        return "Discard card " + cardInstanceId;
    }
}
