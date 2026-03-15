package com.pear.pudding.game.data;

/**
 * Specifies what a card effect can target.
 */
public enum TargetType {
    NONE,           // Effect has no target or is self-targeting
    ANY_ENEMY_MINION,
    ANY_ENEMY_HERO,
    ANY_ALLY_MINION,
    CHOOSE_ENEMY_MINION,
    CHOOSE_ENEMY_HERO,
    CHOOSE_ALLY_MINION
}
