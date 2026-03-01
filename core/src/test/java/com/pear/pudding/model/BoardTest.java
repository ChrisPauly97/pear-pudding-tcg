package com.pear.pudding.model;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector3;
import com.pear.pudding.card.EffectTrigger;
import com.pear.pudding.card.EffectType;
import com.pear.pudding.card.StatusEffect;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.pear.pudding.model.Constants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BoardTest {

    @BeforeAll
    static void setupGdx() {
        Gdx.app = mock(Application.class);
    }

    private Board board;

    @BeforeEach
    void setUp() {
        board = new Board(0, 0, 100, 100);
    }
    @Test
    public void testOnTheLeft() {
        assertTrue(board.onTheLeft(0));
        assertTrue(board.onTheLeft(1));
        assertFalse(board.onTheLeft(board.getCards().length / 2));
        assertFalse(board.onTheLeft(board.getCards().length - 1));
    }

    @Test
    public void testInTheMiddle() {
        assertFalse(board.inTheMiddle(0));
        assertFalse(board.inTheMiddle(1));
        assertTrue(board.inTheMiddle(board.getCards().length / 2));
        assertFalse(board.inTheMiddle(board.getCards().length - 1));
        assertFalse(board.inTheMiddle(board.getCards().length - 2));
    }

    @Test
    public void testOnTheRight() {
        assertFalse(board.onTheRight(0));
        assertFalse(board.onTheRight(1));
        assertFalse(board.onTheRight(board.getCards().length / 2));
        assertTrue(board.onTheRight(board.getCards().length - 1));
    }
    @Test
    public void testFirstEmptySlot(){
        assertEquals(0,board.firstEmptySlot());
        board.addCard(mock(Card.class), 0);
        assertEquals(1,board.firstEmptySlot());
        board.addCard(mock(Card.class), 1);
        assertEquals(2,board.firstEmptySlot());
        board.addCard(mock(Card.class), 2);
        assertEquals(3,board.firstEmptySlot());
        board.addCard(mock(Card.class), 3);
        assertEquals(4,board.firstEmptySlot());
        board.addCard(mock(Card.class), 4);
        assertEquals(-1,board.firstEmptySlot());
    }

    @Test
    public void testCalculateDistance() {
        assertEquals(0, board.calculateDistance(0, 0));
        assertEquals(1, board.calculateDistance(0, 1));
        assertEquals(2, board.calculateDistance(0, 2));
        assertEquals(3, board.calculateDistance(0, 3));
        assertEquals(4, board.calculateDistance(0, 4));
    }

    @Test
    public void testNearestFreeSlotOnLeft() {
        assertEquals(1, board.nearestFreeSlotFromMiddleOnLeft());
        board.addCard(mock(Card.class), 1); // Add a card to an existing slot
        assertEquals(0, board.nearestFreeSlotFromMiddleOnLeft());
        board.addCard(mock(Card.class), 0); // Add a card on the furthest right slot. After this all slots are full
        assertEquals(-1, board.nearestFreeSlotFromMiddleOnLeft());
    }

    @Test
    public void testIndexEmpty() {
        assertTrue(board.isIndexEmpty(0));
        board.addCard(mock(Card.class), 0);
        assertFalse(board.isIndexEmpty(0));
    }

    @Test
    public void testRemoveCard() {
        board.addCard(mock(Card.class), 2);
        board.removeCard(2);
        assertNull(board.getCards()[2]);
    }

    @Test
    public void testHovering() {
        assertFalse(board.mouseHovering(-1,-1));
        assertTrue(board.mouseHovering(50,50));
        assertFalse(board.mouseHovering(101,50));
        assertFalse(board.mouseHovering(50,101));
        assertFalse(board.mouseHovering(101,101));
        assertFalse(board.mouseHovering(101,-1));
        assertFalse(board.mouseHovering(50,-1));
        assertFalse(board.mouseHovering(-1,50));
        assertFalse(board.mouseHovering(-1,101));
    }

    @Test
    public void testAddCard() {
        var card = mock(Card.class);
        assertTrue(board.addCard(card, 2));
        assertFalse(board.addCard(mock(Card.class), 2));
        assertEquals(card, board.getCards()[2]);
    }

    @Test
    public void testDrawCard(){
        var card = mock(Card.class);
        var batch = mock(Batch.class);
        board.addCard(card, 2);
        board.draw(batch);
        verify(card, times(1)).draw(batch, 1);
    }

    @Test
    public void testNearestFreeSlotOnRight() {
        assertEquals(3, board.nearestFreeSlotFromMiddleOnRight());

        board.addCard(mock(Card.class), 3); // Add a card to an existing slot
        assertEquals(4, board.nearestFreeSlotFromMiddleOnRight());

        board.addCard(mock(Card.class), 4); // Add a card on the furthest right slot
        // After this all slots are full
        assertEquals(-1, board.nearestFreeSlotFromMiddleOnRight());
    }

    @Test
    public void testFindCard() {
        var card = board.findCardUnderMouse(new Vector3(0,0,1));
        assertNull(card);
        card = mock(Card.class);
        board.addCard(card, 2);
        when(card.getX()).thenReturn(0f);
        when(card.getY()).thenReturn(0f);
        when(card.getWidth()).thenReturn(CARD_WIDTH);
        when(card.getHeight()).thenReturn(CARD_HEIGHT);

        when(card.contains(new Vector3(0,0,1))).thenReturn(true);
        assertNotNull(board.findCardUnderMouse(new Vector3(0,0,1)));

        when(card.contains(new Vector3(0,0,1))).thenReturn(false);
        assertNull(board.findCardUnderMouse(new Vector3(0,0,1)));


    }
//
//    @Test
//    public void testShuffle() {
//        assertDoesNotThrow(board::shuffle); // No exceptions should be thrown when shuffling the board.shuffle();
//    }


    @Test
    public void testGetters(){
        Board board = new Board(0,0,100,100);

        assertEquals(5, board.getNUMBER_OF_SLOTS());
        assertEquals(0, board.getX());
        assertEquals(0, board.getY());
        assertEquals(100, board.getWidth());
        assertEquals(100, board.getHeight());
//        assertEquals(20, board.getSlotWidth());
    }

    // -------------------------------------------------------------------------
    // nearestFreeSlot — desired placement behaviour
    // -------------------------------------------------------------------------

    @Test
    void nearestFreeSlot_whenBoardEmpty_returnsMiddle() {
        assertEquals(2, board.nearestFreeSlot());
    }

    @Test
    void nearestFreeSlot_whenMiddleOccupied_returnsClosestFreeSlot() {
        board.addCard(mock(Card.class), 2); // occupy middle
        int slot = board.nearestFreeSlot();
        // Nearest free slot should be directly adjacent to middle (1 or 3)
        assertTrue(slot == 1 || slot == 3,
                "Expected slot 1 or 3, got " + slot);
    }

    @Test
    void nearestFreeSlot_whenLeftSideFullRightFree_returnsRightSlot() {
        board.addCard(mock(Card.class), 0);
        board.addCard(mock(Card.class), 1);
        board.addCard(mock(Card.class), 2); // middle
        int slot = board.nearestFreeSlot();
        assertTrue(slot == 3 || slot == 4,
                "Expected a right-side slot, got " + slot);
    }

    @Test
    void nearestFreeSlot_whenBoardFull_returnsNegativeOne() {
        for (int i = 0; i < board.getCards().length; i++) {
            board.addCard(mock(Card.class), i);
        }
        assertEquals(-1, board.nearestFreeSlot());
    }

    // -------------------------------------------------------------------------
    // addCard (auto-placement) — fixes the double nearestFreeSlot() bug
    // -------------------------------------------------------------------------

    @Test
    void addCard_autoPlacement_placesCardAtMiddleOnEmptyBoard() {
        Card card = mock(Card.class);
        board.addCard(card);

        // Middle slot (index 2) should contain the card
        assertEquals(card, board.getCards()[2]);
    }

    @Test
    void addCard_autoPlacement_movedToCorrectSlotPosition() {
        Card card = mock(Card.class);
        board.addCard(card);

        // Card.move() should be called with the x-position of the MIDDLE slot, not the next slot.
        // Middle slot x = boardX + slotWidth * 2 = 0 + CARD_WIDTH * 2
        float expectedX = board.getSlotPositionAtIndex(2).x;
        verify(card).move(eq(expectedX), anyFloat(), any());
    }

    // -------------------------------------------------------------------------
    // handleEffect — desired effect behaviour
    // -------------------------------------------------------------------------

    @Test
    void handleEffect_damageEffect_reducesTargetHealth() {
        Card target = new Card();
        target.setHealth(10);
        board.addCard(target, 2);

        Card attacker = mock(Card.class);
        StatusEffect se = new StatusEffect("Deal 3", EffectTrigger.SUMMON, EffectType.DAMAGE, 3);
        when(attacker.getStatusEffect()).thenReturn(se);

        board.handleEffect(2, attacker);

        assertEquals(7, target.getHealth());
    }

    @Test
    void handleEffect_healEffect_increasesTargetHealth() {
        Card target = new Card();
        target.setHealth(5);
        board.addCard(target, 1);

        Card attacker = mock(Card.class);
        StatusEffect se = new StatusEffect("Heal 2", EffectTrigger.SUMMON, EffectType.HEAL, 2);
        when(attacker.getStatusEffect()).thenReturn(se);

        board.handleEffect(1, attacker);

        assertEquals(7, target.getHealth());
    }

    @Test
    void handleEffect_removeEffect_setsTargetOutOfPlay() {
        Card target = new Card();
        target.setOutOfPlay(0);
        board.addCard(target, 3);

        Card attacker = mock(Card.class);
        StatusEffect se = new StatusEffect("Remove 2 turns", EffectTrigger.SUMMON, EffectType.REMOVE, -2);
        when(attacker.getStatusEffect()).thenReturn(se);

        board.handleEffect(3, attacker);

        assertEquals(-2, target.getOutOfPlay());
    }

    @Test
    void handleEffect_whenSlotIsEmpty_returnsFalse() {
        Card attacker = mock(Card.class);
        StatusEffect se = new StatusEffect("Damage", EffectTrigger.SUMMON, EffectType.DAMAGE, 3);
        when(attacker.getStatusEffect()).thenReturn(se);

        assertFalse(board.handleEffect(2, attacker));
    }

    @Test
    void handleEffect_whenTargetExists_returnsTrue() {
        Card target = new Card();
        target.setHealth(10);
        board.addCard(target, 2);

        Card attacker = mock(Card.class);
        StatusEffect se = new StatusEffect("Damage", EffectTrigger.SUMMON, EffectType.DAMAGE, 1);
        when(attacker.getStatusEffect()).thenReturn(se);

        assertTrue(board.handleEffect(2, attacker));
    }
}