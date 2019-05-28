package warpwriter.model.decide;

import warpwriter.model.nonvoxel.ITurnable;
import warpwriter.model.nonvoxel.Rotation;

/**
 * Uses Rotation to rotate the coordinates of an IDecide
 *
 * @author Ben McLean
 */
public class TurnDecide implements IDecide, ITurnable {
    protected IDecide decide;

    public TurnDecide set(IDecide decide) {
        this.decide = decide;
        return this;
    }

    public IDecide getDecide() {
        return decide;
    }

    protected Rotation rotation;

    public Rotation rotation() {
        return rotation;
    }

    public TurnDecide set(Rotation rotation) {
        this.rotation = rotation;
        return this;
    }

    @Override
    public boolean bool(int x, int y, int z) {
        Rotation.tempTurner.set(rotation).input(x, y, z);
        return decide.bool(
                Rotation.tempTurner.x(),
                Rotation.tempTurner.y(),
                Rotation.tempTurner.z()
        );
    }

    @Override
    public TurnDecide counterX() {
        return set(rotation.counterX());
    }

    @Override
    public TurnDecide counterY() {
        return set(rotation.counterY());
    }

    @Override
    public TurnDecide counterZ() {
        return set(rotation.counterZ());
    }

    @Override
    public TurnDecide clockX() {
        return set(rotation.clockX());
    }

    @Override
    public TurnDecide clockY() {
        return set(rotation.clockY());
    }

    @Override
    public TurnDecide clockZ() {
        return set(rotation.clockZ());
    }

    @Override
    public TurnDecide reset() {
        return set(rotation.reset());
    }

//    @Override
    public float angleX() {
        return rotation.angleX();
    }

//    @Override
    public float angleY() {
        return rotation.angleY();
    }

//    @Override
    public float angleZ() {
        return rotation.angleZ();
    }
}
