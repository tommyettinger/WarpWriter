package warpwriter.model.decide;

/**
 * Inverts the decision of another IDecide
 *
 * @author Ben McLean
 */
public class NotDecide implements IDecide {
    protected IDecide decide;

    public IDecide decide() {
        return decide;
    }

    public NotDecide set(IDecide decide) {
        this.decide = decide;
        return this;
    }

    @Override
    public boolean bool(int x, int y, int z) {
        return !decide.bool(x, y, z);
    }
}
