package warpwriter.model;

import squidpony.squidmath.Noise;
import warpwriter.model.decide.DecideFetch;
import warpwriter.model.decide.IDecide;
import warpwriter.model.fetch.*;
import warpwriter.model.nonvoxel.Turner;

/**
 * This abstract class allows for IFetch implementations to use left-to-right method chaining to defer to other IFetch
 * instances for different coordinates instead of always needing to return a byte themselves.
 *
 * Override {@link #at(int, int, int)} but make sure to call nextFetch().at(x, y, z) if your result is 0 for transparency support.
 *
 * @author Ben McLean
 */
public abstract class Fetch implements IFetch, IDecide {
    public final byte at(int y, int z) {
        return at(0, y, z);
    }

    public final byte at(int z) {
        return at(0, z);
    }

    /**
     * @return true if {@link #at(int, int, int)} given {@code x, y, z} returns any value besides 0.
     */
    @Override
    public boolean bool(int x, int y, int z) {
        return at(x, y, z) != (byte) 0;
    }

    private Fetch nextFetch;

    public Fetch getNextFetch() {
        return nextFetch;
    }

    /**
     * @return Either {@link #getNextFetch()} or else a transparent ColorFetch in case {@link #getNextFetch()} was null.
     */
    public Fetch safeNextFetch() {
        final Fetch nextFetch = getNextFetch();
        return nextFetch == null ? ColorFetch.color((byte) 0) : nextFetch;
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

    public Fetch deferFetch(final byte result) {
        return result == 0 ? deferFetch() : ColorFetch.color(result);
    }

    /**
     * This method does not chain!
     *
     * @return If fetch is null then return getNextFetch(), else return fetch.
     */
    public Fetch deferFetch(final Fetch fetch) {
        Fetch newFetch = fetch == null ? getNextFetch() : fetch;
        return newFetch == null ? ColorFetch.color((byte) 0) : newFetch;
    }

    public byte deferByte(final byte voxel, final int x, final int y, final int z) {
        return voxel == 0 ? safeNextFetch().at(x, y, z) : voxel;
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

    public Fetch chaoticFetch(int seed, byte mainColor) {
        return add(new ChaoticFetch(seed, mainColor));
    }

    public Fetch chaoticFetch(int seed, byte... colors) {
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
