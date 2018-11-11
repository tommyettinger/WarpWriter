package warpwriter.model;

/**
 * Converts an IFetch which doesn't extend Fetch to one that does through encapsulation.
 *
 * @author Ben McLean
 */
public class FetchFetch extends Fetch {
    public IFetch iFetch;

    public FetchFetch(IFetch iFetch) {
        this.iFetch = iFetch;
    }

    @Override
    public Fetch fetch() {
        int x = xChain(), y = yChain(), z = zChain();
        return deferFetch(iFetch.at(x, y, z), x, y, z);
    }

    @Override
    public byte bite() {
        int x = xChain(), y = yChain(), z = zChain();
        return deferByte(iFetch.at(x, y, z), x, y, z);
    }
}
