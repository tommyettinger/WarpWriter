package warpwriter.model;

/**
 * Infinitely loop any IFetch in any or all of the three dimensions.
 *
 * @author Ben McLean
 */
public class Loop extends Fetch implements IModel {
    int xSize, ySize, zSize;

    public Loop(Fetch fetch, int xSize, int ySize, int zSize) {
        this(xSize, ySize, zSize);
        add(fetch);
    }

    public Loop(int xSize, int ySize, int zSize, Fetch fetch) {
        this(xSize, ySize, zSize);
        add(fetch);
    }

    public Loop(int xSize, int ySize, int zSize) {
        this.xSize = xSize;
        this.ySize = ySize;
        this.zSize = zSize;
    }

    public Loop(byte[][][] convenience, Fetch fetch) {
        this(convenience.length, convenience[0].length, convenience[0][0].length, fetch);
    }

    public Loop(Fetch fetch, byte[][][] convenience) {
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
        xChain = loop(x, xSize());
        yChain = loop(y, ySize());
        zChain = loop(z, zSize());
        return getNextFetch();
    }

    /**
     * @param divisor Size of loop or 0 to turn off looping and just return dividend
     */
    public static int loop(int dividend, int divisor) {
        return divisor == 0 ? dividend : dividend < 0 ? divisor - Math.abs(dividend % divisor) : dividend % divisor;
    }
}
