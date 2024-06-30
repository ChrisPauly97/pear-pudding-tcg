package com.pear.pudding.player;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;

import static com.pear.pudding.model.Constants.*;

//TODO move health and health position to here
// CHECK if hero sprite is hit by attacks
// CHECK if hero health <= 0
public class Hero extends Actor {
    private Texture texture;

    public Hero(Texture heroTexture, float xPos, float yPos) {
        this.texture = heroTexture;
        setBounds(xPos, yPos, HERO_DIMENSION, HERO_DIMENSION);

    }

    public void draw(Batch batch) {
        batch.draw(texture, getX(),getY(),getWidth(),getHeight());
    }
}