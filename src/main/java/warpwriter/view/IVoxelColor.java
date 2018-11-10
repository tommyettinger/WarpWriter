package warpwriter.view;

public interface IVoxelColor {
    int topFace(byte voxel);
    int bottomFace(byte voxel);
    int leftFace(byte voxel);
    int rightFace(byte voxel);
}
