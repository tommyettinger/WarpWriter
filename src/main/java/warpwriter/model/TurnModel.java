package warpwriter.model;

/**
 * Rotates models at 90 degree angles, including their sizes
 *
 * @author Ben McLean
 */
public class TurnModel extends Fetch implements IModel {
    protected IModel iModel;
    protected Turner2 turner;

    public TurnModel set(IModel iModel) {
        this.iModel = iModel;
        return this;
    }

    public TurnModel set(Turner2 turner) {
        this.turner = turner;
        return this;
    }

    public IModel getModel() {
        return iModel;
    }

    public Turner2 turner() {
        return turner;
    }

    public TurnModel(IModel iModel, Turner2 turner) {
        set(iModel).set(turner);
    }

    public TurnModel(IModel iModel) {
        set(iModel).set(new Turner2());
    }

    public TurnModel size() {
        turner().input(getModel().sizeX(), getModel().sizeY(), getModel().sizeZ());
        return this;
    }

    public int size(int axis) {
        return modelSize(Turner2.flipBits(turner().rotation(axis)));
    }

    @Override
    public int sizeX() {
        return size(0);
    }

    @Override
    public int sizeY() {
        return size(1);
    }

    @Override
    public int sizeZ() {
        return size(2);
    }

    public int start(int axis) {
        final int rot = turner().rotation(axis);
        if (rot < 0)
            return modelSize(~rot) - 1; // when rot is negative, we need to go from the end of the axis, not the start
        else
            return 0;
    }

    public int startX() {
        return start(0);
    }

    public int startY() {
        return start(1);
    }

    public int startZ() {
        return start(2);
    }

    @Override
    public boolean outside(int x, int y, int z) {
        size();
        return x < 0 || y < 0 || z < 0 || x >= Math.abs(turner().x()) || y >= Math.abs(turner().y()) || z >= Math.abs(turner().z());
    }

    @Override
    public boolean inside(int x, int y, int z) {
        return !outside(x, y, z);
    }

    @Override
    public Fetch fetch() {
        return deferFetch(bite());
    }

    @Override
    public byte bite() {
        turner().input(chainX(), chainY(), chainZ());
        return deferByte(getModel().at(
                turn(0), turn(1), turn(2)
        ));
    }

    public int turn(int axis) {
        //return start(axis) + turner().step(axis) * value;
        //return turner().rotation(axis) < 0 ? size(axis) - value : value;
        return turner().turn(axis) + (
                turner().rotation(axis) < 0 ?
                        modelSize(axis) - 1
                        : 0
        );
    }

    /**
     * Allows treating the sizes of the underlying model as if they were in an array.
     *
     * @param index 0 for x, 1 for y or 2 for z
     * @return the size of the specified dimension
     */
    public int modelSize(int index) {
        switch (index) {
            case 0:
                return getModel().sizeX();
            case 1:
                return getModel().sizeY();
            case 2:
                return getModel().sizeZ();
            default:
                throw new ArrayIndexOutOfBoundsException();
        }
    }
}
