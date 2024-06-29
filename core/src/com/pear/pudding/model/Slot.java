package com.pear.pudding.model;

import com.badlogic.gdx.math.Rectangle;
import com.pear.pudding.card.Card;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Slot extends Rectangle {

    Card card;
    Slot(float x, float y, float width, float height){
        super(x,y,width,height);
    }
}
