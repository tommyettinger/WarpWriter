package warpwriter.model;

/**
 * @author Ben McLean
 */
public class Skew extends Fetch {
    public float skew;

    public Skew(float skew) {
        this.skew = skew;
    }

    @Override
    public Fetch fetch(int x, int y, int z) {
        setChains(x, y, z + (int) (y * skew));
        return getNextFetch();
    }
}
