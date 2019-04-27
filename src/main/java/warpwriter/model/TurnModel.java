package warpwriter.model;

import warpwriter.model.nonvoxel.Turner;

/**
 * Rotates models at 90 degree angles, including their sizes
 *
 * @author Ben McLean
 */
public class TurnModel extends Fetch implements IModel {
    protected IModel iModel;
    protected Turner turner = new Turner();

    public TurnModel set(IModel iModel) {
        this.iModel = iModel;
        return this;
    }

    public TurnModel set(Turner turner) {
        this.turner = turner;
        return this;
    }

    public IModel getModel() {
        return iModel;
    }

    public Turner turner() {
        return turner;
    }

    public TurnModel size() {
        turner().input(getModel().sizeX(), getModel().sizeY(), getModel().sizeZ());
        return this;
    }

    /**
     * @param axis 0 for x, 1 for y, 2 for z
     * @return Model size after rotation
     */
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
        return rot < 0 ?
                modelSize(rot) - 1 // when rot is negative, we need to go from the end of the axis, not the start
                : 0;
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
        return x < 0 || y < 0 || z < 0 || x >= sizeX() || y >= sizeY() || z >= sizeZ();
    }

    @Override
    public boolean inside(int x, int y, int z) {
        return !outside(x, y, z);
    }

    /**
     * Side effect: Uses the input values in turner as temporary storage, wiping out whatever may be stored in there. Outputs of turner are irrelevant, as turner is being used in this case to store rotation data without being used to actually implement the rotation.
     */
    @Override
    public byte at(int x, int y, int z) {
        turner.input(turner().affected(0), start(0) + turner().step(0) * x);
        turner.input(turner().affected(1), start(1) + turner().step(1) * y);
        turner.input(turner().affected(2), start(2) + turner().step(2) * z);
        return deferByte(getModel().at(
                turner.input(0), turner.input(1), turner.input(2)
        ), x, y, z);
    }

    /**
     * This is a slightly slower version of bite() which shows what it would look like if it relied on turner to actually implement rotation rather than just to store rotation data.
     * <p>
     * It is not used because using it would involve six reverse lookups on the rotation array instead of none.
     *
     * @return Output identical to bite()
     */
    private byte slowBite(int x, int y, int z) {
        turner.input(x, y, z);
        return deferByte(getModel().at(
                turner.x() + start(turner.reverseLookup(0)),
                turner.y() + start(turner.reverseLookup(1)),
                turner.z() + start(turner.reverseLookup(2))
        ), x, y, z);
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
