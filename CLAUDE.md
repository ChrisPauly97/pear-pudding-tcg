# Pear Pudding TCG

## Project Overview

A Java-based Isometric Trading Card Game built with the LibGDX framework, combining turn-based card battles with world exploration. Targets Desktop (primary), Android, iOS, and HTML5.

**Build system:** Gradle multi-project (core, desktop, android, ios, html)
**Framework:** LibGDX with Scene2D/Stage UI
**Status:** Core mechanics complete; transitioning from technical debt to feature-rich gameplay

## Architecture

### Key packages under `core/src/com/pear/pudding/`

| Package | Purpose |
|---------|---------|
| `model/` | Card, Deck, Board, Hand, DiscardPile, DrawDeck data models |
| `player/` | Player and Hero classes |
| `input/` | Input processing (PuddingInputProcessor — currently not wired up; PearPudding screen handles input directly) |
| `screen/` | Game screens: PearPudding (main game), MenuScreen, GameLoadingScreen, GameOverScreen |
| `card/` | Card subtypes (Ghost, Skeleton, Zombie, Ghoul) and effect system (EffectTrigger, EffectType, StatusEffect) |
| `enums/` | Location, CardType, CardClass, Side |
| `ai/` | BasicAI player logic (AIActionExecutor) |

## Card Lifecycle & Locations

Cards move between these `Location` enum values:

```
DRAWDECK → HAND → BOARD → DISCARD
                          ↑ (death or discard effect)
              ZOOM  (right-click preview, not a true location)
```

- **DRAWDECK** — face-down, undrawn cards
- **HAND** — up to 5 slots, face-up; left-aligned, gaps filled on rebalance
- **BOARD** — up to 5 slots, face-up; cards centered around the middle slot (index 2)
- **DISCARD** — dead/discarded cards stacked at one visual position
- **ZOOM** — temporary preview state (right-click on hand card); card returns to HAND on right-click again

## Card State

Every `Card extends Actor` and tracks:

- `currentLocation` — where the card is right now (authoritative source of truth)
- `faceUp` — controlled by `Card.move()` based on location (HAND/BOARD → face-up, DISCARD/DRAWDECK → face-down)
- `attackCount` — attacks remaining this turn (reset to 1 on `startTurn`)
- `summoningSick` — true when just played; blocks attacking until next turn
- `outOfPlay` — stun/freeze counter; 0 = active

## Deck / Placement System

`Deck` is the base class for Board, Hand, DiscardPile, DrawDeck.

**Dragging pattern:**
1. `startDragging(card)` — snapshots the deck with the card still in it, marks `draggingCard`
2. During drag, `firstEmptySlot()` treats the dragging card's slot as empty (enabling hover gap animations)
3. `stopDragging()` — clears the dragging marker
4. `restoreSnapshot()` — restores deck to snapshot state (positions + `currentLocation`) and re-snapshots

**Atomic moves:**
`Deck.moveCardBetweenDecks(card, fromDeck, toDeck, toIndex)` — single source of truth for all card transfers; calls `fromDeck.removeCard` + `fromDeck.stopDragging`, then `toDeck.addCard`.

**Hover gap animations:**
Both Board and Hand implement `handleHover(mouseCoords)` which calls `rebalance(targetSlot)` to shift cards and open a gap where the dragged card will land. `restoreSnapshot()` is called when hover slot changes or the mouse leaves.

## Input Handling

**Centralized via `InputController`** (package: `input/`)

- All keybindings defined in `InputController` with sensible defaults (WASD + arrows, E interact, Space jump)
- Supports rebinding at runtime via `setXxxKey()` methods
- Screens call `controller.handleKeyDown/Up()` on key events
- Screens poll `controller.moveUp/Down/Left/Right` state during update

**Usage pattern:**
```java
private final InputController inputController = new InputController();

@Override
public boolean keyDown(int keycode) {
    if (inputController.isInteractPressed(keycode)) {
        handleInteract();
        return true;
    }
    inputController.handleKeyDown(keycode);
    return true;
}

@Override
public boolean keyUp(int keycode) {
    inputController.handleKeyUp(keycode);
    return true;
}

private void update(float delta) {
    float dx = 0, dy = 0;
    if (inputController.moveUp) { dx += SPEED; dy += SPEED; }
    if (inputController.moveDown) { dx -= SPEED; dy -= SPEED; }
    // ... apply movement
}
```

**Battle Input:** `PearPudding` implements `InputProcessor` directly for card drag-drop:
- `touchDown` → `handleLeftClick` / `handleRightClick`
- `handleHitCard(card)` — begins a drag via `startDragging` (board or hand) and snapshots the other deck
- `touchDragged` — moves the card visually and drives hover animations
- `touchUp` — resolves the drop: play to board, attack, or restore to original location

## AI

`BasicAI` (player2 when `isAI=true`) is scheduled with `Timer` tasks after the human player ends their turn:
1. `playCards()` — plays affordable cards to board
2. `attackWithMinions()` — attacks enemy minions/hero
3. `endAITurn()` — ends turn, starts player1's turn

## Recent Improvements (March 2026)

### Fixed Technical Debt

**Asset & Rendering:**
- **Asset Manager Integration:** All textures and audio loaded via AssetManager at startup (AssetLoadingScreen). Eliminates manual `new Texture()` and silent load failures.
- **SpriteBatch Pooling:** WorldScreen and MapEditorScreen create SpriteBatch once in constructor, reuse across frames. Eliminated per-frame allocation/disposal.
- **Texture Disposal:** Screens no longer manually dispose textures (AssetManager owns lifetime). Cleaner dispose() methods.

**Memory & Performance:**
- **Entity List Pooling:** WorldScreen reuses `entitiesPool` List for rendering instead of `new ArrayList<>()` every frame.
- **Vector3 Pooling:** MapEditorScreen uses single pooled `screenVec` for all unprojection instead of allocating per input event.
- **Result:** Eliminated per-frame GC pressure in hot rendering loop.

**Input & World Generation:**
- **InputController:** Centralized keybinding system (input/ package) for rebindable controls. Supports WASD + arrows, E interact, Space jump.
- **Procedural Generation:** WorldMap now generates enemies/chests procedurally with difficulty scaling (easier at low depth, harder at high depth) instead of hardcoded placement.
- **Seeded Randomness:** Reproducible world generation via seeded Random(12345).

### Why This Matters
- **Memory stability:** No per-frame allocations → predictable GC behavior
- **Error visibility:** Asset failures explicit, not silent nulls
- **Performance:** Pooled objects + fewer allocations = smoother 60 FPS
- **Extensibility:** Difficulty scaling sets stage for varied encounters and progression systems
- **Rebindable controls:** Easy foundation for accessibility and user preferences

## Strategic Direction: From Polish to Progression

**Vision:** Build a "Slay the Spire" style adventure TCG where map exploration, enemy variety, and card progression are core to the experience.

### Phase 1: World & Encounter Design (Next 2 weeks)
- [ ] Procedural map difficulty scaling (easier enemies/chests at top-left, harder at bottom-right)
- [ ] Enemy deck variety (different card loadouts per encounter)
- [ ] Loot progression (defeating enemies unlocks cards for player deck)
- [ ] Run-based structure (clear world → victory screen → new run)

### Phase 2: Card System Integration (2-4 weeks)
- [ ] Player deck building screen (pre-run deck selection)
- [ ] Tie card rewards to enemy victories (create reward tables by difficulty)
- [ ] Implement difficulty-based card rarity (rare drops from tough encounters)
- [ ] Persistent run stats (wins/losses, deck evolution tracking)

### Phase 3: UI Polish (Parallel with above)
- [ ] Replace ShapeRenderer toolbar with Scene2D buttons (MapEditorScreen)
- [ ] Victory fanfare + defeat screen transitions
- [ ] Animated card reveals in combat
- [ ] World map HUD improvements (enemy count, chest count, run stats)

### Phase 4: Audio & Feel (End of cycle)
- [ ] Battle music + victory fanfare
- [ ] Card play SFX + attack SFX
- [ ] Footstep variation (already have base sound)
- [ ] Pause menu with keybinding UI

## Game Loop & Screens

**Flow:** MenuScreen → WorldScreen (isometric exploration) → PearPudding (turn-based battle) → back to WorldScreen (or GameOverScreen on defeat)

- **MenuScreen** — main menu
- **WorldScreen** — isometric tile-based world with player, enemies, chests; handles exploration, collision, jumping; auto-triggers battles when tracking enemies get close
- **PearPudding** — turn-based card battle arena; human vs AI
- **MapEditorScreen** — level design tool for editing WorldMap (tiles, walls, enemies, chests)
- **ChestOpenScreen** — sequential card reveal animation
- **GameOverScreen** — victory/defeat state

## Asset Management

**Critical pattern: All textures and audio loaded via AssetManager at app startup.**

- `AssetLoadingScreen` queues all world textures, audio, and UI assets
- `WorldScreen` and `MapEditorScreen` fetch pre-loaded assets via `game.manager.get()`
- **Do NOT manually load textures with `new Texture()`** — use the asset manager
- Textures are managed by AssetManager; screens should NOT dispose them

**Pattern:**
```java
// In show():
texture = game.manager.get("path.png", Texture.class);

// In dispose(): DO NOT dispose textures; only dispose batches and renderers
worldBatch.dispose();
shapeRenderer.dispose();
```

## World Map & Procedural Generation

**WorldMap** encapsulates:
- Tile grid (100×100) with grass/wall tiles
- Wall heights (for 3D stacking in isometric view)
- Enemy and chest lists
- Player spawn point
- Save/load to `map.txt`

**Difficulty Scaling:** Entities placed procedurally with depth-based difficulty:
- Depth = tx + ty (isometric front-to-back ordering)
- Easier encounters at low depth (top-left), harder at high depth (bottom-right)
- 8 enemies spread across depth bands
- 4 chests placed at 25%, 50%, 75%, 100% difficulty thresholds
- Seeded `Random` for reproducible generation

**Save/Load Format:**
```
100 100
[100 tile rows as 0/1 chars]
HEIGHTS
[sparse height entries: x,y,height]
SPAWN x y
ENEMY x y
ENEMY x y
...
CHEST x y card1,card2,card3
```

## Screen Lifecycle

**CRITICAL: UI must always be created in `show()`, never in the constructor.**

LibGDX screens have a lifecycle: constructor → `setScreen()` → `show()` → `render()`. Assets are loaded asynchronously and only guaranteed available *after* `show()` is called.

**Pattern:**
```java
public MyScreen(MyGame game, ...) {
    this.game = game;
    // Initialize fields and simple objects only
    // DO NOT call createUI() here
}

@Override
public void show() {
    // NOW safe to load assets and create UI
    loadTextures();  // Fetch from asset manager
    Gdx.input.setInputProcessor(this);
}
```

## Rendering & Batching

**SpriteBatch pooling:** Create ONE `SpriteBatch` in constructor, reuse it across all frames. Do NOT create/dispose per frame.

**Pattern:**
```java
private SpriteBatch worldBatch;

public MyScreen(...) {
    worldBatch = new SpriteBatch();  // Once
}

private void draw() {
    worldBatch.begin();
    worldBatch.draw(...);  // Multiple draws per frame
    worldBatch.end();
}

@Override
public void dispose() {
    worldBatch.dispose();  // Once at end
}
```
