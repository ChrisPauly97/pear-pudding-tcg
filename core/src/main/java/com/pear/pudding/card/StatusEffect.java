package com.pear.pudding.card;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StatusEffect {
    private String cardText;
    private EffectTrigger effectTrigger;
    private EffectType effectType;
    private int value;

    public StatusEffect(String cardText) {
        this.cardText = cardText;
    }

    public StatusEffect(String cardText, EffectTrigger trigger, EffectType effectType, int value) {
        this.cardText = cardText;
        this.effectTrigger = trigger;
        this.effectType = effectType;
        this.value = value;
    }

}
