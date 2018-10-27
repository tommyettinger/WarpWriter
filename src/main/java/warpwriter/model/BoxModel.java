package warpwriter.model;

/**
 * Draws a wireframe box!
 *
 * @author Ben McLean
 */
public class BoxModel extends FetchModel {
    public Fetch fetch;

    public BoxModel(int xSize, int ySize, int zSize, Fetch fetch) {
        super(xSize, ySize, zSize);
        this.fetch = fetch;
    }

    public BoxModel(Fetch fetch, int xSize, int ySize, int zSize) {
        this(xSize, ySize, zSize, fetch);
    }

    public BoxModel(byte[][][] convenience, Fetch fetch) {
        this(convenience.length, convenience[0].length, convenience[0][0].length, fetch);
    }

    public BoxModel(Fetch fetch, byte[][][] convenience) {
        this(convenience, fetch);
    }

    @Override
    public Fetch fetch(int x, int y, int z) {
        return bool(x, y, z) ? fetch : getNextFetch();
    }

    /**
     * @return True if coordinates are part of the wireframe edges of the box.
     */
    @Override
    public boolean bool(int x, int y, int z) {
        return inside(x, y, z) && (((x == 0 || x == xSize() - 1) && (y == 0 || y == ySize() - 1)) || ((x == 0 || x == xSize() - 1) && (z == 0 || z == zSize() - 1)) || ((y == 0 || y == ySize() - 1) && (z == 0 || z == zSize() - 1)));
    }
}
