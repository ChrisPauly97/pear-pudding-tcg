package com.pear.pudding.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.pear.pudding.MyGame;
import com.pear.pudding.model.*;
import com.pear.pudding.enums.Location;
import com.pear.pudding.player.Hero;
import com.pear.pudding.player.Player;
import com.pear.pudding.screen.MenuScreen;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.pear.pudding.enums.Location.*;

public class PuddingInputProcessor implements InputProcessor {
    Map<Integer, Boolean> stateMap = new HashMap<>();
    Card draggingCard;
    private final Stage stage;
    private final MyGame game;
    private final OrthographicCamera camera;
    private final Player player1;
    private final Player player2;
    private boolean deltaCalculated = false;
    Vector2 deltaVec = new Vector2();

    public PuddingInputProcessor(MyGame game, Stage stage, Player player1, Player player2, OrthographicCamera camera) {
        this.game = game;
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
                handleHitCard(clickedCard);
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

    public void handleHitCard(Card card) {
        if (card.getCurrentLocation() == Location.BOARD || card.getCurrentLocation() == Location.HAND) {
            card.getPlayer().getBoard().removeCard(card);
            card.getPlayer().getHand().removeCard(card);
            card.getPlayer().getBoard().snapShot();
            card.getPlayer().getHand().snapShot();
            draggingCard = card;
        }
    }

    public boolean touchUp(int x, int y, int pointer, int button) {
        Vector3 coordinates = camera.unproject(new Vector3(x, y, camera.position.z));

        if (draggingCard != null) {
            Board playerBoard = draggingCard.getPlayer().getBoard();
            Hand playerHand = draggingCard.getPlayer().getHand();
            Board enemyBoard = player1.isMyTurn() ? player2.getBoard() : player1.getBoard();
            Hero enemyHero = player1.isMyTurn() ? player2.getHero() : player1.getHero();
            int boardTargetSlot = playerBoard.getIndexUnderMouse(coordinates);
            int handTargetSlot = playerHand.getIndexUnderMouse(coordinates);
            int enemyTargetSlot = enemyBoard.getIndexUnderMouse(coordinates);
            boolean enemyHeroTargeted = enemyHero.contains(coordinates);
            if (boardTargetSlot >= 0) {
                Gdx.app.log("Board", draggingCard.getCurrentLocation() + " " + boardTargetSlot);
                if (playerBoard.getCardAtIndex(boardTargetSlot) == null) {
                    if (playerBoard.onTheLeft(boardTargetSlot))
                        boardTargetSlot = playerBoard.nearestFreeSlotOnLeft(boardTargetSlot);
                    else
                        boardTargetSlot = playerBoard.nearestFreeSlotOnRight(boardTargetSlot);

                    playerBoard.addCard(draggingCard, boardTargetSlot);
                }
            } else if (handTargetSlot != -1) {
                Gdx.app.log("Hand", draggingCard.getCurrentLocation() + " " + handTargetSlot);
                if (playerHand.getCardAtIndex(handTargetSlot) == null) {
                    playerHand.addCard(draggingCard, playerHand.firstEmptySlot());
                }
            } else if (enemyTargetSlot != -1 && enemyBoard.getCardAtIndex(enemyTargetSlot) != null) {
                Gdx.app.log("Enemy", draggingCard.getCurrentLocation() + " " + enemyTargetSlot);
                draggingCard.fight(enemyBoard.getCardAtIndex(enemyTargetSlot));
                resetToPreviousLocation(boardTargetSlot, playerBoard, playerHand);
            } else if (enemyHeroTargeted) {
                Gdx.app.log("Hero", draggingCard.getCurrentLocation() + " " + boardTargetSlot);
                boolean gameOver = draggingCard.fight(enemyHero);
                if (gameOver) {
                    game.setScreen(new MenuScreen(game));
                }else {
                resetToPreviousLocation(boardTargetSlot, playerBoard, playerHand);
                }
            } else {
                Gdx.app.log("No target", draggingCard.getCurrentLocation() + " " + boardTargetSlot);
                resetToPreviousLocation(boardTargetSlot, playerBoard, playerHand);
            }

            draggingCard = null;
            return true;
        }

        return false;
    }

    public void resetToPreviousLocation(int boardTargetSlot, Board board, Hand hand) {
        Gdx.app.log("Reset", draggingCard.getCurrentLocation() + " " + boardTargetSlot);
        switch (draggingCard.getCurrentLocation()) {
            case BOARD:
                board.restoreSnapshot();
                if (board.getCardAtIndex(boardTargetSlot) == null) {
                    if (board.onTheLeft(boardTargetSlot))
                        boardTargetSlot = board.nearestFreeSlotOnLeft(boardTargetSlot);
                    else if (!board.onTheLeft(boardTargetSlot))
                        boardTargetSlot = board.nearestFreeSlotOnRight(boardTargetSlot);
                    board.addCard(this.draggingCard, boardTargetSlot);
                }
                board.setPreviousTargetSlot(-1);
                break;
            case HAND:
                hand.restoreSnapshot();
                hand.addCard(this.draggingCard, hand.firstEmptySlot());
                hand.rebalance(-1);
                hand.setPreviousTargetSlot(-1);
        }
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    public boolean touchDragged(int x, int y, int pointer) {
        var mouseCoords = camera.unproject(new Vector3(x, y, camera.position.z));
        if (this.draggingCard != null) {
            if (!deltaCalculated) {
                this.deltaVec = this.draggingCard.calculatePosDelta(mouseCoords.x, mouseCoords.y);
                this.deltaCalculated = true;
            }
            this.draggingCard.move(mouseCoords.x - this.deltaVec.x, mouseCoords.y - this.deltaVec.y, this.draggingCard.getCurrentLocation());
            Board myBoard = this.draggingCard.getPlayer().getBoard();
            myBoard.handleHover(mouseCoords);
            Hand myHand = this.draggingCard.getPlayer().getHand();
            myHand.handleHover(mouseCoords);
            stage.getBatch().begin();
            myBoard.draw(stage.getBatch());
            myHand.draw(stage.getBatch());
            stage.getBatch().end();
        }

        return true;
    }


    public boolean mouseMoved(int x, int y) {
        return false;
    }

    public boolean scrolled(float amountX, float amountY) {
        return false;
    }
}