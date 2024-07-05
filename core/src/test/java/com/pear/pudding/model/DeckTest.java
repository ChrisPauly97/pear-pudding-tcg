package com.pear.pudding.model;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector3;
import com.pear.pudding.model.Deck;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.pear.pudding.model.Constants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DeckTest {
    private final Deck deck = new Deck(5f); // Create a dummy Deck object
    @Test
    public void testOnTheLeft() {
        assertTrue(deck.onTheLeft(0));
        assertTrue(deck.onTheLeft(1));
        assertFalse(deck.onTheLeft(deck.getCards().length / 2));
        assertFalse(deck.onTheLeft(deck.getCards().length - 1));
    }

    @Test
    public void testInTheMiddle() {
        assertFalse(deck.inTheMiddle(0));
        assertFalse(deck.inTheMiddle(1));
        assertTrue(deck.inTheMiddle(deck.getCards().length / 2));
        assertFalse(deck.inTheMiddle(deck.getCards().length - 1));
        assertFalse(deck.inTheMiddle(deck.getCards().length - 2));
    }

    @Test
    public void testOnTheRight() {
        assertFalse(deck.onTheRight(0));
        assertFalse(deck.onTheRight(1));
        assertFalse(deck.onTheRight(deck.getCards().length / 2));
        assertTrue(deck.onTheRight(deck.getCards().length - 1));
    }
    @Test
    public void testFirstEmptySlot(){
        assertEquals(0,deck.firstEmptySlot());
        deck.addCard(mock(Card.class), 0);
        assertEquals(1,deck.firstEmptySlot());
        deck.addCard(mock(Card.class), 1);
        assertEquals(2,deck.firstEmptySlot());
        deck.addCard(mock(Card.class), 2);
        assertEquals(3,deck.firstEmptySlot());
        deck.addCard(mock(Card.class), 3);
        assertEquals(4,deck.firstEmptySlot());
        deck.addCard(mock(Card.class), 4);
        assertEquals(-1,deck.firstEmptySlot());
    }

    @Test
    public void testCalculateDistance() {
        assertEquals(0, deck.calculateDistance(0, 0));
        assertEquals(1, deck.calculateDistance(0, 1));
        assertEquals(2, deck.calculateDistance(0, 2));
        assertEquals(3, deck.calculateDistance(0, 3));
        assertEquals(4, deck.calculateDistance(0, 4));
    }

    @Test
    public void testNearestFreeSlotOnLeft() {
        assertEquals(1, deck.nearestFreeSlotFromMiddleOnLeft());
        deck.addCard(mock(Card.class), 1); // Add a card to an existing slot
        assertEquals(0, deck.nearestFreeSlotFromMiddleOnLeft());
        deck.addCard(mock(Card.class), 0); // Add a card on the furthest right slot. After this all slots are full
        assertEquals(-1, deck.nearestFreeSlotFromMiddleOnLeft());
    }

    @Test
    public void testIndexEmpty() {
        assertTrue(deck.isIndexEmpty(0));
        deck.addCard(mock(Card.class), 0);
        assertFalse(deck.isIndexEmpty(0));
    }

    @Test
    public void testRemoveCard() {
        deck.addCard(mock(Card.class), 2);
        deck.removeCard(2);
        assertNull(deck.getCards()[2]);
    }

    @Test
    public void testHovering() {
        Board board = new Board(0,0,100,100);
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
        assertTrue(deck.addCard(card, 2));
        assertFalse(deck.addCard(mock(Card.class), 2));
        assertEquals(card, deck.getCards()[2]);
    }

    @Test
    public void testDrawCard(){
        var card = mock(Card.class);
        var batch = mock(Batch.class);
        deck.addCard(card, 2);
        deck.draw(batch);
        verify(card, times(1)).draw(batch, 1);
    }

    @Test
    public void testNearestFreeSlotOnRight() {
        assertEquals(3, deck.nearestFreeSlotFromMiddleOnRight());

        deck.addCard(mock(Card.class), 3); // Add a card to an existing slot
        assertEquals(4, deck.nearestFreeSlotFromMiddleOnRight());

        deck.addCard(mock(Card.class), 4); // Add a card on the furthest right slot
        // After this all slots are full
        assertEquals(-1, deck.nearestFreeSlotFromMiddleOnRight());
    }

    @Test
    public void testFindCard() {
        var card = deck.findCardUnderMouse(new Vector3(0,0,1));
        assertNull(card);
        card = mock(Card.class);
        deck.addCard(card, 2);
        when(card.getX()).thenReturn(0f);
        when(card.getY()).thenReturn(0f);
        when(card.getWidth()).thenReturn(CARD_WIDTH);
        when(card.getHeight()).thenReturn(CARD_HEIGHT);

        when(card.contains(new Vector3(0,0,1))).thenReturn(true);
        assertNotNull(deck.findCardUnderMouse(new Vector3(0,0,1)));

        when(card.contains(new Vector3(0,0,1))).thenReturn(false);
        assertNull(deck.findCardUnderMouse(new Vector3(0,0,1)));


    }

    @Test
    public void testShuffle() {
        assertDoesNotThrow(deck::shuffle); // No exceptions should be thrown when shuffling the deck.shuffle();
    }


    @Test
    public void testGetters(){
        Board board = new Board(0,0,100,100);

        assertEquals(5, board.getNUMBER_OF_SLOTS());
        assertEquals(0, board.getX());
        assertEquals(0, board.getY());
        assertEquals(100, board.getWidth());
        assertEquals(100, board.getHeight());
        assertEquals(20, board.getSlotWidth());
    }
}