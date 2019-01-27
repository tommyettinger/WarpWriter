package warpwriter.model.fetch;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import squidpony.squidmath.NumberTools;
import warpwriter.model.Fetch;

public class BurstFetch extends Fetch {
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

    public void setStrength(int strength) {
        this.strength = strength;
    }

    public int duration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
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
    public Fetch fetch() {
        int x = chainX(), y = chainY(), z = chainZ(), f = frame + 1;
        float groundMag = Vector2.len(x - centerX, y - centerY), //mag = Vector3.len(x - centerX, y - centerY, z - centerZ),
                angle = NumberTools.atan2(y - centerY, x - centerX),
                rise = MathUtils.sin(NumberTools.atan2(z - centerZ, groundMag)),
                //portion = f / (float)duration,
                rising = f / (strength + 1f), falling = (f - strength - 1f) / (duration - strength - 1f),
                changeX = MathUtils.cos(angle) * strength * f, changeY = MathUtils.sin(angle) * strength * f,
                changeZ = 0;
//                        (frame <= strength)
//                        ? Math.max(0f, Interpolation.circleOut.apply(z, z + rise * strength, rising))
//                        : Math.max(0f, z + rise * strength - falling * strength * 2f);
        
        if (debrisSource.bool(
                x - Math.round(changeX),
                y - Math.round(changeY),
                z - Math.round(changeZ)
        )) { // if there's debris at this coordinate at this time
            setChains(
                    x - Math.round(changeX),
                    y - Math.round(changeY),
                    z - Math.round(changeZ));
            return debrisSource.setChains(x, y, z);
        }
        // if there's no debris at this coordinate at this time
        return getNextFetch();
    }
}
