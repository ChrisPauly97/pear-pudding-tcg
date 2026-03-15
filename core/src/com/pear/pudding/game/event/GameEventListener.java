package com.pear.pudding.game.event;

import com.pear.pudding.game.state.GameEvent;

/**
 * Listener for game events.
 */
public interface GameEventListener {
    void onEvent(GameEvent event);
}
