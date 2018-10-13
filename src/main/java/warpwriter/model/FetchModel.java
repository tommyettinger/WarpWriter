package warpwriter.model;

/** Converts any Fetch to an IModel
 * @author Ben McLean
 */
public class FetchModel extends Fetch implements IModel {
    public int xSize, ySize, zSize;

    public FetchModel(int xSize, int ySize, int zSize) {
        this.xSize = xSize;
        this.ySize = ySize;
        this.zSize = zSize;
    }

    public FetchModel(Fetch fetch, int xSize, int ySize, int zSize) {
        this(xSize, ySize, zSize);
        add(fetch);
    }

    public FetchModel(int xSize, int ySize, int zSize, Fetch fetch) {
        this(fetch, xSize, ySize, zSize);
    }

    public FetchModel(byte[][][] convenience, Fetch fetch) {
        this(convenience.length, convenience[0].length, convenience[0][0].length, fetch);
    }

    public FetchModel(Fetch fetch, byte[][][] convenience) {
        this(convenience, fetch);
    }

    public int xSize() {
        return xSize;
    }

    public int ySize() {
        return ySize;
    }

    public int zSize() {
        return zSize;
    }

    public Fetch fetch(int x, int y, int z) {
        return getNextFetch();
    }
}
