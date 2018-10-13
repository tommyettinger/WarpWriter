package warpwriter.model;

/**
 * @author Ben McLean
 */
public class BoxModel extends Fetch implements IModel {
    int xSize, ySize, zSize;

    public BoxModel(int xSize, int ySize, int zSize, Fetch yes, Fetch no) {
        this(xSize, ySize, zSize, yes);
        add(no);
    }

    public BoxModel(int xSize, int ySize, int zSize, Fetch yes) {
        this.xSize = xSize;
        this.ySize = ySize;
        this.zSize = zSize;
        this.yesFetch=yes;
    }

    public BoxModel(byte[][][] convenience, Fetch fetch) {
        this(convenience.length, convenience[0].length, convenience[0][0].length, fetch);
    }

    public BoxModel(Fetch fetch, byte[][][] convenience) {
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

    public Fetch yesFetch;

    public Fetch fetch(int x, int y, int z) {
        boolean x0 = x == 0, x1 = x == xSize() - 1, x2 = x0 || x1,
                y0 = y == 0, y1 = y == ySize() - 1, y2 = y0 || y1,
                z0 = z == 0, z1 = z == zSize() - 1, z2 = z0 || z1;
        if ((x2 && y2) || (x2 && z2) || (y2 && z2))
            return yesFetch;
        else
            return getNextFetch();
    }
}
