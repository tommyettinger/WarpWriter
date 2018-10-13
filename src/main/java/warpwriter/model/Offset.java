package warpwriter.model;

/**
 * Allows offsetting the coordinates of any IFetch
 *
 * @author Ben McLean
 */
public class Offset extends FetchModel {
    public Offset(int xSize, int ySize, int zSize) {
        super(xSize, ySize, zSize);
    }

    @Override
    public Fetch fetch(int x, int y, int z) {
        xChain = x + xSize();
        yChain = y + ySize();
        zChain = z + zSize();
        return getNextFetch();
    }
}
