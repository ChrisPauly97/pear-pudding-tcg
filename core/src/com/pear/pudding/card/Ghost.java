package com.pear.pudding.card;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.pear.pudding.model.Card;
import com.pear.pudding.model.Slot;
import com.pear.pudding.player.Player;

import static com.pear.pudding.enums.CardClass.ETHEREAL;
import static com.pear.pudding.enums.CardType.MINION;

public class Ghost extends Card {

    public Ghost(float x, float y, float width, float height, Color color, Player player) {
        super(x, y, width, height, color, 1, 2, 1, MINION, ETHEREAL, StatusEffects.effects.get("Scare"), player);
        Texture texture = new Texture(Gdx.files.internal("ghost.png"));
        setDebug(true);
        var image = new Image(texture);
        image.setBounds(x,y, width, height/2);
        setImage(image);
    }
}
