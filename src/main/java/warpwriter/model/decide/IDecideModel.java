package warpwriter.model.decide;

public interface IDecideModel extends IDecide {
    /**
     * Gets the x size of the IDecideModel, with requests for x limited between 0 (inclusive) to sizeX() (exclusive).
     *
     * @return the size of the x dimension of the IDecideModel
     */
    int sizeX();

    /**
     * Gets the y size of the IDecideModel, with requests for y limited between 0 (inclusive) to sizeY() (exclusive).
     *
     * @return the size of the y dimension of the IDecideModel
     */
    int sizeY();

    /**
     * Gets the z size of the IDecideModel, with requests for z limited between 0 (inclusive) to sizeZ() (exclusive).
     *
     * @return the size of the z dimension of the IDecideModel
     */
    int sizeZ();

    /**
     * @return True if the given coordinate is inside the intended range
     * <p>
     * Recommended (but not required) implementation:
     * public boolean inside(int x, int y, int z) { return !outside(x, y, z); }
     */
    boolean inside(int x, int y, int z);

    /**
     * @return True if the given coordinate is outside the intended range
     * <p>
     * Recommended (but not required) implementation:
     * {@code public boolean outside(int x, int y, int z) { return x < 0 || y < 0 || z < 0 || x >= sizeX() || y >= sizeY() || z >= sizeZ(); } }
     */
    boolean outside(int x, int y, int z);
}
