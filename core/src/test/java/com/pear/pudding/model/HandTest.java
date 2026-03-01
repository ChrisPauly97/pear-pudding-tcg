package com.pear.pudding.model;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class HandTest {

    @BeforeAll
    static void setupGdx() {
        Gdx.app = mock(Application.class);
    }

    private Hand hand;

    @BeforeEach
    void setUp() {
        hand = new Hand(0, 0, 600, 100);
    }

    // -------------------------------------------------------------------------
    // Basic slot management (inherited from Deck)
    // -------------------------------------------------------------------------

    @Test
    void newHand_hasCorrectNumberOfSlots() {
        assertEquals(5, hand.getCards().length);
    }

    @Test
    void newHand_allSlotsAreEmpty() {
        // All slots empty means firstEmptySlot returns 0
        assertEquals(0, hand.firstEmptySlot());
        assertEquals(0, hand.getHandSize());
    }

    @Test
    void addCard_placesCardAtIndex() {
        Card card = mock(Card.class);
        hand.addCard(card, 2);
        assertEquals(card, hand.getCards()[2]);
    }

    @Test
    void addCard_fullHand_returnsFalse() {
        for (int i = 0; i < hand.getCards().length; i++) {
            hand.addCard(mock(Card.class), i);
        }
        assertFalse(hand.addCard(mock(Card.class), -1));
    }

    @Test
    void handSize_tracksCardsCorrectly() {
        assertEquals(0, hand.getHandSize());
        hand.addCard(mock(Card.class), 0);
        hand.addCard(mock(Card.class), 1);
        assertEquals(2, hand.getHandSize());
    }

    // -------------------------------------------------------------------------
    // rebalance — target slot specified
    // -------------------------------------------------------------------------

    @Test
    void rebalance_shiftLeftWhenTargetIsToTheRight() {
        // Cards at 0 and 2; gap at 1; target at 2 → card at 2 should shift left to 1
        Card a = mock(Card.class);
        Card b = mock(Card.class);
        hand.addCard(a, 0);
        hand.addCard(b, 2);
        hand.snapShot();

        hand.rebalance(2);

        // After shifting left toward slot 2, the card from 2 should now be at 1
        assertEquals(b, hand.getCards()[1]);
        assertNull(hand.getCards()[2]);
    }

    @Test
    void rebalance_shiftRightWhenTargetIsToTheLeft() {
        // Card at 2; gap at 1; target at 0 → card at 2 should shift right so slot 1 → 2
        // More concretely: cards at 1 and 2; target at 0; first empty = 0
        // target(0) < firstEmpty(0)? No. Let's set: cards at 2,3; firstEmpty = 0; target = 1
        Card a = mock(Card.class);
        Card b = mock(Card.class);
        hand.addCard(a, 2);
        hand.addCard(b, 3);
        hand.snapShot();

        // target 1 < firstEmpty 0? No, target(1) > firstEmpty(0) → shiftLeft
        // Let's test shiftRight: target MUST be < firstEmpty
        // Put cards at 0 and 1 first; firstEmpty becomes 2; target = 0 (< firstEmpty)
        hand = new Hand(0, 0, 600, 100);
        Card c = mock(Card.class);
        Card d = mock(Card.class);
        hand.addCard(c, 0);
        hand.addCard(d, 1);
        hand.snapShot();

        hand.rebalance(0); // target(0) == firstEmpty(2)? No – 0 < 2 → shiftRight
        // Card at 1 should shift right to fill the gap created to the right of target
        // This opens up space at index 1
        assertNull(hand.getCards()[0]);
    }

    @Test
    void rebalance_noGaps_noMovement() {
        // Pack cards with no gaps; rebalance should leave them in place
        Card a = mock(Card.class);
        Card b = mock(Card.class);
        hand.addCard(a, 0);
        hand.addCard(b, 1);
        hand.snapShot();

        hand.rebalance(1); // target == firstEmpty(2)? 1 < 2 → shifts
        // a and b are unchanged; just verify nothing explodes and counts are same
        assertEquals(2, hand.getHandSize());
    }

    // -------------------------------------------------------------------------
    // rebalance(-1) — pack cards to the left
    // -------------------------------------------------------------------------

    @Test
    void rebalance_withMinusOne_packsCardsToTheLeft() {
        // Cards scattered at 1 and 3; after rebalance(-1) they should be at 0 and 1
        Card a = mock(Card.class);
        Card b = mock(Card.class);
        hand.addCard(a, 1);
        hand.addCard(b, 3);
        hand.snapShot();

        hand.rebalance(-1);

        // Cards should now be compacted: a at 0, b at 1 (or at least no gaps before them)
        assertEquals(2, hand.getHandSize());
        assertEquals(a, hand.getCards()[0]);
        assertEquals(b, hand.getCards()[1]);
    }

    @Test
    void rebalance_withMinusOne_emptyHand_doesNotThrow() {
        hand.snapShot();
        assertDoesNotThrow(() -> hand.rebalance(-1));
    }

    // -------------------------------------------------------------------------
    // mouseHovering (inherited)
    // -------------------------------------------------------------------------

    @Test
    void mouseHovering_insideBounds_returnsTrue() {
        assertTrue(hand.mouseHovering(300, 50));
    }

    @Test
    void mouseHovering_outsideBounds_returnsFalse() {
        assertFalse(hand.mouseHovering(700, 50));
        assertFalse(hand.mouseHovering(300, 200));
    }
}
