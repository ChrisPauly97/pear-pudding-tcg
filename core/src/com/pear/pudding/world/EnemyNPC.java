package com.pear.pudding.world;

public class EnemyNPC {
    public final String id;
    public float x;
    public float y;
    public boolean alive;
    public boolean tracking;  // Whether this enemy tracks the player
    private static final float TRACKING_SPEED = 80f;  // pixels per second

    public EnemyNPC(String id, float x, float y) {
        this(id, x, y, false);
    }

    public EnemyNPC(String id, float x, float y, boolean tracking) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.alive = true;
        this.tracking = tracking;
    }

    public float distanceTo(float px, float py) {
        float dx = x - px;
        float dy = y - py;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Move toward target position if tracking is enabled.
     * Returns distance to target for auto-battle trigger.
     */
    public float moveToward(float targetX, float targetY, float delta) {
        if (!tracking) {
            return distanceTo(targetX, targetY);
        }

        float dx = targetX - x;
        float dy = targetY - y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        if (distance > 0) {
            float moveDistance = TRACKING_SPEED * delta;
            if (moveDistance >= distance) {
                x = targetX;
                y = targetY;
            } else {
                x += (dx / distance) * moveDistance;
                y += (dy / distance) * moveDistance;
            }
        }

        return distanceTo(targetX, targetY);
    }
}
