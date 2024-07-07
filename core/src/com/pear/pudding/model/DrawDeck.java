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

import static com.pear.pudding.enums.Location.DRAW;
import static com.pear.pudding.model.Constants.*;

@Getter
@Setter
public class DrawDeck {
    float x;
    float y;
    float width;
    float height;
    float numberOfSlots;
    Card[] cards;

    public DrawDeck(float x, float y, float width, float height) {
        setNumberOfSlots(NUMBER_OF_DECK_SLOTS);
        setX(x);
        setY(y);
        setWidth(width);
        setHeight(height);
        this.cards = new Card[(int) NUMBER_OF_DECK_SLOTS];
        Gdx.app.log("DrawDeck", Arrays.toString(getCards()));
    }

    public Vector3 getSlotPositionAtIndex() {
        var xPos = getX();
        var yPos = getY();
        return new Vector3(xPos, yPos, 1);
    }

    public boolean isIndexEmpty(int index) {
        return cards[index] == null;
    }

    public int firstEmptySlot() {
        for (int i = 0; i < cards.length; i++) {
            if (cards[i] == null) {
                return i;
            }
        }
        return -1;
    }

    public void removeCard(Card c) {
        for (int i = 0; i < cards.length; i++) {
            if (cards[i] == c) {
                cards[i] = null;
            }
        }
    }

    public void removeCard(int index) {
        cards[index] = null;
    }

    public void draw(Batch batch) {
        for (Card card : getCards()) {
            if (card != null) {
                card.draw(batch, 1);
            }
        }
    }


    public boolean addCard(Card targetCard, int index) {
        if (index != -1 && isIndexEmpty(index)) {
            this.cards[index] = targetCard;
            return true;
        }
        return false;
    }

    public void shuffle() {
        List<Card> cardsList = Arrays.asList(cards);
        Collections.shuffle(cardsList);
        cardsList.toArray(cards);
        System.out.println(Arrays.toString(cards));
    }
}
