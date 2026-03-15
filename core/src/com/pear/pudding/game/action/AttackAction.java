package com.pear.pudding.game.action;

import com.pear.pudding.game.data.CardRegistry;
import com.pear.pudding.game.event.AttackResolvedEvent;
import com.pear.pudding.game.event.HeroDamagedEvent;
import com.pear.pudding.game.state.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Action for one minion to attack another minion or hero.
 */
public class AttackAction implements GameAction {
    private final String attackerId;      // cardInstanceId
    private final String targetId;        // cardInstanceId or opponent player id
    private final String playerId;        // who is performing the attack

    public AttackAction(String playerId, String attackerId, String targetId) {
        this.playerId = playerId;
        this.attackerId = attackerId;
        this.targetId = targetId;
    }

    @Override
    public ActionResult execute(GameState state) {
        // Find attacker card
        CardInstance attacker = state.cardRepository.find(attackerId);
        if (attacker == null) {
            return ActionResult.failure("Attacker card not found");
        }

        // Check if attacker can attack
        if (!attacker.canAttack()) {
            return ActionResult.failure("Card cannot attack (summoning sick or stunned)");
        }

        List<GameEvent> events = new ArrayList<>();

        // Check if target is a hero or card
        PlayerState opponent = state.getOpponent(playerId);
        if (targetId.equals(opponent.getPlayerId())) {
            // Attack hero
            HeroState hero = opponent.getHero();
            int attackerAttack = CardRegistry.get(attacker.getTemplateId()).baseAttack;
            hero.takeDamage(attackerAttack);
            events.add(new HeroDamagedEvent(opponent.getPlayerId(), attackerAttack, hero.getHealth()));
        } else {
            // Attack minion
            CardInstance target = state.cardRepository.find(targetId);
            if (target == null) {
                return ActionResult.failure("Target card not found");
            }

            // Resolve combat
            int attackerAttack = CardRegistry.get(attacker.getTemplateId()).baseAttack;
            int targetAttack = CardRegistry.get(target.getTemplateId()).baseAttack;

            attacker.takeDamage(targetAttack);
            target.takeDamage(attackerAttack);

            events.add(new AttackResolvedEvent(attackerId, targetId, attackerAttack, targetAttack));

            // Check for deaths
            if (attacker.getCurrentHealth() <= 0) {
                attacker.setCurrentZone(ZoneType.DISCARD);
                state.getPlayer(playerId).getBoard().removeCard(attackerId);
            }
            if (target.getCurrentHealth() <= 0) {
                target.setCurrentZone(ZoneType.DISCARD);
                opponent.getBoard().removeCard(targetId);
            }
        }

        // Mark attacker as having attacked
        attacker.recordAttack();

        return ActionResult.success(events);
    }

    @Override
    public String getActorId() {
        return playerId;
    }

    @Override
    public String getDescription() {
        return "Card " + attackerId + " attacks " + targetId;
    }
}
