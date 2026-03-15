package com.pear.pudding.game.state;

import java.util.Arrays;

/**
 * Represents a zone (hand, board, drawdeck, discard) with fixed capacity and slot-based storage.
 * Cards are identified by instanceId.
 */
public class ZoneState {
    public final ZoneType type;
    public final int capacity;
    private final String[] slots;  // instanceIds; null = empty slot

    public ZoneState(ZoneType type, int capacity) {
        this.type = type;
        this.capacity = capacity;
        this.slots = new String[capacity];
    }

    /**
     * Returns the index of the first empty slot, or -1 if full.
     */
    public int firstEmptySlot() {
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] == null) return i;
        }
        return -1;
    }

    /**
     * Adds a card to the specified slot. Returns true if successful.
     */
    public boolean addCard(String instanceId, int slot) {
        if (slot < 0 || slot >= capacity || slots[slot] != null) {
            return false;
        }
        slots[slot] = instanceId;
        return true;
    }

    /**
     * Adds a card to the first empty slot. Returns the slot index, or -1 if full.
     */
    public int addCard(String instanceId) {
        int slot = firstEmptySlot();
        if (slot == -1) return -1;
        slots[slot] = instanceId;
        return slot;
    }

    /**
     * Removes a card from the zone. Returns true if found and removed.
     */
    public boolean removeCard(String instanceId) {
        for (int i = 0; i < slots.length; i++) {
            if (instanceId.equals(slots[i])) {
                slots[i] = null;
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the instanceId at the given slot, or null if empty.
     */
    public String getCardAt(int slot) {
        if (slot < 0 || slot >= capacity) return null;
        return slots[slot];
    }

    /**
     * Finds the slot of a card, or -1 if not found.
     */
    public int findSlot(String instanceId) {
        for (int i = 0; i < slots.length; i++) {
            if (instanceId.equals(slots[i])) return i;
        }
        return -1;
    }

    /**
     * Returns true if the zone is at capacity.
     */
    public boolean isFull() {
        return firstEmptySlot() == -1;
    }

    /**
     * Returns the number of cards in the zone.
     */
    public int size() {
        int count = 0;
        for (String id : slots) {
            if (id != null) count++;
        }
        return count;
    }

    /**
     * Returns a snapshot of the current slot array for restoration later.
     */
    public String[] snapshot() {
        return Arrays.copyOf(slots, slots.length);
    }

    /**
     * Restores the zone from a snapshot.
     */
    public void restore(String[] snapshot) {
        System.arraycopy(snapshot, 0, slots, 0, slots.length);
    }

    /**
     * Returns a copy of all slot contents.
     */
    public String[] getSlots() {
        return Arrays.copyOf(slots, slots.length);
    }

    /**
     * Clears all cards from the zone.
     */
    public void clear() {
        Arrays.fill(slots, null);
    }

    /**
     * Consolidates cards to the left (shifts remaining cards to fill gaps).
     * Used after removing cards to keep hand compact.
     */
    public void consolidate() {
        String[] consolidated = new String[capacity];
        int writeIndex = 0;
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] != null) {
                consolidated[writeIndex++] = slots[i];
            }
        }
        System.arraycopy(consolidated, 0, slots, 0, capacity);
    }
}
