package warpwriter.model.nonvoxel;

/**
 * Rotates integer coordinates based on right-hand rotation.
 *
 * @author Ben McLean
 */
public class Turner implements ITurner {
    public Turner() {
    }

    public Turner(Turner other) {
        System.arraycopy(other.rotation, 0, rotation, 0, rotation.length);
    }

    /**
     * Stores 3 ints that are used to remap axes; if an int {@code n} is negative, the axis corresponding to {@code ~n}
     * will be reversed.
     */
    protected int[] rotation = new int[]{-1, 1, 2};

    public int[] rotation() {
        return rotation;
    }

    public int rotation(int index) {
        return rotation[index];
    }

    public Turner set(int[] rotation) {
        this.rotation = rotation;
        return this;
    }

    @Override
    public Turner counterX() {
        final int y = ~rotation[2], z = rotation[1];
        rotation[1] = y;
        rotation[2] = z;
        return this;
    }

    @Override
    public Turner counterY() {
        final int x = rotation[2], z = ~rotation[0];
        rotation[0] = x;
        rotation[2] = z;
        return this;
    }

    @Override
    public Turner counterZ() {
        final int x = ~rotation[1], y = rotation[0];
        rotation[0] = x;
        rotation[1] = y;
        return this;
    }

    @Override
    public Turner clockX() {
        final int y = rotation[2], z = ~rotation[1];
        rotation[1] = y;
        rotation[2] = z;
        return this;
    }

    @Override
    public Turner clockY() {
        final int x = ~rotation[2], z = rotation[0];
        rotation[0] = x;
        rotation[2] = z;
        return this;
    }

    @Override
    public Turner clockZ() {
        final int x = rotation[1], y = ~rotation[0];
        rotation[0] = x;
        rotation[1] = y;
        return this;
    }

    /**
     * Resets the rotation array.
     *
     * @return this
     */
    @Override
    public Turner reset() {
        rotation[0] = -1;
        rotation[1] = 1;
        rotation[2] = 2;
        return this;
    }

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

    /*
    protected int[] center = new int[] {0, 0, 0};

    public int center(int index) {
        return center[index];
    }

    public int centerX() {
        return center(0);
    }

    public int centerY() {
        return center(1);
    }

    public int centerZ() {
        return center(2);
    }

    public Turner setCenterX(int x) {
        center[0] = x;
        return this;
    }

    public Turner setCenterY(int y) {
        center[1] = y;
        return this;
    }

    public Turner setCenterZ(int z) {
        center[2] = z;
        return this;
    }

    public Turner setCenter(int x, int y, int z) {
        return setCenterX(x).setCenterY(y).setCenterZ(z);
    }
    */

    /*
    protected int[] output = new int[] {0, 0, 0};

    public int output(int index) {
        return output[index];
    }

    public int x() {
        return output(0);
    }

    public int y() {
        return output(1);
    }

    public int z() {
        return output(2);
    }
    */

    protected int[] input = new int[]{0, 0, 0};

    public int input(int index) {
        return input[flipBits(index)];
    }

    public Turner input(int index, int value) {
        input[flipBits(index)] = value;
        return this;
    }

    public Turner input(int x, int y, int z) {
        input[0] = x;
        input[1] = y;
        input[2] = z;
        return this;
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

    public int affected(int axis) {
        return flipBits(rotation(axis));
    }

    public int turn(int axis) {
        final int index = reverseLookup(axis);
        return input(index) * step(index);
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
}
