package com.pear.pudding.world;

import java.util.ArrayList;
import java.util.List;

public class WorldMap {
    public static final int TILE_SIZE = 32;
    public static final int MAP_WIDTH = 100;
    public static final int MAP_HEIGHT = 100;

    private static final byte GRASS = 0;
    private static final byte WALL = 1;

    private final byte[][] tiles;
    private final List<EnemyNPC> enemies;

    public WorldMap() {
        tiles = new byte[MAP_HEIGHT][MAP_WIDTH];
        enemies = new ArrayList<>();
        buildMap();
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

        // Place 8 enemies at hardcoded grass positions
        enemies.add(new EnemyNPC("enemy1", 20 * TILE_SIZE, 15 * TILE_SIZE));
        enemies.add(new EnemyNPC("enemy2", 80 * TILE_SIZE, 20 * TILE_SIZE));
        enemies.add(new EnemyNPC("enemy3", 50 * TILE_SIZE, 35 * TILE_SIZE));
        enemies.add(new EnemyNPC("enemy4", 15 * TILE_SIZE, 60 * TILE_SIZE));
        enemies.add(new EnemyNPC("enemy5", 85 * TILE_SIZE, 65 * TILE_SIZE));
        enemies.add(new EnemyNPC("enemy6", 40 * TILE_SIZE, 80 * TILE_SIZE));
        enemies.add(new EnemyNPC("enemy7", 70 * TILE_SIZE, 45 * TILE_SIZE));
        enemies.add(new EnemyNPC("enemy8", 30 * TILE_SIZE, 75 * TILE_SIZE));
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
}
