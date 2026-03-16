package com.pear.pudding.world;

/** Lightweight data class for a single wind-driven particle (leaf or pollen dot). */
public class WindParticle {
    public float x, y;       // isometric screen-space position
    public float vx, vy;     // velocity in screen pixels/second
    public float life;       // remaining lifetime in seconds
    public float maxLife;    // total lifetime (used for alpha fade)
    public float size;       // rendered size in pixels (2–4)
    public int   type;       // 0 = leaf, 1 = pollen dot

    public WindParticle(float x, float y, float vx, float vy,
                        float life, float size, int type) {
        this.x       = x;
        this.y       = y;
        this.vx      = vx;
        this.vy      = vy;
        this.life    = life;
        this.maxLife = life;
        this.size    = size;
        this.type    = type;
    }
}
