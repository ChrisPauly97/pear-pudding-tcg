package com.pear.pudding.game.ai;

import com.pear.pudding.game.action.AttackAction;
import com.pear.pudding.game.action.GameAction;
import com.pear.pudding.game.action.PlayCardAction;
import com.pear.pudding.game.action.EndTurnAction;
import com.pear.pudding.game.data.CardRegistry;
import com.pear.pudding.game.state.GameState;
import com.pear.pudding.game.state.PlayerState;
import com.pear.pudding.game.state.ZoneType;
import java.util.ArrayList;
import java.util.List;

/**
 * Basic AI strategy: plays cards, attacks, ends turn. Pure game logic.
 */
public class BasicAIStrategy implements AIStrategy {

    @Override
    public List<GameAction> decideTurn(GameState state, String playerId) {
        List<GameAction> actions = new ArrayList<>();

        // 1. Play cards from hand that we can afford
        playCards(state, playerId, actions);

        // 2. Attack with minions
        attackWithMinions(state, playerId, actions);

        // 3. End turn
        actions.add(new EndTurnAction(playerId));

        return actions;
    }

    /**
     * Plays cards from hand that we can afford.
     */
    private void playCards(GameState state, String playerId, List<GameAction> actions) {
        PlayerState player = state.getPlayer(playerId);

        // Try to play each card in hand
        for (int i = 0; i < player.getHand().capacity; i++) {
            String cardId = player.getHand().getCardAt(i);
            if (cardId != null) {
                // Get card from repository
                com.pear.pudding.game.state.CardInstance card = state.cardRepository.find(cardId);
                if (card != null) {
                    // Check if we can afford it
                    int cost = CardRegistry.get(card.getTemplateId()).baseCost;
                    if (player.canAfford(cost) && !player.getBoard().isFull()) {
                        // Find an empty board slot
                        int slot = -1;
                        for (int j = 0; j < player.getBoard().capacity; j++) {
                            if (player.getBoard().getCardAt(j) == null) {
                                slot = j;
                                break;
                            }
                        }
                        if (slot != -1) {
                            actions.add(new PlayCardAction(playerId, cardId, slot));
                        }
                    }
                }
            }
        }
    }

    /**
     * Attacks with minions on the board.
     */
    private void attackWithMinions(GameState state, String playerId, List<GameAction> actions) {
        PlayerState player = state.getPlayer(playerId);
        PlayerState opponent = state.getOpponent(playerId);

        // Iterate all cards on board that can attack
        for (int i = 0; i < player.getBoard().capacity; i++) {
            String cardId = player.getBoard().getCardAt(i);
            if (cardId != null) {
                com.pear.pudding.game.state.CardInstance card = state.cardRepository.find(cardId);
                if (card != null && card.canAttack()) {
                    // Determine target
                    String targetId = "player1";  // Default to enemy hero

                    // Check if enemy has cards on board
                    for (int j = 0; j < opponent.getBoard().capacity; j++) {
                        String enemyCardId = opponent.getBoard().getCardAt(j);
                        if (enemyCardId != null) {
                            // Attack first enemy card found
                            targetId = enemyCardId;
                            break;
                        }
                    }

                    // If no board target found, attack hero
                    if (targetId.equals("player1")) {
                        targetId = opponent.getPlayerId();
                    }

                    actions.add(new AttackAction(playerId, cardId, targetId));
                }
            }
        }
    }
}
