package com.pear.pudding.game.state;

import java.util.ArrayList;
import java.util.List;

/**
 * Central authority for all game state. Immutable references, mutable content.
 */
public class GameState {
    public final PlayerState player1;
    public final PlayerState player2;
    public final TurnState turn;
    public final CardRepository cardRepository;
    public GamePhase phase;
    public GameStatus status;
    public final List<GameEvent> eventLog;

    public GameState(PlayerState player1, PlayerState player2, TurnState turn) {
        this.player1 = player1;
        this.player2 = player2;
        this.turn = turn;
        this.cardRepository = new CardRepository();
        this.phase = GamePhase.DRAW;
        this.status = GameStatus.IN_PROGRESS;
        this.eventLog = new ArrayList<>();
    }

    /**
     * Gets a player by ID.
     */
    public PlayerState getPlayer(String playerId) {
        if (player1.getPlayerId().equals(playerId)) return player1;
        if (player2.getPlayerId().equals(playerId)) return player2;
        throw new IllegalArgumentException("Unknown player: " + playerId);
    }

    /**
     * Gets the opponent of a given player.
     */
    public PlayerState getOpponent(String playerId) {
        if (player1.getPlayerId().equals(playerId)) return player2;
        if (player2.getPlayerId().equals(playerId)) return player1;
        throw new IllegalArgumentException("Unknown player: " + playerId);
    }

    /**
     * Gets the active player.
     */
    public PlayerState getActivePlayer() {
        return getPlayer(turn.getActivePlayerId());
    }

    /**
     * Logs a game event.
     */
    public void logEvent(GameEvent event) {
        eventLog.add(event);
    }
}
