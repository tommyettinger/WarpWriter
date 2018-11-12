package warpwriter.model;

/**
 * Rotates models at 90 degree angles, including their sizes
 *
 * @author Ben McLean
 */
public class TurnModel extends Fetch implements IModel {
    protected IModel iModel;
    protected Turner turner;

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

    public TurnModel(IModel iModel, Turner turner) {
        set(iModel).set(turner);
    }

    public TurnModel(IModel iModel) {
        set(iModel).set(new Turner());
    }

    public TurnModel size(Turner.Face face, Turner.Roll roll) {
        turner.turn(iModel.xSize(), iModel.ySize(), iModel.zSize(), face, roll);
        return this;
    }

    public TurnModel size() {
        return size(turner().face(), turner().roll());
    }

    @Override
    public int xSize() {
        return Math.abs(size().turner().x());
    }

    @Override
    public int ySize() {
        return Math.abs(size().turner().y());
    }

    @Override
    public int zSize() {
        return Math.abs(size().turner().z());
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
        int x = xChain(), y = yChain(), z = zChain();
        size();
        int xSize = turner().x(), ySize = turner().y(), zSize = turner().z();
        turner().turn(x, y, z);
        return deferByte(getModel().at(
                xSize < 0 ?
                        turner().x() + getModel().xSize() - 1
                        : turner().x(),
                ySize < 0 ?
                        turner().y() + getModel().ySize() - 1
                        : turner().y(),
                zSize < 0 ?
                        turner().z() + getModel().zSize() - 1
                        : turner().z()
        ));
    }
}
