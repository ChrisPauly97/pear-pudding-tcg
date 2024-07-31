package com.pear.pudding;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.pear.pudding.screen.LoadingScreen;

public class MyGame extends Game {
    public AssetManager manager = new AssetManager();

    @Override
    public void create()
    {
        setScreen(new LoadingScreen(this));
    }
}

