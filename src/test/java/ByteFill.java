import com.badlogic.gdx.graphics.Pixmap;
import warpwriter.ModelMaker;

import java.util.Arrays;

/**
 * @author Ben McLean
 */
public class ByteFill {
    /**
     * @return color
     */
    public static Fill2D fill2D(final byte color) {
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
     * @return yesFill if transparency is greater than 50%, otherwise noFill
     */
    public static Fill3D transparent(final Pixmap pixmap, final Fill3D yesFill, final Fill3D noFill) {
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
     * @return yesFill if transparency is greater than threshold, otherwise noFill
     */
    public static Fill3D transparent(final Pixmap pixmap, final Fill3D yesFill, final Fill3D noFill, final float threshold) {
        return new Fill3D() {
            @Override
            public byte fill(int x, int y, int z) {
                return (pixmap.getPixel(x, y) & 0xFF) / 255f < threshold ? yesFill.fill(x, y, z) : noFill.fill(x, y, z);
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
     * @return yesFill if transparency is greater than threshold, otherwise noFill
     */
    public static Fill3D transparent(final Pixmap pixmap, final Fill3D yesFill, final Fill3D noFill, final byte threshold) {
        return new Fill3D() {
            @Override
            public byte fill(int x, int y, int z) {
                return (pixmap.getPixel(x, y) & 0xFF) < threshold ? yesFill.fill(x, y, z) : noFill.fill(x, y, z);
            }
        };
    }

    /**
     * @param y will be treated as x
     */
    public static Fill fill(final int y, final Fill2D fill) {
        return new Fill() {
            @Override
            public byte fill(int x) {
                return fill.fill(x, y);
            }
        };
    }

    public static Fill fill(final Fill2D fill, final int y) {
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
    public static Fill2D fill2Dy(final Fill3D fill, final int z) {
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
    public static Fill2D fill2D(final int z, final Fill3D fill) {
        return new Fill2D() {
            @Override
            public byte fill(int x, int y) {
                return fill.fill(z, x, y);
            }
        };
    }

    public static Fill2D fill2D(final Fill3D fill, final int z) {
        return new Fill2D() {
            @Override
            public byte fill(int x, int y) {
                return fill.fill(x, y, z);
            }
        };
    }

    public static Fill2D Fill2D(final Fill fill) {
        return new Fill2D() {
            @Override
            public byte fill(int x, int y) {
                return fill.fill(x);
            }
        };
    }

    public static Fill Fill(final Fill2D fill) {
        return new Fill() {
            @Override
            public byte fill(int x) {
                return fill.fill(x, 0);
            }
        };
    }

    public static Fill2D Fill2D(final Fill3D fill) {
        return new Fill2D() {
            @Override
            public byte fill(int x, int y) {
                return fill.fill(x, y, 0);
            }
        };
    }

    public static Fill3D Fill3D(final Fill2D fill) {
        return new Fill3D() {
            @Override
            public byte fill(int x, int y, int z) {
                return fill.fill(x, y);
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
        byte[][] result = new byte[height][bytes.length];
        Arrays.fill(result, bytes);
        return result;
    }

    public static byte[][][] fill3D(byte[][] bytes, int depth) {
        byte[][][] voxels = new byte[depth][bytes.length][bytes[0].length];
        Arrays.fill(voxels, bytes);
        return voxels;
    }

    public static byte[][] fill2D(byte[][] pixels, Fill2D fill) {
        for (int x = 0; x < pixels.length; x++)
            for (int y = 0; y < pixels[0].length; y++) {
                byte pixel = fill.fill(x, y);
                if (pixel != (byte) 0) pixels[x][y] = pixel;
            }
        return pixels;
    }

    public static byte[][][] fill3D(byte[][][] voxels, Fill3D fill) {
        for (int x = 0; x < voxels.length; x++)
            for (int y = 0; y < voxels[0].length; y++)
                for (int z = 0; z < voxels[0][0].length; z++) {
                    byte voxel = fill.fill(x, y, z);
                    if (voxel != (byte) 0) voxels[x][y][z] = voxel;
                }
        return voxels;
    }

    public static Fill3D wireframeBox(final int width, final int height, final int depth, final Fill3D fill) {
        return wireframeBox(width, height, depth, fill, fill3D((byte) 0));
    }

    public static Fill3D wireframeBox(final int width, final int height, final int depth, final Fill3D fillYes, final Fill3D fillNo) {
        return new Fill3D() {
            @Override
            public byte fill(int x, int y, int z) {
                boolean x0 = x == 0, x1 = x == width-1, x2 = x0 || x1, y0 = y == 0, y1 = y == height-1, y2 = y0 || y1, z0 = z == 0, z1 = z == depth-1, z2 = z0 || z1;
                if (x2 && y2 && z2)
                    return fillYes.fill(x, y, z);
                else
                    return fillNo.fill(x, y, z);
            }
        };
    }

    public static Fill3D wireframeBox(final byte[][][] model, final Fill3D fillYes, final Fill3D fillNo) {
        return wireframeBox(model.length, model[0].length, model[0][0].length, fillYes, fillNo);
    }

    public static Fill3D wireframeBox(final byte[][][] model, final Fill3D fill) {
        return wireframeBox(model, fill, fill3D((byte) 0));
    }

    /**
     * @return if preferred is zero then return backup, else return preferred
     */
    public static Fill3D fill3D(final Fill3D preferred, final Fill3D backup) {
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
    public static Fill2D fill2D(final Fill2D preferred, final Fill2D backup) {
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
    public static Fill fill(final Fill preferred, final Fill backup) {
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
    public static Fill fill(final byte color) {
        return new Fill() {
            @Override
            public byte fill(int x) {
                return color;
            }
        };
    }

    /**
     * @return color
     */
    public static Fill3D fill3D(final byte color) {
        return new Fill3D() {
            @Override
            public byte fill(int x, int y, int z) {
                return color;
            }
        };
    }

    /**
     * @return color
     */
    public static Fill3D fill3D(final long seed, final byte... colors) {
        return new Fill3D() {
            @Override
            public byte fill(int x, int y, int z) {
                return colors[ModelMaker.hashBounded(x, y, z, seed, colors.length)];
            }
        };
    }

    public static byte[][][] fill3D(Fill3D fill, int width, int height, int depth) {
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
