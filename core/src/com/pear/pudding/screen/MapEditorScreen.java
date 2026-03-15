package com.pear.pudding.screen;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.pear.pudding.MyGame;
import com.pear.pudding.world.Chest;
import com.pear.pudding.world.EnemyNPC;
import com.pear.pudding.world.WorldMap;

import static com.pear.pudding.game.data.GameConstants.WINDOW_HEIGHT;
import static com.pear.pudding.game.data.GameConstants.WINDOW_WIDTH;

public class MapEditorScreen implements Screen, InputProcessor {
    enum Brush { GRASS, WALL, ENEMY, CHEST, PLAYER_SPAWN, RESET }

    private final MyGame game;
    private final WorldMap worldMap;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final SpriteBatch hudBatch;
    private final ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private OrthographicCamera hudCam;
    private Texture[] grassTextures;
    private Texture[] wallTextures;
    private Texture enemyTexture;
    private Texture chestTexture;

    // Iso rendering constants (must match WorldScreen)
    private static final int ISO_TW      = 64;
    private static final int ISO_TH      = 32;
    private static final int ISO_HALF_W  = 32;
    private static final int ISO_HALF_H  = 16;
    private static final int WALL_FACE_H = 20;

    private Brush currentBrush = Brush.GRASS;
    private float cameraIsoX;
    private float cameraIsoY;
    private boolean moveUp, moveDown, moveLeft, moveRight;
    private float saveMessageTime = 0;
    private static final float SAVE_MESSAGE_DURATION = 1.5f;

    private static final float CAMERA_SPEED = 400f;
    private static final float TOOLBAR_HEIGHT = 40;
    private static final float BUTTON_WIDTH = 80;
    private static final float BUTTON_SPACING = 5;

    // Toolbar button positions (HUD space)
    private final float grassButtonX;
    private final float wallButtonX;
    private final float enemyButtonX;
    private final float chestButtonX;
    private final float playerSpawnButtonX;
    private final float resetButtonX;
    private final float saveButtonX;
    private final float menuButtonX;

    // Hover tile (tile coords; -1 = none)
    private int hoverTileX = -1;
    private int hoverTileY = -1;

    // Raw screen mouse position
    private int lastMouseX = -1;
    private int lastMouseY = -1;

    public MapEditorScreen(MyGame game) {
        this.game = game;
        this.worldMap = new WorldMap();

        // Start camera at iso-centre of the map
        float centerWorldX = (WorldMap.MAP_WIDTH  / 2f) * WorldMap.TILE_SIZE;
        float centerWorldY = (WorldMap.MAP_HEIGHT / 2f) * WorldMap.TILE_SIZE;
        cameraIsoX = toIsoX(centerWorldX, centerWorldY);
        cameraIsoY = toIsoY(centerWorldX, centerWorldY);

        camera = new OrthographicCamera();
        viewport = new FitViewport(1800, 1000, camera);
        viewport.apply();
        camera.position.set(cameraIsoX, cameraIsoY, 0);
        camera.update();

        hudBatch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        hudCam = new OrthographicCamera();
        hudCam.setToOrtho(false, WINDOW_WIDTH, WINDOW_HEIGHT);
        hudCam.update();

        float startX = BUTTON_SPACING;
        grassButtonX       = startX;
        wallButtonX        = grassButtonX       + BUTTON_WIDTH + BUTTON_SPACING;
        enemyButtonX       = wallButtonX        + BUTTON_WIDTH + BUTTON_SPACING;
        chestButtonX       = enemyButtonX       + BUTTON_WIDTH + BUTTON_SPACING;
        playerSpawnButtonX = chestButtonX       + BUTTON_WIDTH + BUTTON_SPACING;
        resetButtonX       = playerSpawnButtonX + BUTTON_WIDTH + BUTTON_SPACING;
        saveButtonX        = resetButtonX       + BUTTON_WIDTH + BUTTON_SPACING;
        menuButtonX        = saveButtonX        + BUTTON_WIDTH + BUTTON_SPACING;
    }

    // --- Iso helpers (must match WorldScreen) ---

    private float toIsoX(float worldX, float worldY) {
        return (worldX / WorldMap.TILE_SIZE - worldY / WorldMap.TILE_SIZE) * ISO_HALF_W;
    }

    private float toIsoY(float worldX, float worldY) {
        return (worldX / WorldMap.TILE_SIZE + worldY / WorldMap.TILE_SIZE) * ISO_HALF_H;
    }

    /** Inverse iso transform: iso screen position → fractional tile X. Floor to get tile index. */
    private float isoToTileX(float isoX, float isoY) {
        // Tiles are drawn with bottom-left of bounding box at (tx-ty)*32, (tx+ty)*16.
        // Diamond centre is offset +32, +16 from that corner, so inverse needs -1.
        return (isoX / ISO_HALF_W + isoY / ISO_HALF_H) / 2f - 1f;
    }

    /** Inverse iso transform: iso screen position → fractional tile Y. Floor to get tile index. */
    private float isoToTileY(float isoX, float isoY) {
        return (isoY / ISO_HALF_H - isoX / ISO_HALF_W) / 2f;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(this);
        ensureTexturesLoaded();
    }

    private void ensureTexturesLoaded() {
        if (grassTextures == null) {
            try {
                game.manager.finishLoading();
                grassTextures = new Texture[16];
                for (int i = 0; i < 16; i++) {
                    grassTextures[i] = new Texture("grass" + i + ".png");
                    grassTextures[i].setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                }
                wallTextures = new Texture[8];
                for (int i = 0; i < 8; i++) {
                    wallTextures[i] = new Texture("wall" + i + ".png");
                    wallTextures[i].setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                }
                enemyTexture = new Texture("enemy.png");
                enemyTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                chestTexture = new Texture("chest.png");
                chestTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void ensureFontLoaded() {
        if (font == null) {
            try {
                game.manager.finishLoading();
                com.badlogic.gdx.scenes.scene2d.ui.Skin skin =
                    game.manager.get("uiskin.json", com.badlogic.gdx.scenes.scene2d.ui.Skin.class);
                if (skin != null) font = skin.getFont("default-font");
            } catch (Exception e) { /* retry next frame */ }
        }
    }

    @Override
    public void render(float delta) {
        update(delta);
        draw();
    }

    private void update(float delta) {
        // Pan camera directly in iso space
        float dx = 0, dy = 0;
        if (moveRight) dx += CAMERA_SPEED;
        if (moveLeft)  dx -= CAMERA_SPEED;
        if (moveUp)    dy += CAMERA_SPEED;
        if (moveDown)  dy -= CAMERA_SPEED;
        if (dx != 0 && dy != 0) {
            float len = (float) Math.sqrt(dx * dx + dy * dy);
            dx = dx / len * CAMERA_SPEED;
            dy = dy / len * CAMERA_SPEED;
        }
        cameraIsoX += dx * delta;
        cameraIsoY += dy * delta;

        // Clamp to iso world bounds
        cameraIsoX = MathUtils.clamp(cameraIsoX,
            -(WorldMap.MAP_HEIGHT - 1) * ISO_HALF_W + camera.viewportWidth  / 2,
             (WorldMap.MAP_WIDTH  - 1) * ISO_HALF_W - camera.viewportWidth  / 2);
        cameraIsoY = MathUtils.clamp(cameraIsoY,
            camera.viewportHeight / 2,
            (WorldMap.MAP_WIDTH + WorldMap.MAP_HEIGHT - 2) * ISO_HALF_H - camera.viewportHeight / 2);

        camera.position.set(cameraIsoX, cameraIsoY, 0);
        camera.update();

        if (saveMessageTime > 0) saveMessageTime -= delta;

        updateHoverTile(lastMouseX, lastMouseY);
    }

    private void updateHoverTile(int screenX, int screenY) {
        if (screenX < 0 || screenY < 0) { hoverTileX = -1; hoverTileY = -1; return; }

        // Suppress hover when over toolbar
        Vector3 hudPos = hudCam.unproject(new Vector3(screenX, screenY, 0));
        if (hudPos.y > (WINDOW_HEIGHT - TOOLBAR_HEIGHT)) { hoverTileX = -1; hoverTileY = -1; return; }

        // Unproject through world camera → iso world → tile coords
        Vector3 worldPos = camera.unproject(new Vector3(screenX, screenY, 0));
        hoverTileX = (int) Math.floor(isoToTileX(worldPos.x, worldPos.y));
        hoverTileY = (int) Math.floor(isoToTileY(worldPos.x, worldPos.y));
    }

    private void draw() {
        Gdx.gl.glClearColor(0.12f, 0.18f, 0.12f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        ensureTexturesLoaded();

        // --- Iso tile rendering: back-to-front ---
        SpriteBatch spriteBatch = new SpriteBatch();
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();

        int maxDepth = WorldMap.MAP_WIDTH + WorldMap.MAP_HEIGHT - 2;
        for (int depth = maxDepth; depth >= 0; depth--) {
            int txStart = Math.max(0, depth - WorldMap.MAP_HEIGHT + 1);
            int txEnd   = Math.min(WorldMap.MAP_WIDTH - 1, depth);
            for (int tx = txStart; tx <= txEnd; tx++) {
                int ty = depth - tx;
                if (ty < 0 || ty >= WorldMap.MAP_HEIGHT) continue;

                float sx = (tx - ty) * ISO_HALF_W;
                float sy = (tx + ty) * ISO_HALF_H;

                // Viewport cull
                if (sx + ISO_TW < camera.position.x - 950) continue;
                if (sx         > camera.position.x + 950) continue;
                if (sy + ISO_TH + WALL_FACE_H < camera.position.y - 550) continue;
                if (sy         > camera.position.y + 550) continue;

                byte tile = worldMap.getTile(tx, ty);
                int hash  = ((tx * 73856093) ^ (ty * 19349663)) & 0x7FFFFFFF;

                if (tile == 0 && grassTextures != null) {
                    spriteBatch.setColor(1, 1, 1, 1);
                    spriteBatch.draw(grassTextures[hash % 16], sx, sy, ISO_TW, ISO_TH);
                } else if (tile != 0 && wallTextures != null) {
                    spriteBatch.setColor(1, 1, 1, 1);
                    spriteBatch.draw(wallTextures[hash % 8], sx, sy, ISO_TW, ISO_TH);
                    spriteBatch.setColor(0.45f, 0.45f, 0.45f, 1);
                    spriteBatch.draw(wallTextures[hash % 8], sx, sy - WALL_FACE_H, ISO_TW, WALL_FACE_H);
                    spriteBatch.setColor(1, 1, 1, 1);
                }
            }
        }

        spriteBatch.end();

        // --- Entities + hover highlight (ShapeRenderer, world camera) ---
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.setProjectionMatrix(camera.combined);

        // Hover diamond highlight
        if (hoverTileX >= 0 && hoverTileY >= 0
                && hoverTileX < WorldMap.MAP_WIDTH && hoverTileY < WorldMap.MAP_HEIGHT) {
            float sx = (hoverTileX - hoverTileY) * ISO_HALF_W;
            float sy = (hoverTileX + hoverTileY) * ISO_HALF_H;
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(1, 1, 0, 0.25f);
            shapeRenderer.triangle(sx + ISO_HALF_W, sy,
                                   sx + ISO_TW,     sy + ISO_HALF_H,
                                   sx + ISO_HALF_W, sy + ISO_TH);
            shapeRenderer.triangle(sx + ISO_HALF_W, sy,
                                   sx + ISO_HALF_W, sy + ISO_TH,
                                   sx,              sy + ISO_HALF_H);
            shapeRenderer.end();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(1, 1, 0, 0.9f);
            shapeRenderer.line(sx + ISO_HALF_W, sy,              sx + ISO_TW,     sy + ISO_HALF_H);
            shapeRenderer.line(sx + ISO_TW,     sy + ISO_HALF_H, sx + ISO_HALF_W, sy + ISO_TH);
            shapeRenderer.line(sx + ISO_HALF_W, sy + ISO_TH,     sx,              sy + ISO_HALF_H);
            shapeRenderer.line(sx,              sy + ISO_HALF_H, sx + ISO_HALF_W, sy);
            shapeRenderer.end();
        }

        // Player spawn indicator (green portrait rect)
        if (worldMap.hasPlayerSpawn()) {
            float wx = worldMap.getPlayerSpawnX() * WorldMap.TILE_SIZE + WorldMap.TILE_SIZE / 2f;
            float wy = worldMap.getPlayerSpawnY() * WorldMap.TILE_SIZE + WorldMap.TILE_SIZE / 2f;
            float ix = toIsoX(wx, wy);
            float iy = toIsoY(wx, wy);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0.2f, 1, 0.2f, 1);
            shapeRenderer.rect(ix - 8, iy - 12, 16, 24);
            shapeRenderer.end();
        }

        // Enemies (using sprite)
        if (enemyTexture != null) {
            SpriteBatch entityBatch = new SpriteBatch();
            entityBatch.setProjectionMatrix(camera.combined);
            entityBatch.begin();
            for (EnemyNPC enemy : worldMap.getEnemies()) {
                float ix = toIsoX(enemy.x, enemy.y);
                float iy = toIsoY(enemy.x, enemy.y);
                entityBatch.setColor(1, 1, 1, 1);
                entityBatch.draw(enemyTexture, ix - 16, iy - 24, 32, 48);
            }
            entityBatch.end();
            entityBatch.dispose();
        }

        // Chests (using sprite)
        if (chestTexture != null) {
            SpriteBatch entityBatch = new SpriteBatch();
            entityBatch.setProjectionMatrix(camera.combined);
            entityBatch.begin();
            for (Chest chest : worldMap.getChests()) {
                float ix = toIsoX(chest.x, chest.y);
                float iy = toIsoY(chest.x, chest.y);
                entityBatch.setColor(1, 1, 1, 1);
                entityBatch.draw(chestTexture, ix - 24, iy - 20, 48, 40);
            }
            entityBatch.end();
            entityBatch.dispose();
        }

        Gdx.gl.glDisable(GL20.GL_BLEND);

        spriteBatch.dispose();

        // --- Toolbar HUD ---
        hudCam.setToOrtho(false, WINDOW_WIDTH, WINDOW_HEIGHT);
        hudCam.update();

        shapeRenderer.setProjectionMatrix(hudCam.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.1f, 0.1f, 0.1f, 0.9f);
        shapeRenderer.rect(0, WINDOW_HEIGHT - TOOLBAR_HEIGHT, WINDOW_WIDTH, TOOLBAR_HEIGHT);
        shapeRenderer.end();

        drawToolbarButtonBackground(grassButtonX,       WINDOW_HEIGHT - TOOLBAR_HEIGHT, BUTTON_WIDTH, TOOLBAR_HEIGHT, currentBrush == Brush.GRASS);
        drawToolbarButtonBackground(wallButtonX,        WINDOW_HEIGHT - TOOLBAR_HEIGHT, BUTTON_WIDTH, TOOLBAR_HEIGHT, currentBrush == Brush.WALL);
        drawToolbarButtonBackground(enemyButtonX,       WINDOW_HEIGHT - TOOLBAR_HEIGHT, BUTTON_WIDTH, TOOLBAR_HEIGHT, currentBrush == Brush.ENEMY);
        drawToolbarButtonBackground(chestButtonX,       WINDOW_HEIGHT - TOOLBAR_HEIGHT, BUTTON_WIDTH, TOOLBAR_HEIGHT, currentBrush == Brush.CHEST);
        drawToolbarButtonBackground(playerSpawnButtonX, WINDOW_HEIGHT - TOOLBAR_HEIGHT, BUTTON_WIDTH, TOOLBAR_HEIGHT, currentBrush == Brush.PLAYER_SPAWN);
        drawToolbarButtonBackground(resetButtonX,       WINDOW_HEIGHT - TOOLBAR_HEIGHT, BUTTON_WIDTH, TOOLBAR_HEIGHT, currentBrush == Brush.RESET);
        drawToolbarButtonBackground(saveButtonX,        WINDOW_HEIGHT - TOOLBAR_HEIGHT, BUTTON_WIDTH, TOOLBAR_HEIGHT, false);
        drawToolbarButtonBackground(menuButtonX,        WINDOW_HEIGHT - TOOLBAR_HEIGHT, BUTTON_WIDTH, TOOLBAR_HEIGHT, false);

        ensureFontLoaded();
        if (font != null) {
            hudBatch.setProjectionMatrix(hudCam.combined);
            hudBatch.begin();
            font.setColor(1, 1, 1, 1);
            font.draw(hudBatch, "Grass", grassButtonX       + 15, WINDOW_HEIGHT - TOOLBAR_HEIGHT + 25);
            font.draw(hudBatch, "Wall",  wallButtonX        + 18, WINDOW_HEIGHT - TOOLBAR_HEIGHT + 25);
            font.draw(hudBatch, "Enemy", enemyButtonX       + 12, WINDOW_HEIGHT - TOOLBAR_HEIGHT + 25);
            font.draw(hudBatch, "Chest", chestButtonX       + 14, WINDOW_HEIGHT - TOOLBAR_HEIGHT + 25);
            font.draw(hudBatch, "Spawn", playerSpawnButtonX + 10, WINDOW_HEIGHT - TOOLBAR_HEIGHT + 25);
            font.draw(hudBatch, "Reset", resetButtonX       + 13, WINDOW_HEIGHT - TOOLBAR_HEIGHT + 25);
            font.draw(hudBatch, "Save",  saveButtonX        + 18, WINDOW_HEIGHT - TOOLBAR_HEIGHT + 25);
            font.draw(hudBatch, "Menu",  menuButtonX        + 17, WINDOW_HEIGHT - TOOLBAR_HEIGHT + 25);
            font.setColor(1, 1, 1, 1);
            font.draw(hudBatch, "Brush: " + currentBrush.name(), WINDOW_WIDTH - 250, 20);
            if (hoverTileX >= 0 && hoverTileY >= 0)
                font.draw(hudBatch, "[" + hoverTileX + ", " + hoverTileY + "]", WINDOW_WIDTH / 2 - 40f, 20);
            if (saveMessageTime > 0) {
                font.setColor(0, 1, 0, 1);
                font.draw(hudBatch, "Saved!", WINDOW_WIDTH / 2 - 30, 40);
                font.setColor(1, 1, 1, 1);
            }
            hudBatch.end();
        }
    }

    private void drawToolbarButtonBackground(float x, float y, float width, float height, boolean active) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(active ? 0.55f : 0.4f, active ? 0.55f : 0.4f, active ? 0.55f : 0.4f, 1);
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.end();
        if (active) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(1, 1, 1, 1);
            shapeRenderer.rect(x, y, width, height);
            shapeRenderer.end();
        }
    }

    private void paintTile() {
        if (hoverTileX < 0 || hoverTileY < 0 || hoverTileX >= WorldMap.MAP_WIDTH || hoverTileY >= WorldMap.MAP_HEIGHT)
            return;
        int tx = hoverTileX;
        int ty = hoverTileY;
        switch (currentBrush) {
            case GRASS:
                worldMap.setTile(tx, ty, (byte) 0);
                break;
            case WALL:
                worldMap.setTile(tx, ty, (byte) 1);
                break;
            case ENEMY: {
                float wx = tx * WorldMap.TILE_SIZE + WorldMap.TILE_SIZE / 2f;
                float wy = ty * WorldMap.TILE_SIZE + WorldMap.TILE_SIZE / 2f;
                worldMap.addEnemy(new EnemyNPC("enemy_" + System.nanoTime(), wx, wy));
                break;
            }
            case CHEST: {
                float wx = tx * WorldMap.TILE_SIZE + WorldMap.TILE_SIZE / 2f;
                float wy = ty * WorldMap.TILE_SIZE + WorldMap.TILE_SIZE / 2f;
                String[] cardIds = {"ghost", "skeleton", "zombie", "ghoul"};
                String cardId = cardIds[(int) (Math.random() * 4)];
                worldMap.addChest(new Chest("chest_" + System.nanoTime(), wx, wy, cardId));
                break;
            }
            case PLAYER_SPAWN:
                worldMap.setPlayerSpawn(tx, ty);
                break;
            case RESET:
                worldMap.resetMap();
                currentBrush = Brush.GRASS;
                break;
        }
    }

    private void eraseTile() {
        if (hoverTileX < 0 || hoverTileY < 0 || hoverTileX >= WorldMap.MAP_WIDTH || hoverTileY >= WorldMap.MAP_HEIGHT)
            return;
        float wx = hoverTileX * WorldMap.TILE_SIZE + WorldMap.TILE_SIZE / 2f;
        float wy = hoverTileY * WorldMap.TILE_SIZE + WorldMap.TILE_SIZE / 2f;
        Chest nearbyChest = worldMap.findNearbChest(wx, wy, WorldMap.TILE_SIZE);
        if (nearbyChest != null) {
            worldMap.removeChest(nearbyChest);
            return;
        }
        EnemyNPC nearby = worldMap.findNearbyEnemy(wx, wy, WorldMap.TILE_SIZE);
        if (nearby != null) worldMap.removeEnemy(nearby);
        else worldMap.setTile(hoverTileX, hoverTileY, (byte) 0);
    }

    @Override
    public boolean keyDown(int keycode) {
        switch (keycode) {
            case Input.Keys.W: case Input.Keys.UP:    moveUp    = true; return true;
            case Input.Keys.S: case Input.Keys.DOWN:  moveDown  = true; return true;
            case Input.Keys.A: case Input.Keys.LEFT:  moveLeft  = true; return true;
            case Input.Keys.D: case Input.Keys.RIGHT: moveRight = true; return true;
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        switch (keycode) {
            case Input.Keys.W: case Input.Keys.UP:    moveUp    = false; return true;
            case Input.Keys.S: case Input.Keys.DOWN:  moveDown  = false; return true;
            case Input.Keys.A: case Input.Keys.LEFT:  moveLeft  = false; return true;
            case Input.Keys.D: case Input.Keys.RIGHT: moveRight = false; return true;
        }
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        lastMouseX = screenX;
        lastMouseY = screenY;
        updateHoverTile(screenX, screenY);

        Vector3 hudPos = hudCam.unproject(new Vector3(screenX, screenY, 0));
        if (hudPos.y > (WINDOW_HEIGHT - TOOLBAR_HEIGHT)) {
            handleToolbarClick((int) hudPos.x);
            return true;
        }

        if (button == 0) { paintTile(); return true; }
        if (button == 1) { eraseTile(); return true; }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        lastMouseX = screenX;
        lastMouseY = screenY;
        updateHoverTile(screenX, screenY);
        Vector3 hudPos = hudCam.unproject(new Vector3(screenX, screenY, 0));
        if (hudPos.y <= (WINDOW_HEIGHT - TOOLBAR_HEIGHT)) paintTile();
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        lastMouseX = screenX;
        lastMouseY = screenY;
        return false;
    }

    private void handleToolbarClick(int virtX) {
        if      (virtX >= grassButtonX       && virtX < grassButtonX       + BUTTON_WIDTH) currentBrush = Brush.GRASS;
        else if (virtX >= wallButtonX        && virtX < wallButtonX        + BUTTON_WIDTH) currentBrush = Brush.WALL;
        else if (virtX >= enemyButtonX       && virtX < enemyButtonX       + BUTTON_WIDTH) currentBrush = Brush.ENEMY;
        else if (virtX >= chestButtonX       && virtX < chestButtonX       + BUTTON_WIDTH) currentBrush = Brush.CHEST;
        else if (virtX >= playerSpawnButtonX && virtX < playerSpawnButtonX + BUTTON_WIDTH) currentBrush = Brush.PLAYER_SPAWN;
        else if (virtX >= resetButtonX       && virtX < resetButtonX       + BUTTON_WIDTH) currentBrush = Brush.RESET;
        else if (virtX >= saveButtonX        && virtX < saveButtonX        + BUTTON_WIDTH) {
            worldMap.saveToFile(Gdx.files.local("map.txt"));
            saveMessageTime = SAVE_MESSAGE_DURATION;
        } else if (virtX >= menuButtonX && virtX < menuButtonX + BUTTON_WIDTH) {
            game.setScreen(new MenuScreen(game));
        }
    }

    @Override public boolean scrolled(float amountX, float amountY) { return false; }
    @Override public boolean keyTyped(char character) { return false; }
    @Override public boolean touchCancelled(int screenX, int screenY, int pointer, int button) { return false; }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        camera.position.set(cameraIsoX, cameraIsoY, 0);
        camera.update();
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        hudBatch.dispose();
        if (grassTextures != null)
            for (Texture t : grassTextures) if (t != null) t.dispose();
        if (wallTextures != null)
            for (Texture t : wallTextures) if (t != null) t.dispose();
    }
}
