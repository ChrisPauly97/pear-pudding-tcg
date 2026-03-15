package com.pear.pudding.screen;

import com.badlogic.gdx.*;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.pear.pudding.MyGame;
import com.pear.pudding.game.action.ActionResult;
import com.pear.pudding.game.action.DrawCardAction;
import com.pear.pudding.game.action.EndTurnAction;
import com.pear.pudding.game.action.GameAction;
import com.pear.pudding.game.action.PlayCardAction;
import com.pear.pudding.game.ai.BasicAIStrategy;
import com.pear.pudding.game.data.GameConfig;
import com.pear.pudding.game.data.GameConstants;
import com.pear.pudding.game.event.GameEventBus;
import com.pear.pudding.game.state.*;
import com.pear.pudding.render.GameRenderer;
import com.pear.pudding.render.UIState;

import java.util.ArrayList;
import java.util.List;

import static com.pear.pudding.game.data.GameConstants.*;


public class PearPudding implements Screen, InputProcessor {
    private final MyGame game;
    private final AssetManager manager;
    private final OrthographicCamera camera;
    private final Stage stage;

    // Game state and architecture
    private final GameState gameState;
    private final GameEventBus eventBus;
    private final GameRenderer gameRenderer;
    private final UIState uiState;
    private final BasicAIStrategy aiStrategy;

    // UI elements
    private Button endTurnButton;
    private Button backButton;

    // Input tracking
    private Vector2 lastMousePos = Vector2.Zero;
    private boolean isAITurn = false;
    private float aiActionDelay = 0f;
    private List<GameAction> aiActions = new ArrayList<>();

    public PearPudding(MyGame game, AssetManager manager) {
        this.game = game;
        this.manager = manager;

        // Initialize camera and stage (use standard LibGDX: y-up, origin at bottom-left)
        camera = new OrthographicCamera();
        camera.setToOrtho(false, WINDOW_WIDTH, WINDOW_HEIGHT);
        camera.position.set(WINDOW_WIDTH / 2, WINDOW_HEIGHT / 2, 0);
        camera.update();
        FitViewport viewport = new FitViewport(WINDOW_WIDTH, WINDOW_HEIGHT, camera);
        stage = new Stage(viewport);

        // Initialize pure game state
        TurnState turnState = new TurnState("player1");
        PlayerState p1 = new PlayerState("player1", GameConfig.STARTING_MANA,
            GameConfig.STARTING_HERO_HEALTH, GameConfig.STARTING_HERO_ATTACK,
            GameConfig.HAND_CAPACITY, GameConfig.BOARD_CAPACITY, GameConfig.DECK_CAPACITY);
        PlayerState p2 = new PlayerState("player2", GameConfig.STARTING_MANA,
            GameConfig.STARTING_HERO_HEALTH, GameConfig.STARTING_HERO_ATTACK,
            GameConfig.HAND_CAPACITY, GameConfig.BOARD_CAPACITY, GameConfig.DECK_CAPACITY);
        gameState = new GameState(p1, p2, turnState);

        // Initialize supporting systems
        eventBus = new GameEventBus();
        uiState = new UIState();
        gameRenderer = new GameRenderer(gameState, uiState, manager);
        aiStrategy = new BasicAIStrategy();

        // Subscribe renderer to events
        eventBus.subscribe(gameRenderer);

        // Initialize starting deck with 4 copies of each card
        initializeDeck(p1);
        initializeDeck(p2);

        // Draw starting hand
        for (int i = 0; i < GameConfig.STARTING_HAND_SIZE; i++) {
            ActionResult result = new DrawCardAction("player1").execute(gameState);
            gameState.eventLog.addAll(result.events);
        }

        // Increment initial mana for player1
        p1.setTotalMana(GameConfig.STARTING_MANA + 1);
        p1.refillMana();

        // Create UI buttons
        createUI();

        // Set up input
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stage);
        multiplexer.addProcessor(this);
        Gdx.input.setInputProcessor(multiplexer);
    }

    /**
     * Initializes a player's deck with cards.
     */
    private void initializeDeck(PlayerState player) {
        // Add 4 copies of each card type
        String[] cardTypes = {"ghost", "skeleton", "zombie", "ghoul"};
        ZoneState drawDeck = player.getDrawDeck();

        for (String templateId : cardTypes) {
            for (int i = 0; i < 4; i++) {
                CardInstance card = com.pear.pudding.game.data.CardRegistry.createInstance(templateId, player.getPlayerId());
                card.setCurrentZone(ZoneType.DRAW_DECK);
                gameState.cardRepository.register(card);
                drawDeck.addCard(card.getInstanceId());
            }
        }
    }

    /**
     * Creates UI buttons.
     */
    private void createUI() {
        Skin skin = manager.get("uiskin.json", Skin.class);

        endTurnButton = new TextButton("End Turn", skin);
        endTurnButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                endPlayerTurn();
            }
        });
        endTurnButton.setBounds(WINDOW_WIDTH - 100 - BUFFER, WINDOW_HEIGHT / 2, 100, 40);

        backButton = new TextButton("Back", skin);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MenuScreen(game));
            }
        });
        backButton.setBounds(WINDOW_WIDTH - 100 - BUFFER, WINDOW_HEIGHT / 2 + 80, 100, 40);

        stage.addActor(endTurnButton);
        stage.addActor(backButton);
    }

    /**
     * Ends the current player's turn and starts the next player's turn.
     */
    private void endPlayerTurn() {
        String activePlayerId = gameState.turn.getActivePlayerId();
        GameAction action = new EndTurnAction(activePlayerId);
        action.execute(gameState);
        eventBus.publishAll(gameState.eventLog);
        gameState.eventLog.clear();

        // Schedule AI turn if player2 is AI
        if (gameState.turn.getActivePlayerId().equals("player2")) {
            isAITurn = true;
            aiActionDelay = 0.5f;  // Start AI actions after a delay
        }
    }

    /**
     * Executes AI actions one at a time, spread out with delays for visual feedback.
     */
    private void executeAITurn() {
        if (!isAITurn) return;

        aiActionDelay -= Gdx.graphics.getDeltaTime();
        if (aiActionDelay > 0) return;

        // If we don't have actions queued, ask AI for them
        if (aiActions.isEmpty()) {
            aiActions.addAll(aiStrategy.decideTurn(gameState, "player2"));
        }

        // Execute one action
        if (!aiActions.isEmpty()) {
            GameAction action = aiActions.remove(0);
            action.execute(gameState);
            eventBus.publishAll(gameState.eventLog);
            gameState.eventLog.clear();

            // Schedule next action or end turn
            if (aiActions.isEmpty()) {
                isAITurn = false;
            } else {
                aiActionDelay = 0.3f;  // Delay between actions
            }
        }
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        try {
            ScreenUtils.clear(184, 176, 155, 0);

            // Execute AI actions if it's AI's turn
            executeAITurn();

            // Render game using GameRenderer
            Batch batch = stage.getBatch();
            batch.setProjectionMatrix(camera.combined);
            gameRenderer.render(batch);

            // Render UI buttons on top
            stage.draw();

        } catch (Exception e) {
            Gdx.app.error("PearPudding", "Error during rendering", e);
        }
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        stage.dispose();
        manager.dispose();
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        Vector3 mouseCoords = camera.unproject(new Vector3(screenX, screenY, camera.position.z));
        lastMousePos.set(mouseCoords.x, mouseCoords.y);

        // Try to pick a card from the player's hand
        CardInstance card = pickCardFromHand(mouseCoords.x, mouseCoords.y);
        if (card != null) {
            uiState.draggingCard = card;
            uiState.dragSourceZone = "hand";
            uiState.dragOffset = new com.badlogic.gdx.math.Vector2(mouseCoords.x, mouseCoords.y);
            return true;
        }

        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (uiState.draggingCard == null) return false;

        Vector3 mouseCoords = camera.unproject(new Vector3(screenX, screenY, camera.position.z));

        // Try to resolve drop
        resolveDrop(mouseCoords.x, mouseCoords.y);

        // Clear drag state
        uiState.draggingCard = null;
        uiState.dragOffset = null;
        uiState.dragSourceZone = null;

        return true;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        // Cancel drag
        uiState.draggingCard = null;
        uiState.dragOffset = null;
        uiState.dragSourceZone = null;
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (uiState.draggingCard == null) return false;

        Vector3 mouseCoords = camera.unproject(new Vector3(screenX, screenY, camera.position.z));
        uiState.dragOffset.set(mouseCoords.x, mouseCoords.y);

        return true;
    }

    /**
     * Attempts to pick a card from player1's hand at screen coordinates.
     */
    private CardInstance pickCardFromHand(float x, float y) {
        // Player hand is at y 0-140 (bottom of screen with y-up coords), centered x
        if (y < 0 || y > 140) return null;

        ZoneState hand = gameState.player1.getHand();
        float cardWidth = 120f;
        float spacing = 20f;
        float handCenterX = 900f;  // Center of 1800px screen
        float handStartX = handCenterX - 350f;  // Center 5 cards

        for (int i = 0; i < hand.capacity; i++) {
            String instanceId = hand.getCardAt(i);
            if (instanceId != null) {
                float cardX = handStartX + 90f + i * (cardWidth + spacing);
                if (x >= cardX && x < cardX + cardWidth) {
                    return gameState.cardRepository.find(instanceId);
                }
            }
        }
        return null;
    }

    /**
     * Resolves where the dragged card should be played.
     */
    private void resolveDrop(float x, float y) {
        if (uiState.draggingCard == null) return;

        CardInstance card = uiState.draggingCard;
        String activePlayer = gameState.turn.getActivePlayerId();

        // Only allow playing cards if it's player1's turn
        if (!activePlayer.equals("player1")) return;

        // Check if dropped on player board (y ~200-350, centered x)
        if (y >= 200 && y <= 350) {
            // Try to play card to board
            GameAction action = new PlayCardAction("player1", card.getInstanceId(), -1);
            ActionResult result = action.execute(gameState);
            if (result.success) {
                eventBus.publishAll(gameState.eventLog);
                gameState.eventLog.clear();
            }
        }
    }

    @Override
    public boolean mouseMoved(int x, int y) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }
}
