package warpwriter.model.decide;

import warpwriter.model.IDecide;

/**
 * @author Ben McLean
 */
public class LineDecide implements IDecide {
    protected int x1 = 0, y1 = 0, z1 = 0, x2 = 0, y2 = 0, z2 = 0;
    protected double distance = 0;

    public int x1() {
        return x1;
    }

    public int x2() {
        return x2;
    }

    public int y1() {
        return y1;
    }

    public int y2() {
        return y2;
    }

    public int z1() {
        return z1;
    }

    public int z2() {
        return z2;
    }

    public LineDecide setPoints(int x1, int y1, int z1, int x2, int y2, int z2) {
        this.x1 = x1;
        this.y1 = y1;
        this.z1 = z1;
        this.x2 = x2;
        this.y2 = y2;
        this.z2 = z2;
        return this;
    }

    protected LineDecide lowGreat() {
        distance = distance();
        return this;
    }

    public boolean before = false, on = true, after = false;

    public boolean before() {
        return before;
    }

    public boolean on() {
        return on;
    }

    public boolean after() {
        return after;
    }

    public LineDecide setOn(boolean on) {
        this.on = on;
        return this;
    }

    public LineDecide setBefore(boolean before) {
        this.before = before;
        return this;
    }

    public LineDecide setAfter(boolean after) {
        this.after = after;
        return this;
    }

    public LineDecide set(boolean before, boolean on, boolean after) {
        return setBefore(before).setOn(on).setAfter(after);
    }

    public LineDecide(int x1, int y1, int z1, int x2, int y2, int z2, boolean before, boolean on, boolean after) {
        this(x1, y1, z1, x2, y2, z2);
        set(before, on, after);
    }

    public LineDecide(int x1, int y1, int z1, int x2, int y2, int z2) {
        setPoints(x1, y1, z1, x2, y2, z2);
    }

    /**
     * Picked up this algorithm from http://members.chello.at/easyfilter/bresenham.html
     */
    public static boolean checkLine3D(int x, int y, int z, int x0, int y0, int z0, int x1, int y1, int z1) {
        int dx = Math.abs(x1 - x0), sx = x0 < x1 ? 1 : -1;
        int dy = Math.abs(y1 - y0), sy = y0 < y1 ? 1 : -1;
        int dz = Math.abs(z1 - z0), sz = z0 < z1 ? 1 : -1;
        int dm = Math.max(Math.max(dx, dy), dz), i = dm; /* maximum difference */
        x1 = y1 = z1 = dm / 2; /* error offset */

        while (true) {
            //setPixel(x0,y0,z0);
            if (x0 == x && y0 == y && z0 == z) return true;
            if (i-- == 0) break;
            x1 -= dx;
            if (x1 < 0) {
                x1 += dm;
                x0 += sx;
            }
            y1 -= dy;
            if (y1 < 0) {
                y1 += dm;
                y0 += sy;
            }
            z1 -= dz;
            if (z1 < 0) {
                z1 += dm;
                z0 += sz;
            }
        }
        return false;
    }

    @Override
    public boolean bool(int x, int y, int z) {
        return checkLine3D(x, y, z, x1, y1, z1, x2, y2, z2);
    }

    public static boolean equals(double a, double b) {
        return Math.abs(a - b) < 1;
    }

    public double distance() {
        return distance(x1, y1, z1, x2, y2, z2);
    }

    public static double distance(int x1, int y1, int z1, int x2, int y2, int z2) {
        return Math.sqrt(Math.pow((x1 - x2), 2) + Math.pow((y1 - y2), 2) + Math.pow((z1 - z2), 2));
    }
}
