package warpwriter.model.fetch;

import warpwriter.model.Fetch;
import warpwriter.model.nonvoxel.ITurner;
import warpwriter.model.nonvoxel.Turner;

/**
 * Rotates a Fetch chain using a Turner.
 *
 * @author Ben McLean
 */
public class TurnFetch extends Fetch implements ITurner {
    protected Turner turner;

    public TurnFetch() {
        this(Turner.reset);
    }

    public TurnFetch(TurnFetch turnFetch) {
        this(turnFetch.turner());
    }

    public TurnFetch(Turner turner) {
        set(turner);
    }

    public TurnFetch set(Turner turner) {
        this.turner = turner;
        return this;
    }

    public Turner turner() {
        return turner;
    }

    @Override
    public byte at(int x, int y, int z) {
        Turner.tempTurner.set(turner).input(x, y, z);
        return getNextFetch().at(Turner.tempTurner.x(), Turner.tempTurner.y(), Turner.tempTurner.z());
    }

    @Override
    public ITurner counterX() {
        return set(turner.counterX());
    }

    @Override
    public ITurner counterY() {
        return set(turner.counterY());
    }

    @Override
    public ITurner counterZ() {
        return set(turner.counterZ());
    }

    @Override
    public ITurner clockX() {
        return set(turner.clockX());
    }

    @Override
    public ITurner clockY() {
        return set(turner.clockY());
    }

    @Override
    public ITurner clockZ() {
        return set(turner.clockZ());
    }

    @Override
    public ITurner reset() {
        return set(turner.reset());
    }

    @Override
    public float angleX() {
        return turner.angleX();
    }

    @Override
    public float angleY() {
        return turner.angleY();
    }

    @Override
    public float angleZ() {
        return turner.angleZ();
    }
}
