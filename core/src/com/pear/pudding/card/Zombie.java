package com.pear.pudding.card;

import com.badlogic.gdx.graphics.Color;
import com.pear.pudding.model.Card;
import com.pear.pudding.model.Slot;
import com.pear.pudding.player.Player;

import static com.pear.pudding.enums.CardClass.UNDEAD;
import static com.pear.pudding.enums.CardType.MINION;

public class Zombie extends Card {
    public Zombie(float x, float y, float width, float height, Color color, Player player) {
        super(x, y, width, height, color, 2, 2, 3, MINION, UNDEAD, StatusEffects.effects.get("Poison"), player);
    }
}