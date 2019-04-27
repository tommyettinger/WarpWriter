package warpwriter.model.fetch;

import warpwriter.model.Fetch;
import warpwriter.model.IFetch;

/**
 * Peeks into the future of the Fetch chain after it to repaint occurrences of a specific color with a given Fetch.
 *
 * @author Ben McLean
 * @author Tommy Ettinger
 */
public class ReplaceFetch extends Fetch {
    public IFetch fetch;
    public byte color;

    public ReplaceFetch set(IFetch fetch) {
        this.fetch = fetch;
        return this;
    }

    public ReplaceFetch set(byte color) {
        this.color = color;
        return this;
    }

    public IFetch getFetch() {
        return fetch;
    }

    public byte getColorToReplace() {
        return color;
    }

    public ReplaceFetch(IFetch fetch) {
        super();
        set(fetch);
    }

    public ReplaceFetch(IFetch fetch, byte colorToReplace) {
        this(fetch);
        set(colorToReplace);
    }

    @Override
    public byte at(int x, int y, int z) {
        final byte future = getNextFetch().at(x, y, z);
        return future == 0 ? 0 : future == color ? fetch.at(x, y, z) : future;
    }
}
