package com.pear.pudding.game.action;

import com.pear.pudding.game.event.CardDrawnEvent;
import com.pear.pudding.game.state.*;

/**
 * Action to draw a card from the draw deck to hand.
 */
public class DrawCardAction implements GameAction {
    private final String playerId;

    public DrawCardAction(String playerId) {
        this.playerId = playerId;
    }

    @Override
    public ActionResult execute(GameState state) {
        PlayerState player = state.getPlayer(playerId);

        // Check draw deck is not empty
        if (player.getDrawDeck().size() == 0) {
            return ActionResult.failure("Draw deck is empty");
        }

        // Check hand is not full
        if (player.getHand().isFull()) {
            return ActionResult.failure("Hand is full");
        }

        // Find the first card in the draw deck
        String[] deckSlots = player.getDrawDeck().getSlots();
        String cardId = null;
        int cardSlot = -1;
        for (int i = 0; i < deckSlots.length; i++) {
            if (deckSlots[i] != null) {
                cardId = deckSlots[i];
                cardSlot = i;
                break;
            }
        }

        if (cardId == null) {
            return ActionResult.failure("No card to draw");
        }

        // Move from draw deck to hand
        player.getDrawDeck().removeCard(cardId);
        int handSlot = player.getHand().addCard(cardId);

        // Update card instance state
        CardInstance card = state.cardRepository.find(cardId);
        if (card != null) {
            card.setCurrentZone(ZoneType.HAND);
            card.setSlotIndex(handSlot);
        }

        return ActionResult.success(
            new CardDrawnEvent(playerId, cardId, handSlot)
        );
    }

    @Override
    public String getActorId() {
        return playerId;
    }

    @Override
    public String getDescription() {
        return "Draw a card";
    }
}
