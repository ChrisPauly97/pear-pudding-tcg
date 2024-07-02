package com.pear.pudding.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
public class SnapToTests {

    private Deck deck;

    @BeforeEach
    public void setUpBoard() {
        this.deck = createBoardWith5Slots();
    }

    @Test
    public void testSnapTo_InitialTargetSlotIsNull_ShouldReturnNull() {
        // Test case for initial target slot being null
        // Setup
        Card card = mock(Card.class);
        // Execute
        Slot result = deck.snapTo(card, null);

        // Verify
        assertNull(result);
    }

    @Test
    public void testSnapTo_MiddleSlotHasCard_ShouldReturnLeftOfMiddleSlot() {
        // Test case for passing middle slot as the initial target
        // Setup
        Slot middleSlot = deck.getSlots().get(2);
        middleSlot.setCard(mock(Card.class));
        Slot leftOfMiddleSlot = deck.getSlots().get(1);
        Slot initialTargetSlot = deck.getSlots().get(0);

        // Execute
        Slot result = deck.snapTo(mock(Card.class), initialTargetSlot);

        // Verify
        assertEquals(leftOfMiddleSlot, result);
    }

    @Test
    public void testSnapTo_MiddleSlotHasCard_ShouldReturnRightOfMiddleSlot() {
        // Test case for passing middle slot as the initial target
        // Setup
        Slot middleSlot = deck.getSlots().get(2);
        middleSlot.setCard(mock(Card.class));
        Slot rightOfMiddleSlot = deck.getSlots().get(3);
        Slot initialTargetSlot = deck.getSlots().get(4);

        // Execute
        Slot result = deck.snapTo(mock(Card.class), initialTargetSlot);

        // Verify
        assertEquals(rightOfMiddleSlot, result);
    }

    @Test
    public void testSnapTo_MiddleSlotNoCard_ShouldReturnMiddleSlot() {
        // Test case for middle slot having no card
        // Setup
        Slot middleSlot = deck.getSlots().get(2);

        // Execute
        Slot result = deck.snapTo(mock(Card.class), middleSlot);

        // Verify
        assertEquals(middleSlot, result);
    }

    @Test
    public void testSnapTo_MiddleSlotHasCard_ShouldReturnClosestFreeSlot() {
        // Test case for targeting a slot with index 4 when slots with indices 2 and 3 have cards
        // Setup
        Slot leftOfMiddleSlot = deck.getSlots().get(1);
        Slot middleSlot = deck.getSlots().get(2);
        middleSlot.setCard(mock(Card.class));
        Slot rightOfMiddleSlot = deck.getSlots().get(3);
        rightOfMiddleSlot.setCard(mock(Card.class));
        Slot initialTargetSlot = deck.getSlots().get(4);

        // Execute
        Slot result = deck.snapTo(mock(Card.class), initialTargetSlot);

        // Verify
        assertEquals(rightOfMiddleSlot, result);
        assertNotNull(leftOfMiddleSlot.getCard());
        assertNotNull(rightOfMiddleSlot.getCard());
    }

    // Utility method to create a board with 5 slots
    private Deck createBoardWith5Slots() {
        Deck deck = new Deck();
        for (int i = 0; i < 5; i++) {
            deck.addSlot(new Slot(100 * i, 20, 100,100, i));
        }
        return deck;
    }
}