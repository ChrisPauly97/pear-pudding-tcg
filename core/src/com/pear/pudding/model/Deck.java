package com.pear.pudding.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Vector4;
import com.pear.pudding.card.Card;
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

    public Slot firstEmptySlot(){
        for(Slot s: this.slots){
            if(s.getCard() == null){
                return s;
            }
        }
        return null;
    }

    public void removeCard(Card c) {
        for (Slot s : getSlots()) {
            if (s.getCard() != null && s.getCard().equals(c)) {
                s.setCard(null);
            }
        }
    }

    public Slot checkFight(Vector3 vec, Card c){
        for (Slot s : this.slots) {
            if (s.contains(vec.x, vec.y)) {
                return s;
            }
        }
        return null;
    }


    public Slot snapTo(Vector3 vec, Card c) {
        for (Slot s : this.slots) {
            if (s.contains(vec.x, vec.y)) {
                Gdx.app.log("Before snap", " snapping to slot " + s);
                c.move(s.getX(), s.getY());
                s.setCard(c);
                c.setCurrentLocation(this.location);
                if(this.location != Location.DRAW){
                    c.setFaceUp(true);
                }
                Gdx.app.log("Snapped", "Pos after move X=" + c.getX() + " Current Y=" + c.getY());
                c.setPreviousPosition(new Bound(c.getX(), c.getY(), c.getWidth(), c.getHeight()));
                return s;
            }
        }
        return null;
    }
}
