package com.pear.pudding.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.pear.pudding.MyGame;
import com.pear.pudding.card.EffectTrigger;
import com.pear.pudding.model.*;
import com.pear.pudding.enums.Location;
import com.pear.pudding.player.Hero;
import com.pear.pudding.player.Player;
import com.pear.pudding.screen.GameOverScreen;
import com.pear.pudding.screen.MenuScreen;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.pear.pudding.card.EffectTrigger.NONE;
import static com.pear.pudding.card.EffectTrigger.SUMMON;
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
    public Animation<TextureRegion> runningAnimation;

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
            if (clickedCard.getPlayer().isMyTurn() && clickedCard.getCurrentLocation() != ZOOM && clickedCard.isFaceUp()) {
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
            this.draggingCard = card;
        }
    }
    // Cases to handle
    // 1: Targeting my board and has no summon effect
    //      - If I have enough mana, I can add the card to my board
    //      - If I don't have enough mana, return card to hand
    // 2. Targeting my board and has a summon effect
    //      - Return the card to previous location since the summon effect should be applied
    // 3. Targeting enemy board and has a summon effect
    //      - If there is a card in that slot trigger the summon effect and play the card in the frst slot nearest corresponding to the positon on the enemy board
    // 4. Targeting enemy board and has no summon effect
    //      - Return to previous location
    // 5. Targeting enemy hero and has no summon effect
    //      - Return to previous location
    // 6. Targeting enemy hero and has summon effect
    //      - Resolve summon effect


    public boolean touchUp(int x, int y, int pointer, int button) {
        Vector3 coordinates = camera.unproject(new Vector3(x, y, camera.position.z));
        if (draggingCard != null) {

            Board enemyBoard = player1.isMyTurn() ? player2.getBoard() : player1.getBoard();
            Hero enemyHero = player1.isMyTurn() ? player2.getHero() : player1.getHero();
            Hand enemyHand = player1.isMyTurn() ? player2.getHand() : player1.getHand();

            Board playerBoard = draggingCard.getPlayer().getBoard();
            Hero playerHero = draggingCard.getPlayer().getHero();
            Hand playerHand = draggingCard.getPlayer().getHand();

            Player activePlayer = draggingCard.getPlayer();

            int boardTargetSlot = playerBoard.getIndexUnderMouse(coordinates);
            int enemyBoardTargetSlot = enemyBoard.getIndexUnderMouse(coordinates);

            boolean enemyHeroTargeted = enemyHero.contains(coordinates);
            boolean playerHeroTargeted = playerHero.contains(coordinates);
            boolean effectTriggered = false;

            // Check if we have enough mana to play the minion and also check if the minion is not summoned yet.
            if (activePlayer.hasEnoughMana(draggingCard) && draggingCard.getCurrentLocation() == HAND) {
                if (boardTargetSlot != -1) {
                    effectTriggered = playerBoard.handleEffect(boardTargetSlot, draggingCard);
                } else if (enemyBoardTargetSlot != -1) {
                    effectTriggered = enemyBoard.handleEffect(enemyBoardTargetSlot, draggingCard);
                } else if (enemyHeroTargeted) {
                    effectTriggered = enemyHero.handleEffect(draggingCard);
                } else if (playerHeroTargeted) {
                    effectTriggered = playerHero.handleEffect(draggingCard);
                }
                if (effectTriggered || (draggingCard.getStatusEffect().getEffectTrigger().equals(NONE) && boardTargetSlot != -1)) {
                    playerBoard.addCard(draggingCard);
                    activePlayer.setCurrentMana(activePlayer.getCurrentMana() - draggingCard.getCost());
                } else {
                    draggingCard.resetToPreviousLocation();
                }

            }


            // Check if our card has an effect to trigger
//            switch (draggingCard.getStatusEffect().getEffectTrigger()) {
//                // TODO handle discarding a card from your hand
//                case DISCARD:
//                    draggingCard.handleDiscard();
//                    break;
//                // When we are summoning the card from our hand onto the board
//                case SUMMON:
//                    if (boardTargetSlot != -1) {
//                        if (summonEffectTriggeredOnPlayerBoard) {
//                            playerBoard.addCard(draggingCard);
//                            activePlayer.setCurrentMana(activePlayer.getCurrentMana() - draggingCard.getCost());
//                        } else {
//                            draggingCard.resetToPreviousLocation();
//                        }
//                    } else if (enemyBoardTargetSlot != -1) {
//                        if (summonEffectTriggeredOnEnemyBoard) {
//                            playerBoard.addCard(draggingCard);
//                            activePlayer.setCurrentMana(activePlayer.getCurrentMana() - draggingCard.getCost());
//                        } else {
//                            draggingCard.resetToPreviousLocation();
//                        }
//                    }
//                    break;
//                case NONE:
//                    if (boardTargetSlot != -1) {
//                        playerBoard.addCard(draggingCard);
//                        activePlayer.setCurrentMana(activePlayer.getCurrentMana() - draggingCard.getCost());
//                    }
//                    break;
//            }
//        } else if (draggingCard.getCurrentLocation() == BOARD) {
//            if (enemyBoardTargetSlot != -1 && enemyBoard.getCardAtIndex(enemyBoardTargetSlot) != null) {
//                Gdx.app.log("Enemy", draggingCard.getCurrentLocation() + " " + enemyBoardTargetSlot);
//                if (draggingCard.getAttackCount() > 0) {
//                    draggingCard.fight(enemyBoard.getCardAtIndex(enemyBoardTargetSlot));
//                } else {
//                    this.draggingCard.resetToPreviousLocation();
//                }
//            } else if (enemyHeroTargeted) {
//                Gdx.app.log("Hero", draggingCard.getCurrentLocation() + " " + boardTargetSlot);
//                boolean gameOver = draggingCard.fight(enemyHero);
//                if (gameOver) {
//                    game.setScreen(new GameOverScreen(game));
//                } else {
//                    this.draggingCard.resetToPreviousLocation();
//                }
//            }
//        }
        this.draggingCard.resetToPreviousLocation();
        stage.getBatch().begin();
        playerBoard.draw(stage.getBatch());
        playerHand.draw(stage.getBatch());
        playerHero.draw(stage.getBatch());
        enemyBoard.draw(stage.getBatch());
        enemyHero.draw(stage.getBatch());
        enemyHand.draw(stage.getBatch());
        stage.getBatch().end();
        draggingCard = null;
        return true;
    }
    return false;
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
        if (this.draggingCard.getPlayer().hasEnoughMana(this.draggingCard) && this.draggingCard.getStatusEffect().getEffectTrigger() != SUMMON) {
            myBoard.handleHover(mouseCoords);
        } else if (this.draggingCard.getPlayer().hasEnoughMana(this.draggingCard) && this.draggingCard.getStatusEffect().getEffectTrigger() != SUMMON) {
//        var atlas = game.manager.get("data/arrow.pack.atlas", TextureAtlas.class);
//        var anim = new Animation<TextureRegion>(0.033f, atlas.findRegions("arrow"), Animation.PlayMode.LOOP);
//        TextureRegion currentFrame = anim.getKeyFrame(stateTime, true)
//        runningAnimation = new Animation<TextureRegion>(0.033f, atlas.findRegions("SummonEffectArrow"), Animation.PlayMode.LOOP);
        }
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