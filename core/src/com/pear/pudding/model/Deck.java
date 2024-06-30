package com.pear.pudding.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector3;
import com.pear.pudding.enums.Location;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

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

    public Slot findSlotForFight(Vector3 targetPosition) {
        for (Slot slot : slots) {
            if (slot.contains(targetPosition.x, targetPosition.y)) {
                return slot;
            }
        }
        return null;
    }


    public Slot snapTo(Vector3 coords, Card card) {
        Slot initialTargetSlot = null;
        Slot finalTargetSlot = null;
        // Find the slot containing the given coordinates
        for (Slot slot : slots) {
            if (slot.contains(coords.x, coords.y)) {
                initialTargetSlot = slot;
                break;
            }
        }

        if (initialTargetSlot != null) {
            Slot middleSlot = slots.get(slots.size() / 2);
            if (middleSlot.getCard() == null) {
                finalTargetSlot = middleSlot;
            } else if (isToTheLeftOfMiddle(initialTargetSlot)) {
                finalTargetSlot = findClosestSlotToLeftOfMiddle(middleSlot);
                if (finalTargetSlot == null) {
                    var closestFree = findClosestFreeSlotToRightOfMiddle(middleSlot);
                    if (closestFree != null) {
                        shiftCards(initialTargetSlot, closestFree);
                        finalTargetSlot = initialTargetSlot;
                    }
                }
            } else {
                finalTargetSlot = findClosestSlotToRightOfMiddle(middleSlot);
                if (finalTargetSlot == null) {
                    var closestFree = findClosestFreeSlotToLeftOfMiddle(middleSlot);
                    if (closestFree != null) {
                        shiftCards(initialTargetSlot, closestFree);
                        finalTargetSlot = initialTargetSlot;
                    }
                }
            }

            if(finalTargetSlot != null){
                // Place the card in the target slot
                Gdx.app.log("Before snap", " snapping to slot " + finalTargetSlot);
                card.move(finalTargetSlot.getX(), finalTargetSlot.getY());
                finalTargetSlot.setCard(card);
                card.setCurrentLocation(this.location);

                if (this.location != Location.DRAW) {
                    card.setFaceUp(true);
                }

                card.setAttackCount(0);
                Gdx.app.log("Snapped", "Pos after move X=" + card.getX() + " Current Y=" + card.getY());
                card.setPreviousPosition(new Bound(card.getX(), card.getY(), card.getWidth(), card.getHeight()));
            }
        }
        return finalTargetSlot;
    }

    private boolean isToTheLeftOfMiddle(Slot foundSlot) {
        int middleIndex = slots.size() / 2;
        return foundSlot.getIndex() < middleIndex;
    }

    private Slot findClosestFreeSlotToRightOfMiddle(Slot middleSlot) {
        int middleIndex = middleSlot.getIndex();
        for (int i = middleIndex + 1; i < slots.size(); i++) {
            Slot currentSlot = slots.get(i);
            if (currentSlot.getCard() == null) {
                return currentSlot;
            }
        }
        return null;
    }

    private Slot findClosestFreeSlotToLeftOfMiddle(Slot middleSlot) {
        int middleIndex = middleSlot.getIndex();
        for (int i = middleIndex - 1; i >= 0; i--) {
            Slot currentSlot = slots.get(i);
            if (currentSlot.getCard() == null) {
                return currentSlot;
            }
        }
        return null;
    }

    private void shiftCards(Slot targetSlot, Slot closestFreeSlot) {
        // Start index is the target slot index
        int targetIndex = targetSlot.getIndex();
        // End index is the index of the closest slot where getCard is null
        int freeSlotIndex = closestFreeSlot.getIndex();
        // If targetSlot is left of the end index,
        if (targetIndex < freeSlotIndex) {
            for (int i = freeSlotIndex; i > targetIndex; i--) {
                Slot currentSlot = slots.get(i);
                if (currentSlot.getCard() == null && i > 0) {
                    Slot nextSlot = slots.get(i - 1);
                    var cardToMove = nextSlot.getCard();
                    currentSlot.setCard(cardToMove);
                    cardToMove.move(currentSlot.getX(), currentSlot.getY());
                    nextSlot.setCard(null);
                }
            }

        } else if(targetIndex > freeSlotIndex){
            for (int i = freeSlotIndex; i < targetIndex; i++) {
                Slot currentSlot = slots.get(i);
                if (currentSlot.getCard() == null) {
                    Slot rightSlot = slots.get(i + 1);
                    var cardToMove = rightSlot.getCard();
                    currentSlot.setCard(cardToMove);
                    cardToMove.move(currentSlot.getX(), currentSlot.getY());
                    rightSlot.setCard(null);
                }
            }
        }
    }

    private Slot findClosestSlotToRightOfMiddle(Slot middleSlot) {
        int middleIndex = middleSlot.getIndex();
        Integer closestIndex = null;
        for (int i = middleIndex +1; i < slots.size(); i++) {
            Slot currentSlot = slots.get(i);
            if (currentSlot.getCard() == null) {
                closestIndex = i;
                break;
            }
        }
        if (closestIndex != null) {
            return slots.get(closestIndex);
        } else {
            return null;
        }
    }

    private Slot findClosestSlotToLeftOfMiddle(Slot middleSlot) {
        int middleIndex = middleSlot.getIndex();
        Integer closestIndex = null;
        for (int i = middleIndex - 1; i >= 0; i--) {
            Slot currentSlot = slots.get(i);
            if (currentSlot.getCard() == null) {
                closestIndex = i;
                break;
            }
        }
        if (closestIndex != null) {
            return slots.get(closestIndex);
        } else {
            return null;
        }
    }
}
