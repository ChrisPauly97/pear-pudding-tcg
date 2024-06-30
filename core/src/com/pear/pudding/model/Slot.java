package com.pear.pudding.model;

import com.badlogic.gdx.math.Rectangle;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Slot extends Rectangle {
    int index;
    Card card;
    Slot(float x, float y, float width, float height, int index){
        super(x,y,width,height);
        this.index = index;
    }
}
