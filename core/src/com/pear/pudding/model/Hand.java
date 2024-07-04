package com.pear.pudding.model;

import com.badlogic.gdx.Gdx;
import com.pear.pudding.enums.Location;
import com.pear.pudding.enums.Side;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static com.pear.pudding.model.Constants.*;

@Getter
@Setter
public class Hand extends Deck {
    private boolean cardZoomed = false;

    public Hand(float x, float y, float width, float height) {
        super(NUMBER_OF_HAND_SLOTS);
        setX(x);
        setY(y);
        setWidth(width);
        setHeight(height);
        setSlotWidth(width / (float) NUMBER_OF_HAND_SLOTS);
    }
}