package warpwriter.model;

/**
 * Rotates integer coordinates about 0, 0, 0
 *
 * @author Ben McLean
 */
public class Rotator {
    /**
     * I hereby declare that z+ is upwards (TOP) x+ is north and y+ is east
     */
    public enum Face {
        TOP, BOTTOM, NORTH, EAST, SOUTH, WEST
    }

    /**
     * We are following right-hand rotation.
     */
    public enum Roll {
        UP, RIGHT, DOWN, LEFT
    }

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

    public Rotator clockZ() {
        return set(y * -1, x, z);
    }

    public Rotator counterZ() {
        return set(y, x * -1, z);
    }

    public Rotator clockX() {
        return set(x, z * -1, y);
    }

    public Rotator counterX() {
        return set(x, z, y * -1);
    }

    public Rotator clockY() {
        return set(z * -1, y, x);
    }

    public Rotator counterY() {
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
                clockY();
                break;
            case NORTH: // x+
            default:
                switch (roll) {
                    case RIGHT:
                        counterX();
                        break;
                    case DOWN:
                        counterX().counterX();
                        break;
                    case LEFT:
                        clockX();
                        break;
                    case UP:
                    default:
                        break;
                }
                break;
            case EAST: // y+
                clockZ();
                switch (roll) {
                    case RIGHT:
                        clockY();
                        break;
                    case DOWN:
                        clockY().clockY();
                        break;
                    case LEFT:
                        counterY();
                        break;
                    case UP:
                    default:
                        break;
                }
                break;
            case SOUTH: // x-
                clockZ().clockZ();
                switch (roll) {
                    case RIGHT:
                        clockX();
                        break;
                    case DOWN:
                        clockX().clockX();
                        break;
                    case LEFT:
                        counterX();
                        break;
                    case UP:
                    default:
                        break;
                }
                break;
            case WEST: // y-
                counterZ();
                switch (roll) {
                    case RIGHT:
                        counterY();
                        break;
                    case DOWN:
                        counterY().counterY();
                        break;
                    case LEFT:
                        clockY();
                        break;
                    case UP:
                    default:
                        break;
                }
                break;
            case TOP: // z+
                counterY();
        }
        return this;
    }
}
