package com.pear.pudding.game.state;

/**
 * Tracks whose turn it is and which turn number we're on.
 */
public class TurnState {
    private String activePlayerId;
    private int turnNumber;

    public TurnState(String initialPlayerId) {
        this.activePlayerId = initialPlayerId;
        this.turnNumber = 1;
    }

    public String getActivePlayerId() { return activePlayerId; }
    public int getTurnNumber() { return turnNumber; }

    public void setActivePlayerId(String playerId) { this.activePlayerId = playerId; }
    public void incrementTurn() { this.turnNumber++; }

    public void nextPlayer(String nextPlayerId) {
        activePlayerId = nextPlayerId;
        turnNumber++;
    }
}
