package com.pear.pudding.model;

import com.badlogic.gdx.Gdx;
import com.pear.pudding.enums.Location;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static com.pear.pudding.enums.Location.DRAW;
import static com.pear.pudding.model.Constants.NUMBER_OF_DECK_SLOTS;

@Getter
@Setter
@NoArgsConstructor
public class DiscardPile extends Deck {

    public DiscardPile(float x, float y, float width, float height){
        setX(x);
        setY(y);
        setWidth(width);
        setHeight(height);
        setSlotWidth(width);
        setLocation(DRAW);
        for(int i = 0; i < NUMBER_OF_DECK_SLOTS; i++){
            addSlot(new Slot(getX(),getY(),getSlotWidth(), getHeight(), i, Location.DISCARD));
        }
        Gdx.app.log("DrawDeck", getSlots().toString());
    }
}
