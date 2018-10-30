package warpwriter.model;

/**
 * Rotates integer coordinates about 0, 0, 0
 *
 * @author Ben McLean
 */
public class Turner {
    /**
     * I hereby declare that z+ is upwards, x+ is north and y+ is east
     */
    public enum Face {
        UP, DOWN, NORTH, EAST, SOUTH, WEST
    }

    /**
     * We are following right-hand rotation.
     */
    public enum Roll {
        NONE, RIGHT, UTURN, LEFT;

        public Roll uturn() {
            return clock().clock();
        }

        public Roll clock() {
            switch(this) {
                case NONE:
                default:
                    return RIGHT;
                case RIGHT:
                    return UTURN;
                case UTURN:
                    return LEFT;
                case LEFT:
                    return NONE;
            }
        }

        public Roll counter() {
            switch(this) {
                case NONE:
                default:
                    return LEFT;
                case LEFT:
                    return UTURN;
                case UTURN:
                    return RIGHT;
                case RIGHT:
                    return NONE;
            }
        }
    }

    protected Face face;
    protected Roll roll;
    protected int x = 0, y = 0, z = 0;

    public Turner() {
        this(Face.NORTH, Roll.NONE);
    }

    public Turner(Face face, Roll roll) {
        set(face, roll);
    }

    public Turner(Turner turner) {
        this(turner.face(), turner.roll());
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public int z() {
        return z;
    }

    public Face face() {
        return face;
    }

    public Roll roll() {
        return roll;
    }

    public Turner setX(int x) {
        this.x = x;
        return this;
    }

    public Turner setY(int y) {
        this.y = y;
        return this;
    }

    public Turner setZ(int z) {
        this.z = z;
        return this;
    }

    public Turner set(int x, int y, int z) {
        return setX(x).setY(y).setZ(z);
    }

    public Turner set(Face face, Roll roll) {
        return set(face).set(roll);
    }

    public Turner set(Face face) {
        this.face = face;
        return this;
    }

    public Turner set(Roll roll) {
        this.roll = roll;
        return this;
    }

    public Turner clockX() {
        switch (face) {
            case NORTH: // x+
                break;
            case EAST: // y+
                break;
            case SOUTH: // x-
                break;
            case WEST: // y-
                break;
            case UP: // z+
                break;
            case DOWN: // z-
                break;
        }
        return this;
    }

    public Turner counterX() {
        switch (face) {
            case NORTH: // x+
                break;
            case EAST: // y+
                break;
            case SOUTH: // x-
                break;
            case WEST: // y-
                break;
            case UP: // z+
                break;
            case DOWN: // z-
                break;
        }
        return this;
    }

    public Turner clockY() {
        switch (face) {
            case NORTH: // x+
                set(Face.DOWN);
                break;
            case EAST: // y+
                set(roll.counter());
                break;
            case SOUTH: // x-
                set(Face.UP);
                break;
            case WEST: // y-
                set(roll.clock());
                break;
            case UP: // z+
                set(Face.SOUTH);
                set(roll.uturn());
                break;
            case DOWN: // z-
                set(Face.NORTH);
                set(roll.uturn());
                break;
        }
        return this;
    }

    public Turner counterY() {
        switch (face) {
            case NORTH: // x+
                set(Face.UP);
                break;
            case EAST: // y+
                set(roll.clock());
                break;
            case SOUTH: // x-
                set(Face.DOWN);
                break;
            case WEST: // y-
                set(roll.counter());
                break;
            case UP: // z+
                set(Face.NORTH);
                set(roll.uturn());
                break;
            case DOWN: // z-
                set(Face.SOUTH);
                set(roll.uturn());
                break;
        }
        return this;
    }

    public Turner clockZ() {
        switch (face) {
            case NORTH: // x+
                set(Face.EAST);
                break;
            case EAST: // y+
                set(Face.SOUTH);
                break;
            case SOUTH: // x-
                set(Face.WEST);
                break;
            case WEST: // y-
                set(Face.NORTH);
                break;
            case UP: // z+
                set(roll.counter());
                break;
            case DOWN: // z-
                set(roll.clock());
                break;
        }
        return this;
    }

    public Turner counterZ() {
        switch (face) {
            case NORTH: // x+
                set(Face.WEST);
                break;
            case EAST: // y+
                set(Face.NORTH);
                break;
            case SOUTH: // x-
                set(Face.EAST);
                break;
            case WEST: // y-
                set(Face.SOUTH);
                break;
            case UP: // z+
                set(roll.clock());
                break;
            case DOWN: // z-
                set(roll.counter());
                break;
        }
        return this;
    }

    protected Turner setClockX() {
        return set(x, z * -1, y);
    }

    protected Turner setCounterX() {
        return set(x, z, y * -1);
    }

    protected Turner setClockY() {
        return set(z * -1, y, x);
    }

    protected Turner setCounterY() {
        return set(z, y, x * -1);
    }

    protected Turner setClockZ() {
        return set(y * -1, x, z);
    }

    protected Turner setCounterZ() {
        return set(y, x * -1, z);
    }

    public Turner turn() {
        return turn(x, y, z);
    }

    public Turner turn(Face face, Roll roll) {
        return turn(x, y, z, face, roll);
    }

    public Turner turn(int x, int y, int z) {
        return turn(x, y, z, face, roll);
    }

    public Turner turn(int x, int y, int z, Face face, Roll roll) {
        set(x, y, z);
        switch (face) {
            case NORTH: // x+
            default:
                switch (roll) {
                    case RIGHT:
                        setCounterX();
                        break;
                    case UTURN:
                        setCounterX().setCounterX();
                        break;
                    case LEFT:
                        setClockX();
                        break;
                }
                break;
            case EAST: // y+
                setClockZ();
                switch (roll) {
                    case RIGHT:
                        setClockY();
                        break;
                    case UTURN:
                        setClockY().setClockY();
                        break;
                    case LEFT:
                        setCounterY();
                        break;
                }
                break;
            case SOUTH: // x-
                setClockZ().setClockZ();
                switch (roll) {
                    case RIGHT:
                        setClockX();
                        break;
                    case UTURN:
                        setClockX().setClockX();
                        break;
                    case LEFT:
                        setCounterX();
                        break;
                }
                break;
            case WEST: // y-
                setCounterZ();
                switch (roll) {
                    case RIGHT:
                        setCounterY();
                        break;
                    case UTURN:
                        setCounterY().setCounterY();
                        break;
                    case LEFT:
                        setClockY();
                        break;
                }
                break;
            case UP: // z+
                setCounterY();
                switch (roll) {
                    case RIGHT:
                        setClockZ();
                        break;
                    case UTURN:
                        setCounterZ().setCounterZ();
                        break;
                    case LEFT:
                        setCounterZ();
                        break;
                }
                break;
            case DOWN: // z-
                setClockY();
                switch (roll) {
                    case RIGHT:
                        setCounterZ();
                        break;
                    case UTURN:
                        setClockZ().setClockZ();
                        break;
                    case LEFT:
                        setClockZ();
                        break;
                }
                break;
        }
        return this;
    }
}
