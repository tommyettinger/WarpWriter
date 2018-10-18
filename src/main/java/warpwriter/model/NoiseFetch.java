package warpwriter.model;

import warpwriter.ModelMaker;

public class NoiseFetch extends Fetch {
    protected long seed;
    protected byte[] colors;
    protected static byte[] allColors;

    public NoiseFetch (long seed) {
        if (allColors == null) {
            allColors = new byte[256];
            for (int i = 0; i < 256; i++)
                allColors[i] = (byte)(i & 0xFF); // TODO: This is almost certainly wrong. Needs a proper int to byte conversion.
        }
        setSeed(seed);
        setColors(allColors);
    }

    public NoiseFetch(byte... colors) {
        this(0, colors);
    }

    public NoiseFetch(long seed, byte... colors) {
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

    public NoiseFetch setColors (byte[] colors) {
        this.colors = colors;
        return this;
    }

    public NoiseFetch setSeed(long seed) {
        this.seed = seed;
        return this;
    }
}
