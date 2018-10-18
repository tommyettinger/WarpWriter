package warpwriter.model;

import squidpony.squidmath.Noise;
import squidpony.squidmath.NumberTools;
import squidpony.squidmath.WhirlingNoise;
import warpwriter.ModelMaker;

public class NoiseFetch extends Fetch {
    protected Noise.Noise3D noise;
    protected byte[] colors;

    public NoiseFetch (Noise.Noise3D noise) {
        setNoise(noise);
        setColors(ModelMaker.randomColorRange(NumberTools.doubleToLongBits(noise.getNoise(1.1, 2.2, 3.3))));
    }

    public NoiseFetch(byte... colors) {
        this(WhirlingNoise.instance, colors);
    }

    public NoiseFetch(Noise.Noise3D noise, byte... colors) {
        this.noise = noise;
        if (colors.length == 1)
            this.colors = ModelMaker.colorRange(colors[0]);
        else
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

    public NoiseFetch setColors (byte[] colors) {
        this.colors = colors;
        return this;
    }

    public NoiseFetch setNoise(Noise.Noise3D noise) {
        this.noise = noise;
        return this;
    }
}
