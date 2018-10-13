package warpwriter.model;

/**
 * This abstract class allows for IFetch implementations to defer to other IFetch instances for different coordinates instead of needing to return a byte.
 * <p>
 * The point of this class is that, to be useful, child classes must override either fetch(x, y, z) or at(x, y, z) or both. Methods which deal with this class will call fetch(x, y, z) first and if that is null, then call at(x, y, z) to get the answer.
 *
 * @author Ben McLean
 */
public abstract class Fetch implements IFetch, IFetch2D, IFetch1D {
    /**
     * This method is intended to be overridden with logic to determine which fill to return for the provided coordinate.
     * <p>
     * Returning null indicates to outside code that at(x, y, z) should be called instead.
     *
     * @return null
     */
    public Fetch fetch(int x, int y, int z) {
        return null;
    }

    public Fetch fetch(int x, int z) {
        return fetch(x, 0, z);
    }

    public Fetch fetch(int x) {
        return fetch(x, 0);
    }

    public byte at(int x, int y, int z) {
        return fetch(x, y, z).at(x, y, z);
    }

    public byte at(int x, int z) {
        return fetch(x, z).at(x, z);
    }

    public byte at(int x) {
        return fetch(x).at(x);
    }
}
