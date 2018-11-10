package warpwriter.view;

import warpwriter.Coloring;

public class SimpleColor implements IColor {
    public int simple(byte voxel) {
        return Coloring.RINSED[voxel & 255];
    }

    @Override
    public int topFace(byte voxel) {
        return simple(voxel);
    }

    @Override
    public int bottomFace(byte voxel) {
        return simple(voxel);
    }

    @Override
    public int leftFace(byte voxel) {
        return simple(voxel);
    }

    @Override
    public int rightFace(byte voxel) {
        return simple(voxel);
    }
}
