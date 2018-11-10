package warpwriter.model;

public class HeightDecide implements IDecide {
    IHeightMap map;
    double scaleX, scaleY, scaleZ;

    public IHeightMap getMap() {
        return map;
    }

    public HeightDecide(IHeightMap map, double scaleX, double scaleY, double scaleZ) {
        this.map = map;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.scaleZ = scaleZ;
    }

    public HeightDecide(IHeightMap map, double scaleY, double scaleZ) {
        this(map, 1, scaleY, scaleZ);
    }

    public HeightDecide(IHeightMap map, double scaleZ) {
        this(map, 1, scaleZ);
    }

    public HeightDecide(IHeightMap map) {
        this(map, 1);
    }

    @Override
    public boolean bool(int x, int y, int z) {
        return z < (map.heightMap(x * scaleX, y * scaleY) + 1) * scaleZ;
    }

    /**
     * A utility function to calculate the z for (x, y) inside the 3D triangle formed by (x1, y1, z1), (x2, y2, z2) and (x3, y3, z3)
     */
    public static int heightInTriangle(int x, int y, int x1, int y1, int z1, int x2, int y2, int z2, int x3, int y3, int z3) {
        return (z3 * (x - x1) * (y - y2) + z1 * (x - x2) * (y - y3) + z2 * (x - x3) * (y - y1) - z2 * (x - x1) * (y - y3) - z3 * (x - x2) * (y - y1) - z1 * (x - x3) * (y - y2)) / ((x - x1) * (y - y2) + (x - x2) * (y - y3) + (x - x3) * (y - y1) - (x - x1) * (y - y3) - (x - x2) * (y - y1) - (x - x3) * (y - y2));
    }

    /**
     * A utility function to calculate area of triangle formed by (x1, y1) (x2, y2) and (x3, y3)
     *
     * @author Arnav Kr. Mandal
     */
    public static double area(int x1, int y1, int x2, int y2, int x3, int y3) {
        return Math.abs((x1 * (y2 - y3) + x2 * (y3 - y1) + x3 * (y1 - y2)) / 2.0);
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
