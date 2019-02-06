package warpwriter.model.fetch;

import squidpony.squidmath.FastNoise;

public class TerracedHeightMap implements IHeightMap {
    protected int seed=0;
    protected FastNoise noise;
    protected int layers;
    private double inverseLayers;
    public TerracedHeightMap() {
        this(0, 4);
    }
    public TerracedHeightMap(int seed) {
        this(seed, 4);
    }
    public TerracedHeightMap(int seed, int layers) {
        noise = new FastNoise(seed, 0x1.8p-6f, FastNoise.SIMPLEX_FRACTAL, 3, 1.777f, 0.5625f); // 16/9 and 9/16
        setSeed(seed);
        setLayers(layers);
    }

    public TerracedHeightMap setSeed(int seed) {
        this.seed = seed;
        noise.setSeed(seed);
        return this;
    }
    public TerracedHeightMap setLayers(int layers) {
        if(Math.max(2, layers) != this.layers) {
            this.layers = Math.max(2, layers);
            inverseLayers = 1.0 / layers;
        }
        return this;
    }

    @Override
    public double heightMap(double x, double y) {
        return (int)(noise.getSimplexFractal((float) x, (float) y) * layers + layers) * inverseLayers;
    }
}
