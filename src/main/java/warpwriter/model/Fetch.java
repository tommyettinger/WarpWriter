package warpwriter.model;

import squidpony.squidmath.Noise;
import warpwriter.model.decide.DecideFetch;
import warpwriter.model.decide.IDecide;
import warpwriter.model.fetch.*;
import warpwriter.model.nonvoxel.Turner;

/**
 * This abstract class allows for IFetch implementations to use left-to-right method chaining to defer to other IFetch instances for different coordinates instead of always needing to return a byte themselves.
 * <p>
 * DO NOT override byte at(int x, int y, int z).
 * <p>
 * Instead, child classes are expected to use at least one of the following two options:
 * <p>
 * 1. Override Fetch fetch() to defer to another Fetch. This will be tried first, and if the result is null then bite() will be called instead. Get the coordinates to evaluate from chainX(), chainY() and chainZ().
 * <p>
 * 2. Override byte bite() to decide what byte to use at the end of a chain. It is recommended to wrap return statements in deferByte(byte result) to guarantee smart transparency. Get the coordinates to evaluate from chainX(), chainY() and chainZ().
 * <p>
 * If both methods are overridden then a Fetch can be used both as a filter for other Fetches and as a final fetch, depending on whether or not it is on the end of its chain.
 * <p>
 * Failure to implement the required overrides may result in infinite recursion.
 * <p>
 * To defer to the next method in the chain, use getNextFetch().
 * <p>
 * Coordinates to be sent to the next Fetch in the chain can be set through setChains, setChainX, setChainY and setChainZ.
 *
 * @author Ben McLean
 */
public abstract class Fetch implements IFetch, IDecide {
    /**
     * This method is intended to be overridden with a decision about which Fetch to use for the provided coordinate.
     * <p>
     * Returning null indicates to outside code that bite(x, y, z) should be called instead.
     */
    public Fetch fetch() {
        return getNextFetch();
    }

    /**
     * This method is intended to be overridden with a final decision about which byte to return for a given coordinate.
     *
     * @return A final answer, except that it is recommended to wrap results in deferByte(byte result) to ensure smart transparency in the event of a broken chain or if someone screws up and calls this method in the wrong order.
     */
    public byte bite() {
        return deferByte();
    }

    /**
     * Don't override this method or else you'll break the method chaining!
     * <p>
     * Override bite(int x, int y, int z) instead!
     */
    @Override
    public final byte at(int x, int y, int z) {
        Fetch current, next = this;
        do {
            current = next;
            current.setChains(x, y, z);
            next = current.fetch();
            x = current.chainX();
            y = current.chainY();
            z = current.chainZ();
        } while (next != null);
        return current.bite();
    }

    public final byte at(int y, int z) {
        return at(0, y, z);
    }

    public final byte at(int z) {
        return at(0, z);
    }

    /**
     * @return true if at(x, y, z) returns any value besides 0.
     */
    @Override
    public boolean bool(int x, int y, int z) {
        return at(x, y, z) != (byte) 0;
    }

    protected int chainX, chainY, chainZ;

    public int chainX() {
        return chainX;
    }

    public int chainY() {
        return chainY;
    }

    public int chainZ() {
        return chainZ;
    }

    public Fetch setChainX(int x) {
        chainX = x;
        return this;
    }

    public Fetch setChainY(int y) {
        chainY = y;
        return this;
    }

    public Fetch setChainZ(int z) {
        chainZ = z;
        return this;
    }

    public Fetch setChains(int x, int y, int z) {
        return setChainX(x).setChainY(y).setChainZ(z);
    }

    private Fetch nextFetch;

    public Fetch getNextFetch() {
        return nextFetch;
    }

    public Fetch add(Fetch nextFetch) {
        Fetch current, next = this;
        do {
            current = next;
            next = current.getNextFetch();
        } while (next != null);
        current.nextFetch = nextFetch;
        return this;
    }

    public final Fetch breakChain() {
        return breakChain(null);
    }

    public Fetch breakChain(Fetch nextFetch) {
        this.nextFetch = nextFetch;
        return this;
    }

    public Fetch deferFetch() {
        return getNextFetch() == null ? ColorFetch.transparent : getNextFetch();
    }

    public Fetch deferFetch(byte result) {
        return result == 0 ? deferFetch() : ColorFetch.color(result);
    }

    /**
     * This method does not chain!
     *
     * @return If fetch is null then return getNextFetch(), else return fetch.
     */
    public Fetch deferFetch(Fetch fetch) {
        return fetch == null ? getNextFetch() : fetch;
    }

    /**
     * bite() is generally only called by outside code when there is no next fetch.
     * <p>
     * But just in case someone is naughty and breaks the chain, this method allows for a recovery, starting a new chain if necessary. Wrap the result of your bite(int x, int y, int z) overrides in this instead of returning (byte) 0 to ensure you're transparent.
     * <p>
     * This should be the method which ensures that, when no next method is specified in the chain, the background is always transparent.
     */
    public byte deferByte(byte result) {
        return result == (byte) 0 ? deferFetch().at(chainX(), chainY(), chainZ()) : result;
    }

    public byte deferByte() {
        return deferByte((byte) 0);
    }

    /**
     * This method does not chain!
     * It goes on the end, to transform the results of a chain into an IModel
     */
    public IModel model(int sizeX, int sizeY, int sizeZ) {
        return new FetchModel(this, sizeX, sizeY, sizeZ);
    }

    /**
     * This method does not chain!
     *
     * @param convenience This array is used only to get the size of the model.
     * @return A model version of the current Fetch with the size of the convenience array. The actual contents of the convenience array are not touched past [0][0] (to get the length)
     */
    public IModel model(byte[][][] convenience) {
        return new FetchModel(this, convenience);
    }

    /**
     * This method does not chain!
     *
     * @param model A FetchModel to display at the end of the chain.
     * @return A new FetchModel of the chain with size based on model.
     */
    public FetchModel model(FetchModel model) {
        add(model);
        return new FetchModel(this, model.sizeX(), model.sizeY(), model.sizeZ());
    }

    /**
     * This method does not chain!
     *
     * @param model An IModel to display at the end of the chain.
     * @return A new FetchModel of the chain with size based on model.
     */
    public IModel model(IModel model) {
        return model(new FetchModel(model));
    }

    /**
     * Allows treating the chained values as if they were in an array. Allows negative values for index,
     * unlike an array, and will correctly treat the negative index that corresponds to a reversed axis as if it was the
     * the corresponding non-reversed axis. This means -1 will be the same as 0, -2 the same as 1, and -3 the same as 2.
     *
     * @param index 0 or -1 for x, 1 or -2 for y, or 2 or -3 for z; negative index values have the same size,
     *              but different starts and directions
     * @return the size of the specified dimension
     */
    public int chain(int index) {
        switch (index) {
            case 0:
            case -1:
                return chainX();
            case 1:
            case -2:
                return chainY();
            case 2:
            case -3:
                return chainZ();
            default:
                throw new ArrayIndexOutOfBoundsException();
        }
    }

    /**
     * Allows treating the chained values as if they were in an array. Allows negative values for index,
     * unlike an array, and will correctly treat the negative index that corresponds to a reversed axis as if it was the
     * the corresponding non-reversed axis. This means -1 will be the same as 0, -2 the same as 1, and -3 the same as 2.
     *
     * @param index 0 or -1 for x, 1 or -2 for y, or 2 or -3 for z; negative index values have the same size,
     *              but different starts and directions
     * @param value The value to set
     * @return the size of the specified dimension
     */
    public Fetch setChain(int index, int value) {
        switch (index) {
            case 0:
            case -1:
                return setChainX(value);
            case 1:
            case -2:
                return setChainY(value);
            case 2:
            case -3:
                return setChainZ(value);
            default:
                throw new ArrayIndexOutOfBoundsException();
        }
    }

    public Fetch offsetModel(int sizeX, int sizeY, int sizeZ) {
        return add(new OffsetModel(sizeX, sizeY, sizeZ));
    }

    public Fetch loop(int sizeX, int sizeY, int sizeZ) {
        return add(new Loop(sizeX, sizeY, sizeZ));
    }

    public Fetch arrayModel(byte[][][] bytes) {
        return add(new ArrayModel(bytes));
    }

    public Fetch boxModel(int sizeX, int sizeY, int sizeZ, Fetch color) {
        return add(new BoxModel(sizeX, sizeY, sizeZ, color));
    }

    public Fetch boxModel(byte[][][] convenience, Fetch color) {
        return add(new BoxModel(convenience, color));
    }

    public Fetch boxModel(IModel model, Fetch color) {
        return add(new BoxModel(model, color));
    }

    public Fetch fetchFetch(IFetch iFetch) {
        return add(new FetchFetch(iFetch));
    }

    public Fetch skew(float skewZ) {
        return skew(0f, skewZ);
    }

    public Fetch skew(float skewY, float skewZ) {
        return add(new Skew(skewY, skewZ));
    }

    public Fetch swapper(Swapper.Swap swap) {
        return add(new Swapper(swap));
    }

    public Fetch stripes(int[] widths, Fetch[] stripes) {
        return add(new Stripes(widths, stripes));
    }

    public Fetch stripes(Fetch[] stripes, int[] widths) {
        return stripes(widths, stripes);
    }

    public Fetch chaoticFetch(long seed, byte mainColor) {
        return add(new ChaoticFetch(seed, mainColor));
    }

    public Fetch chaoticFetch(long seed, byte... colors) {
        return add(new ChaoticFetch(seed, colors));
    }

    public Fetch noiseFetch(Noise.Noise3D noise, byte... colors) {
        return add(new NoiseFetch(noise, colors));
    }

    public Fetch decideFetch(IDecide decide, Fetch fetch) {
        return add(new DecideFetch(decide, fetch));
    }

    public Fetch turnFetch(Turner turner) {
        return add(new TurnFetch(turner));
    }
}
