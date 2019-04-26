package warpwriter.model.fetch;

import warpwriter.model.Fetch;
import warpwriter.model.IFetch;

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
    public byte at(int x, int y, int z) {
        return deferByte(iFetch.at(x, y, z), x, y, z);
    }
}
