package com.pear.pudding.input;

import com.badlogic.gdx.Input;

/**
 * Centralized input controller for rebindable controls.
 * Manages keyboard input state and keybinding configuration.
 */
public class InputController {
    // Movement
    private int moveUpKey = Input.Keys.W;
    private int moveDownKey = Input.Keys.S;
    private int moveLeftKey = Input.Keys.A;
    private int moveRightKey = Input.Keys.D;

    // Alternate keys (arrow keys)
    private int moveUpKeyAlt = Input.Keys.UP;
    private int moveDownKeyAlt = Input.Keys.DOWN;
    private int moveLeftKeyAlt = Input.Keys.LEFT;
    private int moveRightKeyAlt = Input.Keys.RIGHT;

    // Actions
    private int interactKey = Input.Keys.E;
    private int jumpKey = Input.Keys.SPACE;

    // Input state (updated each frame)
    public boolean moveUp;
    public boolean moveDown;
    public boolean moveLeft;
    public boolean moveRight;

    /**
     * Update input state based on current key press state.
     */
    public void update() {
        moveUp = com.badlogic.gdx.Gdx.input.isKeyPressed(moveUpKey)
            || com.badlogic.gdx.Gdx.input.isKeyPressed(moveUpKeyAlt);
        moveDown = com.badlogic.gdx.Gdx.input.isKeyPressed(moveDownKey)
            || com.badlogic.gdx.Gdx.input.isKeyPressed(moveDownKeyAlt);
        moveLeft = com.badlogic.gdx.Gdx.input.isKeyPressed(moveLeftKey)
            || com.badlogic.gdx.Gdx.input.isKeyPressed(moveLeftKeyAlt);
        moveRight = com.badlogic.gdx.Gdx.input.isKeyPressed(moveRightKey)
            || com.badlogic.gdx.Gdx.input.isKeyPressed(moveRightKeyAlt);
    }

    /**
     * Check if a key press is the interact key (E).
     */
    public boolean isInteractPressed(int keycode) {
        return keycode == interactKey;
    }

    /**
     * Check if a key press is the jump key (Space).
     */
    public boolean isJumpPressed(int keycode) {
        return keycode == jumpKey;
    }

    /**
     * Check if a key press is a movement key.
     */
    public boolean isMovementKey(int keycode) {
        return keycode == moveUpKey || keycode == moveUpKeyAlt
            || keycode == moveDownKey || keycode == moveDownKeyAlt
            || keycode == moveLeftKey || keycode == moveLeftKeyAlt
            || keycode == moveRightKey || keycode == moveRightKeyAlt;
    }

    /**
     * Update movement state based on keycode press/release.
     */
    public void handleKeyDown(int keycode) {
        if (keycode == moveUpKey || keycode == moveUpKeyAlt) moveUp = true;
        else if (keycode == moveDownKey || keycode == moveDownKeyAlt) moveDown = true;
        else if (keycode == moveLeftKey || keycode == moveLeftKeyAlt) moveLeft = true;
        else if (keycode == moveRightKey || keycode == moveRightKeyAlt) moveRight = true;
    }

    public void handleKeyUp(int keycode) {
        if (keycode == moveUpKey || keycode == moveUpKeyAlt) moveUp = false;
        else if (keycode == moveDownKey || keycode == moveDownKeyAlt) moveDown = false;
        else if (keycode == moveLeftKey || keycode == moveLeftKeyAlt) moveLeft = false;
        else if (keycode == moveRightKey || keycode == moveRightKeyAlt) moveRight = false;
    }

    // Getters for rebinding
    public int getMoveUpKey() { return moveUpKey; }
    public int getMoveDownKey() { return moveDownKey; }
    public int getMoveLeftKey() { return moveLeftKey; }
    public int getMoveRightKey() { return moveRightKey; }
    public int getInteractKey() { return interactKey; }
    public int getJumpKey() { return jumpKey; }

    public void setMoveUpKey(int key) { moveUpKey = key; }
    public void setMoveDownKey(int key) { moveDownKey = key; }
    public void setMoveLeftKey(int key) { moveLeftKey = key; }
    public void setMoveRightKey(int key) { moveRightKey = key; }
    public void setInteractKey(int key) { interactKey = key; }
    public void setJumpKey(int key) { jumpKey = key; }

    public void reset() {
        moveUp = moveDown = moveLeft = moveRight = false;
    }
}
