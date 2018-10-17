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
}
