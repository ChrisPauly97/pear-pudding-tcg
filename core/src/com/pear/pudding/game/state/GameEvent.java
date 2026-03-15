package com.pear.pudding.game.state;

/**
 * Base interface for all game events. Events are immutable records of what happened.
 */
public interface GameEvent {
    String getType();
}
