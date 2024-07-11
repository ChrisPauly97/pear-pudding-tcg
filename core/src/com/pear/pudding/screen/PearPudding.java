package com.pear.pudding.screen;

import com.badlogic.gdx.*;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.pear.pudding.MyGame;
import com.pear.pudding.input.EndTurnClickListener;
import com.pear.pudding.input.PuddingInputProcessor;
import com.pear.pudding.model.Card;
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
    Button backButton;
    Button endTurnButton;

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

            backButton.setBounds(WINDOW_WIDTH - 100 - BUFFER, WINDOW_HEIGHT / 2 + 80, 100, 40);
            stage.addActor(backButton);
            stage.addActor(buttonEndTurn);
            player1.drawCard();
            player1.drawCard();
            player1.drawCard();
            player1.setTotalMana(player1.getTotalMana() + 1);
            player1.setCurrentMana(player1.getTotalMana());
            inputProcessor = new PuddingInputProcessor(game, stage, player1, player2, camera);
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
            var p1Board = player1.getBoard();
            var p2Board = player2.getBoard();
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
}
