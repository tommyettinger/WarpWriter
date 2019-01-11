package warpwriter.view;

/**
 * An interface to abstract away the complexity of different color palettes
 *
 * @author Ben McLean
 */
public interface IPalette { // extends ITwilight
    /**
     * @param voxel A color index
     * @return A brighter version of the voxel color, or white if none is available.
     */
    byte brighten(byte voxel);

    /**
     * @param voxel A color index
     * @return A darker version of the same color, or black is none is available.
     */
    byte darken(byte voxel);

    /**
     * @param voxel      A color index
     * @param brightness An integer representing how many shades brighter (if positive) or darker (if negative) the result should be
     * @return A different shade of the same color
     */
    byte dimmer(byte voxel, int brightness);

    /**
     * @return A list of main colors
     */
    byte[] colors();

    /**
     * @param color An RGBA8888 color
     * @return The nearest available color index in the palette
     */
    byte reduce(int color);
}
