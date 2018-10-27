package warpwriter.model;

/**
 * Uses an IDecide to make a choice between two different Fetches
 *
 * @author Ben McLean
 */
public class DecideFetch extends Fetch {
    protected IDecide decide;
    protected Fetch fetch;

    public DecideFetch(IDecide decide, Fetch fetch) {
        set(decide).set(fetch);
    }

    public DecideFetch set(IDecide decide) {
        this.decide = decide;
        return this;
    }

    public DecideFetch set(Fetch fetch) {
        this.fetch = fetch;
        return this;
    }

    public Fetch getFetch() {
        return fetch;
    }

    public IDecide getDecide() {
        return decide;
    }

    @Override
    public Fetch fetch(int x, int y, int z) {
        return decide.bool(x, y, z) ? fetch : getNextFetch();
    }
}
