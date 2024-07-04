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
public class DiscardPile extends Deck {

    public DiscardPile(float x, float y, float width, float height){
        super(NUMBER_OF_DECK_SLOTS);
        setX(x);
        setY(y);
        setWidth(width);
        setHeight(height);
        setSlotWidth(width);
        Gdx.app.log("DrawDeck", getCards().toString());
    }
}
