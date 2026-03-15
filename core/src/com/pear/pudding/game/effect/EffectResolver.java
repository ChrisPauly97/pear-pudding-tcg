package com.pear.pudding.game.effect;

import com.pear.pudding.game.data.*;
import com.pear.pudding.game.event.CardDiedEvent;
import com.pear.pudding.game.event.HeroDamagedEvent;
import com.pear.pudding.game.state.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Centralized effect resolution engine. Handles all effect triggers and resolutions.
 */
public final class EffectResolver {
    private EffectResolver() {}

    /**
     * Resolves effects triggered by a card instance.
     */
    public static List<GameEvent> resolve(GameState state, CardInstance source, EffectTrigger trigger, String targetId) {
        List<GameEvent> events = new ArrayList<>();

        EffectDefinition effect = CardRegistry.get(source.getTemplateId()).effect;

        // Check if effect triggers
        if (!effect.trigger.equals(trigger)) {
            return events;
        }

        // Resolve based on effect type
        switch (effect.type) {
            case DAMAGE:
                events.addAll(applyDamage(state, targetId, effect.value));
                break;
            case HEAL:
                events.addAll(applyHeal(state, targetId, effect.value));
                break;
            case REMOVE:
                events.addAll(applyStun(state, targetId, -effect.value));
                break;
            case NONE:
                break;
        }

        return events;
    }

    /**
     * Applies damage to a target (card or hero).
     */
    private static List<GameEvent> applyDamage(GameState state, String targetId, int amount) {
        List<GameEvent> events = new ArrayList<>();

        // Check if targetId is a player (hero)
        try {
            PlayerState player = state.getPlayer(targetId);
            HeroState hero = player.getHero();
            hero.takeDamage(amount);
            events.add(new HeroDamagedEvent(targetId, amount, hero.getHealth()));
            return events;
        } catch (IllegalArgumentException e) {
            // Not a player, must be a card
        }

        // Find the target card
        // TODO: Implement when CardRepository is added

        return events;
    }

    /**
     * Applies healing to a target.
     */
    private static List<GameEvent> applyHeal(GameState state, String targetId, int amount) {
        List<GameEvent> events = new ArrayList<>();
        // TODO: Implement healing
        return events;
    }

    /**
     * Applies stun/out-of-play effects to a card.
     */
    private static List<GameEvent> applyStun(GameState state, String targetId, int duration) {
        List<GameEvent> events = new ArrayList<>();
        // TODO: Implement stun when CardRepository is added
        return events;
    }
}
