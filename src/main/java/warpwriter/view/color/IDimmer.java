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
    /**
     * Allows implementors to mark whether an IDimmer allows shading and/or outlining to be disabled for each color
     * index, determined by the status of a specific shade bit. If this returns 0, the palette does not support custom
     * shading rules. If this returns a power of two between 1 and 128, when {@code (voxel & getShadeBit()) != 0}, an 
     * alternate set of shading rules will be used, which usually disables shading and outlining for color indices with
     * that bit set.
     * @return 0 if this does not have configurable shading, or a power of two between 1 and 128 when that bit marks special voxels with different shading rules
     */
    int getShadeBit();
    /**
     * Allows implementors to mark whether an IDimmer allows the shading of a voxel to vary depending on that voxel's
     * position in 3D space plus time, determined by the status of a specific wave bit. If this returns 0, the palette
     * does not support custom shading rules. If this returns a power of two between 1 and 128, when
     * {@code (voxel & getWaveBit()) != 0}, an alternate set of shading rules will be used. This has different behavior
     * if the bit that can be specified by {@link #getShadeBit()} is set at the same time the wave bit is specified.
     * If the wave bit is set and the shade bit (if any) is not set, then this is expected to use some form of 4D
     * continuous noise or seamless 3D noise to change the shading of a voxel, but the outline should be drawn with the
     * same color (when using {@link #dimmer(int, byte)}, brightness 0 stays the same, while the other brightnesses
     * should change using the noise). If both the wave bit and the shade bit are set, then the "wave" this refers to is
     * a pulsing light wave with a wavelength, and as with the shade bit on its own there should be no outline. The
     * pulsing effect for non-outline colors is suggested to use
     * {@code int brightness = (x + y + z + time & 3); brightness += 1 - (brightness & (brightness << 1));}, which will
     * cause the brightness to zigzag between 1 and 3, spending more time at brightness 2.
     * @return 0 if this does not have wave shading, or a power of two between 1 and 128 when that bit marks special voxels that change shading over time and across space
     */
    int getWaveBit();

}
