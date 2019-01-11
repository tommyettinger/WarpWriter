package warpwriter.view;

import squidpony.squidmath.IRNG;

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
     * @param rng A randomness source
     * @return A main color chosen at random
     */
    byte random(IRNG rng);

    /**
     * @param rng    A randomness source
     * @param length Desired number of random main colors
     * @return Color indexes that do not repeat, as large as possible without exceeding length
     */
    byte[] random(IRNG rng, int length);

    /**
     * @param rng     A randomness source
     * @param length  Desired number of random main colors
     * @param exclude Bytes to exclude from the results
     * @return Color indexes that do not repeat, as large as possible without exceeding length
     */
    byte[] random(IRNG rng, int length, byte[] exclude);
}
