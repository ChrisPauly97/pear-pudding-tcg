package com.pear.pudding.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.pear.pudding.enums.Location;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import static com.pear.pudding.enums.Location.BOARD;
import static com.pear.pudding.model.Constants.CARD_HEIGHT;
import static com.pear.pudding.model.Constants.CARD_WIDTH;

@Getter
@Setter
public class Deck {
    private float x;
    private float y;
    private float width;
    private float height;
    float slotWidth = CARD_WIDTH;
    float slotHeight = CARD_HEIGHT;
    private float NUMBER_OF_SLOTS;
    private Card[] cards;
    private Card[] snapshot;

    public Deck(float slots) {
        setNUMBER_OF_SLOTS(slots);
        cards = new Card[(int) NUMBER_OF_SLOTS];
    }

    public void snapShot() {
        snapshot = Arrays.copyOf(cards, cards.length);
    }

    public void restoreSnapshot() {
        if(snapshot == null) return;
        cards = Arrays.copyOf(snapshot, snapshot.length);
        for(int i = 0; i < cards.length; i++){
            if(cards[i] != null){
                var targetSlotPos = getSlotPositionAtIndex(i);
                cards[i].move(targetSlotPos.x, targetSlotPos.y);
                cards[i].setCurrentLocation(BOARD);
            }
        }
        snapShot();
    }

    // check if the mouse x y is over the board
    // cards have an
    // cut the board into card size slices, x,y,w,h

    public int getIndexUnderMouse(Vector3 mousePos) {
        for (int i = 0; i < cards.length; i++) {
            Vector3 slotPos = getSlotPositionAtIndex(i);
            if (cardHovering(mousePos, slotPos)) {
                return i;
            }
        }
        return -1;
    }

    public boolean containsCard(Card card){
        for (Card c : cards) {
            if(c == card){
                return true;
            }
        }
        return false;
    }

    public boolean cardHovering(Vector3 mousePos, Vector3 slotPos) {
        return mousePos.x > slotPos.x && mousePos.x < slotPos.x + slotWidth && mousePos.y > slotPos.y && mousePos.y < slotPos.y + slotHeight;
    }

    public boolean mouseHovering(float mouseX, float mouseY) {
        return mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height;
    }

    public Vector3 getSlotPositionAtIndex(int index) {
        var xPos = getX() + slotWidth * index;
        var yPos = getY();
        return new Vector3(xPos, yPos, 1);
    }

    public void draw(Batch batch) {
        for (Card card : getCards()) {
            if(card != null){
                card.draw(batch, 1);
            }
        }
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

    public boolean addCard(Card targetCard, int index) {
        if (index != -1 && isIndexEmpty(index)) {
            this.cards[index] = targetCard;
            return true;
        }
        return false;
    }

    public int handleHover(Vector3 mouseCoords){
        var targetSlot = getIndexUnderMouse(mouseCoords);
        // If you're hovering over a slot on the board
        if (targetSlot != -1) {
            Gdx.app.log("target", "" + targetSlot);
            if (getCardAtIndex(targetSlot) == null) {
                if (onTheLeft(targetSlot)) {
                    targetSlot = nearestFreeSlotOnLeft();
                } else if (!onTheLeft(targetSlot)) {
                    targetSlot = nearestFreeSlotOnRight();
                }
            } else {
                var nearestFreeSlotLeft = nearestFreeSlotOnLeft();
                var nearestFreeSlotRight = nearestFreeSlotOnRight();
                var distanceToMiddleFromLeft = calculateDistance((int) getNUMBER_OF_SLOTS() / 2, nearestFreeSlotLeft);
                var distanceToMiddleFromRight = calculateDistance((int) getNUMBER_OF_SLOTS() / 2, nearestFreeSlotRight);

                // most of the objects are on the right
                if (distanceToMiddleFromLeft < distanceToMiddleFromRight) {
                    // If not balanced, shift one more to the left
                    for (int i = nearestFreeSlotLeft; i < targetSlot; i++) {
                        Card currentCard = getCards()[i];
                        Card nextCard = getCards()[i + 1];
                        if (currentCard == null && nextCard != null) {
                            var newSlotPos = getSlotPositionAtIndex(i);
                            getCards()[i + 1].move(newSlotPos.x, newSlotPos.y);
                            addCard(nextCard, i);
                            removeCard(i + 1);
                        }
                    }
                } else {
// If not balanced, shift one more to the right
                    for (int i = nearestFreeSlotRight; i > targetSlot; i--) {
                        Card currentCard = getCards()[i];
                        Card previousCard = getCards()[i - 1];
                        if (currentCard == null && previousCard != null) {
                            var newSlotPos = getSlotPositionAtIndex(i);
                            getCards()[i - 1].move(newSlotPos.x, newSlotPos.y);
                            addCard(previousCard, i);
                            removeCard(i - 1);
                        }
                    }
                }
            }
            return targetSlot;
        }else{
            return -1;
        }
    }

    public void removeCard(int index) {
        cards[index] = null;
    }

    public Card getCardAtIndex(int index) {
        if(cards[index] != null){
            return cards[index];
        }else{
            return null;
        }
    }

    public void removeCard(Card c) {
        for (int i = 0; i < cards.length; i++) {
            if (cards[i] == c) {
                cards[i] = null;
            }
        }
    }


    public Card findCardUnderMouse(Vector3 targetPosition) {
        for (Card card : cards) {
            if (card != null) {
                if (card.contains(targetPosition)) {
                    return card;
                }
            }
        }
        return null;
    }

    // Tested
    public boolean onTheLeft(int index) {
        int middleSlotIndex = cards.length / 2;
        return index < middleSlotIndex;
    }

    public boolean inTheMiddle(int index) {
        int middleSlotIndex = cards.length / 2;
        return index == middleSlotIndex;
    }

    // Tested
    public boolean onTheRight(int index) {
        int middleSlotIndex = cards.length / 2;
        return index > middleSlotIndex;
    }

    // Tested
    public int calculateDistance(int index1, int index2) {
        return Math.abs(index1 - index2);
    }

    //Tested
    public int nearestFreeSlotOnLeft() {
        int middleSlotIndex = cards.length / 2;
        for (int i = middleSlotIndex; i >= 0; i--) {
            if (isIndexEmpty(i)) {
                return i;
            }
        }
        return -1;
    }

    public int middleSlot(){
        return cards.length / 2;
    }

    //Tested
    public int nearestFreeSlotOnRight() {
        int middleSlotIndex = cards.length / 2;
        for (int i = middleSlotIndex; i < cards.length; i++) {
            if (isIndexEmpty(i)) {
                return i;
            }
        }
        return -1;
    }

    public void shuffle() {
        List<Card> cardsList = Arrays.asList(cards);
        Collections.shuffle(cardsList);
        cardsList.toArray(cards);
        System.out.println(Arrays.toString(cards));
    }
}