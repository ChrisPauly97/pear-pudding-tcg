package com.pear.pudding.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector3;
import com.pear.pudding.enums.Location;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.pear.pudding.enums.Location.DRAWDECK;
import static com.pear.pudding.model.Constants.*;

@Getter
@Setter
public class DrawDeck extends Deck {
    public DrawDeck(float x, float y, float width, float height) {
        super(x, y, width, height, NUMBER_OF_DECK_SLOTS);
        Gdx.app.log("DrawDeck", Arrays.toString(getCards()));
    }

    @Override
    public Vector3 getSlotPositionAtIndex(int index) {
        var xPos = getX() + slotWidth;
        var yPos = getY();
        return new Vector3(xPos, yPos, 1);
    }

    @Override
    public void draw(Batch batch){
        var lastPopulatedIndex = this.lastPopulatedIndex();
        if(lastPopulatedIndex != -1){
            this.cards[lastPopulatedIndex].draw(batch, 1);
        }
    }


    public void shuffle() {
        List<Card> cardsList = Arrays.asList(cards);
        Collections.shuffle(cardsList);
        cardsList.toArray(cards);
        System.out.println(Arrays.toString(cards));
    }
}
