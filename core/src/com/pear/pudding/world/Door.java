package com.pear.pudding.world;

public class Door {
    public final String id;
    public float x;
    public float y;
    public String targetMapName; // null or empty string → exit (return to parent map)

    public Door(String id, float x, float y, String targetMapName) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.targetMapName = targetMapName;
    }

    public float distanceTo(float px, float py) {
        float dx = x - px;
        float dy = y - py;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
}
