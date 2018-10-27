package warpwriter.model;

/**
 * Turns a fetch 90 degrees around 0, 0, 0
 * @author Ben McLean
 */
public class TurnFetch extends Fetch {
    private boolean clockwise;

    public TurnFetch() {
        this(true);
    }

    public TurnFetch (boolean clockwise) {
        this.clockwise = clockwise;
    }

    @Override
    public Fetch fetch(int x, int y, int z) {
        if (clockwise)
            setChains(x, z * -1, y);
        else
            setChains(x, z, y * -1);
        return getNextFetch();
    }
}
