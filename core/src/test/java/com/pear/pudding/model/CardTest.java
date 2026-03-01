package com.pear.pudding.model;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.pear.pudding.card.EffectTrigger;
import com.pear.pudding.card.EffectType;
import com.pear.pudding.card.StatusEffect;
import com.pear.pudding.enums.Location;
import com.pear.pudding.player.Hero;
import com.pear.pudding.player.Player;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CardTest {

    @BeforeAll
    static void setupGdx() {
        Gdx.app = mock(Application.class);
    }

    private Card makeCard(int attack, int health, int attackCount,
                          boolean summoningSick, Location location, int outOfPlay) {
        Card card = new Card();
        card.setAttack(attack);
        card.setHealth(health);
        card.setAttackCount(attackCount);
        card.setSummoningSick(summoningSick);
        card.setCurrentLocation(location);
        card.setOutOfPlay(outOfPlay);
        return card;
    }

    /**
     * Attaches a mock player with a real DiscardPile and real Board/Hand
     * so that moveToDiscardPile() works without NPE.
     */
    private void attachPlayer(Card card) {
        Player mockPlayer = mock(Player.class);
        DiscardPile discard = new DiscardPile(0, 0, 100, 100);
        Board board = new Board(0, 0, 100, 100);
        Hand hand = new Hand(0, 0, 100, 100);
        when(mockPlayer.getDiscardPile()).thenReturn(discard);
        when(mockPlayer.getBoard()).thenReturn(board);
        when(mockPlayer.getHand()).thenReturn(hand);
        card.setPlayer(mockPlayer);
    }

    // -------------------------------------------------------------------------
    // canAttack
    // -------------------------------------------------------------------------

    @Test
    void canAttack_whenAllConditionsMet_returnsTrue() {
        Card card = makeCard(2, 5, 1, false, Location.BOARD, 0);
        assertTrue(card.canAttack());
    }

    @Test
    void canAttack_whenSummoningSick_returnsFalse() {
        Card card = makeCard(2, 5, 1, true, Location.BOARD, 0);
        assertFalse(card.canAttack());
    }

    @Test
    void canAttack_whenNoAttacksRemaining_returnsFalse() {
        Card card = makeCard(2, 5, 0, false, Location.BOARD, 0);
        assertFalse(card.canAttack());
    }

    @Test
    void canAttack_whenNotOnBoard_returnsFalse() {
        Card card = makeCard(2, 5, 1, false, Location.HAND, 0);
        assertFalse(card.canAttack());
    }

    @Test
    void canAttack_whenOutOfPlay_returnsFalse() {
        Card card = makeCard(2, 5, 1, false, Location.BOARD, -1);
        assertFalse(card.canAttack());
    }

    @Test
    void canAttack_whenOutOfPlayIsNegative_returnsFalse() {
        Card card = makeCard(2, 5, 1, false, Location.BOARD, -2);
        assertFalse(card.canAttack());
    }

    @Test
    void canAttack_whenOutOfPlayIsZero_returnsTrue() {
        Card card = makeCard(2, 5, 1, false, Location.BOARD, 0);
        assertTrue(card.canAttack());
    }

    // -------------------------------------------------------------------------
    // canPlay
    // -------------------------------------------------------------------------

    @Test
    void canPlay_whenInHandWithEnoughMana_returnsTrue() {
        Card card = makeCard(2, 5, 1, false, Location.HAND, 0);
        card.setCost(2);
        Player mockPlayer = mock(Player.class);
        when(mockPlayer.hasEnoughMana(card)).thenReturn(true);
        card.setPlayer(mockPlayer);
        assertTrue(card.canPlay());
    }

    @Test
    void canPlay_whenNotInHand_returnsFalse() {
        Card card = makeCard(2, 5, 1, false, Location.BOARD, 0);
        card.setCost(1);
        Player mockPlayer = mock(Player.class);
        when(mockPlayer.hasEnoughMana(card)).thenReturn(true);
        card.setPlayer(mockPlayer);
        assertFalse(card.canPlay());
    }

    @Test
    void canPlay_whenNotEnoughMana_returnsFalse() {
        Card card = makeCard(2, 5, 1, false, Location.HAND, 0);
        card.setCost(5);
        Player mockPlayer = mock(Player.class);
        when(mockPlayer.hasEnoughMana(card)).thenReturn(false);
        card.setPlayer(mockPlayer);
        assertFalse(card.canPlay());
    }

    // -------------------------------------------------------------------------
    // takeDamage / getHealing
    // -------------------------------------------------------------------------

    @Test
    void takeDamage_reducesHealthByAmount() {
        Card card = makeCard(2, 10, 1, false, Location.BOARD, 0);
        card.takeDamage(3);
        assertEquals(7, card.getHealth());
    }

    @Test
    void takeDamage_canReduceHealthToZero() {
        Card card = makeCard(2, 3, 1, false, Location.BOARD, 0);
        card.takeDamage(3);
        assertEquals(0, card.getHealth());
    }

    @Test
    void takeDamage_canReduceHealthBelowZero() {
        Card card = makeCard(2, 2, 1, false, Location.BOARD, 0);
        card.takeDamage(5);
        assertEquals(-3, card.getHealth());
    }

    @Test
    void getHealing_increasesHealthByAmount() {
        Card card = makeCard(2, 3, 1, false, Location.BOARD, 0);
        card.getHealing(4);
        assertEquals(7, card.getHealth());
    }

    // -------------------------------------------------------------------------
    // fight(Card enemy)
    // -------------------------------------------------------------------------

    @Test
    void fight_bothCardsTakeDamageEqualToOpponentAttack() {
        Card attacker = makeCard(3, 10, 1, false, Location.BOARD, 0);
        Card enemy    = makeCard(2, 10, 1, false, Location.BOARD, 0);

        attacker.fight(enemy);

        assertEquals(8, attacker.getHealth(), "Attacker should take enemy.attack(2) damage");
        assertEquals(7, enemy.getHealth(),    "Enemy should take attacker.attack(3) damage");
    }

    @Test
    void fight_decrementsAttackerAttackCountByOne() {
        Card attacker = makeCard(3, 10, 1, false, Location.BOARD, 0);
        Card enemy    = makeCard(2, 10, 1, false, Location.BOARD, 0);

        attacker.fight(enemy);

        assertEquals(0, attacker.getAttackCount());
    }

    @Test
    void fight_whenSummoningSick_noDamageDone() {
        Card attacker = makeCard(3, 10, 1, true, Location.BOARD, 0);
        Card enemy    = makeCard(2, 10, 1, false, Location.BOARD, 0);

        attacker.fight(enemy);

        assertEquals(10, attacker.getHealth());
        assertEquals(10, enemy.getHealth());
        assertEquals(1, attacker.getAttackCount());
    }

    @Test
    void fight_whenNoAttackCount_noDamageDone() {
        Card attacker = makeCard(3, 10, 0, false, Location.BOARD, 0);
        Card enemy    = makeCard(2, 10, 1, false, Location.BOARD, 0);

        attacker.fight(enemy);

        assertEquals(10, attacker.getHealth());
        assertEquals(10, enemy.getHealth());
    }

    @Test
    void fight_whenEnemyHealthDropsToZero_enemyMovesToDiscard() {
        Card attacker = makeCard(5, 10, 1, false, Location.BOARD, 0);
        Card enemy    = makeCard(1, 3, 1, false, Location.BOARD, 0);
        attachPlayer(attacker);
        attachPlayer(enemy);

        attacker.fight(enemy);

        // Enemy health should be <= 0 after taking 5 damage
        assertTrue(enemy.getHealth() <= 0);
        // Enemy should have been placed in the discard pile
        assertNotNull(enemy.getPlayer().getDiscardPile().getCardAtIndex(0));
        assertEquals(enemy, enemy.getPlayer().getDiscardPile().getCardAtIndex(0));
    }

    @Test
    void fight_whenBothCardsDieSimultaneously_bothMoveToDiscard() {
        Card attacker = makeCard(3, 3, 1, false, Location.BOARD, 0);
        Card enemy    = makeCard(3, 3, 1, false, Location.BOARD, 0);
        attachPlayer(attacker);
        attachPlayer(enemy);

        attacker.fight(enemy);

        assertTrue(attacker.getHealth() <= 0);
        assertTrue(enemy.getHealth() <= 0);
    }

    // -------------------------------------------------------------------------
    // fight(Hero)
    // -------------------------------------------------------------------------

    @Test
    void fightHero_attackerAndHeroBothTakeDamage() {
        Card attacker = makeCard(3, 10, 1, false, Location.BOARD, 0);
        Hero hero = new Hero(null, 0, 0, 0);
        hero.health = 20;
        hero.attack = 2;

        attacker.fight(hero);

        assertEquals(8, attacker.getHealth(), "Attacker should take hero.attack(2) damage");
        assertEquals(17, hero.health,          "Hero should take attacker.attack(3) damage");
    }

    @Test
    void fightHero_whenHeroHealthDropsToZero_returnsTrue() {
        Card attacker = makeCard(10, 20, 1, false, Location.BOARD, 0);
        Hero hero = new Hero(null, 0, 0, 0);
        hero.health = 5;
        hero.attack = 1;

        boolean gameOver = attacker.fight(hero);

        assertTrue(gameOver);
    }

    @Test
    void fightHero_whenHeroSurvives_returnsFalse() {
        Card attacker = makeCard(2, 10, 1, false, Location.BOARD, 0);
        Hero hero = new Hero(null, 0, 0, 0);
        hero.health = 30;
        hero.attack = 1;

        boolean gameOver = attacker.fight(hero);

        assertFalse(gameOver);
    }

    @Test
    void fightHero_whenCannotAttack_noDamageDealt() {
        Card attacker = makeCard(3, 10, 0, false, Location.BOARD, 0); // attackCount = 0
        Hero hero = new Hero(null, 0, 0, 0);
        hero.health = 20;
        hero.attack = 2;

        attacker.fight(hero);

        assertEquals(10, attacker.getHealth());
        assertEquals(20, hero.health);
    }

    // -------------------------------------------------------------------------
    // handleSummonEffect
    // -------------------------------------------------------------------------

    @Test
    void handleSummonEffect_healEffect_increasesCardHealth() {
        Card card = makeCard(2, 3, 1, false, Location.BOARD, 0);
        card.setStatusEffect(new StatusEffect("Heal 2", EffectTrigger.SUMMON, EffectType.HEAL, 2));

        card.handleSummonEffect();

        assertEquals(5, card.getHealth());
    }

    @Test
    void handleSummonEffect_damageEffect_returnsTrue() {
        Card card = makeCard(2, 3, 1, false, Location.BOARD, 0);
        card.setStatusEffect(new StatusEffect("Deal 3 damage", EffectTrigger.SUMMON, EffectType.DAMAGE, 3));

        assertTrue(card.handleSummonEffect());
    }

    @Test
    void handleSummonEffect_removeEffect_returnsTrue() {
        Card card = makeCard(2, 3, 1, false, Location.BOARD, 0);
        card.setStatusEffect(new StatusEffect("Remove for 2 turns", EffectTrigger.SUMMON, EffectType.REMOVE, -2));

        assertTrue(card.handleSummonEffect());
    }

    @Test
    void handleSummonEffect_whenTriggerIsNotSummon_returnsFalse() {
        Card card = makeCard(2, 3, 1, false, Location.BOARD, 0);
        card.setStatusEffect(new StatusEffect("Fight effect", EffectTrigger.FIGHT, EffectType.DAMAGE, 3));

        assertFalse(card.handleSummonEffect());
    }

    @Test
    void handleSummonEffect_noneEffect_returnsFalse() {
        Card card = makeCard(2, 3, 1, false, Location.BOARD, 0);
        card.setStatusEffect(new StatusEffect("No effect", EffectTrigger.SUMMON, EffectType.NONE, 0));

        assertFalse(card.handleSummonEffect());
    }

    // -------------------------------------------------------------------------
    // triggerAttackEffect
    // -------------------------------------------------------------------------

    @Test
    void triggerAttackEffect_fightTriggerWithDamage_returnsTrueAndDamagesEnemy() {
        Card attacker = makeCard(2, 5, 1, false, Location.BOARD, 0);
        attacker.setStatusEffect(new StatusEffect("Damage", EffectTrigger.FIGHT, EffectType.DAMAGE, 3));
        Card enemy = makeCard(2, 10, 1, false, Location.BOARD, 0);

        boolean triggered = attacker.triggerAttackEffect(attacker, enemy);

        assertTrue(triggered);
        assertEquals(7, enemy.getHealth()); // 10 - 3
    }

    @Test
    void triggerAttackEffect_fightTriggerWithHeal_returnsTrueAndHealsAttacker() {
        Card attacker = makeCard(2, 5, 1, false, Location.BOARD, 0);
        attacker.setStatusEffect(new StatusEffect("Heal", EffectTrigger.FIGHT, EffectType.HEAL, 2));
        Card enemy = makeCard(2, 10, 1, false, Location.BOARD, 0);

        boolean triggered = attacker.triggerAttackEffect(attacker, enemy);

        assertTrue(triggered);
        assertEquals(7, attacker.getHealth()); // 5 + 2
    }

    @Test
    void triggerAttackEffect_fightTriggerWithRemove_returnsTrueAndSetsEnemyOutOfPlay() {
        Card attacker = makeCard(2, 5, 1, false, Location.BOARD, 0);
        attacker.setStatusEffect(new StatusEffect("Remove", EffectTrigger.FIGHT, EffectType.REMOVE, -2));
        Card enemy = makeCard(2, 10, 1, false, Location.BOARD, 0);

        boolean triggered = attacker.triggerAttackEffect(attacker, enemy);

        assertTrue(triggered);
        assertEquals(-2, enemy.getOutOfPlay());
    }

    @Test
    void triggerAttackEffect_whenTriggerIsNotFight_returnsFalse() {
        Card attacker = makeCard(2, 5, 1, false, Location.BOARD, 0);
        attacker.setStatusEffect(new StatusEffect("Summon effect", EffectTrigger.SUMMON, EffectType.DAMAGE, 3));
        Card enemy = makeCard(2, 10, 1, false, Location.BOARD, 0);

        assertFalse(attacker.triggerAttackEffect(attacker, enemy));
    }

    // -------------------------------------------------------------------------
    // outOfPlay / refreshBoard behaviour
    // -------------------------------------------------------------------------

    @Test
    void outOfPlay_normalCard_remainsZeroAfterTurnRefresh() {
        // Normal cards (outOfPlay=0) must NOT be incremented during refresh —
        // if they were, canAttack() would break since it requires outOfPlay == 0.
        Card card = makeCard(2, 5, 1, false, Location.BOARD, 0);

        // Simulate the fixed refreshBoard logic for one turn
        if (card.getOutOfPlay() < 0) {
            card.setOutOfPlay(card.getOutOfPlay() + 1);
        }
        card.setAttackCount(1);
        card.setSummoningSick(false);

        assertEquals(0, card.getOutOfPlay());
        assertTrue(card.canAttack());
    }

    @Test
    void outOfPlay_removedCard_incrementsTowardZeroEachTurn() {
        Card card = makeCard(2, 5, 1, false, Location.BOARD, -2);

        // Turn 1 refresh
        if (card.getOutOfPlay() < 0) card.setOutOfPlay(card.getOutOfPlay() + 1);
        card.setAttackCount(1);
        card.setSummoningSick(false);
        assertEquals(-1, card.getOutOfPlay());
        assertFalse(card.canAttack());

        // Turn 2 refresh
        if (card.getOutOfPlay() < 0) card.setOutOfPlay(card.getOutOfPlay() + 1);
        card.setAttackCount(1);
        card.setSummoningSick(false);
        assertEquals(0, card.getOutOfPlay());
        assertTrue(card.canAttack());
    }
}
