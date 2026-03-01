package com.pear.pudding.ai;

import com.badlogic.gdx.Gdx;
import com.pear.pudding.model.Card;
import com.pear.pudding.player.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Basic AI opponent that makes strategic decisions for card play and combat.
 * Uses simple heuristics to evaluate card value and target priority.
 */
public class BasicAI {
    private final Player aiPlayer;
    private final Player enemyPlayer;
    private final AIActionExecutor executor;

    public BasicAI(Player aiPlayer, Player enemyPlayer) {
        this.aiPlayer = aiPlayer;
        this.enemyPlayer = enemyPlayer;
        this.executor = new AIActionExecutor();
    }

    /**
     * Play affordable cards from hand to board.
     * Prioritizes higher-value cards (better attack/health ratio).
     * Stops when out of mana or board is full.
     */
    public void playCards() {
        Gdx.app.log("BasicAI", "=== Play Phase Started ===");

        // Get all cards from hand
        List<Card> handCards = new ArrayList<>();
        for (Card card : aiPlayer.getHand().getCards()) {
            if (card != null) {
                handCards.add(card);
            }
        }

        // Sort by card value (attack + health) descending
        handCards.sort(Comparator.comparingInt(this::calculateCardValue).reversed());

        // Play cards while we have mana and board space
        for (Card card : handCards) {
            // Check if we have enough mana
            if (!aiPlayer.hasEnoughMana(card)) {
                Gdx.app.log("BasicAI", "Not enough mana for " + card.getClass().getSimpleName() +
                           " (Cost: " + card.getCost() + ", Mana: " + aiPlayer.getCurrentMana() + ")");
                continue;
            }

            // Find an empty slot on the board
            int emptySlot = aiPlayer.getBoard().firstEmptySlot();
            if (emptySlot == -1) {
                Gdx.app.log("BasicAI", "Board is full, cannot play more cards");
                break;
            }

            // Play the card
            boolean success = executor.playCard(aiPlayer, card, emptySlot);
            if (success) {
                Gdx.app.log("BasicAI", "Successfully played " + card.getClass().getSimpleName());
            }
        }

        Gdx.app.log("BasicAI", "=== Play Phase Ended ===");
    }

    /**
     * Attack with available minions on the board.
     * Prioritizes killing weak enemy minions.
     * Attacks hero if board is clear or AI health is low (aggressive finish).
     */
    public void attackWithMinions() {
        Gdx.app.log("BasicAI", "=== Attack Phase Started ===");

        // Get all attackable minions
        List<Card> attackers = new ArrayList<>();
        for (Card card : aiPlayer.getBoard().getCards()) {
            if (card != null && card.getAttackCount() > 0) {
                attackers.add(card);
            }
        }

        if (attackers.isEmpty()) {
            Gdx.app.log("BasicAI", "No minions available to attack");
            Gdx.app.log("BasicAI", "=== Attack Phase Ended ===");
            return;
        }

        // Get enemy minions
        List<Card> enemyMinions = new ArrayList<>();
        for (Card card : enemyPlayer.getBoard().getCards()) {
            if (card != null) {
                enemyMinions.add(card);
            }
        }

        // Attack with each minion
        for (Card attacker : attackers) {
            if (attacker.getAttackCount() == 0) {
                continue; // Skip if already attacked
            }

            boolean attacked = false;

            // Try to find a good target to kill
            if (!enemyMinions.isEmpty()) {
                Card bestTarget = findBestTarget(attacker, enemyMinions);
                if (bestTarget != null) {
                    executor.attackCard(attacker, bestTarget);
                    if (bestTarget.getHealth() <= 0) {
                        enemyMinions.remove(bestTarget); // Remove dead minion from list
                    }
                    attacked = true;
                }
            }

            // If no good minion target or board is clear, consider attacking hero
            if (!attacked && shouldAttackHero(enemyMinions)) {
                boolean gameOver = executor.attackHero(attacker, enemyPlayer.getHero());
                if (gameOver) {
                    Gdx.app.log("BasicAI", "AI wins the game!");
                }
            }
        }

        Gdx.app.log("BasicAI", "=== Attack Phase Ended ===");
    }

    /**
     * Calculate card value based on attack and health.
     */
    private int calculateCardValue(Card card) {
        return card.getAttack() + card.getHealth();
    }

    /**
     * Find the best target to attack.
     * Prioritizes targets that can be killed in one hit.
     * If no killable targets, attacks the weakest minion.
     */
    private Card findBestTarget(Card attacker, List<Card> enemyMinions) {
        Card bestTarget = null;
        int lowestHealth = Integer.MAX_VALUE;

        for (Card target : enemyMinions) {
            // Prefer targets we can kill
            if (target.getHealth() <= attacker.getAttack()) {
                if (bestTarget == null || target.getHealth() < lowestHealth) {
                    bestTarget = target;
                    lowestHealth = target.getHealth();
                }
            }
        }

        // If no killable target, attack the weakest
        if (bestTarget == null && !enemyMinions.isEmpty()) {
            for (Card target : enemyMinions) {
                if (target.getHealth() < lowestHealth) {
                    bestTarget = target;
                    lowestHealth = target.getHealth();
                }
            }
        }

        return bestTarget;
    }

    /**
     * Decide whether to attack the hero directly.
     * Attack hero if enemy board is empty or AI is going for aggressive finish.
     */
    private boolean shouldAttackHero(List<Card> enemyMinions) {
        // Always attack hero if enemy board is clear
        if (enemyMinions.isEmpty()) {
            Gdx.app.log("BasicAI", "Enemy board is clear, attacking hero");
            return true;
        }

        // Attack hero if AI health is low (aggressive finish)
        if (aiPlayer.getHero().getHealth() < 15) {
            Gdx.app.log("BasicAI", "AI health low, going aggressive on hero");
            return true;
        }

        return false;
    }
}
