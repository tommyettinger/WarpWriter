package warpwriter.model.fetch;

import warpwriter.model.FetchModel;

/**
 * Adds an animated 3D cursor overlay on top of a FetchModel.
 * The selected voxel's position is restricted to being inside the bounds of the model somewhere.
 *
 * @author Ben McLean
 */
public class CursorModel extends FetchModel {
    protected int interval = 4;

    public final int interval() {
        return interval;
    }

    public CursorModel setInterval(final int interval) {
        this.interval = interval;
        return this;
    }

    protected int frame = 0;

    public final int frame() {
        return frame;
    }

    public CursorModel setFrame(final int frame) {
        this.frame = frame;
        return this;
    }

    protected byte color = 0;

    public final byte color() {
        return color;
    }

    public CursorModel setColor(final byte color) {
        this.color = color;
        return this;
    }

    protected int x = 0, y = 0, z = 0;

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public int z() {
        return z;
    }

    public CursorModel setX(final int x) {
        if (x >= 0 && x < sizeX()) this.x = x;
        return this;
    }

    public CursorModel setY(final int y) {
        if (y >= 0 && y < sizeY()) this.y = y;
        return this;
    }

    public CursorModel setZ(final int z) {
        if (z >= 0 && z < sizeZ()) this.z = z;
        return this;
    }

    public CursorModel set(final int x, final int y, final int z) {
        return setX(x).setY(y).setZ(z);
    }

    public CursorModel addX(final int x) {
        return setX(this.x + x);
    }

    public CursorModel addY(final int y) {
        return setY(this.y + y);
    }

    public CursorModel addZ(final int z) {
        return setZ(this.z + z);
    }

    public CursorModel add(final int x, final int y, final int z) {
        return addX(x).addY(y).addZ(z);
    }

    @Override
    public byte at(int x, int y, int z) {
        return (x == x() && y == y() && (z == z() || isOn(z, z())))
                || (x == x() && z == z() && isOn(y, y()))
                || (y == y() && z == z() && isOn(x, x()))
                ? color : safeNextFetch().at(x, y, z);
    }

    public boolean isOn(final int origin, final int coordinate) {
        return isOn(Math.abs(origin - coordinate));
    }

    public boolean isOn(final int distance) {
        return distance % interval == frame % interval;
    }
}
