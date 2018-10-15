package warpwriter.model;

/**
 * Converts a Fetch to a Model with sizes stored in public ints
 *
 * @author Ben McLean
 */
public class FetchModel extends Fetch implements IModel {
    public int xSize, ySize, zSize;

    public FetchModel() {
        this(12, 12, 8);
    }

    public FetchModel(int xSize, int ySize, int zSize) {
        this(xSize, ySize, zSize, null);
    }

    public FetchModel(Fetch fetch, int xSize, int ySize, int zSize) {
        this(xSize, ySize, zSize, fetch);
    }

    public FetchModel(int xSize, int ySize, int zSize, Fetch fetch) {
        this.xSize = xSize;
        this.ySize = ySize;
        this.zSize = zSize;
        if (fetch != null) add(fetch);
    }

    public FetchModel(byte[][][] convenience) {
        this(convenience, null);
    }

    public FetchModel(byte[][][] convenience, Fetch fetch) {
        this(convenience.length, convenience[0].length, convenience[0][0].length, fetch);
    }

    public FetchModel(Fetch fetch, byte[][][] convenience) {
        this(convenience, fetch);
    }

    public FetchModel(IModel convenience, Fetch fetch) {
        this(convenience.xSize(), convenience.ySize(), convenience.zSize(), fetch);
    }

    public FetchModel(Fetch fetch, IModel convenience) {
        this(convenience, fetch);
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
    public Fetch fetch(int x, int y, int z) {
        return getNextFetch();
    }

    @Override
    public boolean outside(int x, int y, int z) {
        return x < 0 || y < 0 || z < 0 || x >= xSize() || y >= ySize() || z >= zSize();
    }

    @Override
    public boolean inside(int x, int y, int z) {
        return !outside(x, y, z);
    }
}
