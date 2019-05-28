package warpwriter.model.decide;

import warpwriter.model.nonvoxel.ITurnable;
import warpwriter.model.nonvoxel.Rotation;

public class QuadrantDecide implements IDecide, ITurnable {
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

    public QuadrantDecide setX(final int x) {
        this.x = x;
        return this;
    }

    public QuadrantDecide setY(final int y) {
        this.y = y;
        return this;
    }

    public QuadrantDecide setZ(final int z) {
        this.z = z;
        return this;
    }

    public QuadrantDecide addX(final int x) {
        return setX(this.x + x);
    }

    public QuadrantDecide addY(final int y) {
        return setY(this.y + y);
    }

    public QuadrantDecide addZ(final int z) {
        return setZ(this.z + z);
    }

    public QuadrantDecide add(final int x, final int y, final int z) {
        return addX(x).addY(y).addZ(z);
    }

    public QuadrantDecide set(final int x, final int y, final int z) {
        return setX(x).setY(y).setZ(z);
    }

    protected Rotation rotation = Rotation.reset;

    public Rotation rotation() {
        return rotation;
    }

    public QuadrantDecide set(Rotation rotation) {
        this.rotation = rotation;
        return this;
    }

    @Override
    public boolean bool(int x, int y, int z) {
        switch (rotation) {
            case EAST1: case NORTH2:
                return x < x() && y < y() && z < z();
            case SOUTH1: case EAST2:
                return x > x() && y < y() && z < z();
            case NORTH1: case WEST2:
                return x < x() && y > y () &&  z < z();
            case SOUTH2: case WEST1:
                return x > x() && y > y() && z < z();
            case EAST0: case NORTH3:
                return x < x() && y < y() && z > z();
            default: case SOUTH0: case EAST3:
                return x > x() && y < y() && z > z();
            case NORTH0: case WEST3:
                return x < x() && y > y() && z > z();
            case SOUTH3: case WEST0:
                return x > x() && y > y() && z > z();
        }
    }

    @Override
    public QuadrantDecide counterX() {
        return set(rotation.counterX());
    }

    @Override
    public QuadrantDecide counterY() {
        return set(rotation.counterY());
    }

    @Override
    public QuadrantDecide counterZ() {
        return set(rotation.counterZ());
    }

    @Override
    public QuadrantDecide clockX() {
        return set(rotation.clockX());
    }

    @Override
    public QuadrantDecide clockY() {
        return set(rotation.clockY());
    }

    @Override
    public QuadrantDecide clockZ() {
        return set(rotation.clockZ());
    }

    @Override
    public QuadrantDecide reset() {
        return set(rotation.reset());
    }
}
