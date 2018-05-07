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
                //fill(vs1, (byte) 0);
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

    private static int firstTight(byte[][][] voxels)
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

}
