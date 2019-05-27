package warpwriter.model;

import warpwriter.model.nonvoxel.ITurnable;
import warpwriter.model.nonvoxel.Rotation;

/**
 * Rotates models at 90 degree angles, including their sizes
 *
 * @author Ben McLean
 */
public class TurnModel extends Fetch implements IModel, ITurnable {
    protected IModel iModel;
    protected Rotation rotation = Rotation.reset;

    public TurnModel set(IModel iModel) {
        this.iModel = iModel;
        return size();
    }

    public TurnModel set(Rotation rotation) {
        this.rotation = rotation;
        return this;
    }

    public IModel getModel() {
        return iModel;
    }

    public Rotation rotation() {
        return rotation;
    }

    public TurnModel size() {
        Rotation.tempTurner.set(rotation).input(getModel().sizeX(), getModel().sizeY(), getModel().sizeZ());
        return this;
    }

    /**
     * @param axis 0 for x, 1 for y, 2 for z
     * @return Model size after rotation
     */
    public int size(int axis) {
        return modelSize(rotation().rotation(axis)); // modelSize() handles negative sizes correctly
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
        final int rot = rotation().rotation(axis);
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
     * Side effect: Uses the input values in rotation as temporary storage, wiping out whatever may be stored in there. Outputs of rotation are irrelevant, as rotation is being used in this case to store rotation data without being used to actually implement the rotation.
     */
    @Override
    public byte at(int x, int y, int z) {
        Rotation.tempTurner
                .set(rotation)
        .input(rotation().affected(0), start(0) + rotation().step(0) * x)
        .input(rotation().affected(1), start(1) + rotation().step(1) * y)
        .input(rotation().affected(2), start(2) + rotation().step(2) * z);
        return deferByte(getModel().at(
                Rotation.tempTurner.input(0), Rotation.tempTurner.input(1), Rotation.tempTurner.input(2)
        ), x, y, z);
    }

    /**
     * This is a slightly slower version of bite() which shows what it would look like if it relied on rotation to actually implement rotation rather than just to store rotation data.
     * <p>
     * It is not used because using it would involve six reverse lookups on the rotation array instead of none.
     *
     * @return Output identical to bite()
     */
    private byte slowBite(int x, int y, int z) {
        Rotation.tempTurner.set(rotation).input(x, y, z);
        return deferByte(getModel().at(
                Rotation.tempTurner.x() + start(rotation.reverseLookup(0)),
                Rotation.tempTurner.y() + start(rotation.reverseLookup(1)),
                Rotation.tempTurner.z() + start(rotation.reverseLookup(2))
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

    @Override
    public TurnModel counterX() {
        return set(rotation.counterZ());
    }

    @Override
    public TurnModel counterY() {
        return set(rotation.counterY());
    }

    @Override
    public TurnModel counterZ() {
        return set(rotation.counterZ());
    }

    @Override
    public TurnModel clockX() {
        return set(rotation.clockX());
    }

    @Override
    public TurnModel clockY() {
        return set(rotation.clockY());
    }

    @Override
    public TurnModel clockZ() {
        return set(rotation.clockZ());
    }

    @Override
    public TurnModel reset() {
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
