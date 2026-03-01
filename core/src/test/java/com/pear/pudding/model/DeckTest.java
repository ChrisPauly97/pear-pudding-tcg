package com.pear.pudding.model;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for base Deck mechanics. Board is used as a concrete Deck
 * (5 slots) so we can instantiate one without a display context.
 */
public class DeckTest {

    @BeforeAll
    static void setupGdx() {
        Gdx.app = mock(Application.class);
    }

    private Board deck;

    @BeforeEach
    void setUp() {
        deck = new Board(0, 0, 600, 100);
    }

    // -------------------------------------------------------------------------
    // firstEmptySlot
    // -------------------------------------------------------------------------

    @Test
    void firstEmptySlot_whenEmpty_returnsZero() {
        assertEquals(0, deck.firstEmptySlot());
    }

    @Test
    void firstEmptySlot_skipsOccupiedSlots() {
        deck.addCard(mock(Card.class), 0);
        deck.addCard(mock(Card.class), 1);
        assertEquals(2, deck.firstEmptySlot());
    }

    @Test
    void firstEmptySlot_whenFull_returnsNegativeOne() {
        for (int i = 0; i < deck.getCards().length; i++) {
            deck.addCard(mock(Card.class), i);
        }
        assertEquals(-1, deck.firstEmptySlot());
    }

    // -------------------------------------------------------------------------
    // addCard / removeCard
    // -------------------------------------------------------------------------

    @Test
    void addCard_toEmptySlot_returnsTrue() {
        assertTrue(deck.addCard(mock(Card.class), 2));
    }

    @Test
    void addCard_toOccupiedSlot_returnsFalse() {
        Card first = mock(Card.class);
        deck.addCard(first, 2);
        assertFalse(deck.addCard(mock(Card.class), 2));
    }

    @Test
    void addCard_withNegativeOneIndex_returnsFalse() {
        assertFalse(deck.addCard(mock(Card.class), -1));
    }

    @Test
    void removeCard_byIndex_setsSlotToNull() {
        deck.addCard(mock(Card.class), 1);
        deck.removeCard(1);
        assertNull(deck.getCards()[1]);
    }

    @Test
    void removeCard_byReference_clearsMatchingSlot() {
        Card card = mock(Card.class);
        deck.addCard(card, 3);
        deck.removeCard(card);
        assertNull(deck.getCards()[3]);
    }

    // -------------------------------------------------------------------------
    // isIndexEmpty / containsCard / getHandSize
    // -------------------------------------------------------------------------

    @Test
    void isIndexEmpty_whenSlotIsNull_returnsTrue() {
        assertTrue(deck.isIndexEmpty(0));
    }

    @Test
    void isIndexEmpty_whenSlotIsOccupied_returnsFalse() {
        deck.addCard(mock(Card.class), 0);
        assertFalse(deck.isIndexEmpty(0));
    }

    @Test
    void containsCard_whenCardIsPresent_returnsTrue() {
        Card card = mock(Card.class);
        deck.addCard(card, 2);
        assertTrue(deck.containsCard(card));
    }

    @Test
    void containsCard_whenCardIsAbsent_returnsFalse() {
        assertFalse(deck.containsCard(mock(Card.class)));
    }

    @Test
    void getHandSize_countsOnlyNonNullSlots() {
        assertEquals(0, deck.getHandSize());
        deck.addCard(mock(Card.class), 0);
        deck.addCard(mock(Card.class), 2);
        assertEquals(2, deck.getHandSize());
    }

    // -------------------------------------------------------------------------
    // lastPopulatedIndex
    // -------------------------------------------------------------------------

    @Test
    void lastPopulatedIndex_whenEmpty_returnsNegativeOne() {
        assertEquals(-1, deck.lastPopulatedIndex());
    }

    @Test
    void lastPopulatedIndex_returnsHighestOccupiedIndex() {
        deck.addCard(mock(Card.class), 0);
        deck.addCard(mock(Card.class), 3);
        assertEquals(3, deck.lastPopulatedIndex());
    }

    // -------------------------------------------------------------------------
    // snapShot / restoreSnapshot
    // -------------------------------------------------------------------------

    @Test
    void restoreSnapshot_restoresCardArray() {
        Card card = mock(Card.class);
        deck.addCard(card, 1);
        deck.snapShot();

        deck.removeCard(1);
        assertNull(deck.getCards()[1], "Card should be gone before restore");

        deck.restoreSnapshot();
        assertEquals(card, deck.getCards()[1], "Card should be back after restore");
    }

    @Test
    void restoreSnapshot_whenNoSnapshotExists_doesNothing() {
        assertDoesNotThrow(() -> deck.restoreSnapshot());
    }

    @Test
    void restoreSnapshot_doesNotAffectUnchangedSlots() {
        Card a = mock(Card.class);
        Card b = mock(Card.class);
        deck.addCard(a, 0);
        deck.addCard(b, 4);
        deck.snapShot();
        deck.removeCard(0);

        deck.restoreSnapshot();

        assertEquals(a, deck.getCards()[0]);
        assertEquals(b, deck.getCards()[4]);
    }

    // -------------------------------------------------------------------------
    // moveCardBetweenDecks (static)
    // -------------------------------------------------------------------------

    @Test
    void moveCardBetweenDecks_movesCardFromSourceToDestination() {
        Board source = new Board(0, 0, 600, 100);
        Board dest   = new Board(0, 0, 600, 100);
        Card card = mock(Card.class);
        source.addCard(card, 0);

        Deck.moveCardBetweenDecks(card, source, dest, 2);

        assertNull(source.getCards()[0],   "Source slot should be empty after move");
        assertEquals(card, dest.getCards()[2], "Card should be at destination index");
    }

    @Test
    void moveCardBetweenDecks_withNullCard_returnsFalse() {
        Board dest = new Board(0, 0, 600, 100);
        assertFalse(Deck.moveCardBetweenDecks(null, null, dest, 0));
    }

    @Test
    void moveCardBetweenDecks_withOutOfBoundsIndex_returnsFalse() {
        Board source = new Board(0, 0, 600, 100);
        Board dest   = new Board(0, 0, 600, 100);
        Card card = mock(Card.class);
        source.addCard(card, 0);

        assertFalse(Deck.moveCardBetweenDecks(card, source, dest, 99));
    }

    @Test
    void moveCardBetweenDecks_withNullSource_stillAddsToDestination() {
        Board dest = new Board(0, 0, 600, 100);
        Card card = mock(Card.class);

        assertTrue(Deck.moveCardBetweenDecks(card, null, dest, 0));
        assertEquals(card, dest.getCards()[0]);
    }

    @Test
    void moveCardBetweenDecks_returnsTrue_onSuccess() {
        Board source = new Board(0, 0, 600, 100);
        Board dest   = new Board(0, 0, 600, 100);
        Card card = mock(Card.class);
        source.addCard(card, 0);

        assertTrue(Deck.moveCardBetweenDecks(card, source, dest, 1));
    }

    // -------------------------------------------------------------------------
    // position helpers
    // -------------------------------------------------------------------------

    @Test
    void onTheLeft_indicesLeftOfMiddle_returnTrue() {
        assertTrue(deck.onTheLeft(0));
        assertTrue(deck.onTheLeft(1));
    }

    @Test
    void onTheLeft_middleAndRight_returnFalse() {
        assertFalse(deck.onTheLeft(2)); // middle
        assertFalse(deck.onTheLeft(3));
        assertFalse(deck.onTheLeft(4));
    }

    @Test
    void inTheMiddle_onlyMiddleIndex_returnsTrue() {
        assertFalse(deck.inTheMiddle(1));
        assertTrue(deck.inTheMiddle(2));
        assertFalse(deck.inTheMiddle(3));
    }

    @Test
    void onTheRight_indicesRightOfMiddle_returnTrue() {
        assertFalse(deck.onTheRight(2)); // middle
        assertTrue(deck.onTheRight(3));
        assertTrue(deck.onTheRight(4));
    }

    @Test
    void calculateDistance_returnsAbsoluteDifference() {
        assertEquals(0, deck.calculateDistance(2, 2));
        assertEquals(2, deck.calculateDistance(0, 2));
        assertEquals(2, deck.calculateDistance(2, 0));
        assertEquals(4, deck.calculateDistance(0, 4));
    }

    @Test
    void middleSlot_returnsCentreIndex() {
        assertEquals(2, deck.middleSlot());
    }

    // -------------------------------------------------------------------------
    // findCardUnderMouse
    // -------------------------------------------------------------------------

    @Test
    void findCardUnderMouse_whenNoCards_returnsNull() {
        assertNull(deck.findCardUnderMouse(new Vector3(50, 50, 0)));
    }

    @Test
    void findCardUnderMouse_returnsCardWhoseContainsIsTrue() {
        Card card = mock(Card.class);
        deck.addCard(card, 1);
        Vector3 point = new Vector3(50, 50, 0);
        when(card.contains(point)).thenReturn(true);

        assertEquals(card, deck.findCardUnderMouse(point));
    }

    @Test
    void findCardUnderMouse_returnsNullWhenContainsIsFalse() {
        Card card = mock(Card.class);
        deck.addCard(card, 1);
        Vector3 point = new Vector3(50, 50, 0);
        when(card.contains(point)).thenReturn(false);

        assertNull(deck.findCardUnderMouse(point));
    }
}
