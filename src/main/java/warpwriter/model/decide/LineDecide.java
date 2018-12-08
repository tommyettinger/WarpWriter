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

    @Override
    public boolean bool(int x, int y, int z) {
        double first = distance(x, y, z, x1, y1, z1),
                second = distance(x, y, z, x2, y2, z2);
        return (on && equals(first + second, distance))
                || (before && equals(first + distance, second))
                || (after && equals(second + distance, first));
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
