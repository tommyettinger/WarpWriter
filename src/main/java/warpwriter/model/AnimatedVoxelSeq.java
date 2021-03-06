package warpwriter.model;

import warpwriter.model.nonvoxel.IntComparator;

import java.io.Serializable;

/**
 * A wrapper around multiple IVoxelSeq objects that should be used as frames in an animation.
 * @author Tommy Ettinger
 */
public class AnimatedVoxelSeq implements IVoxelSeq, ITemporal, Serializable {
    private static final long serialVersionUID = 0L;
    public VoxelSeq[] seqs;

    public AnimatedVoxelSeq() {
        seqs = new VoxelSeq[]{new VoxelSeq(32)};
    }
    public AnimatedVoxelSeq(VoxelSeq[] voxelSeqs) {
        seqs = voxelSeqs;
    }
    public AnimatedVoxelSeq(VoxelSeq single, int duration)
    {
        seqs = new VoxelSeq[duration];
        for (int i = 0; i < duration; i++) {
            seqs[i] = single;
        }
    }

    public AnimatedVoxelSeq(byte[][][][] voxels) {
        this.seqs = new VoxelSeq[voxels.length];
        final int rough = voxels[0].length * voxels[0][0].length * voxels[0][0][0].length * 3 >>> 4;
        for (int i = 0; i < voxels.length; i++) {
            seqs[i] = new VoxelSeq(rough);
            seqs[i].putSurface(voxels[i]);
        }
    }

    @Override
    public int duration() {
        return seqs.length;
    }

    /**
     * This is a no-op; the duration cannot be changed after creation.
     * @param duration ignored
     * @return this for chaining
     */
    @Override
    public AnimatedVoxelSeq setDuration(int duration) {
        return this;
    }

    protected int frame = 0;
    
    @Override
    public int frame() {
        return frame;
    }

    @Override
    public AnimatedVoxelSeq setFrame(int frame) {
        final int d = duration();
        this.frame = ((frame % d) + d) % d;
        return this;
    }

    /**
     * Gets the x size of the IModel, with requests for x limited between 0 (inclusive) to sizeX() (exclusive).
     *
     * @return the size of the x dimension of the IModel
     */
    @Override
    public int sizeX() {
        return seqs[frame].sizeX();
    }

    /**
     * Gets the y size of the IModel, with requests for y limited between 0 (inclusive) to sizeY() (exclusive).
     *
     * @return the size of the y dimension of the IModel
     */
    @Override
    public int sizeY() {
        return seqs[frame].sizeY();
    }

    /**
     * Gets the z size of the IModel, with requests for z limited between 0 (inclusive) to sizeZ() (exclusive).
     *
     * @return the size of the z dimension of the IModel
     */
    @Override
    public int sizeZ() {
        return seqs[frame].sizeZ();
    }

    @Override
    public int size() {
        return seqs[frame].size();
    }

    @Override
    public int fullSize() {
        return seqs[frame].fullSize();
    }

    @Override
    public void clear() {
        seqs[frame].clear();
    }
    public void clearAll()
    {
        for (int i = 0; i < seqs.length; i++) {
            seqs[i].clear();
        }
    }

    @Override
    public void hollow() {
        seqs[frame].hollow();
    }

    @Override
    public void hollowRemoving() {
        seqs[frame].hollowRemoving();
    }

    @Override
    public void sizeX(int i) {
        seqs[frame].sizeX(i);
    }

    @Override
    public void sizeY(int i) {
        seqs[frame].sizeY(i);

    }

    @Override
    public void sizeZ(int i) {
        seqs[frame].sizeZ(i);
    }

    @Override
    public int rotation() {
        return seqs[frame].rotation();
    }

    @Override
    public void rotate(int i) {
        seqs[frame].rotate(i);
    }

    @Override
    public byte get(int x, int y, int z) {
        return seqs[frame].get(x, y, z);
    }

    @Override
    public byte put(int x, int y, int z, byte v) {
        return seqs[frame].put(x, y, z, v);
    }

    @Override
    public int keyAt(int index) {
        return seqs[frame].keyAt(index);
    }

    @Override
    public byte getAt(int index) {
        return seqs[frame].getAt(index);
    }

    @Override
    public byte getAtHollow(int index) {
        return seqs[frame].getAtHollow(index);
    }

    @Override
    public int rotate(int k, int rotation) {
        return seqs[frame].rotate(k, rotation);
    }

    @Override
    public byte getRotated(int x, int y, int z) {
        return seqs[frame].getRotated(x, y, z);
    }

    @Override
    public byte getRotated(int x, int y, int z, int rotation) {
        return seqs[frame].getRotated(x, y, z, rotation);
    }

    @Override
    public int keyAtRotatedHollow(int index) {
        return seqs[frame].keyAtRotatedHollow(index);
    }

    @Override
    public int keyAtRotatedHollow(int index, int rotation) {
        return seqs[frame].keyAtRotatedHollow(index, rotation);
    }

    @Override
    public int keyAtRotatedFull(int index) {
        return seqs[frame].keyAtRotatedFull(index);
    }

    @Override
    public int keyAtRotatedFull(int index, int rotation) {
        return seqs[frame].keyAtRotatedFull(index, rotation);
    }

    @Override
    public byte getRotated(int index) {
        return seqs[frame].getRotated(index);
    }

    @Override
    public byte getRotated(int index, int rotation) {
        return seqs[frame].getRotated(index, rotation);
    }

    @Override
    public void sort(IntComparator comparator) {
        seqs[frame].sort(comparator);
    }

    @Override
    public void putSurface(byte[][][] voxels) {
        seqs[frame].putSurface(voxels);
    }

    @Override
    public AnimatedVoxelSeq counterX() {
        seqs[frame].counterX();
        return this;
    }

    @Override
    public AnimatedVoxelSeq counterY() {
        seqs[frame].counterY();
        return this;
    }

    @Override
    public AnimatedVoxelSeq counterZ() {
        seqs[frame].counterZ();
        return this;
    }
    @Override
    public AnimatedVoxelSeq clockX() {
        seqs[frame].clockX();
        return this;
    }
    
    @Override
    public AnimatedVoxelSeq clockY() {
        seqs[frame].clockY();
        return this;
    }

    @Override
    public AnimatedVoxelSeq clockZ() {
        seqs[frame].clockZ();
        return this;
    }

    @Override
    public AnimatedVoxelSeq reset() {
        rotate(0);
        return this;
    }

    @Override
    public void doubleSize() {
        for (int i = 0; i < seqs.length; i++) {
            seqs[i].doubleSize();
        }
    }

    @Override
    public void doubleSizeSmooth() {
        for (int i = 0; i < seqs.length; i++) {
            seqs[i].doubleSizeSmooth();
        }
    }
}
