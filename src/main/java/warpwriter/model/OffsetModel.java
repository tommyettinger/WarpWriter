package warpwriter.model;

/**
 * Allows offsetting the coordinates of any IFetch
 *
 * @author Ben McLean
 */
public class OffsetModel extends Fetch implements IModel {
    IFetch fetch;
    int xOffset, yOffset, zOffset;

    public OffsetModel(IFetch fetch, int xOffset, int yOffset, int zOffset) {
        this.fetch = fetch;
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
    public byte at(int x, int y, int z) {
        return fetch.at(x + xOffset, y + yOffset, z + zOffset);
    }
}
