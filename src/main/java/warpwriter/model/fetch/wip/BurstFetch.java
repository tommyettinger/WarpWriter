package warpwriter.model.fetch.wip;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import squidpony.squidmath.NumberTools;
import warpwriter.model.Fetch;

/**
 * Created by Tommy Ettinger on 1/25/2019.
 */
public class BurstFetch extends Fetch {
    protected int centerX, centerY, centerZ;
    protected int strength = 3;
    protected int duration = 16;
    protected int frame = 0;
    protected Fetch fetch;

    public BurstFetch(int centerX, int centerY, int centerZ, int strength, int duration, Fetch fetch) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.centerZ = centerZ;
        this.strength = strength;
        this.duration = duration;
        this.fetch = fetch;
    }

    public int getCenterX() {
        return centerX;
    }

    public void setCenterX(int centerX) {
        this.centerX = centerX;
    }

    public int getCenterY() {
        return centerY;
    }

    public void setCenterY(int centerY) {
        this.centerY = centerY;
    }

    public int getCenterZ() {
        return centerZ;
    }

    public void setCenterZ(int centerZ) {
        this.centerZ = centerZ;
    }

    public int getStrength() {
        return strength;
    }

    public void setStrength(int strength) {
        this.strength = strength;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getFrame() {
        return frame;
    }

    public void setFrame(int frame) {
        this.frame = frame;
    }

    public Fetch getFetch() {
        return fetch;
    }

    public void setFetch(Fetch fetch) {
        this.fetch = fetch;
    }

    /**
     * This method is intended to be overridden with a decision about which Fetch to use for the provided coordinate.
     * <p>
     * Returning null indicates to outside code that {@link #bite()} should be called instead.
     */
    @Override
    public Fetch fetch() {
        int x = chainX(), y = chainY(), z = chainZ(), f = frame + 1;
        float groundMag = Vector2.len(x - centerX, y - centerY), mag = Vector3.len(x - centerX, y - centerY, z - centerZ),
                angle = NumberTools.atan2(y - centerY, x - centerX),
                rise = MathUtils.sin(NumberTools.atan2(z - centerZ, groundMag)),
                portion = f / (float)duration,
                rising = f / (strength + 1f), falling = (f - strength - 1f) / (duration - strength - 1f),
                changeX = MathUtils.cos(angle) * strength * f, changeY = MathUtils.sin(angle) * strength * f,
                changeZ = (frame <= strength)
                        ? Math.max(0f, Interpolation.circleOut.apply(z, z + rise * strength * f, rising))
                        : Math.max(0f, z + falling * strength * f);
        return fetch == null ?
                setChains(x + Math.round(changeX), y + Math.round(changeY), z + Math.round(changeZ)).getNextFetch()
                : deferFetch(fetch.setChains(x + Math.round(changeX), y + Math.round(changeY), z + Math.round(changeZ)));
    }
}
