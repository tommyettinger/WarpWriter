package warpwriter.model.decide;

import warpwriter.model.nonvoxel.Turner;

/**
 * Uses Turner to rotate the coordinates of an IDecide
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

    protected Turner turner;

    public Turner turner() {
        return turner;
    }

    public TurnDecide set(Turner turner) {
        this.turner = turner;
        return this;
    }

    @Override
    public boolean bool(int x, int y, int z) {
        Turner.tempTurner.set(turner).input(x, y, z);
        return decide.bool(
                Turner.tempTurner.x(),
                Turner.tempTurner.y(),
                Turner.tempTurner.z()
        );
    }
}
