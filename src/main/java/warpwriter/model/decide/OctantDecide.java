package warpwriter.model.decide;

import warpwriter.model.nonvoxel.ITurnable;
import warpwriter.model.nonvoxel.Rotation;

public class OctantDecide implements IDecide, ITurnable {
    protected int x = 0, y = 0, z = 0;

    public final int x() {
        return x;
    }

    public final int y() {
        return y;
    }

    public final int z() {
        return z;
    }

    public OctantDecide setX(final int x) {
        this.x = x;
        return this;
    }

    public OctantDecide setY(final int y) {
        this.y = y;
        return this;
    }

    public OctantDecide setZ(final int z) {
        this.z = z;
        return this;
    }

    public OctantDecide addX(final int x) {
        return setX(this.x + x);
    }

    public OctantDecide addY(final int y) {
        return setY(this.y + y);
    }

    public OctantDecide addZ(final int z) {
        return setZ(this.z + z);
    }

    public OctantDecide add(final int x, final int y, final int z) {
        return addX(x).addY(y).addZ(z);
    }

    public OctantDecide set(final int x, final int y, final int z) {
        return setX(x).setY(y).setZ(z);
    }

    protected Rotation rotation = Rotation.reset;

    public Rotation rotation() {
        return rotation;
    }

    public OctantDecide set(Rotation rotation) {
        this.rotation = rotation;
        return this;
    }

    @Override
    public boolean bool(int x, int y, int z) {
        switch (rotation.octant()) {
            case 0:
                return x < x() && y < y() && z < z();
            case 1:
                return x > x() && y < y() && z < z();
            case 2:
                return x < x() && y > y() && z < z();
            case 3:
                return x > x() && y > y() && z < z();
            case 4:
                return x < x() && y < y() && z > z();
            default:
            case 5:
                return x > x() && y < y() && z > z();
            case 6:
                return x < x() && y > y() && z > z();
            case 7:
                return x > x() && y > y() && z > z();
        }
    }

    @Override
    public OctantDecide counterX() {
        return set(rotation.counterX());
    }

    @Override
    public OctantDecide counterY() {
        return set(rotation.counterY());
    }

    @Override
    public OctantDecide counterZ() {
        return set(rotation.counterZ());
    }

    @Override
    public OctantDecide clockX() {
        return set(rotation.clockX());
    }

    @Override
    public OctantDecide clockY() {
        return set(rotation.clockY());
    }

    @Override
    public OctantDecide clockZ() {
        return set(rotation.clockZ());
    }

    @Override
    public OctantDecide reset() {
        return set(rotation.reset());
    }
}
