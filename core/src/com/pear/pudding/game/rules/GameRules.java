package com.pear.pudding.game.rules;

import com.pear.pudding.game.data.GameConfig;
import com.pear.pudding.game.state.*;

/**
 * Pure static predicates for game rule validation. No side effects.
 */
public final class GameRules {
    private GameRules() {}

    /**
     * Checks if a player can play a card from their hand.
     */
    public static ValidationResult canPlayCard(GameState state, String playerId, String cardInstanceId) {
        if (!isPlayerTurn(state, playerId)) {
            return ValidationResult.invalid("Not your turn");
        }

        PlayerState player = state.getPlayer(playerId);

        // Check card is in hand
        ZoneState hand = player.getHand();
        if (hand.findSlot(cardInstanceId) == -1) {
            return ValidationResult.invalid("Card not in hand");
        }

        // Check board not full
        if (player.getBoard().isFull()) {
            return ValidationResult.invalid("Board is full");
        }

        return ValidationResult.valid();
    }

    /**
     * Checks if a card can attack.
     */
    public static ValidationResult canAttack(GameState state, String attackerId, String targetId) {
        // TODO: Implement when CardRepository is added
        return ValidationResult.valid();
    }

    /**
     * Checks if it's a given player's turn.
     */
    public static boolean isPlayerTurn(GameState state, String playerId) {
        return state.turn.getActivePlayerId().equals(playerId);
    }

    /**
     * Checks if a player's board is full.
     */
    public static boolean isBoardFull(GameState state, String playerId) {
        return state.getPlayer(playerId).getBoard().isFull();
    }

    /**
     * Checks if a player's hand is full.
     */
    public static boolean isHandFull(GameState state, String playerId) {
        return state.getPlayer(playerId).getHand().isFull();
    }

    /**
     * Checks if a player can afford a card.
     */
    public static boolean canAfford(GameState state, String playerId, int cost) {
        return state.getPlayer(playerId).canAfford(cost);
    }
}
