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

    @Override
    public int sizeX() {
        return modelSize(turner().affected(0));
    }

    @Override
    public int sizeY() {
        return modelSize(turner().affected(1));
    }

    @Override
    public int sizeZ() {
        return modelSize(turner().affected(2));
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
//        turner().offsets(getModel().sizeX(), getModel().sizeY(), getModel().sizeZ());
//        int offsetX = turner().x(), offsetY = turner().y(), offsetZ = turner().z();
        turner().input(chainX(), chainY(), chainZ());
        final int x = turner().x(),
                y = turner().y(),
                z = turner().z(),
                sizeX = modelSize(turner().affected(0)) - 1,
                sizeY = modelSize(turner().affected(1)) - 1,
                sizeZ = modelSize(turner().affected(2)) - 1;

        return deferByte(getModel().at(
                turner().rotation(0) < 0 ? sizeX - x : x,
                turner().rotation(1) < 0 ? sizeY - y : y,
                turner().rotation(2) < 0 ? sizeZ - z : z
        ));
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
