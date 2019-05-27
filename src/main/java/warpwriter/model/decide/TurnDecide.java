package warpwriter.model.decide;

import warpwriter.model.nonvoxel.Rotation;

/**
 * Uses Rotation to rotate the coordinates of an IDecide
 *
 * @author Ben McLean
 */
public class TurnDecide implements IDecide {
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
}
