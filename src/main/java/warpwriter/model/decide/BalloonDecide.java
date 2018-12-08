package warpwriter.model.decide;

import warpwriter.model.IDecide;

/**
 * Draws a balloon shape stretched two points. Discovered by accident!
 *
 * @author Ben McLean
 */
public class BalloonDecide implements IDecide {
    protected int x1 = 0, y1 = 0, z1 = 0, x2 = 0, y2 = 0, z2 = 0;
            //lessX = 0, greatX = 0, lessY = 0, greatY = 0, lessZ = 0, greatZ = 0;
    protected double distance = 0, width = 1;

    public double width() {
        return width;
    }

    public BalloonDecide set(double width) {
        this.width = width;
        return this;
    }

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

    public BalloonDecide set(int x1, int y1, int z1, int x2, int y2, int z2) {
        this.x1 = x1;
        this.y1 = y1;
        this.z1 = z1;
        this.x2 = x2;
        this.y2 = y2;
        this.z2 = z2;
        distance = distance();
        return this;
    }

    public BalloonDecide(int x1, int y1, int z1, int x2, int y2, int z2) {
        set(x1, y1, z1, x2, y2, z2);
    }

    @Override
    public boolean bool(int x, int y, int z) {
        return equals(distance(x, y, z, x1, y1, z1) + distance(x, y, z, x2, y2, z2), distance);
    }

    public boolean equals(double a, double b) {
        return Math.abs(a - b) < width;
    }

    public double distance() {
        return distance(x1, y1, z1, x2, y2, z2);
    }

    public static double distance(int x1, int y1, int z1, int x2, int y2, int z2) {
        return Math.sqrt(Math.pow((x1 - x2), 2) + Math.pow((y1 - y2), 2) + Math.pow((z1 - z2), 2));
    }
}
