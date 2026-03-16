import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Random;

/**
 * Generates 8 animated grass frames (grass_anim0.png ... grass_anim7.png, 64x32 each).
 * Each frame shows grass blades at a different horizontal sway position, pivoting at
 * the base of the diamond. Frames 0-7 cover one full sine cycle.
 *
 * Run from the project root: javac GrassAnimGenerator.java && java GrassAnimGenerator
 */
public class GrassAnimGenerator {
    private static final int ISO_WIDTH  = 64;
    private static final int ISO_HEIGHT = 32;
    private static final int FRAMES     = 8;
    private static final String ASSETS_DIR = "assets";

    // Base grass color (matches TextureGenerator)
    private static final int BASE_R = 135;
    private static final int BASE_G = 177;
    private static final int BASE_B = 87;

    public static void main(String[] args) throws Exception {
        System.out.println("Generating animated grass frames...");
        new File(ASSETS_DIR).mkdirs();

        for (int frame = 0; frame < FRAMES; frame++) {
            BufferedImage img = generateAnimFrame(frame);
            File out = new File(ASSETS_DIR, "grass_anim" + frame + ".png");
            ImageIO.write(img, "PNG", out);
            System.out.println("Generated: " + out.getAbsolutePath());
        }

        System.out.println("Done! " + FRAMES + " animated grass frames in " + ASSETS_DIR + "/");
    }

    /**
     * Generate one 64x32 animated grass frame.
     *
     * Sway logic:
     * - shift = (int)(MAX_SHIFT * sin(frame/FRAMES * 2π))  →  -3..+3 pixels at tip
     * - rowShift(y) = shift * max(0, (y - 16) / 16.0)     →  zero at equator, full at tip
     * - For each output pixel (ox, oy): read source color from (ox - rowShift, oy)
     *   If the source is outside the diamond, clamp to nearest in-diamond column.
     *
     * Colors use a per-(x,y) seed so the same pixel has the same color in every frame
     * (no color flicker during animation, only position changes).
     */
    private static BufferedImage generateAnimFrame(int frame) {
        BufferedImage img = new BufferedImage(ISO_WIDTH, ISO_HEIGHT, BufferedImage.TYPE_INT_ARGB);

        // Sway amount at the tip for this frame (-3..+3 pixels)
        double angle = (double) frame / FRAMES * 2.0 * Math.PI;
        int tipShift = (int) Math.round(3.0 * Math.sin(angle));

        for (int oy = 0; oy < ISO_HEIGHT; oy++) {
            if (!rowHasAnyDiamondPixel(oy)) continue;

            // Sway gradient: zero below equator (y < 16), full at tip (y = 32)
            float gradient = Math.max(0f, (oy - 16) / 16.0f);
            int rowShift = (int) Math.round(tipShift * gradient);

            for (int ox = 0; ox < ISO_WIDTH; ox++) {
                if (!isInsideDiamond(ox, oy)) {
                    img.setRGB(ox, oy, 0x00000000);
                    continue;
                }

                // Source column: shift pixel source, clamped inside diamond
                int sx = clampToDiamond(ox - rowShift, oy);
                img.setRGB(ox, oy, grassColor(sx, oy));
            }
        }

        return img;
    }

    /** Returns the ARGB grass color for a given pixel position, using a fixed per-(x,y) seed. */
    private static int grassColor(int x, int y) {
        Random rand = new Random((long) x * 1000 + y);
        if (rand.nextFloat() < 0.98f) {
            int var = rand.nextInt(12) - 6;
            int r = clamp(BASE_R + var);
            int g = clamp(BASE_G + var);
            int b = clamp(BASE_B + var);
            return (255 << 24) | (r << 16) | (g << 8) | b;
        } else {
            // Dirt spots (matches TextureGenerator)
            int var = rand.nextInt(10) - 5;
            int r = clamp(110 + var);
            int g = clamp(100 + var);
            int b = clamp(75  + var);
            return (255 << 24) | (r << 16) | (g << 8) | b;
        }
    }

    /**
     * Clamp x to the leftmost/rightmost in-diamond column at row y.
     * Used when a shifted pixel overshoots the diamond boundary.
     */
    private static int clampToDiamond(int x, int y) {
        // The diamond at row y spans [minX, maxX]
        // From isInsideDiamond: |px-32|/32 + |py-16|/16 <= 1
        // => |px-32|/32 <= 1 - |py-16|/16
        // => |px-32| <= 32 * (1 - |py-16|/16)
        float halfWidth = 32.0f * (1.0f - Math.abs(y - 16) / 16.0f);
        int minX = (int) Math.ceil(32 - halfWidth);
        int maxX = (int) Math.floor(32 + halfWidth);
        return Math.max(minX, Math.min(maxX, x));
    }

    /** True if the given row y contains at least one diamond pixel. */
    private static boolean rowHasAnyDiamondPixel(int y) {
        return Math.abs(y - 16) <= 16;
    }

    /** Standard isometric diamond test: center (32,16), axes 32 and 16. */
    private static boolean isInsideDiamond(int px, int py) {
        float dx = Math.abs(px - 32) / 32.0f;
        float dy = Math.abs(py - 16) / 16.0f;
        return dx + dy <= 1.0f;
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }
}
