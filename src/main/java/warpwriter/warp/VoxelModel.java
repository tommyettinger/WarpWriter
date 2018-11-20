package warpwriter.warp;

import warpwriter.model.IModel;

/**
 * Trying again; a way of storing voxel models using a 1D array and a more involved method of lookup that should deal
 * correctly with rotation. The idea here is to provide tools for iterating over voxels along certain axes, which is
 * mostly what rendering code needs.
 * <br>
 * Created by Tommy Ettinger on 11/19/2018.
 */
public class VoxelModel implements IModel {
    public byte[] voxels;
    /**
     *  Sizes of the model without rotation, with sizes[0] referring to x, [1] to y, and [2] to z. It is expected that
     *  {@link #rotation} will be used to remap axes when this is accessed, but this (sizes) should not change.
     */
    public int[] sizes;
    /**
     * Stores 3 ints that are used to remap axes; if an int {@code n} is negative, the axis corresponding to {@code ~n}
     * will be reversed.
     */
    public int[] rotation;

    /**
     * Working array for manipulating x, y, and z by their indices.
     */
    final public int[] temp = new int[3];
    
    public VoxelModel()
    {
        voxels = new byte[1];
        sizes = new int[]{1, 1, 1};
        rotation = new int[]{-1, 1, 2};
    }
    
    public VoxelModel(byte[][][] data)
    {
        final int sx = data.length, sy = data[0].length, sz = data[0][0].length;
        sizes = new int[]{sx, sy, sz};
        voxels = new byte[sx * sy * sz];
        rotation = new int[]{-1, 1, 2};
        for (int x = 0; x < sx; x++) {
            for (int y = 0; y < sy; y++) {
                System.arraycopy(data[x][y], 0, voxels, sz * (x * sy + y), sz);
            }
        }
    }

    @Override
    public int sizeX()
    {
        final int rot = rotation[0];
        // bitwise stuff here flips the bits in rot if negative, leaves it alone if positive
        return sizes[rot ^ rot >> 31];
    }

    @Override
    public int sizeY()
    {
        final int rot = rotation[1];
        // bitwise stuff here flips the bits in rot if negative, leaves it alone if positive
        return sizes[rot ^ rot >> 31];
    }

    @Override
    public int sizeZ()
    {
        final int rot = rotation[2];
        // bitwise stuff here flips the bits in rot if negative, leaves it alone if positive
        return sizes[rot ^ rot >> 31];
    }

    @Override
    public boolean inside(int x, int y, int z) {
        return !outside(x, y, z);
    }

    @Override
    public boolean outside(int x, int y, int z) {
        return x < 0 || y < 0 || z < 0 || x >= sizeX() || y >= sizeY() || z >= sizeZ();
    }

    public int startX()
    {
        final int rot = rotation[0];
        if(rot < 0)
            return sizes[~rot] - 1; // when rot is negative, we need to go from the end of the axis, not the start
        else
            return 0;
    }

    public int startY()
    {
        final int rot = rotation[1];
        if(rot < 0)
            return sizes[~rot] - 1; // when rot is negative, we need to go from the end of the axis, not the start
        else
            return 0;
    }

    public int startZ()
    {
        final int rot = rotation[2];
        if(rot < 0)
            return sizes[~rot] - 1; // when rot is negative, we need to go from the end of the axis, not the start
        else
            return 0;
    }

    public int stepX()
    {
        return rotation[0] >> 31 | 1; // if selected rotation is negative, return -1, otherwise return 1
    }
    
    public int stepY()
    {
        return rotation[1] >> 31 | 1; // if selected rotation is negative, return -1, otherwise return 1
    }
    
    public int stepZ()
    {
        return rotation[2] >> 31 | 1; // if selected rotation is negative, return -1, otherwise return 1
    }

    /**
     * Using the adjusted x, y, z (after rotation) produced with {@link #startX()}, {@link #stepX()}, and the other
     * versions for y and z, this gets the voxel color as a byte at that adjusted coordinate.
     * @param x adjusted x, as from {@code startX() + stepX() * x}
     * @param y adjusted y, as from {@code startY() + stepY() * y}
     * @param z adjusted z, as from {@code startZ() + stepZ() * z}
     * @return the voxel color at the given position, as a byte that should probably be masked with {@code & 255}
     */
    @Override
    public byte at(int x, int y, int z)
    {
//        x = startX() + stepX() * x;
//        y = startY() + stepY() * y;
//        z = startZ() + stepZ() * z;
        final int sy = sizeY(), sz = sizeZ();
        return voxels[sz * (sy * x + y) + z];
    }
}
