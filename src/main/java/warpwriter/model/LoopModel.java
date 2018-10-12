package warpwriter.model;

/**
 * Infinitely loop any IFetch in any or all of the three dimensions.
 *
 * @author Ben McLean
 */
public class LoopModel implements IModel {
    IFetch fetch;
    int xSize, ySize, zSize;

    /**
     * @param fetch What to loop
     * @param xSize Size to loop in x dimension, (from 0 inclusive to that size exclusive) or 0 to not loop in that dimension
     * @param ySize Size to loop in y dimension, (from 0 inclusive to that size exclusive) or 0 to not loop in that dimension
     * @param zSize Size to loop in z dimension, (from 0 inclusive to that size exclusive) or 0 to not loop in that dimension
     */
    public LoopModel(IFetch fetch, int xSize, int ySize, int zSize) {
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
        return fetch.at(loop(x, xSize), loop(y, ySize), loop(z, zSize));
    }

    /**
     * @param divisor Size of loop or 0 to turn off looping and just return dividend
     */
    public static int loop(int dividend, int divisor) {
        return divisor == 0 ? dividend : dividend < 0 ? divisor - Math.abs(dividend % divisor) : dividend % divisor;
    }
}
