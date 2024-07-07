package com.pear.pudding.model;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector3;
import com.pear.pudding.model.Board;
import org.junit.jupiter.api.Test;

import static com.pear.pudding.model.Constants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BoardTest {
    private final Board board = new Board(0,0,100,100); // Create a dummy board object
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
}