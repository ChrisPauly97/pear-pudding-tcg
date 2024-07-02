package com.pear.pudding.model;

public class Constants {
    public static final float MAX_HAND_SIZE = 5;
    public static final float BUFFER = 20;
    public static final Float TEXT_BUFFER = 5f;
    public static final int NUMBER_OF_BOARD_SLOTS = 5;
    public static final float NUMBER_OF_HAND_SLOTS = 5;
    public static final float NUMBER_OF_DECK_SLOTS = 5;
    public static float WINDOW_WIDTH = 1800;
    public static float WINDOW_HEIGHT = 1200;
    public static final float WINDOWED_WINDOW_WIDTH = 1800;
    public static final float WINDOWED_WINDOW_HEIGHT = 1200;
    public static final float CARD_WIDTH = WINDOW_WIDTH / 15;
    public static final float CARD_HEIGHT = WINDOW_HEIGHT / 8;
    public static final float HERO_DIMENSION = WINDOW_WIDTH/12;
    public static final float BOARD_AND_HAND_STARTING_X_POS = (WINDOW_WIDTH / 2);// ;
    public static final float WINDOW_HEIGHT_MINUS_BUFFER = WINDOW_HEIGHT - CARD_HEIGHT - BUFFER;
    public static final float BOARD_BUFFER = BUFFER + CARD_HEIGHT + BUFFER + CARD_HEIGHT;
}