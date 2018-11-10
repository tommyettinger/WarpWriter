package warpwriter.view;

import warpwriter.Coloring;

public class SimpleColor implements IColor {

    @Override
    public int vertColor(byte voxel) {
        return Coloring.RINSED[voxel & 255];
    }

    @Override
    public int leftColor(byte voxel) {
        return Coloring.RINSED[voxel & 255];
    }

    @Override
    public int rightColor(byte voxel) {
        return Coloring.RINSED[voxel & 255];
    }
}
