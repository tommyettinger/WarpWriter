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
        return modelSize(turner().rotation(axis)); // modelSize() handles negative sizes correctly
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
            return modelSize(rot) - 1; // when rot is negative, we need to go from the end of the axis, not the start
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

    protected int[] temp = new int[]{0, 0, 0};

    @Override
    public byte bite() {
        for (int i = 0; i < temp.length; i++)
            temp[turner().affected(i)] = start(i) + turner().step(i) * chain(i);
        return deferByte(getModel().at(
                temp[0], temp[1], temp[2]
        ));
    }

    /**
     * Allows treating the sizes of the underlying model as if they were in an array. Allows negative values for index,
     * unlike an array, and will correctly treat the negative index that corresponds to a reversed axis as if it was the
     * the corresponding non-reversed axis. This means -1 will be the same as 0, -2 the same as 1, and -3 the same as 2.
     *
     * @param index 0 or -1 for x, 1 or -2 for y, or 2 or -3 for z; negative index values have the same size,
     *              but different starts and directions
     * @return the size of the specified dimension
     */
    public int modelSize(int index) {
        switch (index) {
            case 0:
            case -1:
                return getModel().sizeX();
            case 1:
            case -2:
                return getModel().sizeY();
            case 2:
            case -3:
                return getModel().sizeZ();
            default:
                throw new ArrayIndexOutOfBoundsException();
        }
    }
}
