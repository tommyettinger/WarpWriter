package warpwriter.model;

/**
 * Rotates integer coordinates.
 * x+ is north, y+ is east and z+ is up.
 * We are following right-hand rotation.
 *
 * @author Ben McLean
 */
public class Turner {
    public enum Face {
        UP, // z+
        DOWN, // z-
        NORTH, // x+
        EAST, // y+
        SOUTH, // x-
        WEST; // y-

        public Face clockX() {
            switch (this) {
                default:
                    return this;
                case UP:
                    return EAST;
                case EAST:
                    return DOWN;
                case DOWN:
                    return WEST;
                case WEST:
                    return UP;
            }
        }

        public Face counterX() {
            switch (this) {
                default:
                    return this;
                case UP:
                    return WEST;
                case WEST:
                    return DOWN;
                case DOWN:
                    return EAST;
                case EAST:
                    return UP;
            }
        }

        public Face clockY() {
            switch (this) {
                default:
                    return this;
                case UP:
                    return NORTH;
                case NORTH:
                    return DOWN;
                case DOWN:
                    return SOUTH;
                case SOUTH:
                    return UP;
            }
        }

        public Face counterY() {
            switch (this) {
                default:
                    return this;
                case UP:
                    return SOUTH;
                case SOUTH:
                    return DOWN;
                case DOWN:
                    return NORTH;
                case NORTH:
                    return UP;
            }
        }

        public Face clockZ() {
            switch (this) {
                default:
                    return this;
                case NORTH:
                    return WEST;
                case WEST:
                    return SOUTH;
                case SOUTH:
                    return EAST;
                case EAST:
                    return NORTH;
            }
        }

        public Face counterZ() {
            switch (this) {
                default:
                    return this;
                case NORTH:
                    return EAST;
                case EAST:
                    return SOUTH;
                case SOUTH:
                    return WEST;
                case WEST:
                    return NORTH;
            }
        }

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

    public enum Roll {
        NONE, RIGHT, UTURN, LEFT;

        public Roll opposite() {
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

    protected Face face = Face.NORTH;
    protected Roll roll = Roll.NONE;
    protected int x = 0, y = 0, z = 0, centerX = 0, centerY = 0, centerZ = 0;

    public Turner() {
        reset();
    }

    public Turner(Face face, Roll roll) {
        set(face, roll);
    }

    public Turner(Face face, Roll roll, int centerX, int centerY, int centerZ) {
        set(face, roll, centerX, centerY, centerZ);
    }

    public Turner(int centerX, int centerY, int centerZ) {
        setCenter(centerX, centerY, centerZ);
    }

    public Turner(Turner turner) {
        set(turner);
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
        return set(0, 0, 0, Face.NORTH, Roll.NONE, 0, 0, 0);
    }

    public Turner set(int x, int y, int z, Face face, Roll roll, int centerX, int centerY, int centerZ) {
        return set(x, y, z).set(face, roll).setCenter(centerX, centerY, centerZ);
    }

    public Turner set(Face face, Roll roll, int centerX, int centerY, int centerZ) {
        return set(face, roll).setCenter(centerX, centerY, centerZ);
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
        return set(face.clockX(), face == Face.NORTH ? roll.clock() : roll.counter());
    }

    public Turner counterX() {
        return set(face.counterX(), face == Face.NORTH ? roll.counter() : roll.clock());
    }

    public Turner clockY() {
        switch (face) {
            case DOWN: // z-
                set(roll.opposite());
                break;
            case SOUTH: // x-
                set(roll.opposite());
                break;
            case EAST: // y+
                set(roll.counter());
                break;
            case WEST: // y-
                set(roll.clock());
                break;
        }
        return set(face.clockY());
    }

    public Turner counterY() {
        switch (face) {
            case UP: // z+
                set(roll.opposite());
                break;
            case SOUTH: // x-
                set(roll.opposite());
                break;
            case EAST: // y+
                set(roll.clock());
                break;
            case WEST: // y-
                set(roll.counter());
                break;
        }
        return set(face.counterY());
    }

    public Turner clockZ() {
        switch (face) {
            case UP: // z+
                return set(roll.counter());
            case DOWN: // z-
                return set(roll.clock());
            default:
                return set(face.clockZ());
        }
    }

    public Turner counterZ() {
        switch (face) {
            case UP: // z+
                return set(roll.clock());
            case DOWN: // z-
                return set(roll.counter());
            default:
                return set(face.counterZ());
        }
    }

    public Turner setClockX() {
        return set(x, z * -1, y);
    }

    public Turner setCounterX() {
        return set(x, z, y * -1);
    }

    public Turner setClockY() {
        return set(z * -1, y, x);
    }

    public Turner setCounterY() {
        return set(z, y, x * -1);
    }

    public Turner setClockZ() {
        return set(y * -1, x, z);
    }

    public Turner setCounterZ() {
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

    public Turner turn(int x, int y, int z, int centerX, int centerY, int centerZ) {
        return turn(x, y, z, face, roll, centerX, centerY, centerZ);
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

    /**
     * Given the size of a model, sets x(), y(), z() to the offset needed to push the model back onto 0, 0, 0.
     *
     * @return this
     */
    public Turner offsets(int sizeX, int sizeY, int sizeZ) {
        int x = sizeX - 1, y = sizeY - 1, z = sizeZ - 1;
        switch (face) {
            case NORTH:
                switch (roll) {
                    default:
                    case NONE:
                        return set(0, 0, 0);
                    case RIGHT:
                        return set(0, 0, z);
                    case UTURN:
                        return set(0, y, z);
                    case LEFT:
                        return set(0, y, 0);
                }
            case EAST:
            case SOUTH:
                switch (roll) {
                    default:
                    case NONE:
                        return set(x, y, 0);
                    case RIGHT:
                        return set(x, y, z);
                    case UTURN:
                        return set(x, 0, z);
                    case LEFT:
                        return set(x, 0, 0);
                }
            case WEST:
            case UP:
                switch (roll) {
                    default:
                    case NONE:
                        return set(0, 0, z);
                    case RIGHT:
                        return set(x, 0, z);
                    case UTURN:
                        return set(x, y, z);
                    case LEFT:
                        return set(0, y, z);
                }
            case DOWN:
        }
        return set(0, 0, 0);
    }
}
