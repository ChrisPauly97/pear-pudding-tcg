package com.pear.pudding.game.action;

import com.pear.pudding.game.event.TurnEndedEvent;
import com.pear.pudding.game.event.TurnStartedEvent;
import com.pear.pudding.game.state.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Action to end the current turn and start the next player's turn.
 */
public class EndTurnAction implements GameAction {
    private final String playerId;

    public EndTurnAction(String playerId) {
        this.playerId = playerId;
    }

    @Override
    public ActionResult execute(GameState state) {
        if (!state.turn.getActivePlayerId().equals(playerId)) {
            return ActionResult.failure("It's not your turn");
        }

        List<GameEvent> events = new ArrayList<>();

        // End current turn
        events.add(new TurnEndedEvent(playerId, state.turn.getTurnNumber()));

        // Switch to next player
        PlayerState nextPlayer = state.getOpponent(playerId);
        state.turn.nextPlayer(nextPlayer.getPlayerId());
        state.phase = GamePhase.DRAW;

        // Reset next player's minions
        for (String cardId : nextPlayer.getBoard().getSlots()) {
            if (cardId != null) {
                com.pear.pudding.game.state.CardInstance card = state.cardRepository.find(cardId);
                if (card != null) {
                    card.resetForTurn();
                }
            }
        }

        // Increment mana and refill
        if (nextPlayer.getTotalMana() < 10) {
            nextPlayer.setTotalMana(nextPlayer.getTotalMana() + 1);
        }
        nextPlayer.refillMana();

        events.add(new TurnStartedEvent(nextPlayer.getPlayerId(), state.turn.getTurnNumber()));

        return ActionResult.success(events);
    }

    @Override
    public String getActorId() {
        return playerId;
    }

    @Override
    public String getDescription() {
        return "End turn";
    }
}
