package com.pear.pudding.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.pear.pudding.MyGame;
import com.pear.pudding.game.data.CardRegistry;
import com.pear.pudding.game.data.CardTemplate;
import com.pear.pudding.world.Chest;

import static com.pear.pudding.game.data.GameConstants.WINDOW_HEIGHT;
import static com.pear.pudding.game.data.GameConstants.WINDOW_WIDTH;

public class ChestOpenScreen implements Screen {
    private final MyGame game;
    private final Screen previousScreen;
    private final Chest chest;
    private final Stage stage;
    private final SpriteBatch batch;
    private final OrthographicCamera camera;
    private BitmapFont font;
    private Texture[] cardTextures;

    private int currentCardIndex = 0;
    private float cardRevealTimer = 0f;
    private static final float CARD_REVEAL_DURATION = 0.8f;
    private static final float AUTO_ADVANCE_DELAY = 3.0f;
    private float autoAdvanceTimer = 0f;
    private boolean allCardsShown = false;
    private boolean uiCreated = false;

    public ChestOpenScreen(MyGame game, Screen previousScreen, Chest chest) {
        this.game = game;
        this.previousScreen = previousScreen;
        this.chest = chest;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, WINDOW_WIDTH, WINDOW_HEIGHT);
        camera.position.set(WINDOW_WIDTH / 2, WINDOW_HEIGHT / 2, 0);
        camera.update();

        FitViewport viewport = new FitViewport(WINDOW_WIDTH, WINDOW_HEIGHT, camera);
        stage = new Stage(viewport);
        batch = new SpriteBatch();

        Gdx.input.setInputProcessor(stage);
    }

    private void createUI() {
        try {
            Skin skin = game.manager.get("uiskin.json", Skin.class);

            try {
                font = skin.getFont("default");
            } catch (Exception e) {
                font = new BitmapFont();
            }

            // Accept button (only shown when all cards revealed)
            TextButton acceptButton = new TextButton("Claim All", skin);
            acceptButton.setPosition(WINDOW_WIDTH / 2 - 75, 100);
            acceptButton.setSize(150, 50);
            acceptButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (previousScreen instanceof WorldScreen) {
                        ((WorldScreen) previousScreen).removeChest(chest);
                    } else {
                        chest.opened = true;
                    }
                    game.setScreen(previousScreen);
                }
            });
            acceptButton.setVisible(false);
            acceptButton.setName("acceptButton");
            stage.addActor(acceptButton);

            // Next button (skip to next card)
            TextButton nextButton = new TextButton("Next", skin);
            nextButton.setPosition(WINDOW_WIDTH / 2 + 100, 100);
            nextButton.setSize(100, 50);
            nextButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    advanceToNextCard();
                }
            });
            nextButton.setName("nextButton");
            stage.addActor(nextButton);

            // Load all card textures
            cardTextures = new Texture[chest.cardTemplateIds.size()];
            for (int i = 0; i < chest.cardTemplateIds.size(); i++) {
                CardTemplate template = CardRegistry.get(chest.cardTemplateIds.get(i));
                try {
                    cardTextures[i] = new Texture(template.texturePath);
                    cardTextures[i].setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                } catch (Exception e) {
                    cardTextures[i] = null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void advanceToNextCard() {
        currentCardIndex++;
        cardRevealTimer = 0f;
        autoAdvanceTimer = 0f;

        if (currentCardIndex >= chest.cardTemplateIds.size()) {
            // All cards shown
            allCardsShown = true;
            TextButton acceptButton = (TextButton) stage.getRoot().findActor("acceptButton");
            if (acceptButton != null) {
                acceptButton.setVisible(true);
            }
        }
    }

    @Override
    public void show() {
        if (!uiCreated) {
            createUI();
            uiCreated = true;
        }
    }

    @Override
    public void render(float delta) {
        // Check for Enter key to advance cards
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            if (!allCardsShown) {
                advanceToNextCard();
            } else {
                // All cards shown, treat Enter as accept
                if (previousScreen instanceof WorldScreen) {
                    ((WorldScreen) previousScreen).removeChest(chest);
                } else {
                    chest.opened = true;
                }
                game.setScreen(previousScreen);
            }
        }

        cardRevealTimer += delta;
        if (!allCardsShown) {
            autoAdvanceTimer += delta;
        }

        ScreenUtils.clear(0.1f, 0.1f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Auto-advance to next card after delay
        if (!allCardsShown && autoAdvanceTimer >= AUTO_ADVANCE_DELAY) {
            advanceToNextCard();
        }

        // Calculate zoom scale for card reveal (0 to 1)
        float revealProgress = Math.min(cardRevealTimer / CARD_REVEAL_DURATION, 1f);
        float scale = 0.3f + revealProgress * 0.7f; // Scales from 0.3 to 1.0
        float alpha = revealProgress; // Fades in from transparent

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        if (currentCardIndex < chest.cardTemplateIds.size()) {
            CardTemplate cardTemplate = CardRegistry.get(chest.cardTemplateIds.get(currentCardIndex));

            // Draw card centered, zoomed with fade-in
            float cardWidth = 120 * scale;
            float cardHeight = 160 * scale;
            float cardX = WINDOW_WIDTH / 2 - cardWidth / 2;
            float cardY = WINDOW_HEIGHT / 2 + 50 - cardHeight / 2;

            batch.setColor(1, 1, 1, alpha);
            if (cardTextures[currentCardIndex] != null) {
                batch.draw(cardTextures[currentCardIndex], cardX, cardY, cardWidth, cardHeight);
            } else {
                batch.setColor(0.2f, 0.4f, 0.6f, alpha);
                batch.draw(getPlaceholderTexture(), cardX, cardY, cardWidth, cardHeight);
            }
            batch.setColor(1, 1, 1, 1);

            // Draw card name and stats
            if (font != null) {
                font.setColor(1, 1, 1, alpha);
                font.draw(batch, cardTemplate.displayName, cardX + 10, cardY + cardHeight - 20);
                font.draw(batch, "Cost: " + cardTemplate.baseCost, cardX + 10, cardY + 40);
                font.draw(batch, "ATK: " + cardTemplate.baseAttack + " HP: " + cardTemplate.baseHealth, cardX + 10, cardY + 20);
            }

            // Draw progress indicator and hint
            if (chest.cardTemplateIds.size() > 1 && font != null) {
                font.setColor(1, 1, 1, 1);
                font.draw(batch, "Card " + (currentCardIndex + 1) + " / " + chest.cardTemplateIds.size(), WINDOW_WIDTH / 2 - 50, WINDOW_HEIGHT - 50);
                font.setColor(0.7f, 0.7f, 1, 1);
                font.draw(batch, "Press ENTER to skip", WINDOW_WIDTH / 2 - 70, WINDOW_HEIGHT - 80);
            }
        }

        batch.end();

        // Update button visibility
        TextButton nextButton = (TextButton) stage.getRoot().findActor("nextButton");
        if (nextButton != null) {
            nextButton.setVisible(!allCardsShown);
        }

        stage.draw();
    }

    private Texture getPlaceholderTexture() {
        Texture placeholder = new Texture(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(0.2f, 0.4f, 0.6f, 1);
        pixmap.fill();
        placeholder.draw(pixmap, 0, 0);
        pixmap.dispose();
        return placeholder;
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        stage.dispose();
        if (cardTextures != null) {
            for (Texture texture : cardTextures) {
                if (texture != null) {
                    texture.dispose();
                }
            }
        }
    }
}
