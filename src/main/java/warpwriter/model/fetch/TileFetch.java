package warpwriter.model.fetch;

import warpwriter.model.decide.DecideFetch;
import warpwriter.model.decide.IDecide;
import warpwriter.model.decide.PlaneDecide;

public class TileFetch {
    public static final PlaneDecide Slope16x16x16 = new PlaneDecide(0.5d, 0d, 0.5d, 8, PlaneDecide.Condition.ON_BELOW);
    public static final PlaneDecide Diagonal16x16x16 = new PlaneDecide(0.5d, 0.5d, 0.5d, 8, PlaneDecide.Condition.ON_BELOW);
    private static IDecide[] slopes16x16x16 = new IDecide[16];

    public static IDecide getSlope(int which) {
        if (which < 0 || which > 15) which = 0;
        if (slopes16x16x16[which] == null) {
            switch (which) {
                case 0:
                default:
                    slopes16x16x16[which] = DecideFetch.falsehood;
                    break;
                case 15:
                    slopes16x16x16[which] = DecideFetch.truth;
                    break;
            }
        }
        return slopes16x16x16[which];
    }

    public static IDecide slope16x16x16(boolean north, boolean east, boolean south, boolean west) {
        return getSlope(
                (north ? 8 : 0)
                        + (east ? 4 : 0)
                        + (south ? 2 : 0)
                        + (west ? 1 : 0)
        );
    }
}
