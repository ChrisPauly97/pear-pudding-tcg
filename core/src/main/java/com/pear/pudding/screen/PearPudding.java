package com.pear.pudding.screen;

import com.badlogic.gdx.*;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.pear.pudding.MyGame;
import com.pear.pudding.config.CardProperties;
import com.pear.pudding.enums.Location;
import com.pear.pudding.input.EndTurnClickListener;
import com.pear.pudding.model.Board;
import com.pear.pudding.model.Card;
import com.pear.pudding.model.Hand;
import com.pear.pudding.player.Hero;
import com.pear.pudding.player.Player;


import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.pear.pudding.card.EffectTrigger.NONE;
import static com.pear.pudding.card.EffectTrigger.SUMMON;
import static com.pear.pudding.enums.Location.*;
import static com.pear.pudding.model.Constants.*;


// TODO implement menu screen
// TODO implement game over
// TODO implement game win
// TODO implement card ability
// TODO fix font resolution
public class PearPudding implements Screen, InputProcessor{
    Stage stage;
    OrthographicCamera camera;
    Player player1;
    Player player2;
    AssetManager manager;
    Button backButton;
    Button endTurnButton;
    float stateTime;
    Animation<TextureRegion> runningAnimation;
    Card draggingCard;
    Map<Integer, Boolean> stateMap = new HashMap<>();
    private boolean deltaCalculated = false;
    Vector2 deltaVec = new Vector2();
    Animation<TextureRegion> currentAnimation;
    CardProperties props = new CardProperties();

    public PearPudding(MyGame game, AssetManager manager, Player player1, Player player2) {
        try {

            this.manager = manager;
            this.player1 = player1;
            this.player2 = player2;
            camera = new OrthographicCamera();
            camera.setToOrtho(false, 100, 100);
            FitViewport viewp = new FitViewport(WINDOW_WIDTH, WINDOW_HEIGHT, camera);
            this.stage = new Stage(viewp);
            for(Card c: player1.getDrawDeck().getCards()){
                stage.addActor(c);
            }
            for(Card c: player2.getDrawDeck().getCards()){
                stage.addActor(c);
            }
            stage.addActor(player1.getHero());
            stage.addActor(player2.getHero());

            Button buttonEndTurn = new TextButton("End Turn", manager.get("uiskin.json", Skin.class));
            buttonEndTurn.addListener(new EndTurnClickListener(player1, player2));
            buttonEndTurn.setBounds(WINDOW_WIDTH - 100 - BUFFER, WINDOW_HEIGHT / 2, 100, 40);

            Button backButton = new TextButton("Back", manager.get("uiskin.json", Skin.class));
            backButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    game.setScreen(new MenuScreen(game));
                }
            });

            final int FRAME_COLS = 10, FRAME_ROWS = 8;
            var walkSheet = new Texture(Gdx.files.internal("hauntingHarold.png"));
            TextureRegion[][] tmp = TextureRegion.split(walkSheet,
                    walkSheet.getWidth() / FRAME_COLS,
                    walkSheet.getHeight() / FRAME_ROWS);
            TextureRegion[] walkFrames = new TextureRegion[FRAME_COLS * FRAME_ROWS];
            int index = 0;
            for (int i = 0; i < FRAME_ROWS; i++) {
                for (int j = 0; j < FRAME_COLS; j++) {
                    walkFrames[index++] = tmp[i][j];
                }
            }
            runningAnimation = new Animation<>(0.01f, walkFrames);
            runningAnimation.setPlayMode(Animation.PlayMode.LOOP);


            backButton.setBounds(WINDOW_WIDTH - 100 - BUFFER, WINDOW_HEIGHT / 2 + 80, 100, 40);
            stage.addActor(backButton);
            stage.addActor(buttonEndTurn);
            player1.drawCard();
            player1.drawCard();
            player1.drawCard();
            player1.setTotalMana(player1.getTotalMana() + 1);
            player1.setCurrentMana(player1.getTotalMana());
            InputMultiplexer multiplexer = new InputMultiplexer();
            multiplexer.addProcessor(stage);
            multiplexer.addProcessor(this); // Your screen
            Gdx.input.setInputProcessor(multiplexer);
        } catch (Exception e) {
            Gdx.app.log("Create", "Failed", e);
        }
    }

    //TODO Card Types should have some affinity, i.e. undead are good against living creatures
    // ghouls are good against beings with souls
    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        try {
            ScreenUtils.clear(184, 176, 155, 0);
            Batch batch = stage.getBatch();
            batch.begin();
            batch.draw(manager.get("background.png", Texture.class), 0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
            stateTime += Gdx.graphics.getDeltaTime(); // Accumulate elapsed animation time
            batch.setProjectionMatrix(camera.combined);
            var p1Board = player1.getBoard();
            var p2Board = player2.getBoard();
            if(currentAnimation != null){
                if(this.draggingCard != null){
                    batch.draw(currentAnimation.getKeyFrame(stateTime), this.draggingCard.getX(),
                            this.draggingCard.getY() + this.draggingCard.getHeight(), this.draggingCard.getWidth(), this.draggingCard.getHeight());
                }
            }
            batch.draw(manager.get("card.png", Texture.class), p1Board.getX(), p1Board.getY(), p1Board.getWidth(), p1Board.getHeight());
            batch.draw(manager.get("card.png", Texture.class), p2Board.getX(), p2Board.getY(), p2Board.getWidth(), p2Board.getHeight());
            stage.getActors().forEach(actor -> actor.draw(batch, 1));

            player1.draw(batch);
            player2.draw(batch);

            batch.end();
        } catch (Exception e) {
            stage.getBatch().end();
            Gdx.app.error("MainApp", "Error during rendering", e);
        }
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        stage.dispose();
        manager.dispose();
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
                if (boardTargetSlot != -1) { // Target My Board
                    effectTriggered = playerBoard.handleEffect(boardTargetSlot, draggingCard);
                } else if (enemyBoardTargetSlot != -1) { // Target Enemy Board
                    effectTriggered = enemyBoard.handleEffect(enemyBoardTargetSlot, draggingCard);
                } else if (enemyHeroTargeted) { // Target Enemy Hero
                    effectTriggered = enemyHero.handleEffect(draggingCard);
                } else if (playerHeroTargeted) { // Target Player Hero
                    effectTriggered = playerHero.handleEffect(draggingCard);
                }
                // If a summon effect triggered or if we are dragging from hand to board, play the card
                if (effectTriggered || (draggingCard.getStatusEffect().getEffectTrigger().equals(NONE) && boardTargetSlot != -1)) {
                    playerBoard.addCard(draggingCard);
                    activePlayer.setCurrentMana(activePlayer.getCurrentMana() - draggingCard.getCost());
                } else {
                    draggingCard.resetToPreviousLocation();
                }
            } else if (draggingCard.getAttackCount() > 0 && draggingCard.getCurrentLocation() == BOARD) {
                if (enemyBoardTargetSlot != -1) { // Target Enemy Board
                    var enemyCard = enemyBoard.getCardAtIndex(enemyBoardTargetSlot);
                    if(enemyCard != null){
                        draggingCard.fight(enemyCard);
                    }
                } else if (enemyHeroTargeted) { // Target Enemy Hero
                    draggingCard.fight(enemyHero);
                }
                draggingCard.resetToPreviousLocation();
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
            currentAnimation = null;
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
            currentAnimation = runningAnimation;
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
            TextureRegion currentFrame = runningAnimation.getKeyFrame(stateTime, true);
            stage.getBatch().draw(currentFrame , this.draggingCard.getImage().getImageX(), this.draggingCard.getImage().getImageY());
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
