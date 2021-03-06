package warpwriter.model.fetch;

import squidpony.squidmath.FastNoise;
import squidpony.squidmath.Noise;

public class NoiseHeightMap implements IHeightMap {
    protected long seed=0;
    protected Noise.Noise2D noise;
    
    public NoiseHeightMap () {
        this(FastNoise.instance, 0);
    }
    public NoiseHeightMap (long seed) {
        this(FastNoise.instance, seed);
    }
    public NoiseHeightMap (Noise.Noise2D noise, long seed) {
        setSeed(seed);
        setNoise(noise);
    }

    public NoiseHeightMap setSeed(long seed) {
        this.seed = seed;
        return this;
    }

    public NoiseHeightMap setNoise(Noise.Noise2D noise){
        this.noise = noise;
        return this;
    }

    @Override
    public double heightMap(double x, double y) {
        return noise.getNoiseWithSeed(x, y, seed);
    }
}
