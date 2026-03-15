package com.pear.pudding.game.data;

/**
 * When an effect is triggered.
 */
public enum EffectTrigger {
    NONE,        // No effect
    SUMMON,      // Card played to board
    FIGHT,       // Card attacks
    DISCARD,     // Card discarded
    DAMAGED,     // Card takes damage
    DEATH        // Card dies
}
