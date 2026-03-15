package com.pear.pudding.game.event;

import com.pear.pudding.game.state.GameEvent;

/**
 * Event fired when an attack is resolved.
 */
public class AttackResolvedEvent implements GameEvent {
    public final String attackerId;
    public final String targetId;
    public final int attackerDamage;
    public final int targetDamage;

    public AttackResolvedEvent(String attackerId, String targetId, int attackerDamage, int targetDamage) {
        this.attackerId = attackerId;
        this.targetId = targetId;
        this.attackerDamage = attackerDamage;
        this.targetDamage = targetDamage;
    }

    @Override
    public String getType() {
        return "ATTACK_RESOLVED";
    }
}
