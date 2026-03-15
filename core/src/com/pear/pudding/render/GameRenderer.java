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
        // Map of card type to actual filename
        java.util.Map<String, String> cardFiles = new java.util.HashMap<>();
        cardFiles.put("ghost", "ghostnew.png");
        cardFiles.put("skeleton", "skeleton.jpg");
        cardFiles.put("zombie", "ghoul.png");  // Use ghoul for zombie (no zombie.png exists)
        cardFiles.put("ghoul", "ghoul.png");

        for (String cardType : cardFiles.keySet()) {
            try {
                String filename = cardFiles.get(cardType);
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
     * Renders enemy board (centered, above enemy hero, touching player board).
     */
    private void renderEnemyBoard(Batch batch) {
        float boardWidth = 700f;
        float boardHeight = 140f;
        float centerX = 900f;  // Center of 1800px screen
        float boardX = centerX - boardWidth / 2;
        float boardY = 550f;  // Below enemy hero

        // Draw zone boundary
        batch.setColor(new Color(0.3f, 0.2f, 0.2f, 0.5f));
        batch.draw(RenderConstants.getPixelTexture(), boardX, boardY, boardWidth, boardHeight);
        batch.setColor(Color.WHITE);
        batch.draw(RenderConstants.getPixelTexture(), boardX, boardY, boardWidth, 2f);  // Top line

        // Check if dragged card can attack
        boolean canDraggedCardAttack = false;
        if ("board".equals(uiState.dragSourceZone) && uiState.draggingCard != null) {
            canDraggedCardAttack = uiState.draggingCard.canAttack();
        }

        // Render cards (centered with 10px margin on each side: 5*120 + 4*20 = 680, leaves 20px total)
        float cardsStartX = boardX + 10f;
        renderZoneCardsWithGap(batch, gameState.player2.getBoard(), cardsStartX, boardY + 15f, false, uiState.dragSourceZone, null, canDraggedCardAttack);
    }

    /**
     * Renders player board (centered, above player hero).
     */
    private void renderPlayerBoard(Batch batch) {
        float boardWidth = 700f;
        float boardHeight = 140f;
        float centerX = 900f;  // Center of 1800px screen
        float boardX = centerX - boardWidth / 2;
        float boardY = 290f;  // Above player hero

        // Draw zone boundary
        batch.setColor(new Color(0.2f, 0.3f, 0.4f, 0.5f));
        batch.draw(RenderConstants.getPixelTexture(), boardX, boardY, boardWidth, boardHeight);
        batch.setColor(Color.WHITE);
        batch.draw(RenderConstants.getPixelTexture(), boardX, boardY, boardWidth, 2f);  // Top line

        // Check if dragged card can attack
        boolean canDraggedCardAttack = false;
        if ("board".equals(uiState.dragSourceZone) && uiState.draggingCard != null) {
            canDraggedCardAttack = uiState.draggingCard.canAttack();
        }

        // Render cards with hover gap (centered with 10px margin on each side)
        float cardsStartX = boardX + 10f;
        renderZoneCardsWithGap(batch, gameState.player1.getBoard(), cardsStartX, boardY + 15f, true, uiState.dragSourceZone, uiState.boardHoverSlot, canDraggedCardAttack);
    }

    /**
     * Renders all cards in a zone with optional context.
     */
    private void renderZoneCards(Batch batch, ZoneState zone, float startX, float y, boolean isPlayerZone) {
        renderZoneCards(batch, zone, startX, y, isPlayerZone, null);
    }

    /**
     * Renders all cards in a zone with context-aware outline colors.
     */
    private void renderZoneCards(Batch batch, ZoneState zone, float startX, float y, boolean isPlayerZone, String dragSourceZone) {
        boolean canDraggedCardAttack = false;
        if ("board".equals(dragSourceZone) && uiState.draggingCard != null) {
            canDraggedCardAttack = uiState.draggingCard.canAttack();
        }
        renderZoneCardsWithGap(batch, zone, startX, y, isPlayerZone, dragSourceZone, null, canDraggedCardAttack);
    }

    /**
     * Renders all cards in a zone with a hover gap for drag preview.
     */
    private void renderZoneCardsWithGap(Batch batch, ZoneState zone, float startX, float y, boolean isPlayerZone, String dragSourceZone, Integer hoverSlot, boolean canDraggedCardAttack) {
        float cardWidth = 120f;
        float cardHeight = 160f;
        float spacing = 20f;

        boolean isDraggingBoard = "board".equals(dragSourceZone);
        boolean isDraggingFromHand = "hand".equals(dragSourceZone) && isPlayerZone;
        boolean isDraggingFromBoard = isDraggingBoard && isPlayerZone;

        // If dragging from hand or board over player board with hover slot, rebalance the display
        if ((isDraggingFromHand || isDraggingFromBoard) && hoverSlot != null) {
            String[] rebalanced = computeRebalancedSlots(zone, hoverSlot);
            renderRebalancedZone(batch, rebalanced, startX, y, isPlayerZone, isDraggingBoard && canDraggedCardAttack, cardWidth, cardHeight, spacing);
        } else {
            // Normal rendering
            for (int i = 0; i < zone.capacity; i++) {
                String instanceId = zone.getCardAt(i);
                if (instanceId != null) {
                    CardInstance card = gameState.cardRepository.find(instanceId);
                    if (card != null) {
                        float cardX = startX + i * (cardWidth + spacing);
                        Color outlineColor = null;

                        if (isPlayerZone) {
                            // Player cards: green if playable (in hand) or can attack (on board)
                            if (card.getCurrentZone() == ZoneType.HAND && isCardPlayable(card)) {
                                outlineColor = Color.GREEN;
                            } else if (card.getCurrentZone() == ZoneType.BOARD && card.canAttack()) {
                                outlineColor = Color.GREEN;
                            }
                        } else {
                            // Enemy cards: red if dragging an attackable board card (valid attack target)
                            if (isDraggingBoard && canDraggedCardAttack && card.getCurrentZone() == ZoneType.BOARD) {
                                outlineColor = Color.RED;
                            }
                        }

                        renderCard(batch, card, cardX, y, cardWidth, cardHeight, outlineColor);
                    }
                }
            }
        }
    }

    /**
     * Computes rebalanced slot arrangement with dragged card at target position.
     * Other cards shift to make room.
     */
    private String[] computeRebalancedSlots(ZoneState zone, int targetSlot) {
        String[] result = new String[zone.capacity];
        java.util.List<String> cardIds = new java.util.ArrayList<>();

        // Collect all cards except the dragged one
        for (int i = 0; i < zone.capacity; i++) {
            String id = zone.getCardAt(i);
            if (id != null && !id.equals(uiState.draggingCard.getInstanceId())) {
                cardIds.add(id);
            }
        }

        // Fill slots: cards before target stay in order, then gap, then remaining cards
        int writeIndex = 0;
        for (int i = 0; i < cardIds.size(); i++) {
            if (writeIndex == targetSlot) {
                writeIndex++; // Skip target slot (for dragged card)
            }
            result[writeIndex++] = cardIds.get(i);
        }

        return result;
    }

    /**
     * Renders a zone using rebalanced slot positions.
     */
    private void renderRebalancedZone(Batch batch, String[] rebalancedSlots, float startX, float y, boolean isPlayerZone, boolean isDraggingBoard, float cardWidth, float cardHeight, float spacing) {
        for (int i = 0; i < rebalancedSlots.length; i++) {
            if (i == uiState.boardHoverSlot) {
                // Render the gap where the dragged card will go
                batch.setColor(new Color(0.3f, 0.5f, 0.3f, 0.3f));  // Green semi-transparent
                float gapX = startX + i * (cardWidth + spacing);
                batch.draw(RenderConstants.getPixelTexture(), gapX, y, cardWidth, cardHeight);
                batch.setColor(Color.WHITE);
            } else if (rebalancedSlots[i] != null) {
                // Render the card
                CardInstance card = gameState.cardRepository.find(rebalancedSlots[i]);
                if (card != null) {
                    float cardX = startX + i * (cardWidth + spacing);
                    Color outlineColor = null;

                    if (isPlayerZone) {
                        if (card.getCurrentZone() == ZoneType.HAND && isCardPlayable(card)) {
                            outlineColor = Color.GREEN;
                        } else if (card.getCurrentZone() == ZoneType.BOARD && card.canAttack()) {
                            outlineColor = Color.GREEN;
                        }
                    } else {
                        if (isDraggingBoard && card.getCurrentZone() == ZoneType.BOARD) {
                            outlineColor = Color.RED;
                        }
                    }

                    renderCard(batch, card, cardX, y, cardWidth, cardHeight, outlineColor);
                }
            }
        }
    }

    /**
     * Renders a single card with its stats.
     */
    private void renderCard(Batch batch, CardInstance card, float x, float y, float w, float h) {
        renderCard(batch, card, x, y, w, h, null);
    }

    /**
     * Renders a single card with optional outline color.
     */
    private void renderCard(Batch batch, CardInstance card, float x, float y, float w, float h, Color outlineColor) {
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

        // Draw colored outline based on context
        if (outlineColor != null) {
            batch.setColor(outlineColor);
            batch.draw(RenderConstants.getPixelTexture(), x, y, w, 3);  // Bottom
            batch.draw(RenderConstants.getPixelTexture(), x, y + h - 3, w, 3);  // Top
            batch.draw(RenderConstants.getPixelTexture(), x, y, 3, h);  // Left
            batch.draw(RenderConstants.getPixelTexture(), x + w - 3, y, 3, h);  // Right
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
     * Checks if a card can be played from hand.
     */
    private boolean isCardPlayable(CardInstance card) {
        PlayerState player = gameState.player1;
        if (player.getHand().findSlot(card.getInstanceId()) == -1) {
            return false;
        }

        // Check if it's player1's turn
        if (!gameState.turn.getActivePlayerId().equals("player1")) {
            return false;
        }

        // Check if player can afford it and board not full
        int cost = CardRegistry.get(card.getTemplateId()).baseCost;
        return player.canAfford(cost) && !player.getBoard().isFull();
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
            // MY hand at bottom (y=0 is bottom in y-up coords)
            float handY = 0f;

            // Draw zone boundary for hand
            batch.setColor(new Color(0.2f, 0.4f, 0.3f, 0.5f));
            batch.draw(RenderConstants.getPixelTexture(), handX, handY, handWidth, handHeight);
            batch.setColor(Color.WHITE);
            batch.draw(RenderConstants.getPixelTexture(), handX, handY + handHeight, handWidth, 2f);  // Top line

            // Render cards centered in hand zone (10px margin, cards at y=20 to stay within boundary)
            float cardsStartX = handX + 10f;
            renderZoneCards(batch, handZone, cardsStartX, handY + 20f, true);
        } else {
            // Enemy hand at top (y=860+ is near top in y-up coords)
            float handY = 860f;

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
     * Renders a player's hero centered on the screen.
     */
    private void renderHero(Batch batch, PlayerState player, boolean isPlayer1) {
        float w = 180f;
        float h = 110f;
        float centerX = 900f;  // Center of 1800px screen
        float x = centerX - w / 2;  // Center the hero box
        float y = isPlayer1 ? 145f : 715f;  // Player hero: y=145-255, Enemy hero: y=715-825

        HeroState hero = player.getHero();

        // Draw hero card with golden border
        batch.setColor(new Color(0.5f, 0.4f, 0.2f, 1f));  // Gold/brown
        batch.draw(RenderConstants.getPixelTexture(), x, y, w, h);

        // Draw inner area (darker)
        batch.setColor(new Color(0.3f, 0.25f, 0.15f, 1f));
        batch.draw(RenderConstants.getPixelTexture(), x + 5f, y + 5f, w - 10f, h - 10f);

        // Draw red outline if enemy hero is a valid attack target (only if dragged card can attack)
        if (!isPlayer1 && "board".equals(uiState.dragSourceZone) && uiState.draggingCard != null && uiState.draggingCard.canAttack()) {
            batch.setColor(Color.RED);
            batch.draw(RenderConstants.getPixelTexture(), x, y, w, 3);  // Bottom
            batch.draw(RenderConstants.getPixelTexture(), x, y + h - 3, w, 3);  // Top
            batch.draw(RenderConstants.getPixelTexture(), x, y, 3, h);  // Left
            batch.draw(RenderConstants.getPixelTexture(), x + w - 3, y, 3, h);  // Right
        }

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
            float manaY = 15f;
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
            float deckY = 100f;
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
            float manaY = 915f;
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
            float deckY = 840f;
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
        }

        batch.setColor(Color.WHITE);
    }

    /**
     * Renders turn and game info at top right.
     */
    private void renderTurnInfo(Batch batch) {
        if (font != null) {
            String activePlayer = gameState.turn.getActivePlayerId();

            // Draw turn indicator at top right
            float infoX = 1500f;
            float infoY = 900f;

            // Draw turn text
            String turnText = activePlayer.equals("player1") ? "YOUR TURN" : "ENEMY TURN";
            batch.setColor(activePlayer.equals("player1") ? Color.GREEN : Color.RED);
            font.draw(batch, turnText, infoX, infoY);

            // Draw turn number
            batch.setColor(Color.WHITE);
            font.draw(batch, "Turn: " + gameState.turn.getTurnNumber(), infoX, infoY - 25f);
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
