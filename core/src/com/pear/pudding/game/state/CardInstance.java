package com.pear.pudding.game.state;

/**
 * Mutable per-game card state. Represents an instance of a card in play.
 * Links to CardTemplate for static properties.
 */
public class CardInstance {
    private final String instanceId;
    private final String templateId;
    private final String ownerId;

    private int currentHealth;
    private int attackCount;
    private boolean summoningSick;
    private int outOfPlay;  // stun counter; 0 = active
    private ZoneType currentZone;
    private int slotIndex;  // position within zone (for hand/board layout)

    public CardInstance(String instanceId, String templateId, String ownerId, int baseHealth) {
        this.instanceId = instanceId;
        this.templateId = templateId;
        this.ownerId = ownerId;
        this.currentHealth = baseHealth;
        this.attackCount = 0;
        this.summoningSick = true;
        this.outOfPlay = 0;
        this.currentZone = ZoneType.DRAW_DECK;
        this.slotIndex = -1;
    }

    // Getters
    public String getInstanceId() { return instanceId; }
    public String getTemplateId() { return templateId; }
    public String getOwnerId() { return ownerId; }
    public int getCurrentHealth() { return currentHealth; }
    public int getAttackCount() { return attackCount; }
    public boolean isSummoningSick() { return summoningSick; }
    public int getOutOfPlay() { return outOfPlay; }
    public ZoneType getCurrentZone() { return currentZone; }
    public int getSlotIndex() { return slotIndex; }

    // Setters for mutable state
    public void setCurrentHealth(int health) { this.currentHealth = health; }
    public void setAttackCount(int count) { this.attackCount = count; }
    public void setSummoningSick(boolean sick) { this.summoningSick = sick; }
    public void setOutOfPlay(int count) { this.outOfPlay = count; }
    public void setCurrentZone(ZoneType zone) { this.currentZone = zone; }
    public void setSlotIndex(int slot) { this.slotIndex = slot; }

    // Mutators
    public void takeDamage(int amount) {
        currentHealth -= amount;
    }

    public void heal(int amount) {
        currentHealth += amount;
    }

    public void incrementOutOfPlay() {
        outOfPlay++;
    }

    public void decrementOutOfPlay() {
        if (outOfPlay > 0) outOfPlay--;
    }

    public boolean isActive() {
        return outOfPlay == 0;
    }

    public boolean canAttack() {
        return !summoningSick && outOfPlay == 0 && attackCount > 0;
    }

    public void recordAttack() {
        if (attackCount > 0) attackCount--;
    }

    public void resetForTurn() {
        attackCount = 1;
        summoningSick = false;
    }
}
