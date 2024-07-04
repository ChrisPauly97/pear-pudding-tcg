package com.pear.pudding.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.pear.pudding.model.Board;
import com.pear.pudding.model.Bound;
import com.pear.pudding.model.Card;
import com.pear.pudding.enums.Location;
import com.pear.pudding.model.Slot;
import com.pear.pudding.player.Player;

import java.util.HashMap;
import java.util.Map;

import static com.pear.pudding.enums.Location.*;

public class PuddingInputProcessor implements InputProcessor {
    Map<Integer, Boolean> stateMap = new HashMap<>();
    Card draggingCard;
    private Slot previousTargetSlot;
    private final Stage stage;
    private final OrthographicCamera camera;
    private final Player player1;
    private final Player player2;
    private boolean deltaCalculated = false;
    Vector2 deltaVec = new Vector2();

    public PuddingInputProcessor(Stage stage, Player player1, Player player2, OrthographicCamera camera) {
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

    public boolean touchDown(int touchX, int touchY, int pointer, int button) {
        Vector3 touchCoords = camera.unproject(new Vector3(touchX, touchY, camera.position.z));
        int x = (int) touchCoords.x;
        int y = (int) touchCoords.y;

        Actor hitObject = stage.hit(x, y, true);

        if (button == Input.Buttons.LEFT) {
            handleLeftClick(hitObject);
        } else if (button == Input.Buttons.RIGHT) {
            handleRightClick(hitObject);
        }
        return true;
    }

    private void handleLeftClick(Actor hitObject) {
        if (hitObject instanceof Card clickedCard) {
            if (clickedCard.getPlayer().isMyTurn() && clickedCard.getCurrentLocation() != ZOOM) {
                resolveHitObject(clickedCard);
            }
        }
    }

    private void handleRightClick(Actor hitObject) {
        if (hitObject instanceof Card clickedCard) {
            Location currentLocation = clickedCard.getCurrentLocation();
            if (currentLocation == HAND && !player1.getHand().isCardZoomed()) {
                clickedCard.zoom();
                player1.getHand().setCardZoomed(true);
                clickedCard.setCurrentLocation(ZOOM);
            } else if (currentLocation == ZOOM) {
//                clickedCard.reverseZoom();
                player1.getHand().setCardZoomed(false);
                clickedCard.setCurrentLocation(HAND);
            }
        }
    }

    public void resolveHitObject(Card hitCard) {
        Gdx.app.log("Resolve Hit", hitCard.getName() + ", " + hitCard.getCurrentLocation());
        switch (hitCard.getCurrentLocation()) {
            case HAND:
                this.draggingCard = hitCard;
                this.draggingCard.getPlayer().getBoard().snapShot();
                break;
            case DRAW, DISCARD:
                break;
        }
    }


    public boolean touchUp(int xCoord, int yCoord, int pointer, int button) {
        Vector3 coordinates = camera.unproject(new Vector3(xCoord, yCoord, camera.position.z));
        if (draggingCard != null) {
            var board = draggingCard.getPlayer().getBoard();
            // If you're hovering over a slot on the board
            var slot = board.handleHover(coordinates);
            if(slot != -1) {
                var targetSlotPos = board.getSlotPositionAtIndex(slot);
                board.addCard(this.draggingCard, slot);
                this.draggingCard.move(targetSlotPos.x, targetSlotPos.y);
                this.draggingCard.setCurrentLocation(BOARD);
            }


            // TODO: remove this
            var slot1 = draggingCard.getPlayer().getBoard().getCards()[0];
            var slot2 = draggingCard.getPlayer().getBoard().getCards()[1];
            var slot3 = draggingCard.getPlayer().getBoard().getCards()[2];
            var slot4 = draggingCard.getPlayer().getBoard().getCards()[3];
            var slot5 = draggingCard.getPlayer().getBoard().getCards()[4];

            if (slot1 != null) {
                Gdx.app.log("Slot 1", slot1.toString());
            } else {
                Gdx.app.log("Slot 1", "null");
            }
            if (slot2 != null) {
                Gdx.app.log("Slot 2", slot2.toString());
            } else {
                Gdx.app.log("Slot 2", "null");
            }
            if (slot3 != null) {
                Gdx.app.log("Slot 3", slot3.toString());
            } else {
                Gdx.app.log("Slot 3", "null");
            }
            if (slot4 != null) {
                Gdx.app.log("Slot 4", slot4.toString());
            } else {
                Gdx.app.log("Slot 4", "null");
            }
            if (slot5 != null) {
                Gdx.app.log("Slot 5", slot5.toString());
            } else {
                Gdx.app.log("Slot 5", "null");
            }
            return false;
        }

//
//        }
//        this.previousTargetSlot = null;
//        deltaCalculated = false;
//        draggingCard = null;
//        return false;
//    }
        return false;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    public boolean touchDragged(int x, int y, int pointer) {
        var mouseCoords = camera.unproject(new Vector3(x, y, camera.position.z));
        if (this.draggingCard != null) {
            Board board = this.draggingCard.getPlayer().getBoard();
            if (board.containsCard(this.draggingCard)) {
                board.removeCard(this.draggingCard);
            }
            if (!deltaCalculated) {
                this.deltaVec = this.draggingCard.calculatePosDelta(mouseCoords.x, mouseCoords.y);
                this.deltaCalculated = true;
            }
            this.draggingCard.move(mouseCoords.x - this.deltaVec.x, mouseCoords.y - this.deltaVec.y);
            board.handleHover(mouseCoords);
            this.stage.getBatch().begin();
            this.draggingCard.draw(this.stage.getBatch(), 1f);
            this.stage.getBatch().end();

        }
        return false;
    }


    public boolean mouseMoved(int x, int y) {
        return false;
    }

    public boolean scrolled(float amountX, float amountY) {
        return false;
    }
}