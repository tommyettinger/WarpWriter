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
    public Fetch fetch(int x, int y, int z) {
        return deferFetch(bite(x, y, z), x, y, z);
    }

    @Override
    public byte bite(int x, int y, int z) {
        size();
        int xSize = turner().x(), ySize = turner().y(), zSize = turner().z();
        turner().turn(x, y, z);
        int xAns = turner().x(), yAns = turner().y(), zAns = turner().z();
        if (xSize < 0) xAns += getModel().xSize() - 1;
        if (ySize < 0) yAns += getModel().ySize() - 1;
        if (zSize < 0) zAns += getModel().zSize() - 1;
        return deferByte(getModel().at(xAns, yAns, zAns), x, y, z);
    }
}
