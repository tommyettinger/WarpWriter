package warpwriter.model;

/**
 * A rewrite of Turner based on the idea of using a rotation array instead of two enums.
 *
 * @author Ben McLean
 */
public class Turner2 {
    public Turner2() {
        reset();
    }

    public Turner2(Turner2 other) {
        System.arraycopy(other.rotation(), 0, rotation, 0, rotation().length);
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

    public Turner2 set(int[] rotation) {
        this.rotation = rotation;
        return this;
    }

    public Turner2 counterX() {
        final int y = ~rotation[2], z = rotation[1];
        rotation[1] = y;
        rotation[2] = z;
        return this;
    }

    public Turner2 counterY() {
        final int x = rotation[2], z = ~rotation[0];
        rotation[0] = x;
        rotation[2] = z;
        return this;
    }

    public Turner2 counterZ() {
        final int x = ~rotation[1], y = rotation[0];
        rotation[0] = x;
        rotation[1] = y;
        return this;
    }

    public Turner2 clockX() {
        final int y = rotation[2], z = ~rotation[1];
        rotation[1] = y;
        rotation[2] = z;
        return this;
    }

    public Turner2 clockY() {
        final int x = ~rotation[2], z = rotation[0];
        rotation[0] = x;
        rotation[2] = z;
        return this;
    }

    public Turner2 clockZ() {
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
    public Turner2 reset() {
        rotation[0] = -1;
        rotation[1] = 1;
        rotation[2] = 2;
        return this;
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

    public Turner2 setCenterX(int x) {
        center[0] = x;
        return this;
    }

    public Turner2 setCenterY(int y) {
        center[1] = y;
        return this;
    }

    public Turner2 setCenterZ(int z) {
        center[2] = z;
        return this;
    }

    public Turner2 setCenter(int x, int y, int z) {
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

    public int[] input = new int[]{0, 0, 0};

    public int input(int index) {
        return input[index];
    }

    public Turner2 input(int x, int y, int z) {
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

    public int turn(int index) {
        return input(affected(index)) * step(index);
    }

    /**
     * bitwise stuff here flips the bits in rot if negative, leaves it alone if positive
     */
    public static int flipBits(int rot) {
        return rot ^ rot >> 31;
    }
}
