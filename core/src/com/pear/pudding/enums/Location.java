package com.pear.pudding.enums;

public enum Location {
    BOARD("Board"),
    HAND("Hand"),
    DISCARD("DiscardPile"),
    DRAWDECK("DrawDeck"),
    ZOOM("Zoom");

    private final String value;

    Location(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return getValue();
    }

    public static Location getEnum(String value) {
        for(Location v : values()) {
            if (v.getValue().equalsIgnoreCase(value)) return v;
        }
        throw new IllegalArgumentException();
    }
}
