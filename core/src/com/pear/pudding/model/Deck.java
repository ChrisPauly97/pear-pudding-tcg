package com.pear.pudding.model;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector3;
import com.pear.pudding.enums.Location;
import com.pear.pudding.enums.Side;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.pear.pudding.enums.Side.*;
import static com.pear.pudding.model.Constants.CARD_WIDTH;

@Getter
@Setter
public class Deck {
    private float x;
    private float y;
    private float width;
    private float height;
    float slotWidth = CARD_WIDTH;
    private Location location;
    private final List<Slot> slots = new ArrayList<>();

    public void addSlot(Slot slot) {
        this.slots.add(slot);
    }

    public void draw(Batch batch) {
        for (Slot s : getSlots()) {
            if (s.getCard() != null) {
                s.getCard().draw(batch, 1);
            }
        }
    }

    public Slot firstEmptySlot() {
        for (Slot s : this.slots) {
            if (s.getCard() == null) {
                return s;
            }
        }
        return null;
    }

    public void removeCard(Card targetCard) {
        for (Slot slot : getSlots()) {
            Card currentCard = slot.getCard();
            if (currentCard != null && currentCard.equals(targetCard)) {
                slot.setCard(null);
                break;
            }
        }
    }

    public Slot findSlot(Vector3 targetPosition) {
        for (Slot slot : slots) {
            if (slot.contains(targetPosition.x, targetPosition.y)) {
                return slot;
            }
        }
        return null;
    }

    // If mouseTarget is currently null
    // check if it was not null before
    // If mouseTargetSlot becomes null, move all cards back to their previous position and return null waiting for another drag action
    // if mouseTargetSlot is null, set previousTargetSlot to null and return null
    public Slot onHover(Slot mouseTargetSlot, Card draggingCard, Slot previousTargetSlot) {
//        // If you move the mouse out of the board, move all cards back to their previous position
        if (mouseTargetSlot == null ){
            if(previousTargetSlot != null){
                for (Slot s : this.slots) {
                    //if the slot has a card
                    if (s.getCard() != null) {
                        // previous slot is the old slot this card used to be in
                        Slot previousSlot = s.getCard().getPreviousSlot();
                        if(!previousSlot.equals(s)){
                            previousSlot.setCard(s.getCard());
                            s.getCard().moveToPreviousPosition();
                            s.setCard(null);
                        }
                    }
                }
                return null;
            }else{

                return null;
            }
        }
            // For each slot, if it's not empty, move the card back


        int middleSlotIndex = slots.size() / 2;
        Slot middleSlot = slots.get(middleSlotIndex);
        if (slotEmpty(middleSlot) && !draggingCard.equals(middleSlot.getCard()) &&  !slotHasMiddleAsPreviousPosition()) {
            return middleSlot;
        } else {
            int nearestLeftSlotIndex = findNearestFreeSlotToMiddleOnSide(LEFT);
            int nearestRightSlotIndex = findNearestFreeSlotToMiddleOnSide(RIGHT);
            if (nearestRightSlotIndex == 0 && nearestLeftSlotIndex == 0) {
                return null;
            }
            int distanceToLeft = Math.abs(mouseTargetSlot.getIndex() - nearestLeftSlotIndex);
            int distanceToRight = Math.abs(mouseTargetSlot.getIndex() - nearestRightSlotIndex);


            if (distanceToLeft <=    distanceToRight) {
                if (distanceToLeft == 0) {
                    return mouseTargetSlot;
                }
                // If not balanced, shift one more to the left
                for (int i = nearestLeftSlotIndex; i < mouseTargetSlot.getIndex(); i++) {
                    Slot currentSlot = slots.get(i);
                    Slot nextSlot = slots.get(i + 1);
                    if (nextSlot.getCard() != null) {
                        // Need to track previous position of all cards we're moving, because if I move the card off the board, the situation needs to reset
                        nextSlot.getCard().moveToSlot(currentSlot);
                    }
                }
                return mouseTargetSlot;
            } else {
                if (distanceToRight == 0) {
                    return mouseTargetSlot;
                }
                // If not balanced, shift one more to the right
                for (int j = nearestRightSlotIndex; j > mouseTargetSlot.getIndex(); j--) {
                    Slot currentSlot = slots.get(j);
                    Slot previousSlot = slots.get(j - 1);
                    if (previousSlot.getCard() != null) {
                        previousSlot.getCard().moveToSlot(currentSlot);
                    }
                }
                return mouseTargetSlot;
            }
        }
    }

    public boolean slotHasMiddleAsPreviousPosition() {
        int middleSlotIndex = slots.size() / 2;
        Slot middleSlot = slots.get(middleSlotIndex);
        for (Slot s : this.slots) {
            if (s.getCard() == null) continue;
            if (s.getCard().getPreviousSlot().equals(middleSlot)) {
                return true;
            }
        }
        return false;
    }


//
//            if(slot != null) {
//                slot.setCard(middleSlot.getCard());
//                slot.getCard().move(slot.getX(), slot.getY());
//                middleSlot.setCard(null);
//                return slot;
//            }
//
//        int cardSlotIndex = mouseTargetSlot.getIndex();
//        Side cardSlotSide = mouseTargetSlot.getSide();
//        int leftCardCount = 0;
//        int rightCardCount = 0;
//
//        // Count the number of cards to the left and right of the initial target slot
//        for (int i = 0; i < middleSlotIndex; i++) {
//            if (slots.get(i).getCard() != null) {
//                leftCardCount++;
//            }
//        }
//        for (int i = middleSlotIndex + 1; i < slots.size(); i++) {
//            if (slots.get(i).getCard() != null) {
//                rightCardCount++;
//            }
//        }
//        if (leftCardCount > rightCardCount && cardSlotSide == LEFT) {
//            // Shift all cards one to the right
//            for (int i = slots.size() - 1; i > 0; i--) {
//                Slot currentSlot = slots.get(i);
//                Slot previousSlot = slots.get(i - 1);
//                if (previousSlot.getCard() != null) {
//                    moveCard(previousSlot.getCard(), currentSlot);
//                    previousSlot.setCard(null);
//                }
//            }
//
//            // Check the balance after shifting all cards
//            leftCardCount = 0;
//            rightCardCount = 0;
//            for (int i = 0; i < middleSlotIndex; i++) {
//                if (slots.get(i).getCard() != null) {
//                    leftCardCount++;
//                }
//            }
//            for (int i = middleSlotIndex + 1; i < slots.size(); i++) {
//                if (slots.get(i).getCard() != null) {
//                    rightCardCount++;
//                }
//            }
//            if (leftCardCount > rightCardCount) {
//                // If not balanced, shift one more to the right
//                for (int i = slots.size() - 1; i > 0; i--) {
//                    Slot currentSlot = slots.get(i);
//                    Slot previousSlot = slots.get(i - 1);
//                    if (currentSlot.getCard() != null) {
//                        moveCard(currentSlot.getCard(), previousSlot);
//                        currentSlot.setCard(null);
//                    }
//                }
//
//                // Check if the initial target slot is free for the new card
//                if (mouseTargetSlot.getCard() != null) {
//                    // Check if there's a slot next to the initial target slot on the left that's free
//                    if (cardSlotIndex > 0 && slots.get(cardSlotIndex - 1).getCard() == null) {
//                        return slots.get(cardSlotIndex - 1);
//                    } else {
//                        // Check if there's a slot on the right of the middle that's free
//                        for (int i = middleSlotIndex + 1; i < slots.size(); i++) {
//                            if (slots.get(i).getCard() == null) {
//                                // If not balanced, shift one more to the right
//                                for (int j = slots.size() - 1; j > cardSlotIndex; j--) {
//                                    Slot currentSlot = slots.get(j);
//                                    Slot previousSlot = slots.get(j - 1);
//                                    if (currentSlot.getCard() != null) {
//                                        moveCard(currentSlot.getCard(), previousSlot);
//                                        currentSlot.setCard(null);
//                                    }
//                                }
//                                return slots.get(i);
//                            }
//                        }
//                    }
//                }
//            } else {
//                Integer newTargetSlotIndex = findNearestSlotToMiddleOnSide(cardSlotSide);
//                var newSlot = slots.get(newTargetSlotIndex);
//                return Objects.requireNonNullElse(newTargetSlotIndex, newSlot);
//            }
//        } else if (leftCardCount < rightCardCount && cardSlotSide == RIGHT) {
//            for (int i = 0; i < slots.size() - 1; i++) {
//                Slot currentSlot = slots.get(i);
//                Slot nextSlot = slots.get(i + 1);
//                if (nextSlot.getCard() != null) {
//                    moveCard(nextSlot.getCard(), currentSlot);
//                    nextSlot.setCard(null);
//                }
//            }
//
//            // Check the balance after shifting all cards
//            leftCardCount = 0;
//            rightCardCount = 0;
//            for (int i = 0; i < middleSlotIndex; i++) {
//                if (slots.get(i).getCard() != null) {
//                    leftCardCount++;
//                }
//            }
//            for (int i = middleSlotIndex + 1; i < slots.size(); i++) {
//                if (slots.get(i).getCard() != null) {
//                    rightCardCount++;
//                }
//            }
//            if (leftCardCount < rightCardCount) {
//
//                // If not balanced, shift one more to the left
//                for (int i = 0; i < slots.size() - 1; i++) {
//                    Slot currentSlot = slots.get(i);
//                    Slot nextSlot = slots.get(i + 1);
//                    if (nextSlot.getCard() != null) {
//                        moveCard(nextSlot.getCard(), currentSlot);
//                        nextSlot.setCard(null);
//                    }
//                }
//
//                // Check if the initial target slot is free for the new card
//                if (initialTargetSlot.getCard() != null) {
//                    // Check if there's a slot next to the initial target slot on the left that's free
//                    if (cardSlotIndex > 0 && slots.get(cardSlotIndex - 1).getCard() == null) {
//                        return slots.get(cardSlotIndex - 1);
//                    } else {
//                        // Check if there's a slot on the right of the middle that's free
//                        for (int i = middleSlotIndex + 1; i < slots.size(); i++) {
//                            if (slots.get(i).getCard() == null) {
//                                // Shift all cards from the initial target slot to the free slot one position to the right
//                                for (int j = cardSlotIndex; j < i; j++) {
//                                    Slot currentSlot = slots.get(j);
//                                    Slot nextSlot = slots.get(j + 1);
//                                    if (currentSlot.getCard() != null) {
//                                        moveCard(currentSlot.getCard(), nextSlot);
//                                        currentSlot.setCard(null);
//                                    }
//                                }
//                                return slots.get(i);
//                            }
//                        }
//                    }
//                }
//            }
//        } else {
//            Integer newTargetSlot = findNearestSlotToMiddleOnSide(cardSlotSide);
//            return slots.get(newTargetSlot);
//        }
//        return null;
//    }

    public boolean slotEmpty(Slot slot) {
        return slot.getCard() == null;
    }


    private int findNearestFreeSlotToMiddleOnSide(Side side) {
        int middleSlotIndex = slots.size() / 2;
        if (side == LEFT) {
            for (int i = middleSlotIndex - 1; i >= 0; i--) {
                if (slotEmpty(slots.get(i))) {
                    return i;
                }
            }
        } else if (side == RIGHT) {
            for (int i = middleSlotIndex + 1; i < slots.size(); i++) {
                if (slotEmpty(slots.get(i))) {
                    return i;
                }
            }
        }
        return 0;
    }
}