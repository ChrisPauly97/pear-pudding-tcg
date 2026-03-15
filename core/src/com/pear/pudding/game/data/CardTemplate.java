package com.pear.pudding.game.data;

import com.pear.pudding.enums.CardClass;
import com.pear.pudding.enums.CardType;
import com.pear.pudding.game.state.CardInstance;
import java.util.UUID;

/**
 * Immutable definition of a card type. Serves as a template for creating CardInstance objects.
 */
public final class CardTemplate {
    public final String templateId;
    public final String displayName;
    public final int baseCost;
    public final int baseAttack;
    public final int baseHealth;
    public final CardType type;
    public final CardClass cardClass;
    public final EffectDefinition effect;
    public final String texturePath;

    public CardTemplate(String templateId, String displayName, int baseCost, int baseAttack,
                       int baseHealth, CardType type, CardClass cardClass,
                       EffectDefinition effect, String texturePath) {
        this.templateId = templateId;
        this.displayName = displayName;
        this.baseCost = baseCost;
        this.baseAttack = baseAttack;
        this.baseHealth = baseHealth;
        this.type = type;
        this.cardClass = cardClass;
        this.effect = effect;
        this.texturePath = texturePath;
    }

    /**
     * Creates a CardInstance from this template.
     */
    public CardInstance createInstance(String ownerId) {
        return new CardInstance(
            UUID.randomUUID().toString(),
            this.templateId,
            ownerId,
            this.baseHealth
        );
    }
}
