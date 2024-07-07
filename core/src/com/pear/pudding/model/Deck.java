package com.pear.pudding.model;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector3;
import com.pear.pudding.enums.Location;
import static com.pear.pudding.model.Constants.CARD_HEIGHT;
import static com.pear.pudding.model.Constants.CARD_WIDTH;

public interface Deck {

    float slotWidth = CARD_WIDTH;
    float slotHeight = CARD_HEIGHT;

    public void snapShot();

    public void restoreSnapshot();

    public int getIndexUnderMouse(Vector3 mousePos);

    public boolean containsCard(Card card);

    public boolean cardHovering(Vector3 mousePos, Vector3 slotPos);

    public boolean mouseHovering(float mouseX, float mouseY);

    public Vector3 getSlotPositionAtIndex(int index);

    public void draw(Batch batch);

    public boolean isIndexEmpty(int index);

    public int firstEmptySlot();

    public boolean addCard(Card targetCard, int index);

    public int handleHover(Vector3 mouseCoords);

    public void removeCard(int index);

    public Card getCardAtIndex(int index);

    public void removeCard(Card c);

    public Card findCardUnderMouse(Vector3 targetPosition);

    // Tested
    public boolean onTheLeft(int index);

    public boolean inTheMiddle(int index);

    // Tested
    public boolean onTheRight(int index);

    // Tested
    public int calculateDistance(int index1, int index2);

    //Tested
    public int nearestFreeSlotFromMiddleOnLeft();

    //Tested
    public int nearestFreeSlotOnLeft(int targetSlot);


    public int middleSlot();


    //Tested
    public int nearestFreeSlotFromMiddleOnRight();


    //Tested
    public int nearestFreeSlotOnRight(int targetIndex);


}