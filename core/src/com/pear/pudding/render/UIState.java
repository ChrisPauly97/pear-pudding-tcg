package com.pear.pudding.render;

import com.badlogic.gdx.math.Vector2;
import com.pear.pudding.game.state.CardInstance;

/**
 * UI-only state that tracks presentation concerns, not game state.
 * This includes drag state, hover animations, zoomed card preview, etc.
 */
public class UIState {
    // Dragging state
    public CardInstance draggingCard;
    public Vector2 dragOffset;
    public String dragSourceZone;  // which zone card came from

    // Hover state for animations
    public Integer boardHoverSlot;
    public Integer handHoverSlot;

    // Zoomed card preview state
    public CardInstance zoomedCard;

    // Layout display state (for rendering gaps)
    public int[] boardDisplaySlots;  // rebalanced board positions
    public int[] handDisplaySlots;   // rebalanced hand positions

    public UIState() {
        this.boardDisplaySlots = new int[5];
        this.handDisplaySlots = new int[5];
        resetDisplaySlots();
    }

    /**
     * Resets display slots to identity (no rebalancing).
     */
    public void resetDisplaySlots() {
        for (int i = 0; i < boardDisplaySlots.length; i++) {
            boardDisplaySlots[i] = i;
            handDisplaySlots[i] = i;
        }
    }

    /**
     * Clears all UI state (e.g., on game end).
     */
    public void clear() {
        draggingCard = null;
        dragOffset = null;
        dragSourceZone = null;
        boardHoverSlot = null;
        handHoverSlot = null;
        zoomedCard = null;
        resetDisplaySlots();
    }
}
