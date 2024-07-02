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
@NoArgsConstructor
public class Hand extends Deck {
    private boolean cardZoomed = false;
    private final float slotWidth = CARD_WIDTH;

    public Hand(float x, float y, float width, float height) {
        super();
        setX(x);
        setY(y);
        setWidth(width);
        setHeight(height);
        setLocation(Location.HAND);

        float middleSlotIndex = NUMBER_OF_HAND_SLOTS / 2;
        for (int i = 0; i < NUMBER_OF_HAND_SLOTS; i++) {
            Side slotSide = i < middleSlotIndex ? Side.LEFT : (i > middleSlotIndex ? Side.RIGHT : Side.MIDDLE);
            var slot = new Slot(getX() + i * getSlotWidth(), getY(), getSlotWidth(), getHeight(), i);
            slot.setSide(slotSide);
            this.addSlot(slot);
        }

        Gdx.app.log("Hand", getSlots().toString());
    }
}