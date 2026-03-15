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
import com.pear.pudding.input.InputController;
import com.pear.pudding.world.Chest;
import com.pear.pudding.world.Door;
import com.pear.pudding.world.EnemyNPC;
import com.pear.pudding.world.WorldMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.pear.pudding.game.data.GameConstants.WINDOW_HEIGHT;
import static com.pear.pudding.game.data.GameConstants.WINDOW_WIDTH;

public class WorldScreen implements Screen, InputProcessor {
    private final MyGame game;
    private final WorldMap worldMap;
    private final WorldScreen parentScreen;
    private final String mapName;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final SpriteBatch hudBatch;
    private SpriteBatch worldBatch;
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
    private Sound footstepSound;

    // Pooled collections for rendering (reused every frame to avoid allocations)
    private final List<float[]> entitiesPool = new ArrayList<>();

    // Input controller (centralized rebindable controls)
    private final InputController inputController = new InputController();

    private float playerX = 50 * WorldMap.TILE_SIZE;
    private float playerY = 15 * WorldMap.TILE_SIZE;

    private static final float PLAYER_SPEED = 180f;
    private static final float PLAYER_RADIUS = 12f;
    private static final float INTERACT_RANGE = 48f;
    private static final float TRACKING_AUTO_BATTLE_RANGE = 32f;  // Auto-battle when tracking enemy gets this close
    private static final float CAMERA_LERP = 5f;
    private static final float FOOTSTEP_INTERVAL = 1.0f;
    private static final float JUMP_VELOCITY = 220f;  // Jump high enough to clear 2-height walls
    private static final float GRAVITY = -600f;
    private static final float WALL_HEIGHT = 24f;
    private static final int CLIMBABLE_HEIGHT = 0;  // Must jump to get on any wall

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

    private float footstepTimer = 0f;

    // Trail system
    private final HashMap<Long, Float> trailTiles = new HashMap<>();  // packed tile coord → intensity [0,1]
    private long lastPlayerTile = -1L;

    // HUD camera (reused every frame to avoid allocation)
    private OrthographicCamera hudCamera;

    // Constants
    private static final float TRAIL_FADE_RATE = 0.22f;  // fades in ~4.5 seconds
    private static final float TRAIL_MAX_DARKEN = 0.15f; // darkens to ~85% brightness at peak (subtle)
    private static final float TRAIL_INITIAL_INTENSITY = 0.6f; // reduced from 1.0

    // Primary constructor with map name and parent screen
    public WorldScreen(MyGame game, String mapName, WorldScreen parentScreen) {
        this.game = game;
        this.mapName = mapName;
        this.parentScreen = parentScreen;
        this.worldMap = new WorldMap(mapName);

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
        worldBatch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = null;
    }

    // Back-compat constructor: main entry from menu
    public WorldScreen(MyGame game) {
        this(game, "main", null);
    }

    // --- Iso coordinate helpers ---

    private float toIsoX(float worldX, float worldY) {
        return (worldX / WorldMap.TILE_SIZE - worldY / WorldMap.TILE_SIZE) * ISO_HALF_W;
    }

    private float toIsoY(float worldX, float worldY) {
        return (worldX / WorldMap.TILE_SIZE + worldY / WorldMap.TILE_SIZE) * ISO_HALF_H;
    }

    // --- Trail system helpers ---

    private static long packTile(int tx, int ty) {
        return ((long) tx << 32) | (ty & 0xFFFFFFFFL);
    }

    private float getTrailDarkening(int tx, int ty) {
        float d = trailTiles.getOrDefault(packTile(tx, ty), 0f);
        // Minimal blending: only slight softness at edges
        // Cardinal neighbors (tenth weight - very subtle)
        d += trailTiles.getOrDefault(packTile(tx+1, ty), 0f) * 0.1f;
        d += trailTiles.getOrDefault(packTile(tx-1, ty), 0f) * 0.1f;
        d += trailTiles.getOrDefault(packTile(tx, ty+1), 0f) * 0.1f;
        d += trailTiles.getOrDefault(packTile(tx, ty-1), 0f) * 0.1f;
        // Diagonal neighbors not included - trail stays tight
        return Math.min(d, 1f);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(this);
        // Ensure world assets are queued for loading and fully loaded
        AssetLoadingScreen.loadWorldAssets(game.manager);
        // Process asset loading until world assets are ready
        while (!game.manager.update() && !game.manager.contains("grass0.png", Texture.class)) {
            // Keep loading until grass0.png is available
        }
        loadTextures();
        hudCamera = new OrthographicCamera(1800, 1000);
        hudCamera.position.set(900, 500, 0);
        hudCamera.update();
    }

    private void loadTextures() {
        if (grassTextures == null) {
            try {
                grassTextures = new Texture[16];
                for (int i = 0; i < 16; i++) {
                    grassTextures[i] = game.manager.get("grass" + i + ".png", Texture.class);
                    grassTextures[i].setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                }
                wallTextures = new Texture[8];
                for (int i = 0; i < 8; i++) {
                    wallTextures[i] = game.manager.get("wall" + i + ".png", Texture.class);
                    wallTextures[i].setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                }
                brickWallTop = game.manager.get("wallbrick_top.png", Texture.class);
                brickWallTop.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                brickWallFront = game.manager.get("wallbrick_front.png", Texture.class);
                brickWallFront.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                brickWallLeft = game.manager.get("wallbrick_left.png", Texture.class);
                brickWallLeft.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                brickWallRight = game.manager.get("wallbrick_right.png", Texture.class);
                brickWallRight.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                playerTexture = game.manager.get("player.png", Texture.class);
                playerTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                enemyTexture = game.manager.get("enemy.png", Texture.class);
                enemyTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                chestTexture = game.manager.get("chest.png", Texture.class);
                chestTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                footstepSound = game.manager.get("freesound_community-running-in-grass-6237.mp3", Sound.class);
            } catch (Exception e) {
                // Assets not ready yet, will retry next frame
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
        if (inputController.moveUp)    { dx += PLAYER_SPEED; dy += PLAYER_SPEED; }
        if (inputController.moveDown)  { dx -= PLAYER_SPEED; dy -= PLAYER_SPEED; }
        if (inputController.moveLeft)  { dx -= PLAYER_SPEED; dy += PLAYER_SPEED; }
        if (inputController.moveRight) { dx += PLAYER_SPEED; dy -= PLAYER_SPEED; }

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
        if (!isJumping && (inputController.moveUp || inputController.moveDown || inputController.moveLeft || inputController.moveRight) && footstepSound != null) {
            footstepTimer -= delta;
            if (footstepTimer <= 0) {
                footstepSound.play(0.5f);
                footstepTimer = FOOTSTEP_INTERVAL;
            }
        } else {
            if (footstepSound != null) footstepSound.stop();
            footstepTimer = 0;
        }

        // Trail: mark tile we just LEFT (not current tile)
        int curTileX = (int)(playerX / WorldMap.TILE_SIZE);
        int curTileY = (int)(playerY / WorldMap.TILE_SIZE) + 1;
        long curKey = packTile(curTileX, curTileY);
        if (curKey != lastPlayerTile) {
            // Add the tile we just LEFT to the trail
            if (lastPlayerTile != -1L) {
                trailTiles.put(lastPlayerTile, TRAIL_INITIAL_INTENSITY);
            }
            lastPlayerTile = curKey;
        }

        // Trail: decay all tiles
        Iterator<Map.Entry<Long, Float>> it = trailTiles.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Long, Float> e = it.next();
            float intensity = e.getValue() - delta * TRAIL_FADE_RATE;
            if (intensity <= 0f) it.remove();
            else e.setValue(intensity);
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

        float newPlayerX = playerX + dx;
        float newPlayerY = playerY + dy;

        int playerTileX = (int) (playerX / WorldMap.TILE_SIZE);
        int playerTileY = (int) (playerY / WorldMap.TILE_SIZE);
        int playerDepth = playerTileX + playerTileY;

        // Test only the leading face in the direction of movement.
        // Diagonal 8-point circles extend perpendicular to movement and cause false
        // collisions when sliding along wall edges (the NE/SE points hit the wall tile
        // even though you're moving parallel to it).
        // Use 3 collinear points along the forward face with a small safe spread (4px)
        // that won't cross a tile boundary (TILE_SIZE=32, spread<<16).
        final float SPREAD = 4f;
        float[] testXs, testYs;
        if (dx != 0) {
            float fwd = Math.signum(dx) * PLAYER_RADIUS;
            testXs = new float[]{ newPlayerX + fwd, newPlayerX + fwd, newPlayerX + fwd };
            testYs = new float[]{ newPlayerY,        newPlayerY + SPREAD, newPlayerY - SPREAD };
        } else {
            float fwd = Math.signum(dy) * PLAYER_RADIUS;
            testXs = new float[]{ newPlayerX,        newPlayerX + SPREAD, newPlayerX - SPREAD };
            testYs = new float[]{ newPlayerY + fwd,  newPlayerY + fwd,   newPlayerY + fwd   };
        }

        for (int i = 0; i < 3; i++) {
            int testTileX = (int) (testXs[i] / WorldMap.TILE_SIZE);
            int testTileY = (int) (testYs[i] / WorldMap.TILE_SIZE);
            int testDepth = testTileX + testTileY;
            byte wallHeight = worldMap.getWallHeight(testXs[i], testYs[i]);
            if (wallHeight > CLIMBABLE_HEIGHT && testDepth > playerDepth && groundZ < wallHeight * WALL_FACE_H) {
                return;
            }
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

        // --- Tile rendering: back-to-front diagonal order ---
        worldBatch.setProjectionMatrix(camera.combined);
        worldBatch.begin();

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
                    float trailDark = getTrailDarkening(tx, ty);
                    float c = 1f - trailDark * TRAIL_MAX_DARKEN;
                    worldBatch.setColor(c, c, c, 1f);
                    worldBatch.draw(grassTextures[hash % 16], sx, sy, ISO_TW, ISO_TH);
                    worldBatch.setColor(1, 1, 1, 1);  // reset for walls
                } else if (tile != 0 && brickWallTop != null) {
                    // Isometric brick wall - stack by height
                    byte wallHeight = worldMap.getHeight(tx, ty);

                    // Check adjacent walls to determine which faces are exposed
                    byte leftWallHeight = worldMap.getHeight(tx - 1, ty);
                    byte rightWallHeight = worldMap.getHeight(tx + 1, ty);

                    // Draw each level of the wall (only visible side faces)
                    for (int level = 0; level < wallHeight; level++) {
                        float levelOffsetY = level * WALL_FACE_H;

                        // Left side face - only if no adjacent wall at same height
                        if (leftWallHeight < wallHeight) {
                            float leftX = sx;
                            float leftY = sy + ISO_HALF_H + levelOffsetY;
                            worldBatch.setColor(0.7f, 0.7f, 0.7f, 1);
                            worldBatch.draw(brickWallLeft, leftX, leftY, ISO_HALF_W, WALL_FACE_H);
                        }

                        // Right side face - only if no adjacent wall at same height
                        if (rightWallHeight < wallHeight) {
                            float rightX = sx + ISO_HALF_W;
                            float rightY = sy + ISO_HALF_H + levelOffsetY;
                            worldBatch.setColor(0.65f, 0.65f, 0.65f, 1);
                            worldBatch.draw(brickWallRight, rightX, rightY, ISO_HALF_W, WALL_FACE_H);
                        }
                    }

                    // Top face (drawn last so it's on top, positioned at final height)
                    float topFaceY = sy + (wallHeight * WALL_FACE_H);
                    worldBatch.setColor(1, 1, 1, 1);
                    worldBatch.draw(brickWallTop, sx, topFaceY, ISO_TW, ISO_TH);
                }
            }
        }

        worldBatch.end();

        // --- Entity rendering: Y-sorted so closer entities overdraw farther ones ---
        EnemyNPC nearby = worldMap.findNearbyEnemy(playerX, playerY, INTERACT_RANGE);
        Chest nearbyChest = worldMap.findNearbChest(playerX, playerY, INTERACT_RANGE);
        Door nearbyDoor = worldMap.findNearbyDoor(playerX, playerY, INTERACT_RANGE);

        // Reuse pooled collection to avoid per-frame allocation
        entitiesPool.clear();
        entitiesPool.add(new float[]{playerX, playerY, 0}); // 0 = player
        for (EnemyNPC e : worldMap.getEnemies()) {
            if (e.alive) entitiesPool.add(new float[]{e.x, e.y, 1}); // 1 = enemy
        }
        for (Chest c : worldMap.getChests()) {
            if (!c.opened) entitiesPool.add(new float[]{c.x, c.y, 2}); // 2 = chest
        }
        for (Door d : worldMap.getDoors()) {
            entitiesPool.add(new float[]{d.x, d.y, 3}); // 3 = door
        }
        // Sort descending by isoY: furthest (highest isoY) drawn first
        entitiesPool.sort((a, b) -> Float.compare(toIsoY(b[0], b[1]), toIsoY(a[0], a[1])));

        // Draw sprites with worldBatch
        worldBatch.setProjectionMatrix(camera.combined);
        worldBatch.begin();

        for (float[] ent : entitiesPool) {
            float isoX = toIsoX(ent[0], ent[1]);
            float isoY = toIsoY(ent[0], ent[1]);
            int entityType = (int) ent[2];

            if (entityType == 0) {
                // Player sprite (32×48)
                // In isometric space with fixed camera, vertical jump goes straight up
                float visualOffsetY = playerZ * 0.5f;
                if (playerTexture != null) {
                    worldBatch.setColor(1, 1, 1, 1);
                    worldBatch.draw(playerTexture, isoX - 16, isoY - 24 + visualOffsetY, 32, 48);
                }
            } else if (entityType == 1) {
                // Enemy sprite (32×48)
                if (enemyTexture != null) {
                    worldBatch.setColor(1, 1, 1, 1);
                    worldBatch.draw(enemyTexture, isoX - 16, isoY - 24, 32, 48);
                }
            } else if (entityType == 2) {
                // Chest sprite (48×40)
                if (chestTexture != null) {
                    worldBatch.setColor(1, 1, 1, 1);
                    worldBatch.draw(chestTexture, isoX - 24, isoY - 20, 48, 40);
                }
            }
            // Door rendering (type 3) done in ShapeRenderer section below
        }

        worldBatch.end();

        // Door rendering with ShapeRenderer
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (Door d : worldMap.getDoors()) {
            float isoX = toIsoX(d.x, d.y);
            float isoY = toIsoY(d.x, d.y);
            // Draw door as a brown rectangle (base)
            shapeRenderer.setColor(0.6f, 0.4f, 0.2f, 1); // Brown
            shapeRenderer.rect(isoX - 16, isoY - 12, 32, 24);
            // Draw darker outline (arch)
            shapeRenderer.setColor(0.4f, 0.2f, 0.05f, 1); // Dark brown
            shapeRenderer.rect(isoX - 18, isoY - 14, 36, 28);
        }
        shapeRenderer.end();

        // Draw shadows and interaction prompts with ShapeRenderer
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Player shadow when jumping (isometric ellipse on ground plane)
        if (isJumping) {
            float isoX = toIsoX(playerX, playerY);
            float isoY = toIsoY(playerX, playerY);
            float surfaceBelow = worldMap.isWall(playerX, playerY) ? WALL_HEIGHT : 0f;
            float heightAbove = playerZ - surfaceBelow;
            // Shadow fades quickly at height (much dimmer when airborne)
            float shadowAlpha = Math.max(0.05f, 1f - heightAbove / 30f);

            // Shadow shrinks with height (very small at apex)
            float shadowScale = Math.max(0.1f, 1f - heightAbove / 40f);

            // Small shadow ellipse: width stretched for iso, height compressed
            float shadowWidth = 20f * shadowScale;    // ~20-7px wide at ground level
            float shadowHeight = 8f * shadowScale;    // ~8-3px tall at ground level

            // Position shadow on ground plane, directly below player
            // Shadow stays fixed on ground (jump doesn't move X/Y)
            float shadowIsoX = isoX - shadowWidth;     // Centered horizontally
            float shadowIsoY = isoY - 12f;             // Fixed on ground plane

            shapeRenderer.setColor(0.05f, 0.1f, 0.05f, shadowAlpha);
            // Draw shadow as a small ellipse with proper iso proportions
            shapeRenderer.ellipse(shadowIsoX, shadowIsoY, shadowWidth * 2, shadowHeight * 2);
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
        } else if (nearbyDoor != null) {
            float dx = toIsoX(nearbyDoor.x, nearbyDoor.y);
            float dy = toIsoY(nearbyDoor.x, nearbyDoor.y) + 12f; // Above door
            shapeRenderer.setColor(0.1f, 0.1f, 0.1f, 1);
            shapeRenderer.rect(dx - 16, dy - 1, 32, 20);
            shapeRenderer.setColor(1, 1, 0.8f, 1);
            shapeRenderer.rect(dx - 15, dy, 30, 18);
        }

        shapeRenderer.end();

        // --- HUD (screen-space) ---
        ensureFontLoaded();
        if (font != null) {
            hudBatch.setProjectionMatrix(hudCamera.combined);
            hudBatch.begin();

            font.draw(hudBatch, "WASD/Arrows: move   Space: jump   E: battle", 20, 980);

            int aliveCount = 0;
            for (EnemyNPC e : worldMap.getEnemies()) {
                if (e.alive) aliveCount++;
            }
            font.draw(hudBatch, "Enemies remaining: " + aliveCount, 20, 920);

            int closedChests = 0;
            for (Chest c : worldMap.getChests()) {
                if (!c.opened) closedChests++;
            }
            font.draw(hudBatch, "Chests remaining: " + closedChests, 20, 860);

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
            } else if (nearbyDoor != null) {
                float isoDx = toIsoX(nearbyDoor.x, nearbyDoor.y);
                float isoDy = toIsoY(nearbyDoor.x, nearbyDoor.y) + 30f; // Above door
                float hudX = isoDx - camera.position.x + 900f;
                float hudY = isoDy - camera.position.y + 500f;
                font.setColor(0.1f, 0.1f, 0.1f, 1);
                font.draw(hudBatch, "[E]", hudX - 13f, hudY);
                font.setColor(1, 1, 1, 1);
            }

            hudBatch.end();
        }
    }

    @Override
    public boolean keyDown(int keycode) {
        // Handle interact and jump immediately
        if (inputController.isInteractPressed(keycode)) {
            handleInteract();
            return true;
        }
        if (inputController.isJumpPressed(keycode)) {
            handleJump();
            return true;
        }

        // Handle movement via controller
        inputController.handleKeyDown(keycode);
        return inputController.isMovementKey(keycode);
    }

    @Override
    public boolean keyUp(int keycode) {
        // Handle movement via controller
        inputController.handleKeyUp(keycode);
        return inputController.isMovementKey(keycode);
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
            return;
        }

        Door door = worldMap.findNearbyDoor(playerX, playerY, INTERACT_RANGE);
        if (door != null) {
            if (door.targetMapName == null || door.targetMapName.isEmpty()) {
                // Exit sub-area → return to parent
                if (parentScreen != null) {
                    game.setScreen(parentScreen);
                }
            } else {
                // Enter new map as a sub-area
                game.setScreen(new WorldScreen(game, door.targetMapName, this));
            }
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
        // Reset input when leaving screen to prevent stuck movement
        inputController.reset();
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        hudBatch.dispose();
        worldBatch.dispose();
        // Textures and sounds are managed by AssetManager, not disposed here
    }
}
