package com.pear.pudding.game.event;

import com.pear.pudding.game.state.GameEvent;

/**
 * Event fired when a card is drawn from deck to hand.
 */
public class CardDrawnEvent implements GameEvent {
    public final String playerId;
    public final String cardInstanceId;
    public final int handSlot;

    public CardDrawnEvent(String playerId, String cardInstanceId, int handSlot) {
        this.playerId = playerId;
        this.cardInstanceId = cardInstanceId;
        this.handSlot = handSlot;
    }

    @Override
    public String getType() {
        return "CARD_DRAWN";
    }
}
