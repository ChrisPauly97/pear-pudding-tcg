package com.pear.pudding.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.pear.pudding.enums.Location;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Arrays;

import static com.pear.pudding.enums.Location.DRAW;
import static com.pear.pudding.model.Constants.NUMBER_OF_DECK_SLOTS;

@Getter
@Setter
public class DiscardPile extends Deck {

    public DiscardPile(float x, float y, float width, float height){
        super(NUMBER_OF_DECK_SLOTS);
        setX(x);
        setY(y);
        setWidth(width);
        setHeight(height);
        setSlotWidth(width);
        Gdx.app.log("DrawDeck", Arrays.toString(getCards()));
    }

    @Override
    public Vector3 getSlotPositionAtIndex(int index) {
        var xPos = getX() + slotWidth;
        var yPos = getY();
        return new Vector3(xPos, yPos, 1);
    }
}
