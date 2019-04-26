package warpwriter.model.decide;

import warpwriter.model.Fetch;
import warpwriter.model.IFetch;

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

    public IFetch getFetch() {
        return fetch;
    }

    public IDecide getDecide() {
        return decide;
    }

    @Override
    public byte at(int x, int y, int z) {
        return decide.bool(x, y, z) ? deferFetch(fetch).at(x, y, z) : safeNextFetch().at(x, y, z);
    }

    public static final IDecide truth = new IDecide() {
        @Override
        public boolean bool(int x, int y, int z) {
            return true;
        }
    };

    public static final IDecide falsehood = new IDecide() {
        @Override
        public boolean bool(int x, int y, int z) {
            return false;
        }
    };
}
