package com.pear.pudding.game.event;

import com.pear.pudding.game.state.GameEvent;

/**
 * Event fired when a card dies (moves to discard).
 */
public class CardDiedEvent implements GameEvent {
    public final String cardInstanceId;
    public final String ownerId;

    public CardDiedEvent(String cardInstanceId, String ownerId) {
        this.cardInstanceId = cardInstanceId;
        this.ownerId = ownerId;
    }

    @Override
    public String getType() {
        return "CARD_DIED";
    }
}
