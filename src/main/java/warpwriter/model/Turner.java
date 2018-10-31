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
        UP, // z+
        DOWN, // z-
        NORTH, // x+
        EAST, // y+
        SOUTH, // x-
        WEST; // y-

        public Face opposite() {
            switch (this) {
                case UP:
                    return DOWN;
                case NORTH:
                default:
                    return SOUTH;
                case EAST:
                    return WEST;
                case SOUTH:
                    return NORTH;
                case WEST:
                    return EAST;
                case DOWN:
                    return UP;
            }
        }
    }

    /**
     * We are following right-hand rotation.
     */
    public enum Roll {
        NONE, RIGHT, UTURN, LEFT;

        public Roll uturn() {
            switch (this) {
                case NONE:
                default:
                    return UTURN;
                case RIGHT:
                    return LEFT;
                case UTURN:
                    return NONE;
                case LEFT:
                    return RIGHT;
            }
        }

        public Roll clock() {
            switch (this) {
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
            switch (this) {
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
    protected int x = 0, y = 0, z = 0, centerX = 0, centerY = 0, centerZ = 0;

    public Turner() {
        this(Face.NORTH, Roll.NONE);
    }

    public Turner(Face face, Roll roll) {
        set(face, roll);
    }

    public Turner(Turner turner) {
        this(turner.face(), turner.roll());
        setCenter(turner.centerX(), turner.centerY(), turner.centerZ());
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

    public int centerX() {
        return centerX;
    }

    public int centerY() {
        return centerY;
    }

    public int centerZ() {
        return centerZ;
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

    public Turner setCenterX(int x) {
        centerX = x;
        return this;
    }

    public Turner setCenterY(int y) {
        centerY = y;
        return this;
    }

    public Turner setCenterZ(int z) {
        centerZ = z;
        return this;
    }

    public Turner setCenter(int x, int y, int z) {
        return setCenterX(x).setCenterY(y).setCenterZ(z);
    }

    public Turner reset() {
        return set(0, 0, 0, Face.NORTH, Roll.NONE);
    }

    public Turner set(int x, int y, int z, Face face, Roll roll) {
        return set(x, y, z).set(face, roll);
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

    public Turner set(Turner turner) {
        return set(turner.face(), turner.roll());
    }

    public Turner clockX() {
        switch (face) {
            case NORTH: // x+
            default:
                return set(roll.clock());
            case EAST: // y+
                return set(Face.DOWN, roll.counter());
            case SOUTH: // x-
                return set(roll.counter());
            case WEST: // y-
                return set(Face.UP, roll.counter());
            case UP: // z+
                return set(Face.EAST, roll.counter());
            case DOWN: // z-
                return set(Face.WEST, roll.counter());
        }
    }

    public Turner counterX() {
        switch (face) {
            case NORTH: // x+
            default:
                return set(roll.counter());
            case EAST: // y+
                return set(Face.UP, roll.clock());
            case SOUTH: // x-
                return set(roll.clock());
            case WEST: // y-
                return set(Face.DOWN, roll.clock());
            case UP: // z+
                return set(Face.WEST, roll.clock());
            case DOWN: // z-
                return set(Face.EAST, roll.clock());
        }
    }

    public Turner clockY() {
        switch (face) {
            case NORTH: // x+
            default:
                return set(Face.DOWN);
            case EAST: // y+
                return set(roll.counter());
            case SOUTH: // x-
                return set(Face.UP, roll.uturn());
            case WEST: // y-
                return set(roll.clock());
            case UP: // z+
                return set(Face.NORTH);
            case DOWN: // z-
                return set(Face.SOUTH, roll.uturn());
        }
    }

    public Turner counterY() {
        switch (face) {
            case NORTH: // x+
            default:
                return set(Face.UP);
            case EAST: // y+
                return set(roll.clock());
            case SOUTH: // x-
                return set(Face.DOWN, roll.uturn());
            case WEST: // y-
                return set(roll.counter());
            case UP: // z+
                return set(Face.SOUTH, roll.uturn());
            case DOWN: // z-
                return set(Face.NORTH);
        }
    }

    public Turner clockZ() {
        switch (face) {
            case NORTH: // x+
            default:
                return set(Face.EAST);
            case EAST: // y+
                return set(Face.SOUTH);
            case SOUTH: // x-
                return set(Face.WEST);
            case WEST: // y-
                return set(Face.NORTH);
            case UP: // z+
                return set(roll.counter());
            case DOWN: // z-
                return set(roll.clock());
        }
    }

    public Turner counterZ() {
        switch (face) {
            case NORTH: // x+
            default:
                return set(Face.WEST);
            case EAST: // y+
                return set(Face.NORTH);
            case SOUTH: // x-
                return set(Face.EAST);
            case WEST: // y-
                return set(Face.SOUTH);
            case UP: // z+
                return set(roll.clock());
            case DOWN: // z-
                return set(roll.counter());
        }
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

    public Turner add(int x, int y, int z) {
        return set(x() + x, y() + y, z() + z);
    }

    public Turner add(Turner turner) {
        return add(turner.face(), turner.roll());
    }

    public Turner add(Face face, Roll roll) {
        switch (face) {
            case NORTH: // x+
            default:
                switch (roll) {
                    case RIGHT:
                        return counterX();
                    case UTURN:
                        return counterX().counterX();
                    case LEFT:
                        return clockX();
                }
                break;
            case EAST: // y+
                clockZ();
                switch (roll) {
                    case RIGHT:
                        return clockY();
                    case UTURN:
                        return clockY().clockY();
                    case LEFT:
                        return counterY();
                }
                break;
            case SOUTH: // x-
                clockZ().clockZ();
                switch (roll) {
                    case RIGHT:
                        return clockX();
                    case UTURN:
                        return clockX().clockX();
                    case LEFT:
                        return counterX();
                }
                break;
            case WEST: // y-
                counterZ();
                switch (roll) {
                    case RIGHT:
                        return counterY();
                    case UTURN:
                        return counterY().counterY();
                    case LEFT:
                        return clockY();
                }
                break;
            case UP: // z+
                counterY();
                switch (roll) {
                    case RIGHT:
                        return clockZ();
                    case UTURN:
                        return counterZ().counterZ();
                    case LEFT:
                        return counterZ();
                }
                break;
            case DOWN: // z-
                clockY();
                switch (roll) {
                    case RIGHT:
                        return counterZ();
                    case UTURN:
                        return clockZ().clockZ();
                    case LEFT:
                        return clockZ();
                }
                break;
        }
        return this;
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
        return turn(x, y, z, face, roll, centerX, centerY, centerZ);
    }

    public Turner turn(int x, int y, int z, Face face, Roll roll, int centerX, int centerY, int centerZ) {
        set(x - centerX, y - centerY, z - centerZ);
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
        return add(centerX, centerY, centerZ);
    }
}
