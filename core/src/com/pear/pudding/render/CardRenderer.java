package com.pear.pudding.render;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.Color;
import com.pear.pudding.game.data.CardRegistry;
import com.pear.pudding.game.state.CardInstance;

/**
 * Renders a single card. Reads from CardInstance (game state) to determine what to draw.
 */
public class CardRenderer {
    private final CardInstance card;
    private float x;
    private float y;
    private boolean faceUp;

    public CardRenderer(CardInstance card, float x, float y, boolean faceUp) {
        this.card = card;
        this.x = x;
        this.y = y;
        this.faceUp = faceUp;
    }

    /**
     * Renders the card at its current position.
     */
    public void render(Batch batch) {
        if (!faceUp) {
            // Draw back of card
            drawCardBack(batch);
            return;
        }

        // Draw card front
        var template = CardRegistry.get(card.getTemplateId());

        // Draw background
        batch.setColor(getCardColor());
        batch.draw(getCardTexture(), x, y, RenderConstants.CARD_WIDTH, RenderConstants.CARD_HEIGHT);

        // Draw stats (simplified)
        // In a real implementation, use BitmapFont
        // drawStats(batch, template.baseAttack, card.getCurrentHealth());

        batch.setColor(Color.WHITE);
    }

    /**
     * Draws the back of a card (face-down).
     */
    private void drawCardBack(Batch batch) {
        batch.setColor(Color.DARK_GRAY);
        batch.draw(getCardBackTexture(), x, y, RenderConstants.CARD_WIDTH, RenderConstants.CARD_HEIGHT);
        batch.setColor(Color.WHITE);
    }

    /**
     * Gets the card color based on its class.
     */
    private Color getCardColor() {
        var template = CardRegistry.get(card.getTemplateId());
        return switch (template.cardClass) {
            case ETHEREAL -> new Color(0.8f, 0.8f, 1f, 1f);  // Light blue
            case UNDEAD -> new Color(0.6f, 0.8f, 0.6f, 1f);   // Light green
        };
    }

    private com.badlogic.gdx.graphics.Texture getCardTexture() {
        // TODO: Load textures from CardTemplate
        return null;
    }

    private com.badlogic.gdx.graphics.Texture getCardBackTexture() {
        // TODO: Load back texture
        return null;
    }

    // Getters/Setters
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public void setFaceUp(boolean faceUp) { this.faceUp = faceUp; }
}
