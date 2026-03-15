package com.pear.pudding.world;

public class Chest {
    public final String id;
    public float x;
    public float y;
    public boolean opened;
    public final String cardTemplateId;

    public Chest(String id, float x, float y, String cardTemplateId) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.cardTemplateId = cardTemplateId;
        this.opened = false;
    }

    public float distanceTo(float px, float py) {
        float dx = x - px;
        float dy = y - py;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
}
