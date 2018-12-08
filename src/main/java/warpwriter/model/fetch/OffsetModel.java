package warpwriter.model.fetch;

import warpwriter.model.Fetch;
import warpwriter.model.FetchModel;

/**
 * Allows offsetting the coordinates of any Fetch
 *
 * @author Ben McLean
 */
public class OffsetModel extends FetchModel {
    protected Fetch fetch;

    public Fetch getFetch() {
        return fetch;
    }

    /**
     * @param fetch If fetch is set then the offset will only be applied to it
     * @return this
     */
    public OffsetModel set(Fetch fetch) {
        this.fetch = fetch;
        return this;
    }

    public OffsetModel() {
        this(0, 0, 0);
    }

    public OffsetModel(Fetch fetch) {
        this(0, 0, 0);
        set(fetch);
    }

    public OffsetModel(int sizeX, int sizeY, int sizeZ) {
        this(sizeX, sizeY, sizeZ, null);
    }

    public OffsetModel(int sizeX, int sizeY, int sizeZ, Fetch fetch) {
        super(sizeX, sizeY, sizeZ);
        set(fetch);
    }

    @Override
    public Fetch fetch() {
        return fetch == null ?
                setChains(chainX() + sizeX(), chainY() + sizeY(), chainZ() + sizeZ()).getNextFetch()
                : deferFetch(fetch.setChains(chainX() + sizeX(), chainY() + sizeY(), chainZ() + sizeZ()));
    }
}
