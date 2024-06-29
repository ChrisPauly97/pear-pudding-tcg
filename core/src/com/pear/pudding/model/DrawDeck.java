package com.pear.pudding.model;

import com.badlogic.gdx.Gdx;
import com.pear.pudding.card.Card;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static com.pear.pudding.enums.Location.DRAW;
import static com.pear.pudding.model.Constants.*;

@Getter
@Setter
@NoArgsConstructor
public class DrawDeck extends Deck {

    public DrawDeck(float x, float y, float width, float height){
        setX(x);
        setY(y);
        setWidth(width);
        setHeight(height);
        setSlotWidth(width);
        setLocation(DRAW);
        for(int i = 0; i < NUMBER_OF_DECK_SLOTS; i++){
            addSlot(new Slot(getX(),getY(),getSlotWidth(), getHeight()));
        }
        Gdx.app.log("DrawDeck", getSlots().toString());
    }
}
