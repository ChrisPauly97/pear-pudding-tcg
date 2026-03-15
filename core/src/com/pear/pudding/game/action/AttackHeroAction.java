package com.pear.pudding.game.action;

import com.pear.pudding.game.state.GameState;

/**
 * Action for a minion to attack the opponent's hero.
 */
public class AttackHeroAction implements GameAction {
    private final String playerId;
    private final String attackerCardId;
    private final String targetPlayerId;

    public AttackHeroAction(String playerId, String attackerCardId, String targetPlayerId) {
        this.playerId = playerId;
        this.attackerCardId = attackerCardId;
        this.targetPlayerId = targetPlayerId;
    }

    @Override
    public ActionResult execute(GameState state) {
        // TODO: Implement hero attack logic
        return ActionResult.failure("Not yet implemented");
    }

    @Override
    public String getActorId() {
        return playerId;
    }

    @Override
    public String getDescription() {
        return "Card " + attackerCardId + " attacks hero " + targetPlayerId;
    }
}
