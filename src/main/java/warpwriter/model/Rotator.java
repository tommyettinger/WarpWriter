package warpwriter.model;

/**
 * Rotates integer coordinates about 0, 0, 0
 *
 * @author Ben McLean
 */
public class Rotator {
    /**
     * I hereby declare that z+ is upwards (TOP) y+ is north and x+ is east.
     */
    public enum Face {
        TOP, BOTTOM, NORTH, EAST, SOUTH, WEST
    }

    public enum Roll {UP, RIGHT, DOWN, LEFT}

    protected Face face;
    protected Roll roll;
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

    public Rotator setX(int x) {
        this.x = x;
        return this;
    }

    public Rotator setY(int y) {
        this.y = y;
        return this;
    }

    public Rotator setZ(int z) {
        this.z = z;
        return this;
    }

    public Rotator set(int x, int y, int z) {
        return setX(x).setY(y).setZ(z);
    }

    public Rotator set(Face face) {
        this.face = face;
        return this;
    }

    public Rotator set(Roll roll) {
        this.roll = roll;
        return this;
    }

    public Rotator clockwiseXY() {
        return set(y * -1, x, z);
    }

    public Rotator counterXY() {
        return set(y, x * -1, z);
    }

    public Rotator clockwiseYZ() {
        return set(x, z * -1, y);
    }

    public Rotator counterYZ() {
        return set(x, z, y * -1);
    }

    public Rotator clockwiseXZ() {
        return set(z * -1, y, x);
    }

    public Rotator counterXZ() {
        return set(z, y, x * -1);
    }

    public Rotator turn() {
        return turn(x, y, z);
    }

    public Rotator turn(Face face, Roll roll) {
        return turn(x, y, z, face, roll);
    }

    public Rotator turn(int x, int y, int z) {
        return turn(x, y, z, face, roll);
    }

    public Rotator turn(int x, int y, int z, Face face, Roll roll) {
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
                        break;
                    case DOWN:
                        clockwiseXY().clockwiseXY();
                        break;
                    case LEFT:
                        counterXY();
                        break;
                    case UP:
                    default:
                        break;
                }
                break;
        }
        return this;
    }
}
