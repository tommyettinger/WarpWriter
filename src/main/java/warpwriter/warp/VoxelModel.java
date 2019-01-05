package warpwriter.warp;

import warpwriter.model.IModel;

/**
 * Trying again; a way of storing voxel models using a 1D array and a more involved method of lookup that should deal
 * correctly with rotation. The idea here is to provide tools for iterating over voxels along certain axes, which is
 * mostly what rendering code needs.
 * <p>
 * Created by Tommy Ettinger on 11/19/2018.
 */
public class VoxelModel implements IModel {
    public byte[] voxels;

    public byte[] voxels() {
        return voxels;
    }

    public VoxelModel set(byte[] voxels) {
        this.voxels = voxels;
        return this;
    }

    /**
     * Sizes of the model without rotation, with sizes[0] referring to x, [1] to y, and [2] to z. It is expected that
     * {@link #rotation} will be used to remap axes when this is accessed, but this (sizes) should not change.
     */
    protected int[] sizes;

    public int[] sizes() {
        return sizes;
    }

    public VoxelModel setSizes(int[] sizes) {
        this.sizes = sizes;
        return this;
    }

    /**
     * Stores 3 ints that are used to remap axes; if an int {@code n} is negative, the axis corresponding to {@code ~n}
     * will be reversed.
     */
    protected int[] rotation;

    public int[] rotation() {
        return rotation;
    }

    public VoxelModel setRotation(int[] rotation) {
        this.rotation = rotation;
        return this;
    }

    /**
     * Working array for manipulating x, y, and z by their indices.
     */
    final public int[] temp = new int[3];

    public VoxelModel() {
        set(new byte[1]);
        setSizes(new int[]{1, 1, 1});
        setRotation(rotation = new int[]{-1, 1, 2});
    }

    public VoxelModel(byte[][][] data) {
        final int sx = data.length, sy = data[0].length, sz = data[0][0].length;
        setSizes(new int[]{sx, sy, sz});
        set(new byte[sx * sy * sz]);
        setRotation(new int[]{-1, 1, 2});
        for (int x = 0; x < sx; x++) {
            for (int y = 0; y < sy; y++) {
                System.arraycopy(data[x][y], 0, voxels, sz * (x * sy + y), sz);
            }
        }
    }

    @Override
    public int sizeX() {
        return sizes()[affected(0)];
    }

    @Override
    public int sizeY() {
        return sizes()[affected(1)];
    }

    @Override
    public int sizeZ() {
        return sizes()[affected(2)];
    }

    public int affected(int axis) {
        return flipBits(rotation[axis]);
    }

    /**
     * bitwise stuff here flips the bits in rot if negative, leaves it alone if positive
     */
    public static int flipBits(int rot) {
        return rot ^ rot >> 31;
    }

    @Override
    public boolean inside(int x, int y, int z) {
        return !outside(x, y, z);
    }

    @Override
    public boolean outside(int x, int y, int z) {
        return x < 0 || y < 0 || z < 0 || x >= sizeX() || y >= sizeY() || z >= sizeZ();
    }

    public int startX() {
        final int rot = rotation()[0];
        if (rot < 0)
            return sizes()[~rot] - 1; // when rot is negative, we need to go from the end of the axis, not the start
        else
            return 0;
    }

    public int startY() {
        final int rot = rotation()[1];
        if (rot < 0)
            return sizes()[~rot] - 1; // when rot is negative, we need to go from the end of the axis, not the start
        else
            return 0;
    }

    public int startZ() {
        final int rot = rotation()[2];
        if (rot < 0)
            return sizes()[~rot] - 1; // when rot is negative, we need to go from the end of the axis, not the start
        else
            return 0;
    }

    public int stepX() {
        return rotation()[0] >> 31 | 1; // if selected rotation is negative, return -1, otherwise return 1
    }

    public int stepY() {
        return rotation()[1] >> 31 | 1; // if selected rotation is negative, return -1, otherwise return 1
    }

    public int stepZ() {
        return rotation()[2] >> 31 | 1; // if selected rotation is negative, return -1, otherwise return 1
    }

    /**
     * Using the adjusted x, y, z (after rotation) produced with {@link #startX()}, {@link #stepX()}, and the other
     * versions for y and z, this gets the voxel color as a byte at that adjusted coordinate.
     *
     * @param x adjusted x, as from {@code startX() + stepX() * x}
     * @param y adjusted y, as from {@code startY() + stepY() * y}
     * @param z adjusted z, as from {@code startZ() + stepZ() * z}
     * @return the voxel color at the given position, as a byte that should probably be masked with {@code & 255}
     */
    @Override
    public byte at(int x, int y, int z) {
        temp[flipBits(rotation()[0])] = startX() + stepX() * x;
        temp[flipBits(rotation()[1])] = startY() + stepY() * y;
        temp[flipBits(rotation()[2])] = startZ() + stepZ() * z;
        final int index = index(temp[0], temp[1], temp[2]);
        return index < 0 || index >= voxels().length ? 0 : voxels()[index];
    }

    public int index(int x, int y, int z) {
        return sizes()[2] * (sizes()[1] * x + y) + z;
    }

    public int x(int index) {
        return (index / (sizeZ() * sizeY()));
    }

    public int y(int index) {
        return (index / sizeZ()) % sizeY();
    }

    public int z(int index) {
        return index % sizeZ();
    }

    public VoxelModel counterX() {
        final int y = ~rotation()[2], z = rotation()[1];
        rotation()[1] = y;
        rotation()[2] = z;
        return this;
    }

    public VoxelModel counterY() {
        final int x = rotation()[2], z = ~rotation()[0];
        rotation()[0] = x;
        rotation()[2] = z;
        return this;
    }

    public VoxelModel counterZ() {
        final int x = ~rotation()[1], y = rotation()[0];
        rotation()[0] = x;
        rotation()[1] = y;
        return this;
    }

    public VoxelModel clockX() {
        final int y = rotation()[2], z = ~rotation()[1];
        rotation()[1] = y;
        rotation()[2] = z;
        return this;
    }

    public VoxelModel clockY() {
        final int x = ~rotation()[2], z = rotation()[0];
        rotation()[0] = x;
        rotation()[2] = z;
        return this;
    }

    public VoxelModel clockZ() {
        final int x = rotation()[1], y = ~rotation()[0];
        rotation()[0] = x;
        rotation()[1] = y;
        return this;
    }

    /**
     * Resets the rotation array.
     *
     * @return this
     */
    public VoxelModel reset() {
        rotation()[0] = -1;
        rotation()[1] = 1;
        rotation()[2] = 2;
        return this;
    }
}