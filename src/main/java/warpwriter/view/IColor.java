package warpwriter.view;

public interface IColor {
    int topFace(byte voxel);
    int bottomFace(byte voxel);
    int leftFace(byte voxel);
    int rightFace(byte voxel);
}
