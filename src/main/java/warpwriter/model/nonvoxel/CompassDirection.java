package warpwriter.model.nonvoxel;

import squidpony.squidmath.Coord;
import squidpony.squidmath.NumberTools;

public enum CompassDirection {
    NORTH(0, 1),
    NORTH_EAST(1, 1),
    EAST(1, 0),
    SOUTH_EAST(1, -1),
    SOUTH(0, -1),
    SOUTH_WEST(-1, -1),
    WEST(-1, 0),
    NORTH_WEST(-1, 1),
    NONE(0, 0);

    public static final CompassDirection[] CARDINALS = new CompassDirection[]{SOUTH, NORTH, WEST, EAST};
    public static final CompassDirection[] CARDINALS_CLOCKWISE = new CompassDirection[]{SOUTH, EAST, NORTH, WEST};
    public static final CompassDirection[] CARDINALS_COUNTERCLOCKWISE = new CompassDirection[]{SOUTH, WEST, NORTH, EAST};
    public static final CompassDirection[] DIAGONALS = new CompassDirection[]{SOUTH_WEST, SOUTH_EAST, NORTH_WEST, NORTH_EAST};
    public static final CompassDirection[] OUTWARDS = new CompassDirection[]{SOUTH, NORTH, WEST, EAST, SOUTH_WEST, SOUTH_EAST, NORTH_WEST, NORTH_EAST};
    public static final CompassDirection[] CLOCKWISE = new CompassDirection[]{SOUTH, SOUTH_EAST, EAST, NORTH_EAST, NORTH, NORTH_WEST, WEST, SOUTH_WEST};
    public static final CompassDirection[] COUNTERCLOCKWISE = new CompassDirection[]{SOUTH, SOUTH_WEST, WEST, NORTH_WEST, NORTH, NORTH_EAST, EAST, SOUTH_EAST};
    public final int deltaX;
    public final int deltaY;

    public static CompassDirection getDirection(int x, int y) {
        if (x == 0 && y == 0) {
            return NONE;
        } else {
            float degree = NumberTools.atan2_(y, x) * 360f; // gets degrees from 0 to 360
            if (degree < 22.5f) {
                return NORTH;
            } else if (degree < 67.5f) {
                return NORTH_EAST;
            } else if (degree < 112.5f) {
                return EAST;
            } else if (degree < 157.5f) {
                return SOUTH_EAST;
            } else if (degree < 202.5f) {
                return SOUTH;
            } else if (degree < 247.5f) {
                return SOUTH_WEST;
            } else if (degree < 292.5f) {
                return WEST;
            } else {
                return degree < 337.5f ? NORTH_WEST : NORTH;
            }
        }
    }

    public static CompassDirection getRoughDirection(int x, int y) {
        x = x == 0 ? 0 : x >> 31 | 1;
        y = y == 0 ? 0 : y >> 31 | 1;
        switch(x) {
            case -1:
                switch(y) {
                    case -1:
                        return SOUTH_WEST;
                    case 1:
                        return NORTH_WEST;
                    default:
                        return WEST;
                }
            case 1:
                switch(y) {
                    case -1:
                        return SOUTH_EAST;
                    case 1:
                        return NORTH_EAST;
                    default:
                        return EAST;
                }
            default:
                switch(y) {
                    case -1:
                        return SOUTH;
                    case 1:
                        return NORTH;
                    default:
                        return NONE;
                }
        }
    }

    public static CompassDirection getCardinalDirection(int x, int y) {
        if (x == 0 && y == 0) {
            return NONE;
        } else {
            int absx = Math.abs(x);
            if (y > absx) {
                return SOUTH;
            } else {
                int absy = Math.abs(y);
                if (absy > absx) {
                    return NORTH;
                } else if (x > 0) {
                    return -y == x ? NORTH : EAST;
                } else {
                    return y == x ? SOUTH : WEST;
                }
            }
        }
    }

    public static CompassDirection toGoTo(Coord from, Coord to) {
        return getDirection(to.x - from.x, to.y - from.y);
    }

    public CompassDirection counterClockwise() {
        switch(this) {
            case SOUTH:
                return SOUTH_EAST;
            case NORTH:
                return NORTH_WEST;
            case WEST:
                return SOUTH_WEST;
            case EAST:
                return NORTH_EAST;
            case SOUTH_WEST:
                return SOUTH;
            case SOUTH_EAST:
                return EAST;
            case NORTH_WEST:
                return WEST;
            case NORTH_EAST:
                return NORTH;
            case NONE:
            default:
                return NONE;
        }
    }

    public CompassDirection clockwise() {
        switch(this) {
            case SOUTH:
                return SOUTH_WEST;
            case NORTH:
                return NORTH_EAST;
            case WEST:
                return NORTH_WEST;
            case EAST:
                return SOUTH_EAST;
            case SOUTH_WEST:
                return WEST;
            case SOUTH_EAST:
                return SOUTH;
            case NORTH_WEST:
                return NORTH;
            case NORTH_EAST:
                return EAST;
            case NONE:
            default:
                return NONE;
        }
    }

    public CompassDirection opposite() {
        switch(this) {
            case SOUTH:
                return NORTH;
            case NORTH:
                return SOUTH;
            case WEST:
                return EAST;
            case EAST:
                return WEST;
            case SOUTH_WEST:
                return NORTH_EAST;
            case SOUTH_EAST:
                return NORTH_WEST;
            case NORTH_WEST:
                return SOUTH_EAST;
            case NORTH_EAST:
                return SOUTH_WEST;
            case NONE:
            default:
                return NONE;
        }
    }

    public boolean isDiagonal() {
        return (this.deltaX & this.deltaY) != 0;
    }

    public boolean isCardinal() {
        return (this.deltaX + this.deltaY & 1) != 0;
    }

    public boolean hasUp() {
        switch(this) {
            case SOUTH:
            case SOUTH_WEST:
            case SOUTH_EAST:
                return true;
            case NORTH:
            case WEST:
            case EAST:
            case NORTH_WEST:
            case NORTH_EAST:
            case NONE:
                return false;
            default:
                throw new IllegalStateException("Unmatched " + this.getClass().getSimpleName() + ": " + this);
        }
    }

    public boolean hasDown() {
        switch(this) {
            case SOUTH:
            case WEST:
            case EAST:
            case SOUTH_WEST:
            case SOUTH_EAST:
            case NONE:
                return false;
            case NORTH:
            case NORTH_WEST:
            case NORTH_EAST:
                return true;
            default:
                throw new IllegalStateException("Unmatched " + this.getClass().getSimpleName() + ": " + this);
        }
    }

    public boolean hasLeft() {
        switch(this) {
            case SOUTH:
            case NORTH:
            case EAST:
            case SOUTH_EAST:
            case NORTH_EAST:
            case NONE:
                return false;
            case WEST:
            case SOUTH_WEST:
            case NORTH_WEST:
                return true;
            default:
                throw new IllegalStateException("Unmatched " + this.getClass().getSimpleName() + ": " + this);
        }
    }

    public boolean hasRight() {
        switch(this) {
            case SOUTH:
            case NORTH:
            case WEST:
            case SOUTH_WEST:
            case NORTH_WEST:
            case NONE:
                return false;
            case EAST:
            case SOUTH_EAST:
            case NORTH_EAST:
                return true;
            default:
                throw new IllegalStateException("Unmatched " + this.getClass().getSimpleName() + ": " + this);
        }
    }

    private CompassDirection(int x, int y) {
        this.deltaX = x;
        this.deltaY = y;
    }

    public CompassDirection right() {
        return clockwise().clockwise();
    }

    public CompassDirection left() {
        return counterClockwise().counterClockwise();
    }
}
