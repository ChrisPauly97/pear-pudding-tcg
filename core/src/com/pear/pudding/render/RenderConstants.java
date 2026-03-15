package com.pear.pudding.render;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

/**
 * Rendering constants (dimensions, colors, positions).
 * Game constants stay in GameConfig.
 */
public final class RenderConstants {
    // Card dimensions
    public static final float CARD_WIDTH = 100f;
    public static final float CARD_HEIGHT = 140f;

    // Board layout
    public static final float BOARD_CENTER_X = 400f;
    public static final float BOARD_Y = 250f;
    public static final float BOARD_SLOT_SPACING = 120f;

    // Hand layout
    public static final float HAND_Y = 50f;
    public static final float HAND_START_X = 100f;
    public static final float HAND_SLOT_SPACING = 120f;

    // Discard and draw deck positions
    public static final float DISCARD_X = 600f;
    public static final float DISCARD_Y = 100f;
    public static final float DRAW_DECK_X = 600f;
    public static final float DRAW_DECK_Y = 300f;

    // Hero positions
    public static final float HERO1_X = 50f;
    public static final float HERO1_Y = 200f;
    public static final float HERO2_X = 750f;
    public static final float HERO2_Y = 400f;

    // Animation timings
    public static final float CARD_MOVE_DURATION = 0.3f;
    public static final float HOVER_GAP_ANIMATION_DURATION = 0.1f;

    // A simple 1x1 white pixel texture for drawing colored rectangles (lazy-loaded)
    private static Texture PIXEL_TEXTURE = null;

    public static Texture getPixelTexture() {
        if (PIXEL_TEXTURE == null) {
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(1, 1, 1, 1);
            pixmap.fill();
            PIXEL_TEXTURE = new Texture(pixmap);
            pixmap.dispose();
        }
        return PIXEL_TEXTURE;
    }

    private RenderConstants() {}
}
