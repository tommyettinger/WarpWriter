package warpwriter.model;

/**
 * Rotates models at 90 degree angles, including their sizes
 *
 * @author Ben McLean
 */
public class TurnModel extends Fetch implements IModel {
    protected IModel iModel;
    protected Rotator rotator = new Rotator();

    public TurnModel set(IModel iModel) {
        this.iModel = iModel;
        return this;
    }

    public TurnModel set(Rotator.Face face) {
        rotator.set(face);
        return this;
    }

    public TurnModel set(Rotator.Roll roll) {
        rotator.set(roll);
        return this;
    }

    public IModel getModel() {
        return iModel;
    }

    public Rotator.Face getFace() {
        return rotator.face;
    }

    public Rotator.Roll getRoll() {
        return rotator.roll;
    }

    public int x() {
        return rotator.x();
    }

    public int y() {
        return rotator.y();
    }

    public int z() {
        return rotator.z();
    }

    public TurnModel(IModel iModel, Rotator.Face face, Rotator.Roll roll) {
        set(iModel).set(face).set(roll);
    }

    public TurnModel size(Rotator.Face face, Rotator.Roll roll) {
        rotator.turn(iModel.xSize(), iModel.ySize(), iModel.zSize(), face, roll);
        return this;
    }

    public TurnModel size() {
        return size(rotator.face, rotator.roll);
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
        rotator.turn(x, y, z);
        int xAns = x(), yAns = y(), zAns = z();
        if (xSize < 0) xAns -= xSize;
        if (ySize < 0) yAns -= ySize;
        if (zSize < 0) zAns -= zSize;
        return deferByte(iModel.at(xAns, yAns, zAns), x, y, z);
    }
}
