# Pear Pudding TCG

## Project Overview

A Java-based Trading Card Game built with the LibGDX framework, targeting Desktop, Android, iOS, and HTML5.

**Build system:** Gradle multi-project (core, desktop, android, ios, html)
**Framework:** LibGDX with Scene2D/Stage UI

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

`PearPudding` (the main game screen) implements `InputProcessor` directly and is registered via `InputMultiplexer`. Key methods:

- `touchDown` → `handleLeftClick` / `handleRightClick`
- `handleHitCard(card)` — begins a drag via `startDragging` (board or hand) and snapshots the other deck
- `touchDragged` — moves the card visually and drives hover animations
- `touchUp` — resolves the drop: play to board, attack, or restore to original location

## AI

`BasicAI` (player2 when `isAI=true`) is scheduled with `Timer` tasks after the human player ends their turn:
1. `playCards()` — plays affordable cards to board
2. `attackWithMinions()` — attacks enemy minions/hero
3. `endAITurn()` — ends turn, starts player1's turn
