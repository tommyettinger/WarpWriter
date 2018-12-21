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
    public Fetch fetch() {
        return deferFetch(iFetch.at(chainX(), chainY(), chainZ()));
    }

    @Override
    public byte bite() {
        return deferByte(iFetch.at(chainX(), chainY(), chainZ()));
    }
}