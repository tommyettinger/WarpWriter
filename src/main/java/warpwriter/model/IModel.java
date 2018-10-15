package warpwriter.model;

/**
 * An abstraction for IFetch types that also have limits on their 3D bounds, limiting what values can be passed to
 * {@link #at(int, int, int)} to between 0 (inclusive) and the xSize(), ySize(), and zSize() methods here (exclusive).
 * The limits here are intended to be used to determine the size of rendered images or models, so they shouldn't be used
 * for a different purpose unless the implementation is not meant to be rendered.
 * <br>
 * The meanings of x, y, and z are important to clarify here; this uses MagicaVoxel's convention that z is up, and the
 * convention that in arrays, the dimensions are in the order x, y, z. When a fourth dimension is added, e.g. for time,
 * it goes outside x (meaning before it in a 4D array), which allows accessing a 3D element of a 4D array and passing
 * that to a renderer or modifier that affects 3D things. While MagicaVoxel specifies that z is the up/down axis and
 * positive z goes toward the sky, models commonly rotate and so x and y are somewhat muddled. When a model is facing
 * the camera, as with a head-on portrait, it is intended that positive y goes right, and positive x comes from far away
 * toward the camera (x coincides with the direction the model faces). This allows a 2D slice of a 3D model {@code arr}
 * to be taken by copying out the contents of a given x (depth) position, like so: {@code byte[][] twoDim = arr[x];}. If
 * the 3D model was extruded text that was readable when facing the camera, then this 2D slice will still contain a
 * readable slice of that text, just no longer extruded. This matches the behavior of getting a 3D model from a 4D
 * animation of multiple models. Similarly to the 2D case, if you get a 1D position by specifying x and y, then you have
 * an up-and-down line out of a model: {@code byte[] oneDim = arr[x][y];}. If you're using a 2D slice, it is still
 * suggested to use x and y as the names of the axes, and even though they become y and z when copied directly into a 3D
 * model, they can also be used in other ways where x and y don't correspond to y and z (a 2D slice might be used to
 * texture a cube, for example, so some side faces would have the 2D x,y correspond to 3D y,z, others to 3D x,z, and the
 * top and bottom faces to 3D x,y).  A 1D line should probably use x to refer to its one dimension, but there's no other
 * axis to confuse it with, so you could use any convention there (x is still recommended). Like with 2D, a 1D line can
 * be applied in many ways to a 3D model.
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
