package warpwriter.model;

import warpwriter.model.nonvoxel.ITurnable;
import warpwriter.model.nonvoxel.IntComparator;

/**
 * Created by Tommy Ettinger on 2/24/2019.
 */
public interface IVoxelSeq extends ITurnable {
    int size();
    void clear();
    void hollow();
    int sizeX();
    void sizeX(int i);
    int sizeY();
    void sizeY(int i);
    int sizeZ();
    void sizeZ(int i);
    int rotation();
    void rotate(int i);
    byte get(int x, int y, int z);
    int keyAt(int index);
    byte getAt(int index);
    byte getAtHollow(int index);
    byte getRotated(int x, int y, int z);
    byte getRotated(int x, int y, int z, int rotation);
    int keyAtRotated(int index);
    int keyAtRotated(int index, int rotation);
    byte getRotated(int index);
    byte getRotated(int index, int rotation);
    void sort(IntComparator comparator);
    void putSurface(byte[][][] voxels);
}
