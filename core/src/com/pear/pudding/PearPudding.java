package com.pear.pudding;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
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
import com.pear.pudding.model.*;
import com.pear.pudding.player.Player;

import static com.pear.pudding.model.Constants.*;


// TODO implement menu screen
// TODO implement game over
// TODO implement game win
// TODO implement card ability
// TODO fix font resolution
public class PearPudding extends ApplicationAdapter {
    Stage stage;
    PuddingInputProcessor inputProcessor;
    OrthographicCamera camera;
    Player player1;
    Player player2;
    Texture p1Texture;
    Texture p2Texture;
    Texture hero1Texture;
    Texture hero2Texture;
    Texture background;

    //TODO Card Types should have some affinity, i.e. undead are good against living creatures
    // ghouls are good against beings with souls
    @Override
    public void create() {
        try {
            p1Texture = new Texture(Gdx.files.internal("card.png"));
            p2Texture = new Texture(Gdx.files.internal("cardback.jpg"));
            background = new Texture(Gdx.files.internal("background.png"));
            hero1Texture = new Texture(Gdx.files.internal("ghost.png"));
            hero2Texture = new Texture(Gdx.files.internal("ghost.png"));
            player1 = new Player(true, hero1Texture);
            player2 = new Player(false, hero2Texture);
            camera = new OrthographicCamera();
            camera.setToOrtho(false, 100, 100);
            FitViewport viewp = new FitViewport(WINDOW_WIDTH, WINDOW_HEIGHT, camera);
            stage = new Stage(viewp);
            stage.addActor(player1.getHero());
            stage.addActor(player2.getHero());
            player1.initializeDeck(stage);
            player2.initializeDeck(stage);
            Skin skin = new Skin(Gdx.files.internal("uiskin.json"));
            Button buttonEndTurn = new TextButton("End Turn", skin);
            buttonEndTurn.addListener(new EndTurnClickListener(player1, player2));
            buttonEndTurn.setBounds(WINDOW_WIDTH - 100 - BUFFER, WINDOW_HEIGHT / 2, 100, 40);
            stage.addActor(buttonEndTurn);
            player1.drawCard();
            player1.setTotalMana(player1.getTotalMana() + 1);
            player1.setCurrentMana(player1.getTotalMana());
            inputProcessor = new PuddingInputProcessor(stage, player1, player2, camera);
            InputMultiplexer multiplexer = new InputMultiplexer();
            multiplexer.addProcessor(stage);
            multiplexer.addProcessor(inputProcessor); // Your screen
            Gdx.input.setInputProcessor(multiplexer);
        } catch (Exception e) {
            Gdx.app.log("Create", "Failed", e);
        }

    }

    @Override
    public void render() {
        try {
            ScreenUtils.clear(184, 176, 155, 0);

            Batch batch = stage.getBatch();
            batch.begin();

            batch.draw(background, 0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
            batch.setProjectionMatrix(camera.combined);

            stage.getActors().forEach(actor -> actor.draw(batch, 1));

            for (Slot slot : player1.getBoard().getSlots()) {
                batch.draw(p1Texture, slot.getX(), slot.getY(), slot.getWidth(), slot.getHeight());
            }

            for (Slot slot : player2.getBoard().getSlots()) {
                batch.draw(p1Texture, slot.getX(), slot.getY(), slot.getWidth(), slot.getHeight());
            }

            player1.draw(batch);
            player2.draw(batch);

            batch.end();
        } catch (Exception e) {
            stage.getBatch().end();
            Gdx.app.error("MainApp", "Error during rendering", e);
        }
    }

    @Override
    public void dispose() {
        stage.getBatch().dispose();

    }
}
