import com.badlogic.gdx.graphics.Pixmap;
import warpwriter.ModelMaker;

/**
 * @author Ben McLean
 */
public class ByteFill {
    public interface Fill {
        byte fill(int x);
    }

    public interface Fill2D {
        byte fill(int x, int y);
    }

    public interface Fill3D {
        byte fill(int x, int y, int z);
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
     * @return yes if transparency is greater than 50%, otherwise no
     */
    public static Fill2D transparent(final Pixmap pixmap, final Fill2D yes, final Fill2D no) {
        return transparent(pixmap, yes, no, 0.5f);
    }

    /**
     * @return yes if transparency is greater than 50%, otherwise no
     */
    public static Fill3D transparent(final Pixmap pixmap, final Fill3D yes, final Fill3D no) {
        return transparent(pixmap, yes, no, 0.5f);
    }

    /**
     * @return yes if transparency is greater than threshold, otherwise no
     */
    public static Fill2D transparent(final Pixmap pixmap, final Fill2D yes, final Fill2D no, final float threshold) {
        return new Fill2D() {
            @Override
            public byte fill(int x, int y) {
                return (pixmap.getPixel(loop(x, pixmap.getWidth()), loop(y, pixmap.getHeight())) & 0xFF) / 255f < threshold ? yes.fill(x, y) : no.fill(x, y);
            }
        };
    }

    /**
     * @return yes if transparency is greater than threshold, otherwise no
     */
    public static Fill3D transparent(final Pixmap pixmap, final Fill3D yes, final Fill3D no, final float threshold) {
        return new Fill3D() {
            @Override
            public byte fill(int x, int y, int z) {
                return (pixmap.getPixel(loop(y, pixmap.getWidth()), loop(z, pixmap.getHeight())) & 0xFF) / 255f < threshold ? yes.fill(x, y, z) : no.fill(x, y, z);
            }
        };
    }

    /**
     * @return yes if transparency is greater than threshold, otherwise no
     */
    public static Fill2D transparent(final Pixmap pixmap, final Fill2D yes, final Fill2D no, final int threshold) {
        return new Fill2D() {
            @Override
            public byte fill(int x, int y) {
                return (pixmap.getPixel(loop(x, pixmap.getWidth()), loop(y, pixmap.getHeight())) & 0xFF) < threshold ? yes.fill(x, y) : no.fill(x, y);
            }
        };
    }

    /**
     * @return yes if transparency is greater than threshold, otherwise no
     */
    public static Fill3D transparent(final Pixmap pixmap, final Fill3D yes, final Fill3D no, final int threshold) {
        return new Fill3D() {
            @Override
            public byte fill(int x, int y, int z) {
                return (pixmap.getPixel(loop(y, pixmap.getWidth()), loop(z, pixmap.getHeight())) & 0xFF) < threshold ? yes.fill(x, y, z) : no.fill(x, y, z);
            }
        };
    }

    public static Fill mod(final Fill fill, final int divisor) {
        return new Fill() {
            @Override
            public byte fill(int x) {
                return fill.fill(x % divisor);
            }
        };
    }

    public static Fill2D mod(final Fill2D fill, final int divisorX, final int divisorY) {
        return new Fill2D() {
            @Override
            public byte fill(int x, int y) {
                return fill.fill(x % divisorX, y % divisorY);
            }
        };
    }

    public static Fill2D mod(final Fill2D fill, final int divisorY) {
        return new Fill2D() {
            @Override
            public byte fill(int x, int y) {
                return fill.fill(x, y % divisorY);
            }
        };
    }

    public static Fill2D mod(final int divisorX, final Fill2D fill) {
        return new Fill2D() {
            @Override
            public byte fill(int x, int y) {
                return fill.fill(x % divisorX, y);
            }
        };
    }

    public static Fill3D mod(final Fill3D fill, final int divisorX, final int divisorY, final int divisorZ) {
        return new Fill3D() {
            @Override
            public byte fill(int x, int y, int z) {
                return fill.fill(x % divisorX, y % divisorY, z % divisorZ);
            }
        };
    }

    public static Fill3D mod(final int divisorX, final int divisorY, final Fill3D fill) {
        return new Fill3D() {
            @Override
            public byte fill(int x, int y, int z) {
                return fill.fill(x % divisorX, y % divisorY, z);
            }
        };
    }

    public static Fill3D mod(final int divisorX, final Fill3D fill, final int divisorZ) {
        return new Fill3D() {
            @Override
            public byte fill(int x, int y, int z) {
                return fill.fill(x % divisorX, y, z % divisorZ);
            }
        };
    }

    public static Fill3D mod(final Fill3D fill, final int divisorY, final int divisorZ) {
        return new Fill3D() {
            @Override
            public byte fill(int x, int y, int z) {
                return fill.fill(x, y % divisorY, z % divisorZ);
            }
        };
    }

    public static Fill3D mod(final int divisorX, final Fill3D fill) {
        return new Fill3D() {
            @Override
            public byte fill(int x, int y, int z) {
                return fill.fill(x % divisorX, y, z);
            }
        };
    }

    public static Fill3D mod(final Fill3D fill, final int divisorY) {
        return new Fill3D() {
            @Override
            public byte fill(int x, int y, int z) {
                return fill.fill(x, y % divisorY, z);
            }
        };
    }

    public static Fill3D modZ(final Fill3D fill, final int divisorZ) {
        return new Fill3D() {
            @Override
            public byte fill(int x, int y, int z) {
                return fill.fill(x, y, z % divisorZ);
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
    public static Fill2D filly(final Fill3D fill, final int z) {
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
    public static Fill2D fill(final int z, final Fill3D fill) {
        return new Fill2D() {
            @Override
            public byte fill(int x, int y) {
                return fill.fill(z, x, y);
            }
        };
    }

    public static Fill2D fill(final Fill3D fill, final int z) {
        return new Fill2D() {
            @Override
            public byte fill(int x, int y) {
                return fill.fill(x, y, z);
            }
        };
    }

    public static Fill2D fill(final Fill fill) {
        return new Fill2D() {
            @Override
            public byte fill(int x, int y) {
                return fill.fill(x);
            }
        };
    }

    public static Fill fill(final Fill2D fill) {
        return new Fill() {
            @Override
            public byte fill(int x) {
                return fill.fill(x, 0);
            }
        };
    }

    public static Fill2D fill2D(final Fill3D fill) {
        return new Fill2D() {
            @Override
            public byte fill(int x, int y) {
                return fill.fill(x, y, 0);
            }
        };
    }

    public static Fill3D fill3D(final Fill2D fill) {
        return new Fill3D() {
            @Override
            public byte fill(int x, int y, int z) {
                return fill.fill(x, y);
            }
        };
    }

    /**
     * @return x and y are reversed
     */
    public static Fill2D fillY(final Fill2D fill) {
        return new Fill2D() {
            @Override
            public byte fill(int x, int y) {
                return fill.fill(y, x);
            }
        };
    }

    public static Fill3D fillXZY(final Fill3D fill) {
        return new Fill3D() {
            @Override
            public byte fill(int x, int y, int z) {
                return fill.fill(x, z, y);
            }
        };
    }

    public static Fill3D fillYXZ(final Fill3D fill) {
        return new Fill3D() {
            @Override
            public byte fill(int x, int y, int z) {
                return fill.fill(y, x, z);
            }
        };
    }

    public static Fill3D fillYZX(final Fill3D fill) {
        return new Fill3D() {
            @Override
            public byte fill(int x, int y, int z) {
                return fill.fill(y, z, x);
            }
        };
    }

    public static Fill3D fillZXY(final Fill3D fill) {
        return new Fill3D() {
            @Override
            public byte fill(int x, int y, int z) {
                return fill.fill(z, x, y);
            }
        };
    }

    public static Fill3D fillZYX(final Fill3D fill) {
        return new Fill3D() {
            @Override
            public byte fill(int x, int y, int z) {
                return fill.fill(z, y, x);
            }
        };
    }

    /**
     * @return yes if pixel from pixmap matches color, otherwise no
     */
    public static Fill2D colorMatch(final Pixmap pixmap, final int color, final Fill2D yes, final Fill2D no) {
        return new Fill2D() {
            @Override
            public byte fill(int x, int y) {
                return pixmap.getPixel(x, y) == color ? yes.fill(x, y) : no.fill(x, y);
            }
        };
    }

    public static byte[][] fill(ByteFill.Fill2D fill, int width, int height) {
        byte[][] result = new byte[width][height];
        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++)
                result[x][y] = fill.fill(x, y);
        return result;
    }

    public static byte[][][] fill3D(ByteFill.Fill2D fill, int width, int height) {
        return fill(fill, width, height, 1);
    }

    public static byte[][][] fill(ByteFill.Fill2D fill, int width, int height, int depth) {
        return fill(fill(fill, width, height), depth);
    }

    public static byte[][] pixmap2D(Pixmap pixmap, PaletteReducer reducer) {
        return fill(pixmap(pixmap, reducer), pixmap.getWidth(), pixmap.getHeight());
    }

    public static byte[][][] pixmap3D(Pixmap pixmap, PaletteReducer reducer) {
        return pixmap3D(pixmap, reducer, 1);
    }

    public static byte[][][] pixmap3D(Pixmap pixmap, PaletteReducer reducer, int depth) {
        return fill(pixmap2D(pixmap, reducer), depth);
    }

    public static byte[][][] fill3D(byte[][] bytes) {
        return fill(bytes, 1);
    }

    public static byte[][] fill(byte[] bytes, int height) {
        byte[][] result = new byte[height][bytes.length];
        for (int x = 0; x < bytes.length; x++) {
            System.arraycopy(bytes, 0, result[x], 0, bytes.length);
        }
        return result;
    }

    /**
     * Copies the given 2D array of voxel bytes into each depth-slice of a new 3D array.
     * <br>
     * Note, MagicaVoxel uses an unusual convention where x is forward/back, y is left/right, and z is up/down.
     *
     * @param bytes a 2D array of bytes representing voxel palette indices
     * @param depth the depth to copy the bytes
     * @return a new 3D array containing copies of bytes, depth-thick
     */
    public static byte[][][] fill(byte[][] bytes, int depth) {
        byte[][][] voxels = new byte[depth][bytes.length][bytes[0].length];
        for (int x = 0; x < depth; x++) {
            for (int y = 0; y < bytes.length; y++) {
                System.arraycopy(bytes[y], 0, voxels[x][y], 0, bytes[y].length);
            }
        }
        return voxels;
    }

    public static byte[] fill(byte[] pixels, Fill fill) {
        for (int x = 0; x < pixels.length; x++) {
            byte pixel = fill.fill(x);
            if (pixel != (byte) 0) pixels[x] = pixel;
        }
        return pixels;
    }

    public static byte[][] fill(byte[][] pixels, Fill2D fill) {
        for (int x = 0; x < pixels.length; x++)
            for (int y = 0; y < pixels[0].length; y++) {
                byte pixel = fill.fill(x, y);
                if (pixel != (byte) 0) pixels[x][y] = pixel;
            }
        return pixels;
    }

    public static byte[][][] fill(byte[][][] voxels, Fill3D fill) {
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

    public static Fill3D wireframeBox(final int width, final int height, final int depth, final Fill3D yes, final Fill3D no) {
        return new Fill3D() {
            @Override
            public byte fill(int x, int y, int z) {
                boolean x0 = x == 0, x1 = x == width - 1, x2 = x0 || x1,
                        y0 = y == 0, y1 = y == height - 1, y2 = y0 || y1,
                        z0 = z == 0, z1 = z == depth - 1, z2 = z0 || z1;
                if ((x2 && y2) || (x2 && z2) || (y2 && z2))
                    return yes.fill(x, y, z);
                else
                    return no.fill(x, y, z);
            }
        };
    }

    public static Fill3D wireframeBox(final byte[][][] model, final Fill3D yes, final Fill3D no) {
        return wireframeBox(model.length, model[0].length, model[0][0].length, yes, no);
    }

    public static Fill3D wireframeBox(final byte[][][] model, final Fill3D fill) {
        return wireframeBox(model, fill, fill3D((byte) 0));
    }

    public static Fill offset(final Fill fill, final int xOffset) {
        return new Fill() {
            @Override
            public byte fill(int x) {
                return fill.fill(x + xOffset);
            }
        };
    }

    public static Fill2D offset(final Fill2D fill, final int xOffset, final int yOffset) {
        return new Fill2D() {
            @Override
            public byte fill(int x, int y) {
                return fill.fill(x + xOffset, y + yOffset);
            }
        };
    }

    public static Fill3D offset(final Fill3D fill, final int xOffset, final int yOffset, final int zOffset) {
        return new Fill3D() {
            @Override
            public byte fill(int x, int y, int z) {
                return fill.fill(x + xOffset, y + yOffset, z + zOffset);
            }
        };
    }

    /**
     * @return if preferred is zero then return backup, else return preferred
     */
    public static Fill3D fill(final Fill3D preferred, final Fill3D backup) {
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
    public static Fill2D fill(final Fill2D preferred, final Fill2D backup) {
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
    public static Fill2D fill2D(final byte color) {
        return new Fill2D() {
            @Override
            public byte fill(int x, int y) {
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
     * @return deterministic random noise
     */
    public static Fill3D noise3D(final long seed, final byte... colors) {
        return new Fill3D() {
            @Override
            public byte fill(int x, int y, int z) {
                return colors[ModelMaker.hashBounded(x, y, z, seed, colors.length)];
            }
        };
    }

    public static byte[][][] fill(Fill3D fill, int width, int height, int depth) {
        byte[][][] result = new byte[width][height][depth];
        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++)
                for (int z = 0; z < depth; z++)
                    result[x][y][z] = fill.fill(x, y, z);
        return result;
    }

    /**
     * @param stripes The height of each stripe. All should be positive
     * @param fills   What to fill each stripe with. Is expected to be the same size as stripes
     */
    public static Fill stripes(final int[] stripes, final Fill[] fills) {
        return new Fill() {
            int repeat = repeat();

            int repeat() {
                repeat = 0;
                for (int stripe : stripes)
                    repeat += stripe;
                return repeat;
            }

            @Override
            public byte fill(int x) {
                int xStep = loop(x, repeat);
                int step = 0;
                for (int i = 0; i < stripes.length; i++)
                    if (step <= xStep)
                        step += stripes[i];
                    else
                        return fills[i].fill(x);
                return fills[0].fill(x);
            }
        };
    }

    /**
     * @param stripes The height of each stripe. All should be positive
     * @param fills   What to fill each stripe with. Is expected to be the same size as stripes
     */
    public static Fill2D stripes(final int[] stripes, final Fill2D[] fills) {
        return new Fill2D() {
            int repeat = repeat();

            int repeat() {
                repeat = 0;
                for (int stripe : stripes)
                    repeat += stripe;
                return repeat;
            }

            @Override
            public byte fill(int x, int y) {
                int xStep = loop(x, repeat);
                int step = 0;
                for (int i = 0; i < stripes.length; i++)
                    if (step <= xStep)
                        step += stripes[i];
                    else
                        return fills[i].fill(x, y);
                return fills[0].fill(x, y);
            }
        };
    }

    /**
     * @param stripes The height of each stripe. All should be positive
     * @param fills   What to fill each stripe with. Is expected to be the same size as stripes
     */
    public static Fill3D stripes(final int[] stripes, final Fill3D[] fills) {
        return new Fill3D() {
            int repeat = repeat();

            int repeat() {
                repeat = 0;
                for (int stripe : stripes)
                    repeat += stripe;
                return repeat;
            }

            @Override
            public byte fill(int x, int y, int z) {
                int xStep = loop(x, repeat);
                int step = 0;
                for (int i = 0; i < stripes.length; i++)
                    if (step <= xStep)
                        step += stripes[i];
                    else
                        return fills[i].fill(x, y, z);
                return fills[0].fill(x, y, z);
            }
        };
    }

    public static Fill2D checkers(Fill2D white, Fill2D black, int size) {
        return checkers(white, black, size, size);
    }

    public static Fill2D checkers(Fill2D white, Fill2D black, int x, int y) {
        return checkers(white, black, x, x, y, y);
    }

    public static Fill2D checkers(Fill2D white, Fill2D black, int x1, int x2, int y1, int y2) {
        return checkers(white, black, new int[]{x1, x2}, new int[]{y1, y2});
    }

    /**
     * @param x Two positive width values for columns
     * @param y Two positive height values for rows
     */
    public static Fill2D checkers(Fill2D white, Fill2D black, int[] x, int[] y) {
        return stripes(x, new Fill2D[]{
                fillY(stripes(y, new Fill2D[]{white, black})),
                fillY(stripes(y, new Fill2D[]{black, white}))
        });
    }

    /**
     * @param x Two positive width values for columns
     * @param y Two positive height values for rows
     */
    public static Fill3D checkers(Fill3D white, Fill3D black, int[] x, int[] y) {
        return checkers(new Fill3D[]{white, black}, new Fill3D[]{black, white}, x, y);
    }

    /**
     * @param x Two positive width values for columns
     * @param y Two positive height values for rows
     */
    public static Fill3D checkers(Fill3D[] a, Fill3D[] b, int[] x, int[] y) {
        return stripes(x, new Fill3D[]{
                fillYXZ(stripes(y, a)),
                fillYXZ(stripes(y, b))
        });
    }

    public static Fill3D checkers(Fill3D white, Fill3D black, int size) {
        return checkers(white, black, size, size, size);
    }

    public static Fill3D checkers(Fill3D white, Fill3D black, int x, int y, int z) {
        return checkers(white, black, x, x, y, y, z, z);
    }

    public static Fill3D checkers(Fill3D white, Fill3D black, int x1, int x2, int y1, int y2, int z1, int z2) {
        return checkers(white, black, new int[]{x1, x2}, new int[]{y1, y2}, new int[]{z1, z2});
    }

    /**
     * @param x Two positive width values for columns
     * @param y Two positive height values for rows
     * @param z Two positive depth values for layers
     */
    public static Fill3D checkers(Fill3D white, Fill3D black, int[] x, int[] y, int[] z) {
        Fill3D[] a = new Fill3D[]{white, black};
        Fill3D[] b = new Fill3D[]{black, white};
        return stripes(
                x,
                new Fill3D[]{
                        fillYZX(checkers(a, b, y, z)),
                        fillYZX(checkers(b, a, y, z))
                }
        );
    }

    public static Fill2D skew(final Fill2D fill, final float skewX) {
        return new Fill2D() {
            @Override
            public byte fill(int x, int y) {
                return fill.fill(x + (int) (y * skewX), y);
            }
        };
    }

    public static Fill3D skew(final Fill3D fill, final float skewX) {
        return new Fill3D() {
            @Override
            public byte fill(int x, int y, int z) {
                return fill.fill(x + (int) (y * skewX), y, z);
            }
        };
    }

    public static Fill fill(final byte[] src) {
        return new Fill() {
            @Override
            public byte fill(int x) {
                return src[loop(x, src.length)];
            }
        };
    }

    public static Fill2D fill(final byte[][] src) {
        return new Fill2D() {
            @Override
            public byte fill(int x, int y) {
                int x2 = loop(x, src.length);
                return src[x2][loop(y, src[x2].length)];
            }
        };
    }

    public static Fill3D fill(final byte[][][] src) {
        return new Fill3D() {
            @Override
            public byte fill(int x, int y, int z) {
                int x2 = loop(x, src.length), y2 = loop(y, src[x2].length);
                return src[x2][y2][loop(z, src[x2][y2].length)];
            }
        };
    }

    public static int loop (int dividend, int divisor) {
        return dividend < 0 ? divisor - Math.abs(dividend % divisor) : dividend % divisor;
    }
}
