package warpwriter.model;

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

    public OffsetModel(int xSize, int ySize, int zSize) {
        this(xSize, ySize, zSize, null);
    }

    public OffsetModel(int xSize, int ySize, int zSize, Fetch fetch) {
        super(xSize, ySize, zSize);
        set(fetch);
    }

    @Override
    public Fetch fetch() {
        return fetch == null ?
                setChains(xChain() + sizeX(), yChain() + sizeY(), zChain() + sizeZ()).getNextFetch()
                : deferFetch(fetch.setChains(xChain() + sizeX(), yChain() + sizeY(), zChain() + sizeZ()));
    }
}
