package com.pear.pudding.model;

import com.badlogic.gdx.Gdx;
import com.pear.pudding.enums.Location;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static com.pear.pudding.model.Constants.*;

@Getter
@Setter
@NoArgsConstructor
public class Hand extends Deck{
    private boolean cardZoomed = false;
    float slotWidth = CARD_WIDTH;

    public Hand(float x,float y,float width, float height) {
        super();
        setX(x);
        setY(y);
        setWidth(width);
        setHeight(height);
        setLocation(Location.HAND);
        for(int i = 0; i < NUMBER_OF_HAND_SLOTS; i++){
            this.addSlot(new Slot(getX() + i*getSlotWidth(),getY(),getSlotWidth(), getHeight(), i));
        }
        Gdx.app.log("Hand", getSlots().toString());
    }
}