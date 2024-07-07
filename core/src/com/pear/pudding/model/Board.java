package com.pear.pudding.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector3;
import com.pear.pudding.enums.Location;
import com.pear.pudding.enums.Side;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.pear.pudding.enums.Location.BOARD;
import static com.pear.pudding.model.Constants.*;

@Getter
@Setter
public class Board implements Deck {
    private int previousTargetSlot = -1;
    float x;
    float y;
    float width;
    float height;
    float NUMBER_OF_SLOTS;
    Card[] cards;
    Card[] snapshot;

    public Board(float x, float y, float width, float height) {
        setX(x);
        setY(y);
        setWidth(width);
        setHeight(height);
        setNUMBER_OF_SLOTS(NUMBER_OF_BOARD_SLOTS);
        this.cards = new Card[(int) NUMBER_OF_SLOTS];
    }

    @Override
    public void snapShot() {
        this.snapshot = Arrays.copyOf(cards, cards.length);
    }

    @Override
    public void restoreSnapshot() {
        if (snapshot == null) return;
        setCards(Arrays.copyOf(snapshot, snapshot.length));
        for (int i = 0; i < cards.length; i++) {
            if (cards[i] != null) {
                var targetSlotPos = getSlotPositionAtIndex(i);
                cards[i].move(targetSlotPos.x, targetSlotPos.y);
            }
        }
        snapShot();
    }

    @Override
    public int getIndexUnderMouse(Vector3 mousePos) {
        for (int i = 0; i < cards.length; i++) {
            Vector3 slotPos = getSlotPositionAtIndex(i);
            if (cardHovering(mousePos, slotPos)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean containsCard(Card card) {
        for (Card c : cards) {
            if (c == card) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean cardHovering(Vector3 mousePos, Vector3 slotPos) {
        return mousePos.x > slotPos.x && mousePos.x < slotPos.x + slotWidth && mousePos.y > slotPos.y && mousePos.y < slotPos.y + slotHeight;
    }

    @Override
    public boolean mouseHovering(float mouseX, float mouseY) {
        return mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height;
    }

    @Override
    public Vector3 getSlotPositionAtIndex(int index) {
        var xPos = getX() + slotWidth * index;
        var yPos = getY();
        return new Vector3(xPos, yPos, 1);
    }

    @Override
    public void draw(Batch batch) {
        for (Card card : getCards()) {
            if (card != null) {
                card.draw(batch, 1);
            }
        }
    }

    @Override
    public boolean isIndexEmpty(int index) {
        return cards[index] == null;
    }

    @Override
    public int firstEmptySlot() {
        for (int i = 0; i < cards.length; i++) {
            if (cards[i] == null) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean addCard(Card targetCard, int index) {
        if (index != -1) {
            this.cards[index] = targetCard;
            var targetSlotPos = getSlotPositionAtIndex(index);
            targetCard.move(targetSlotPos.x, targetSlotPos.y);
            return true;
        }
        return false;
    }

    @Override
    public int handleHover(Vector3 mouseCoords) {
        var targetSlot = getIndexUnderMouse(mouseCoords);
        // target slot is 3
        // previous is 2
        // If you're hovering over a slot on the board
        if (targetSlot != -1) {
            Gdx.app.log("previous target slot and target slot when target on board", previousTargetSlot + ", " + targetSlot);
            // target slot is 3
            // If the slot you're hovering over is not the one you were hovering over before
            if (this.previousTargetSlot != targetSlot) {
                if (previousTargetSlot == -1) {
                    Gdx.app.log("Board before balance", Arrays.toString(this.getCards()));
                    rebalance(targetSlot);
                    Gdx.app.log("Board after balance", Arrays.toString(this.getCards()));
                }else{
                    Gdx.app.log("Board before restoreSnapshot", Arrays.toString(this.getCards()));
                    restoreSnapshot();
                    rebalance(targetSlot);
                    Gdx.app.log("Board after restoreSnapshot", Arrays.toString(this.getCards()));
                }
            }
            this.previousTargetSlot = targetSlot;
            return targetSlot;
        } else { // Not hovering over the board
            Gdx.app.log("previous target slot and target slot when target not on board", previousTargetSlot + ", " + targetSlot);
            // If I was just hovering over the board
            if (this.previousTargetSlot != -1) {
                restoreSnapshot();
            }
            this.previousTargetSlot = -1;
            return -1;
        }

    }


    // mak

    public void rebalance(int targetSlot) {
//        Gdx.app.log("Rebalancing", "around " + targetSlot);
        // 1
        var nearestFreeSlotLeft = nearestFreeSlotFromMiddleOnLeft();
//        Gdx.app.log("Rebalancing", "nearestFreeSlotFromMiddleOnLeft  " + nearestFreeSlotLeft);
        // 4
        var nearestFreeSlotRight = nearestFreeSlotFromMiddleOnRight();
//        Gdx.app.log("Rebalancing", "nearestFreeSlotFromMiddleOnRight  " + nearestFreeSlotRight);

        // 1
        var distanceToMiddleFromLeft = calculateDistance(middleSlot(), nearestFreeSlotLeft);
//        Gdx.app.log("Rebalancing", "distance from middle to nearest left free slot " + distanceToMiddleFromLeft);

        // 2
        var distanceToMiddleFromRight = calculateDistance(middleSlot(), nearestFreeSlotRight);
//        Gdx.app.log("Rebalancing", "distance from middle to nearest right free slot " + distanceToMiddleFromRight);

        // most of the objects are on the right
        if (distanceToMiddleFromLeft < distanceToMiddleFromRight) {
            //targetSlot = 2
            if (targetSlot == -1) targetSlot = nearestFreeSlotRight;
//            Gdx.app.log("Rebalancing", "shifting left " + targetSlot);
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
            if (targetSlot == -1) targetSlot = nearestFreeSlotLeft;
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

    @Override
    public void removeCard(int index) {
        cards[index] = null;

    }

    @Override
    public Card getCardAtIndex(int index) {
        if (cards[index] != null) {
            return cards[index];
        } else {
            return null;
        }
    }

    @Override
    public void removeCard(Card c) {
        for (int i = 0; i < cards.length; i++) {
            if (cards[i] == c) {
                cards[i] = null;
            }
        }
    }

    @Override
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

    @Override
    public boolean onTheLeft(int index) {
        int middleSlotIndex = cards.length / 2;
        return index < middleSlotIndex;
    }

    @Override
    public boolean inTheMiddle(int index) {
        int middleSlotIndex = cards.length / 2;
        return index == middleSlotIndex;
    }

    @Override
    public boolean onTheRight(int index) {
        int middleSlotIndex = cards.length / 2;
        return index > middleSlotIndex;
    }

    @Override
    public int calculateDistance(int index1, int index2) {
        return Math.abs(index1 - index2);
    }

    @Override
    public int nearestFreeSlotFromMiddleOnLeft() {
        int middleSlotIndex = cards.length / 2;
        for (int i = middleSlotIndex - 1; i >= 0; i--) {
            if (isIndexEmpty(i)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int nearestFreeSlotOnLeft(int targetSlot) {
        int middleSlotIndex = cards.length / 2;
        for (int i = targetSlot; i <= middleSlotIndex; i++) {
            if (i == middleSlotIndex && isIndexEmpty(middleSlotIndex)) {
                return middleSlotIndex;
            } else if (!isIndexEmpty(i + 1)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int middleSlot() {
        return cards.length / 2;
    }

    @Override
    public int nearestFreeSlotFromMiddleOnRight() {
        int middleSlotIndex = cards.length / 2;
        for (int i = middleSlotIndex + 1; i < cards.length; i++) {
            if (isIndexEmpty(i)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int nearestFreeSlotOnRight(int targetIndex) {
        int middleSlotIndex = cards.length / 2;
        for (int i = targetIndex; i >= middleSlotIndex; i--) {
            if (i == middleSlotIndex && isIndexEmpty(middleSlotIndex)) {
                return middleSlotIndex;
            } else if (!isIndexEmpty(i - 1)) {
                return i;
            }
        }
        return -1;
    }


}