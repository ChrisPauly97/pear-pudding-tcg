package com.pear.pudding.model;

import com.badlogic.gdx.Gdx;
import com.pear.pudding.card.Card;
import com.pear.pudding.enums.Location;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import static com.pear.pudding.model.Constants.*;

@Getter
@Setter
public class Board extends Deck {
    float slotWidth = CARD_WIDTH;

    public Board(float x,float y,float width, float height) {
        setX(x);
        setY(y);
        setWidth(width);
        setHeight(height);
        setLocation(Location.BOARD);
        setSlotWidth(width / NUMBER_OF_BOARD_SLOTS);
        for(int i = 0; i < NUMBER_OF_BOARD_SLOTS; i++){
            addSlot(new Slot(getX() + i*this.slotWidth,this.getY(),this.slotWidth, this.getHeight()));
        }
        Gdx.app.log("Board", getSlots().toString());
    }

}