package warpwriter;

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
}
