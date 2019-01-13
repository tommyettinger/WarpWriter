package warpwriter.model.fetch;

import warpwriter.model.decide.PlaneDecide;

public class TileFetch {
    public static PlaneDecide Slope16x16x16 = new PlaneDecide(1, 0, 0, 0, PlaneDecide.Condition.ON_BELOW);
    public static PlaneDecide Diagonal16x16x16 = new PlaneDecide(1, 1, 0, 16, PlaneDecide.Condition.ON_BELOW);
}
