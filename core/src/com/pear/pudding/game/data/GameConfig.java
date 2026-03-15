package com.pear.pudding.game.data;

/**
 * Global game configuration constants. No rendering constants here.
 */
public final class GameConfig {
    // Zone capacities
    public static final int HAND_CAPACITY = 5;
    public static final int BOARD_CAPACITY = 5;
    public static final int DECK_CAPACITY = 10;

    // Starting values
    public static final int STARTING_HAND_SIZE = 3;
    public static final int STARTING_HERO_HEALTH = 20;
    public static final int STARTING_HERO_ATTACK = 0;
    public static final int STARTING_MANA = 1;
    public static final int MAX_MANA = 10;

    // Game rules
    public static final int ATTACKS_PER_TURN = 1;
    public static final int DRAW_CARDS_PER_TURN = 1;

    private GameConfig() {}
}
