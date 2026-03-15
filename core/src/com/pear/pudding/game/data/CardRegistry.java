package com.pear.pudding.game.data;

import com.pear.pudding.enums.CardClass;
import com.pear.pudding.enums.CardType;

import java.util.HashMap;
import java.util.Map;

/**
 * Central registry of all card templates. Single source of truth for card definitions.
 */
public final class CardRegistry {
    private static final Map<String, CardTemplate> TEMPLATES = new HashMap<>();

    static {
        // Ghost: 1 cost, 2 attack, 1 health. SUMMON effect removes an enemy minion for 2 turns.
        register(new CardTemplate(
            "ghost",
            "Ghost",
            1, 2, 1,
            CardType.MINION,
            CardClass.ETHEREAL,
            new EffectDefinition(EffectTrigger.SUMMON, EffectType.REMOVE, -2,
                "Remove a minion for two turns", TargetType.CHOOSE_ENEMY_MINION),
            "ghostnew.png"
        ));

        // Skeleton: 1 cost, 1 attack, 2 health. FIGHT effect damages attacker.
        register(new CardTemplate(
            "skeleton",
            "Skeleton",
            1, 1, 2,
            CardType.MINION,
            CardClass.UNDEAD,
            new EffectDefinition(EffectTrigger.FIGHT, EffectType.DAMAGE, 1,
                "Deal 1 damage to attacker", TargetType.NONE),
            "skeletonnew.png"
        ));

        // Zombie: 2 cost, 2 attack, 3 health. SUMMON effect damages random enemy.
        register(new CardTemplate(
            "zombie",
            "Zombie",
            2, 2, 3,
            CardType.MINION,
            CardClass.UNDEAD,
            new EffectDefinition(EffectTrigger.SUMMON, EffectType.DAMAGE, 1,
                "Deal 1 damage to a random enemy minion", TargetType.ANY_ENEMY_MINION),
            "zombienew.png"
        ));

        // Ghoul: 2 cost, 3 attack, 1 health. DISCARD effect damages enemy hero.
        register(new CardTemplate(
            "ghoul",
            "Ghoul",
            2, 3, 1,
            CardType.MINION,
            CardClass.UNDEAD,
            new EffectDefinition(EffectTrigger.DISCARD, EffectType.DAMAGE, 2,
                "Deal 2 damage to the hero", TargetType.ANY_ENEMY_HERO),
            "ghoulnew.png"
        ));
    }

    /**
     * Registers a card template.
     */
    private static void register(CardTemplate template) {
        TEMPLATES.put(template.templateId, template);
    }

    /**
     * Gets a card template by ID.
     */
    public static CardTemplate get(String templateId) {
        CardTemplate template = TEMPLATES.get(templateId);
        if (template == null) {
            throw new IllegalArgumentException("Unknown card template: " + templateId);
        }
        return template;
    }

    /**
     * Creates a card instance from a template ID.
     */
    public static com.pear.pudding.game.state.CardInstance createInstance(String templateId, String ownerId) {
        return get(templateId).createInstance(ownerId);
    }

    /**
     * Returns all registered template IDs.
     */
    public static java.util.Set<String> getTemplateIds() {
        return TEMPLATES.keySet();
    }
}
