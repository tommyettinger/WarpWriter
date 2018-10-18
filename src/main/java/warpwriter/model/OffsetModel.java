package warpwriter.model;

/**
 * Allows offsetting the coordinates of any Fetch
 *
 * @author Ben McLean
 */
public class OffsetModel extends FetchModel {
    public OffsetModel() {
        this(0, 0, 0);
    }

    public OffsetModel(Fetch fetch) {
        this(0, 0, 0, fetch);
    }

    public OffsetModel(int xSize, int ySize, int zSize) {
        this(xSize, ySize, zSize, null);
    }

    public OffsetModel(int xSize, int ySize, int zSize, Fetch fetch) {
        super(xSize, ySize, zSize, fetch);
    }

    @Override
    public Fetch fetch(int x, int y, int z) {
        setChains(x + xSize(), y + ySize(), z + zSize());
        return getNextFetch();
    }
}
