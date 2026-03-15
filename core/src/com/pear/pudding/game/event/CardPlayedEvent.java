package com.pear.pudding.game.event;

import com.pear.pudding.game.state.GameEvent;

/**
 * Event fired when a card is played from hand to board.
 */
public class CardPlayedEvent implements GameEvent {
    public final String playerId;
    public final String cardInstanceId;
    public final int boardSlot;

    public CardPlayedEvent(String playerId, String cardInstanceId, int boardSlot) {
        this.playerId = playerId;
        this.cardInstanceId = cardInstanceId;
        this.boardSlot = boardSlot;
    }

    @Override
    public String getType() {
        return "CARD_PLAYED";
    }
}
