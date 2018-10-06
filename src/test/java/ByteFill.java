import com.badlogic.gdx.graphics.Pixmap;

import java.util.Arrays;

/**
 * @author Ben McLean
 */
public class ByteFill {
    /**
     * @return color
     */
    public static Fill2D Fill2D(final byte color) {
        return new Fill2D() {
            @Override
            public byte fill(int x, int y) {
                return color;
            }
        };
    }

    /**
     * @return nearest color in the PaletteReducer to the color from the pixmap
     */
    public static Fill2D pixmap(final Pixmap pixmap, final PaletteReducer reducer) {
        return new Fill2D() {
            @Override
            public byte fill(int x, int y) {
                return reducer.paletteMapping[pixmap.getPixel(x, y)];
            }
        };
    }

    /**
     * @return yesFill if transparency is greater than 50%, otherwise noFill
     */
    public static Fill2D transparent2D(final Pixmap pixmap, final Fill2D yesFill, final Fill2D noFill) {
        return transparent2D(pixmap, yesFill, noFill, 0.5f);
    }

    /**
     * @return yesFill if transparency is greater than threshold, otherwise noFill
     */
    public static Fill2D transparent2D(final Pixmap pixmap, final Fill2D yesFill, final Fill2D noFill, final float threshold) {
        return new Fill2D() {
            @Override
            public byte fill(int x, int y) {
                return (pixmap.getPixel(x, y) & 0xFF) / 255f < threshold ? yesFill.fill(x, y) : noFill.fill(x, y);
            }
        };
    }

    /**
     * @return yesFill if transparency is greater than threshold, otherwise noFill
     */
    public static Fill2D transparent2D(final Pixmap pixmap, final Fill2D yesFill, final Fill2D noFill, final int threshold) {
        return new Fill2D() {
            @Override
            public byte fill(int x, int y) {
                return (pixmap.getPixel(x, y) & 0xFF) < threshold ? yesFill.fill(x, y) : noFill.fill(x, y);
            }
        };
    }

    /**
     * @return yesFill if pixel from pixmap matches color, otherwise noFill
     */
    public static Fill2D colorMatch(final Pixmap pixmap, final int color, final Fill2D yesFill, final Fill2D noFill) {
        return new Fill2D() {
            @Override
            public byte fill(int x, int y) {
                return pixmap.getPixel(x, y) == color ? yesFill.fill(x, y) : noFill.fill(x, y);
            }
        };
    }

    public static byte[][] fill2D(ByteFill.Fill2D fill, int width, int height) {
        byte[][] result = new byte[width][height];
        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++)
                result[x][y] = fill.fill(x, y);
        return result;
    }

    public static byte[][][] fillToVoxels(ByteFill.Fill2D fill, int width, int height) {
        return fillToVoxels(fill, width, height, 1);
    }

    public static byte[][][] fillToVoxels(ByteFill.Fill2D fill, int width, int height, int depth) {
        return voxels2D(fill2D(fill, width, height), depth);
    }

    public static byte[][] pixmap2D(Pixmap pixmap, PaletteReducer reducer) {
        return fill2D(pixmap(pixmap, reducer), pixmap.getWidth(), pixmap.getHeight());
    }

    public static byte[][][] voxels2D(byte[][] bytes) {
        return voxels2D(bytes, 1);
    }

    public static byte[][][] voxels2D(byte[][] bytes, int depth) {
        byte[][][] voxels = new byte[depth][bytes.length][bytes[0].length];
        Arrays.fill(voxels, bytes);
        return voxels;
    }

    public interface Fill2D {
        byte fill(int x, int y);
    }
}
