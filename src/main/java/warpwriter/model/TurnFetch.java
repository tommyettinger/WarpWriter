package warpwriter.model;

/**
 * Rotates any Fetch around the point defined by the sizes from FetchModel. If no sizes are specified, turns around 0, 0, 0.
 *
 * @author Ben McLean
 */
public class TurnFetch extends FetchModel {
    protected Rotator rotator = new Rotator();

    public TurnFetch(Rotator.Face face, Rotator.Roll roll) {
        super(0, 0, 0);
        set(face).set(roll);
    }

    public TurnFetch(int x, int y, int z, Rotator.Face face, Rotator.Roll roll) {
        super(x, y, z);
        set(face).set(roll);
    }

    public TurnFetch set(Rotator.Face face) {
        rotator.set(face);
        return this;
    }

    public TurnFetch set(Rotator.Roll roll) {
        rotator.set(roll);
        return this;
    }

    @Override
    public Fetch fetch(int x, int y, int z) {
        rotator.turn(x + xSize(), y + ySize(), z + zSize());
        setChains(rotator.x() - xSize(), rotator.y() - ySize(), rotator.z() - zSize());
        return getNextFetch();
    }
}
