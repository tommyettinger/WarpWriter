package warpwriter.model.fetch;

import warpwriter.model.Fetch;

/**
 * Peeks into the future of the Fetch chain after it to repaint everything with the Fetch specified.
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

    @Override
    public Fetch fetch() {
        return getNextFetch().at(chainX(), chainY(), chainZ()) == 0 ?
                ColorFetch.color((byte) 0)
                : fetch;
    }
}
