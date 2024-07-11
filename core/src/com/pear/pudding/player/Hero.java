package com.pear.pudding.player;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import lombok.Getter;
import lombok.Setter;

import static com.pear.pudding.model.Constants.*;

//TODO move health and health position to here
// CHECK if hero sprite is hit by attacks
// CHECK if hero health <= 0
@Getter
@Setter
public class Hero extends Actor {
    private final Texture texture;
    private final float rotation;
    public int attack = 2;
    public int health = 30;
    public Hero(Texture heroTexture, float xPos, float yPos, float rotateBy) {
        setX(xPos);
        setY(yPos);
        setWidth(HERO_DIMENSION);
        setHeight(HERO_DIMENSION);
        this.rotation = rotateBy;
        this.setOrigin(HERO_DIMENSION / 2, HERO_DIMENSION / 2);
        this.texture = heroTexture;
        setBounds(xPos, yPos, HERO_DIMENSION, HERO_DIMENSION);
    }

    public boolean contains(Vector3 point) {
        float minX = getX();
        float minY = getY();
        float maxX = getX() + getWidth();
        float maxY = getY() + getHeight();

        return point.x >= minX && point.x <= maxX && point.y >= minY && point.y <= maxY;
    }

    public void draw(Batch batch) {
        TextureRegion textureRegion = new TextureRegion(texture);
        batch.draw(textureRegion, getX() - getOriginX(), getY() - getOriginY(), getOriginX(), getOriginY(), getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation());
    }
}