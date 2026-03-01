package com.pear.pudding.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector3;
import com.pear.pudding.enums.Location;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

import static com.pear.pudding.model.Constants.*;

@Getter
@Setter
public class Deck {
    private int previousTargetSlot = -1;
    float x;
    float y;
    float width;
    float height;
    float NUMBER_OF_SLOTS;
    Card[] cards;
    Card[] snapshot;
    float slotWidth = CARD_WIDTH;
    float slotHeight = CARD_HEIGHT;
    protected Card draggingCard = null;
    protected int draggingCardOriginalIndex = -1;

    Deck(float x, float y, float width, float height, float slotCount) {
        setX(x);
        setY(y);
        setWidth(width);
        setHeight(height);
        setNUMBER_OF_SLOTS(slotCount);
        this.cards = new Card[(int) NUMBER_OF_SLOTS];
    }

    public void snapShot() {
        this.snapshot = Arrays.copyOf(cards, cards.length);
    }

    /**
     * Marks a card as being dragged without removing it from the deck array.
     * This prevents rebalancing from treating the slot as empty during drag.
     */
    public void startDragging(Card card) {
        for (int i = 0; i < cards.length; i++) {
            if (cards[i] == card) {
                this.draggingCard = card;
                this.draggingCardOriginalIndex = i;
                snapShot();
                return;
            }
        }
    }

    /**
     * Clears the dragging state.
     */
    public void stopDragging() {
        this.draggingCard = null;
        this.draggingCardOriginalIndex = -1;
    }

    /**
     * Returns true if this deck is currently tracking a dragging card.
     */
    public boolean isDragging() {
        return this.draggingCard != null;
    }

    public void restoreSnapshot() {
        if (snapshot == null) return;
        setCards(Arrays.copyOf(snapshot, snapshot.length));
        for (int i = 0; i < cards.length; i++) {
            if (cards[i] != null) {
                var targetSlotPos = getSlotPositionAtIndex(i);
                cards[i].move(targetSlotPos.x, targetSlotPos.y, Location.getEnum(this.getClass().getSimpleName()));
            }
        }
        snapShot();
    }

    public int getIndexUnderMouse(Vector3 mousePos) {
        for (int i = 0; i < cards.length; i++) {
            Vector3 slotPos = getSlotPositionAtIndex(i);
            if (cardHovering(mousePos, slotPos)) {
                return i;
            }
        }
        return -1;
    }

    public boolean containsCard(Card card) {
        for (Card c : cards) {
            if (c == card) {
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
            if (card != null) {
                card.draw(batch, 1);
            }
        }
    }

    public int getHandSize(){
        var cardCount = 0;
        for (Card card : getCards()) {
            if (card != null) {
                cardCount++;
            }
        }
        return cardCount;
    }

    public int lastPopulatedIndex(){
        for (int i = cards.length-1; i >= 0 ; i--) {
            if (cards[i] != null) {
                return i;
            }
        }
        return -1;
    }

    public boolean isIndexEmpty(int index) {
        return cards[index] == null;
    }

    public int firstEmptySlot() {
        for (int i = 0; i < cards.length; i++) {
            if (cards[i] == null || cards[i] == draggingCard) {
                return i;
            }
        }
        return -1;
    }

    public boolean addCard(Card targetCard, int index) {
        if (index != -1) {
            this.cards[index] = targetCard;
            var targetSlotPos = getSlotPositionAtIndex(index);
            targetCard.move(targetSlotPos.x, targetSlotPos.y, Location.getEnum(this.getClass().getSimpleName()));
            return true;
        }
        return false;
    }

    public void shiftLeft(int startSlot, int targetSlot) {
        // If not balanced, shift one more to the left
        for (int i = startSlot; i < targetSlot; i++) {
            Card currentCard = getCards()[i];
            Card nextCard = getCards()[i + 1];
            // Treat dragging card's slot as empty, but don't move the dragging card itself
            boolean currentEmpty = currentCard == null || currentCard == draggingCard;
            boolean nextExists = nextCard != null && nextCard != draggingCard;
            if (currentEmpty && nextExists) {
                var newSlotPos = getSlotPositionAtIndex(i);
                getCards()[i + 1].move(newSlotPos.x, newSlotPos.y, getCards()[i+1].getCurrentLocation());
                addCard(nextCard, i);
                removeCard(i + 1);
            }
        }
    }

    public void shiftRight(int startSlot, int targetSlot) {
        // If not balanced, shift one more to the right
        for (int i = startSlot; i > targetSlot; i--) {
            Card currentCard = getCards()[i];
            Card previousCard = getCards()[i - 1];
            // Treat dragging card's slot as empty, but don't move the dragging card itself
            boolean currentEmpty = currentCard == null || currentCard == draggingCard;
            boolean prevExists = previousCard != null && previousCard != draggingCard;
            if (currentEmpty && prevExists) {
                var newSlotPos = getSlotPositionAtIndex(i);
                getCards()[i - 1].move(newSlotPos.x, newSlotPos.y, getCards()[i-1].getCurrentLocation());
                addCard(previousCard, i);
                removeCard(i - 1);
            }
        }
    }

    public int nearestFreeSlotOnLeft(int targetSlot) {
        int middleSlotIndex = cards.length / 2;
        for (int i = targetSlot; i <= middleSlotIndex; i++) {
            boolean middleEmpty = isIndexEmpty(middleSlotIndex) || cards[middleSlotIndex] == draggingCard;
            boolean nextOccupied = (i + 1 < cards.length) && !isIndexEmpty(i + 1) && cards[i + 1] != draggingCard;
            if (i == middleSlotIndex && middleEmpty) {
                return middleSlotIndex;
            } else if (nextOccupied) {
                return i;
            }
        }
        return -1;
    }

    public int nearestFreeSlotOnRight(int targetIndex) {
        int middleSlotIndex = cards.length / 2;
        for (int i = targetIndex; i >= middleSlotIndex; i--) {
            boolean middleEmpty = isIndexEmpty(middleSlotIndex) || cards[middleSlotIndex] == draggingCard;
            boolean prevOccupied = (i - 1 >= 0) && !isIndexEmpty(i - 1) && cards[i - 1] != draggingCard;
            if (i == middleSlotIndex && middleEmpty) {
                return middleSlotIndex;
            } else if (prevOccupied) {
                return i;
            }
        }
        return -1;
    }

    public void removeCard(int index) {
        cards[index] = null;

    }


    public Card getCardAtIndex(int index) {
        if(index == -1 || cards[index] == null){
            return null;
        }else {
            return cards[index];
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

    /**
     * Atomically moves a card from one deck to another (or within the same deck).
     * This is the single source of truth for card transfers.
     *
     * @param card The card to move
     * @param fromDeck The source deck (can be null if card is new)
     * @param toDeck The destination deck
     * @param toIndex The target index in the destination deck
     * @return true if the move was successful
     */
    public static boolean moveCardBetweenDecks(Card card, Deck fromDeck, Deck toDeck, int toIndex) {
        if (card == null || toDeck == null || toIndex < 0 || toIndex >= toDeck.cards.length) {
            return false;
        }

        // Remove from source deck if specified
        if (fromDeck != null) {
            fromDeck.removeCard(card);
            fromDeck.stopDragging();
        }

        // Add to destination deck
        boolean added = toDeck.addCard(card, toIndex);
        if (added) {
            toDeck.stopDragging();
        }

        return added;
    }


    public boolean onTheLeft(int index) {
        int middleSlotIndex = cards.length / 2;
        return index < middleSlotIndex;
    }


    public boolean inTheMiddle(int index) {
        int middleSlotIndex = cards.length / 2;
        return index == middleSlotIndex;
    }


    public boolean onTheRight(int index) {
        int middleSlotIndex = cards.length / 2;
        return index > middleSlotIndex;
    }


    public int calculateDistance(int index1, int index2) {
        return Math.abs(index1 - index2);
    }


    public int middleSlot() {
        return cards.length / 2;
    }
    //Tested


}