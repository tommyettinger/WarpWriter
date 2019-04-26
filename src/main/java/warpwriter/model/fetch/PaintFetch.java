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
    public byte at(int x, int y, int z) {
        final byte canvas = safeNextFetch().at(x, y, z);
        if (canvas == 0) return 0;
        final byte paint = fetch.at(x, y, z);
        return showThru && paint == 0 ? canvas : paint;
    }
}
