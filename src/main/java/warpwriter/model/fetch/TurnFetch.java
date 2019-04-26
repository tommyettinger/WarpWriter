package warpwriter.model.fetch;

import warpwriter.model.Fetch;
import warpwriter.model.nonvoxel.Turner;

/**
 * Rotates a Fetch chain using a Turner.
 *
 * @author Ben McLean
 */
public class TurnFetch extends Fetch {
    protected Turner turner;

    public TurnFetch() {
        this(new Turner());
    }

    public TurnFetch(TurnFetch turnFetch) {
        this(new Turner(turnFetch.turner()));
    }

    public TurnFetch(Turner turner) {
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
    public byte at(int x, int y, int z) {
        turner.input(x, y, z);
        return getNextFetch().at(turner.x(), turner.y(), turner.z());
    }
}
