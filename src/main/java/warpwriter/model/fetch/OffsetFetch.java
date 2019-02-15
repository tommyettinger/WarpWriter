package warpwriter.model.fetch;

import warpwriter.model.Fetch;
import warpwriter.model.IFetch;

/**
 * Offsets the coordinates of the contained IFetch
 *
 * @author Ben McLean
 */
public class OffsetFetch extends Fetch {
    protected IFetch fetch;

    public IFetch getFetch() {
        return fetch;
    }

    public OffsetFetch set(Fetch fetch) {
        this.fetch = fetch;
        return this;
    }

    protected int x = 0, y = 0, z = 0;

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public int z() {
        return z;
    }

    public OffsetFetch setX(final int x) {
        this.x = x;
        return this;
    }

    public OffsetFetch setY(final int y) {
        this.y = y;
        return this;
    }

    public OffsetFetch setZ(final int z) {
        this.z = z;
        return this;
    }

    public OffsetFetch set(final int x, final int y, final int z) {
        return setX(x).setY(y).setZ(z);
    }

    @Override
    public byte bite() {
        return deferByte(fetch.at(chainX() + x(), chainY() + y(), chainZ() + z()));
    }
}
