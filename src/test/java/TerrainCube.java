/** @author Ben McLean */
public class TerrainCube {
    public static byte[][][] terrainCube(int size, int heightNE, int heightSE, int heightSW, int heightNW, int heightCenter, byte color) {
        return terrainCube(size, heightNE, heightSE, heightSW, heightNW, heightCenter, color, color, color, color);
    }

    public static byte[][][] terrainCube(int size, int heightNE, int heightSE, int heightSW, int heightNW, int heightCenter, byte colorNorth, byte colorEast, byte colorSouth, byte colorWest) {
        byte[][][] voxels = new byte[size][size][size];
        int center = size / 2;
        voxels = fillBeneathTriangle(voxels, 0, 0, heightSW, size, 0, heightSE, center, center, heightCenter, colorSouth);
        voxels = fillBeneathTriangle(voxels, center, center, heightCenter, size, 0, heightSE, size, size, heightNE, colorEast);
        voxels = fillBeneathTriangle(voxels, center, center, heightCenter, 0, size, heightNW, size, size, heightNE, colorNorth);
        voxels = fillBeneathTriangle(voxels, 0, 0, heightSW, center, center, heightCenter, 0, size, heightNW, colorWest);
        return voxels;
    }

    public static byte[][][] fillBeneathTriangle(byte[][][] voxels, int x1, int y1, int z1, int x2, int y2, int z2, int x3, int y3, int z3, byte fill) {
        for (int x = smallest(x1, x2, x3); x < largest(x1, x2, x3); x++)
            for (int y = smallest(y1, y2, y3); y < largest(y1, y2, y3); y++) {
                if (isInside(x, y, x1, y1, x2, y2, x3, y3)) {
                    int height = (z3 * (x - x1) * (y - y2) + z1 * (x - x2) * (y - y3) + z2 * (x - x3) * (y - y1) - z2 * (x - x1) * (y - y3) - z3 * (x - x2) * (y - y1) - z1 * (x - x3) * (y - y2)) / ((x - x1) * (y - y2) + (x - x2) * (y - y3) + (x - x3) * (y - y1) - (x - x1) * (y - y3) - (x - x2) * (y - y1) - (x - x3) * (y - y2));
                    for (int z = 0; z < height; z++)
                        voxels[x][y][z] = fill;
                }
            }
        return voxels;
    }

    public static int largest(int x1, int x2, int x3) {
        return x1 > x2 ? x1 > x3 ? x1 : x3 : x2 > x3 ? x2 : x3;
    }

    public static int smallest(int x1, int x2, int x3) {
        return x1 < x2 ? x1 < x3 ? x1 : x3 : x2 < x3 ? x2 : x3;
    }

    /**
     * A utility function to calculate area of triangle formed by (x1, y1) (x2, y2) and (x3, y3)
     *
     * @author Arnav Kr. Mandal
     */
    public static double area(int x1, int y1, int x2, int y2, int x3, int y3) {
        return Math.abs((x1 * (y2 - y3) + x2 * (y3 - y1) +
                x3 * (y1 - y2)) / 2.0);
    }

    /**
     * A function to check whether point P(x, y) lies inside the triangle formed by A(x1, y1), B(x2, y2) and C(x3, y3)
     *
     * @author Arnav Kr. Mandal
     */
    public static boolean isInside(int x, int y, int x1, int y1, int x2, int y2, int x3, int y3) {
        /* Calculate area of triangle ABC */
        double A = area(x1, y1, x2, y2, x3, y3);
        /* Calculate area of triangle PBC */
        double A1 = area(x, y, x2, y2, x3, y3);
        /* Calculate area of triangle PAC */
        double A2 = area(x1, y1, x, y, x3, y3);
        /* Calculate area of triangle PAB */
        double A3 = area(x1, y1, x2, y2, x, y);
        /* Check if sum of A1, A2 and A3 is same as A */
        return (int) A == (int) (A1 + A2 + A3);
    }
}
