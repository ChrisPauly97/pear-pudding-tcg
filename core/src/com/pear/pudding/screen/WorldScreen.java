package com.pear.pudding.screen;

import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.pear.pudding.MyGame;
import com.pear.pudding.world.Chest;
import com.pear.pudding.world.EnemyNPC;
import com.pear.pudding.world.WorldMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
    private Texture[] grassTextures;
    private Texture[] wallTextures;
    private Texture brickWallTop;
    private Texture brickWallFront;
    private Texture brickWallLeft;
    private Texture brickWallRight;
    private Texture playerTexture;
    private Texture enemyTexture;
    private Texture chestTexture;

    private float playerX = 50 * WorldMap.TILE_SIZE;
    private float playerY = 15 * WorldMap.TILE_SIZE;
    private boolean moveUp, moveDown, moveLeft, moveRight;

    private static final float PLAYER_SPEED = 180f;
    private static final float PLAYER_RADIUS = 12f;
    private static final float INTERACT_RANGE = 48f;
    private static final float TRACKING_AUTO_BATTLE_RANGE = 32f;  // Auto-battle when tracking enemy gets this close
    private static final float CAMERA_LERP = 5f;
    private static final float FOOTSTEP_INTERVAL = 1.0f;
    private static final float JUMP_VELOCITY = 280f;
    private static final float GRAVITY = -600f;
    private static final float WALL_HEIGHT = 24f;

    // Isometric rendering constants
    private static final int ISO_TW     = 64;   // tile diamond width
    private static final int ISO_TH     = 32;   // tile diamond height
    private static final int ISO_HALF_W = 32;
    private static final int ISO_HALF_H = 16;
    private static final int WALL_FACE_H = 20;  // wall front-face height

    private Random tileRand = new Random();

    private float playerZ = 0f;
    private float velocityZ = 0f;
    private float groundZ = 0f;
    private boolean isJumping = false;

    private Sound footstepSound;
    private float footstepTimer = 0f;

    public WorldScreen(MyGame game) {
        this.game = game;
        this.worldMap = new WorldMap();

        if (worldMap.hasPlayerSpawn()) {
            playerX = worldMap.getPlayerSpawnX() * WorldMap.TILE_SIZE + WorldMap.TILE_SIZE / 2;
            playerY = worldMap.getPlayerSpawnY() * WorldMap.TILE_SIZE + WorldMap.TILE_SIZE / 2;
        } else {
            playerX = 50 * WorldMap.TILE_SIZE;
            playerY = 15 * WorldMap.TILE_SIZE;
        }

        camera = new OrthographicCamera();
        viewport = new FitViewport(1800, 1000, camera);
        viewport.apply();
        camera.position.set(toIsoX(playerX, playerY), toIsoY(playerX, playerY), 0);
        camera.update();

        hudBatch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = null;
    }

    // --- Iso coordinate helpers ---

    private float toIsoX(float worldX, float worldY) {
        return (worldX / WorldMap.TILE_SIZE - worldY / WorldMap.TILE_SIZE) * ISO_HALF_W;
    }

    private float toIsoY(float worldX, float worldY) {
        return (worldX / WorldMap.TILE_SIZE + worldY / WorldMap.TILE_SIZE) * ISO_HALF_H;
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
                brickWallTop = new Texture("wallbrick_top.png");
                brickWallTop.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                brickWallFront = new Texture("wallbrick_front.png");
                brickWallFront.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                brickWallLeft = new Texture("wallbrick_left.png");
                brickWallLeft.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                brickWallRight = new Texture("wallbrick_right.png");
                brickWallRight.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                playerTexture = new Texture("player.png");
                playerTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                enemyTexture = new Texture("enemy.png");
                enemyTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                chestTexture = new Texture("chest.png");
                chestTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                if (footstepSound == null) {
                    footstepSound = Gdx.audio.newSound(Gdx.files.internal("freesound_community-running-in-grass-6237.mp3"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void ensureFontLoaded() {
        if (font == null) {
            try {
                game.manager.finishLoading();
                Skin skin = game.manager.get("uiskin.json", Skin.class);
                font = skin.getFont("default");
            } catch (Exception e) {
                // Font will be loaded next frame
            }
        }
    }

    @Override
    public void render(float delta) {
        update(delta);
        draw();
    }

    private void update(float delta) {
        // Iso-remapped controls: W/S = northeast/southwest, A/D = northwest/southeast
        float dx = 0, dy = 0;
        if (moveUp)    { dx += PLAYER_SPEED; dy += PLAYER_SPEED; }
        if (moveDown)  { dx -= PLAYER_SPEED; dy -= PLAYER_SPEED; }
        if (moveLeft)  { dx -= PLAYER_SPEED; dy += PLAYER_SPEED; }
        if (moveRight) { dx += PLAYER_SPEED; dy -= PLAYER_SPEED; }

        // Normalize diagonal movement
        if (dx != 0 && dy != 0) {
            float length = (float) Math.sqrt(dx * dx + dy * dy);
            dx = (dx / length) * PLAYER_SPEED;
            dy = (dy / length) * PLAYER_SPEED;
        }

        movePlayerAxis(dx * delta, 0);
        movePlayerAxis(0, dy * delta);

        // Update tracking enemies and check for auto-battle
        for (EnemyNPC enemy : worldMap.getEnemies()) {
            if (!enemy.alive) continue;
            float distance = enemy.moveToward(playerX, playerY, delta);
            // Auto-battle if tracking enemy gets close enough
            if (enemy.tracking && distance < TRACKING_AUTO_BATTLE_RANGE) {
                game.setScreen(new PearPudding(game, game.manager, this, enemy));
                return;
            }
        }

        // Jump physics
        if (isJumping) {
            velocityZ += GRAVITY * delta;
            playerZ += velocityZ * delta;
            float surfaceBelow = worldMap.isWall(playerX, playerY) ? WALL_HEIGHT : 0f;
            if (playerZ <= surfaceBelow) {
                playerZ = surfaceBelow;
                groundZ = surfaceBelow;
                velocityZ = 0;
                isJumping = false;
            }
        }

        // Footstep sound
        if (!isJumping && (moveUp || moveDown || moveLeft || moveRight) && footstepSound != null) {
            footstepTimer -= delta;
            if (footstepTimer <= 0) {
                footstepSound.play(0.5f);
                footstepTimer = FOOTSTEP_INTERVAL;
            }
        } else {
            if (footstepSound != null) footstepSound.stop();
            footstepTimer = 0;
        }

        // Camera tracks player in iso space
        float targetCamX = toIsoX(playerX, playerY);
        float targetCamY = toIsoY(playerX, playerY);

        targetCamX = MathUtils.clamp(targetCamX,
            -(WorldMap.MAP_HEIGHT - 1) * ISO_HALF_W + camera.viewportWidth  / 2,
             (WorldMap.MAP_WIDTH  - 1) * ISO_HALF_W - camera.viewportWidth  / 2);
        targetCamY = MathUtils.clamp(targetCamY,
            camera.viewportHeight / 2,
            (WorldMap.MAP_WIDTH + WorldMap.MAP_HEIGHT - 2) * ISO_HALF_H - camera.viewportHeight / 2);

        camera.position.x += (targetCamX - camera.position.x) * CAMERA_LERP * delta;
        camera.position.y += (targetCamY - camera.position.y) * CAMERA_LERP * delta;
        camera.update();
    }

    private void handleJump() {
        if (!isJumping) {
            velocityZ = JUMP_VELOCITY;
            isJumping = true;
        }
    }

    private void movePlayerAxis(float dx, float dy) {
        if (isJumping) {
            playerX += dx;
            playerY += dy;
            return;
        }

        // Check ground height under player
        byte wallHeightBelow = worldMap.getWallHeight(playerX, playerY);
        float surfaceHeight = wallHeightBelow > 0 ? wallHeightBelow * WALL_FACE_H : 0f;

        if (groundZ >= surfaceHeight + WALL_FACE_H) {
            // Player is in the air on a high platform, allow movement and fall
            playerX += dx;
            playerY += dy;
            if (worldMap.getWallHeight(playerX, playerY) <= groundZ / WALL_FACE_H) {
                isJumping = true;
                velocityZ = 0;
            }
            return;
        }

        // Test movement at four corners
        float[] testPoints = {
            playerX - PLAYER_RADIUS, playerY - PLAYER_RADIUS,
            playerX + PLAYER_RADIUS, playerY - PLAYER_RADIUS,
            playerX - PLAYER_RADIUS, playerY + PLAYER_RADIUS,
            playerX + PLAYER_RADIUS, playerY + PLAYER_RADIUS
        };

        for (int i = 0; i < testPoints.length; i += 2) {
            float newX = testPoints[i] + dx;
            float newY = testPoints[i + 1] + dy;
            if (worldMap.isWall(newX, newY)) return;
        }

        // Update player ground height based on new position
        playerX += dx;
        playerY += dy;
        byte newWallHeight = worldMap.getWallHeight(playerX, playerY);
        groundZ = newWallHeight > 0 ? newWallHeight * WALL_FACE_H : 0f;
    }

    private void draw() {
        Gdx.gl.glClearColor(0.12f, 0.18f, 0.12f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        ensureTexturesLoaded();

        // --- Tile rendering: back-to-front diagonal order ---
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

                // Viewport cull (with margin for wall faces)
                if (sx + ISO_TW < camera.position.x - 950) continue;
                if (sx         > camera.position.x + 950) continue;
                if (sy + ISO_TH + WALL_FACE_H < camera.position.y - 550) continue;
                if (sy         > camera.position.y + 550) continue;

                byte tile = worldMap.getTile(tx, ty);
                int hash  = ((tx * 73856093) ^ (ty * 19349663)) & 0x7FFFFFFF;

                if (tile == 0 && grassTextures != null) {
                    spriteBatch.setColor(1, 1, 1, 1);
                    spriteBatch.draw(grassTextures[hash % 16], sx, sy, ISO_TW, ISO_TH);
                } else if (tile != 0 && brickWallTop != null) {
                    // Isometric brick wall - stack by height
                    byte wallHeight = worldMap.getHeight(tx, ty);

                    // Draw each level of the wall
                    for (int level = 0; level < wallHeight; level++) {
                        float levelOffsetY = level * WALL_FACE_H;

                        // Left side face - extends from left vertex downward
                        float leftX = sx;
                        float leftY = sy + ISO_HALF_H + levelOffsetY;
                        spriteBatch.setColor(0.7f, 0.7f, 0.7f, 1);
                        spriteBatch.draw(brickWallLeft, leftX, leftY, ISO_HALF_W, WALL_FACE_H);

                        // Right side face - extends from right vertex downward
                        float rightX = sx + ISO_HALF_W;
                        float rightY = sy + ISO_HALF_H + levelOffsetY;
                        spriteBatch.setColor(0.65f, 0.65f, 0.65f, 1);
                        spriteBatch.draw(brickWallRight, rightX, rightY, ISO_HALF_W, WALL_FACE_H);

                        // Front face - below the diamond (or below previous level)
                        spriteBatch.setColor(0.75f, 0.75f, 0.75f, 1);
                        spriteBatch.draw(brickWallFront, sx, sy + ISO_TH + levelOffsetY, ISO_TW, WALL_FACE_H);
                    }

                    // Top face (drawn last so it's on top, positioned at final height)
                    float topFaceY = sy + (wallHeight * WALL_FACE_H);
                    spriteBatch.setColor(1, 1, 1, 1);
                    spriteBatch.draw(brickWallTop, sx, topFaceY, ISO_TW, ISO_TH);
                }
            }
        }

        spriteBatch.end();

        // --- Entity rendering: Y-sorted so closer entities overdraw farther ones ---
        EnemyNPC nearby = worldMap.findNearbyEnemy(playerX, playerY, INTERACT_RANGE);
        Chest nearbyChest = worldMap.findNearbChest(playerX, playerY, INTERACT_RANGE);

        List<float[]> entities = new ArrayList<>();
        entities.add(new float[]{playerX, playerY, 0}); // 0 = player
        for (EnemyNPC e : worldMap.getEnemies()) {
            if (e.alive) entities.add(new float[]{e.x, e.y, 1}); // 1 = enemy
        }
        for (Chest c : worldMap.getChests()) {
            if (!c.opened) entities.add(new float[]{c.x, c.y, 2}); // 2 = chest
        }
        // Sort descending by isoY: furthest (highest isoY) drawn first
        entities.sort((a, b) -> Float.compare(toIsoY(b[0], b[1]), toIsoY(a[0], a[1])));

        // Draw sprites with SpriteBatch (reuse existing batch)
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();

        for (float[] ent : entities) {
            float isoX = toIsoX(ent[0], ent[1]);
            float isoY = toIsoY(ent[0], ent[1]);
            int entityType = (int) ent[2];

            if (entityType == 0) {
                // Player sprite (32×48)
                float visualOffsetY = playerZ * 0.5f;
                if (playerTexture != null) {
                    spriteBatch.setColor(1, 1, 1, 1);
                    spriteBatch.draw(playerTexture, isoX - 16, isoY - 24 + visualOffsetY, 32, 48);
                }
            } else if (entityType == 1) {
                // Enemy sprite (32×48)
                if (enemyTexture != null) {
                    spriteBatch.setColor(1, 1, 1, 1);
                    spriteBatch.draw(enemyTexture, isoX - 16, isoY - 24, 32, 48);
                }
            } else if (entityType == 2) {
                // Chest sprite (48×40)
                if (chestTexture != null) {
                    spriteBatch.setColor(1, 1, 1, 1);
                    spriteBatch.draw(chestTexture, isoX - 24, isoY - 20, 48, 40);
                }
            }
        }

        spriteBatch.end();

        // Draw shadows and interaction prompts with ShapeRenderer
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Player shadow when jumping (isometric ellipse on ground plane)
        if (isJumping) {
            float isoX = toIsoX(playerX, playerY);
            float isoY = toIsoY(playerX, playerY);
            float surfaceBelow = worldMap.isWall(playerX, playerY) ? WALL_HEIGHT : 0f;
            float heightAbove = playerZ - surfaceBelow;
            float shadowAlpha = Math.max(0.2f, 1f - heightAbove / 60f);

            // Shadow scales with height (fades as player jumps higher)
            float shadowScale = Math.max(0.3f, 1f - heightAbove / 80f);

            // In isometric space: width is 2x height for proper perspective
            float shadowWidth = PLAYER_RADIUS * shadowScale * 2f;   // horizontal (x-axis in iso)
            float shadowHeight = PLAYER_RADIUS * shadowScale * 0.8f; // vertical (y-axis in iso)

            // Position on ground plane
            float shadowIsoY = isoY - PLAYER_RADIUS * 0.5f;

            shapeRenderer.setColor(0.05f, 0.1f, 0.05f, shadowAlpha);
            // Draw shadow as a wider ellipse to match isometric perspective
            shapeRenderer.ellipse(isoX - shadowWidth, shadowIsoY - shadowHeight, shadowWidth * 2, shadowHeight * 2);
        }

        // [E] popup box above nearby entity
        if (nearby != null) {
            float ex = toIsoX(nearby.x, nearby.y);
            float ey = toIsoY(nearby.x, nearby.y) + 12f; // Above enemy sprite
            shapeRenderer.setColor(0.1f, 0.1f, 0.1f, 1);
            shapeRenderer.rect(ex - 16, ey - 1, 32, 20);
            shapeRenderer.setColor(1, 1, 0.8f, 1);
            shapeRenderer.rect(ex - 15, ey, 30, 18);
        } else if (nearbyChest != null) {
            float cx = toIsoX(nearbyChest.x, nearbyChest.y);
            float cy = toIsoY(nearbyChest.x, nearbyChest.y) + 12f; // Above chest sprite
            shapeRenderer.setColor(0.1f, 0.1f, 0.1f, 1);
            shapeRenderer.rect(cx - 16, cy - 1, 32, 20);
            shapeRenderer.setColor(1, 1, 0.8f, 1);
            shapeRenderer.rect(cx - 15, cy, 30, 18);
        }

        shapeRenderer.end();

        spriteBatch.dispose();

        // --- HUD (screen-space) ---
        ensureFontLoaded();
        if (font != null) {
            hudBatch.setProjectionMatrix(new OrthographicCamera(1800, 1000).combined);
            hudBatch.begin();

            font.draw(hudBatch, "WASD/Arrows: move   Space: jump   E: battle", 20, 980);

            int aliveCount = 0;
            for (EnemyNPC e : worldMap.getEnemies()) {
                if (e.alive) aliveCount++;
            }
            font.draw(hudBatch, "Enemies remaining: " + aliveCount, 20, 920);

            // [E] label projected to HUD space
            if (nearby != null) {
                float isoEx = toIsoX(nearby.x, nearby.y);
                float isoEy = toIsoY(nearby.x, nearby.y) + 30f; // Above enemy sprite
                float hudX = isoEx - camera.position.x + 900f;
                float hudY = isoEy - camera.position.y + 500f;
                font.setColor(0.1f, 0.1f, 0.1f, 1);
                font.draw(hudBatch, "[E]", hudX - 13f, hudY);
                font.setColor(1, 1, 1, 1);
            } else if (nearbyChest != null) {
                float isoCx = toIsoX(nearbyChest.x, nearbyChest.y);
                float isoCy = toIsoY(nearbyChest.x, nearbyChest.y) + 30f; // Above chest sprite
                float hudX = isoCx - camera.position.x + 900f;
                float hudY = isoCy - camera.position.y + 500f;
                font.setColor(0.1f, 0.1f, 0.1f, 1);
                font.draw(hudBatch, "[E]", hudX - 13f, hudY);
                font.setColor(1, 1, 1, 1);
            }

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
                handleInteract();
                return true;
            case Input.Keys.SPACE:
                handleJump();
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
        Chest chest = worldMap.findNearbChest(playerX, playerY, INTERACT_RANGE);
        if (chest != null) {
            game.setScreen(new ChestOpenScreen(game, (Screen) this, chest));
            return;
        }

        EnemyNPC enemy = worldMap.findNearbyEnemy(playerX, playerY, INTERACT_RANGE);
        if (enemy != null) {
            game.setScreen(new PearPudding(game, game.manager, this, enemy));
        }
    }

    public void removeChest(Chest chest) {
        worldMap.removeChest(chest);
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
    public boolean keyTyped(char character) { return false; }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) { return false; }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }

    @Override
    public boolean mouseMoved(int screenX, int screenY) { return false; }

    @Override
    public boolean scrolled(float amountX, float amountY) { return false; }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) { return false; }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        camera.position.set(toIsoX(playerX, playerY), toIsoY(playerX, playerY), 0);
        camera.update();
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {
        // Reset movement flags when leaving screen to prevent stuck movement
        moveUp = false;
        moveDown = false;
        moveLeft = false;
        moveRight = false;
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        hudBatch.dispose();
        if (grassTextures != null) {
            for (Texture texture : grassTextures) {
                if (texture != null) texture.dispose();
            }
        }
        if (wallTextures != null) {
            for (Texture texture : wallTextures) {
                if (texture != null) texture.dispose();
            }
        }
        if (brickWallTop != null) brickWallTop.dispose();
        if (brickWallFront != null) brickWallFront.dispose();
        if (brickWallLeft != null) brickWallLeft.dispose();
        if (brickWallRight != null) brickWallRight.dispose();
        if (playerTexture != null) playerTexture.dispose();
        if (enemyTexture != null) enemyTexture.dispose();
        if (chestTexture != null) chestTexture.dispose();
        if (footstepSound != null) {
            footstepSound.dispose();
        }
    }
}
