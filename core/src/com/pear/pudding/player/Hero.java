package com.pear.pudding.player;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;

import static com.pear.pudding.model.Constants.*;

//TODO move health and health position to here
// CHECK if hero sprite is hit by attacks
// CHECK if hero health <= 0
public class Hero extends Actor {
    private final Texture texture;
    private final float rotation;
    public Hero(Texture heroTexture, float xPos, float yPos, float rotateBy) {
        this.rotation = rotateBy;
        this.setOrigin(HERO_DIMENSION / 2, HERO_DIMENSION / 2);
        this.texture = heroTexture;
        setBounds(xPos, yPos, HERO_DIMENSION, HERO_DIMENSION);
    }

    public void draw(Batch batch) {
        TextureRegion textureRegion = new TextureRegion(texture);
        batch.draw(textureRegion, getX() - getOriginX(), getY() - getOriginY(), getOriginX(), getOriginY(), getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation());
    }
}