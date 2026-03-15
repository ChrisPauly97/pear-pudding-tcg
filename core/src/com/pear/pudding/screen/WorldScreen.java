package com.pear.pudding.screen;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.pear.pudding.MyGame;
import com.pear.pudding.world.EnemyNPC;
import com.pear.pudding.world.WorldMap;

import static com.pear.pudding.game.data.GameConstants.WINDOW_HEIGHT;
import static com.pear.pudding.game.data.GameConstants.WINDOW_WIDTH;

public class WorldScreen implements Screen, InputProcessor {
    private final MyGame game;
    private final WorldMap worldMap;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final SpriteBatch hudBatch;
    private final ShapeRenderer shapeRenderer;
    private BitmapFont font;

    private float playerX = 50 * WorldMap.TILE_SIZE;
    private float playerY = 50 * WorldMap.TILE_SIZE;
    private boolean moveUp, moveDown, moveLeft, moveRight;

    private static final float PLAYER_SPEED = 180f;
    private static final float PLAYER_RADIUS = 12f;
    private static final float INTERACT_RANGE = 48f;
    private static final float CAMERA_LERP = 5f;

    public WorldScreen(MyGame game) {
        this.game = game;
        this.worldMap = new WorldMap();

        // Initialize camera with world-sized viewport
        camera = new OrthographicCamera();
        viewport = new FitViewport(1800, 1000, camera);
        viewport.apply();
        camera.position.set(playerX, playerY, 0);
        camera.update();

        // Initialize rendering
        hudBatch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = null;  // Will be loaded in show()
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(this);

        // Load font from skin
        if (font == null) {
            game.manager.finishLoading();
            Skin skin = game.manager.get("uiskin.json", Skin.class);
            font = skin.getFont("default");
        }
    }

    @Override
    public void render(float delta) {
        update(delta);
        draw();
    }

    private void update(float delta) {
        // Build movement vector
        float dx = 0, dy = 0;
        if (moveRight) dx += PLAYER_SPEED;
        if (moveLeft) dx -= PLAYER_SPEED;
        if (moveUp) dy += PLAYER_SPEED;
        if (moveDown) dy -= PLAYER_SPEED;

        // Normalize diagonal movement
        if (dx != 0 && dy != 0) {
            float length = (float) Math.sqrt(dx * dx + dy * dy);
            dx = (dx / length) * PLAYER_SPEED;
            dy = (dy / length) * PLAYER_SPEED;
        }

        // Apply movement with separate-axis collision
        movePlayerAxis(dx * delta, 0);
        movePlayerAxis(0, dy * delta);

        // Smooth camera follow
        float targetCamX = playerX;
        float targetCamY = playerY;

        float worldPixelWidth = WorldMap.MAP_WIDTH * WorldMap.TILE_SIZE;
        float worldPixelHeight = WorldMap.MAP_HEIGHT * WorldMap.TILE_SIZE;
        float viewportHalfWidth = camera.viewportWidth / 2;
        float viewportHalfHeight = camera.viewportHeight / 2;

        targetCamX = MathUtils.clamp(targetCamX, viewportHalfWidth, worldPixelWidth - viewportHalfWidth);
        targetCamY = MathUtils.clamp(targetCamY, viewportHalfHeight, worldPixelHeight - viewportHalfHeight);

        camera.position.x += (targetCamX - camera.position.x) * CAMERA_LERP * delta;
        camera.position.y += (targetCamY - camera.position.y) * CAMERA_LERP * delta;
        camera.update();
    }

    private void movePlayerAxis(float dx, float dy) {
        // Test 4 corners of player AABB
        float[] testPoints = {
            playerX - PLAYER_RADIUS, playerY - PLAYER_RADIUS,
            playerX + PLAYER_RADIUS, playerY - PLAYER_RADIUS,
            playerX - PLAYER_RADIUS, playerY + PLAYER_RADIUS,
            playerX + PLAYER_RADIUS, playerY + PLAYER_RADIUS
        };

        // Check if all corners are clear at the new position
        for (int i = 0; i < testPoints.length; i += 2) {
            float newX = testPoints[i] + dx;
            float newY = testPoints[i + 1] + dy;
            if (worldMap.isWall(newX, newY)) {
                return;  // Blocked
            }
        }

        // All clear, apply movement
        playerX += dx;
        playerY += dy;
    }

    private void draw() {
        Gdx.gl.glClearColor(0.2f, 0.3f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Draw tiles with viewport culling
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        int startTileX = Math.max(0, (int) ((camera.position.x - camera.viewportWidth / 2) / WorldMap.TILE_SIZE) - 1);
        int endTileX = Math.min(WorldMap.MAP_WIDTH, (int) ((camera.position.x + camera.viewportWidth / 2) / WorldMap.TILE_SIZE) + 1);
        int startTileY = Math.max(0, (int) ((camera.position.y - camera.viewportHeight / 2) / WorldMap.TILE_SIZE) - 1);
        int endTileY = Math.min(WorldMap.MAP_HEIGHT, (int) ((camera.position.y + camera.viewportHeight / 2) / WorldMap.TILE_SIZE) + 1);

        for (int y = startTileY; y < endTileY; y++) {
            for (int x = startTileX; x < endTileX; x++) {
                float worldX = x * WorldMap.TILE_SIZE;
                float worldY = y * WorldMap.TILE_SIZE;

                byte tile = worldMap.getTile(x, y);
                if (tile == 0) {  // Grass
                    shapeRenderer.setColor(0.3f, 0.6f, 0.3f, 1);
                } else {  // Wall
                    shapeRenderer.setColor(0.5f, 0.5f, 0.5f, 1);
                }
                shapeRenderer.rect(worldX, worldY, WorldMap.TILE_SIZE, WorldMap.TILE_SIZE);
            }
        }

        // Draw enemy NPCs
        for (EnemyNPC enemy : worldMap.getEnemies()) {
            if (enemy.alive) {
                shapeRenderer.setColor(1, 0.2f, 0.2f, 1);
                float size = WorldMap.TILE_SIZE * 0.7f;
                shapeRenderer.rect(enemy.x - size / 2, enemy.y - size / 2, size, size);
            }
        }

        // Draw player
        shapeRenderer.setColor(0.2f, 0.6f, 1, 1);
        shapeRenderer.rect(playerX - PLAYER_RADIUS, playerY - PLAYER_RADIUS, PLAYER_RADIUS * 2, PLAYER_RADIUS * 2);

        shapeRenderer.end();

        // Draw HUD (screen-space)
        if (font != null) {
            hudBatch.setProjectionMatrix(new OrthographicCamera(1800, 1000).combined);
            hudBatch.begin();

            // Movement hint
            font.draw(hudBatch, "WASD/Arrows to move", 20, 980);

            // Check for nearby enemy
            EnemyNPC nearby = worldMap.findNearbyEnemy(playerX, playerY, INTERACT_RANGE);
            if (nearby != null) {
                font.setColor(1, 1, 0, 1);  // Yellow
                font.draw(hudBatch, "Press E/Space to battle!", 20, 950);
                font.setColor(1, 1, 1, 1);  // Reset to white
            }

            // Count remaining enemies
            int aliveCount = 0;
            for (EnemyNPC e : worldMap.getEnemies()) {
                if (e.alive) aliveCount++;
            }
            font.draw(hudBatch, "Enemies remaining: " + aliveCount, 20, 920);

            hudBatch.end();
        }
    }

    @Override
    public boolean keyDown(int keycode) {
        switch (keycode) {
            case Input.Keys.W:
            case Input.Keys.UP:
                moveUp = true;
                return true;
            case Input.Keys.A:
            case Input.Keys.LEFT:
                moveLeft = true;
                return true;
            case Input.Keys.S:
            case Input.Keys.DOWN:
                moveDown = true;
                return true;
            case Input.Keys.D:
            case Input.Keys.RIGHT:
                moveRight = true;
                return true;
            case Input.Keys.E:
            case Input.Keys.SPACE:
                handleInteract();
                return true;
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        switch (keycode) {
            case Input.Keys.W:
            case Input.Keys.UP:
                moveUp = false;
                return true;
            case Input.Keys.A:
            case Input.Keys.LEFT:
                moveLeft = false;
                return true;
            case Input.Keys.S:
            case Input.Keys.DOWN:
                moveDown = false;
                return true;
            case Input.Keys.D:
            case Input.Keys.RIGHT:
                moveRight = false;
                return true;
        }
        return false;
    }

    private void handleInteract() {
        EnemyNPC enemy = worldMap.findNearbyEnemy(playerX, playerY, INTERACT_RANGE);
        if (enemy != null) {
            game.setScreen(new PearPudding(game, game.manager, this, enemy));
        }
    }

    public void onBattleWon(EnemyNPC npc) {
        npc.alive = false;
        if (worldMap.allEnemiesDefeated()) {
            game.setScreen(new GameOverScreen(game, "You cleared the world!"));
        } else {
            game.setScreen(this);
        }
    }

    public void onBattleLost() {
        game.setScreen(new GameOverScreen(game, "Enemy"));
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        camera.position.set(playerX, playerY, 0);
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
        shapeRenderer.dispose();
        hudBatch.dispose();
    }
}
