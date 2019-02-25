package warpwriter.model;

import warpwriter.model.nonvoxel.IntComparator;

/**
 * Created by Tommy Ettinger on 2/24/2019.
 */
public interface IVoxelSeq {
    int size();
    void clear();
    int getSizeX();
    void setSizeX(int i);
    int getSizeY();
    void setSizeY(int i);
    int getSizeZ();
    void setSizeZ(int i);
    int getRotation();
    void setRotation(int i);
    byte get(int x, int y, int z);
    int keyAt(int index);
    byte getAt(int index);
    byte getRotated(int x, int y, int z);
    byte getRotated(int x, int y, int z, int rotation);
    int keyAtRotated(int index);
    int keyAtRotated(int index, int rotation);
    byte getRotated(int index);
    byte getRotated(int index, int rotation);
    void sort(IntComparator comparator);
    void putSurface(byte[][][] voxels);
}
