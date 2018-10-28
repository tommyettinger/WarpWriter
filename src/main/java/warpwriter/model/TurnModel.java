package warpwriter.model;

/**
 * Rotates models at 90 degree angles, including their sizes
 *
 * @author Ben McLean
 */
public class TurnModel extends Fetch implements IModel {
    /**
     * I hereby declare that z+ is upwards (TOP) y+ is north and x+ is east.
     */
    public enum Face {
        TOP, BOTTOM, NORTH, EAST, SOUTH, WEST
    }

    public enum Roll {UP, RIGHT, DOWN, LEFT}

    protected IModel iModel;
    protected Face face;
    protected Roll roll;

    public TurnModel set(IModel iModel) {
        this.iModel = iModel;
        return this;
    }

    public TurnModel set(Face face) {
        this.face = face;
        return this;
    }

    public TurnModel set(Roll roll) {
        this.roll = roll;
        return this;
    }

    public IModel getModel() {
        return iModel;
    }

    public Face getFace() {
        return face;
    }

    public Roll getRoll() {
        return roll;
    }

    public TurnModel(IModel iModel, Face face, Roll roll) {
        set(iModel).set(face).set(roll);
    }

    protected int x, y, z;

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public int z() {
        return z;
    }

    public TurnModel setX(int x) {
        this.x = x;
        return this;
    }

    public TurnModel setY(int y) {
        this.y = y;
        return this;
    }

    public TurnModel setZ(int z) {
        this.z = z;
        return this;
    }

    public TurnModel set(int x, int y, int z) {
        return setX(x).setY(y).setZ(z);
    }

    public TurnModel clockwiseXY() {
        return set(y * -1, x, z);
    }

    public TurnModel counterXY() {
        return set(y, x * -1, z);
    }

    public TurnModel clockwiseYZ() {
        return set(x, z * -1, y);
    }

    public TurnModel counterYZ() {
        return set(x, z, y * -1);
    }

    public TurnModel clockwiseXZ() {
        return set(z * -1, y, x);
    }

    public TurnModel counterXZ() {
        return set(z, y, x * -1);
    }

    public TurnModel turn() {
        return turn(x, y, z);
    }

    public TurnModel turn(Face face, Roll roll) {
        return turn(x, y, z, face, roll);
    }

    public TurnModel turn(int x, int y, int z) {
        return turn(x, y, z, face, roll);
    }

    public TurnModel turn(int x, int y, int z, Face face, Roll roll) {
        set(x, y, z);
        switch (face) {
            case BOTTOM: // z-
                clockwiseYZ().clockwiseYZ();
                switch (roll) {
                    case RIGHT:
                        counterXY();
                        break;
                    case DOWN:
                        break;
                    case LEFT:
                        clockwiseXY();
                        break;
                    case UP:
                    default:
                        counterXY().counterXY();
                        break;
                }
                break;
            case NORTH: // y+
                clockwiseYZ();
                switch (roll) {
                    case RIGHT:
                        clockwiseXZ();
                        break;
                    case DOWN:
                        clockwiseXZ().clockwiseXZ();
                        break;
                    case LEFT:
                        counterXZ();
                        break;
                    case UP:
                    default:
                        break;
                }
                break;
            case EAST: // x+
                clockwiseXZ();
                switch (roll) {
                    case RIGHT:
                        clockwiseYZ();
                        break;
                    case DOWN:
                        clockwiseYZ().clockwiseYZ();
                        break;
                    case LEFT:
                        counterYZ();
                        break;
                    case UP:
                    default:
                        break;
                }
                break;
            case SOUTH: // y-
                counterYZ();
                switch (roll) {
                    case RIGHT:
                        counterXZ();
                        break;
                    case DOWN:
                        counterXZ().counterXZ();
                        break;
                    case LEFT:
                        clockwiseXZ();
                        break;
                    case UP:
                    default:
                        break;
                }
                break;
            case WEST: // x-
                counterXZ();
                switch (roll) {
                    case RIGHT:
                        counterYZ();
                        break;
                    case DOWN:
                        counterYZ().counterYZ();
                        break;
                    case LEFT:
                        clockwiseYZ();
                        break;
                    case UP:
                    default:
                        break;
                }
                break;
            case TOP: // z+
            default:
                switch (roll) {
                    case RIGHT:
                        clockwiseXY();
                    case DOWN:
                        clockwiseXY().clockwiseXY();
                    case LEFT:
                        counterXY();
                    case UP:
                    default:
                        break;
                }
                break;
        }
        return this;
    }

    @Override
    public int xSize() {
        turn(iModel.xSize(), iModel.ySize(), iModel.zSize());
        return x;
    }

    @Override
    public int ySize() {
        turn(iModel.xSize(), iModel.ySize(), iModel.zSize());
        return y;
    }

    @Override
    public int zSize() {
        turn(iModel.xSize(), iModel.ySize(), iModel.zSize());
        return z;
    }

    @Override
    public boolean outside(int x, int y, int z) {
        return x < 0 || y < 0 || z < 0 || x >= xSize() || y >= ySize() || z >= zSize();
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
        turn(x, y, z);
        int xAns = x(), yAns = y(), zAns = z();
        return deferByte(iModel.at(Loop.loop(xAns, xSize()), Loop.loop(yAns, ySize()), Loop.loop(zAns, zSize())), x, y, z);
    }
}
