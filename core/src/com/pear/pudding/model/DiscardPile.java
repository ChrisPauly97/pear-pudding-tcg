package com.pear.pudding.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.pear.pudding.enums.Location;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Arrays;

import static com.pear.pudding.enums.Location.DRAWDECK;
import static com.pear.pudding.model.Constants.*;

@Getter
@Setter
public class DiscardPile extends Deck {
    public DiscardPile(float x, float y, float width, float height) {
        super(x, y, width, height, NUMBER_OF_DECK_SLOTS);
        Gdx.app.log("Discard Pile", Arrays.toString(getCards()));
    }

    public boolean isIndexEmpty(int index) {
        return cards[index] == null;
    }

    @Override
    public Vector3 getSlotPositionAtIndex(int index) {
        var xPos = getX() + slotWidth;
        var yPos = getY();
        return new Vector3(xPos, yPos, 1);
    }


    public boolean addCard(Card targetCard, int index) {
        if (index != -1 && isIndexEmpty(index)) {
            this.cards[index] = targetCard;
            return true;
        }
        return false;
    }

    public int firstEmptySlot() {
        for (int i = 0; i < cards.length; i++) {
            if (cards[i] == null) {
                return i;
            }
        }
        return -1;
    }
}
