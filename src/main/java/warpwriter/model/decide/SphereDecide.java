package warpwriter.model.decide;

/**
 * @author Ben McLean
 */
public class SphereDecide implements IDecide {
    protected int x, y, z, radius;

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public int z() {
        return z;
    }

    public int radius() {
        return radius;
    }

    public SphereDecide set(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public SphereDecide set(int radius) {
        this.radius = radius;
        return this;
    }

    public SphereDecide set(int x, int y, int z, int radius) {
        return set(x, y, z).set(radius);
    }

    public SphereDecide(int x, int y, int z, int radius) {
        set(x, y, z, radius);
    }

    @Override
    public boolean bool(int x, int y, int z) {
        return distance(x, y, z, this.x, this.y, this.z) < radius;
    }

    public static double distance(int x1, int y1, int z1, int x2, int y2, int z2) {
        return Math.sqrt(Math.pow((x1 - x2), 2) + Math.pow((y1 - y2), 2) + Math.pow((z1 - z2), 2));
    }
}
