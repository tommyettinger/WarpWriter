package warpwriter.model;

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

    public TurnModel set(Turner.Face face) {
        turner.set(face);
        return this;
    }

    public TurnModel set(Turner.Roll roll) {
        turner.set(roll);
        return this;
    }

    public IModel getModel() {
        return iModel;
    }

    public Turner.Face getFace() {
        return turner.face;
    }

    public Turner.Roll getRoll() {
        return turner.roll;
    }

    public int x() {
        return turner.x();
    }

    public int y() {
        return turner.y();
    }

    public int z() {
        return turner.z();
    }

    public TurnModel(IModel iModel, Turner.Face face, Turner.Roll roll) {
        set(iModel).set(face).set(roll);
    }

    public TurnModel size(Turner.Face face, Turner.Roll roll) {
        turner.turn(iModel.xSize(), iModel.ySize(), iModel.zSize(), face, roll);
        return this;
    }

    public TurnModel size() {
        return size(turner.face, turner.roll);
    }

    @Override
    public int xSize() {
        return Math.abs(size().x());
    }

    @Override
    public int ySize() {
        return Math.abs(size().y());
    }

    @Override
    public int zSize() {
        return Math.abs(size().z());
    }

    @Override
    public boolean outside(int x, int y, int z) {
        size();
        return x < 0 || y < 0 || z < 0 || x >= Math.abs(x()) || y >= Math.abs(y()) || z >= Math.abs(z());
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
        int xSize = x(), ySize = y(), zSize = z();
        turner.turn(x, y, z);
        int xAns = x(), yAns = y(), zAns = z();
        if (xSize < 0) xAns -= xSize;
        if (ySize < 0) yAns -= ySize;
        if (zSize < 0) zAns -= zSize;
        return deferByte(iModel.at(xAns, yAns, zAns), x, y, z);
    }
}
