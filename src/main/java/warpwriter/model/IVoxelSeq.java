package warpwriter.model;

import warpwriter.model.nonvoxel.ITurnable;
import warpwriter.model.nonvoxel.IntComparator;

/**
 * Created by Tommy Ettinger on 2/24/2019.
 */
public interface IVoxelSeq extends ITurnable {
    int size();
    int fullSize();
    void clear();
    void hollow();
    void hollowRemoving();
    int sizeX();
    void sizeX(int i);
    int sizeY();
    void sizeY(int i);
    int sizeZ();
    void sizeZ(int i);
    int rotation();
    int rotate(int k, int rotation);
    void rotate(int i);
    byte get(int x, int y, int z);
    byte put(int x, int y, int z, byte v);
    int keyAt(int index);
    byte getAt(int index);
    byte getAtHollow(int index);
    byte getRotated(int x, int y, int z);
    byte getRotated(int x, int y, int z, int rotation);
    int keyAtRotatedHollow(int index);
    int keyAtRotatedHollow(int index, int rotation);
    int keyAtRotatedFull(int index);
    int keyAtRotatedFull(int index, int rotation);
    byte getRotated(int index);
    byte getRotated(int index, int rotation);
    void sort(IntComparator comparator);
    void putSurface(byte[][][] voxels);
}
