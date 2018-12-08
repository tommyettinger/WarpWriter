package warpwriter.model.fetch;

import warpwriter.ModelMaker;
import warpwriter.model.Fetch;

/**
 * A Fetch that pseudo-randomly chooses a color from a given array or vararg of byte colors, with (hopefully) no
 * correlation between two coordinates and the color that each is given. This looks like white noise, and is not
 * continuous like {@link NoiseFetch} (that Fetch changes smoothly, this doesn't).
 */
public class ChaoticFetch extends Fetch {
    protected long seed;
    protected byte[] colors;

    public ChaoticFetch (long seed) {
        setSeed(seed);
        setColors(ModelMaker.randomColorRange(seed));
    }

    public ChaoticFetch(byte... colors) {
        this(0, colors);
    }

    public ChaoticFetch(long seed, byte... colors) {
        this.seed = seed;
        if (colors.length == 1)
            this.colors = ModelMaker.colorRange(colors[0]);
        else
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

    public ChaoticFetch setColors (byte[] colors) {
        this.colors = colors;
        return this;
    }

    public ChaoticFetch setSeed(long seed) {
        this.seed = seed;
        return this;
    }
}
