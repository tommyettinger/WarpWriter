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
        Z_PLUS, // z+
        Z_MINUS, // z-
        X_PLUS, // x+
        Y_PLUS, // y+
        X_MINUS, // x-
        Y_MINUS; // y-

        public Face clockX() {
            switch (this) {
                case Z_PLUS:
                    return Y_PLUS;
                case Y_PLUS:
                    return Z_MINUS;
                case Z_MINUS:
                    return Y_MINUS;
                case Y_MINUS:
                    return Z_PLUS;
                default:
                    return this;
            }
        }

        public Face counterX() {
            switch (this) {
                case Z_PLUS:
                    return Y_MINUS;
                case Y_MINUS:
                    return Z_MINUS;
                case Z_MINUS:
                    return Y_PLUS;
                case Y_PLUS:
                    return Z_PLUS;
                default:
                    return this;
            }
        }

        public Face clockY() {
            switch (this) {
                case Z_MINUS:
                    return X_PLUS;
                case X_PLUS:
                    return Z_PLUS;
                case Z_PLUS:
                    return X_MINUS;
                case X_MINUS:
                    return Z_MINUS;
                default:
                    return this;
            }
        }

        public Face counterY() {
            switch (this) {
                case Z_MINUS:
                    return X_MINUS;
                case X_MINUS:
                    return Z_PLUS;
                case Z_PLUS:
                    return X_PLUS;
                case X_PLUS:
                    return Z_MINUS;
                default:
                    return this;
            }
        }

        public Face clockZ() {
            switch (this) {
                case X_PLUS:
                    return Y_MINUS;
                case Y_MINUS:
                    return X_MINUS;
                case X_MINUS:
                    return Y_PLUS;
                case Y_PLUS:
                    return X_PLUS;
                default:
                    return this;
            }
        }

        public Face counterZ() {
            switch (this) {
                case X_PLUS:
                    return Y_PLUS;
                case Y_PLUS:
                    return X_MINUS;
                case X_MINUS:
                    return Y_MINUS;
                case Y_MINUS:
                    return X_PLUS;
                default:
                    return this;
            }
        }

        public Face opposite() {
            switch (this) {
                case Z_PLUS:
                    return Z_MINUS;
                case Y_PLUS:
                    return Y_MINUS;
                case X_MINUS:
                    return X_PLUS;
                case Y_MINUS:
                    return Y_PLUS;
                case Z_MINUS:
                    return Z_PLUS;
                case X_PLUS:
                default:
                    return X_MINUS;
            }
        }
    }

    public enum Roll {
        TWELVE, THREE, SIX, NINE;

        public Roll opposite() {
            switch (this) {
                case THREE:
                    return NINE;
                case SIX:
                    return TWELVE;
                case NINE:
                    return THREE;
                case TWELVE:
                default:
                    return SIX;
            }
        }

        public Roll clock() {
            switch (this) {
                case THREE:
                    return SIX;
                case SIX:
                    return NINE;
                case NINE:
                    return TWELVE;
                case TWELVE:
                default:
                    return THREE;
            }
        }

        public Roll counter() {
            switch (this) {
                case NINE:
                    return SIX;
                case SIX:
                    return THREE;
                case THREE:
                    return TWELVE;
                case TWELVE:
                default:
                    return NINE;
            }
        }
    }

    protected Face face = Face.X_PLUS;
    protected Roll roll = Roll.TWELVE;
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
        return set(0, 0, 0, Face.X_PLUS, Roll.TWELVE, 0, 0, 0);
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
        switch (face) {
            case X_PLUS:
                return set(roll.clock());
            case X_MINUS:
                return set(roll.counter());
            default:
                return set(face.clockX(), roll.counter());
        }
    }

    public Turner counterX() {
        switch (face) {
            case X_PLUS:
                return set(roll.counter());
            case X_MINUS:
                return set(roll.clock());
            default:
                return set(face.counterX(), roll.clock());
        }
    }

    public Turner clockY() {
        switch (face) {
            case Z_PLUS: // z+
            case X_MINUS: // x-
                set(roll.opposite());
                break;
            case Y_PLUS: // y+
                return set(roll.counter());
            case Y_MINUS: // y-
                return set(roll.clock());
        }
        return set(face.clockY());
    }

    public Turner counterY() {
        switch (face) {
            case Z_MINUS: // z-
            case X_MINUS: // x-
                set(roll.opposite());
                break;
            case Y_PLUS: // y+
                return set(roll.clock());
            case Y_MINUS: // y-
                return set(roll.counter());
        }
        return set(face.counterY());
    }

    public Turner clockZ() {
        switch (face) {
            case Z_PLUS: // z+
                return set(roll.counter());
            case Z_MINUS: // z-
                return set(roll.clock());
            default:
                return set(face.clockZ());
        }
    }

    public Turner counterZ() {
        switch (face) {
            case Z_PLUS: // z+
                return set(roll.clock());
            case Z_MINUS: // z-
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
            case Y_PLUS: // y+
                clockZ();
                switch (roll) {
                    case THREE:
                        return clockY();
                    case SIX:
                        return clockY().clockY();
                    case NINE:
                        return counterY();
                }
                break;
            case X_MINUS: // x-
                clockZ().clockZ();
                switch (roll) {
                    case THREE:
                        return clockX();
                    case SIX:
                        return clockX().clockX();
                    case NINE:
                        return counterX();
                }
                break;
            case Y_MINUS: // y-
                counterZ();
                switch (roll) {
                    case THREE:
                        return counterY();
                    case SIX:
                        return counterY().counterY();
                    case NINE:
                        return clockY();
                }
                break;
            case Z_PLUS: // z+
                counterY();
                switch (roll) {
                    case THREE:
                        return clockZ();
                    case SIX:
                        return counterZ().counterZ();
                    case NINE:
                        return counterZ();
                }
                break;
            case Z_MINUS: // z-
                clockY();
                switch (roll) {
                    case THREE:
                        return counterZ();
                    case SIX:
                        return clockZ().clockZ();
                    case NINE:
                        return clockZ();
                }
                break;
            case X_PLUS: // x+
            default:
                switch (roll) {
                    case THREE:
                        return counterX();
                    case SIX:
                        return counterX().counterX();
                    case NINE:
                        return clockX();
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
            case Y_PLUS: // y+
                setClockZ();
//                switch (roll) {
//                    case THREE:
//                        setCounterY();
//                        break;
//                    case SIX:
//                        setClockY().setClockY();
//                        break;
//                    case NINE:
//                        setClockY();
//                        break;
//                }
                break;
            case X_MINUS: // x-
                setClockZ().setClockZ();
//                switch (roll) {
//                    case THREE:
//                        setClockX();
//                        break;
//                    case SIX:
//                        setClockX().setClockX();
//                        break;
//                    case NINE:
//                        setCounterX();
//                        break;
//                }
                break;
            case Y_MINUS: // y-
                setCounterZ();
//                switch (roll) {
//                    case THREE:
//                        setClockY();
//                        break;
//                    case SIX:
//                        setCounterY().setCounterY();
//                        break;
//                    case NINE:
//                        setCounterY();
//                        break;
//                }
                break;
            case Z_PLUS: // z+
                setCounterY();
//                switch (roll) {
//                    case THREE:
//                        setClockZ();
//                        break;
//                    case SIX:
//                        setCounterZ().setCounterZ();
//                        break;
//                    case NINE:
//                        setCounterZ();
//                        break;
//                }
                break;
            case Z_MINUS: // z-
                setClockY();
//                switch (roll) {
//                    case THREE:
//                        setCounterZ();
//                        break;
//                    case SIX:
//                        setClockZ().setClockZ();
//                        break;
//                    case NINE:
//                        setClockZ();
//                        break;
//                }
                break;
//            case X_PLUS: // x+, no work needed
//            default:
//                break;
        }
        switch (roll) {
            case THREE:
                setClockX();
                break;
            case SIX:
                setCounterX().setCounterX();
                break;
            case NINE:
                setCounterX();
                break;
        }

        return add(centerX, centerY, centerZ);
    }
}
