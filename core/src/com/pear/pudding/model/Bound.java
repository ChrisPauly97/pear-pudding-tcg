package com.pear.pudding.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Bound {
    private float x;
    private float y;
    private float w;
    private float h;
    /**
     * Represents a bounding box with x,y coords and a width and height
     * Used for maintaining the previous position of the card where reversing a previous action is required
     *
     * @param x The x coordinate of the bounding box
     * @param y The y coordinate of the bounding box
     * @param w The width of the bounding box
     * @param h the height of the bounding box
     */
    public Bound(float x, float y, float w, float h){
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public void setBounds(float x, float y, float w, float h){
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public void setPosition(float x, float y){
        this.x = x;
        this.y = y;
    }
}
