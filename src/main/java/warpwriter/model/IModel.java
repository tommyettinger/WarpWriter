package warpwriter.model;

/**
 * An abstraction for IFetch types that also have limits on their 3D bounds, limiting what values can be passed to
 * {@link #at(int, int, int)} to between 0 (inclusive) and the xSize(), ySize(), and zSize() methods here (exclusive).
 * <br>
 * Created by Tommy Ettinger on 10/11/2018.
 *
 * @author Tommy Ettinger
 */
public interface IModel extends IFetch {
    /**
     * Gets the x size of the IModel, with requests for x limited between 0 (inclusive) to xSize() (exclusive).
     *
     * @return the size of the x dimension of the IModel
     */
    int xSize();

    /**
     * Gets the y size of the IModel, with requests for y limited between 0 (inclusive) to ySize() (exclusive).
     *
     * @return the size of the y dimension of the IModel
     */
    int ySize();

    /**
     * Gets the z size of the IModel, with requests for z limited between 0 (inclusive) to zSize() (exclusive).
     *
     * @return the size of the z dimension of the IModel
     */
    int zSize();

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
     * public boolean outside(int x, int y, int z) { return x < 0 || y < 0 || z < 0 || x >= xSize() || y >= ySize() || z >= zSize(); }
     */
    boolean outside(int x, int y, int z);
}
