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
    private final List<EnemyNPC> enemies;
    private final List<Chest> chests;
    private int playerSpawnX = -1;  // -1 means not set
    private int playerSpawnY = -1;

    public WorldMap() {
        tiles = new byte[MAP_HEIGHT][MAP_WIDTH];
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
            }
        }

        // Border walls
        for (int x = 0; x < MAP_WIDTH; x++) {
            tiles[0][x] = WALL;
            tiles[MAP_HEIGHT - 1][x] = WALL;
        }
        for (int y = 0; y < MAP_HEIGHT; y++) {
            tiles[y][0] = WALL;
            tiles[y][MAP_WIDTH - 1] = WALL;
        }

        // 3 interior horizontal wall segments
        for (int x = 10; x < 35; x++) tiles[30][x] = WALL;
        for (int x = 45; x < 70; x++) tiles[50][x] = WALL;
        for (int x = 10; x < 35; x++) tiles[70][x] = WALL;

        // 2 interior vertical wall segments
        for (int y = 20; y < 40; y++) tiles[y][55] = WALL;
        for (int y = 60; y < 85; y++) tiles[y][25] = WALL;

        // Place 8 enemies at hardcoded grass positions, ~10% tracking
        Random rand = new Random();
        enemies.add(new EnemyNPC("enemy1", 20 * TILE_SIZE, 15 * TILE_SIZE, rand.nextDouble() < 0.1));
        enemies.add(new EnemyNPC("enemy2", 80 * TILE_SIZE, 20 * TILE_SIZE, rand.nextDouble() < 0.1));
        enemies.add(new EnemyNPC("enemy3", 50 * TILE_SIZE, 35 * TILE_SIZE, rand.nextDouble() < 0.1));
        enemies.add(new EnemyNPC("enemy4", 15 * TILE_SIZE, 60 * TILE_SIZE, rand.nextDouble() < 0.1));
        enemies.add(new EnemyNPC("enemy5", 85 * TILE_SIZE, 65 * TILE_SIZE, rand.nextDouble() < 0.1));
        enemies.add(new EnemyNPC("enemy6", 40 * TILE_SIZE, 80 * TILE_SIZE, rand.nextDouble() < 0.1));
        enemies.add(new EnemyNPC("enemy7", 70 * TILE_SIZE, 45 * TILE_SIZE, rand.nextDouble() < 0.1));
        enemies.add(new EnemyNPC("enemy8", 30 * TILE_SIZE, 75 * TILE_SIZE, rand.nextDouble() < 0.1));

        // Place 4 chests at hardcoded grass positions with random card rewards
        String[] cardIds = {"ghost", "skeleton", "zombie", "ghoul"};
        chests.add(new Chest("chest1", 35 * TILE_SIZE, 25 * TILE_SIZE, cardIds[0]));
        chests.add(new Chest("chest2", 65 * TILE_SIZE, 55 * TILE_SIZE, cardIds[1]));
        chests.add(new Chest("chest3", 25 * TILE_SIZE, 45 * TILE_SIZE, cardIds[2]));
        chests.add(new Chest("chest4", 75 * TILE_SIZE, 75 * TILE_SIZE, cardIds[3]));
    }

    public byte getTile(int tx, int ty) {
        if (tx < 0 || tx >= MAP_WIDTH || ty < 0 || ty >= MAP_HEIGHT) {
            return WALL;
        }
        return tiles[ty][tx];
    }

    public boolean isWall(float worldX, float worldY) {
        int tx = (int) (worldX / TILE_SIZE);
        int ty = (int) (worldY / TILE_SIZE);
        return getTile(tx, ty) == WALL;
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

        for (int y = 0; y < MAP_HEIGHT; y++) {
            for (int x = 0; x < MAP_WIDTH; x++) {
                sb.append((int) tiles[y][x]);
            }
            sb.append("\n");
        }

        if (hasPlayerSpawn()) {
            sb.append("SPAWN ").append(playerSpawnX).append(" ").append(playerSpawnY).append("\n");
        }

        for (EnemyNPC enemy : enemies) {
            sb.append("ENEMY ").append((int) enemy.x).append(" ").append((int) enemy.y).append("\n");
        }

        for (Chest chest : chests) {
            sb.append("CHEST ").append((int) chest.x).append(" ").append((int) chest.y).append(" ").append(chest.cardTemplateId).append("\n");
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
                }
            }

            playerSpawnX = -1;
            playerSpawnY = -1;
            enemies.clear();
            chests.clear();
            for (int i = MAP_HEIGHT + 1; i < lines.length; i++) {
                String line = lines[i].trim();
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
                        enemies.add(new EnemyNPC("enemy_" + System.currentTimeMillis() + "_" + i, x, y));
                    }
                } else if (line.startsWith("CHEST ")) {
                    String[] parts = line.split(" ");
                    if (parts.length >= 4) {
                        float x = Float.parseFloat(parts[1]);
                        float y = Float.parseFloat(parts[2]);
                        String cardId = parts[3];
                        chests.add(new Chest("chest_" + System.currentTimeMillis() + "_" + i, x, y, cardId));
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
