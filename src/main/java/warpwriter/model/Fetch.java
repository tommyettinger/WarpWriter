package warpwriter.model;

import squidpony.squidmath.Noise;

/**
 * This abstract class allows for IFetch implementations to use left-to-right method chaining to defer to other IFetch instances for different coordinates instead of always needing to return a byte themselves.
 * <p>
 * DO NOT override byte at(int x, int y, int z).
 * <p>
 * Instead, child classes are expected to use at least one of the following two options:
 * <p>
 * 1. Override Fetch fetch(int x, int y, int z) to defer to another Fetch. This will be tried first, and if the result is null then bite(int x, int y, int z) will be called instead.
 * <p>
 * 2. Override byte bite(int x, int y, int z) to decide what byte to use at the end of a chain. It is recommended to wrap return statements in deferByte(byte result, int x, int y, int z) to guarantee smart transparency in the event of a broken chain or if the bite method is accidentally called at the wrong time.
 * <p>
 * If both methods are overridden then a Fetch can be used both as a filter for other Fetches and as a final fetch, depending on whether or not it is on the end of it's chain.
 * <p>
 * Failure to implement the required overrides may result in infinite recursion.
 * <p>
 * To defer to the next method in the chain, use getNextFetch()
 * Coordinates sent to the next Fetch in the chain be manipulated through setChains, setXChain, setYChain and setZChain.
 *
 * @author Ben McLean
 */
public abstract class Fetch implements IFetch, IFetch2D, IFetch1D, IDecide {
    /**
     * This method is intended to be overridden with a decision about which Fetch to use for the provided coordinate.
     * <p>
     * Returning null indicates to outside code that bite(x, y, z) should be called instead.
     */
    public Fetch fetch(int x, int y, int z) {
        return getNextFetch();
    }

    public final Fetch fetch(int y, int z) {
        return fetch(0, y, z);
    }

    public final Fetch fetch(int z) {
        return fetch(0, z);
    }

    /**
     * This method is intended to be overridden with a final decision about which byte to return for a given coordinate.
     *
     * @return A final answer, except that it is recommended to wrap results in deferByte(byte result, int x, int y, int z) to ensure smart transparency in the event of a broken chain or if someone screws up and calls this method in the wrong order.
     */
    public byte bite(int x, int y, int z) {
        return deferByte(x, y, z);
    }

    public final byte bite(int y, int z) {
        return bite(0, y, z);
    }

    public final byte bite(int z) {
        return bite(0, z);
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
            next = current.fetch(x, y, z);
            x = current.xChain;
            y = current.yChain;
            z = current.zChain;
        } while (next != null);
        return current.bite(x, y, z);
    }

    @Override
    public final byte at(int y, int z) {
        return at(0, y, z);
    }

    @Override
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

    public int xChain, yChain, zChain;

    public Fetch setXChain(int x) {
        xChain = x;
        return this;
    }

    public Fetch setYChain(int y) {
        yChain = y;
        return this;
    }

    public Fetch setZChain(int z) {
        zChain = z;
        return this;
    }

    public Fetch setChains(int x, int y, int z) {
        return setXChain(x).setYChain(y).setZChain(z);
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

    public Fetch deferFetch(int x, int y, int z) {
        return getNextFetch() == null ? ColorFetch.transparent : getNextFetch();
    }

    public Fetch deferFetch(byte result, int x, int y, int z) {
        return result == 0 ? deferFetch(x, y, z) : ColorFetch.color(result);
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
     * bite(int x, int y, int z) is only supposed to be called when there is no next fetch.
     * <p>
     * But just in case someone is naughty and breaks the chain, this method allows for a recovery, starting a new chain if necessary. Wrap the result of your bite(int x, int y, int z) overrides in this instead of returning (byte) 0 to ensure you're transparent.
     * <p>
     * This should be the method which ensures that, when no next method is specified in the chain, the background is always transparent.
     */
    public byte deferByte(byte result, int x, int y, int z) {
        return result == (byte) 0 ? deferFetch(x, y, z).at(x, y, z) : result;
    }

    public byte deferByte(int x, int y, int z) {
        return deferByte((byte) 0, x, y, z);
    }

    /**
     * This method does not chain!
     * It goes on the end, to transform the results of a chain into an IModel
     */
    public FetchModel model(int xSize, int ySize, int zSize) {
        return new FetchModel(this, xSize, ySize, zSize);
    }

    /**
     * This method does not chain!
     *
     * @param convenience This array is used only to get the size of the model.
     * @return A model version of the current Fetch with the size of the convenience array. The actual contents of the convenience array are not touched past [0][0] (to get the length)
     */
    public FetchModel model(byte[][][] convenience) {
        return new FetchModel(this, convenience);
    }

    public FetchModel model() {
        return new FetchModel();
    }

    public Fetch offsetModel(int xSize, int ySize, int zSize) {
        return add(new OffsetModel(xSize, ySize, zSize));
    }

    public Fetch loop(int xSize, int ySize, int zSize) {
        return add(new Loop(xSize, ySize, zSize));
    }

    public Fetch arrayModel(byte[][][] bytes) {
        return add(new ArrayModel(bytes));
    }

    public Fetch boxModel(int xSize, int ySize, int zSize, Fetch color) {
        return add(new BoxModel(xSize, ySize, zSize, color));
    }

    public Fetch boxModel(byte[][][] convenience, Fetch color) {
        return add(new BoxModel(convenience, color));
    }

    public Fetch fetchFetch(IFetch iFetch) {
        return add(new FetchFetch(iFetch));
    }

    public Fetch skew(float zSkew) {
        return skew(0f, zSkew);
    }

    public Fetch skew(float ySkew, float zSkew) {
        return add(new Skew(ySkew, zSkew));
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

    public Fetch turnFetch(int x, int y, int z, Rotator.Face face, Rotator.Roll roll) {
        return add(new TurnFetch(x, y, z, face, roll));
    }

    public Fetch turnFetch(Rotator.Face face, Rotator.Roll roll) {
        return turnFetch(0, 0, 0, face, roll);
    }
}
