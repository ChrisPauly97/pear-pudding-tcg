package com.pear.pudding.game.action;

import com.pear.pudding.game.data.CardRegistry;
import com.pear.pudding.game.event.CardPlayedEvent;
import com.pear.pudding.game.rules.GameRules;
import com.pear.pudding.game.state.*;

/**
 * Action to play a card from hand to board.
 */
public class PlayCardAction implements GameAction {
    private final String playerId;
    private final String cardInstanceId;
    private final int targetBoardSlot;

    public PlayCardAction(String playerId, String cardInstanceId, int targetBoardSlot) {
        this.playerId = playerId;
        this.cardInstanceId = cardInstanceId;
        this.targetBoardSlot = targetBoardSlot;
    }

    @Override
    public ActionResult execute(GameState state) {
        // Validate action is legal
        if (!GameRules.isPlayerTurn(state, playerId)) {
            return ActionResult.failure("Not your turn");
        }

        PlayerState player = state.getPlayer(playerId);
        CardInstance card = findCard(state, cardInstanceId);
        if (card == null) {
            return ActionResult.failure("Card not found");
        }

        if (!card.getCurrentZone().equals(ZoneType.HAND)) {
            return ActionResult.failure("Card is not in hand");
        }

        // Check mana
        String templateId = card.getTemplateId();
        int cost = CardRegistry.get(templateId).baseCost;
        if (!player.canAfford(cost)) {
            return ActionResult.failure("Not enough mana");
        }

        // Check board not full
        if (player.getBoard().isFull()) {
            return ActionResult.failure("Board is full");
        }

        // Perform the move
        player.getHand().removeCard(cardInstanceId);
        player.spendMana(cost);

        card.setCurrentZone(ZoneType.BOARD);
        card.setSlotIndex(targetBoardSlot);
        card.setSummoningSick(true);
        player.getBoard().addCard(cardInstanceId, targetBoardSlot);

        return ActionResult.success(
            new CardPlayedEvent(playerId, cardInstanceId, targetBoardSlot)
        );
    }

    @Override
    public String getActorId() {
        return playerId;
    }

    @Override
    public String getDescription() {
        return "Play card " + cardInstanceId + " to board slot " + targetBoardSlot;
    }

    private CardInstance findCard(GameState state, String instanceId) {
        return state.cardRepository.find(instanceId);
    }
}
