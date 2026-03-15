package com.pear.pudding.game.data;

/**
 * What kind of effect happens.
 */
public enum EffectType {
    NONE,        // No effect
    DAMAGE,      // Deal damage
    HEAL,        // Restore health
    BUFF,        // Increase attack/health
    DEBUFF,      // Decrease attack/health
    STUN,        // Freeze for turns
    REMOVE,      // Remove from board temporarily
    DRAW,        // Draw card
    DISCARD_CARD // Discard a card
}
