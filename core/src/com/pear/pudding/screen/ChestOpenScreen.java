package com.pear.pudding.screen;

import com.badlogic.gdx.Gdx;
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
    private final CardTemplate cardTemplate;
    private final Stage stage;
    private final SpriteBatch batch;
    private final OrthographicCamera camera;
    private BitmapFont font;
    private Texture cardTexture;
    private boolean uiCreated = false;

    private float elapsedTime = 0f;
    private static final float ZOOM_DURATION = 0.5f;

    public ChestOpenScreen(MyGame game, Screen previousScreen, Chest chest) {
        this.game = game;
        this.previousScreen = previousScreen;
        this.chest = chest;
        this.cardTemplate = CardRegistry.get(chest.cardTemplateId);

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
            game.manager.finishLoading();
            Skin skin = game.manager.get("uiskin.json", Skin.class);

            // Try to get font from skin, fallback to creating a simple one
            try {
                font = skin.getFont("default");
            } catch (Exception e) {
                // Skin doesn't have "default" font, create a basic one
                font = new BitmapFont();
            }

            // Accept button
            TextButton acceptButton = new TextButton("Accept Card", skin);
            acceptButton.setPosition(WINDOW_WIDTH / 2 - 75, 100);
            acceptButton.setSize(150, 50);
            acceptButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (previousScreen instanceof WorldScreen) {
                        ((WorldScreen) previousScreen).removeChest(chest);
                    } else {
                        chest.opened = true;  // Fallback
                    }
                    game.setScreen(previousScreen);
                }
            });
            stage.addActor(acceptButton);

            // Try to load card texture
            try {
                cardTexture = new Texture(cardTemplate.texturePath);
                cardTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            } catch (Exception e) {
                cardTexture = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        elapsedTime += delta;
        ScreenUtils.clear(0.1f, 0.1f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Calculate zoom scale (0 to 1)
        float progress = Math.min(elapsedTime / ZOOM_DURATION, 1f);
        float scale = 0.5f + progress * 0.5f; // Scales from 0.5 to 1.0

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Draw card centered, zoomed
        float cardWidth = 120 * scale;
        float cardHeight = 160 * scale;
        float cardX = WINDOW_WIDTH / 2 - cardWidth / 2;
        float cardY = WINDOW_HEIGHT / 2 + 50 - cardHeight / 2;

        if (cardTexture != null) {
            batch.draw(cardTexture, cardX, cardY, cardWidth, cardHeight);
        } else {
            // Placeholder: colored rectangle with stats
            batch.setColor(0.2f, 0.4f, 0.6f, 1);
            batch.draw(getPlaceholderTexture(), cardX, cardY, cardWidth, cardHeight);
            batch.setColor(1, 1, 1, 1);
        }

        // Draw card name and stats
        if (font != null) {
            font.setColor(1, 1, 1, 1);
            font.draw(batch, cardTemplate.displayName, cardX + 10, cardY + cardHeight - 20);
            font.draw(batch, "Cost: " + cardTemplate.baseCost, cardX + 10, cardY + 40);
            font.draw(batch, "ATK: " + cardTemplate.baseAttack + " HP: " + cardTemplate.baseHealth, cardX + 10, cardY + 20);
        }

        batch.end();

        stage.draw();
    }

    private Texture getPlaceholderTexture() {
        // Return a white pixel that can be scaled
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
        batch.dispose();
        stage.dispose();
        if (cardTexture != null) {
            cardTexture.dispose();
        }
    }
}
