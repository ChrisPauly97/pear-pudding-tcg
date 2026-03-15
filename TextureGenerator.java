import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Random;

/**
 * Generates isometric (64×32) grass and wall textures.
 * Diamond-shaped with transparent backgrounds.
 * 16 grass variations, 8 wall variations to break up repeating patterns.
 */
public class TextureGenerator {
    private static final int ISO_WIDTH = 64;
    private static final int ISO_HEIGHT = 32;
    private static final int GRASS_VARIATIONS = 16;
    private static final int WALL_VARIATIONS = 8;
    private static final String ASSETS_DIR = "assets";

    public static void main(String[] args) throws Exception {
        System.out.println("Generating isometric grass and wall textures...");

        // Generate grass textures
        for (int i = 0; i < GRASS_VARIATIONS; i++) {
            BufferedImage texture = generateGrassTexture(i);
            File outputFile = new File(ASSETS_DIR, "grass" + i + ".png");
            ImageIO.write(texture, "PNG", outputFile);
            System.out.println("Generated: " + outputFile.getAbsolutePath());
        }

        // Generate wall textures
        for (int i = 0; i < WALL_VARIATIONS; i++) {
            BufferedImage texture = generateWallTexture(i);
            File outputFile = new File(ASSETS_DIR, "wall" + i + ".png");
            ImageIO.write(texture, "PNG", outputFile);
            System.out.println("Generated: " + outputFile.getAbsolutePath());
        }

        // Generate brick wall textures for 3D effect
        BufferedImage brickTop = generateBrickWallTop();
        ImageIO.write(brickTop, "PNG", new File(ASSETS_DIR, "wallbrick_top.png"));
        System.out.println("Generated: " + new File(ASSETS_DIR, "wallbrick_top.png").getAbsolutePath());

        BufferedImage brickFront = generateBrickWallFront();
        ImageIO.write(brickFront, "PNG", new File(ASSETS_DIR, "wallbrick_front.png"));
        System.out.println("Generated: " + new File(ASSETS_DIR, "wallbrick_front.png").getAbsolutePath());

        BufferedImage brickLeft = generateBrickWallSide(true);
        ImageIO.write(brickLeft, "PNG", new File(ASSETS_DIR, "wallbrick_left.png"));
        System.out.println("Generated: " + new File(ASSETS_DIR, "wallbrick_left.png").getAbsolutePath());

        BufferedImage brickRight = generateBrickWallSide(false);
        ImageIO.write(brickRight, "PNG", new File(ASSETS_DIR, "wallbrick_right.png"));
        System.out.println("Generated: " + new File(ASSETS_DIR, "wallbrick_right.png").getAbsolutePath());

        // Generate entity sprites
        BufferedImage playerSprite = generatePlayerSprite();
        ImageIO.write(playerSprite, "PNG", new File(ASSETS_DIR, "player.png"));
        System.out.println("Generated: " + new File(ASSETS_DIR, "player.png").getAbsolutePath());

        BufferedImage enemySprite = generateEnemySprite();
        ImageIO.write(enemySprite, "PNG", new File(ASSETS_DIR, "enemy.png"));
        System.out.println("Generated: " + new File(ASSETS_DIR, "enemy.png").getAbsolutePath());

        BufferedImage chestSprite = generateChestSprite();
        ImageIO.write(chestSprite, "PNG", new File(ASSETS_DIR, "chest.png"));
        System.out.println("Generated: " + new File(ASSETS_DIR, "chest.png").getAbsolutePath());

        System.out.println("Done! All textures and sprites generated in " + ASSETS_DIR + "/");
    }

    /**
     * Generate a 64×32 isometric grass tile with diamond shape and alpha transparency.
     */
    private static BufferedImage generateGrassTexture(int variation) {
        BufferedImage img = new BufferedImage(ISO_WIDTH, ISO_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Random rand = new Random(variation * 12345);

        // Grass color
        int baseR = 135;
        int baseG = 177;
        int baseB = 87;

        // Diamond vertices for isometric 64×32 tile
        int bottomX = 32, bottomY = 0;
        int leftX = 0, leftY = 16;
        int topX = 32, topY = 32;
        int rightX = 64, rightY = 16;

        // Fill pixels inside the diamond
        for (int y = 0; y < ISO_HEIGHT; y++) {
            for (int x = 0; x < ISO_WIDTH; x++) {
                if (isInsideDiamond(x, y, bottomX, bottomY, leftX, leftY, topX, topY, rightX, rightY)) {
                    // Inside diamond - draw grass
                    if (rand.nextFloat() < 0.98f) {
                        // Grass with subtle variation
                        int var = rand.nextInt(12) - 6;
                        int r = Math.max(0, Math.min(255, baseR + var));
                        int g = Math.max(0, Math.min(255, baseG + var));
                        int b = Math.max(0, Math.min(255, baseB + var));
                        int rgb = (255 << 24) | (r << 16) | (g << 8) | b; // Fully opaque
                        img.setRGB(x, y, rgb);
                    } else {
                        // Tiny dirt spots
                        int dirtR = 110;
                        int dirtG = 100;
                        int dirtB = 75;
                        int var = rand.nextInt(10) - 5;
                        int r = Math.max(0, Math.min(255, dirtR + var));
                        int g = Math.max(0, Math.min(255, dirtG + var));
                        int b = Math.max(0, Math.min(255, dirtB + var));
                        int rgb = (255 << 24) | (r << 16) | (g << 8) | b;
                        img.setRGB(x, y, rgb);
                    }
                } else {
                    // Outside diamond - transparent
                    img.setRGB(x, y, 0x00000000); // Fully transparent
                }
            }
        }

        return img;
    }

    /**
     * Generate a 64×32 isometric wall tile with diamond shape and alpha transparency.
     */
    private static BufferedImage generateWallTexture(int variation) {
        BufferedImage img = new BufferedImage(ISO_WIDTH, ISO_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Random rand = new Random(variation * 54321);

        // Stone/wall color
        int baseR = 145;
        int baseG = 145;
        int baseB = 145;

        // Diamond vertices
        int bottomX = 32, bottomY = 0;
        int leftX = 0, leftY = 16;
        int topX = 32, topY = 32;
        int rightX = 64, rightY = 16;

        // Fill pixels inside the diamond
        for (int y = 0; y < ISO_HEIGHT; y++) {
            for (int x = 0; x < ISO_WIDTH; x++) {
                if (isInsideDiamond(x, y, bottomX, bottomY, leftX, leftY, topX, topY, rightX, rightY)) {
                    // Inside diamond - draw stone
                    int var = rand.nextInt(16) - 8;
                    int r = Math.max(0, Math.min(255, baseR + var));
                    int g = Math.max(0, Math.min(255, baseG + var));
                    int b = Math.max(0, Math.min(255, baseB + var));
                    int rgb = (255 << 24) | (r << 16) | (g << 8) | b; // Fully opaque
                    img.setRGB(x, y, rgb);
                } else {
                    // Outside diamond - transparent
                    img.setRGB(x, y, 0x00000000); // Fully transparent
                }
            }
        }

        return img;
    }

    /**
     * Check if a point (px, py) is inside the diamond.
     * Diamond is centered at (32, 16) with vertices at (32,0), (0,16), (32,32), (64,16).
     * Uses the formula: |dx|/32 + |dy|/16 <= 1
     */
    private static boolean isInsideDiamond(int px, int py,
                                           int bottomX, int bottomY,
                                           int leftX, int leftY,
                                           int topX, int topY,
                                           int rightX, int rightY) {
        // Center of diamond
        int centerX = 32;
        int centerY = 16;

        // Distance from center (normalized to diamond axes)
        float dx = Math.abs(px - centerX) / 32.0f;
        float dy = Math.abs(py - centerY) / 16.0f;

        // Point is inside if Manhattan distance in normalized space <= 1
        return (dx + dy) <= 1.0f;
    }

    /**
     * Generate a player sprite (32×48 isometric figure, blue).
     */
    private static BufferedImage generatePlayerSprite() {
        BufferedImage img = new BufferedImage(32, 48, BufferedImage.TYPE_INT_ARGB);
        Random rand = new Random(42); // Fixed seed for consistent look

        // Player is a vertical isometric figure: head (top), body (middle), legs (bottom)
        // Head: small diamond at top (around y=8-16)
        // Body: rectangle in middle (around y=16-32)
        // Legs: two small rectangles at bottom (around y=32-48)

        int headCenterX = 16, headCenterY = 10;
        int bodyTop = 16, bodyBottom = 32;
        int legLeftX = 10, legRightX = 22;
        int legY = 32, legHeight = 16;

        // Head (small blue diamond)
        for (int y = 0; y < 48; y++) {
            for (int x = 0; x < 32; x++) {
                int px = x, py = y;
                // Head diamond (small)
                float hDx = Math.abs(px - headCenterX) / 6.0f;
                float hDy = Math.abs(py - headCenterY) / 6.0f;
                if (hDx + hDy <= 1.0f) {
                    int rgb = (255 << 24) | (50 << 16) | (150 << 8) | 255; // Blue
                    img.setRGB(px, py, rgb);
                    continue;
                }

                // Body (vertical rectangle, blue)
                if (px >= 12 && px < 20 && py >= bodyTop && py < bodyBottom) {
                    int brightness = 100 + rand.nextInt(30);
                    int rgb = (255 << 24) | (brightness << 16) | (150 + brightness/2) << 8 | (200 + brightness);
                    img.setRGB(px, py, rgb);
                    continue;
                }

                // Legs (two small shapes)
                if ((px >= legLeftX && px < legLeftX + 4 || px >= legRightX && px < legRightX + 4)
                        && py >= legY && py < legY + legHeight) {
                    int rgb = (255 << 24) | (30 << 16) | (100 << 8) | 180; // Dark blue
                    img.setRGB(px, py, rgb);
                }
            }
        }

        return img;
    }

    /**
     * Generate an enemy sprite (32×48 isometric figure, red).
     */
    private static BufferedImage generateEnemySprite() {
        BufferedImage img = new BufferedImage(32, 48, BufferedImage.TYPE_INT_ARGB);
        Random rand = new Random(99);

        int headCenterX = 16, headCenterY = 10;
        int bodyTop = 16, bodyBottom = 32;
        int legLeftX = 10, legRightX = 22;
        int legY = 32, legHeight = 16;

        // Head (red diamond)
        for (int y = 0; y < 48; y++) {
            for (int x = 0; x < 32; x++) {
                int px = x, py = y;
                // Head diamond
                float hDx = Math.abs(px - headCenterX) / 6.0f;
                float hDy = Math.abs(py - headCenterY) / 6.0f;
                if (hDx + hDy <= 1.0f) {
                    int rgb = (255 << 24) | (220 << 16) | (50 << 8) | 50; // Red
                    img.setRGB(px, py, rgb);
                    continue;
                }

                // Body (red)
                if (px >= 12 && px < 20 && py >= bodyTop && py < bodyBottom) {
                    int brightness = 150 + rand.nextInt(30);
                    int rgb = (255 << 24) | (brightness << 16) | (Math.max(0, brightness - 50) << 8) | Math.max(0, brightness - 100);
                    img.setRGB(px, py, rgb);
                    continue;
                }

                // Legs (dark red)
                if ((px >= legLeftX && px < legLeftX + 4 || px >= legRightX && px < legRightX + 4)
                        && py >= legY && py < legY + legHeight) {
                    int rgb = (255 << 24) | (180 << 16) | (20 << 8) | 20; // Dark red
                    img.setRGB(px, py, rgb);
                }
            }
        }

        return img;
    }

    /**
     * Generate a chest sprite (48×40 isometric box, gold).
     */
    private static BufferedImage generateChestSprite() {
        BufferedImage img = new BufferedImage(48, 40, BufferedImage.TYPE_INT_ARGB);
        Random rand = new Random(77);

        // Chest is an isometric box with visible top and front faces
        // Top face: 48×20 at top
        // Front face: 48×20 at bottom (slightly darker)

        int topFaceTop = 0, topFaceBottom = 20;
        int frontFaceTop = 20, frontFaceBottom = 40;

        // Generate top and front faces with gold color
        for (int y = 0; y < 40; y++) {
            for (int x = 0; x < 48; x++) {
                int px = x, py = y;

                // Top face (bright gold with diamond-like shading)
                if (py >= topFaceTop && py < topFaceBottom) {
                    // Create diamond shading on top face
                    float dx = Math.abs(px - 24) / 24.0f;
                    float dy = Math.abs(py - 10) / 10.0f;
                    if (dx + dy <= 1.0f) {
                        int brightness = 200 + rand.nextInt(30);
                        int rgb = (255 << 24) | (brightness << 16) | (brightness - 40) << 8 | (brightness - 80);
                        img.setRGB(px, py, rgb);
                    }
                    continue;
                }

                // Front face (darker gold)
                if (py >= frontFaceTop && py < frontFaceBottom) {
                    int brightness = 160 + rand.nextInt(20);
                    int rgb = (255 << 24) | (brightness << 16) | (brightness - 60) << 8 | (brightness - 100);
                    img.setRGB(px, py, rgb);
                }
            }
        }

        // Add a lid outline (decorative)
        for (int x = 8; x < 40; x++) {
            int rgb = (255 << 24) | (100 << 16) | (100 << 8) | (50);
            img.setRGB(x, 19, rgb); // Line at top-front edge
        }

        return img;
    }

    /**
     * Generate an isometric brick wall top face (64×32).
     * Shows a diamond-shaped brick pattern.
     */
    private static BufferedImage generateBrickWallTop() {
        BufferedImage img = new BufferedImage(64, 32, BufferedImage.TYPE_INT_ARGB);
        Random rand = new Random(12345);

        // Brick color palette (terracotta/red)
        int[] brickColors = {
            0xFF8B4513, // Saddle brown
            0xFFA0522D, // Sienna
            0xFFCD853F, // Peru
            0xFFD2691E, // Chocolate
            0xFF8B6914  // Dark gold-brown
        };

        // Fill the diamond with brick pattern
        for (int y = 0; y < 32; y++) {
            for (int x = 0; x < 64; x++) {
                if (isInsideDiamond(x, y, 32, 0, 0, 16, 32, 32, 64, 16)) {
                    // Brick grid pattern
                    int brickRow = (y + 4) / 8;
                    int brickCol = (x + (brickRow % 2) * 8) / 16;
                    int brickColor = brickColors[(brickRow * 3 + brickCol) % brickColors.length];

                    // Add mortar (darker lines between bricks)
                    if (y % 8 < 1 || x % 16 < 1) {
                        brickColor = 0xFF3D3D3D; // Dark mortar
                    } else {
                        // Vary brick color slightly
                        int var = rand.nextInt(20) - 10;
                        int r = Math.max(0, Math.min(255, ((brickColor >> 16) & 0xFF) + var));
                        int g = Math.max(0, Math.min(255, ((brickColor >> 8) & 0xFF) + var));
                        int b = Math.max(0, Math.min(255, (brickColor & 0xFF) + var));
                        brickColor = (255 << 24) | (r << 16) | (g << 8) | b;
                    }

                    img.setRGB(x, y, brickColor | (255 << 24));
                } else {
                    img.setRGB(x, y, 0x00000000); // Transparent
                }
            }
        }

        return img;
    }

    /**
     * Generate brick wall front face (64×20).
     * Darker brick pattern for the wall front face.
     */
    private static BufferedImage generateBrickWallFront() {
        BufferedImage img = new BufferedImage(64, 20, BufferedImage.TYPE_INT_ARGB);
        Random rand = new Random(54321);

        // Darker brick colors for front face (more depth)
        int[] brickColors = {
            0xFF704214, // Darker saddle brown
            0xFF8B4513, // Saddle brown
            0xFF964B00, // Brown
            0xFF7B3F00, // Dark brown
            0xFF6B3410  // Very dark brown
        };

        for (int y = 0; y < 20; y++) {
            for (int x = 0; x < 64; x++) {
                // Brick grid pattern (tighter for smaller height)
                int brickRow = (y + 2) / 5;
                int brickCol = (x + (brickRow % 2) * 8) / 16;
                int brickColor = brickColors[(brickRow * 3 + brickCol) % brickColors.length];

                // Add mortar lines
                if (y % 5 < 1 || x % 16 < 1) {
                    brickColor = 0xFF2A2A2A; // Dark mortar
                } else {
                    // Vary color
                    int var = rand.nextInt(15) - 7;
                    int r = Math.max(0, Math.min(255, ((brickColor >> 16) & 0xFF) + var));
                    int g = Math.max(0, Math.min(255, ((brickColor >> 8) & 0xFF) + var));
                    int b = Math.max(0, Math.min(255, (brickColor & 0xFF) + var));
                    brickColor = (r << 16) | (g << 8) | b;
                }

                img.setRGB(x, y, brickColor | (255 << 24));
            }
        }

        return img;
    }

    /**
     * Generate brick wall side face (32×20).
     * Even darker to show depth. If left=true, shade left; if false, shade right.
     */
    private static BufferedImage generateBrickWallSide(boolean isLeft) {
        BufferedImage img = new BufferedImage(32, 20, BufferedImage.TYPE_INT_ARGB);
        Random rand = new Random(isLeft ? 11111 : 22222);

        // Very dark brick colors for side faces
        int[] brickColors = {
            0xFF4A2511, // Very dark brown
            0xFF5A3515, // Dark brown
            0xFF6B3F1D, // Darker
            0xFF3D2817, // Almost black brown
            0xFF4F3319  // Dark shade
        };

        for (int y = 0; y < 20; y++) {
            for (int x = 0; x < 32; x++) {
                int brickRow = (y + 2) / 5;
                int brickCol = x / 8;
                int brickColor = brickColors[(brickRow + brickCol) % brickColors.length];

                // Add mortar
                if (y % 5 < 1 || x % 8 < 1) {
                    brickColor = 0xFF1A1A1A; // Very dark mortar
                } else {
                    // Slight variation
                    int var = rand.nextInt(10) - 5;
                    int r = Math.max(0, Math.min(255, ((brickColor >> 16) & 0xFF) + var));
                    int g = Math.max(0, Math.min(255, ((brickColor >> 8) & 0xFF) + var));
                    int b = Math.max(0, Math.min(255, (brickColor & 0xFF) + var));
                    brickColor = (r << 16) | (g << 8) | b;
                }

                img.setRGB(x, y, brickColor | (255 << 24));
            }
        }

        return img;
    }
}
