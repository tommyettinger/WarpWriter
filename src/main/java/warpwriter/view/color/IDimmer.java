package warpwriter.view.color;

/**
 * An IDimmer converts from the color index of a voxel to an actual color based on lighting, like a dimmer on a light
 * switch. This converts from byte indices to RGBA8888 ints for actual colors.
 *
 * @author Ben McLean
 */
public interface IDimmer {
    int dark(byte voxel);

    int dim(byte voxel);

    int medium(byte voxel);

    int bright(byte voxel);

    /**
     * @param brightness 0 for dark, 1 for dim, 2 for medium and 3 for bright. Negative numbers are expected to normally be interpreted as black and numbers higher than 3 as white.
     * @param voxel      The color index of a voxel
     * @return An rgba8888 color
     */
    int dimmer(int brightness, byte voxel);
}
