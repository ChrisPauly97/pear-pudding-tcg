package com.pear.pudding.card;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatusEffect {
    String cardText;
    EffectTrigger effectTrigger;
    EffectType effectType;
    int value;

    public StatusEffect(String cardText, EffectTrigger trigger, EffectType effectType, int value) {
        this.cardText = cardText;
        this.effectTrigger = trigger;
        this.effectType = effectType;
        this.value = value;
    }

}
