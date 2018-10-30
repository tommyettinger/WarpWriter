package warpwriter.model;

/**
 * Rotates any Fetch around the point defined by the sizes from FetchModel. If no sizes are specified, turns around 0, 0, 0.
 *
 * @author Ben McLean
 */
public class TurnFetch extends FetchModel {
    protected Turner turner = new Turner();

    public TurnFetch(Turner.Face face, Turner.Roll roll) {
        super(0, 0, 0);
        set(face).set(roll);
    }

    public TurnFetch(int x, int y, int z, Turner.Face face, Turner.Roll roll) {
        super(x, y, z);
        set(face).set(roll);
    }

    public TurnFetch set(Turner.Face face) {
        turner.set(face);
        return this;
    }

    public TurnFetch set(Turner.Roll roll) {
        turner.set(roll);
        return this;
    }

    @Override
    public Fetch fetch(int x, int y, int z) {
        turner.turn(x + xSize(), y + ySize(), z + zSize());
        setChains(turner.x() - xSize(), turner.y() - ySize(), turner.z() - zSize());
        return getNextFetch();
    }
}
