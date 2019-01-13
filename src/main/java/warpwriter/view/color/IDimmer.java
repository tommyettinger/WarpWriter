package warpwriter.view.color;

/**
 * An IDimmer converts from the color index of a voxel to an actual color based on lighting.
 *
 * @author Ben McLean
 */
public interface IDimmer {
    int dark(byte voxel);

    int dim(byte voxel);

    int twilight(byte voxel);

    int bright(byte voxel);

    /**
     * @param brightness 0 for dark, 1 for dim, 2 for twilight and 3 for bright. Negative numbers are expected to normally be interpreted as black and numbers higher than 3 as white.
     * @param voxel      The color index of a voxel
     * @return An rgba8888 color
     */
    int dimmer(int brightness, byte voxel);
}
