package warpwriter.model.fetch;

import warpwriter.model.decide.PlaneDecide;

public class TileFetch {
    public static PlaneDecide Slope16x16x16 = new PlaneDecide(0.5d, 0d, 0.5d, 8, PlaneDecide.Condition.ON_BELOW);
    public static PlaneDecide Diagonal16x16x16 = new PlaneDecide(0.5d, 0.5d, 0.5d, 8, PlaneDecide.Condition.ON_BELOW);
}
