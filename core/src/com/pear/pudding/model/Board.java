package com.pear.pudding.model;

import com.badlogic.gdx.Gdx;
import com.pear.pudding.enums.Location;
import com.pear.pudding.enums.Side;
import lombok.Getter;
import lombok.Setter;

import static com.pear.pudding.model.Constants.*;

@Getter
@Setter
public class Board extends Deck {
    public Board(float x, float y, float width, float height) {
        super(NUMBER_OF_BOARD_SLOTS);
        setX(x);
        setY(y);
        setWidth(width);
        setHeight(height);
        setSlotWidth(width / (float) NUMBER_OF_BOARD_SLOTS);
    }
}