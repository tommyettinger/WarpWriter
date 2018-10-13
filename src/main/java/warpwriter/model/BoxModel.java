package warpwriter.model;

/**
 * Draws a wireframe box!
 *
 * @author Ben McLean
 */
public class BoxModel extends FetchModel {
    public Fetch color;

    public BoxModel(int xSize, int ySize, int zSize, Fetch color) {
        super(xSize, ySize, zSize);
        this.color = color;
    }

    public BoxModel(Fetch color, int xSize, int ySize, int zSize) {
        this(xSize, ySize, zSize, color);
    }

    public BoxModel(byte[][][] convenience, Fetch color) {
        this(convenience.length, convenience[0].length, convenience[0][0].length, color);
    }

    public BoxModel(Fetch color, byte[][][] convenience) {
        this(convenience, color);
    }

    @Override
    public Fetch fetch(int x, int y, int z) {
        if (inside(x, y, z)) {
            boolean x0 = x == 0, x1 = x == xSize() - 1, x2 = x0 || x1,
                    y0 = y == 0, y1 = y == ySize() - 1, y2 = y0 || y1,
                    z0 = z == 0, z1 = z == zSize() - 1, z2 = z0 || z1;
            if ((x2 && y2) || (x2 && z2) || (y2 && z2))
                return color;
        }
        return getNextFetch();
    }
}
