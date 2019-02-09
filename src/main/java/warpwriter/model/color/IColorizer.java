package warpwriter.model.color;

/**
 * An interface to abstract away the complexity of different color palettes
 *
 * @author Ben McLean
 */
public interface IColorizer {
    /**
     * @param voxel A color index
     * @return A brighter version of the voxel color, or the lightest color index in the palette if none is available.
     */
    byte brighten(byte voxel);

    /**
     * @param voxel A color index
     * @return A darker version of the same color, or the darkest color index in the palette if none is available.
     */
    byte darken(byte voxel);

    /**
     * @param voxel      A color index
     * @param brightness An integer representing how many shades brighter (if positive) or darker (if negative) the result should be
     * @return A different shade of the same color
     */
    byte colorize(byte voxel, int brightness);

    /**
     * @return An array of main colors as byte indices, chosen for aesthetic reasons as the primary colors to use.
     */
    byte[] mainColors();

    /**
     * @return An array of grayscale or close-to-grayscale color indices, with the darkest first and lightest last.
     */
    byte[] grayscale();

    /**
     * @param color An RGBA8888 color
     * @return The nearest available color index in the palette.
     */
    byte reduce(int color);
}
