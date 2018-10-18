package warpwriter.model;

import warpwriter.ModelMaker;

/**
 * A Fetch that pseudo-randomly chooses a color from a given array or vararg of byte colors, with (hopefully) no
 * correlation between two coordinates and the color that each is given. This looks like white noise, and is not
 * continuous like {@link NoiseFetch} (that Fetch changes smoothly, this doesn't).
 */
public class ChaoticFetch extends Fetch {
    public long seed;
    public byte[] colors;

    public ChaoticFetch(byte... colors) {
        this(0, colors);
    }

    public ChaoticFetch(long seed, byte... colors) {
        this.seed = seed;
        this.colors = colors;
    }

    public byte bite(int x, int y, int z) {
        return colors[ModelMaker.hashBounded(x, y, z, seed, colors.length)];
    }

    /**
     * This is a final fetch. Anything added after it should be ignored.
     *
     * @param fetch Ignored
     * @return this
     */
    @Override
    public Fetch add(Fetch fetch) {
        return this;
    }

    /**
     * This is a final fetch. Anything added after it should be ignored.
     *
     * @param fetch Ignored
     * @return this
     */
    @Override
    public Fetch breakChain(Fetch fetch) {
        return this;
    }
}
