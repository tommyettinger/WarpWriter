package warpwriter.model;

/**
 * Allows offsetting the coordinates of any IFetch
 *
 * @author Ben McLean
 */
public class Offset extends Fetch implements IModel {
    int xOffset, yOffset, zOffset;

    public Offset(Fetch fetch, int xSize, int ySize, int zSize) {
        this(xSize, ySize, zSize);
        add(fetch);
    }

    public Offset(int xSize, int ySize, int zSize, Fetch fetch) {
        this(xSize, ySize, zSize);
        add(fetch);
    }

    public Offset(int xOffset, int yOffset, int zOffset) {
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.zOffset = zOffset;
    }

    @Override
    public int xSize() {
        return xOffset;
    }

    @Override
    public int ySize() {
        return yOffset;
    }

    @Override
    public int zSize() {
        return zOffset;
    }

    @Override
    public Fetch fetch(int x, int y, int z) {
        xChain = x + xOffset;
        yChain = y + yOffset;
        zChain = z + zOffset;
        return getNextFetch();
    }
}
