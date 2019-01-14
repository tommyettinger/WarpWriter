package warpwriter.model.decide;

/**
 * Adds an offset to an IDecide
 *
 * @author Ben McLean
 */
public class OffsetDecide implements IDecide {
    protected IDecide decide;

    public OffsetDecide set(IDecide decide) {
        this.decide = decide;
        return this;
    }

    public IDecide getDecide() {
        return decide;
    }

    protected int x=0, y=0, z=0;

    public OffsetDecide set(int x, int y, int z) {
        return setX(x).setY(y).setZ(z);
    }

    public OffsetDecide setX(int x) {
        this.x = x;
        return this;
    }

    public OffsetDecide setY(int y) {
        this.y = y;
        return this;
    }

    public OffsetDecide setZ(int z) {
        this.z = z;
        return this;
    }

    @Override
    public boolean bool(int x, int y, int z) {
        return decide.bool(x + this.x, y + this.y, z + this.z);
    }
}
