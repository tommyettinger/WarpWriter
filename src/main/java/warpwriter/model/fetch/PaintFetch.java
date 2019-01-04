package warpwriter.model.fetch;

import warpwriter.model.Fetch;

/**
 * Peeks into the future of the Fetch chain after it to repaint everything with the Fetch specified.
 * <p>
 * If showThru is true, then any time the specified fetch is transparent, the original color from the chain shows through.
 *
 * @author Ben McLean
 */
public class PaintFetch extends Fetch {
    Fetch fetch;

    public PaintFetch set(Fetch fetch) {
        this.fetch = fetch;
        return this;
    }

    public Fetch getFetch() {
        return fetch;
    }

    public PaintFetch(Fetch fetch) {
        super();
        set(fetch);
    }

    public PaintFetch(Fetch fetch, boolean showThru) {
        this(fetch);
        set(showThru);
    }

    protected boolean showThru = true;

    public PaintFetch set(boolean showThru) {
        this.showThru = showThru;
        return this;
    }

    public boolean showThru() {
        return showThru;
    }

    @Override
    public Fetch fetch() {
        final int x = chainX(), y = chainY(), z = chainZ();
        return getNextFetch().at(x, y, z) == 0 ?
                ColorFetch.color((byte) 0)
                : showThru ?
                fetch.at(x, y, z) == 0 ?
                        getNextFetch()
                        : fetch
                : fetch;
    }
}
