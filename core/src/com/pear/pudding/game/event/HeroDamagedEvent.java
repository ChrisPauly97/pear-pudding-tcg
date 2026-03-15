package com.pear.pudding.game.event;

import com.pear.pudding.game.state.GameEvent;

/**
 * Event fired when a hero takes damage.
 */
public class HeroDamagedEvent implements GameEvent {
    public final String playerId;
    public final int damage;
    public final int remainingHealth;

    public HeroDamagedEvent(String playerId, int damage, int remainingHealth) {
        this.playerId = playerId;
        this.damage = damage;
        this.remainingHealth = remainingHealth;
    }

    @Override
    public String getType() {
        return "HERO_DAMAGED";
    }
}
