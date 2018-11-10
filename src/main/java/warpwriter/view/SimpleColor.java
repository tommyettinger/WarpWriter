package warpwriter.view;

import warpwriter.Coloring;

public class SimpleColor implements IColor {

    @Override
    public int topFace(byte voxel) {
        return Coloring.RINSED[voxel & 255];
    }

    @Override
    public int leftFace(byte voxel) {
        return Coloring.RINSED[voxel & 255];
    }

    @Override
    public int rightFace(byte voxel) {
        return Coloring.RINSED[voxel & 255];
    }
}
