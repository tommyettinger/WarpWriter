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
        return deferFetch(iFetch.at(xChain(), yChain(), zChain()));
    }

    @Override
    public byte bite() {
        return deferByte(iFetch.at(xChain(), yChain(), zChain()));
    }
}
