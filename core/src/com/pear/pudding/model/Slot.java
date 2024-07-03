package com.pear.pudding.model;

import com.badlogic.gdx.math.Rectangle;
import com.pear.pudding.enums.Location;
import com.pear.pudding.enums.Side;
import lombok.Getter;
import lombok.Setter;

import static com.pear.pudding.model.Constants.NUMBER_OF_BOARD_SLOTS;

@Setter
@Getter
public class Slot extends Rectangle {
    int index;
    Card card;
    Side side;
    Location location;
    Slot(float x, float y, float width, float height, int index, Location location){
        super(x,y,width,height);
        this.index = index;
        this.location = location;
    }
}
