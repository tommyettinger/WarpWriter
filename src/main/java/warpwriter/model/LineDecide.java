package warpwriter.model;

/**
 * @author Ben McLean
 */
public class LineDecide implements IDecide {
    protected int x1, y1, z1, x2, y2, z2;

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
        return this;
    }

    public LineDecide setPoint2(int x2, int y2, int z2) {
        this.x2 = x2;
        this.y2 = y2;
        this.z2 = z2;
        return this;
    }

    public LineDecide(int x1, int y1, int z1, int x2, int y2, int z2) {
        setPoint1(x1, y1, z1).setPoint2(x2, y2, z2);
    }

    @Override
    public boolean bool(int x, int y, int z) {
        return x * this.x1 + y * y1 + z * z1 == 0 && x * x2 + y * y2 + z * z2 == 0;
    }
}
