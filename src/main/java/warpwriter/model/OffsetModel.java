package warpwriter.model;

/**
 * Allows offsetting the coordinates of any Fetch
 *
 * @author Ben McLean
 */
public class OffsetModel extends FetchModel {
    public OffsetModel(int xSize, int ySize, int zSize) {
        super(xSize, ySize, zSize);
    }

    @Override
    public Fetch fetch(int x, int y, int z) {
        setChains(x + xSize(), y + ySize(), z + zSize());
        return getNextFetch();
    }
}
