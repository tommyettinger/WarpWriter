package warpwriter.model;

/**
 * Infinitely loop any IFetch in any or all of the three dimensions.
 *
 * @author Ben McLean
 */
public class Loop extends FetchModel implements IModel {
    public Loop(int xSize, int ySize, int zSize) {
        super(xSize, ySize, zSize);
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
