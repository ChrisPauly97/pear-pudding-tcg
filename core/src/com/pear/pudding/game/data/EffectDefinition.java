package com.pear.pudding.game.data;

/**
 * Immutable definition of a card's effect.
 */
public final class EffectDefinition {
    public final EffectTrigger trigger;
    public final EffectType type;
    public final int value;
    public final String cardText;
    public final TargetType targetType;

    public EffectDefinition(EffectTrigger trigger, EffectType type, int value,
                           String cardText, TargetType targetType) {
        this.trigger = trigger;
        this.type = type;
        this.value = value;
        this.cardText = cardText;
        this.targetType = targetType;
    }

    public static final EffectDefinition NONE = new EffectDefinition(
        EffectTrigger.NONE, EffectType.NONE, 0, "", TargetType.NONE
    );
}
