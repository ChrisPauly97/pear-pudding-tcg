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
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.pear.pudding.MyGame;
import com.pear.pudding.world.Chest;
import com.pear.pudding.world.Door;
import com.pear.pudding.world.EnemyNPC;
import com.pear.pudding.world.WorldMap;

import static com.pear.pudding.game.data.GameConstants.WINDOW_HEIGHT;
import static com.pear.pudding.game.data.GameConstants.WINDOW_WIDTH;

public class MapEditorScreen implements Screen, InputProcessor {
    enum Brush { GRASS, WALL, ENEMY, CHEST, DOOR, PLAYER_SPAWN, RESET }

    private final MyGame game;
    private WorldMap worldMap;
    private String currentMapName = "main";
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final SpriteBatch hudBatch;
    private SpriteBatch worldBatch;
    private final ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private OrthographicCamera hudCam;
    private Texture[] grassTextures;
    private Texture[] wallTextures;
    private Texture brickWallTop;
    private Texture brickWallFront;
    private Texture brickWallLeft;
    private Texture brickWallRight;
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
    private boolean texturesLoaded = false;
    private boolean assetsFullyLoaded = false;

    // Map list overlay
    private boolean showMapListOverlay = false;
    private java.util.List<String> mapNames = new java.util.ArrayList<>();
    private int mapListHoverIndex = -1;

    // Deferred dialog creation (to avoid input event conflicts)
    private String pendingMapNameDialog = null;
    private float pendingDoorX = 0, pendingDoorY = 0;
    private boolean pendingDoorDialog = false;

    private static final float CAMERA_SPEED = 400f;
    private static final float TOOLBAR_HEIGHT = 40;

    // Scene2D toolbar UI
    private Stage toolbarStage;
    private TextButton[] brushButtons;

    // Hover tile (tile coords; -1 = none)
    private int hoverTileX = -1;
    private int hoverTileY = -1;

    // Raw screen mouse position
    private int lastMouseX = -1;
    private int lastMouseY = -1;

    // Wall height editing
    private static final int MAX_WALL_HEIGHT = 5;

    // Track last painted tile to prevent double-painting during drag
    private int lastPaintedTileX = -1;
    private int lastPaintedTileY = -1;

    // Pooled Vector3 to avoid per-frame allocations
    private final com.badlogic.gdx.math.Vector3 screenVec = new com.badlogic.gdx.math.Vector3();

    public MapEditorScreen(MyGame game) {
        this.game = game;
        this.currentMapName = "main";
        loadMap(currentMapName);

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
        worldBatch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        hudCam = new OrthographicCamera();
        hudCam.setToOrtho(false, WINDOW_WIDTH, WINDOW_HEIGHT);
        hudCam.update();
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

    private void loadMap(String mapName) {
        this.currentMapName = mapName;
        this.worldMap = new WorldMap(mapName);
    }

    @Override
    public void show() {
        mapNames = WorldMap.listMapNames();

        // Create Scene2D toolbar stage
        toolbarStage = new Stage(new FitViewport(WINDOW_WIDTH, WINDOW_HEIGHT));
        Skin skin = game.manager.get("uiskin.json", Skin.class);

        Table toolbar = new Table(skin);
        toolbar.setFillParent(true);
        toolbar.top().left().pad(4);

        // Create brush toggle buttons (7 buttons for brushes)
        brushButtons = new TextButton[7];
        Brush[] brushTypes = {Brush.GRASS, Brush.WALL, Brush.ENEMY, Brush.CHEST, Brush.DOOR, Brush.PLAYER_SPAWN, Brush.RESET};
        String[] buttonLabels = {"Grass", "Wall", "Enemy", "Chest", "Door", "Spawn", "Reset"};

        for (int i = 0; i < brushTypes.length; i++) {
            final Brush brush = brushTypes[i];
            brushButtons[i] = new TextButton(buttonLabels[i], skin);
            final int index = i;
            brushButtons[i].addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    currentBrush = brush;
                    updateBrushHighlight();
                }
            });
            toolbar.add(brushButtons[i]).width(60).height(32).padRight(2);
        }

        // Save button
        TextButton saveButton = new TextButton("Save", skin);
        saveButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                worldMap.saveToFile(Gdx.files.local("maps/" + currentMapName + ".txt"));
                saveMessageTime = SAVE_MESSAGE_DURATION;
            }
        });
        toolbar.add(saveButton).width(60).height(32).padRight(2);

        // New button
        TextButton newButton = new TextButton("New", skin);
        newButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showMapNameDialog(skin, "New Map", "Enter map name", "");
            }
        });
        toolbar.add(newButton).width(60).height(32).padRight(2);

        // Load button
        TextButton loadButton = new TextButton("Load", skin);
        loadButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showMapListOverlay = !showMapListOverlay;
                mapListHoverIndex = -1;
            }
        });
        toolbar.add(loadButton).width(60).height(32).padRight(2);

        // Menu button
        TextButton menuButton = new TextButton("Menu", skin);
        menuButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MenuScreen(game));
            }
        });
        toolbar.add(menuButton).width(60).height(32);

        toolbarStage.addActor(toolbar);

        // Set up input multiplexer: UI first, then world
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(toolbarStage);
        multiplexer.addProcessor(this);
        Gdx.input.setInputProcessor(multiplexer);

        updateBrushHighlight();
    }

    private void updateBrushHighlight() {
        Brush[] brushTypes = {Brush.GRASS, Brush.WALL, Brush.ENEMY, Brush.CHEST, Brush.DOOR, Brush.PLAYER_SPAWN, Brush.RESET};
        for (int i = 0; i < brushButtons.length; i++) {
            Brush brush = brushTypes[i];
            if (brush == currentBrush) {
                brushButtons[i].getLabel().setColor(1, 1, 0, 1);  // Yellow for active
            } else {
                brushButtons[i].getLabel().setColor(1, 1, 1, 1);  // White for inactive
            }
        }
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
                enemyTexture = game.manager.get("enemy.png", Texture.class);
                enemyTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                chestTexture = game.manager.get("chest.png", Texture.class);
                chestTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                texturesLoaded = true;
            } catch (Exception e) {
                // Should not happen since assetsFullyLoaded is true
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
        // Process asset loading
        if (!assetsFullyLoaded) {
            assetsFullyLoaded = game.manager.update();  // Returns true when all queued assets are loaded
        }

        // Ensure textures are loaded before rendering
        if (!texturesLoaded && assetsFullyLoaded) {
            loadTextures();
        }
        update(delta);
        draw();
    }

    private void update(float delta) {
        // Handle deferred dialog creation (avoid input event conflicts)
        if (pendingDoorDialog) {
            pendingDoorDialog = false;
            showDoorNameDialog(pendingDoorX, pendingDoorY);
        }

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

        // Suppress hover when over toolbar (reuse pooled vector)
        screenVec.set(screenX, screenY, 0);
        hudCam.unproject(screenVec);
        if (screenVec.y > (WINDOW_HEIGHT - TOOLBAR_HEIGHT)) { hoverTileX = -1; hoverTileY = -1; return; }

        // Unproject through world camera → iso world → tile coords (reuse pooled vector)
        screenVec.set(screenX, screenY, 0);
        camera.unproject(screenVec);
        hoverTileX = (int) Math.floor(isoToTileX(screenVec.x, screenVec.y));
        hoverTileY = (int) Math.floor(isoToTileY(screenVec.x, screenVec.y));
    }

    private void draw() {
        Gdx.gl.glClearColor(0.12f, 0.18f, 0.12f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Skip world rendering if textures aren't loaded yet
        if (!texturesLoaded) {
            return;
        }

        // --- Iso tile rendering: back-to-front ---
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

                // Viewport cull
                if (sx + ISO_TW < camera.position.x - 950) continue;
                if (sx         > camera.position.x + 950) continue;
                if (sy + ISO_TH + WALL_FACE_H < camera.position.y - 550) continue;
                if (sy         > camera.position.y + 550) continue;

                byte tile = worldMap.getTile(tx, ty);
                int hash  = ((tx * 73856093) ^ (ty * 19349663)) & 0x7FFFFFFF;

                if (tile == 0 && grassTextures != null) {
                    worldBatch.setColor(1, 1, 1, 1);
                    worldBatch.draw(grassTextures[hash % 16], sx, sy, ISO_TW, ISO_TH);
                } else if (tile != 0 && brickWallTop != null) {
                    // Render stacked brick walls by height
                    byte wallHeight = worldMap.getHeight(tx, ty);

                    // Check adjacent walls to determine which faces are exposed
                    byte leftWallHeight = worldMap.getHeight(tx - 1, ty);
                    byte rightWallHeight = worldMap.getHeight(tx + 1, ty);
                    byte topWallHeight = worldMap.getHeight(tx, ty + 1);

                    // Draw each level of the wall
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

                        // Front face - only if no adjacent wall at same height
                        if (topWallHeight < wallHeight) {
                            worldBatch.setColor(0.75f, 0.75f, 0.75f, 1);
                            worldBatch.draw(brickWallFront, sx, sy + ISO_TH + levelOffsetY, ISO_TW, WALL_FACE_H);
                        }
                    }

                    // Top face (drawn last, positioned at final height)
                    float topFaceY = sy + (wallHeight * WALL_FACE_H);
                    worldBatch.setColor(1, 1, 1, 1);
                    worldBatch.draw(brickWallTop, sx, topFaceY, ISO_TW, ISO_TH);
                }
            }
        }

        worldBatch.end();

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

        // Enemies (using sprite, with debug fallback)
        if (worldMap.getEnemies().size() > 0) {
            if (enemyTexture != null) {
                worldBatch.setProjectionMatrix(camera.combined);
                worldBatch.begin();
                for (EnemyNPC enemy : worldMap.getEnemies()) {
                    float ix = toIsoX(enemy.x, enemy.y);
                    float iy = toIsoY(enemy.x, enemy.y);
                    worldBatch.setColor(1, 1, 1, 1);
                    worldBatch.draw(enemyTexture, ix - 16, iy - 24, 32, 48);
                }
                worldBatch.end();
            } else {
                // Debug fallback: draw red circles if texture missing
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                for (EnemyNPC enemy : worldMap.getEnemies()) {
                    float ix = toIsoX(enemy.x, enemy.y);
                    float iy = toIsoY(enemy.x, enemy.y);
                    shapeRenderer.setColor(1, 0, 0, 1);
                    shapeRenderer.circle(ix, iy, 16);
                }
                shapeRenderer.end();
            }
        }

        // Chests (using sprite, with debug fallback)
        if (worldMap.getChests().size() > 0) {
            if (chestTexture != null) {
                worldBatch.setProjectionMatrix(camera.combined);
                worldBatch.begin();
                for (Chest chest : worldMap.getChests()) {
                    float ix = toIsoX(chest.x, chest.y);
                    float iy = toIsoY(chest.x, chest.y);
                    worldBatch.setColor(1, 1, 1, 1);
                    worldBatch.draw(chestTexture, ix - 24, iy - 20, 48, 40);
                }
                worldBatch.end();
            } else {
                // Debug fallback: draw yellow rectangles if texture missing
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                for (Chest chest : worldMap.getChests()) {
                    float ix = toIsoX(chest.x, chest.y);
                    float iy = toIsoY(chest.x, chest.y);
                    shapeRenderer.setColor(1, 1, 0, 1);
                    shapeRenderer.rect(ix - 24, iy - 20, 48, 40);
                }
                shapeRenderer.end();
            }
        }

        // Doors (brown arch rectangles)
        if (worldMap.getDoors().size() > 0) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            for (Door door : worldMap.getDoors()) {
                float ix = toIsoX(door.x, door.y);
                float iy = toIsoY(door.x, door.y);
                // Door base
                shapeRenderer.setColor(0.6f, 0.4f, 0.2f, 1); // Brown
                shapeRenderer.rect(ix - 16, iy - 12, 32, 24);
                // Door outline (arch)
                shapeRenderer.setColor(0.4f, 0.2f, 0.05f, 1); // Dark brown
                shapeRenderer.rect(ix - 18, iy - 14, 36, 28);
            }
            shapeRenderer.end();
        }

        Gdx.gl.glDisable(GL20.GL_BLEND);

        // Render Scene2D toolbar stage
        toolbarStage.getViewport().apply();
        toolbarStage.act(Gdx.graphics.getDeltaTime());
        toolbarStage.draw();
        viewport.apply();

        // Draw HUD text (status info at bottom)
        ensureFontLoaded();
        if (font != null) {
            hudBatch.setProjectionMatrix(hudCam.combined);
            hudBatch.begin();
            font.setColor(1, 1, 1, 1);
            font.draw(hudBatch, "Brush: " + currentBrush.name(), WINDOW_WIDTH - 250, 20);
            if (hoverTileX >= 0 && hoverTileY >= 0) {
                font.draw(hudBatch, "[" + hoverTileX + ", " + hoverTileY + "]", WINDOW_WIDTH / 2 - 40f, 20);

                // Show wall height if hovering over wall
                if (worldMap.getTile(hoverTileX, hoverTileY) != 0) {
                    byte height = worldMap.getHeight(hoverTileX, hoverTileY);
                    font.setColor(1, 1, 0, 1);
                    font.draw(hudBatch, "Height: " + height + " (scroll to change)", WINDOW_WIDTH / 2 - 100f, 40);
                    font.setColor(1, 1, 1, 1);
                }
            }
            if (saveMessageTime > 0) {
                font.setColor(0, 1, 0, 1);
                font.draw(hudBatch, "Saved!", WINDOW_WIDTH / 2 - 30, 40);
                font.setColor(1, 1, 1, 1);
            }
            hudBatch.end();
        }

        // Draw map list overlay
        if (showMapListOverlay) {
            drawMapListOverlay();
        }
    }

    private void drawMapListOverlay() {
        // Semi-transparent dark background
        shapeRenderer.setProjectionMatrix(hudCam.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.7f);
        shapeRenderer.rect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        shapeRenderer.end();

        // Panel background
        float panelX = 200;
        float panelY = 100;
        float panelW = 500;
        float panelH = Math.min(600, 40 + mapNames.size() * 35);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 0.95f);
        shapeRenderer.rect(panelX, panelY, panelW, panelH);
        shapeRenderer.setColor(0.8f, 0.8f, 0.8f, 1);
        shapeRenderer.rect(panelX + 2, panelY + 2, panelW - 4, panelH - 4);
        shapeRenderer.end();

        // Draw map names
        ensureFontLoaded();
        if (font != null) {
            hudBatch.setProjectionMatrix(hudCam.combined);
            hudBatch.begin();
            font.setColor(0, 0, 0, 1);
            float yOffset = panelY + panelH - 30;
            for (int i = 0; i < mapNames.size(); i++) {
                if (i == mapListHoverIndex) {
                    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                    shapeRenderer.setColor(0.5f, 0.5f, 0.9f, 0.5f);
                    shapeRenderer.rect(panelX + 5, yOffset - 25, panelW - 10, 30);
                    shapeRenderer.end();
                }
                font.draw(hudBatch, mapNames.get(i), panelX + 20, yOffset);
                yOffset -= 35;
            }
            hudBatch.end();
        }
    }

    private void paintTile() {
        if (hoverTileX < 0 || hoverTileY < 0 || hoverTileX >= WorldMap.MAP_WIDTH || hoverTileY >= WorldMap.MAP_HEIGHT)
            return;

        // Skip if we already painted this tile this frame (prevents double-painting during drag)
        if (lastPaintedTileX == hoverTileX && lastPaintedTileY == hoverTileY)
            return;

        lastPaintedTileX = hoverTileX;
        lastPaintedTileY = hoverTileY;

        int tx = hoverTileX;
        int ty = hoverTileY;
        switch (currentBrush) {
            case GRASS:
                worldMap.setTile(tx, ty, (byte) 0);
                break;
            case WALL:
                // If already a wall, increase height; otherwise create new wall
                if (worldMap.getTile(tx, ty) != 0) {
                    byte currentHeight = worldMap.getHeight(tx, ty);
                    byte newHeight = (byte) Math.min(MAX_WALL_HEIGHT, currentHeight + 1);
                    worldMap.setHeight(tx, ty, newHeight);
                } else {
                    worldMap.setTile(tx, ty, (byte) 1);
                    worldMap.setHeight(tx, ty, (byte) 1);
                }
                break;
            case ENEMY: {
                float wx = tx * WorldMap.TILE_SIZE + WorldMap.TILE_SIZE / 2f;
                float wy = ty * WorldMap.TILE_SIZE + WorldMap.TILE_SIZE / 2f;
                worldMap.addEnemy(new EnemyNPC("enemy_" + System.nanoTime(), wx, wy, true));
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
            case DOOR: {
                float wx = tx * WorldMap.TILE_SIZE + WorldMap.TILE_SIZE / 2f;
                float wy = ty * WorldMap.TILE_SIZE + WorldMap.TILE_SIZE / 2f;
                // Defer dialog creation to next frame to avoid input event conflicts
                pendingDoorX = wx;
                pendingDoorY = wy;
                pendingDoorDialog = true;
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

        // Skip if we already erased this tile this frame
        if (lastPaintedTileX == hoverTileX && lastPaintedTileY == hoverTileY)
            return;

        lastPaintedTileX = hoverTileX;
        lastPaintedTileY = hoverTileY;

        float wx = hoverTileX * WorldMap.TILE_SIZE + WorldMap.TILE_SIZE / 2f;
        float wy = hoverTileY * WorldMap.TILE_SIZE + WorldMap.TILE_SIZE / 2f;
        Chest nearbyChest = worldMap.findNearbChest(wx, wy, WorldMap.TILE_SIZE);
        if (nearbyChest != null) {
            worldMap.removeChest(nearbyChest);
            return;
        }
        EnemyNPC nearby = worldMap.findNearbyEnemy(wx, wy, WorldMap.TILE_SIZE);
        if (nearby != null) {
            worldMap.removeEnemy(nearby);
            return;
        }
        Door nearbyDoor = worldMap.findNearbyDoor(wx, wy, WorldMap.TILE_SIZE);
        if (nearbyDoor != null) {
            worldMap.removeDoor(nearbyDoor);
            return;
        }

        // If hovering over a wall, decrease height instead of removing
        if (worldMap.getTile(hoverTileX, hoverTileY) != 0) {
            byte currentHeight = worldMap.getHeight(hoverTileX, hoverTileY);
            if (currentHeight > 1) {
                worldMap.setHeight(hoverTileX, hoverTileY, (byte) (currentHeight - 1));
            } else {
                worldMap.setTile(hoverTileX, hoverTileY, (byte) 0);  // Remove wall if height reaches 0
            }
        }
    }

    @Override
    public boolean keyDown(int keycode) {
        // Handle Escape to close map list overlay
        if (keycode == Input.Keys.ESCAPE) {
            if (showMapListOverlay) {
                showMapListOverlay = false;
                return true;
            }
        }

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

        // Handle map list overlay
        if (showMapListOverlay) {
            handleMapListClick(screenX, screenY);
            return true;
        }

        updateHoverTile(screenX, screenY);

        if (button == 0) { paintTile(); return true; }
        if (button == 1) { eraseTile(); return true; }
        return false;
    }

    private void handleMapListClick(int screenX, int screenY) {
        float panelX = 200;
        float panelY = 100;
        float panelW = 500;
        float panelH = Math.min(600, 40 + mapNames.size() * 35);

        // Close overlay if clicking outside
        if (screenX < panelX || screenX > panelX + panelW || screenY < panelY || screenY > panelY + panelH) {
            showMapListOverlay = false;
            return;
        }

        // Determine which map was clicked
        float yOffset = panelY + panelH - 30;
        for (int i = 0; i < mapNames.size(); i++) {
            if (screenY >= yOffset - 25 && screenY <= yOffset + 5) {
                loadMap(mapNames.get(i));
                showMapListOverlay = false;
                updateBrushHighlight();
                return;
            }
            yOffset -= 35;
        }
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        lastMouseX = screenX;
        lastMouseY = screenY;

        // Update map list hover if overlay is visible
        if (showMapListOverlay) {
            updateMapListHover(screenX, screenY);
            return true;
        }

        updateHoverTile(screenX, screenY);
        screenVec.set(screenX, screenY, 0);
        hudCam.unproject(screenVec);
        if (screenVec.y <= (WINDOW_HEIGHT - TOOLBAR_HEIGHT)) paintTile();
        return false;
    }

    private void updateMapListHover(int screenX, int screenY) {
        float panelX = 200;
        float panelY = 100;
        float panelW = 500;
        float panelH = Math.min(600, 40 + mapNames.size() * 35);

        if (screenX < panelX || screenX > panelX + panelW || screenY < panelY || screenY > panelY + panelH) {
            mapListHoverIndex = -1;
            return;
        }

        float yOffset = panelY + panelH - 30;
        for (int i = 0; i < mapNames.size(); i++) {
            if (screenY >= yOffset - 25 && screenY <= yOffset + 5) {
                mapListHoverIndex = i;
                return;
            }
            yOffset -= 35;
        }
        mapListHoverIndex = -1;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        lastMouseX = screenX;
        lastMouseY = screenY;
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        // Change wall height when hovering over a wall
        if (hoverTileX >= 0 && hoverTileY >= 0 && hoverTileX < WorldMap.MAP_WIDTH && hoverTileY < WorldMap.MAP_HEIGHT) {
            if (worldMap.getTile(hoverTileX, hoverTileY) != 0) {
                byte currentHeight = worldMap.getHeight(hoverTileX, hoverTileY);
                byte newHeight = currentHeight;

                if (amountY > 0) {
                    // Scroll up = increase height
                    newHeight = (byte) Math.min(MAX_WALL_HEIGHT, currentHeight + 1);
                } else if (amountY < 0) {
                    // Scroll down = decrease height
                    newHeight = (byte) Math.max(1, currentHeight - 1);
                }

                if (newHeight != currentHeight) {
                    worldMap.setHeight(hoverTileX, hoverTileY, newHeight);
                    return true;
                }
            }
        }
        return false;
    }
    @Override public boolean keyTyped(char character) { return false; }
    @Override public boolean touchCancelled(int screenX, int screenY, int pointer, int button) { return false; }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        camera.position.set(cameraIsoX, cameraIsoY, 0);
        camera.update();
        if (toolbarStage != null) {
            toolbarStage.getViewport().update(width, height, true);
        }
    }

    private void showMapNameDialog(Skin skin, String title, String prompt, String defaultText) {
        final TextField nameField = new TextField(defaultText, skin);
        final Dialog dialog = new Dialog(title, skin) {
            @Override
            protected void result(Object object) {
                if ((Boolean) object) {
                    String mapName = nameField.getText().trim();
                    if (!mapName.isEmpty()) {
                        loadMap(mapName);
                        updateBrushHighlight();
                    }
                }
            }
        };

        dialog.getContentTable().add(prompt).padRight(10);
        dialog.getContentTable().add(nameField).width(200).row();
        dialog.button("OK", true);
        dialog.button("Cancel", false);
        dialog.key(com.badlogic.gdx.Input.Keys.ENTER, true);
        dialog.key(com.badlogic.gdx.Input.Keys.ESCAPE, false);
        dialog.show(toolbarStage);
    }

    private void showDoorNameDialog(float wx, float wy) {
        Skin skin = game.manager.get("uiskin.json", Skin.class);
        final TextField targetField = new TextField("", skin);
        final TextField targetDoorField = new TextField("", skin);
        final Dialog dialog = new Dialog("Door Target", skin) {
            @Override
            protected void result(Object object) {
                if ((Boolean) object) {
                    String target = targetField.getText().trim();
                    String targetDoorId = targetDoorField.getText().trim();
                    worldMap.addDoor(new Door("door_" + System.nanoTime(), wx, wy, target, targetDoorId));
                }
            }
        };

        dialog.getContentTable().add("Target map (empty=exit)").padRight(10);
        dialog.getContentTable().add(targetField).width(200).row();

        dialog.getContentTable().add("Target door ID (optional)").padRight(10);
        dialog.getContentTable().add(targetDoorField).width(200).row();

        // Add available maps list
        if (!mapNames.isEmpty()) {
            dialog.getContentTable().add("Available maps:").padTop(10).row();
            List<String> mapList = new List<String>(skin);
            mapList.setItems(mapNames.toArray(new String[0]));
            mapList.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    String selected = mapList.getSelected();
                    if (selected != null) {
                        targetField.setText(selected);
                        targetDoorField.setText("");  // Clear target door when map changes
                    }
                }
            });
            ScrollPane scrollPane = new ScrollPane(mapList);
            dialog.getContentTable().add(scrollPane).width(200).height(100).row();
        }

        // Add available doors list (updated when target map changes)
        dialog.getContentTable().add("Available doors in target:").padTop(10).row();
        final List<String> doorList = new List<String>(skin);
        final ScrollPane doorScrollPane = new ScrollPane(doorList);
        dialog.getContentTable().add(doorScrollPane).width(200).height(100).row();

        doorList.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String selected = doorList.getSelected();
                if (selected != null) {
                    targetDoorField.setText(selected);
                }
            }
        });

        // Update door list when target map is typed or selected
        targetField.setTextFieldListener((textField, c) -> {
            String mapName = textField.getText().trim();
            if (!mapName.isEmpty()) {
                WorldMap tmpMap = new WorldMap(mapName);
                java.util.List<String> doorIds = new java.util.ArrayList<>();
                for (Door d : tmpMap.getDoors()) {
                    doorIds.add(d.id);
                }
                doorList.setItems(doorIds.toArray(new String[0]));
            } else {
                doorList.setItems();
            }
        });

        dialog.button("OK", true);
        dialog.button("Cancel", false);
        dialog.key(com.badlogic.gdx.Input.Keys.ENTER, true);
        dialog.key(com.badlogic.gdx.Input.Keys.ESCAPE, false);
        dialog.show(toolbarStage);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        hudBatch.dispose();
        worldBatch.dispose();
        if (toolbarStage != null) {
            toolbarStage.dispose();
        }
        // Textures are managed by AssetManager, not disposed here
    }
}
