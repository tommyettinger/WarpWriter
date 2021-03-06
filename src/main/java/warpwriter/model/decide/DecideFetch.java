package warpwriter.model.decide;

import warpwriter.model.Fetch;
import warpwriter.model.IFetch;
import warpwriter.model.fetch.ColorFetch;

/**
 * Uses an IDecide to make a choice between two different Fetches
 *
 * @author Ben McLean
 */
public class DecideFetch extends Fetch {
    protected IDecide decide = falsehood;
    protected IFetch iFetch = ColorFetch.transparent;

    public DecideFetch set(IDecide decide) {
        return setDecide(decide);
    }

    public DecideFetch setDecide(IDecide decide) {
        this.decide = decide;
        return this;
    }

    public DecideFetch set(IFetch iFetch) {
        return setFetch(iFetch);
    }

    public DecideFetch setFetch(IFetch iFetch) {
        this.iFetch = iFetch;
        return this;
    }

    public IFetch getFetch() {
        return iFetch;
    }

    public IDecide getDecide() {
        return decide;
    }

    @Override
    public byte at(int x, int y, int z) {
        return decide.bool(x, y, z) ? iFetch.at(x, y, z) : safeNextFetch().at(x, y, z);
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
