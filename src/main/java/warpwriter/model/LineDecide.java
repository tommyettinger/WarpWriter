package warpwriter.model;

/**
 * @author Ben McLean
 */
public class LineDecide implements IDecide {
    protected int x1=0, y1=0, z1=0, x2=0, y2=0, z2=0,
            lessX=0, greatX=0, lessY=0, greatY=0, lessZ=0, greatZ=0;

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
        }
        else {
            lessX = x2;
            greatX = x1;
        }
        if (y1 <= y2) {
            lessY = y1;
            greatY = y2;
        }
        else {
            lessY = y2;
            greatY = y1;
        }
        if (z1 <= z2) {
            lessZ = z1;
            greatZ = z2;
        }
        else {
            lessZ = z2;
            greatZ = z1;
        }
        return this;
    }

    public LineDecide(int x1, int y1, int z1, int x2, int y2, int z2) {
        setPoint1(x1, y1, z1).setPoint2(x2, y2, z2);
    }

    @Override
    public boolean bool(int x, int y, int z) {
        return false; // TODO: implement method
    }
}
