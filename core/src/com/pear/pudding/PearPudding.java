package com.pear.pudding;

import com.badlogic.gdx.*;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.pear.pudding.input.EndTurnClickListener;
import com.pear.pudding.input.PuddingInputProcessor;
import com.pear.pudding.model.*;
import com.pear.pudding.player.Player;

import static com.pear.pudding.model.Constants.*;


// TODO implement menu screen
// TODO implement game over
// TODO implement game win
// TODO implement card ability
// TODO fix font resolution
public class PearPudding implements Screen {
    Stage stage;
    PuddingInputProcessor inputProcessor;
    OrthographicCamera camera;
    Player player1;
    Player player2;
    AssetManager manager;

    public PearPudding(AssetManager manager) {
        try {
            this.manager = manager;
            player1 = new Player(true, manager);
            player2 = new Player(false, manager);
            camera = new OrthographicCamera();
            camera.setToOrtho(false, 100, 100);
            FitViewport viewp = new FitViewport(WINDOW_WIDTH, WINDOW_HEIGHT, camera);
            stage = new Stage(viewp);
            stage.addActor(player1.getHero());
            stage.addActor(player2.getHero());
            player1.initializeDeck(stage);
            player2.initializeDeck(stage);
            Button buttonEndTurn = new TextButton("End Turn",manager.get("uiskin.json", Skin.class));
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
            batch.setProjectionMatrix(camera.combined);

            stage.getActors().forEach(actor -> actor.draw(batch, 1));

            var p1Board = player1.getBoard();
            var p2Board = player2.getBoard();
            batch.draw(manager.get("card.png", Texture.class), p1Board.getX(), p1Board.getY(), p1Board.getWidth(), p1Board.getHeight());

            batch.draw(manager.get("card.png", Texture.class), p2Board.getX(), p2Board.getY(), p2Board.getWidth(), p2Board.getHeight());


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
        stage.getBatch().dispose();
        manager.dispose();
    }
}
