package com.pear.pudding.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
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

        // Create title label with animation
        boolean playerWon = winner.equals("You") || winner.startsWith("You cleared");
        String titleText = playerWon ? (winner.startsWith("You cleared") ? winner : "YOU WIN!") : "YOU LOSE!";

        Label.LabelStyle titleStyle = new Label.LabelStyle(skin.get(Label.LabelStyle.class));
        titleStyle.fontColor = playerWon ? new Color(1, 0.85f, 0.1f, 1) : new Color(0.9f, 0.2f, 0.2f, 1);
        Label titleLabel = new Label(titleText, titleStyle);
        titleLabel.setFontScale(2f);
        titleLabel.getColor().a = 0f;
        titleLabel.addAction(Actions.fadeIn(0.5f));

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

        // Animate button entrance with staggered delays
        playAgainButton.getColor().a = 0f;
        playAgainButton.addAction(Actions.sequence(Actions.delay(0.4f), Actions.fadeIn(0.35f)));

        menuButton.getColor().a = 0f;
        menuButton.addAction(Actions.sequence(Actions.delay(0.55f), Actions.fadeIn(0.35f)));

        mainTable.add(titleLabel).padBottom(50);
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

        stage.act(delta);
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
