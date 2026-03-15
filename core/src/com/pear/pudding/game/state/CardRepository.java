package com.pear.pudding.game.state;

import java.util.HashMap;
import java.util.Map;

/**
 * Central registry of all CardInstance objects in play.
 * Maps instanceId -> CardInstance for quick lookup.
 */
public class CardRepository {
    private final Map<String, CardInstance> cards = new HashMap<>();

    /**
     * Registers a card instance.
     */
    public void register(CardInstance card) {
        cards.put(card.getInstanceId(), card);
    }

    /**
     * Finds a card by instance ID.
     */
    public CardInstance find(String instanceId) {
        return cards.get(instanceId);
    }

    /**
     * Unregisters a card (when it goes to discard permanently, or game ends).
     */
    public void unregister(String instanceId) {
        cards.remove(instanceId);
    }

    /**
     * Returns the number of cards in the repository.
     */
    public int size() {
        return cards.size();
    }

    /**
     * Clears all cards (game reset).
     */
    public void clear() {
        cards.clear();
    }
}
