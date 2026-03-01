package com.pear.pudding.card;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.pear.pudding.model.Card;
import com.pear.pudding.player.Player;

import static com.pear.pudding.enums.CardClass.ETHEREAL;
import static com.pear.pudding.enums.CardType.MINION;

public class Ghoul extends Card {
    public Ghoul(float x, float y, float width, float height, Color color, Player player) {
        super(x, y, width, height, color, 3, 4, 3, MINION, ETHEREAL,
                new StatusEffect("Discards in play minion and summon this in it's place, Maintains initiative",
                        EffectTrigger.SUMMON, EffectType.DAMAGE, 2), player);
        Texture texture = new Texture(Gdx.files.internal("ghoul.png"));
        var image = new Image(texture);
        image.setBounds(x,y, width, height/2);
        setImage(image);
    }
}