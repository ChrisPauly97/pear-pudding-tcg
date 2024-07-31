package com.pear.pudding.screen;

import com.badlogic.gdx.Game;
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
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.pear.pudding.MyGame;

import static com.pear.pudding.model.Constants.*;

public class OptionsScreen implements Screen {
    private MyGame game;
    private SpriteBatch batch;
    protected Stage stage;
    private Viewport viewport;
    private OrthographicCamera camera;
    private TextureAtlas atlas;
    protected Skin skin;
    private AssetManager manager;
    public OptionsScreen(MyGame game) {
        this.manager = game.manager;
        this.game = game;
        manager.load("uiskin.json", Skin.class);
        manager.finishLoading();
        skin = manager.get("uiskin.json", Skin.class);
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
        TextButton backButton = new TextButton("Back", skin);
        TextButton fullscreenButton = new TextButton("Fullscreen", skin);
        Slider fpsSlider = new Slider(30, 120, 1, false, skin);
        fpsSlider.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.graphics.setForegroundFPS((int)fpsSlider.getValue());
            }
        });
        //Add listeners to buttons
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ((Game) Gdx.app.getApplicationListener()).setScreen(new MenuScreen(game));
            }
        });

        fullscreenButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(!Gdx.graphics.isFullscreen()) {
                    Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
                    fullscreenButton.setText("Windowed");
                    WINDOW_HEIGHT = Gdx.graphics.getHeight();
                    WINDOW_WIDTH = Gdx.graphics.getWidth();
                }else{
                    Gdx.graphics.setWindowedMode((int) WINDOWED_WINDOW_WIDTH, (int) WINDOWED_WINDOW_HEIGHT);
                    fullscreenButton.setText("Fullscreen");
                    WINDOW_WIDTH = WINDOWED_WINDOW_WIDTH;
                    WINDOW_HEIGHT = WINDOWED_WINDOW_HEIGHT;
                }
            }
        });
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ((Game) Gdx.app.getApplicationListener()).setScreen(new MenuScreen(game));
            }
        });

        //Add buttons to table
        mainTable.add(fullscreenButton).width(WINDOW_WIDTH/3).height(WINDOW_HEIGHT/6);
        mainTable.row();
        mainTable.add(backButton).width(WINDOW_WIDTH/3).height(WINDOW_HEIGHT/6);
        mainTable.row();
        mainTable.add(fpsSlider).width(WINDOW_WIDTH/3).height(WINDOW_HEIGHT/6);
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
