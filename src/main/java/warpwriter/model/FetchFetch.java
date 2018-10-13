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

    public Fetch fetch(int x, int y, int z) {
        return zeroFetch(iFetch.at(x, y, z), x, y, z);
    }

    public byte bite(int x, int y, int z) {
        return zeroByte(iFetch.at(x, y, z), x, y, z);
    }
}
