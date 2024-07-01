package com.pear.pudding;

import com.badlogic.gdx.tools.texturepacker.TexturePacker;

/**
 * @author Mats Svensson
 */
public class PackSkins {

    public static void main(String[] args) {
        TexturePacker.Settings s = new TexturePacker.Settings();
        TexturePacker.process(s, "assets/workfiles/finished", "assets/data", "loading.pack");
    }
}
