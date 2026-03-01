package com.pear.pudding.ai;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.pear.pudding.enums.Location;
import com.pear.pudding.model.Card;
import com.pear.pudding.player.Hero;
import com.pear.pudding.player.Player;

/**
 * Helper class to execute AI actions programmatically.
 * Provides methods to play cards, attack minions, and attack heroes.
 */
public class AIActionExecutor {

    /**
     * Plays a card from the AI's hand to the board.
     * @param player The AI player
     * @param card The card to play
     * @param boardSlot The board slot index to place the card
     * @return true if the card was successfully played, false otherwise
     */
    public boolean playCard(Player player, Card card, int boardSlot) {
        try {
            // Verify the player has enough mana
            if (!player.hasEnoughMana(card)) {
                Gdx.app.log("AIActionExecutor", "Not enough mana to play card: " + card.getClass().getSimpleName());
                return false;
            }

            // Use atomic move
            boolean success = com.pear.pudding.model.Deck.moveCardBetweenDecks(
                card,
                player.getHand(),
                player.getBoard(),
                boardSlot
            );

            if (success) {
                // Deduct mana and mark as summoning sick (cannot attack same turn it's played)
                player.setCurrentMana(player.getCurrentMana() - card.getCost());
                card.setSummoningSick(true);
                Gdx.app.log("AIActionExecutor", "Played card: " + card.getClass().getSimpleName() + " at slot " + boardSlot);
            }

            return success;
        } catch (Exception e) {
            Gdx.app.error("AIActionExecutor", "Error playing card", e);
            return false;
        }
    }

    /**
     * Executes an attack from one card to another.
     * @param attacker The attacking card
     * @param target The target card
     * @return true if the target was destroyed, false otherwise
     */
    public boolean attackCard(Card attacker, Card target) {
        try {
            Gdx.app.log("AIActionExecutor", "Attacking " + target.getClass().getSimpleName() +
                       " (HP: " + target.getHealth() + ") with " + attacker.getClass().getSimpleName() +
                       " (ATK: " + attacker.getAttack() + ")");

            attacker.fight(target);
            attacker.setAttackCount(0);

            return target.getHealth() <= 0;
        } catch (Exception e) {
            Gdx.app.error("AIActionExecutor", "Error attacking card", e);
            return false;
        }
    }

    /**
     * Executes an attack from a card to the enemy hero.
     * @param attacker The attacking card
     * @param hero The target hero
     * @return true if the game is over (hero defeated), false otherwise
     */
    public boolean attackHero(Card attacker, Hero hero) {
        try {
            Gdx.app.log("AIActionExecutor", "Attacking hero (HP: " + hero.getHealth() +
                       ") with " + attacker.getClass().getSimpleName() + " (ATK: " + attacker.getAttack() + ")");

            boolean gameOver = attacker.fight(hero);
            attacker.setAttackCount(0);

            if (gameOver) {
                Gdx.app.log("AIActionExecutor", "Hero defeated! Game Over!");
            }

            return gameOver;
        } catch (Exception e) {
            Gdx.app.error("AIActionExecutor", "Error attacking hero", e);
            return false;
        }
    }
}
