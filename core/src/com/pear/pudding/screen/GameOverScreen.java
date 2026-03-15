package com.pear.pudding.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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

import static com.pear.pudding.game.data.GameConstants.WINDOW_HEIGHT;
import static com.pear.pudding.game.data.GameConstants.WINDOW_WIDTH;

public class GameOverScreen implements Screen {
    private SpriteBatch batch;
    private Stage stage;
    private Viewport viewport;
    private OrthographicCamera camera;
    private Skin skin;
    private MyGame game;
    private String winner;

    public GameOverScreen(MyGame game, String winner) {
        this.game = game;
        this.winner = winner;
        this.skin = game.manager.get("uiskin.json", Skin.class);
        this.batch = new SpriteBatch();
        this.camera = new OrthographicCamera(WINDOW_WIDTH, WINDOW_HEIGHT);
        this.viewport = new FitViewport(WINDOW_WIDTH, WINDOW_HEIGHT, camera);
        this.viewport.apply();

        camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0);
        camera.update();

        stage = new Stage(viewport, batch);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);

        Table mainTable = new Table();
        mainTable.setBounds(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        mainTable.setFillParent(true);
        mainTable.align(Align.center);

        // Add title
        BitmapFont font = skin.getFont("default");
        boolean playerWon = winner.equals("You") || winner.startsWith("You cleared");
        String titleText = playerWon ? (winner.startsWith("You cleared") ? winner : "YOU WIN!") : "YOU LOSE!";

        TextButton playAgainButton = new TextButton("Play Again", skin);
        TextButton menuButton = new TextButton("Back to Menu", skin);

        playAgainButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new WorldScreen(game));
            }
        });

        menuButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MenuScreen(game));
            }
        });

        mainTable.add(titleText).padBottom(50);
        mainTable.row();
        mainTable.add(playAgainButton).width(WINDOW_WIDTH / 3).height(WINDOW_HEIGHT / 6);
        mainTable.row();
        mainTable.add(menuButton).width(WINDOW_WIDTH / 3).height(WINDOW_HEIGHT / 6);

        stage.addActor(mainTable);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.12f, 0.16f, 1);
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
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        batch.dispose();
        stage.dispose();
    }
}
