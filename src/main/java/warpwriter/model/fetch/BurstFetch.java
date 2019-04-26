package warpwriter.model.fetch;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import squidpony.squidmath.Noise;
import squidpony.squidmath.NumberTools;
import warpwriter.model.Fetch;
import warpwriter.model.ITemporal;

public class BurstFetch extends Fetch implements ITemporal {
    protected Fetch debrisSource;

    public BurstFetch(Fetch debrisSource, int centerX, int centerY, int centerZ, int duration, int strength) {
        this.debrisSource = debrisSource;
        this.centerX = centerX;
        this.centerY = centerY;
        this.centerZ = centerZ;
        this.strength = strength;
        this.duration = duration;
    }

    public BurstFetch set(Fetch debrisSource) {
        this.debrisSource = debrisSource;
        return this;
    }

    public Fetch getModel() {
        return debrisSource;
    }

    protected int centerX = 0, centerY = 0, centerZ = 0, frame = 0;
    protected int strength = 3;
    protected int duration = 16;
    protected int seed = 424242;

    public BurstFetch setX(int x) {
        this.centerX = x;
        return this;
    }

    public BurstFetch setY(int y) {
        this.centerY = y;
        return this;
    }

    public BurstFetch setZ(int z) {
        this.centerZ = z;
        return this;
    }

    public int seed() {
        return seed;
    }

    public BurstFetch setSeed(int seed) {
        this.seed = seed;
        return this;
    }

    public BurstFetch setCenter(int x, int y, int z) {
        return setX(x).setY(y).setZ(z);
    }

    public BurstFetch setFrame(int frame) {
        this.frame = frame;
        return this;
    }
    public int strength() {
        return strength;
    }

    public BurstFetch setStrength(int strength) {
        this.strength = strength;
        return this;
    }

    public int duration() {
        return duration;
    }

    public BurstFetch setDuration(int duration) {
        this.duration = duration;
        return this;
    }


    public int x() {
        return centerX;
    }

    public int y() {
        return centerY;
    }

    public int z() {
        return centerZ;
    }

    public int frame() {
        return frame;
    }

    @Override
    public byte at(int x, int y, int z) {
        int f = frame + 1,
                h = Noise.IntPointHash.hashAll(x >>> 1, y >>> 1, z >>> 1, seed);
        float groundMag = Vector2.len(x - centerX, y - centerY),
                angle = NumberTools.atan2(y - centerY, x - centerX),
                rise = MathUtils.sin(NumberTools.atan2(z - centerZ, groundMag)) * strength,
                rising = f / (strength + 1f), falling = strength * 0.75f * (f - strength - 1f) / (duration - strength - 1f);
        int changeX = Math.round(MathUtils.cos(angle) * (strength) * f),
                changeY = Math.round(MathUtils.sin(angle) * (strength) * f),
                changeZ = Math.round(
                        (frame <= strength)
                        ? Interpolation.circleOut.apply(0, rise * strength, rising)
                        : (rise - falling) * strength);
        int xx = x - changeX, yy = y - changeY, zz = z - changeZ;
        if((h & 3) > 1)
        {
            xx = x - changeX * (h & 3);
            yy = y - changeY * (h & 3);
            zz = z - changeZ * (h & 3);
        }
        if (debrisSource.bool(
                xx,
                yy,
                zz) && (Noise.IntPointHash.hashAll(xx, yy, zz, seed) >>> -f ^ h >>> -f) == 0
        ) { // if there's debris at this coordinate at this time
            //setChains(xx, yy, zz);
            return debrisSource.at(x, y, z);
        }
        // if there's no debris at this coordinate at this time
        return getNextFetch().at(x, y, z);
    }
}
