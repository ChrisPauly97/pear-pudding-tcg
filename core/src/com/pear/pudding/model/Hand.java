package com.pear.pudding.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;


import static com.pear.pudding.model.Constants.*;

@Getter
@Setter
public class Hand extends Deck {
    private int previousTargetSlot = -1;
    private boolean cardZoomed = false;

    public Hand(float x, float y, float width, float height) {
        super(x, y, width, height, NUMBER_OF_HAND_SLOTS);
        Gdx.app.log("Hand", Arrays.toString(getCards()));
    }

    public void handleHover(Vector3 mouseCoords) {
        var targetSlot = getIndexUnderMouse(mouseCoords);
        if (targetSlot != -1) {
            Gdx.app.log("previous target slot and target slot when target on board", previousTargetSlot + ", " + targetSlot);
            if (this.previousTargetSlot != targetSlot) {
                if (previousTargetSlot == -1) {
                    rebalance(targetSlot);
                } else {
                    restoreSnapshot();
                    rebalance(targetSlot);
                }
            }
            this.previousTargetSlot = targetSlot;
        } else {
            if (this.previousTargetSlot != -1) {
                restoreSnapshot();
                rebalance(-1);
            }
            this.previousTargetSlot = -1;
        }
    }

    public void rebalance(int targetSlot) {
        if (targetSlot == -1) {
            shiftLeft(0, cards.length - 1);
        } else {
            var nearestFreeSlotFromLeft = firstEmptySlot();
            if (targetSlot > nearestFreeSlotFromLeft) {
                shiftLeft(nearestFreeSlotFromLeft, targetSlot);
            } else {
                shiftRight(nearestFreeSlotFromLeft, targetSlot);
            }
        }
    }

}