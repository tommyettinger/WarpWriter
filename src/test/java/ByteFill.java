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
    public static Fill2D transparent(final Pixmap pixmap, final Fill2D yesFill, final Fill2D noFill) {
        return transparent(pixmap, yesFill, noFill, 0.5f);
    }

    /**
     * @return yesFill if transparency is greater than threshold, otherwise noFill
     */
    public static Fill2D transparent(final Pixmap pixmap, final Fill2D yesFill, final Fill2D noFill, final float threshold) {
        return new Fill2D() {
            @Override
            public byte fill(int x, int y) {
                return (pixmap.getPixel(x, y) & 0xFF) / 255f < threshold ? yesFill.fill(x, y) : noFill.fill(x, y);
            }
        };
    }

    /**
     * @param y will be treated as x
     */
    public static Fill Fill(final int y, final Fill2D fill) {
        return new Fill() {
            @Override
            public byte fill(int x) {
                return fill.fill(x, y);
            }
        };
    }

    public static Fill Fill(final Fill2D fill, final int y) {
        return new Fill() {
            @Override
            public byte fill(int x) {
                return fill.fill(x, y);
            }
        };
    }

    /**
     * @param z will be treated as y
     */
    public static Fill2D Fill2Dy(final Fill3D fill, final int z) {
        return new Fill2D() {
            @Override
            public byte fill(int x, int y) {
                return fill.fill(x, z, y);
            }
        };
    }

    /**
     * @param z will be treated as x
     */
    public static Fill2D Fill2D(final int z, final Fill3D fill) {
        return new Fill2D() {
            @Override
            public byte fill(int x, int y) {
                return fill.fill(z, x, y);
            }
        };
    }

    public static Fill2D Fill2D(final Fill3D fill, final int z) {
        return new Fill2D() {
            @Override
            public byte fill(int x, int y) {
                return fill.fill(x, y, z);
            }
        };
    }

    /**
     * @return yesFill if transparency is greater than threshold, otherwise noFill
     */
    public static Fill2D transparent(final Pixmap pixmap, final Fill2D yesFill, final Fill2D noFill, final byte threshold) {
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

    public static byte[][][] fill3D(ByteFill.Fill2D fill, int width, int height) {
        return fill3D(fill, width, height, 1);
    }

    public static byte[][][] fill3D(ByteFill.Fill2D fill, int width, int height, int depth) {
        return fill3D(fill2D(fill, width, height), depth);
    }

    public static byte[][] pixmap2D(Pixmap pixmap, PaletteReducer reducer) {
        return fill2D(pixmap(pixmap, reducer), pixmap.getWidth(), pixmap.getHeight());
    }

    public static byte[][][] pixmap3D(Pixmap pixmap, PaletteReducer reducer) {
        return pixmap3D(pixmap, reducer, 1);
    }

    public static byte[][][] pixmap3D(Pixmap pixmap, PaletteReducer reducer, int depth) {
        return fill3D(pixmap2D(pixmap, reducer), depth);
    }

    public static byte[][][] fill3D(byte[][] bytes) {
        return fill3D(bytes, 1);
    }

    public static byte[][] fill2D(byte[] bytes, int height) {
        byte[][] result = new byte[bytes.length][height];
        Arrays.fill(result, bytes);
        return result;
    }

    public static byte[][][] fill3D(byte[][] bytes, int depth) {
        byte[][][] voxels = new byte[depth][bytes.length][bytes[0].length];
        Arrays.fill(voxels, bytes);
        return voxels;
    }

    /**
     * @return if preferred is zero then return backup, else return preferred
     */
    public static Fill3D Fill3D(final Fill3D preferred, final Fill3D backup) {
        return new Fill3D() {
            @Override
            public byte fill(int x, int y, int z) {
                byte check = preferred.fill(x, y, z);
                return check == (byte) 0 ? backup.fill(x, y, z) : check;
            }
        };
    }

    /**
     * @return if preferred is zero then return backup, else return preferred
     */
    public static Fill2D Fill2D(final Fill2D preferred, final Fill2D backup) {
        return new Fill2D() {
            @Override
            public byte fill(int x, int y) {
                byte check = preferred.fill(x, y);
                return check == (byte) 0 ? backup.fill(x, y) : check;
            }
        };
    }

    /**
     * @return if preferred is zero then return backup, else return preferred
     */
    public static Fill Fill(final Fill preferred, final Fill backup) {
        return new Fill() {
            @Override
            public byte fill(int x) {
                byte check = preferred.fill(x);
                return check == (byte) 0 ? backup.fill(x) : check;
            }
        };
    }

    /**
     * @return color
     */
    public static Fill Fill(final byte color) {
        return new Fill() {
            @Override
            public byte fill (int x) {
                return color;
            }
        };
    }
    
    /**
     * @return color
     */
    public static Fill3D Fill3D(final byte color) {
        return new Fill3D() {
            @Override
            public byte fill(int x, int y, int z) {
                return color;
            }
        };
    }

    public byte[][][] fill3D(Fill3D fill, int width, int height, int depth) {
        byte[][][] result = new byte[width][height][depth];
        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++)
                for (int z = 0; z < depth; z++)
                    result[x][y][z] = fill.fill(x, y, z);
        return result;
    }

    public interface Fill3D {
        byte fill(int x, int y, int z);
    }

    public interface Fill2D {
        byte fill(int x, int y);
    }

    public interface Fill {
        byte fill(int x);
    }
}
