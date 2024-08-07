package com.pear.pudding.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.pear.pudding.enums.Location;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

import static com.pear.pudding.model.Constants.*;

@Getter
@Setter
public class Board extends Deck {
    private int previousTargetSlot = -1;

    public Board(float x, float y, float width, float height) {
        super(x, y, width, height, NUMBER_OF_BOARD_SLOTS);
        Gdx.app.log("Board", Arrays.toString(getCards()));
    }

    public boolean handleEffect(int boardTargetSlot, Card attackingCard) {
        var targetCard = getCardAtIndex(boardTargetSlot);
        if (targetCard != null) {
            switch (attackingCard.getStatusEffect().getEffectType()) {
                case DAMAGE:
                    targetCard.takeDamage(attackingCard.getStatusEffect().getValue());
                    break;
                case HEAL:
                    targetCard.getHealing(attackingCard.getStatusEffect().getValue());
                    break;
                case REMOVE:
                    targetCard.handleRemoveEffect(attackingCard.getStatusEffect().getValue());
                    break;
                case NONE:
                    return false;
            }
            if(targetCard.getHealth() <= 0){
                targetCard.moveToDiscardPile();
            }
            return true;
        }
        return false;
    }

    public void handleHover(Vector3 mouseCoords) {
        var targetSlot = getIndexUnderMouse(mouseCoords);
        if (targetSlot != -1) {
            Gdx.app.log("previous target slot and target slot when target on board", previousTargetSlot + ", " + targetSlot);
            if (this.previousTargetSlot != targetSlot) {
                if (previousTargetSlot == -1) {
                    Gdx.app.log("Board before balance", Arrays.toString(this.getCards()));
                    rebalance(targetSlot);
                    Gdx.app.log("Board after balance", Arrays.toString(this.getCards()));
                } else {
                    Gdx.app.log("Board before restoreSnapshot", Arrays.toString(this.getCards()));
                    restoreSnapshot();
                    rebalance(targetSlot);
                    Gdx.app.log("Board after restoreSnapshot", Arrays.toString(this.getCards()));
                }
            }
            this.previousTargetSlot = targetSlot;
        } else {
            if (this.previousTargetSlot != -1) {
                restoreSnapshot();
            }
            this.previousTargetSlot = -1;
        }
    }

    public int nearestFreeSlotFromMiddleOnLeft() {
        int middleSlotIndex = cards.length / 2;
        for (int i = middleSlotIndex - 1; i >= 0; i--) {
            if (isIndexEmpty(i)) {
                return i;
            }
        }
        return -1;
    }

    public int nearestFreeSlotFromMiddleOnRight() {
        int middleSlotIndex = cards.length / 2;
        for (int i = middleSlotIndex + 1; i < cards.length; i++) {
            if (isIndexEmpty(i)) {
                return i;
            }
        }
        return -1;
    }

    public int nearestFreeSlot() {
        var nearestFreeSlotLeft = nearestFreeSlotFromMiddleOnLeft();
        var nearestFreeSlotRight = nearestFreeSlotFromMiddleOnRight();
        Gdx.app.log("Finding Nearest Slot ", nearestFreeSlotLeft + ", " + nearestFreeSlotRight);
        if (getCardAtIndex(middleSlot()) == null) {
            return middleSlot();
        }
        var distanceToMiddleFromLeft = calculateDistance(middleSlot(), nearestFreeSlotLeft);
        var distanceToMiddleFromRight = calculateDistance(middleSlot(), nearestFreeSlotRight);
        if (distanceToMiddleFromLeft < distanceToMiddleFromRight) {
            return nearestFreeSlotLeft;
        } else {
            return nearestFreeSlotRight;
        }
    }

    public boolean addCard(Card targetCard) {
        var targetSlot = nearestFreeSlot();
        if (targetSlot != -1) {
            this.cards[targetSlot] = targetCard;
            var targetSlotPos = getSlotPositionAtIndex(nearestFreeSlot());
            targetCard.move(targetSlotPos.x, targetSlotPos.y, Location.getEnum(this.getClass().getSimpleName()));
            return true;
        }
        return false;
    }

    public void rebalance(int targetSlot) {
        var nearestFreeSlotLeft = nearestFreeSlotFromMiddleOnLeft();
        var nearestFreeSlotRight = nearestFreeSlotFromMiddleOnRight();
        if (nearestFreeSlotLeft == -1 && nearestFreeSlotRight == -1) {
            nearestFreeSlotLeft = middleSlot();
        }

        var distanceToMiddleFromLeft = calculateDistance(middleSlot(), nearestFreeSlotLeft);
        var distanceToMiddleFromRight = calculateDistance(middleSlot(), nearestFreeSlotRight);

        if (nearestFreeSlotLeft == -1 && targetSlot > middleSlot()) {
            shiftLeft(nearestFreeSlotRight, targetSlot);
        } else if (nearestFreeSlotRight == -1 && targetSlot < middleSlot()) {
            shiftRight(nearestFreeSlotLeft, targetSlot);
        } else if (distanceToMiddleFromLeft < distanceToMiddleFromRight) {
            if (targetSlot == -1) targetSlot = nearestFreeSlotRight;
            shiftLeft(nearestFreeSlotLeft, targetSlot);
        } else {
            if (targetSlot == -1) targetSlot = nearestFreeSlotLeft;
            shiftRight(nearestFreeSlotRight, targetSlot);
        }
    }

}