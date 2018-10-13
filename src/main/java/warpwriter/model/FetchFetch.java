package warpwriter.model;

/**
 * Converts an IFetch which doesn't implement Fetch to one that does through encapsulation.
 *
 * @author Ben McLean
 */
public class FetchFetch extends Fetch {
    public IFetch iFetch;

    public FetchFetch(IFetch iFetch) {
        this.iFetch = iFetch;
    }

    @Override
    public Fetch fetch(int x, int y, int z) {
        return deferFetch(iFetch.at(x, y, z), x, y, z);
    }

    @Override
    public byte bite(int x, int y, int z) {
        return deferByte(iFetch.at(x, y, z), x, y, z);
    }
}
