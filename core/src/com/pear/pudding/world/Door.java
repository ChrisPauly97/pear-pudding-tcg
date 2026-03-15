package com.pear.pudding.world;

public class Door {
    public final String id;
    public float x;
    public float y;
    public String targetMapName; // null or empty string → exit (return to parent map)
    public String targetDoorId; // null or empty string → spawn at map spawn point (or 0,0 if none)

    public Door(String id, float x, float y, String targetMapName) {
        this(id, x, y, targetMapName, "");
    }

    public Door(String id, float x, float y, String targetMapName, String targetDoorId) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.targetMapName = targetMapName;
        this.targetDoorId = targetDoorId != null ? targetDoorId : "";
    }

    public float distanceTo(float px, float py) {
        float dx = x - px;
        float dy = y - py;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
}
