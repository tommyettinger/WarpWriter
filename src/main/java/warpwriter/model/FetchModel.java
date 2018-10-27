package warpwriter.model;

/**
 * Converts a Fetch to a Model with sizes stored in ints
 *
 * @author Ben McLean
 */
public class FetchModel extends Fetch implements IModel {
    protected int xSize, ySize, zSize;

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
        set(xSize, ySize, zSize);
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

    /**
     * Copies size
     * @param convenience An IModel with a size to copy
     * @param fetch Actual fetch to use
     */
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

    public FetchModel add(int x, int y, int z) {
        return addX(x).addY(y).addZ(z);
    }

    public FetchModel addX(int x) {
        xSize+=x;
        return this;
    }

    public FetchModel addY(int y) {
        ySize+=y;
        return this;
    }

    public FetchModel addZ(int z) {
        zSize+=z;
        return this;
    }

    public FetchModel add(CompassDirection direction) {
        return addX(direction.deltaX).addY(direction.deltaY);
    }

    public FetchModel set(int x, int y, int z) {
        return setX(x).setY(y).setZ(z);
    }

    public FetchModel setX(int x) {
        xSize = x;
        return this;
    }

    public FetchModel setY(int y) {
        ySize = y;
        return this;
    }

    public FetchModel setZ(int z) {
        zSize = z;
        return this;
    }

    /**
     * @return inside(x, y, z)
     */
    @Override
    public boolean bool(int x, int y, int z) {
        return inside(x, y, z);
    }
}
