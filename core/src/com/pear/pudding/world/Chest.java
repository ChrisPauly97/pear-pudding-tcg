package com.pear.pudding.world;

import java.util.ArrayList;
import java.util.List;

public class Chest {
    public final String id;
    public float x;
    public float y;
    public boolean opened;
    public final List<String> cardTemplateIds; // Support multiple items

    // Constructor for single card (backward compatible)
    public Chest(String id, float x, float y, String cardTemplateId) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.cardTemplateIds = new ArrayList<>();
        this.cardTemplateIds.add(cardTemplateId);
        this.opened = false;
    }

    // Constructor for multiple cards
    public Chest(String id, float x, float y, List<String> cardTemplateIds) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.cardTemplateIds = new ArrayList<>(cardTemplateIds);
        this.opened = false;
    }

    public float distanceTo(float px, float py) {
        float dx = x - px;
        float dy = y - py;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
}
