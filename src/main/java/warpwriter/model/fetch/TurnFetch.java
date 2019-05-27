package warpwriter.model.fetch;

import warpwriter.model.Fetch;
import warpwriter.model.nonvoxel.ITurnable;
import warpwriter.model.nonvoxel.Rotation;

/**
 * Rotates a Fetch chain using a Rotation.
 *
 * @author Ben McLean
 */
public class TurnFetch extends Fetch implements ITurnable {
    protected Rotation rotation;

    public TurnFetch() {
        this(Rotation.reset);
    }

    public TurnFetch(TurnFetch turnFetch) {
        this(turnFetch.rotation());
    }

    public TurnFetch(Rotation rotation) {
        set(rotation);
    }

    public TurnFetch set(Rotation rotation) {
        this.rotation = rotation;
        return this;
    }

    public Rotation rotation() {
        return rotation;
    }

    @Override
    public byte at(int x, int y, int z) {
        Rotation.tempTurner.set(rotation).input(x, y, z);
        return getNextFetch().at(Rotation.tempTurner.x(), Rotation.tempTurner.y(), Rotation.tempTurner.z());
    }

    @Override
    public ITurnable counterX() {
        return set(rotation.counterX());
    }

    @Override
    public ITurnable counterY() {
        return set(rotation.counterY());
    }

    @Override
    public ITurnable counterZ() {
        return set(rotation.counterZ());
    }

    @Override
    public ITurnable clockX() {
        return set(rotation.clockX());
    }

    @Override
    public ITurnable clockY() {
        return set(rotation.clockY());
    }

    @Override
    public ITurnable clockZ() {
        return set(rotation.clockZ());
    }

    @Override
    public ITurnable reset() {
        return set(rotation.reset());
    }

    @Override
    public float angleX() {
        return rotation.angleX();
    }

    @Override
    public float angleY() {
        return rotation.angleY();
    }

    @Override
    public float angleZ() {
        return rotation.angleZ();
    }
}
