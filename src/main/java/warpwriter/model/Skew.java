package warpwriter.model;

/**
 * @author Ben McLean
 */
public class Skew extends Fetch {
    public float ySkew=0f, zSkew;

    public Skew(float zSkew) {
        this.zSkew = zSkew;
    }

    public Skew(float ySkew, float zSkew) {
        this(zSkew);
        this.ySkew = ySkew;
    }

    @Override
    public Fetch fetch() {
        int x = xChain(), y = yChain(), z = zChain();
        setChains(x, y + (int) (x + ySkew), z + (int) (y * zSkew));
        return getNextFetch();
    }
}
