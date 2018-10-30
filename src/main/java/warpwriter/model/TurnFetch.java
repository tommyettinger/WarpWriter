package warpwriter.model;

/**
 * Rotates any Fetch around the point defined by the sizes from FetchModel. If no sizes are specified, turns around 0, 0, 0.
 *
 * @author Ben McLean
 */
public class TurnFetch extends FetchModel {
    protected Turner turner;

    public TurnFetch(Turner turner) {
        this(0, 0, 0, turner);
    }

    public TurnFetch(int x, int y, int z, Turner turner) {
        super(x, y, z);
        set(turner);
    }

    public TurnFetch set(Turner turner) {
        this.turner = turner;
        return this;
    }

    public Turner turner() {
        return turner;
    }

    @Override
    public Fetch fetch(int x, int y, int z) {
        turner.turn(x + xSize(), y + ySize(), z + zSize());
        setChains(turner.x() - xSize(), turner.y() - ySize(), turner.z() - zSize());
        return getNextFetch();
    }
}
