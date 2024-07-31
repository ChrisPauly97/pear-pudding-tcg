package com.pear.pudding.card;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.pear.pudding.model.Card;
import com.pear.pudding.player.Player;

import static com.pear.pudding.enums.CardClass.UNDEAD;
import static com.pear.pudding.enums.CardType.MINION;

public class Skeleton extends Card {
    public Skeleton(float x, float y, float width, float height, Color color, Player player) {
        super(x, y, width, height, color, 1, 3, 1, MINION, UNDEAD,new StatusEffect("", EffectTrigger.NONE, EffectType.NONE, 0), player);
        Texture texture = new Texture(Gdx.files.internal("skeleton.jpg"));
        var image = new Image(texture);
        image.setBounds(x,y + height/2,width,height /2);
        setImage(image);
    }
}