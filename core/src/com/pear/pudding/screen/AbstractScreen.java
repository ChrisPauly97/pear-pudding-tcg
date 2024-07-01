package com.pear.pudding.screen;

import com.badlogic.gdx.Screen;
import com.pear.pudding.MyGame;

/**
 * @author Mats Svensson
 */
public abstract class AbstractScreen implements Screen {

    protected MyGame game;

    public AbstractScreen(MyGame game) {
        this.game = game;
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
    }
}