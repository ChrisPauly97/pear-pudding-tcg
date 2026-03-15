package com.pear.pudding.screen;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.pear.pudding.MyGame;

/**
 * Simple loading screen that loads required assets before showing the menu.
 */
public class AssetLoadingScreen implements Screen {
    private final AssetManager manager;
    private final Game game;

    public AssetLoadingScreen(Game game, AssetManager manager) {
        this.game = game;
        this.manager = manager;
        loadAssets();
    }

    private void loadAssets() {
        // Register freetype loader
        manager.setLoader(com.badlogic.gdx.graphics.g2d.BitmapFont.class,
            new FreetypeFontLoader(new com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver()));

        // UI
        manager.load("uiskin.json", Skin.class);

        // World textures: grass variants
        for (int i = 0; i < 16; i++) {
            manager.load("grass" + i + ".png", com.badlogic.gdx.graphics.Texture.class);
        }

        // World textures: wall variants
        for (int i = 0; i < 8; i++) {
            manager.load("wall" + i + ".png", com.badlogic.gdx.graphics.Texture.class);
        }

        // Isometric brick wall textures
        manager.load("wallbrick_top.png", com.badlogic.gdx.graphics.Texture.class);
        manager.load("wallbrick_front.png", com.badlogic.gdx.graphics.Texture.class);
        manager.load("wallbrick_left.png", com.badlogic.gdx.graphics.Texture.class);
        manager.load("wallbrick_right.png", com.badlogic.gdx.graphics.Texture.class);

        // Entity sprites
        manager.load("player.png", com.badlogic.gdx.graphics.Texture.class);
        manager.load("enemy.png", com.badlogic.gdx.graphics.Texture.class);
        manager.load("chest.png", com.badlogic.gdx.graphics.Texture.class);

        // Audio
        manager.load("freesound_community-running-in-grass-6237.mp3", com.badlogic.gdx.audio.Sound.class);
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (manager.update()) {
            // Assets loaded, go to menu
            game.setScreen(new MenuScreen((MyGame) game));
            dispose();
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
    }
}
