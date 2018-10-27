package warpwriter;

import java.util.Arrays;

/**
 * Just laying some foundation for 3D array manipulation.
 * Created by Tommy Ettinger on 11/2/2017.
 */
public class Tools3D {

    public static byte[][][] deepCopy(byte[][][] voxels)
    {
        int xs, ys, zs;
        byte[][][] next = new byte[xs = voxels.length][ys = voxels[0].length][zs = voxels[0][0].length];
        for (int x = 0; x < xs; x++) {
            for (int y = 0; y < ys; y++) {
                System.arraycopy(voxels[x][y], 0, next[x][y], 0, zs);
            }
        }
        return next;
    }

    public static byte[][][] deepCopyInto(byte[][][] voxels, byte[][][] target)
    {
        final int xs = voxels.length, ys = voxels[0].length, zs = voxels[0][0].length;
        for (int x = 0; x < xs; x++) {
            for (int y = 0; y < ys; y++) {
                System.arraycopy(voxels[x][y], 0, target[x][y], 0, zs);
            }
        }
        return target;
    }
    public static void fill(byte[][][] array3d, int value) {
        final int depth = array3d.length;
        final int breadth = depth == 0 ? 0 : array3d[0].length;
        final int height = breadth == 0 ? 0 : array3d[0][0].length;
        if(depth > 0 && breadth > 0) {
            Arrays.fill(array3d[0][0], (byte)value);
        }
        for (int y = 1; y < breadth; y++) {
            System.arraycopy(array3d[0][0], 0, array3d[0][y], 0, height);
        }
        for (int x = 1; x < depth; x++) {
            for (int y = 0; y < breadth; y++) {
                System.arraycopy(array3d[0][0], 0, array3d[x][y], 0, height);
            }
        }
    }


    public static byte[][][] rotate(byte[][][] voxels, int turns)
    {
        int xs, ys, zs;
        byte[][][] next = new byte[xs = voxels.length][ys = voxels[0].length][zs = voxels[0][0].length];
        switch (turns & 3)
        {
            case 0:
                return deepCopy(voxels);
            case 1:
            {
                for (int x = 0; x < xs; x++) {
                    for (int y = 0; y < ys; y++) {
                        System.arraycopy(voxels[y][xs - 1 - x], 0, next[x][y], 0, zs);
                    }
                }
            }
            break;
            case 2:
            {
                for (int x = 0; x < xs; x++) {
                    for (int y = 0; y < ys; y++) {
                        System.arraycopy(voxels[xs - 1 - x][ys - 1 - y], 0, next[x][y], 0, zs);
                    }
                }
            }
            break;
            case 3:
            {
                for (int x = 0; x < xs; x++) {
                    for (int y = 0; y < ys; y++) {
                        System.arraycopy(voxels[ys - 1 - y][x], 0, next[x][y], 0, zs);
                    }
                }
            }
            break;
        }
        return next;
    }

    public static byte[][][] mirrorX(byte[][][] voxels)
    {
        int xs, ys, zs;
        byte[][][] next = new byte[(xs = voxels.length) << 1][ys = voxels[0].length][zs = voxels[0][0].length];
        for (int x = 0; x < xs; x++) {
            for (int y = 0; y < ys; y++) {
                System.arraycopy(voxels[x][y], 0, next[x][y], 0, zs);
                System.arraycopy(voxels[x][y], 0, next[(xs << 1) - 1 - x][y], 0, zs);
            }
        }
        return next;
    }

    public static byte[][][] mirrorY(byte[][][] voxels)
    {
        int xs, ys, zs;
        byte[][][] next = new byte[xs = voxels.length][(ys = voxels[0].length) << 1][zs = voxels[0][0].length];
        for (int x = 0; x < xs; x++) {
            for (int y = 0; y < ys; y++) {
                System.arraycopy(voxels[x][y], 0, next[x][y], 0, zs);
                System.arraycopy(voxels[x][y], 0, next[x][(ys << 1) - 1 - y], 0, zs);
            }
        }
        return next;
    }

    public static byte[][][] mirrorXY(byte[][][] voxels)
    {
        int xs, ys, zs;
        byte[][][] next = new byte[(xs = voxels.length) << 1][(ys = voxels[0].length) << 1][zs = voxels[0][0].length];
        for (int x = 0; x < xs; x++) {
            for (int y = 0; y < ys; y++) {
                System.arraycopy(voxels[x][y], 0, next[x][y], 0, zs);
                System.arraycopy(voxels[x][y], 0, next[(xs << 1) - 1 - x][y], 0, zs);
                System.arraycopy(voxels[x][y], 0, next[x][(ys << 1) - 1 - y], 0, zs);
                System.arraycopy(voxels[x][y], 0, next[(xs << 1) - 1 - x][(ys << 1) - 1 - y], 0, zs);
            }
        }
        return next;
    }


    public static int countNot(byte[][][] voxels, int avoid)
    {
        final int xs = voxels.length, ys = voxels[0].length, zs = voxels[0][0].length;
        int c = 0;
        for (int x = 0; x < xs; x++) {
            for (int y = 0; y < ys; y++) {
                for (int z = 0; z < zs; z++) {
                    c += (voxels[x][y][z] != avoid) ? 1 : 0;
                }
            }
        }
        return c;
    }
    public static int count(byte[][][] voxels)
    {
        return countNot(voxels, 0);
    }
    public static int count(byte[][][] voxels, int match)
    {
        final int xs = voxels.length, ys = voxels[0].length, zs = voxels[0][0].length;
        int c = 0;
        for (int x = 0; x < xs; x++) {
            for (int y = 0; y < ys; y++) {
                for (int z = 0; z < zs; z++) {
                    c+= (voxels[x][y][z] == match) ? 1 : 0;
                }
            }
        }
        return c;
    }
    public static byte[][][] runCA(byte[][][] voxels, int smoothLevel)
    {
        if(smoothLevel < 1)
            return voxels;
        final int xs = voxels.length, ys = voxels[0].length, zs = voxels[0][0].length;
        //Dictionary<byte, int> colorCount = new Dictionary<byte, int>();
        int[] colorCount = new int[256];
        byte[][][] vs0 = deepCopy(voxels), vs1 = new byte[xs][ys][zs];
        for(int v = 0; v < smoothLevel; v++)
        {
            if(v >= 1)
            {
                deepCopyInto(vs1, vs0);
                //fetch(vs1, (byte) 0);
            }
            for(int x = 0; x < xs; x++)
            {
                for(int y = 0; y < ys; y++)
                {
                    for(int z = 0; z < zs; z++)
                    {
                        Arrays.fill(colorCount, 0);
                        if(x == 0 || y == 0 || z == 0 || x == xs - 1 || y == ys - 1 || z == zs - 1 || vs0[x][y][z] == 2)
                        {
                            colorCount[vs0[x][y][z] & 255] = 10000;
                            colorCount[0] = -100000;
                        }
                        else
                        {
                            for(int xx = -1; xx < 2; xx++)
                            {
                                for(int yy = -1; yy < 2; yy++)
                                {
                                    for(int zz = -1; zz < 2; zz++)
                                    {
                                        byte smallColor = vs0[x + xx][y + yy][z + zz];
                                        colorCount[smallColor & 255]++;
                                    }
                                }
                            }
                        }
                        if(colorCount[0] >= 23)
                        {
                            vs1[x][y][z] = 0;
                        }
                        else
                        {
                            byte max = 0;
                            int cc = colorCount[0] / 3, tmp;
                            for(byte idx = 1; idx != 0; idx++)
                            {
                                tmp = colorCount[idx & 255];
                                if(tmp > 0 && tmp > cc)
                                {
                                    cc = tmp;
                                    max = idx;
                                }
                            }
                            vs1[x][y][z] = max;
                        }
                    }
                }
            }
        }
        return vs1;
    }

    public static int firstTight(byte[][][] voxels)
    {
        final int xs = voxels.length, ys = voxels[0].length, zs = voxels[0][0].length;
        for (int x = 0; x < xs; x++) {
            for (int y = 0; y < ys; y++) {
                for (int z = 0; z < zs; z++) {
                    if (voxels[x][y][z] != 0)
                        return zs * (x * ys + y) + z;
                }
            }
        }
        return -1;
    }

    public static void findConnectors(byte[][][] voxels, int[] connectors)
    {
        Arrays.fill(connectors, -1);
        int curr;
        final int xs = voxels.length, ys = voxels[0].length, zs = voxels[0][0].length;
        for (int x = 0; x < xs; x++) {
            for (int y = 0; y < ys; y++) {
                for (int z = 0; z < zs; z++) {
                    curr = voxels[x][y][z] & 255;
                    if(curr >= 8 && curr < 16)
                        connectors[curr - 8] = zs * (x * ys + y) + z;
                    else if(curr >= 136 && curr < 144)
                        connectors[curr - 128] = zs * (x * ys + y) + z;
                }
            }
        }
    }
    public static int flood(byte[][][] base, byte[][][] bounds)
    {
        final int xs = base.length, ys = base[0].length, zs = base[0][0].length;
        int size = count(base), totalSize = 0;
        /*
        for (int x = 0; x < xs; x++) {
            for (int y = 0; y < ys; y++) {
                for (int z = 0; z < zs; z++) {
                    if(base[x][y][z] != 0 && bounds[x][y][z] != 0)
                        size++;
                }
            }
        }
        */

        byte[][][] nx = deepCopy(base);
        byte t;
        do {
            totalSize += size;
            size = 0;
            for (int x = 0; x < xs; x++) {
                for (int y = 0; y < ys; y++) {
                    for (int z = 0; z < zs; z++) {
                        if (nx[x][y][z] != 0 && (t = bounds[x][y][z]) != 0) {
                            nx[x][y][z] = t;
                            //++size;
                            if (x > 0 && nx[x - 1][y][z] == 0 && (t = bounds[x - 1][y][z]) != 0) {
                                nx[x - 1][y][z] = t;
                                ++size;
                            }
                            if (x < xs - 1 && nx[x + 1][y][z] == 0 && (t = bounds[x + 1][y][z]) != 0) {
                                nx[x + 1][y][z] = t;
                                ++size;
                            }
                            if (y > 0 && nx[x][y - 1][z] == 0 && (t = bounds[x][y - 1][z]) != 0) {
                                nx[x][y - 1][z] = t;
                                ++size;
                            }
                            if (y < ys - 1 && nx[x][y + 1][z] == 0 && (t = bounds[x][y + 1][z]) != 0) {
                                nx[x][y + 1][z] = t;
                                ++size;
                            }
                            if (z > 0 && nx[x][y][z - 1] == 0 && (t = bounds[x][y][z - 1]) != 0) {
                                nx[x][y][z - 1] = t;
                                ++size;
                            }
                            if (z < zs - 1 && nx[x][y][z + 1] == 0 && (t = bounds[x][y][z + 1]) != 0) {
                                nx[x][y][z + 1] = t;
                                ++size;
                            }
                        }
                    }
                }
            }
        } while (size != 0);
        deepCopyInto(nx, base);
        return totalSize + size;
    }

    public static byte[][][] largestPart(byte[][][] voxels)
    {
        final int xs = voxels.length, ys = voxels[0].length, zs = voxels[0][0].length;
        int fst = firstTight(voxels), bestSize = 0, currentSize, x, y, z;

        byte[][][] remaining = deepCopy(voxels), filled = new byte[xs][ys][zs],
                choice = new byte[xs][ys][zs];
        while (fst >= 0) {
            fill(filled, 0);
            filled[x = fst / (ys * zs)][y = (fst / zs) % ys][z = fst % zs] = voxels[x][y][z];
            currentSize = flood(filled, remaining);
            if(currentSize > bestSize)
            {
                bestSize = currentSize;
                deepCopyInto(filled, choice);
            }

            for (x = 0; x < xs; x++) {
                for (y = 0; y < ys; y++) {
                    for (z = 0; z < zs; z++) {
                        if(filled[x][y][z] != 0)
                            remaining[x][y][z] = 0;
                    }
                }
            }
            fst = firstTight(remaining);
        }
        return choice;
    }

    public static byte[][][] translateCopy(byte[][][] voxels, int xMove, int yMove, int zMove)
    {
        int xs, ys, zs;
        byte[][][] next = new byte[xs = voxels.length][ys = voxels[0].length][zs = voxels[0][0].length];
        final int xLimit = xs - Math.abs(xMove), xStart = Math.max(0, -xMove);
        final int yLimit = ys - Math.abs(yMove), yStart = Math.max(0, -yMove);
        final int zLimit = zs - Math.abs(zMove), zStart = Math.max(0, -zMove);
        if(zLimit <= 0)
            return next;
        for (int x = xStart, xx = 0; x < xs && xx < xLimit && xx < xs; x++, xx++) {
            for (int y = yStart, yy = 0; y < ys && yy < yLimit && yy < ys; y++, yy++) {
                System.arraycopy(voxels[x][y], 0, next[xx][yy], zStart, zLimit);
            }
        }
        return next;

    }

    public static void translateCopyInto(byte[][][] voxels, byte[][][] into, int xMove, int yMove, int zMove)
    {
        int xs, ys, zs;
        xs = voxels.length;
        ys = voxels[0].length;
        zs = voxels[0][0].length;
        final int xLimit = xs - Math.abs(xMove), xStart = Math.max(0, xMove);
        final int yLimit = ys - Math.abs(yMove), yStart = Math.max(0, yMove);
        final int zLimit = zs - Math.abs(zMove), zStart = Math.max(0, zMove);
        for (int x = xStart, xx = 0; x < xs && xx < xLimit && xx < xs; x++, xx++) {
            for (int y = yStart, yy = 0; y < ys && yy < yLimit && yy < ys; y++, yy++) {
                for (int z = zStart, zz = 0; z < zs && zz < zLimit && zz < zs; z++, zz++) {
                    if(into[x][y][z] == 0 && voxels[xx][yy][zz] != 0)
                        into[x][y][z] = voxels[xx][yy][zz];
                }
            }
        }
    }

    private static long hash64(final byte[] data) {
        long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result ^= (z += (data[i] ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L);
        }
        result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
        result ^= result >>> 25 ^ z ^ z >>> 29;
//        result = (result ^ result >>> 33) * 0xFF51AFD7ED558CCDL;
//        result = (result ^ result >>> 33) * 0xC4CEB9FE1A85EC53L;
        return (result ^ result >>> 33);
    }

    private static long hash64(final byte[][] data) {
        long result = 0x9E3779B97F4A7C15L, z = 0xC6BC279692B5CC83L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result ^= (z += hash64(data[i]) + i);
        }
        result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
        result ^= result >>> 25 ^ z ^ z >>> 29;
//        result = (result ^ result >>> 33) * 0xFF51AFD7ED558CCDL;
//        result = (result ^ result >>> 33) * 0xC4CEB9FE1A85EC53L;
        return (result ^ result >>> 33);
    }
    /**
     * Gets a 64-bit high-quality hash of the given 3D byte array. When GWT is a possible target, you should prefer
     * {@link #hash(byte[][][])} if possible, since it's quite a lot faster to work with ints there.
     * @param data a 3D byte array; if null this returns 0, but if any sub-arrays are null this will throw an exception
     * @return a 64-bit hash of the given data, with all bits approximately equally likely to be set
     */
    public static long hash64(byte[][][] data)
    {
        if (data == null)
            return 0L;
        long result = 0x60642E2A34326F1EL, z = 0x1A976FDF6BF60B85L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result ^= (z += hash64(data[i]) ^ i);
        }
        result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
        result ^= result >>> 25 ^ z ^ z >>> 29;
        result = (result ^ result >>> 33) * 0xFF51AFD7ED558CCDL;
        result = (result ^ result >>> 33) * 0xC4CEB9FE1A85EC53L;
        return (result ^ result >>> 33);
    }
    private static int hash(final byte[] data) {
        int result = 0x1A976FDF, z = 0x60642E25;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result ^= (z += (data[i] ^ 0xC3564E95) * 0x9E375);
            z ^= (result = (result << 20 | result >>> 12));
        }
        result += (z ^ z >>> 15 ^ 0xAE932BD5) * 0x632B9;
//        result = (result ^ result >>> 15) * 0xFF51D;
//        result = (result ^ result >>> 15) * 0xC4CEB;
        return result ^ result >>> 15;
    }
    private static int hash(final byte[][] data) {
        int result = 0xC3564E9F, z = 0x1A976FD5;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result ^= (z += hash(data[i]) + i);
            z ^= (result = (result << 20 | result >>> 12));
        }
        result += (z ^ z >>> 15 ^ 0xAE932BD5) * 0x632B9;
//        result = (result ^ result >>> 15) * 0xFF51D;
//        result = (result ^ result >>> 15) * 0xC4CEB;
        return result ^ result >>> 15;
    }

    /**
     * Gets a 32-bit high-quality hash of the given 3D byte array. This should be preferred over
     * {@link #hash64(byte[][][])} when GWT is a possible target, since it's quite a lot faster to work with ints there.
     * @param data a 3D byte array; if null this returns 0, but if any sub-arrays are null this will throw an exception
     * @return a 32-bit hash of the given data, with all bits approximately equally likely to be set
     */
    public static int hash(final byte[][][] data) {
        if (data == null)
            return 0;
        int result = 0x60642E2F, z = 0xC3564E95;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result ^= (z += hash(data[i]) ^ i);
            z ^= (result = (result << 20 | result >>> 12));
        }
        result += (z ^ z >>> 15 ^ 0xAE932BD5) * 0x632B9;
        result = (result ^ result >>> 15) * 0xFF51D;
        result = (result ^ result >>> 15) * 0xC4CEB;
        return result ^ result >>> 15;
    }

    /**
     * A helper method for taking already-random input states and getting random values inside a small range.
     * The state can be any int, though only the least-significant 16 bits will be used, and the bound can be positive
     * or negative but should be limited to between -65536 to 65536 (precision will be lost past positive or negative
     * 65536). The result will be between 0, inclusive, and bound, exclusive. This method is safe for GWT and does not
     * use long math. If you give this values for state that aren't especially random, this may have noticeable patterns
     * in its output, but if you use {@link #hash(byte[][][])} to get state, you should be fine. If you need to make
     * multiple calls to this but have only one very-random input state, consider calling this with
     * {@code determineSmallBounded((state = (state ^ 0x9E3779B9) * 0x9E377) ^ state >>> 16, bound)}; this will change
     * state in a not-terribly random way (an XLCG; using XOR instead of addition makes it GWT-friendly) but then uses
     * a common xorshift operation to conceal issues with the lower bits (without changing state again).
     * @param state any int, but only the bottom 16 bits will be used; should already be random (e.g. from a hash)
     * @param bound an int that is at least -65536 and no more than 65536; will be used as the exclusive outer bound
     * @return an int between 0, inclusive, and bound, exclusive
     */
    public static int determineSmallBounded(final int state, final int bound)
    {
        return ((bound * (state & 0xFFFF)) >> 16);
    }

}
