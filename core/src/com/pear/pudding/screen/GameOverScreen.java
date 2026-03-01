package com.pear.pudding.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.pear.pudding.MyGame;

import static com.pear.pudding.model.Constants.WINDOW_HEIGHT;
import static com.pear.pudding.model.Constants.WINDOW_WIDTH;

public class GameOverScreen implements Screen {

    private SpriteBatch batch;
    protected Stage stage;
    private Viewport viewport;
    private OrthographicCamera camera;
    private TextureAtlas atlas;
    protected Skin skin;
    private AssetManager manager;
    private MyGame game;
    public GameOverScreen(MyGame game) {
        this.manager = game.manager;
        this.game = game;
        manager.finishLoading();
        skin = manager.
                get("uiskin.json", Skin.class);
        batch = new SpriteBatch();
        camera = new OrthographicCamera(WINDOW_WIDTH, WINDOW_HEIGHT);
        viewport = new FitViewport(WINDOW_WIDTH, WINDOW_HEIGHT, camera);
        viewport.apply();

        camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0);
        camera.update();

        stage = new Stage(viewport, batch);
    }


    @Override
    public void show() {
        //Stage should control input:
        Gdx.input.setInputProcessor(stage);

        //Create Table
        Table mainTable = new Table();
        mainTable.setBounds(0,0, WINDOW_WIDTH, WINDOW_HEIGHT);
        //Set table to fill stage
        mainTable.setFillParent(true);
        //Set alignment of contents in the table.
        mainTable.align(Align.center);

        //Create buttons
        TextButton playButton = new TextButton("Play Again", skin);
        TextButton backButton = new TextButton("Back To Main Menu", skin);
        TextButton exitButton = new TextButton("Exit", skin);
        //Add listeners to buttons
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new GameLoadingScreen(game));
            }
        });
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MenuScreen(game));
            }
        });
        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });

        //Add buttons to table
        mainTable.add(playButton).width(WINDOW_WIDTH/3).height(WINDOW_HEIGHT/6);
        mainTable.row();
        mainTable.add(backButton).width(WINDOW_WIDTH/3).height(WINDOW_HEIGHT/6);
        mainTable.row();
        mainTable.add(exitButton).width(WINDOW_WIDTH/3).height(WINDOW_HEIGHT/6);

        //Add table to stage
        stage.addActor(mainTable);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(.1f, .12f, .16f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act();
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0);
        camera.update();
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
        skin.dispose();
        atlas.dispose();
    }
}
