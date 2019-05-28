package warpwriter.model.decide;

import warpwriter.model.nonvoxel.ITurnable;
import warpwriter.model.nonvoxel.Rotation;

public class QuarterDecide implements IDecide, ITurnable {
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

    public QuarterDecide setX(final int x) {
        this.x = x;
        return this;
    }

    public QuarterDecide setY(final int y) {
        this.y = y;
        return this;
    }

    public QuarterDecide setZ(final int z) {
        this.z = z;
        return this;
    }

    public QuarterDecide set(final int x, final int y, final int z) {
        return setX(x).setY(y).setZ(z);
    }

    protected Rotation rotation;

    public QuarterDecide set(Rotation rotation) {
        this.rotation = rotation;
        return this;
    }

    @Override
    public boolean bool(int x, int y, int z) {
        return x < x() && y < y() && z < z();
    }

    @Override
    public QuarterDecide counterX() {
        return set(rotation.counterX());
    }

    @Override
    public QuarterDecide counterY() {
        return set(rotation.counterY());
    }

    @Override
    public QuarterDecide counterZ() {
        return set(rotation.counterZ());
    }

    @Override
    public QuarterDecide clockX() {
        return set(rotation.clockX());
    }

    @Override
    public QuarterDecide clockY() {
        return set(rotation.clockY());
    }

    @Override
    public QuarterDecide clockZ() {
        return set(rotation.clockZ());
    }

    @Override
    public QuarterDecide reset() {
        return set(rotation.reset());
    }
}
