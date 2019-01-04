package warpwriter.model.fetch;

import warpwriter.model.Fetch;

/**
 * Peeks into the future of the Fetch chain after it to repaint occurrences of a specific color with a given Fetch.
 *
 * @author Ben McLean
 * @author Tommy Ettinger
 */
public class ReplaceFetch extends Fetch {
    public Fetch fetch;
    public byte color;

    public ReplaceFetch set(Fetch fetch) {
        this.fetch = fetch;
        return this;
    }

    public ReplaceFetch set(byte color) {
        this.color = color;
        return this;
    }

    public Fetch getFetch() {
        return fetch;
    }

    public byte getColorToReplace() {
        return color;
    }

    public ReplaceFetch(Fetch fetch) {
        super();
        set(fetch);
    }

    public ReplaceFetch(Fetch fetch, byte colorToReplace) {
        this(fetch);
        set(colorToReplace);
    }

    @Override
    public Fetch fetch() {
        final int x = chainX(), y = chainY(), z = chainZ();
        final byte future = getNextFetch().at(x, y, z);
        if (future == 0)
            return ColorFetch.color((byte) 0);
        else if (future == color)
            return fetch;
        else
            return getNextFetch();
//        return future == 0 ?
//                ColorFetch.color((byte) 0)
//                : future != color ?
//                        getNextFetch()
//                        : fetch;
    }
}
