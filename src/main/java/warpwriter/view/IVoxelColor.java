package warpwriter.view;

/**
 * An IVoxelColor converts from the color index of a voxel to the rgba8888 colors which the visible faces of the cube representing it should be.
 *
 * @author Ben McLean
 */
public interface IVoxelColor {
    int topFace(byte voxel);

    int bottomFace(byte voxel);

    int leftFace(byte voxel);

    int rightFace(byte voxel);
}
