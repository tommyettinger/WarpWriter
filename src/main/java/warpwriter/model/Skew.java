package warpwriter.model;

/**
 * @author Ben McLean
 */
public class Skew extends Fetch {
    public float xSkew, ySkew;

    public Skew(float xSkew, float ySkew) {
        this.xSkew = xSkew;
        this.ySkew = ySkew;
    }

    public Skew(float xSkew) {
        this(xSkew, 0f);
    }

    public Fetch fetch(int x, int y, int z) {
        setChains(x + (int) (y * xSkew), y + (int) (x * ySkew), z);
        return getNextFetch();
    }
}
