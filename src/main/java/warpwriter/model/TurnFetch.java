package warpwriter.model;

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
    public Fetch fetch() {
        turner.input(chainX(), chainY(), chainZ());
        setChains(turner.x(), turner.y(), turner.z());
        return getNextFetch();
    }
}
