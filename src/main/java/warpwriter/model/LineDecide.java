package warpwriter.model;

/**
 * @author Ben McLean
 */
public class LineDecide implements IDecide {
    protected int a1, b1, c1, a2, b2, c2;

    public int a1() {
        return a1;
    }

    public int a2() {
        return a2;
    }

    public int b1() {
        return b1;
    }

    public int b2() {
        return b2;
    }

    public int c1() {
        return c1;
    }

    public int c2() {
        return c2;
    }

    public LineDecide setPlane1(int a1, int b1, int c1) {
        this.a1 = a1;
        this.b1 = b1;
        this.c1 = c1;
        return this;
    }

    public LineDecide setPlane2(int a2, int b2, int c2) {
        this.a2 = a2;
        this.b2 = b2;
        this.c2 = c2;
        return this;
    }

    public LineDecide(int a1, int b1, int c1, int a2, int b2, int c2) {
        setPlane1(a1, b1, c1).setPlane2(a2, b2, c2);
    }

    @Override
    public boolean bool(int x, int y, int z) {
        return x * a1 + y * b1 + z * c1 == 0 && x * a2 + y * b2 + z * c2 == 0;
    }
}
