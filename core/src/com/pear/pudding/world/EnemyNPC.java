package com.pear.pudding.world;

public class EnemyNPC {
    public final String id;
    public float x;
    public float y;
    public boolean alive;

    public EnemyNPC(String id, float x, float y) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.alive = true;
    }

    public float distanceTo(float px, float py) {
        float dx = x - px;
        float dy = y - py;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
}
