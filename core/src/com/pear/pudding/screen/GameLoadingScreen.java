package com.pear.pudding.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.pear.pudding.MyGame;

import static com.pear.pudding.game.data.GameConstants.WINDOW_HEIGHT;
import static com.pear.pudding.game.data.GameConstants.WINDOW_WIDTH;

/**
 * Loading screen shown while world assets are being loaded.
 * Automatically transitions to the target screen when loading completes.
 */
public class GameLoadingScreen implements Screen {
    private final MyGame game;
    private final ScreenType targetScreenType;
    private final SpriteBatch batch;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private BitmapFont font;

    public enum ScreenType {
        WORLD, EDITOR
    }

    public GameLoadingScreen(MyGame game, ScreenType targetScreenType) {
        this.game = game;
        this.targetScreenType = targetScreenType;

        camera = new OrthographicCamera(WINDOW_WIDTH, WINDOW_HEIGHT);
        viewport = new FitViewport(WINDOW_WIDTH, WINDOW_HEIGHT, camera);
        viewport.apply();
        camera.position.set(WINDOW_WIDTH / 2, WINDOW_HEIGHT / 2, 0);
        camera.update();

        batch = new SpriteBatch();
    }

    @Override
    public void show() {
        // Load world assets
        AssetLoadingScreen.loadWorldAssets(game.manager);

        // Get font from skin for loading text
        try {
            Skin skin = game.manager.get("uiskin.json", Skin.class);
            font = skin.getFont("default");
        } catch (Exception e) {
            font = new BitmapFont();
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Update asset loading
        boolean finished = game.manager.update();

        // Draw loading bar and text
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        if (font != null) {
            font.draw(batch, "Loading...", WINDOW_WIDTH / 2 - 40, WINDOW_HEIGHT / 2 + 50);
        }

        // Draw progress bar background
        float barWidth = 300;
        float barHeight = 20;
        float barX = WINDOW_WIDTH / 2 - barWidth / 2;
        float barY = WINDOW_HEIGHT / 2 - barHeight / 2;

        // Progress is from 0 to 1
        float progress = game.manager.getProgress();

        // Draw background (empty bar)
        batch.setColor(0.3f, 0.3f, 0.3f, 1);
        batch.draw(getPixel(), barX, barY, barWidth, barHeight);

        // Draw filled portion
        batch.setColor(0.2f, 0.8f, 0.2f, 1);
        batch.draw(getPixel(), barX, barY, barWidth * progress, barHeight);

        batch.setColor(1, 1, 1, 1);
        batch.end();

        // Switch to target screen when done
        if (finished) {
            Screen targetScreen = null;
            switch (targetScreenType) {
                case WORLD:
                    targetScreen = new WorldScreen(game);
                    break;
                case EDITOR:
                    targetScreen = new MapEditorScreen(game);
                    break;
            }
            if (targetScreen != null) {
                game.setScreen(targetScreen);
                dispose();
            }
        }
    }

    private com.badlogic.gdx.graphics.Texture pixel;

    private com.badlogic.gdx.graphics.Texture getPixel() {
        if (pixel == null) {
            com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
            pixmap.setColor(1, 1, 1, 1);
            pixmap.fill();
            pixel = new com.badlogic.gdx.graphics.Texture(pixmap);
            pixmap.dispose();
        }
        return pixel;
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
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
        if (font != null && !font.isFlipped()) {
            // Only dispose if we created it
        }
        if (pixel != null) {
            pixel.dispose();
        }
    }
}
