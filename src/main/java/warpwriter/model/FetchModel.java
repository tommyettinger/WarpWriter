package warpwriter.model;

/** Converts any IFetch to an IModel
 * @author Ben McLean
 */
public class FetchModel implements IModel {
    public IFetch fetch;
    public int xSize, ySize, zSize;

    public FetchModel(IFetch fetch, int xSize, int ySize, int zSize) {
        this.fetch = fetch;
        this.xSize = xSize;
        this.ySize = ySize;
        this.zSize = zSize;
    }

    @Override
    public int xSize() {
        return xSize;
    }

    @Override
    public int ySize() {
        return ySize;
    }

    @Override
    public int zSize() {
        return zSize;
    }

    @Override
    public byte at(int x, int y, int z) {
        return fetch.at(x, y, z);
    }
}
