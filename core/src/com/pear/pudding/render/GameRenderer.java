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
     * Layout from top to bottom (centered):
     * 1. Enemy hand
     * 2. Enemy hero
     * 3. Enemy board
     * 4. Player board
     * 5. Player hero
     * 6. Player hand
     */
    public void render(Batch batch) {
        // Ensure font is loaded
        ensureFontLoaded();

        batch.begin();

        // Draw background
        batch.setColor(new Color(0.15f, 0.20f, 0.25f, 1f));
        batch.draw(RenderConstants.getPixelTexture(), 0, 0, 1800, 1000);

        batch.setColor(Color.WHITE);

        // Top to bottom layout:
        renderHand(batch, gameState.player2, false);      // Enemy hand (top)
        renderHero(batch, gameState.player2, false);      // Enemy hero
        renderEnemyBoard(batch);                          // Enemy board
        renderPlayerBoard(batch);                         // Player board
        renderHero(batch, gameState.player1, true);       // Player hero
        renderHand(batch, gameState.player1, true);       // Player hand (bottom)

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
     * Renders enemy board (centered, below enemy hero).
     */
    private void renderEnemyBoard(Batch batch) {
        float boardWidth = 700f;
        float boardHeight = 150f;
        float centerX = 900f;  // Center of 1800px screen
        float boardX = centerX - boardWidth / 2;
        float boardY = 350f;  // Below enemy hero

        // Draw zone boundary
        batch.setColor(new Color(0.3f, 0.2f, 0.2f, 0.5f));
        batch.draw(RenderConstants.getPixelTexture(), boardX, boardY, boardWidth, boardHeight);
        batch.setColor(Color.WHITE);
        batch.draw(RenderConstants.getPixelTexture(), boardX, boardY, boardWidth, 2f);  // Top line

        // Render cards
        float cardsStartX = boardX + 90f;
        renderZoneCards(batch, gameState.player2.getBoard(), cardsStartX, boardY + 15f, false);
    }

    /**
     * Renders player board (centered, above player hero).
     */
    private void renderPlayerBoard(Batch batch) {
        float boardWidth = 700f;
        float boardHeight = 150f;
        float centerX = 900f;  // Center of 1800px screen
        float boardX = centerX - boardWidth / 2;
        float boardY = 200f;  // Above player hero

        // Draw zone boundary
        batch.setColor(new Color(0.2f, 0.3f, 0.4f, 0.5f));
        batch.draw(RenderConstants.getPixelTexture(), boardX, boardY, boardWidth, boardHeight);
        batch.setColor(Color.WHITE);
        batch.draw(RenderConstants.getPixelTexture(), boardX, boardY, boardWidth, 2f);  // Top line

        // Render cards
        float cardsStartX = boardX + 90f;
        renderZoneCards(batch, gameState.player1.getBoard(), cardsStartX, boardY + 15f, true);
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
                com.badlogic.gdx.scenes.scene2d.ui.Skin skin = assetManager.get("uiskin.json", com.badlogic.gdx.scenes.scene2d.ui.Skin.class);
                // Try default-font first, then fall back to default
                font = skin.getFont("default-font");
                if (font == null) {
                    font = skin.getFont("default");
                }
                fontLoaded = (font != null);
            } catch (Exception e) {
                // Font not available
                fontLoaded = false;
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
     * Renders a player's hand with zone boundary.
     */
    private void renderHand(Batch batch, PlayerState player, boolean isPlayer1) {
        ZoneState handZone = player.getHand();
        float handWidth = 700f;
        float handHeight = 140f;
        float centerX = 900f;
        float handX = centerX - handWidth / 2;

        if (isPlayer1) {
            // MY hand at bottom
            float handY = 0f;

            // Draw zone boundary for hand
            batch.setColor(new Color(0.2f, 0.4f, 0.3f, 0.5f));
            batch.draw(RenderConstants.getPixelTexture(), handX, handY, handWidth, handHeight);
            batch.setColor(Color.WHITE);
            batch.draw(RenderConstants.getPixelTexture(), handX, handY + handHeight, handWidth, 2f);  // Top line

            // Render cards centered in hand zone
            float cardsStartX = handX + 90f;
            renderZoneCards(batch, handZone, cardsStartX, handY + 10f, true);
        } else {
            // Enemy hand at top - show card count instead of individual cards
            float handY = 850f;

            // Draw zone boundary
            batch.setColor(new Color(0.3f, 0.3f, 0.2f, 0.5f));
            batch.draw(RenderConstants.getPixelTexture(), handX, handY, handWidth, handHeight);
            batch.setColor(Color.WHITE);
            batch.draw(RenderConstants.getPixelTexture(), handX, handY, handWidth, 2f);  // Bottom line

            // Show enemy hand count instead of individual cards
            if (font != null) {
                batch.setColor(Color.CYAN);
                font.draw(batch, "Enemy Hand: " + handZone.size() + " cards", handX + 20f, handY + 60f);
            }
        }
    }

    /**
     * Renders a player's hero on the left side.
     */
    private void renderHero(Batch batch, PlayerState player, boolean isPlayer1) {
        float x = 20f;
        float w = 180f;
        float h = 130f;
        float y = isPlayer1 ? 70f : 710f;  // Player hero: y=70-200, Enemy hero: y=710-840

        HeroState hero = player.getHero();

        // Draw hero card with golden border
        batch.setColor(new Color(0.5f, 0.4f, 0.2f, 1f));  // Gold/brown
        batch.draw(RenderConstants.getPixelTexture(), x, y, w, h);

        // Draw inner area (darker)
        batch.setColor(new Color(0.3f, 0.25f, 0.15f, 1f));
        batch.draw(RenderConstants.getPixelTexture(), x + 5f, y + 5f, w - 10f, h - 10f);

        // Draw hero title and stats
        if (font != null) {
            batch.setColor(Color.YELLOW);
            font.draw(batch, isPlayer1 ? "YOUR HERO" : "ENEMY HERO", x + 10f, y + h - 20f);

            // Health and Attack
            batch.setColor(Color.RED);
            font.draw(batch, "HP: " + hero.getHealth(), x + 10f, y + 70f);
            batch.setColor(Color.ORANGE);
            font.draw(batch, "ATK: " + hero.getAttack(), x + 10f, y + 45f);
        }

        batch.setColor(Color.WHITE);

        // Mana pool and draw deck positioning
        if (isPlayer1) {
            // MY mana and draw deck: bottom right corner
            float manaX = 1800f - 150f;
            float manaY = 20f;
            float manaW = 130f;
            float manaH = 70f;

            batch.setColor(new Color(0.2f, 0.3f, 0.6f, 1f));  // Blue
            batch.draw(RenderConstants.getPixelTexture(), manaX, manaY, manaW, manaH);

            if (font != null) {
                batch.setColor(Color.CYAN);
                font.draw(batch, "MANA", manaX + 20f, manaY + 50f);
                batch.setColor(Color.WHITE);
                font.draw(batch, player.getCurrentMana() + " / " + player.getTotalMana(),
                    manaX + 25f, manaY + 25f);
            }

            // Draw deck below mana
            float deckX = manaX;
            float deckY = manaY - 80f;
            float deckW = 130f;
            float deckH = 60f;

            batch.setColor(new Color(0.3f, 0.3f, 0.3f, 1f));  // Gray
            batch.draw(RenderConstants.getPixelTexture(), deckX, deckY, deckW, deckH);
            batch.setColor(Color.WHITE);
            batch.draw(RenderConstants.getPixelTexture(), deckX, deckY, deckW, 2f);  // Top line

            if (font != null) {
                batch.setColor(Color.WHITE);
                font.draw(batch, "Deck: " + player.getDrawDeck().size(), deckX + 10f, deckY + 35f);
            }
        } else {
            // Enemy mana and draw deck: top right corner
            float manaX = 1800f - 150f;
            float manaY = 900f;
            float manaW = 130f;
            float manaH = 70f;

            batch.setColor(new Color(0.2f, 0.3f, 0.6f, 1f));  // Blue
            batch.draw(RenderConstants.getPixelTexture(), manaX, manaY, manaW, manaH);

            if (font != null) {
                batch.setColor(Color.CYAN);
                font.draw(batch, "MANA", manaX + 20f, manaY + 50f);
                batch.setColor(Color.WHITE);
                font.draw(batch, player.getCurrentMana() + " / " + player.getTotalMana(),
                    manaX + 25f, manaY + 25f);
            }

            // Draw deck above mana
            float deckX = manaX;
            float deckY = manaY + 70f;
            float deckW = 130f;
            float deckH = 60f;

            batch.setColor(new Color(0.3f, 0.3f, 0.3f, 1f));  // Gray
            batch.draw(RenderConstants.getPixelTexture(), deckX, deckY, deckW, deckH);
            batch.setColor(Color.WHITE);
            batch.draw(RenderConstants.getPixelTexture(), deckX, deckY + deckH, deckW, 2f);  // Top line

            if (font != null) {
                batch.setColor(Color.WHITE);
                font.draw(batch, "Deck: " + player.getDrawDeck().size(), deckX + 10f, deckY + 35f);
            }
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
