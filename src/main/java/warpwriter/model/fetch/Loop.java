package warpwriter.model.fetch;

import warpwriter.model.Fetch;
import warpwriter.model.FetchModel;

/**
 * Infinitely loop any Fetch in any or all of the three dimensions.
 *
 * @author Ben McLean
 */
public class Loop extends FetchModel {
    public Loop(int xSize, int ySize, int zSize) {
        super(xSize, ySize, zSize);
    }

    @Override
    public Fetch fetch() {
        setChains(loop(chainX(), sizeX()), loop(chainY(), sizeY()), loop(chainZ(), sizeZ()));
        return getNextFetch();
    }

    /**
     * @param divisor Size of loop or 0 to turn off looping and just return dividend
     */
    public static int loop(int dividend, int divisor) {
        return divisor == 0 ? dividend : dividend < 0 ? divisor - Math.abs(dividend % divisor) : dividend % divisor;
    }
}
