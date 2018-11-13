package warpwriter.model;

/**
 * @author Ben McLean
 */
public class LineDecide implements IDecide {
    protected int x1 = 0, y1 = 0, z1 = 0, x2 = 0, y2 = 0, z2 = 0,
            lessX = 0, greatX = 0, lessY = 0, greatY = 0, lessZ = 0, greatZ = 0;
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

    public LineDecide setPoint1(int x1, int y1, int z1) {
        this.x1 = x1;
        this.y1 = y1;
        this.z1 = z1;
        return lowGreat();
    }

    public LineDecide setPoint2(int x2, int y2, int z2) {
        this.x2 = x2;
        this.y2 = y2;
        this.z2 = z2;
        return lowGreat();
    }

    protected LineDecide lowGreat() {
        if (x1 <= x2) {
            lessX = x1;
            greatX = x2;
        } else {
            lessX = x2;
            greatX = x1;
        }
        if (y1 <= y2) {
            lessY = y1;
            greatY = y2;
        } else {
            lessY = y2;
            greatY = y1;
        }
        if (z1 <= z2) {
            lessZ = z1;
            greatZ = z2;
        } else {
            lessZ = z2;
            greatZ = z1;
        }
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
        setPoint1(x1, y1, z1).setPoint2(x2, y2, z2);
    }

    @Override
    public boolean bool(int x, int y, int z) {
        double less = distance(x, y, z, lessX, lessY, lessZ),
                great = distance(x, y, z, greatX, greatY, greatZ);
        return (on && equals(less + great, distance))
                || (before && equals(less + distance, great))
                || (after && equals(great + distance, less));
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
