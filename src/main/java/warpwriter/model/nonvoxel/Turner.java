package warpwriter.model.nonvoxel;

import squidpony.StringKit;

/**
 * Rotates integer coordinates based on right-hand rotation.
 *
 * @author Ben McLean
 */
public enum Turner implements ITurner {
    SOUTH0(0, "SOUTH0", -1, 1, 2),
    SOUTH1(1, "SOUTH1", -1, -3, 1),
    SOUTH2(2, "SOUTH2", -1, -2, -3),
    SOUTH3(3, "SOUTH3", -1, 2, -2),
    EAST0(4, "EAST0", -2, -1, 2),
    EAST1(5, "EAST1", -2, -3, -1),
    EAST2(6, "EAST2", -2, 0, -3),
    EAST3(7, "EAST3", -2, 2, 0),
    NORTH0(8, "NORTH0", 0, -2, 2),
    NORTH1(9, "NORTH1", 0, -3, -2),
    NORTH2(10, "NORTH2", 0, 1, -3),
    NORTH3(11, "NORTH3", 0, 2, 1),
    WEST0(12, "WEST0", 1, 0, 2),
    WEST1(13, "WEST1", 1, -3, 0),
    WEST2(14, "WEST2", 1, -1, -3),
    WEST3(15, "WEST3", 1, 2, -1),
    UP0(16, "UP0", -3, -2, 0),
    UP1(17, "UP1", -3, -1, -2),
    UP2(18, "UP2", -3, 1, -1),
    UP3(19, "UP3", -3, 0, 1),
    DOWN0(20, "DOWN0", 2, -2, -1),
    DOWN1(21, "DOWN1", 2, 0, -2),
    DOWN2(22, "DOWN2", 2, 1, 0),
    DOWN3(23, "DOWN3", 2, -1, 1);

    public static final Turner[] rotations = new Turner[24];

    static {
        for (Turner rotation : Turner.values()) {
            rotations[rotation.value] = rotation;
        }
    }

    private final int value;
    private final String name;
    private final int[] rotation;

    public final int[] rotation() {
        return rotation;
    }

    public int rotation(int index) {
        return rotation[index];
    }

    public final int value() {
        return value;
    }

    Turner(final int value, final String name, final int x, final int y, final int z) {
        this.value = value;
        this.name = name;
        this.rotation = new int[]{x, y, z};
    }

    @Override
    public Turner counterX() {
        switch (this) {
            default:
            case SOUTH0:
                return SOUTH1;
            case SOUTH1:
                return SOUTH2;
            case SOUTH2:
                return SOUTH3;
            case SOUTH3:
                return SOUTH0;
            case EAST0:
                return EAST1;
            case EAST1:
                return EAST2;
            case EAST2:
                return EAST3;
            case EAST3:
                return EAST0;
            case NORTH0:
                return NORTH1;
            case NORTH1:
                return NORTH2;
            case NORTH2:
                return NORTH3;
            case NORTH3:
                return NORTH0;
            case WEST0:
                return WEST1;
            case WEST1:
                return WEST2;
            case WEST2:
                return WEST3;
            case WEST3:
                return WEST0;
            case UP0:
                return UP1;
            case UP1:
                return UP2;
            case UP2:
                return UP3;
            case UP3:
                return UP0;
            case DOWN0:
                return DOWN1;
            case DOWN1:
                return DOWN2;
            case DOWN2:
                return DOWN3;
            case DOWN3:
                return DOWN0;
        }
    }

    @Override
    public Turner counterY() {
        switch (this) {
            default:
            case SOUTH0:
                return DOWN2;
            case SOUTH1:
                return WEST1;
            case SOUTH2:
                return UP0;
            case SOUTH3:
                return EAST3;
            case EAST0:
                return DOWN3;
            case EAST1:
                return SOUTH1;
            case EAST2:
                return UP3;
            case EAST3:
                return NORTH3;
            case NORTH0:
                return DOWN0;
            case NORTH1:
                return EAST1;
            case NORTH2:
                return UP2;
            case NORTH3:
                return WEST3;
            case WEST0:
                return DOWN1;
            case WEST1:
                return NORTH1;
            case WEST2:
                return UP1;
            case WEST3:
                return SOUTH3;
            case UP0:
                return NORTH0;
            case UP1:
                return EAST0;
            case UP2:
                return SOUTH0;
            case UP3:
                return WEST0;
            case DOWN0:
                return SOUTH2;
            case DOWN1:
                return EAST2;
            case DOWN2:
                return NORTH2;
            case DOWN3:
                return WEST2;
        }
    }

    @Override
    public Turner counterZ() {
        switch (this) {
            default:
            case SOUTH0:
                return EAST0;
            case SOUTH1:
                return DOWN3;
            case SOUTH2:
                return WEST2;
            case SOUTH3:
                return UP1;
            case EAST0:
                return NORTH0;
            case EAST1:
                return DOWN0;
            case EAST2:
                return SOUTH2;
            case EAST3:
                return UP0;
            case NORTH0:
                return WEST0;
            case NORTH1:
                return DOWN1;
            case NORTH2:
                return EAST2;
            case NORTH3:
                return UP3;
            case WEST0:
                return SOUTH0;
            case WEST1:
                return DOWN2;
            case WEST2:
                return NORTH2;
            case WEST3:
                return UP2;
            case UP0:
                return WEST1;
            case UP1:
                return NORTH1;
            case UP2:
                return EAST1;
            case UP3:
                return SOUTH1;
            case DOWN0:
                return WEST3;
            case DOWN1:
                return SOUTH3;
            case DOWN2:
                return EAST3;
            case DOWN3:
                return NORTH3;
        }
    }

    @Override
    public Turner clockX() {
        switch (this) {
            default:
            case SOUTH0:
                return SOUTH3;
            case SOUTH1:
                return SOUTH0;
            case SOUTH2:
                return SOUTH1;
            case SOUTH3:
                return SOUTH2;
            case EAST0:
                return EAST3;
            case EAST1:
                return EAST0;
            case EAST2:
                return EAST1;
            case EAST3:
                return EAST2;
            case NORTH0:
                return NORTH3;
            case NORTH1:
                return NORTH0;
            case NORTH2:
                return NORTH1;
            case NORTH3:
                return NORTH2;
            case WEST0:
                return WEST3;
            case WEST1:
                return WEST0;
            case WEST2:
                return WEST1;
            case WEST3:
                return WEST2;
            case UP0:
                return UP3;
            case UP1:
                return UP0;
            case UP2:
                return UP1;
            case UP3:
                return UP2;
            case DOWN0:
                return DOWN3;
            case DOWN1:
                return DOWN0;
            case DOWN2:
                return DOWN1;
            case DOWN3:
                return DOWN2;
        }
    }

    @Override
    public Turner clockY() {
        switch (this) {
            default:
            case SOUTH0:
                return UP2;
            case SOUTH1:
                return EAST1;
            case SOUTH2:
                return DOWN0;
            case SOUTH3:
                return WEST3;
            case EAST0:
                return UP1;
            case EAST1:
                return NORTH1;
            case EAST2:
                return DOWN1;
            case EAST3:
                return SOUTH3;
            case NORTH0:
                return UP0;
            case NORTH1:
                return WEST1;
            case NORTH2:
                return DOWN2;
            case NORTH3:
                return EAST3;
            case WEST0:
                return UP3;
            case WEST1:
                return SOUTH1;
            case WEST2:
                return DOWN3;
            case WEST3:
                return NORTH3;
            case UP0:
                return SOUTH2;
            case UP1:
                return WEST2;
            case UP2:
                return NORTH2;
            case UP3:
                return EAST2;
            case DOWN0:
                return NORTH0;
            case DOWN1:
                return WEST0;
            case DOWN2:
                return SOUTH0;
            case DOWN3:
                return EAST0;
        }
    }

    @Override
    public Turner clockZ() {
        switch (this) {
            default:
            case SOUTH0:
                return WEST0;
            case SOUTH1:
                return UP3;
            case SOUTH2:
                return EAST2;
            case SOUTH3:
                return DOWN1;
            case EAST0:
                return SOUTH0;
            case EAST1:
                return UP2;
            case EAST2:
                return NORTH2;
            case EAST3:
                return DOWN2;
            case NORTH0:
                return EAST0;
            case NORTH1:
                return UP1;
            case NORTH2:
                return WEST2;
            case NORTH3:
                return DOWN3;
            case WEST0:
                return NORTH0;
            case WEST1:
                return UP0;
            case WEST2:
                return SOUTH2;
            case WEST3:
                return DOWN0;
            case UP0:
                return EAST3;
            case UP1:
                return SOUTH3;
            case UP2:
                return WEST3;
            case UP3:
                return NORTH3;
            case DOWN0:
                return EAST1;
            case DOWN1:
                return NORTH1;
            case DOWN2:
                return WEST1;
            case DOWN3:
                return SOUTH1;
        }
    }

    @Override
    public Turner reset() {
        return reset;
    }

    public static final Turner reset = SOUTH0;

    @Override
    public float angleX() {
        return 90f;
    }

    @Override
    public float angleY() {
        return 90f;
    }

    @Override
    public float angleZ() {
        return 90f;
    }

    public String toString() {
        return name;
    }

    public static Turner rotation(final int[] rotation) {
        for (Turner value : Turner.values()) {
            if (value.rotation[0] == rotation[0] &&
                    value.rotation[1] == rotation[1] &&
                    value.rotation[2] == rotation[2])
                return value;
        }
        throw new IllegalArgumentException("Turner array " + StringKit.join(", ", rotation) + " does not correspond to a rotation.");
    }

    public int affected(int axis) {
        return flipBits(rotation(axis));
    }

    /**
     * Does a reverse lookup on the rotation array for the axis affected by the rotation
     *
     * @param axis 0 or -1 for x, 1 or -2 for y, 2 or -3 for z
     * @return Which axis the specified axis was before the rotation. 0 for x, 1 for y, 2 for z.
     */
    public int reverseLookup(int axis) {
        final int index = flipBits(axis);
        for (int rot = 0; rot < rotation.length; rot++)
            if (index == affected(rot))
                return rot;
        throw new IllegalStateException();
    }

    /**
     * Flips the bits in rot if rot is negative, leaves it alone if positive. Where x, y, and z correspond to elements
     * 0, 1, and 2 in a rotation array, if those axes are reversed we use {@code ~0}, or -1, for x, and likewise -2 and
     * -3 for y and z when reversed.
     */
    public static int flipBits(int rot) {
        return rot ^ rot >> 31; // (rot ^ rot >> 31) is roughly equal to (rot < 0 ? -1 - rot : rot)
    }

    /**
     * @param index 0 for x, 1 for y, 2 for z
     * @return if selected rotation is negative, return -1, otherwise return 1
     */
    public int step(int index) {
        return rotation[index] >> 31 | 1;
    }

    public int stepX() {
        return step(0);
    }

    public int stepY() {
        return step(1);
    }

    public int stepZ() {
        return step(2);
    }

    public static TempTurner tempTurner = new TempTurner();

    public static class TempTurner {
        protected Turner turner = SOUTH0;

        public TempTurner set(Turner turner) {
            this.turner = turner;
            return this;
        }

        public Turner turner() {
            return turner;
        }

        protected int[] input = new int[]{0, 0, 0};

        public int input(int index) {
            return input[flipBits(index)];
        }

        public TempTurner input(int index, int value) {
            input[flipBits(index)] = value;
            return this;
        }

        public TempTurner input(int x, int y, int z) {
            input[0] = x;
            input[1] = y;
            input[2] = z;
            return this;
        }

        public int turn(int axis) {
            final int index = turner.reverseLookup(axis);
            return input(index) * turner.step(index);
        }

        public int x() {
            return turn(0);
        }

        public int y() {
            return turn(1);
        }

        public int z() {
            return turn(2);
        }
    }
}
