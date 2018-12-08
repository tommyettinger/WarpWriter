package warpwriter.model.fetch;

import warpwriter.model.Fetch;

/**
 * @author Ben McLean
 */
public class Skew extends Fetch {
    public float skewY =0f, skewZ;

    public Skew(float skewZ) {
        this.skewZ = skewZ;
    }

    public Skew(float skewY, float skewZ) {
        this(skewZ);
        this.skewY = skewY;
    }

    @Override
    public Fetch fetch() {
        int x = chainX(), y = chainY(), z = chainZ();
        setChains(x, y + (int) (x + skewY), z + (int) (y * skewZ));
        return getNextFetch();
    }
}
