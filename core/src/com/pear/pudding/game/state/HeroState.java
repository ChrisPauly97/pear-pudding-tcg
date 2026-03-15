package com.pear.pudding.game.state;

/**
 * Represents a player's hero/avatar with health and attack stats.
 */
public class HeroState {
    private final String ownerId;
    private int health;
    private int attack;

    public HeroState(String ownerId, int initialHealth, int initialAttack) {
        this.ownerId = ownerId;
        this.health = initialHealth;
        this.attack = initialAttack;
    }

    // Getters
    public String getOwnerId() { return ownerId; }
    public int getHealth() { return health; }
    public int getAttack() { return attack; }

    // Setters
    public void setHealth(int h) { this.health = h; }
    public void setAttack(int a) { this.attack = a; }

    // Mutators
    public void takeDamage(int amount) {
        health -= amount;
    }

    public void heal(int amount) {
        health += amount;
    }

    public boolean isAlive() {
        return health > 0;
    }
}
