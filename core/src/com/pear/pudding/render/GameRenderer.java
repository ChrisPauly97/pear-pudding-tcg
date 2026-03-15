package com.pear.pudding.render;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.pear.pudding.game.data.CardRegistry;
import com.pear.pudding.game.event.GameEventListener;
import com.pear.pudding.game.state.*;

/**
 * Main renderer that reads GameState and UIState and produces visual output.
 * Subscribes to GameEventBus for animation triggers.
 */
public class GameRenderer implements GameEventListener {
    private final GameState gameState;
    private final UIState uiState;
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final AssetManager assetManager;
    private BitmapFont font;
    private boolean fontLoaded = false;
    private final com.badlogic.gdx.utils.ObjectMap<String, com.badlogic.gdx.graphics.Texture> cardTextures = new com.badlogic.gdx.utils.ObjectMap<>();

    public GameRenderer(GameState gameState, UIState uiState, AssetManager assetManager) {
        this.gameState = gameState;
        this.uiState = uiState;
        this.assetManager = assetManager;
        preloadCardTextures();
    }

    /**
     * Preload card textures from assets.
     */
    private void preloadCardTextures() {
        String[] cardTypes = {"ghost", "skeleton", "zombie", "ghoul"};
        for (String cardType : cardTypes) {
            try {
                String filename = cardType + "new.png";
                com.badlogic.gdx.files.FileHandle file = com.badlogic.gdx.Gdx.files.internal(filename);
                if (file.exists()) {
                    cardTextures.put(cardType, new com.badlogic.gdx.graphics.Texture(file));
                }
            } catch (Exception e) {
                // Texture not found, use colored rectangles
            }
        }
    }

    /**
     * Renders the entire game state.
     */
    public void render(Batch batch) {
        // Ensure font is loaded
        ensureFontLoaded();

        batch.begin();

        // Draw background
        batch.setColor(new Color(0.15f, 0.20f, 0.25f, 1f));
        batch.draw(RenderConstants.getPixelTexture(), 0, 0, 1800, 1000);

        batch.setColor(Color.WHITE);

        // Draw board zones for both players
        renderBoard(batch);

        // Draw hands
        renderHand(batch, gameState.player1, true);
        renderHand(batch, gameState.player2, false);

        // Draw heroes
        renderHero(batch, gameState.player1, true);
        renderHero(batch, gameState.player2, false);

        // Draw turn info
        renderTurnInfo(batch);

        batch.end();

        // Draw dragged card on top
        if (uiState.draggingCard != null) {
            batch.begin();
            renderDraggedCard(batch);
            batch.end();
        }
    }

    /**
     * Renders the board (both players' minions).
     */
    private void renderBoard(Batch batch) {
        // Player 1 board (bottom, y-up coords)
        renderZoneCards(batch, gameState.player1.getBoard(), 400, 200, true);

        // Player 2 board (top, y-up coords)
        renderZoneCards(batch, gameState.player2.getBoard(), 400, 700, false);
    }

    /**
     * Renders all cards in a zone.
     */
    private void renderZoneCards(Batch batch, ZoneState zone, float startX, float y, boolean faceUp) {
        float cardWidth = 120f;
        float cardHeight = 160f;
        float spacing = 20f;
        float currentX = startX;

        for (int i = 0; i < zone.capacity; i++) {
            String instanceId = zone.getCardAt(i);
            if (instanceId != null) {
                CardInstance card = gameState.cardRepository.find(instanceId);
                if (card != null) {
                    renderCard(batch, card, currentX, y, cardWidth, cardHeight);
                }
                currentX += cardWidth + spacing;
            }
        }
    }

    /**
     * Renders a single card with its stats.
     */
    private void renderCard(Batch batch, CardInstance card, float x, float y, float w, float h) {
        String templateId = card.getTemplateId();

        // Try to use card texture, fall back to colored rectangle
        com.badlogic.gdx.graphics.Texture cardTexture = cardTextures.get(templateId);
        if (cardTexture != null) {
            batch.setColor(Color.WHITE);
            batch.draw(cardTexture, x, y, w, h);
        } else {
            // Fallback: color code cards by type
            Color cardColor = getCardColor(templateId);
            batch.setColor(cardColor);
            batch.draw(RenderConstants.getPixelTexture(), x, y, w, h);
            batch.setColor(Color.WHITE);
            batch.draw(RenderConstants.getPixelTexture(), x, y + h - 8, w, 2);  // Top border
        }

        // Draw card stats on top
        try {
            ensureFontLoaded();
            if (font != null) {
                int cost = CardRegistry.get(templateId).baseCost;
                int attack = CardRegistry.get(templateId).baseAttack;
                int health = CardRegistry.get(templateId).baseHealth;

                // Card name (capitalize first letter)
                String name = templateId.substring(0, 1).toUpperCase() + templateId.substring(1);
                batch.setColor(Color.WHITE);
                font.draw(batch, name, x + 8f, y + h - 20f);

                // Cost at top left (yellow)
                batch.setColor(Color.YELLOW);
                font.draw(batch, "Cost: " + cost, x + 8f, y + h - 35f);

                // Attack at bottom left (red)
                batch.setColor(Color.RED);
                font.draw(batch, "ATK: " + attack, x + 8f, y + 15f);

                // Health at bottom right (green)
                batch.setColor(Color.GREEN);
                font.draw(batch, "HP: " + health, x + w - 50f, y + 15f);

                batch.setColor(Color.WHITE);
            }
        } catch (Exception e) {
            // Ignore rendering errors
        }

        batch.setColor(Color.WHITE);
    }

    /**
     * Ensures the font is loaded from the skin.
     */
    private void ensureFontLoaded() {
        if (!fontLoaded && assetManager.isLoaded("uiskin.json")) {
            try {
                font = assetManager.get("uiskin.json", com.badlogic.gdx.scenes.scene2d.ui.Skin.class).getFont("default");
                fontLoaded = (font != null);
            } catch (Exception e) {
                // Font not available
            }
        }
    }

    /**
     * Returns a color for the card based on its type.
     */
    private Color getCardColor(String templateId) {
        return switch (templateId) {
            case "ghost" -> new Color(0.7f, 0.3f, 0.7f, 1f);      // Purple
            case "skeleton" -> new Color(0.6f, 0.6f, 0.6f, 1f);    // Gray
            case "zombie" -> new Color(0.3f, 0.6f, 0.3f, 1f);      // Green
            case "ghoul" -> new Color(0.6f, 0.3f, 0.3f, 1f);       // Dark red
            default -> new Color(0.3f, 0.4f, 0.6f, 1f);            // Blue
        };
    }

    /**
     * Renders a player's hand.
     */
    private void renderHand(Batch batch, PlayerState player, boolean isPlayer1) {
        ZoneState handZone = player.getHand();
        float y = isPlayer1 ? 20f : 820f;  // y-up: player1 near bottom, player2 near top
        float startX = 50f;

        renderZoneCards(batch, handZone, startX, y, true);
    }

    /**
     * Renders a player's hero.
     */
    private void renderHero(Batch batch, PlayerState player, boolean isPlayer1) {
        float x = 20f;
        float y = isPlayer1 ? 250f : 750f;  // y-up coords
        float w = 200f;
        float h = 120f;

        HeroState hero = player.getHero();

        // Draw hero card with golden border
        batch.setColor(new Color(0.5f, 0.4f, 0.2f, 1f));  // Gold/brown
        batch.draw(RenderConstants.getPixelTexture(), x, y, w, h);

        // Draw inner area (darker)
        batch.setColor(new Color(0.3f, 0.25f, 0.15f, 1f));
        batch.draw(RenderConstants.getPixelTexture(), x + 5f, y + 5f, w - 10f, h - 10f);

        // Draw hero title and stats
        ensureFontLoaded();
        if (font != null) {
            batch.setColor(Color.YELLOW);
            font.draw(batch, isPlayer1 ? "YOUR HERO" : "ENEMY HERO", x + 15f, y + h - 20f);

            // Health and Attack
            batch.setColor(Color.RED);
            font.draw(batch, "HP: " + hero.getHealth(), x + 15f, y + 60f);
            batch.setColor(Color.ORANGE);
            font.draw(batch, "ATK: " + hero.getAttack(), x + 15f, y + 35f);
        }

        // Draw mana pool
        batch.setColor(new Color(0.2f, 0.3f, 0.6f, 1f));  // Blue
        batch.draw(RenderConstants.getPixelTexture(), x + w + 20f, y, 120f, 60f);

        if (font != null) {
            batch.setColor(Color.CYAN);
            font.draw(batch, "MANA", x + w + 30f, y + 45f);
            batch.setColor(Color.WHITE);
            font.draw(batch, player.getCurrentMana() + " / " + player.getTotalMana(),
                x + w + 35f, y + 25f);
        }

        batch.setColor(Color.WHITE);
    }

    /**
     * Renders turn and game info.
     */
    private void renderTurnInfo(Batch batch) {
        if (font != null) {
            String activePlayer = gameState.turn.getActivePlayerId();

            // Draw turn indicator background
            batch.setColor(new Color(0.1f, 0.1f, 0.1f, 0.7f));
            batch.draw(RenderConstants.getPixelTexture(), 600f, 450f, 600f, 100f);

            // Draw turn text
            String turnText = activePlayer.equals("player1") ? "YOUR TURN" : "ENEMY TURN";
            batch.setColor(activePlayer.equals("player1") ? Color.GREEN : Color.RED);
            font.draw(batch, turnText, 750f, 500f);

            // Draw turn number
            batch.setColor(Color.WHITE);
            font.draw(batch, "Turn: " + gameState.turn.getTurnNumber(), 750f, 470f);
        }
    }

    /**
     * Renders the card being dragged.
     */
    private void renderDraggedCard(Batch batch) {
        if (uiState.dragOffset != null && uiState.draggingCard != null) {
            CardInstance card = uiState.draggingCard;
            String templateId = card.getTemplateId();
            float x = uiState.dragOffset.x - 60f;
            float y = uiState.dragOffset.y - 80f;
            float w = 120f;
            float h = 160f;

            // Try to use card texture, fall back to colored rectangle
            com.badlogic.gdx.graphics.Texture cardTexture = cardTextures.get(templateId);
            if (cardTexture != null) {
                batch.setColor(Color.WHITE);
                batch.draw(cardTexture, x, y, w, h);
            } else {
                Color cardColor = getCardColor(templateId);
                batch.setColor(cardColor);
                batch.draw(RenderConstants.getPixelTexture(), x, y, w, h);
                batch.setColor(Color.WHITE);
                batch.draw(RenderConstants.getPixelTexture(), x, y + h - 8, w, 2);
            }

            // Draw stats on top
            if (font != null) {
                try {
                    int cost = CardRegistry.get(templateId).baseCost;
                    int attack = CardRegistry.get(templateId).baseAttack;
                    int health = CardRegistry.get(templateId).baseHealth;

                    batch.setColor(Color.WHITE);
                    String name = templateId.substring(0, 1).toUpperCase() + templateId.substring(1);
                    font.draw(batch, name, x + 8f, y + h - 20f);

                    batch.setColor(Color.YELLOW);
                    font.draw(batch, "Cost: " + cost, x + 8f, y + h - 35f);

                    batch.setColor(Color.RED);
                    font.draw(batch, "ATK: " + attack, x + 8f, y + 15f);

                    batch.setColor(Color.GREEN);
                    font.draw(batch, "HP: " + health, x + w - 50f, y + 15f);

                    batch.setColor(Color.WHITE);
                } catch (Exception e) {
                    // Ignore rendering errors
                }
            }
        }
    }

    @Override
    public void onEvent(com.pear.pudding.game.state.GameEvent event) {
        // Trigger animations based on events
        // TODO: Implement animations for card played, card drawn, etc.
    }

    /**
     * Cleans up resources.
     */
    public void dispose() {
        for (com.badlogic.gdx.graphics.Texture texture : cardTextures.values()) {
            texture.dispose();
        }
        cardTextures.clear();
        shapeRenderer.dispose();
    }
}
