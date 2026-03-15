package com.pear.pudding.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WorldMap {
    public static final int TILE_SIZE = 32;
    public static final int MAP_WIDTH = 100;
    public static final int MAP_HEIGHT = 100;

    private static final byte GRASS = 0;
    private static final byte WALL = 1;

    private final byte[][] tiles;
    private final byte[][] heights;  // Wall height in levels (0 = no wall, 1+ = height)
    private final List<EnemyNPC> enemies;
    private final List<Chest> chests;
    private int playerSpawnX = -1;  // -1 means not set
    private int playerSpawnY = -1;

    private static final Random WORLD_RAND = new Random(12345);  // Seeded for reproducibility

    public WorldMap() {
        tiles = new byte[MAP_HEIGHT][MAP_WIDTH];
        heights = new byte[MAP_HEIGHT][MAP_WIDTH];
        enemies = new ArrayList<>();
        chests = new ArrayList<>();

        FileHandle fh = Gdx.files.local("map.txt");
        if (fh.exists()) {
            loadFromFile(fh);
        } else {
            buildMap();
        }
    }

    private void buildMap() {
        // Fill all with grass
        for (int y = 0; y < MAP_HEIGHT; y++) {
            for (int x = 0; x < MAP_WIDTH; x++) {
                tiles[y][x] = GRASS;
                heights[y][x] = 0;
            }
        }

        // Border walls (height 1)
        for (int x = 0; x < MAP_WIDTH; x++) {
            tiles[0][x] = WALL;
            heights[0][x] = 1;
            tiles[MAP_HEIGHT - 1][x] = WALL;
            heights[MAP_HEIGHT - 1][x] = 1;
        }
        for (int y = 0; y < MAP_HEIGHT; y++) {
            tiles[y][0] = WALL;
            heights[y][0] = 1;
            tiles[y][MAP_WIDTH - 1] = WALL;
            heights[y][MAP_WIDTH - 1] = 1;
        }

        // 3 interior horizontal wall segments (varying heights)
        for (int x = 10; x < 35; x++) {
            tiles[30][x] = WALL;
            heights[30][x] = (byte) ((x % 3 == 0) ? 2 : 1);  // Some 2-high sections
        }
        for (int x = 45; x < 70; x++) {
            tiles[50][x] = WALL;
            heights[50][x] = 2;  // 2-high wall
        }
        for (int x = 10; x < 35; x++) {
            tiles[70][x] = WALL;
            heights[70][x] = 1;
        }

        // 2 interior vertical wall segments (varying heights)
        for (int y = 20; y < 40; y++) {
            tiles[y][55] = WALL;
            heights[y][55] = (byte) ((y % 4 == 0) ? 2 : 1);
        }
        for (int y = 60; y < 85; y++) {
            tiles[y][25] = WALL;
            heights[y][25] = 1;
        }

        // Procedurally place enemies and chests with difficulty scaling
        // Difficulty increases from top-left (easier) to bottom-right (harder)
        generateEntitiesWithDifficulty();
    }

    /**
     * Generate enemies and chests procedurally with difficulty scaling.
     * Difficulty increases from top-left (depth 0) to bottom-right (depth max).
     */
    private void generateEntitiesWithDifficulty() {
        String[] cardIds = {"ghost", "skeleton", "zombie", "ghoul"};
        int maxDepth = MAP_WIDTH + MAP_HEIGHT - 2;

        // Place 8 enemies across difficulty tiers
        int enemyCount = 0;
        for (int depth = 10; depth < maxDepth && enemyCount < 8; depth += (maxDepth - 20) / 8) {
            int tx = WORLD_RAND.nextInt(Math.min(MAP_WIDTH - 2, depth + 1));
            int ty = depth - tx;
            if (ty < 1 || ty >= MAP_HEIGHT - 1 || tiles[ty][tx] != GRASS) continue;

            float wx = tx * TILE_SIZE + TILE_SIZE / 2f;
            float wy = ty * TILE_SIZE + TILE_SIZE / 2f;
            enemies.add(new EnemyNPC("enemy_" + enemyCount, wx, wy, true));
            enemyCount++;
        }

        // Place 4 chests at varying difficulties (easier ones early, harder ones late)
        int chestCount = 0;
        int[] chestDepths = {maxDepth / 4, maxDepth / 2, (maxDepth * 3) / 4, maxDepth - 10};
        for (int targetDepth : chestDepths) {
            if (chestCount >= 4) break;
            int depth = targetDepth + WORLD_RAND.nextInt(10) - 5;  // Random variance
            depth = Math.max(5, Math.min(maxDepth - 5, depth));

            int tx = WORLD_RAND.nextInt(Math.min(MAP_WIDTH - 2, depth + 1));
            int ty = depth - tx;
            if (ty < 1 || ty >= MAP_HEIGHT - 1 || tiles[ty][tx] != GRASS) continue;

            float wx = tx * TILE_SIZE + TILE_SIZE / 2f;
            float wy = ty * TILE_SIZE + TILE_SIZE / 2f;
            String cardId = cardIds[chestCount % cardIds.length];
            chests.add(new Chest("chest_" + chestCount, wx, wy, cardId));
            chestCount++;
        }
    }

    public byte getTile(int tx, int ty) {
        if (tx < 0 || tx >= MAP_WIDTH || ty < 0 || ty >= MAP_HEIGHT) {
            return WALL;
        }
        return tiles[ty][tx];
    }

    public byte getHeight(int tx, int ty) {
        if (tx < 0 || tx >= MAP_WIDTH || ty < 0 || ty >= MAP_HEIGHT) {
            return 1;  // Out of bounds = wall height 1
        }
        return heights[ty][tx];
    }

    public void setHeight(int tx, int ty, byte height) {
        if (tx >= 0 && tx < MAP_WIDTH && ty >= 0 && ty < MAP_HEIGHT) {
            heights[ty][tx] = height;
        }
    }

    public boolean isWall(float worldX, float worldY) {
        int tx = (int) (worldX / TILE_SIZE);
        int ty = (int) (worldY / TILE_SIZE);
        return getTile(tx, ty) == WALL;
    }

    public byte getWallHeight(float worldX, float worldY) {
        int tx = (int) (worldX / TILE_SIZE);
        int ty = (int) (worldY / TILE_SIZE);
        return isWall(worldX, worldY) ? getHeight(tx, ty) : 0;
    }

    public EnemyNPC findNearbyEnemy(float px, float py, float range) {
        for (EnemyNPC enemy : enemies) {
            if (enemy.alive && enemy.distanceTo(px, py) <= range) {
                return enemy;
            }
        }
        return null;
    }

    public boolean allEnemiesDefeated() {
        for (EnemyNPC enemy : enemies) {
            if (enemy.alive) {
                return false;
            }
        }
        return true;
    }

    public List<EnemyNPC> getEnemies() {
        return enemies;
    }

    public Chest findNearbChest(float px, float py, float range) {
        for (Chest chest : chests) {
            if (!chest.opened && chest.distanceTo(px, py) <= range) {
                return chest;
            }
        }
        return null;
    }

    public List<Chest> getChests() {
        return chests;
    }

    public void setTile(int tx, int ty, byte value) {
        if (tx >= 0 && tx < MAP_WIDTH && ty >= 0 && ty < MAP_HEIGHT) {
            tiles[ty][tx] = value;
        }
    }

    public void addEnemy(EnemyNPC enemy) {
        enemies.add(enemy);
    }

    public void removeEnemy(EnemyNPC enemy) {
        enemies.remove(enemy);
    }

    public void addChest(Chest chest) {
        chests.add(chest);
    }

    public void removeChest(Chest chest) {
        chests.remove(chest);
    }

    public int getPlayerSpawnX() {
        return playerSpawnX;
    }

    public int getPlayerSpawnY() {
        return playerSpawnY;
    }

    public void setPlayerSpawn(int tx, int ty) {
        this.playerSpawnX = tx;
        this.playerSpawnY = ty;
    }

    public boolean hasPlayerSpawn() {
        return playerSpawnX >= 0 && playerSpawnY >= 0;
    }

    public void resetMap() {
        // Fill all with grass
        for (int y = 0; y < MAP_HEIGHT; y++) {
            for (int x = 0; x < MAP_WIDTH; x++) {
                tiles[y][x] = GRASS;
                heights[y][x] = 0;
            }
        }
        // Clear player spawn and entities
        playerSpawnX = -1;
        playerSpawnY = -1;
        enemies.clear();
        chests.clear();
    }

    public void saveToFile(FileHandle fh) {
        StringBuilder sb = new StringBuilder();
        sb.append(MAP_WIDTH).append(" ").append(MAP_HEIGHT).append("\n");

        // Save tiles
        for (int y = 0; y < MAP_HEIGHT; y++) {
            for (int x = 0; x < MAP_WIDTH; x++) {
                sb.append((int) tiles[y][x]);
            }
            sb.append("\n");
        }

        // Save heights (only for walls)
        sb.append("HEIGHTS\n");
        for (int y = 0; y < MAP_HEIGHT; y++) {
            for (int x = 0; x < MAP_WIDTH; x++) {
                if (tiles[y][x] != 0) {  // Only save heights for walls
                    sb.append(x).append(",").append(y).append(",").append((int) heights[y][x]).append("\n");
                }
            }
        }

        if (hasPlayerSpawn()) {
            sb.append("SPAWN ").append(playerSpawnX).append(" ").append(playerSpawnY).append("\n");
        }

        for (EnemyNPC enemy : enemies) {
            sb.append("ENEMY ").append((int) enemy.x).append(" ").append((int) enemy.y).append("\n");
        }

        for (Chest chest : chests) {
            // Save all card IDs, comma-separated
            StringBuilder cardIds = new StringBuilder();
            for (int i = 0; i < chest.cardTemplateIds.size(); i++) {
                if (i > 0) cardIds.append(",");
                cardIds.append(chest.cardTemplateIds.get(i));
            }
            sb.append("CHEST ").append((int) chest.x).append(" ").append((int) chest.y).append(" ").append(cardIds).append("\n");
        }

        fh.writeString(sb.toString(), false);
    }

    public void loadFromFile(FileHandle fh) {
        try {
            String content = fh.readString();
            String[] lines = content.split("\n");

            if (lines.length < MAP_HEIGHT + 1) {
                throw new RuntimeException("Invalid map file: not enough tile rows");
            }

            for (int y = 0; y < MAP_HEIGHT; y++) {
                String line = lines[y + 1].trim();
                if (line.length() < MAP_WIDTH) {
                    throw new RuntimeException("Invalid map file: row " + y + " too short");
                }
                for (int x = 0; x < MAP_WIDTH; x++) {
                    tiles[y][x] = (byte) (line.charAt(x) - '0');
                    heights[y][x] = 0;  // Default height
                }
            }

            playerSpawnX = -1;
            playerSpawnY = -1;
            enemies.clear();
            chests.clear();

            boolean inHeightsSection = false;
            for (int i = MAP_HEIGHT + 1; i < lines.length; i++) {
                String line = lines[i].trim();
                if (line.isEmpty()) continue;

                if (line.equals("HEIGHTS")) {
                    inHeightsSection = true;
                    continue;
                }

                // Check for entity/spawn commands BEFORE heights section (heights check must be last)
                if (line.startsWith("SPAWN ")) {
                    String[] parts = line.split(" ");
                    if (parts.length >= 3) {
                        playerSpawnX = Integer.parseInt(parts[1]);
                        playerSpawnY = Integer.parseInt(parts[2]);
                    }
                } else if (line.startsWith("ENEMY ")) {
                    String[] parts = line.split(" ");
                    if (parts.length >= 3) {
                        float x = Float.parseFloat(parts[1]);
                        float y = Float.parseFloat(parts[2]);
                        enemies.add(new EnemyNPC("enemy_" + System.currentTimeMillis() + "_" + i, x, y, true));
                    }
                } else if (line.startsWith("CHEST ")) {
                    String[] parts = line.split(" ");
                    if (parts.length >= 4) {
                        float x = Float.parseFloat(parts[1]);
                        float y = Float.parseFloat(parts[2]);
                        String cardIdsStr = parts[3];
                        // Parse comma-separated card IDs
                        java.util.List<String> cardIds = new java.util.ArrayList<>();
                        for (String cardId : cardIdsStr.split(",")) {
                            cardIds.add(cardId.trim());
                        }
                        chests.add(new Chest("chest_" + System.currentTimeMillis() + "_" + i, x, y, cardIds));
                    }
                } else if (inHeightsSection) {
                    // Parse height entries: "x,y,height"
                    String[] parts = line.split(",");
                    if (parts.length == 3) {
                        try {
                            int x = Integer.parseInt(parts[0].trim());
                            int y = Integer.parseInt(parts[1].trim());
                            byte h = Byte.parseByte(parts[2].trim());
                            if (x >= 0 && x < MAP_WIDTH && y >= 0 && y < MAP_HEIGHT) {
                                heights[y][x] = h;
                            }
                        } catch (NumberFormatException e) {
                            // Skip invalid height lines
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading map file: " + e.getMessage());
            e.printStackTrace();
            buildMap();
        }
    }
}
