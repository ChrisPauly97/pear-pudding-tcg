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
                clickedCard.unzoom();
                player1.getHand().setCardZoomed(false);
                clickedCard.setCurrentLocation(HAND);
            }
        }
    }

    public void handleHitCard(Card card) {
        if (card.getCurrentLocation() == Location.BOARD || card.getCurrentLocation() == Location.HAND) {
            // Use the dragging flag instead of removing the card
            if (card.getCurrentLocation() == Location.BOARD) {
                card.getPlayer().getBoard().startDragging(card);
            } else if (card.getCurrentLocation() == Location.HAND) {
                card.getPlayer().getHand().startDragging(card);
            }
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

            // Determine source deck
            Deck sourceDeck = draggingCard.getCurrentLocation() == BOARD ? playerBoard : playerHand;

            int boardTargetSlot = playerBoard.getIndexUnderMouse(coordinates);
            int handTargetSlot = playerHand.getIndexUnderMouse(coordinates);
            int enemyTargetSlot = enemyBoard.getIndexUnderMouse(coordinates);
            boolean enemyHeroTargeted = enemyHero.contains(coordinates);

            if (boardTargetSlot >= 0) {
                Gdx.app.log("Board", draggingCard.getCurrentLocation() + " " + boardTargetSlot);
                Card targetCard = playerBoard.getCardAtIndex(boardTargetSlot);
                // Allow dropping on empty slots or on the dragging card's own slot
                if (targetCard == null || targetCard == draggingCard) {
                    // Only allow playing from hand to board if player has enough mana
                    if (draggingCard.getCurrentLocation() == HAND && !draggingCard.getPlayer().hasEnoughMana(draggingCard)) {
                        // Not enough mana, return card to original location
                        resetToPreviousLocation(boardTargetSlot, playerBoard, playerHand);
                    } else {
                        // Find the actual target slot (snap to nearest free slot)
                        if (targetCard != draggingCard) {
                            if (playerBoard.onTheLeft(boardTargetSlot))
                                boardTargetSlot = playerBoard.nearestFreeSlotOnLeft(boardTargetSlot);
                            else
                                boardTargetSlot = playerBoard.nearestFreeSlotOnRight(boardTargetSlot);
                        }

                        // Deduct mana and apply summoning sickness if playing from hand
                        if (draggingCard.getCurrentLocation() == HAND) {
                            draggingCard.getPlayer().spendManaForCard(draggingCard);
                            draggingCard.setSummoningSick(true);
                        }

                        // Atomic move
                        Deck.moveCardBetweenDecks(draggingCard, sourceDeck, playerBoard, boardTargetSlot);
                    }
                }
            } else if (handTargetSlot != -1) {
                Gdx.app.log("Hand", draggingCard.getCurrentLocation() + " " + handTargetSlot);
                int targetIndex = playerHand.firstEmptySlot();
                Deck.moveCardBetweenDecks(draggingCard, sourceDeck, playerHand, targetIndex);
                playerHand.rebalance(-1);
            } else if (enemyTargetSlot != -1 && enemyBoard.getCardAtIndex(enemyTargetSlot) != null && draggingCard.getCurrentLocation() == BOARD) {
                Gdx.app.log("Enemy", draggingCard.getCurrentLocation() + " " + enemyTargetSlot);
                draggingCard.fight(enemyBoard.getCardAtIndex(enemyTargetSlot));
                // Don't reset - let dead cards stay dead and attacking card stay in place
                playerBoard.stopDragging();
            } else if (enemyHeroTargeted && draggingCard.getCurrentLocation() == BOARD) {
                Gdx.app.log("Hero", draggingCard.getCurrentLocation() + " " + boardTargetSlot);
                boolean gameOver = draggingCard.fight(enemyHero);
                if (gameOver) {
                    game.setScreen(new MenuScreen(game));
                } else {
                    // Don't reset - let attacking card stay in place
                    playerBoard.stopDragging();
                }
            } else {
                Gdx.app.log("No target", draggingCard.getCurrentLocation() + " " + boardTargetSlot);
                resetToPreviousLocation(boardTargetSlot, playerBoard, playerHand);
            }

            draggingCard = null;
            deltaCalculated = false;
            return true;
        }

        return false;
    }

    public void resetToPreviousLocation(int boardTargetSlot, Board board, Hand hand) {
        Gdx.app.log("Reset", draggingCard.getCurrentLocation() + " " + boardTargetSlot);
        switch (draggingCard.getCurrentLocation()) {
            case BOARD:
                board.restoreSnapshot();
                board.setPreviousTargetSlot(-1);
                board.stopDragging();
                break;
            case HAND:
                hand.restoreSnapshot();
                hand.setPreviousTargetSlot(-1);
                hand.stopDragging();
                break;
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
            // Note: Drawing is handled by the main render loop, no need to manually draw here
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