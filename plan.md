 Plan to implement                                                                                                    │
│                                                                                                                      │
│ World Exploration Layer - Implementation Plan                                                                        │
│                                                                                                                      │
│ Context                                                                                                              │
│                                                                                                                      │
│ Add a Pokémon-style 2D world exploration screen between the menu and TCG battle. The player moves freely around a    │
│ tile-based world, encounters enemy NPCs by pressing E/Space, and transitions into the existing TCG battle system. On │
│  win, returns to the world (enemy gone). On loss, goes to GameOverScreen. Clear all enemies = victory.               │
│                                                                                                                      │
│ User Choices                                                                                                         │
│                                                                                                                      │
│ - Visual style: colored rectangles (green=grass, gray=wall, red=enemy, blue=player)                                  │
│ - Movement: smooth/free with hold-to-move WASD/arrows                                                                │
│ - Encounter trigger: E or Space when adjacent to enemy                                                               │
│ - Post-battle: return to world on win; GameOverScreen on loss                                                        │
│                                                                                                                      │
│ ---                                                                                                                  │
│ New Files                                                                                                            │
│                                                                                                                      │
│ core/src/com/pear/pudding/world/EnemyNPC.java                                                                        │
│                                                                                                                      │
│ Plain Java data class (no LibGDX). Fields: String id, float x, float y, boolean alive. Method: distanceTo(px, py).   │
│                                                                                                                      │
│ core/src/com/pear/pudding/world/WorldMap.java                                                                        │
│                                                                                                                      │
│ - Constants: TILE_SIZE = 32, MAP_WIDTH = MAP_HEIGHT = 100 → 3200x3200 world pixels                                   │
│ - byte[][] tiles — 0=GRASS, 1=WALL                                                                                   │
│ - List<EnemyNPC> enemies — 8 NPCs at hardcoded grass tile positions                                                  │
│ - buildMap(): fills all grass, adds border walls + 3 interior horizontal + 2 vertical wall segments                  │
│ - Methods: getTile(tx,ty), isWall(wx,wy), findNearbyEnemy(px,py,range), allEnemiesDefeated()                         │
│                                                                                                                      │
│ core/src/com/pear/pudding/screen/WorldScreen.java                                                                    │
│                                                                                                                      │
│ Implements Screen + InputProcessor.                                                                                  │
│                                                                                                                      │
│ Fields:                                                                                                              │
│ - OrthographicCamera camera + FitViewport viewport (1800x1000 virtual)                                               │
│ - SpriteBatch hudBatch (HUD text, screen-space) + ShapeRenderer shapeRenderer (world tiles)                          │
│ - BitmapFont font (from game.manager.get("uiskin.json", Skin.class).getFont("default"))                              │
│ - WorldMap worldMap, float playerX/Y, boolean moveUp/Down/Left/Right                                                 │
│ - Constants: PLAYER_SPEED=180, PLAYER_RADIUS=12, INTERACT_RANGE=48, CAMERA_LERP=5                                    │
│                                                                                                                      │
│ show(): Gdx.input.setInputProcessor(this)                                                                            │
│                                                                                                                      │
│ render(delta): calls update(delta) then draw()                                                                       │
│                                                                                                                      │
│ update(delta):                                                                                                       │
│ 1. Build dx/dy from move flags; normalize diagonal                                                                   │
│ 2. Separate-axis collision: movePlayerAxis(dx, 0) then movePlayerAxis(0, dy)                                         │
│ 3. Camera lerp: camera.position.x += (target - camera.position.x) * CAMERA_LERP * delta                              │
│ 4. Clamp camera to world bounds via MathUtils.clamp                                                                  │
│ 5. camera.update()                                                                                                   │
│                                                                                                                      │
│ movePlayerAxis(dx, dy): Test 4 corners of AABB (radius=12) against worldMap.isWall(). Apply movement only if all     │
│ corners clear.                                                                                                       │
│                                                                                                                      │
│ draw():                                                                                                              │
│ 1. Clear screen (dark bg)                                                                                            │
│ 2. shapeRenderer with camera.combined: render visible tiles only (viewport culling — compute startTile/endTile from  │
│ camera pos)                                                                                                          │
│ 3. Draw alive enemy NPCs as red squares (0.7×TILE_SIZE)                                                              │
│ 4. Draw player as blue square                                                                                        │
│ 5. shapeRenderer.end()                                                                                               │
│ 6. hudBatch (projection = setToOrtho2D(0,0,1800,1000)): draw movement hint, "Press E/Space to battle!" (yellow, when │
│  enemy nearby), enemies-remaining count                                                                              │
│                                                                                                                      │
│ keyDown(): Set move flags (WASD + arrows). On E/Space: call handleInteract().                                        │
│                                                                                                                      │
│ keyUp(): Clear move flags.                                                                                           │
│                                                                                                                      │
│ handleInteract(): worldMap.findNearbyEnemy(playerX, playerY, INTERACT_RANGE) → if found: game.setScreen(new          │
│ PearPudding(game, game.manager, this, npc))                                                                          │
│                                                                                                                      │
│ onBattleWon(EnemyNPC npc): Set npc.alive = false. If worldMap.allEnemiesDefeated() → game.setScreen(new              │
│ GameOverScreen(game, "You cleared the world!")). Else → game.setScreen(this).                                        │
│                                                                                                                      │
│ onBattleLost(): game.setScreen(new GameOverScreen(game, "Enemy"))                                                    │
│                                                                                                                      │
│ ---                                                                                                                  │
│ Modified Files                                                                                                       │
│                                                                                                                      │
│ screen/PearPudding.java                                                                                              │
│                                                                                                                      │
│ New fields:                                                                                                          │
│ private final WorldScreen worldScreen;   // null if standalone                                                       │
│ private final EnemyNPC triggeredEnemy;  // null if standalone                                                        │
│                                                                                                                      │
│ Refactor constructor — existing body moves to 4-arg form; 2-arg delegates:                                           │
│ public PearPudding(MyGame game, AssetManager manager) {                                                              │
│     this(game, manager, null, null);                                                                                 │
│ }                                                                                                                    │
│ public PearPudding(MyGame game, AssetManager manager, WorldScreen ws, EnemyNPC npc) {                                │
│     // all existing body here                                                                                        │
│     this.worldScreen = ws;                                                                                           │
│     this.triggeredEnemy = npc;                                                                                       │
│ }                                                                                                                    │
│                                                                                                                      │
│ Modify checkGameOver() — replace direct game.setScreen(new GameOverScreen(...)) calls:                               │
│ if (hero1.getHealth() <= 0) {                                                                                        │
│     if (worldScreen != null) worldScreen.onBattleLost();                                                             │
│     else game.setScreen(new GameOverScreen(game, "Enemy"));                                                          │
│     return true;                                                                                                     │
│ } else if (hero2.getHealth() <= 0) {                                                                                 │
│     if (worldScreen != null) worldScreen.onBattleWon(triggeredEnemy);                                                │
│     else game.setScreen(new GameOverScreen(game, "You"));                                                            │
│     return true;                                                                                                     │
│ }                                                                                                                    │
│                                                                                                                      │
│ screen/MenuScreen.java                                                                                               │
│                                                                                                                      │
│ One line change in Play button listener:                                                                             │
│ // Before:                                                                                                           │
│ game.setScreen(new PearPudding(game, manager));                                                                      │
│ // After:                                                                                                            │
│ game.setScreen(new WorldScreen(game));                                                                               │
│                                                                                                                      │
│ screen/GameOverScreen.java                                                                                           │
│                                                                                                                      │
│ Play Again button → new WorldScreen(game) instead of new PearPudding(game, game.manager)                             │
│                                                                                                                      │
│ Fix title text to handle the world-cleared message:                                                                  │
│ boolean playerWon = winner.equals("You") || winner.startsWith("You cleared");                                        │
│ String titleText = playerWon ? (winner.startsWith("You cleared") ? winner : "YOU WIN!") : "YOU LOSE!";               │
│                                                                                                                      │
│ ---                                                                                                                  │
│ Implementation Order                                                                                                 │
│                                                                                                                      │
│ 1. EnemyNPC.java (no deps)                                                                                           │
│ 2. WorldMap.java (depends on EnemyNPC)                                                                               │
│ 3. Modify PearPudding.java (add 4-arg constructor + modify checkGameOver)                                            │
│ 4. WorldScreen.java (depends on WorldMap, EnemyNPC, modified PearPudding)                                            │
│ 5. Modify MenuScreen.java (one line)                                                                                 │
│ 6. Modify GameOverScreen.java (Play Again + title fix)                                                               │
│                                                                                                                      │
│ No New Assets / No Gradle Changes Needed                                                                             │
│                                                                                                                      │
│ All rendering uses ShapeRenderer (colored rectangles). No tilemaps, no new textures. ShapeRenderer is already a      │
│ LibGDX core dependency.                                                                                              │
│                                                                                                                      │
│ Verification                                                                                                         │
│                                                                                                                      │
│ - ./gradlew desktop:build should compile clean                                                                       │
│ - Run game: Menu → Play → WorldScreen renders green/gray tile world with blue player                                 │
│ - WASD moves player; camera follows with lerp; walls block movement                                                  │
│ - Red enemy squares visible; approaching and pressing E/Space → TCG battle starts                                    │
│ - Win battle → back to world, enemy gone; lose → GameOverScreen                                                      │
│ - Defeat all 8 enemies → "You cleared the world!" screen                                                             │
╰────────────────────────────────────────────────────────────