package warpwriter.model;

import squidpony.squidmath.Noise;
import squidpony.squidmath.WhirlingNoise;

public class NoiseFetch extends Fetch {
    public Noise.Noise3D noise;
    public byte[] colors;

    public NoiseFetch(byte... colors) {
        this(WhirlingNoise.instance, colors);
    }

    public NoiseFetch(Noise.Noise3D noise, byte... colors) {
        this.noise = noise;
        this.colors = colors;
    }

    public byte bite(int x, int y, int z) {
        return colors[(int)((noise.getNoise(x * 0.12, y * 0.12, z * 0.12) * 0.499999 + 0.5) * colors.length)];
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
