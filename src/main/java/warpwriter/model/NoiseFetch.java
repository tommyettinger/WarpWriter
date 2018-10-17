package warpwriter.model;

import warpwriter.ModelMaker;

public class NoiseFetch extends Fetch {
    public long seed;
    public byte[] colors;

    public NoiseFetch(byte... colors) {
        this(0, colors);
    }

    public NoiseFetch(long seed, byte... colors) {
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
