package com.pear.pudding.game.state;

/**
 * Phases within a player's turn.
 */
public enum GamePhase {
    DRAW,              // Draw phase
    PLAY_AND_ATTACK,   // Main phase - play cards and attack
    END                // End phase
}
