package com.pear.pudding;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Vector4;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.pear.pudding.card.Card;
import com.pear.pudding.enums.Location;
import com.pear.pudding.model.Bound;
import com.pear.pudding.model.Slot;
import com.pear.pudding.player.Player;

import java.util.HashMap;
import java.util.Map;

import static com.pear.pudding.enums.Location.*;

public class PuddingInputProcessor implements InputProcessor {
    Map<Integer, Boolean> stateMap = new HashMap<>();
    Card draggingCard;
    private Stage stage;
    private OrthographicCamera camera;
    private Player player1;
    private Player player2;
    private boolean deltaCalculated = false;
    Vector2 deltaVec = new Vector2();

    PuddingInputProcessor(Stage stage, Player player1, Player player2, OrthographicCamera camera) {
        this.stage = stage;
        this.camera = camera;
        this.player1 = player1;
        this.player2 = player2;
    }

    public boolean keyDown(int keycode) {
//        if (this.stateMap.containsKey(keycode)) {
//            if (this.stateMap.get(keycode)) {
//                return false;
//            } else {
//                this.stateMap.replace(keycode, true);
//                drawDeck.addCard(stage);
//            }
//        } else {
//            this.stateMap.put(keycode, true);
//            drawDeck.addCard(stage);
//        }
        return false;
    }

    public boolean keyUp(int keycode) {
        if (this.stateMap.containsKey(keycode)) {
            if (this.stateMap.get(keycode)) {
                this.stateMap.replace(keycode, false);
            }
        } else {
            this.stateMap.put(keycode, false);
        }
        return false;
    }

    public boolean keyTyped(char character) {
        return false;
    }

    public boolean touchDown(int x, int y, int pointer, int button) {
        Gdx.app.log("Click", "X=" + x + " Y=" + y);
        var coords = camera.unproject(new Vector3(x, y, camera.position.z));
        x = (int) coords.x;
        y = (int) coords.y;

        Gdx.app.log("Click", "X=" + x + " Y=" + y + " Button=" + button);
        // Check if mouse pointer hit any object in stage
        Actor hitObject = stage.hit(x, y, true);
        Gdx.app.log("Left Click Hit", "" + (stage.hit(x, y, true)));

        // If mouse clicked
        if (button == Input.Buttons.LEFT) {
            // If we clicked a card, check where the card was and act accordingly
            if (hitObject instanceof Card hitCard) {
                if (hitCard.getPlayer().isMyTurn() && hitCard.getCurrentLocation() != ZOOM) {
                    resolveHitObject(hitCard);
                }
            }
        } else if (button == Input.Buttons.RIGHT) {
            // Check if mouse pointer hit any object in stage
            Gdx.app.log("Right Click Hit", "" + (stage.hit(x, y, true)));
            Location currentLocation;
            // If we clicked a card, check where the card was and act accordingly
            if (hitObject instanceof Card hitCard) {
                currentLocation = hitCard.getCurrentLocation();
                if (currentLocation == HAND && !player1.getHand().isCardZoomed()) {
                    hitCard.zoom();
                    player1.getHand().setCardZoomed(true);
                    hitCard.setCurrentLocation(ZOOM);
                } else if (currentLocation == ZOOM) {
                    hitCard.reverseZoom();
                    player1.getHand().setCardZoomed(false);
                    hitCard.setCurrentLocation(HAND);
                }
            }
        }
        return true;
    }

    public void resolveHitObject(Card hitCard) {
        var board = hitCard.getPlayer().getBoard();
        var hand = hitCard.getPlayer().getHand();
        var drawDeck = hitCard.getPlayer().getDrawDeck();
        Gdx.app.log("Resolve Hit", hitCard.getName() + ", " + hitCard.getCurrentLocation());
        switch (hitCard.getCurrentLocation()) {
            case HAND:
                this.draggingCard = hitCard;
                break;
            case DRAW:
//                for (Slot slot : hand.getSlots()) {
//                    if (slot.getCard() == null) {
//                        hitCard.move(slot.getX(), slot.getY());
//                        hitCard.setFaceUp(true);
//                        hitCard.setCurrentLocation(HAND);
//                        slot.setCard(hitCard);
//                        drawDeck.removeCard(hitCard);
//                        return;
//                    } else {
//                        continue;
//                    }
//                }
                break;

            case BOARD:
                this.draggingCard = hitCard;
                break;
            case DISCARD:
                break;
        }

//        var currentLocation = hitCard.getCurrentLocation();
//         If card is currently in your hand, move it to board
//        if (currentLocation == HAND) {
//            for (Slot slot : board.getSlots()) {
//                if (slot.getCard() == null) {
//                    hitCard.setPosition(slot.getX(), slot.getY());
//                    hitCard.getCardBack().setPosition(slot.getX(), slot.getY());
//                    if (hitCard.getImage() != null) {
//                        hitCard.getImage().setPosition(slot.getX(), slot.getY());
//                    }
//                    hitCard.setFaceUp(true);
//                    hitCard.setCurrentLocation(BOARD);
//                    slot.setCard(hitCard);
//                    hand.removeCard(hitCard);
//                    return;
//                } else {
//                    continue;
//                }
//
//            }
//            // If card is currently in draw pile, move to hand
//        } else
//        if (currentLocation.equals(Location.DRAW)) {
//            for (Slot slot : hand.getSlots()) {
//                if (slot.getCard() == null) {
//                    hitCard.setPosition(slot.getX(), slot.getY());
//                    hitCard.getCardBack().setPosition(slot.getX(), slot.getY());
//                    if (hitCard.getImage() != null) {
//                        hitCard.getImage().setPosition(slot.getX(), slot.getY());
//                    }
//                    hitCard.setFaceUp(false);
//                    hitCard.setCurrentLocation(HAND);
//                    slot.setCard(hitCard);
//                    drawDeck.removeCard(hitCard);
//                    return;
//                } else {
//                    continue;
//                }
//            }
//        }
    }

    public boolean touchUp(int x, int y, int pointer, int button) {
        // Get the right Y value coords from camera view
        var coords = camera.unproject(new Vector3(x, y, camera.position.z));
        // If we were dragging a card
        if (this.draggingCard != null) {
            // Check if the card we are dragging belongs to our player, if so, check if it's x,y matches any slot in the game
            if (this.draggingCard.getPlayer().equals(player1)) {
                snapTo(player1, coords, this.draggingCard);
            } else if (this.draggingCard.getPlayer().equals(player2)) {
                snapTo(player2, coords, this.draggingCard);
            }
        }
        // Reset the delta
        this.deltaCalculated = false;
        // Reset the dragging card since we're not dragging a card anymore
        this.draggingCard = null;
        return false;
    }


    /**
     * Checks if any slot belonging to the player contains the mouseX/Y
     *
     * @param player The current player whos turn it is
     * @param coords The coordinates of the mouse when the snap is triggered
     * @param c      The card that we want to snap.
     */
    public void snapTo(Player player, Vector3 coords, Card c) {
        var boardSnapped = false;
        if(player.hasEnoughMana(c)){
            boardSnapped = player.getBoard().snapTo(coords, c);
            if (boardSnapped) {
                player.getHand().removeCard(c);
                player.setMana(player.getMana() - c.getCost());
            }
        }else{
            // return to previous position
            c.move(c.getPreviousPosition().getX(), c.getPreviousPosition().getY(), c.getPreviousPosition().getW(), c.getPreviousPosition().getH());
            c.setPreviousPosition(new Bound(c.getX(), c.getY(), c.getWidth(), c.getHeight()));
        }

    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    public boolean touchDragged(int x, int y, int pointer) {
        var coords = camera.unproject(new Vector3(x, y, camera.position.z));
        if (this.draggingCard != null) {
            if (!deltaCalculated) {
                this.deltaVec = calculatePosDelta(this.draggingCard.getX(), this.draggingCard.getY(), coords.x, coords.y);
                this.deltaCalculated = true;
            }
            this.draggingCard.move(coords.x - this.deltaVec.x, coords.y - this.deltaVec.y);
        }
        return false;
    }

    public Vector2 calculatePosDelta(float cardX, float cardY, float mouseX, float mouseY) {
        return new Vector2(mouseX - cardX, mouseY - cardY);
    }

    public boolean mouseMoved(int x, int y) {
        return false;
    }

    public boolean scrolled(float amountX, float amountY) {
        return false;
    }
}