import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Random;

/**
 * Generates procedural grass textures - light, with subtle variation.
 * 16 variations to break up repeating patterns.
 */
public class TextureGenerator {
    private static final int TILE_SIZE = 32;
    private static final int GRASS_VARIATIONS = 16;
    private static final int WALL_VARIATIONS = 8;
    private static final String ASSETS_DIR = "assets";

    public static void main(String[] args) throws Exception {
        System.out.println("Generating light grass and wall textures...");

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

        System.out.println("Done! Textures generated in " + ASSETS_DIR + "/");
    }

    private static BufferedImage generateGrassTexture(int variation) {
        BufferedImage img = new BufferedImage(TILE_SIZE, TILE_SIZE, BufferedImage.TYPE_INT_RGB);
        Random rand = new Random(variation * 12345);

        // Same base color for all grass variations - randomness creates the difference
        int baseR = 135;
        int baseG = 177;
        int baseB = 87;

        // Fill entire tile with mostly grass, small subtle variations
        for (int y = 0; y < TILE_SIZE; y++) {
            for (int x = 0; x < TILE_SIZE; x++) {
                // Most pixels are grass (98%), occasional dirt showing through (2%)
                if (rand.nextFloat() < 0.98f) {
                    // Grass with very small variation
                    int var = rand.nextInt(12) - 6; // Only -6 to +6 variation
                    int r = Math.max(0, Math.min(255, baseR + var));
                    int g = Math.max(0, Math.min(255, baseG + var));
                    int b = Math.max(0, Math.min(255, baseB + var));
                    int rgb = (r << 16) | (g << 8) | b;
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
                    int rgb = (r << 16) | (g << 8) | b;
                    img.setRGB(x, y, rgb);
                }
            }
        }

        return img;
    }

    private static BufferedImage generateWallTexture(int variation) {
        BufferedImage img = new BufferedImage(TILE_SIZE, TILE_SIZE, BufferedImage.TYPE_INT_RGB);
        Random rand = new Random(variation * 54321);

        // Same base color for all wall variations
        int baseR = 145;
        int baseG = 145;
        int baseB = 145;

        // Fill with minimal variation
        for (int y = 0; y < TILE_SIZE; y++) {
            for (int x = 0; x < TILE_SIZE; x++) {
                int var = rand.nextInt(16) - 8; // Small variation
                int r = Math.max(0, Math.min(255, baseR + var));
                int g = Math.max(0, Math.min(255, baseG + var));
                int b = Math.max(0, Math.min(255, baseB + var));
                int rgb = (r << 16) | (g << 8) | b;
                img.setRGB(x, y, rgb);
            }
        }

        return img;
    }
}
