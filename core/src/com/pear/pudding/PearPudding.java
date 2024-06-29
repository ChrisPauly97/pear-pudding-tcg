package com.pear.pudding;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.pear.pudding.card.*;
import com.pear.pudding.model.Board;
import com.pear.pudding.model.DrawDeck;
import com.pear.pudding.model.Hand;
import com.pear.pudding.model.Slot;
import com.pear.pudding.player.Player;

import static com.pear.pudding.model.Constants.*;

public class PearPudding extends ApplicationAdapter {
    Stage stage;
    PuddingInputProcessor inputProcessor;
    OrthographicCamera camera;
    Player player1;
    Player player2;
    Texture p1Texture;
    Texture p2Texture;
    Texture background;
    //TODO Card Types should have some affinity, i.e. undead are good against living creatures
    // ghouls are good against beings with souls

    @Override
    public void create() {
        try {
            p1Texture =
            p2Texture = new Texture(Gdx.files.internal("cardback.jpg"));
            background = new Texture(Gdx.files.internal("background.png"));

            //TODO add a hero object to the player that has an image that is drawn
            player1 = new Player(true);
            player2 = new Player(false);

            player1.setBoard(new Board(BOARD_AND_HAND_STARTING_X_POS,BOARD_BUFFER, NUMBER_OF_BOARD_SLOTS * CARD_WIDTH, CARD_HEIGHT ));
            player1.setHand(new Hand(BOARD_AND_HAND_STARTING_X_POS, BUFFER, NUMBER_OF_HAND_SLOTS * CARD_WIDTH, CARD_HEIGHT));
            player2.setBoard(new Board(BOARD_AND_HAND_STARTING_X_POS, WINDOW_HEIGHT - BOARD_BUFFER - CARD_HEIGHT, NUMBER_OF_BOARD_SLOTS * CARD_WIDTH, CARD_HEIGHT));
            player2.setHand(new Hand(BOARD_AND_HAND_STARTING_X_POS,WINDOW_HEIGHT_MINUS_BUFFER, NUMBER_OF_HAND_SLOTS * CARD_WIDTH, CARD_HEIGHT));
            player1.setDrawDeck(new DrawDeck(WINDOW_WIDTH - 2*CARD_WIDTH,BUFFER,CARD_WIDTH,CARD_HEIGHT));
            player2.setDrawDeck(new DrawDeck(WINDOW_WIDTH - 2*CARD_WIDTH,WINDOW_HEIGHT - BUFFER - CARD_HEIGHT,CARD_WIDTH,CARD_HEIGHT));
            player2.setHealthPosition(new Vector2(WINDOW_WIDTH/2, WINDOW_HEIGHT - BUFFER *2));
            player1.setHealthPosition(new Vector2(WINDOW_WIDTH/2, BUFFER *2));

            camera = new OrthographicCamera();
            camera.setToOrtho(false, 100, 100);
            FitViewport viewp = new FitViewport(WINDOW_WIDTH,WINDOW_HEIGHT , camera);
            stage = new Stage(viewp);
            player1.initializeDeck(stage);
            player2.initializeDeck(stage);
            Skin skin = new Skin(Gdx.files.internal("uiskin.json"));
            Button buttonEndTurn = new TextButton("End Turn", skin);
            buttonEndTurn.addListener(new EndTurnClickListener(player1, player2));
            buttonEndTurn.setBounds(WINDOW_WIDTH - 100 - BUFFER,WINDOW_HEIGHT /2,100,40);
            stage.addActor(buttonEndTurn);
            player1.drawCard();
            player1.setMana(player1.getMana() + 1);
            inputProcessor = new PuddingInputProcessor(stage, player1, player2, camera);
            InputMultiplexer multiplexer = new InputMultiplexer();
            multiplexer.addProcessor(stage);
            multiplexer.addProcessor(inputProcessor); // Your screen
            Gdx.input.setInputProcessor(multiplexer);
        }catch (Exception e){
            Gdx.app.log("Create", "Failed", e);
        }

    }

    @Override
    public void render() {
        try {
            ScreenUtils.clear(184, 176, 155, 0);

            stage.getBatch().begin();
//            Gdx.app.log("Main", stage.getActors().toString());
            stage.getBatch().draw(background, 0,0,WINDOW_WIDTH, WINDOW_HEIGHT);
            stage.getBatch().setProjectionMatrix(camera.combined);
            stage.getActors().forEach(a -> a.draw(stage.getBatch(), 1));
            for(Slot s: player1.getBoard().getSlots()){
                stage.getBatch().draw(p1Texture, s.getX(), s.getY(), s.getWidth(), s.getHeight());
            }
            for(Slot s: player2.getBoard().getSlots()){
                stage.getBatch().draw(p1Texture, s.getX(), s.getY(), s.getWidth(), s.getHeight());
            }

            player1.draw(stage.getBatch());
            player2.draw(stage.getBatch());
            stage.getBatch().end();
        } catch (Exception e) {
            stage.getBatch().end();
            Gdx.app.log("MainApp", "error {}", e);
        }


    }

    @Override
    public void dispose() {
        stage.getBatch().dispose();

    }
}
