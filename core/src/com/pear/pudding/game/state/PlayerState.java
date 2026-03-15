package com.pear.pudding.game.state;

/**
 * Represents a player's state: hero, mana, hand, board, decks.
 */
public class PlayerState {
    private final String playerId;
    private int totalMana;
    private int currentMana;
    private final HeroState hero;
    private final ZoneState hand;
    private final ZoneState board;
    private final ZoneState drawDeck;
    private final ZoneState discard;

    public PlayerState(String playerId, int maxMana, int heroHealth, int heroAttack,
                       int handCapacity, int boardCapacity, int deckCapacity) {
        this.playerId = playerId;
        this.totalMana = maxMana;
        this.currentMana = maxMana;
        this.hero = new HeroState(playerId, heroHealth, heroAttack);
        this.hand = new ZoneState(ZoneType.HAND, handCapacity);
        this.board = new ZoneState(ZoneType.BOARD, boardCapacity);
        this.drawDeck = new ZoneState(ZoneType.DRAW_DECK, deckCapacity);
        this.discard = new ZoneState(ZoneType.DISCARD, deckCapacity);  // discard has same capacity as deck
    }

    // Getters
    public String getPlayerId() { return playerId; }
    public int getTotalMana() { return totalMana; }
    public int getCurrentMana() { return currentMana; }
    public HeroState getHero() { return hero; }
    public ZoneState getHand() { return hand; }
    public ZoneState getBoard() { return board; }
    public ZoneState getDrawDeck() { return drawDeck; }
    public ZoneState getDiscard() { return discard; }

    // Mana management
    public void setCurrentMana(int mana) { this.currentMana = mana; }
    public void setTotalMana(int mana) { this.totalMana = mana; }

    public boolean canAfford(int cost) {
        return currentMana >= cost;
    }

    public void spendMana(int amount) {
        currentMana -= amount;
    }

    public void restoreMana(int amount) {
        currentMana = Math.min(currentMana + amount, totalMana);
    }

    public void refillMana() {
        currentMana = totalMana;
    }

    /**
     * Gets a zone by type.
     */
    public ZoneState getZone(ZoneType type) {
        return switch (type) {
            case HAND -> hand;
            case BOARD -> board;
            case DRAW_DECK -> drawDeck;
            case DISCARD -> discard;
        };
    }

    /**
     * Finds a card instance in any zone.
     */
    public ZoneState findCard(String instanceId) {
        if (hand.findSlot(instanceId) != -1) return hand;
        if (board.findSlot(instanceId) != -1) return board;
        if (drawDeck.findSlot(instanceId) != -1) return drawDeck;
        if (discard.findSlot(instanceId) != -1) return discard;
        return null;
    }
}
