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

    public Slot findSlot(Vector3 targetPosition) {
        for (Slot slot : slots) {
            if (slot.contains(targetPosition.x, targetPosition.y)) {
                return slot;
            }
        }
        return null;
    }

    // TODO PLace the card in the initial target slot while
    //  balancing the number of cards on either side of the middle slot
    //  while also maintaining card order. Each slot has a Slot value of LEFT MIDDLE or RIGHT
    // You can use this to tell whether it's to the left of the middle or not
    public Slot snapTo(Card card, Slot initialTargetSlot) {
        int middleSlotIndex = slots.size() / 2;
        int cardSlotIndex = initialTargetSlot.getIndex();
        int leftCardCount = 0;
        int rightCardCount = 0;

        // Count the number of cards to the left and right of the initial target slot
        for (int i = 0; i < middleSlotIndex; i++) {
            if (slots.get(i).getCard() != null) {
                leftCardCount++;
            }
        }
        for (int i = middleSlotIndex + 1; i < slots.size(); i++) {
            if (slots.get(i).getCard() != null) {
                rightCardCount++;
            }
        }

        // Determine where to place the card based on balancing
        if (leftCardCount < rightCardCount) {
            // Place the card to the left of the initial target slot
            Slot leftSlot = slots.get(cardSlotIndex - 1);
            leftSlot.setCard(card);
            return leftSlot;
        } else {
            // Place the card to the right of the initial target slot
            Slot rightSlot = slots.get(cardSlotIndex + 1);
            rightSlot.setCard(card);
            return rightSlot;
        }
    }
}