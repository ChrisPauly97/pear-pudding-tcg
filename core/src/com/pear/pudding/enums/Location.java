package com.pear.pudding.enums;

import com.badlogic.gdx.Gdx;
import lombok.Getter;

@Getter
public enum Location {
    BOARD("Board"),
    HAND("Hand"),
    DISCARD("DiscardPile"),
    DRAWDECK("DrawDeck"),
    ZOOM("Zoom");

    private String value;

    Location(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.getValue();
    }

    public static Location getEnum(String value) {
        for(Location v : values()) {
            if (v.getValue().equalsIgnoreCase(value)) return v;
        }
        throw new IllegalArgumentException();
    }
}
