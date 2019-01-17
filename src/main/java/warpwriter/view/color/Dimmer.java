package warpwriter.view.color;

import com.badlogic.gdx.math.MathUtils;
import warpwriter.Coloring;

/**
 * Dimmer provides default implementations of bright, dimmer, dim and dark which refer to the light method, and a default implementation of the light method which refers to the other four. Extension classes can overload just the light method or alternatively can overload the other four. Either way will work.
 * <p>
 * Also contained here are various extensions as member classes.
 *
 * @author Ben McLean
 */
public abstract class Dimmer implements IDimmer {
    /**
     * Refers to the light method to get the answer.
     */
    @Override
    public int bright(byte voxel) {
        return dimmer(3, voxel);
    }

    /**
     * Refers to the light method to get the answer.
     */
    @Override
    public int medium(byte voxel) {
        return dimmer(2, voxel);
    }

    /**
     * Refers to the light method to get the answer.
     */
    @Override
    public int dim(byte voxel) {
        return dimmer(1, voxel);
    }

    /**
     * Refers to the light method to get the answer.
     */
    @Override
    public int dark(byte voxel) {
        return dimmer(0, voxel);
    }

    /**
     * Refers to the dark, dim, dimmer and bright methods to get the answer.
     *
     * @param brightness 0 for dark, 1 for dim, 2 for medium and 3 for bright. Negative numbers are expected to normally be interpreted as black and numbers higher than 3 as white.
     * @param voxel      The color index of a voxel
     * @return An rgba8888 color
     */
    @Override
    public int dimmer(int brightness, byte voxel) {
        if (voxel == 0) return 0; // 0 is equivalent to Color.rgba8888(Color.CLEAR)
        switch (brightness) {
            case 0:
                return dark(voxel);
            case 1:
                return dim(voxel);
            case 2:
                return medium(voxel);
            case 3:
                return bright(voxel);
        }
        return brightness > 3
                ? 0xffffffff //rgba8888 value of Color.WHITE
                : 0xff;      //rgba8888 value of Color.BLACK
    }

    /**
     * Renders arbitrarily brighter or darker using the colors available in another Dimmer.
     *
     * @author Ben McLean
     */
    public static class OffsetDimmer extends Dimmer {
        public OffsetDimmer(IDimmer dimmer) {
            super();
            set(dimmer);
        }

        protected int offset = 0;

        public int offset() {
            return offset;
        }

        public OffsetDimmer set(int offset) {
            this.offset = offset;
            return this;
        }

        public OffsetDimmer add(int offset) {
            this.offset += offset;
            return this;
        }

        protected IDimmer dimmer;

        public IDimmer dimmer() {
            return dimmer;
        }

        public OffsetDimmer set(IDimmer dimmer) {
            this.dimmer = dimmer;
            return this;
        }

        @Override
        public int dimmer(int brightness, byte voxel) {
            return dimmer.dimmer(brightness + offset, voxel);
        }
    }

    public static final IDimmer RinsedDimmer = new Dimmer() {
        protected int[] palette = Coloring.RINSED;

        @Override
        public int bright(byte voxel) {
            return palette[(voxel & 248) + Math.max((voxel & 7) - 1, 0)];
        }

        @Override
        public int medium(byte voxel) {
            return palette[(voxel & 255)];
        }

        @Override
        public int dim(byte voxel) {
            return palette[(voxel & 248) + Math.min((voxel & 7) + 1, 7)];
        }

        @Override
        public int dark(byte voxel) {
            return palette[(voxel & 248) + Math.min((voxel & 7) + 2, 7)];
        }
    };

    public static final IDimmer AuroraDimmer = new Dimmer() {
        @Override
        public int dimmer(int brightness, byte voxel) {
            return AURORA_RAMP_VALUES[voxel & 255][
                    brightness <= 0
                            ? 3
                            : brightness >= 3
                            ? 0
                            : 3 - brightness
                    ];
        }
    };

    public static final IDimmer AuroraWarmthDimmer = new Dimmer() {
        @Override
        public int dark(byte voxel) {
            return AURORA_WARMTH_RAMP_VALUES[voxel & 255][3];
        }

        @Override
        public int dim(byte voxel) {
            return AURORA_WARMTH_RAMP_VALUES[voxel & 255][2];
        }

        @Override
        public int medium(byte voxel) {
            return AURORA_WARMTH_RAMP_VALUES[voxel & 255][1];
        }

        @Override
        public int bright(byte voxel) {
            return AURORA_WARMTH_RAMP_VALUES[voxel & 255][0];
        }
    };

    public static IDimmer arbitraryDimmer(final int[] rgbaPalette) {
        return new Dimmer() {
            private int[][] RAMP_VALUES = new int[256][4];

            {
                for (int i = 1; i < 256 && i < rgbaPalette.length; i++) {
                    int color = RAMP_VALUES[i][2] = rgbaPalette[i],
                            r = (color >>> 24),
                            g = (color >>> 16 & 0xFF),
                            b = (color >>> 8 & 0xFF);
                    int co = r - b, t = b + (co >> 1), cg = g - t, y = t + (cg >> 1),
                            yBright = y * 21 >> 4, yDim = y * 5 >> 3, yDark = y >> 1, chromO, chromG;
                    chromO = (co * 3) >> 2;
                    chromG = (cg * 3) >> 2;
                    t = yDim - (chromG >> 1);
                    g = chromG + t;
                    b = t - (chromO >> 1);
                    r = b + chromO;
                    RAMP_VALUES[i][1] =
                            MathUtils.clamp(r, 0, 255) << 24 |
                                    MathUtils.clamp(g, 0, 255) << 16 |
                                    MathUtils.clamp(b, 0, 255) << 8 | 0xFF;
                    chromO = (co * 3) >> 2;
                    chromG = (cg * (256 - yBright) * 3) >> 9;
                    t = yBright - (chromG >> 1);
                    g = chromG + t;
                    b = t - (chromO >> 1);
                    r = b + chromO;
                    RAMP_VALUES[i][3] =
                            MathUtils.clamp(r, 0, 255) << 24 |
                                    MathUtils.clamp(g, 0, 255) << 16 |
                                    MathUtils.clamp(b, 0, 255) << 8 | 0xFF;
                    chromO = (co * 13) >> 4;
                    chromG = (cg * (256 - yDark) * 13) >> 11;
                    t = yDark - (chromG >> 1);
                    g = chromG + t;
                    b = t - (chromO >> 1);
                    r = b + chromO;
                    RAMP_VALUES[i][0] =
                            MathUtils.clamp(r, 0, 255) << 24 |
                                    MathUtils.clamp(g, 0, 255) << 16 |
                                    MathUtils.clamp(b, 0, 255) << 8 | 0xFF;
                }
            }

            @Override
            public int dimmer(int brightness, byte voxel) {
                return RAMP_VALUES[voxel & 255][
                        brightness <= 0
                                ? 0
                                : brightness >= 3
                                ? 3
                                : brightness
                        ];
            }
        };
    }

    public static final IDimmer AuroraToFlesurrectDimmer = new Dimmer() {
        @Override
        public int dimmer(int brightness, byte voxel) {
            if(voxel == 0) return 0;
            final int color = AURORA_RAMP_VALUES[voxel & 255][1];
            return FLESURRECT_RAMP_VALUES[Coloring.FLESURRECT_REDUCER.paletteMapping[
                    (color >>> 17 & 0x7C00)
                            | (color >>> 14 & 0x3E0)
                            | (color >>> 11 & 0x1F)] & 0xFF][
                    brightness <= 0
                            ? 3
                            : brightness >= 3
                            ? 0
                            : 3 - brightness
                    ];
        }
    };

    /**
     * Bytes that correspond to palette indices to use when shading an Aurora-palette model.
     * The color in index 1 of each 4-element sub-array is the "dimmer" color and the one that will match the voxel
     * color used in a model. The color at index 0 is "bright", index 2 is "dim", and index 3 is "dark". Normal usage
     * with a renderer will use {@link #AURORA_RAMP_VALUES}; this array would be used to figure out what indices are related to
     * another color for the purpose of procedurally using similar colors with different lightness.
     * To visualize this, <a href="https://i.imgur.com/rQDrPE7.png">use this image</a>, with the first 32 items in
     * AURORA_RAMPS corresponding to the first column from top to bottom, the next 32 items in the second column, etc.
     */
    public static final byte[][] AURORA_RAMPS = new byte[][]{
            { 0, 0, 0, 0 },
            { 3, 1, 1, 1 },
            { -124, 2, 1, 1 },
            { 119, 3, 1, 1 },
            { 6, 4, 117, 2 },
            { 7, 5, -124, 3 },
            { 51, 6, 119, 4 },
            { 50, 7, 5, 119 },
            { 10, 8, 6, 5 },
            { 47, 9, 7, 6 },
            { 12, 10, 8, 51 },
            { -116, 11, 9, 50 },
            { 127, 12, 10, 9 },
            { 15, 13, 11, 47 },
            { 15, 14, 12, 11 },
            { 15, 15, 13, -116 },
            { -65, 16, -53, -32 },
            { -44, 17, -65, -66 },
            { -59, 18, -42, -56 },
            { 15, 19, 111, 107 },
            { -25, 20, -38, -37 },
            { -36, 21, 23, 1 },
            { -38, 22, -31, -33 },
            { -35, 23, 1, 1 },
            { -53, 24, 1, 1 },
            { -16, 25, -12, 1 },
            { -20, 26, -17, -16 },
            { -20, 27, -16, 25 },
            { -22, 28, -20, 26 },
            { 15, 29, -114, -113 },
            { -6, 30, -103, 32 },
            { -102, 31, -105, 33 },
            { -100, 32, -3, -1 },
            { -1, 33, -108, 1 },
            { 61, 34, 1, 1 },
            { 32, 35, -106, -107 },
            { 56, 36, 32, -94 },
            { -98, 37, -95, -104 },
            { 39, 38, 56, 55 },
            { 15, 39, 79, 68 },
            { 39, 40, -88, -89 },
            { -87, 41, -89, 42 },
            { -88, 42, -92, -93 },
            { -70, 43, 87, 1 },
            { -71, 44, -68, -75 },
            { -71, 45, -70, -75 },
            { 97, 46, 95, 100 },
            { -116, 47, 9, -119 },
            { 68, 48, 54, 64 },
            { 12, 49, 8, 7 },
            { 11, 50, 7, -126 },
            { 10, 51, 6, 120 },
            { 9, 52, 5, 61 },
            { 65, 53, -2, 62 },
            { 56, 54, 63, -2 },
            { 57, 55, 53, 63 },
            { 59, 56, 54, 53 },
            { 29, 57, 55, 54 },
            { 67, 58, 55, 54 },
            { 39, 59, 48, 65 },
            { 15, 60, 12, -115 },
            { 63, 61, -109, -108 },
            { 52, 62, -11, -107 },
            { 64, 63, 61, -11 },
            { 65, 64, 63, 62 },
            { 66, 65, 64, 53 },
            { 67, 66, 65, -112 },
            { 15, 67, -114, -113 },
            { 14, 68, 48, 65 },
            { 66, 69, 64, 53 },
            { 9, 70, 71, 62 },
            { 70, 71, 73, 72 },
            { 5, 72, 1, 1 },
            { 71, 73, 87, 2 },
            { 75, 74, 86, 73 },
            { 49, 75, 74, 71 },
            { 48, 76, 70, 74 },
            { 68, 77, 75, -90 },
            { 80, 78, 69, 76 },
            { 14, 79, 48, 49 },
            { 15, 80, 12, 11 },
            { 14, 81, 82, 95 },
            { 81, 82, 9, 75 },
            { 82, 83, 84, 85 },
            { 100, 84, 90, 86 },
            { 93, 85, 89, 86 },
            { 101, 86, 103, 87 },
            { 4, 87, 1, 1 },
            { 91, 88, 117, 2 },
            { 101, 89, 103, 3 },
            { 51, 90, 4, 103 },
            { 51, 91, 88, -67 },
            { 93, 92, 89, 88 },
            { 95, 93, 92, 101 },
            { 107, 94, 92, 101 },
            { 99, 95, 93, 51 },
            { 109, 96, 100, 93 },
            { 15, 97, 110, 111 },
            { 97, 98, 107, 95 },
            { 97, 99, 100, 106 },
            { 96, 100, 8, 51 },
            { 50, 101, 89, 88 },
            { 105, 102, -67, -32 },
            { 5, 103, 1, 1 },
            { 105, 104, 117, 1 },
            { 51, 105, 119, 104 },
            { 107, 106, 115, 105 },
            { 108, 107, 106, 114 },
            { 97, 108, 107, 113 },
            { 15, 109, 111, 112 },
            { 97, 110, 125, 113 },
            { 127, 111, 113, 124 },
            { 126, 112, 124, 123 },
            { 111, 113, 50, 122 },
            { -45, 114, -51, -53 },
            { 50, 115, 116, 120 },
            { 115, 116, -54, -32 },
            { 104, 117, 1, 1 },
            { 119, 118, 1, 1 },
            { 121, 119, 118, 117 },
            { 115, 120, -54, 118 },
            { 123, 121, 119, -54 },
            { 124, 122, 120, -125 },
            { -45, 123, 121, 22 },
            { 125, 124, 122, -126 },
            { 110, 125, 124, 123 },
            { 15, 126, 112, 125 },
            { 15, 127, -43, 112 },
            { -43, -128, 122, -126 },
            { -26, -127, -16, -15 },
            { -119, -126, -125, -15 },
            { -126, -125, -13, -12 },
            { 5, -124, 1, 1 },
            { -122, -123, -108, 1 },
            { -120, -122, -14, -124 },
            { -119, -121, -125, -14 },
            { -128, -120, -122, -10 },
            { -114, -119, -121, -126 },
            { -117, -118, -113, -112 },
            { 15, -117, -115, -118 },
            { 15, -116, 47, -113 },
            { -117, -115, -113, -119 },
            { -116, -114, -112, -119 },
            { -118, -113, -120, -121 },
            { -114, -112, -120, -121 },
            { -113, -111, 63, -10 },
            { -122, -110, -108, 1 },
            { -123, -109, 1, 1 },
            { -110, -108, 1, 1 },
            { 61, -107, 1, 1 },
            { 35, -106, -108, 1 },
            { 32, -105, -108, 1 },
            { -101, -104, 33, -108 },
            { 30, -103, -3, -1 },
            { -101, -102, -105, 33 },
            { 30, -101, -102, -104 },
            { 57, -100, 32, -3 },
            { -85, -99, -97, -89 },
            { 38, -98, -97, -96 },
            { -98, -97, -95, -94 },
            { -98, -96, -94, -105 },
            { 36, -95, 35, -105 },
            { -96, -94, -105, -106 },
            { 71, -93, -108, 1 },
            { 74, -92, -78, 87 },
            { 74, -91, -78, -107 },
            { 9, -90, 71, 86 },
            { 41, -89, -94, 35 },
            { -87, -88, 42, -91 },
            { 39, -87, 41, -88 },
            { -85, -86, -97, -89 },
            { 39, -85, -99, -97 },
            { 39, -84, -82, -81 },
            { 46, -83, 83, -81 },
            { -84, -82, -80, 42 },
            { -83, -81, 42, -79 },
            { 83, -80, -91, -92 },
            { 92, -79, -77, -78 },
            { 86, -78, 1, 1 },
            { 86, -77, 2, 1 },
            { 89, -76, 87, 2 },
            { 44, -75, -76, -77 },
            { -71, -74, -75, 43 },
            { -71, -73, -75, 43 },
            { 46, -72, -71, 44 },
            { -72, -71, 44, -69 },
            { -71, -70, 43, -76 },
            { -65, -69, 102, -76 },
            { 92, -68, -67, 117 },
            { 116, -67, 1, 1 },
            { 17, -66, -55, -32 },
            { 17, -65, 16, -55 },
            { -63, -64, 16, 102 },
            { -61, -63, -65, -66 },
            { 19, -62, -64, -65 },
            { 19, -61, -63, 17 },
            { 19, -60, -44, 17 },
            { 19, -59, -57, -46 },
            { 19, -58, 17, -47 },
            { 18, -57, -56, 16 },
            { -46, -56, -49, -52 },
            { -66, -55, -32, 24 },
            { 116, -54, 1, 1 },
            { 22, -53, 24, 23 },
            { -49, -52, 23, 1 },
            { -40, -51, -53, -54 },
            { -41, -50, -35, -34 },
            { -40, -49, -52, -53 },
            { -46, -48, -52, -34 },
            { -45, -47, -40, 115 },
            { -59, -46, -56, -48 },
            { -60, -45, 20, -39 },
            { -60, -44, -56, -48 },
            { 127, -43, 124, 123 },
            { 18, -42, -41, -50 },
            { -42, -41, -50, -52 },
            { 20, -40, -49, -51 },
            { -25, -39, -37, 22 },
            { -39, -38, -35, -34 },
            { -39, -37, -30, -35 },
            { 20, -36, -35, -34 },
            { -36, -35, 23, 1 },
            { -36, -34, 1, 1 },
            { 22, -33, 1, 1 },
            { 119, -32, 1, 1 },
            { -29, -31, 23, 1 },
            { -37, -30, -33, 23 },
            { -26, -29, -31, -13 },
            { -26, -28, -27, -30 },
            { -18, -27, -33, 23 },
            { -25, -26, -28, -29 },
            { -23, -25, -26, -127 },
            { -22, -24, -26, -127 },
            { 15, -23, 125, -128 },
            { 15, -22, -24, -113 },
            { -24, -21, 26, -29 },
            { 28, -20, -19, -17 },
            { -20, -19, -16, 25 },
            { -26, -18, -16, -27 },
            { -20, -17, 25, -13 },
            { -18, -16, -31, 25 },
            { -122, -15, 24, 1 },
            { -10, -14, -108, 1 },
            { -125, -13, 1, 1 },
            { -15, -12, 1, 1 },
            { -122, -11, 1, 1 },
            { 26, -10, -13, -108 },
            { -5, -9, 25, 33 },
            { 28, -8, -5, -4 },
            { 28, -7, -5, -4 },
            { 67, -6, -7, -112 },
            { -8, -5, -9, -1 },
            { -8, -4, -1, 33 },
            { -4, -3, 33, -108 },
            { -111, -2, -11, 34 },
            { 32, -1, 33, -108 },
    };
    
    /**
     * Color values as RGBA8888 ints to use when shading an Aurora-palette model.
     * The color in index 1 of each 4-element sub-array is the "dimmer" color and the one that will match the voxel
     * color used in a model. The color at index 0 is "bright", index 2 is "dim", and index 3 is "dark".
     * To visualize this, <a href="https://i.imgur.com/rQDrPE7.png">use this image</a>, with the first 32 items in
     * AURORA_RAMPS corresponding to the first column from top to bottom, the next 32 items in the second column, etc.
     */
    public static final int[][] AURORA_RAMP_VALUES = new int[][]{
            { 0x00000000, 0x00000000, 0x00000000, 0x00000000 },
            { 0x252525FF, 0x010101FF, 0x010101FF, 0x010101FF },
            { 0x3C233CFF, 0x131313FF, 0x010101FF, 0x010101FF },
            { 0x3B3B57FF, 0x252525FF, 0x010101FF, 0x010101FF },
            { 0x5B5B5BFF, 0x373737FF, 0x0F192DFF, 0x131313FF },
            { 0x6E6E6EFF, 0x494949FF, 0x3C233CFF, 0x252525FF },
            { 0x6E8287FF, 0x5B5B5BFF, 0x3B3B57FF, 0x373737FF },
            { 0x7E9494FF, 0x6E6E6EFF, 0x494949FF, 0x3B3B57FF },
            { 0xA4A4A4FF, 0x808080FF, 0x5B5B5BFF, 0x494949FF },
            { 0xBCAFC0FF, 0x929292FF, 0x6E6E6EFF, 0x5B5B5BFF },
            { 0xC9C9C9FF, 0xA4A4A4FF, 0x808080FF, 0x6E8287FF },
            { 0xE3C7E3FF, 0xB6B6B6FF, 0x929292FF, 0x7E9494FF },
            { 0xE3E3FFFF, 0xC9C9C9FF, 0xA4A4A4FF, 0x929292FF },
            { 0xFFFFFFFF, 0xDBDBDBFF, 0xB6B6B6FF, 0xBCAFC0FF },
            { 0xFFFFFFFF, 0xEDEDEDFF, 0xC9C9C9FF, 0xB6B6B6FF },
            { 0xFFFFFFFF, 0xFFFFFFFF, 0xDBDBDBFF, 0xE3C7E3FF },
            { 0x06C491FF, 0x007F7FFF, 0x0F377DFF, 0x0C2148FF },
            { 0x5AC5FFFF, 0x3FBFBFFF, 0x06C491FF, 0x129880FF },
            { 0x55E6FFFF, 0x00FFFFFF, 0x00BFFFFF, 0x109CDEFF },
            { 0xFFFFFFFF, 0xBFFFFFFF, 0xABC7E3FF, 0x8FC7C7FF },
            { 0xB991FFFF, 0x8181FFFF, 0x4A5AFFFF, 0x6241F6FF },
            { 0x3C3CF5FF, 0x0000FFFF, 0x00007FFF, 0x010101FF },
            { 0x4A5AFFFF, 0x3F3FBFFF, 0x5010B0FF, 0x231094FF },
            { 0x101CDAFF, 0x00007FFF, 0x010101FF, 0x010101FF },
            { 0x0F377DFF, 0x0F0F50FF, 0x010101FF, 0x010101FF },
            { 0x8C14BEFF, 0x7F007FFF, 0x320A46FF, 0x010101FF },
            { 0xFF52FFFF, 0xBF3FBFFF, 0xBD10C5FF, 0x8C14BEFF },
            { 0xFF52FFFF, 0xF500F5FF, 0x8C14BEFF, 0x7F007FFF },
            { 0xF8C6FCFF, 0xFD81FFFF, 0xFF52FFFF, 0xBF3FBFFF },
            { 0xFFFFFFFF, 0xFFC0CBFF, 0xD7A0BEFF, 0xC78FB9FF },
            { 0xFAA0B9FF, 0xFF8181FF, 0xD5524AFF, 0xBF3F3FFF },
            { 0xFF3C0AFF, 0xFF0000FF, 0xA5140AFF, 0x7F0000FF },
            { 0xFF6262FF, 0xBF3F3FFF, 0xBD1039FF, 0x911437FF },
            { 0x911437FF, 0x7F0000FF, 0x280A1EFF, 0x010101FF },
            { 0x573B3BFF, 0x551414FF, 0x010101FF, 0x010101FF },
            { 0xBF3F3FFF, 0x7F3F00FF, 0x621800FF, 0x401811FF },
            { 0xE19B7DFF, 0xBF7F3FFF, 0xBF3F3FFF, 0xA04B05FF },
            { 0xFFA53CFF, 0xFF7F00FF, 0xB45A00FF, 0xDA2010FF },
            { 0xFFFFBFFF, 0xFFBF81FF, 0xE19B7DFF, 0xD08A74FF },
            { 0xFFFFFFFF, 0xFFFFBFFF, 0xDADAABFF, 0xE3C7ABFF },
            { 0xFFFFBFFF, 0xFFFF00FF, 0xB1B10AFF, 0xAC9400FF },
            { 0xE6D55AFF, 0xBFBF3FFF, 0xAC9400FF, 0x7F7F00FF },
            { 0xB1B10AFF, 0x7F7F00FF, 0x53500AFF, 0x5F3214FF },
            { 0x00C514FF, 0x007F00FF, 0x191E0FFF, 0x010101FF },
            { 0x4BF05AFF, 0x3FBF3FFF, 0x1C8C4EFF, 0x149605FF },
            { 0x4BF05AFF, 0x00FF00FF, 0x00C514FF, 0x149605FF },
            { 0xE1F8FAFF, 0xAFFFAFFF, 0x8FC78FFF, 0x87B48EFF },
            { 0xE3C7E3FF, 0xBCAFC0FF, 0x929292FF, 0xAB73ABFF },
            { 0xE3C7ABFF, 0xCBAA89FF, 0xC07872FF, 0xAB7373FF },
            { 0xC9C9C9FF, 0xA6A090FF, 0x808080FF, 0x6E6E6EFF },
            { 0xB6B6B6FF, 0x7E9494FF, 0x6E6E6EFF, 0x73578FFF },
            { 0xA4A4A4FF, 0x6E8287FF, 0x5B5B5BFF, 0x494973FF },
            { 0x929292FF, 0x7E6E60FF, 0x494949FF, 0x573B3BFF },
            { 0xC78F8FFF, 0xA0695FFF, 0x98344DFF, 0x73413CFF },
            { 0xE19B7DFF, 0xC07872FF, 0x8E5555FF, 0x98344DFF },
            { 0xEBAA8CFF, 0xD08A74FF, 0xA0695FFF, 0x8E5555FF },
            { 0xF6C8AFFF, 0xE19B7DFF, 0xC07872FF, 0xA0695FFF },
            { 0xFFC0CBFF, 0xEBAA8CFF, 0xD08A74FF, 0xC07872FF },
            { 0xF8D2DAFF, 0xF5B99BFF, 0xD08A74FF, 0xC07872FF },
            { 0xFFFFBFFF, 0xF6C8AFFF, 0xCBAA89FF, 0xC78F8FFF },
            { 0xFFFFFFFF, 0xF5E1D2FF, 0xC9C9C9FF, 0xE1B9D2FF },
            { 0x8E5555FF, 0x573B3BFF, 0x321623FF, 0x280A1EFF },
            { 0x7E6E60FF, 0x73413CFF, 0x551937FF, 0x401811FF },
            { 0xAB7373FF, 0x8E5555FF, 0x573B3BFF, 0x551937FF },
            { 0xC78F8FFF, 0xAB7373FF, 0x8E5555FF, 0x73413CFF },
            { 0xE3ABABFF, 0xC78F8FFF, 0xAB7373FF, 0xA0695FFF },
            { 0xF8D2DAFF, 0xE3ABABFF, 0xC78F8FFF, 0xC87DA0FF },
            { 0xFFFFFFFF, 0xF8D2DAFF, 0xD7A0BEFF, 0xC78FB9FF },
            { 0xEDEDEDFF, 0xE3C7ABFF, 0xCBAA89FF, 0xC78F8FFF },
            { 0xE3ABABFF, 0xC49E73FF, 0xAB7373FF, 0xA0695FFF },
            { 0x929292FF, 0x8F7357FF, 0x73573BFF, 0x73413CFF },
            { 0x8F7357FF, 0x73573BFF, 0x414123FF, 0x3B2D1FFF },
            { 0x494949FF, 0x3B2D1FFF, 0x010101FF, 0x010101FF },
            { 0x73573BFF, 0x414123FF, 0x191E0FFF, 0x131313FF },
            { 0x8F8F57FF, 0x73733BFF, 0x465032FF, 0x414123FF },
            { 0xA6A090FF, 0x8F8F57FF, 0x73733BFF, 0x73573BFF },
            { 0xCBAA89FF, 0xA2A255FF, 0x8F7357FF, 0x73733BFF },
            { 0xE3C7ABFF, 0xB5B572FF, 0x8F8F57FF, 0x8C805AFF },
            { 0xEDEDC7FF, 0xC7C78FFF, 0xC49E73FF, 0xA2A255FF },
            { 0xEDEDEDFF, 0xDADAABFF, 0xCBAA89FF, 0xA6A090FF },
            { 0xFFFFFFFF, 0xEDEDC7FF, 0xC9C9C9FF, 0xB6B6B6FF },
            { 0xEDEDEDFF, 0xC7E3ABFF, 0xABC78FFF, 0x8FC78FFF },
            { 0xC7E3ABFF, 0xABC78FFF, 0x929292FF, 0x8F8F57FF },
            { 0xABC78FFF, 0x8EBE55FF, 0x738F57FF, 0x587D3EFF },
            { 0x87B48EFF, 0x738F57FF, 0x506450FF, 0x465032FF },
            { 0x73AB73FF, 0x587D3EFF, 0x3B573BFF, 0x465032FF },
            { 0x507D5FFF, 0x465032FF, 0x1E2D23FF, 0x191E0FFF },
            { 0x373737FF, 0x191E0FFF, 0x010101FF, 0x010101FF },
            { 0x3B7349FF, 0x235037FF, 0x0F192DFF, 0x131313FF },
            { 0x507D5FFF, 0x3B573BFF, 0x1E2D23FF, 0x252525FF },
            { 0x6E8287FF, 0x506450FF, 0x373737FF, 0x1E2D23FF },
            { 0x6E8287FF, 0x3B7349FF, 0x235037FF, 0x123832FF },
            { 0x73AB73FF, 0x578F57FF, 0x3B573BFF, 0x235037FF },
            { 0x8FC78FFF, 0x73AB73FF, 0x578F57FF, 0x507D5FFF },
            { 0x8FC7C7FF, 0x64C082FF, 0x578F57FF, 0x507D5FFF },
            { 0xABE3C5FF, 0x8FC78FFF, 0x73AB73FF, 0x6E8287FF },
            { 0xC7F1F1FF, 0xA2D8A2FF, 0x87B48EFF, 0x73AB73FF },
            { 0xFFFFFFFF, 0xE1F8FAFF, 0xBED2F0FF, 0xABC7E3FF },
            { 0xE1F8FAFF, 0xB4EECAFF, 0x8FC7C7FF, 0x8FC78FFF },
            { 0xE1F8FAFF, 0xABE3C5FF, 0x87B48EFF, 0x64ABABFF },
            { 0xA2D8A2FF, 0x87B48EFF, 0x808080FF, 0x6E8287FF },
            { 0x7E9494FF, 0x507D5FFF, 0x3B573BFF, 0x235037FF },
            { 0x3B7373FF, 0x0F6946FF, 0x123832FF, 0x0C2148FF },
            { 0x494949FF, 0x1E2D23FF, 0x010101FF, 0x010101FF },
            { 0x3B7373FF, 0x234146FF, 0x0F192DFF, 0x010101FF },
            { 0x6E8287FF, 0x3B7373FF, 0x3B3B57FF, 0x234146FF },
            { 0x8FC7C7FF, 0x64ABABFF, 0x57738FFF, 0x3B7373FF },
            { 0xABE3E3FF, 0x8FC7C7FF, 0x64ABABFF, 0x578FC7FF },
            { 0xE1F8FAFF, 0xABE3E3FF, 0x8FC7C7FF, 0x8FABC7FF },
            { 0xFFFFFFFF, 0xC7F1F1FF, 0xABC7E3FF, 0xA8B9DCFF },
            { 0xE1F8FAFF, 0xBED2F0FF, 0xABABE3FF, 0x8FABC7FF },
            { 0xE3E3FFFF, 0xABC7E3FF, 0x8FABC7FF, 0x8F8FC7FF },
            { 0xD0DAF8FF, 0xA8B9DCFF, 0x8F8FC7FF, 0x7676CAFF },
            { 0xABC7E3FF, 0x8FABC7FF, 0x7E9494FF, 0x736EAAFF },
            { 0x90B0FFFF, 0x578FC7FF, 0x326496FF, 0x0F377DFF },
            { 0x7E9494FF, 0x57738FFF, 0x3B5773FF, 0x494973FF },
            { 0x57738FFF, 0x3B5773FF, 0x162C52FF, 0x0C2148FF },
            { 0x234146FF, 0x0F192DFF, 0x010101FF, 0x010101FF },
            { 0x3B3B57FF, 0x1F1F3BFF, 0x010101FF, 0x010101FF },
            { 0x57578FFF, 0x3B3B57FF, 0x1F1F3BFF, 0x0F192DFF },
            { 0x57738FFF, 0x494973FF, 0x162C52FF, 0x1F1F3BFF },
            { 0x7676CAFF, 0x57578FFF, 0x3B3B57FF, 0x162C52FF },
            { 0x8F8FC7FF, 0x736EAAFF, 0x494973FF, 0x573B73FF },
            { 0x90B0FFFF, 0x7676CAFF, 0x57578FFF, 0x3F3FBFFF },
            { 0xABABE3FF, 0x8F8FC7FF, 0x736EAAFF, 0x73578FFF },
            { 0xBED2F0FF, 0xABABE3FF, 0x8F8FC7FF, 0x7676CAFF },
            { 0xFFFFFFFF, 0xD0DAF8FF, 0xA8B9DCFF, 0xABABE3FF },
            { 0xFFFFFFFF, 0xE3E3FFFF, 0xBEB9FAFF, 0xA8B9DCFF },
            { 0xBEB9FAFF, 0xAB8FC7FF, 0x736EAAFF, 0x73578FFF },
            { 0xBD62FFFF, 0x8F57C7FF, 0x8C14BEFF, 0x5A187BFF },
            { 0xAB73ABFF, 0x73578FFF, 0x573B73FF, 0x5A187BFF },
            { 0x73578FFF, 0x573B73FF, 0x410062FF, 0x320A46FF },
            { 0x494949FF, 0x3C233CFF, 0x010101FF, 0x010101FF },
            { 0x724072FF, 0x463246FF, 0x280A1EFF, 0x010101FF },
            { 0xAB57ABFF, 0x724072FF, 0x641464FF, 0x3C233CFF },
            { 0xAB73ABFF, 0x8F578FFF, 0x573B73FF, 0x641464FF },
            { 0xAB8FC7FF, 0xAB57ABFF, 0x724072FF, 0xA01982FF },
            { 0xD7A0BEFF, 0xAB73ABFF, 0x8F578FFF, 0x73578FFF },
            { 0xFFDCF5FF, 0xEBACE1FF, 0xC78FB9FF, 0xC87DA0FF },
            { 0xFFFFFFFF, 0xFFDCF5FF, 0xE1B9D2FF, 0xEBACE1FF },
            { 0xFFFFFFFF, 0xE3C7E3FF, 0xBCAFC0FF, 0xC78FB9FF },
            { 0xFFDCF5FF, 0xE1B9D2FF, 0xC78FB9FF, 0xAB73ABFF },
            { 0xE3C7E3FF, 0xD7A0BEFF, 0xC87DA0FF, 0xAB73ABFF },
            { 0xEBACE1FF, 0xC78FB9FF, 0xAB57ABFF, 0x8F578FFF },
            { 0xD7A0BEFF, 0xC87DA0FF, 0xAB57ABFF, 0x8F578FFF },
            { 0xC78FB9FF, 0xC35A91FF, 0x8E5555FF, 0xA01982FF },
            { 0x724072FF, 0x4B2837FF, 0x280A1EFF, 0x010101FF },
            { 0x463246FF, 0x321623FF, 0x010101FF, 0x010101FF },
            { 0x4B2837FF, 0x280A1EFF, 0x010101FF, 0x010101FF },
            { 0x573B3BFF, 0x401811FF, 0x010101FF, 0x010101FF },
            { 0x7F3F00FF, 0x621800FF, 0x280A1EFF, 0x010101FF },
            { 0xBF3F3FFF, 0xA5140AFF, 0x280A1EFF, 0x010101FF },
            { 0xF55A32FF, 0xDA2010FF, 0x7F0000FF, 0x280A1EFF },
            { 0xFF8181FF, 0xD5524AFF, 0xBD1039FF, 0x911437FF },
            { 0xF55A32FF, 0xFF3C0AFF, 0xA5140AFF, 0x7F0000FF },
            { 0xFF8181FF, 0xF55A32FF, 0xFF3C0AFF, 0xDA2010FF },
            { 0xEBAA8CFF, 0xFF6262FF, 0xBF3F3FFF, 0xBD1039FF },
            { 0xFFEA4AFF, 0xF6BD31FF, 0xD79B0FFF, 0xAC9400FF },
            { 0xFFBF81FF, 0xFFA53CFF, 0xD79B0FFF, 0xDA6E0AFF },
            { 0xFFA53CFF, 0xD79B0FFF, 0xB45A00FF, 0xA04B05FF },
            { 0xFFA53CFF, 0xDA6E0AFF, 0xA04B05FF, 0xA5140AFF },
            { 0xBF7F3FFF, 0xB45A00FF, 0x7F3F00FF, 0xA5140AFF },
            { 0xDA6E0AFF, 0xA04B05FF, 0xA5140AFF, 0x621800FF },
            { 0x73573BFF, 0x5F3214FF, 0x280A1EFF, 0x010101FF },
            { 0x73733BFF, 0x53500AFF, 0x283405FF, 0x191E0FFF },
            { 0x73733BFF, 0x626200FF, 0x283405FF, 0x401811FF },
            { 0x929292FF, 0x8C805AFF, 0x73573BFF, 0x465032FF },
            { 0xBFBF3FFF, 0xAC9400FF, 0xA04B05FF, 0x7F3F00FF },
            { 0xE6D55AFF, 0xB1B10AFF, 0x7F7F00FF, 0x626200FF },
            { 0xFFFFBFFF, 0xE6D55AFF, 0xBFBF3FFF, 0xB1B10AFF },
            { 0xFFEA4AFF, 0xFFD510FF, 0xD79B0FFF, 0xAC9400FF },
            { 0xFFFFBFFF, 0xFFEA4AFF, 0xF6BD31FF, 0xD79B0FFF },
            { 0xFFFFBFFF, 0xC8FF41FF, 0x96DC19FF, 0x73C805FF },
            { 0xAFFFAFFF, 0x9BF046FF, 0x8EBE55FF, 0x73C805FF },
            { 0xC8FF41FF, 0x96DC19FF, 0x6AA805FF, 0x7F7F00FF },
            { 0x9BF046FF, 0x73C805FF, 0x7F7F00FF, 0x3C6E14FF },
            { 0x8EBE55FF, 0x6AA805FF, 0x626200FF, 0x53500AFF },
            { 0x578F57FF, 0x3C6E14FF, 0x204608FF, 0x283405FF },
            { 0x465032FF, 0x283405FF, 0x010101FF, 0x010101FF },
            { 0x465032FF, 0x204608FF, 0x131313FF, 0x010101FF },
            { 0x3B573BFF, 0x0C5C0CFF, 0x191E0FFF, 0x131313FF },
            { 0x3FBF3FFF, 0x149605FF, 0x0C5C0CFF, 0x204608FF },
            { 0x4BF05AFF, 0x0AD70AFF, 0x149605FF, 0x007F00FF },
            { 0x4BF05AFF, 0x14E60AFF, 0x149605FF, 0x007F00FF },
            { 0xAFFFAFFF, 0x7DFF73FF, 0x4BF05AFF, 0x3FBF3FFF },
            { 0x7DFF73FF, 0x4BF05AFF, 0x3FBF3FFF, 0x05B450FF },
            { 0x4BF05AFF, 0x00C514FF, 0x007F00FF, 0x0C5C0CFF },
            { 0x06C491FF, 0x05B450FF, 0x0F6946FF, 0x0C5C0CFF },
            { 0x578F57FF, 0x1C8C4EFF, 0x123832FF, 0x0F192DFF },
            { 0x3B5773FF, 0x123832FF, 0x010101FF, 0x010101FF },
            { 0x3FBFBFFF, 0x129880FF, 0x055A5CFF, 0x0C2148FF },
            { 0x3FBFBFFF, 0x06C491FF, 0x007F7FFF, 0x055A5CFF },
            { 0x2DEBA8FF, 0x00DE6AFF, 0x007F7FFF, 0x0F6946FF },
            { 0x6AFFCDFF, 0x2DEBA8FF, 0x06C491FF, 0x129880FF },
            { 0xBFFFFFFF, 0x3CFEA5FF, 0x00DE6AFF, 0x06C491FF },
            { 0xBFFFFFFF, 0x6AFFCDFF, 0x2DEBA8FF, 0x3FBFBFFF },
            { 0xBFFFFFFF, 0x91EBFFFF, 0x5AC5FFFF, 0x3FBFBFFF },
            { 0xBFFFFFFF, 0x55E6FFFF, 0x08DED5FF, 0x4AA4FFFF },
            { 0xBFFFFFFF, 0x7DD7F0FF, 0x3FBFBFFF, 0x699DC3FF },
            { 0x00FFFFFF, 0x08DED5FF, 0x109CDEFF, 0x007F7FFF },
            { 0x4AA4FFFF, 0x109CDEFF, 0x186ABDFF, 0x004A9CFF },
            { 0x129880FF, 0x055A5CFF, 0x0C2148FF, 0x0F0F50FF },
            { 0x3B5773FF, 0x162C52FF, 0x010101FF, 0x010101FF },
            { 0x3F3FBFFF, 0x0F377DFF, 0x0F0F50FF, 0x00007FFF },
            { 0x186ABDFF, 0x004A9CFF, 0x00007FFF, 0x010101FF },
            { 0x4B7DC8FF, 0x326496FF, 0x0F377DFF, 0x162C52FF },
            { 0x007FFFFF, 0x0052F6FF, 0x101CDAFF, 0x0010BDFF },
            { 0x4B7DC8FF, 0x186ABDFF, 0x004A9CFF, 0x0F377DFF },
            { 0x4AA4FFFF, 0x2378DCFF, 0x004A9CFF, 0x0010BDFF },
            { 0x90B0FFFF, 0x699DC3FF, 0x4B7DC8FF, 0x57738FFF },
            { 0x55E6FFFF, 0x4AA4FFFF, 0x109CDEFF, 0x2378DCFF },
            { 0x91EBFFFF, 0x90B0FFFF, 0x8181FFFF, 0x786EF0FF },
            { 0x91EBFFFF, 0x5AC5FFFF, 0x109CDEFF, 0x2378DCFF },
            { 0xE3E3FFFF, 0xBEB9FAFF, 0x8F8FC7FF, 0x7676CAFF },
            { 0x00FFFFFF, 0x00BFFFFF, 0x007FFFFF, 0x0052F6FF },
            { 0x00BFFFFF, 0x007FFFFF, 0x0052F6FF, 0x004A9CFF },
            { 0x8181FFFF, 0x4B7DC8FF, 0x186ABDFF, 0x326496FF },
            { 0xB991FFFF, 0x786EF0FF, 0x6241F6FF, 0x3F3FBFFF },
            { 0x786EF0FF, 0x4A5AFFFF, 0x101CDAFF, 0x0010BDFF },
            { 0x786EF0FF, 0x6241F6FF, 0x6010D0FF, 0x101CDAFF },
            { 0x8181FFFF, 0x3C3CF5FF, 0x101CDAFF, 0x0010BDFF },
            { 0x3C3CF5FF, 0x101CDAFF, 0x00007FFF, 0x010101FF },
            { 0x3C3CF5FF, 0x0010BDFF, 0x010101FF, 0x010101FF },
            { 0x3F3FBFFF, 0x231094FF, 0x010101FF, 0x010101FF },
            { 0x3B3B57FF, 0x0C2148FF, 0x010101FF, 0x010101FF },
            { 0x8732D2FF, 0x5010B0FF, 0x00007FFF, 0x010101FF },
            { 0x6241F6FF, 0x6010D0FF, 0x231094FF, 0x00007FFF },
            { 0xBD62FFFF, 0x8732D2FF, 0x5010B0FF, 0x410062FF },
            { 0xBD62FFFF, 0x9C41FFFF, 0x7F00FFFF, 0x6010D0FF },
            { 0xBD29FFFF, 0x7F00FFFF, 0x231094FF, 0x00007FFF },
            { 0xB991FFFF, 0xBD62FFFF, 0x9C41FFFF, 0x8732D2FF },
            { 0xD7C3FAFF, 0xB991FFFF, 0xBD62FFFF, 0x8F57C7FF },
            { 0xF8C6FCFF, 0xD7A5FFFF, 0xBD62FFFF, 0x8F57C7FF },
            { 0xFFFFFFFF, 0xD7C3FAFF, 0xABABE3FF, 0xAB8FC7FF },
            { 0xFFFFFFFF, 0xF8C6FCFF, 0xD7A5FFFF, 0xC78FB9FF },
            { 0xD7A5FFFF, 0xE673FFFF, 0xBF3FBFFF, 0x8732D2FF },
            { 0xFD81FFFF, 0xFF52FFFF, 0xDA20E0FF, 0xBD10C5FF },
            { 0xFF52FFFF, 0xDA20E0FF, 0x8C14BEFF, 0x7F007FFF },
            { 0xBD62FFFF, 0xBD29FFFF, 0x8C14BEFF, 0x7F00FFFF },
            { 0xFF52FFFF, 0xBD10C5FF, 0x7F007FFF, 0x410062FF },
            { 0xBD29FFFF, 0x8C14BEFF, 0x5010B0FF, 0x7F007FFF },
            { 0x724072FF, 0x5A187BFF, 0x0F0F50FF, 0x010101FF },
            { 0xA01982FF, 0x641464FF, 0x280A1EFF, 0x010101FF },
            { 0x573B73FF, 0x410062FF, 0x010101FF, 0x010101FF },
            { 0x5A187BFF, 0x320A46FF, 0x010101FF, 0x010101FF },
            { 0x724072FF, 0x551937FF, 0x010101FF, 0x010101FF },
            { 0xBF3FBFFF, 0xA01982FF, 0x410062FF, 0x280A1EFF },
            { 0xFC3A8CFF, 0xC80078FF, 0x7F007FFF, 0x7F0000FF },
            { 0xFD81FFFF, 0xFF50BFFF, 0xFC3A8CFF, 0xE61E78FF },
            { 0xFD81FFFF, 0xFF6AC5FF, 0xFC3A8CFF, 0xE61E78FF },
            { 0xF8D2DAFF, 0xFAA0B9FF, 0xFF6AC5FF, 0xC87DA0FF },
            { 0xFF50BFFF, 0xFC3A8CFF, 0xC80078FF, 0x911437FF },
            { 0xFF50BFFF, 0xE61E78FF, 0x911437FF, 0x7F0000FF },
            { 0xE61E78FF, 0xBD1039FF, 0x7F0000FF, 0x280A1EFF },
            { 0xC35A91FF, 0x98344DFF, 0x551937FF, 0x551414FF },
            { 0xBF3F3FFF, 0x911437FF, 0x7F0000FF, 0x280A1EFF },
    };


    /**
     * Bytes that correspond to palette indices to use when shading an Aurora-palette model and ramps should consider
     * the aesthetic warmth or coolness of a color, brightening to a warmer color and darkening to a cooler one.
     * The color in index 1 of each 4-element sub-array is the "dimmer" color and the one that will match the voxel
     * color used in a model. The color at index 0 is "bright", index 2 is "dim", and index 3 is "dark". Normal usage
     * with a renderer will use {@link #AURORA_WARMTH_RAMP_VALUES}; this array would be used to figure out what indices are
     * related to another color for the purpose of procedurally using similar colors with different lightness.
     * To visualize this, <a href="https://i.imgur.com/zeJpyQ4.png">use this image</a>, with the first 32 items in
     * AURORA_WARMTH_RAMPS corresponding to the first column from top to bottom, the next 32 items in the second column, etc.
     */
    public static final byte[][] AURORA_WARMTH_RAMPS = new byte[][]{
            {0, 0, 0, 0},
            {2, 1, 1, 1},
            {87, 2, 1, 1},
            {103, 3, 87, 2},
            {73, 4, 103, 118},
            {62, 5, 119, 4},
            {63, 6, 5, 119},
            {52, 7, 6, 116},
            {64, 8, 51, 7},
            {49, 9, 50, 8},
            {48, 10, 49, 9},
            {66, 11, 10, 113},
            {-116, 12, 11, 112},
            {60, 13, 110, 12},
            {15, 14, 127, 13},
            {15, 15, 97, 109},
            {105, 16, -55, -52},
            {106, 17, -65, -56},
            {-59, 18, -57, -42},
            {97, 19, -60, -59},
            {-25, 20, -39, -40},
            {-27, 21, -34, 21},
            {-29, 22, -53, 23},
            {-33, 23, 1, 1},
            {-12, 24, 1, 1},
            {-10, 25, -14, -13},
            {-111, 26, -29, 22},
            {-19, 27, -17, -16},
            {-6, 28, -21, -26},
            {67, 29, -115, 47},
            {57, 30, 55, 54},
            {-102, 31, -105, -1},
            {-103, 32, -2, 62},
            {-105, 33, 34, -108},
            {-106, 34, -107, -108},
            {-94, 35, -93, 72},
            {-97, 36, 70, 52},
            {-98, 37, -96, 36},
            {59, 38, 57, 48},
            {15, 39, 80, 81},
            {-85, 40, -86, -82},
            {-87, 41, 83, 76},
            {-89, 42, -91, -79},
            {-75, 43, -76, -55},
            {83, 44, -69, -66},
            {-73, 45, -74, -70},
            {80, 46, -72, -61},
            {11, 47, 10, 113},
            {57, 48, 69, 49},
            {48, 49, 9, 50},
            {9, 50, 51, 115},
            {8, 51, 115, 105},
            {70, 52, 7, 6},
            {64, 53, 63, 6},
            {55, 54, 64, 53},
            {56, 55, 54, 64},
            {57, 56, 69, 55},
            {58, 57, 56, 48},
            {59, 58, 57, 48},
            {60, 59, 68, 12},
            {80, 60, 13, 12},
            {62, 61, 4, -67},
            {-2, 62, 61, -123},
            {53, 63, 71, 62},
            {54, 64, 53, 52},
            {56, 65, 9, 64},
            {58, 66, -114, -113},
            {60, 67, -116, 12},
            {59, 68, 78, 11},
            {56, 69, 76, 75},
            {53, 70, 52, 74},
            {63, 71, 86, 5},
            {-93, 72, 3, 87},
            {-92, 73, 4, 72},
            {70, 74, 85, 90},
            {76, 75, 84, 92},
            {69, 76, 75, 84},
            {48, 77, 100, 76},
            {68, 78, 82, 77},
            {60, 79, 81, 78},
            {39, 80, 81, 99},
            {79, 81, 96, 107},
            {78, 82, 95, 100},
            {77, 83, 93, 92},
            {75, 84, 92, 85},
            {74, 85, 91, 102},
            {71, 86, 73, 4},
            {-107, 87, 2, 1},
            {89, 88, 104, -67},
            {86, 89, 88, 104},
            {74, 90, 89, 88},
            {85, 91, 102, -55},
            {84, 92, 101, 91},
            {100, 93, 92, -66},
            {100, 94, 106, -66},
            {82, 95, 100, 94},
            {81, 96, 95, 94},
            {15, 97, 109, 108},
            {80, 98, 99, -58},
            {98, 99, 107, -58},
            {95, 100, 93, 106},
            {92, 101, 91, 105},
            {91, 102, -55, -53},
            {72, 103, 87, 117},
            {88, 104, -67, -54},
            {101, 105, 116, 102},
            {100, 106, 114, -66},
            {111, 107, 106, 17},
            {98, 108, -58, -44},
            {97, 109, 108, -60},
            {126, 110, 111, -58},
            {110, 111, 112, 107},
            {111, 112, 113, -45},
            {47, 113, -47, 114},
            {-47, 114, -40, -48},
            {51, 115, 105, -51},
            {6, 116, 104, -53},
            {118, 117, 1, 1},
            {-124, 118, 117, 24},
            {5, 119, 104, -54},
            {-122, 120, 119, -53},
            {-126, 121, 116, 120},
            {-119, 122, 115, 121},
            {124, 123, -40, -51},
            {-128, 124, 123, 114},
            {112, 125, 113, 124},
            {127, 126, 110, 111},
            {14, 127, 126, 110},
            {-113, -128, 124, 123},
            {-120, -127, -126, 121},
            {-121, -126, 121, 120},
            {-122, -125, 119, -54},
            {-110, -124, 3, -109},
            {61, -123, 4, -124},
            {63, -122, -125, 119},
            {-111, -121, -126, 121},
            {-111, -120, -121, -126},
            {-112, -119, 122, -121},
            {29, -118, -24, -114},
            {15, -117, -22, -116},
            {67, -116, 12, -43},
            {29, -115, 47, 125},
            {66, -114, -113, -128},
            {-114, -113, -128, -119},
            {65, -112, -119, 122},
            {54, -111, -120, -121},
            {61, -110, -124, -109},
            {-107, -109, -108, 2},
            {-109, -108, 2, 1},
            {34, -107, -109, -108},
            {-105, -106, 34, -107},
            {-104, -105, -106, 33},
            {-102, -104, -105, -1},
            {-101, -103, 32, 63},
            {-101, -102, -104, 32},
            {-100, -101, -103, 32},
            {30, -100, -103, -111},
            {38, -99, 41, -97},
            {-99, -98, -97, 36},
            {-98, -97, -89, 75},
            {37, -96, -95, 74},
            {-96, -95, -94, 71},
            {-95, -94, 35, -92},
            {35, -93, 72, 3},
            {-91, -92, 73, -77},
            {42, -91, -92, 73},
            {75, -90, 52, 7},
            {-97, -89, 42, 74},
            {-97, -88, -89, -80},
            {-85, -87, 41, 77},
            {-85, -86, -99, -88},
            {39, -85, -87, -83},
            {-85, -84, -83, -72},
            {-84, -83, -71, 94},
            {41, -82, -81, 44},
            {-82, -81, -80, 44},
            {-88, -80, 85, -75},
            {-91, -79, -76, 88},
            {-93, -78, 87, 2},
            {-92, -77, -78, 103},
            {-79, -76, -67, -32},
            {-80, -75, 43, 102},
            {-73, -74, -70, -69},
            {-81, -73, -74, -70},
            {46, -72, -71, -63},
            {-72, -71, -64, -65},
            {-74, -70, -69, -75},
            {44, -69, -66, -68},
            {92, -68, 102, -55},
            {4, -67, 117, -32},
            {92, -66, 16, -52},
            {17, -65, -66, 16},
            {44, -64, -65, -66},
            {-62, -63, -57, -65},
            {-61, -62, -63, -57},
            {98, -61, -62, -63},
            {108, -60, -58, -59},
            {-58, -59, -57, -42},
            {108, -58, -44, -42},
            {-63, -57, -42, -56},
            {114, -56, -41, -49},
            {102, -55, -53, -54},
            {119, -54, -32, 24},
            {119, -53, -32, 24},
            {22, -52, -53, 23},
            {115, -51, -55, -52},
            {-36, -50, -52, -34},
            {-51, -49, -52, -53},
            {-40, -48, -49, -52},
            {124, -47, 114, -40},
            {-45, -46, -56, -41},
            {125, -45, -46, -56},
            {-58, -44, -46, -42},
            {-23, -43, 125, -45},
            {-46, -42, -56, -41},
            {-48, -41, -50, -52},
            {114, -40, -48, -49},
            {123, -39, -40, -38},
            {-39, -38, -36, -50},
            {-28, -37, -36, 22},
            {-37, -36, 22, -35},
            {-30, -35, -34, 23},
            {-35, -34, 23, 1},
            {-31, -33, 23, 1},
            {118, -32, 24, 1},
            {-30, -31, -33, 23},
            {-16, -30, -31, -33},
            {26, -29, 22, -53},
            {-26, -28, -37, -36},
            {-17, -27, -30, -31},
            {-21, -26, -39, -28},
            {-24, -25, 20, -39},
            {-118, -24, -25, 20},
            {-116, -23, -43, -45},
            {-117, -22, -23, -43},
            {28, -21, -26, -39},
            {-7, -20, -26, -28},
            {-8, -19, -17, -16},
            {-19, -18, -29, -37},
            {-19, -17, -16, -31},
            {-17, -16, -31, -33},
            {-14, -15, -13, -12},
            {-10, -14, -12, 24},
            {-14, -13, -12, 24},
            {-11, -12, 24, 1},
            {-1, -11, -109, 118},
            {-4, -10, -14, -15},
            {-4, -9, -10, 25},
            {-7, -8, -111, 26},
            {30, -7, -8, -111},
            {58, -6, -114, -113},
            {-100, -5, -4, 26},
            {-5, -4, -10, -14},
            {-104, -3, -1, -11},
            {32, -2, 62, 61},
            {-3, -1, -11, -12},
    };

    /**
     * Bytes that correspond to palette indices to use when shading an Aurora-palette model and ramps should consider
     * the aesthetic warmth or coolness of a color, brightening to a warmer color and darkening to a cooler one.
     * The color in index 1 of each 4-element sub-array is the "dimmer" color and the one that will match the voxel
     * color used in a model. The color at index 0 is "bright", index 2 is "dim", and index 3 is "dark".
     * To visualize this, <a href="https://i.imgur.com/zeJpyQ4.png">use this image</a>, with the first 32 items in
     * AURORA_WARMTH_RAMPS corresponding to the first column from top to bottom, the next 32 items in the second column, etc.
     * Color values as RGBA8888 ints to use when shading an Aurora-palette model.
     */
    public static final int[][] AURORA_WARMTH_RAMP_VALUES = {
            {0x00000000, 0x00000000, 0x00000000, 0x00000000},
            {0x131313FF, 0x010101FF, 0x010101FF, 0x010101FF},
            {0x191E0FFF, 0x131313FF, 0x010101FF, 0x010101FF},
            {0x1E2D23FF, 0x252525FF, 0x191E0FFF, 0x131313FF},
            {0x414123FF, 0x373737FF, 0x1E2D23FF, 0x1F1F3BFF},
            {0x73413CFF, 0x494949FF, 0x3B3B57FF, 0x373737FF},
            {0x8E5555FF, 0x5B5B5BFF, 0x494949FF, 0x3B3B57FF},
            {0x7E6E60FF, 0x6E6E6EFF, 0x5B5B5BFF, 0x3B5773FF},
            {0xAB7373FF, 0x808080FF, 0x6E8287FF, 0x6E6E6EFF},
            {0xA6A090FF, 0x929292FF, 0x7E9494FF, 0x808080FF},
            {0xCBAA89FF, 0xA4A4A4FF, 0xA6A090FF, 0x929292FF},
            {0xE3ABABFF, 0xB6B6B6FF, 0xA4A4A4FF, 0x8FABC7FF},
            {0xE3C7E3FF, 0xC9C9C9FF, 0xB6B6B6FF, 0xA8B9DCFF},
            {0xF5E1D2FF, 0xDBDBDBFF, 0xBED2F0FF, 0xC9C9C9FF},
            {0xFFFFFFFF, 0xEDEDEDFF, 0xE3E3FFFF, 0xDBDBDBFF},
            {0xFFFFFFFF, 0xFFFFFFFF, 0xE1F8FAFF, 0xC7F1F1FF},
            {0x3B7373FF, 0x007F7FFF, 0x055A5CFF, 0x004A9CFF},
            {0x64ABABFF, 0x3FBFBFFF, 0x06C491FF, 0x109CDEFF},
            {0x55E6FFFF, 0x00FFFFFF, 0x08DED5FF, 0x00BFFFFF},
            {0xE1F8FAFF, 0xBFFFFFFF, 0x91EBFFFF, 0x55E6FFFF},
            {0xB991FFFF, 0x8181FFFF, 0x786EF0FF, 0x4B7DC8FF},
            {0x7F00FFFF, 0x0000FFFF, 0x0010BDFF, 0x0000FFFF},
            {0x8732D2FF, 0x3F3FBFFF, 0x0F377DFF, 0x00007FFF},
            {0x231094FF, 0x00007FFF, 0x010101FF, 0x010101FF},
            {0x320A46FF, 0x0F0F50FF, 0x010101FF, 0x010101FF},
            {0xA01982FF, 0x7F007FFF, 0x641464FF, 0x410062FF},
            {0xC35A91FF, 0xBF3FBFFF, 0x8732D2FF, 0x3F3FBFFF},
            {0xDA20E0FF, 0xF500F5FF, 0xBD10C5FF, 0x8C14BEFF},
            {0xFAA0B9FF, 0xFD81FFFF, 0xE673FFFF, 0xBD62FFFF},
            {0xF8D2DAFF, 0xFFC0CBFF, 0xE1B9D2FF, 0xBCAFC0FF},
            {0xEBAA8CFF, 0xFF8181FF, 0xD08A74FF, 0xC07872FF},
            {0xFF3C0AFF, 0xFF0000FF, 0xA5140AFF, 0x911437FF},
            {0xD5524AFF, 0xBF3F3FFF, 0x98344DFF, 0x73413CFF},
            {0xA5140AFF, 0x7F0000FF, 0x551414FF, 0x280A1EFF},
            {0x621800FF, 0x551414FF, 0x401811FF, 0x280A1EFF},
            {0xA04B05FF, 0x7F3F00FF, 0x5F3214FF, 0x3B2D1FFF},
            {0xD79B0FFF, 0xBF7F3FFF, 0x8F7357FF, 0x7E6E60FF},
            {0xFFA53CFF, 0xFF7F00FF, 0xDA6E0AFF, 0xBF7F3FFF},
            {0xF6C8AFFF, 0xFFBF81FF, 0xEBAA8CFF, 0xCBAA89FF},
            {0xFFFFFFFF, 0xFFFFBFFF, 0xEDEDC7FF, 0xC7E3ABFF},
            {0xFFEA4AFF, 0xFFFF00FF, 0xFFD510FF, 0x96DC19FF},
            {0xE6D55AFF, 0xBFBF3FFF, 0x8EBE55FF, 0xA2A255FF},
            {0xAC9400FF, 0x7F7F00FF, 0x626200FF, 0x3C6E14FF},
            {0x149605FF, 0x007F00FF, 0x0C5C0CFF, 0x055A5CFF},
            {0x8EBE55FF, 0x3FBF3FFF, 0x05B450FF, 0x129880FF},
            {0x14E60AFF, 0x00FF00FF, 0x0AD70AFF, 0x00C514FF},
            {0xEDEDC7FF, 0xAFFFAFFF, 0x7DFF73FF, 0x6AFFCDFF},
            {0xB6B6B6FF, 0xBCAFC0FF, 0xA4A4A4FF, 0x8FABC7FF},
            {0xEBAA8CFF, 0xCBAA89FF, 0xC49E73FF, 0xA6A090FF},
            {0xCBAA89FF, 0xA6A090FF, 0x929292FF, 0x7E9494FF},
            {0x929292FF, 0x7E9494FF, 0x6E8287FF, 0x57738FFF},
            {0x808080FF, 0x6E8287FF, 0x57738FFF, 0x3B7373FF},
            {0x8F7357FF, 0x7E6E60FF, 0x6E6E6EFF, 0x5B5B5BFF},
            {0xAB7373FF, 0xA0695FFF, 0x8E5555FF, 0x5B5B5BFF},
            {0xD08A74FF, 0xC07872FF, 0xAB7373FF, 0xA0695FFF},
            {0xE19B7DFF, 0xD08A74FF, 0xC07872FF, 0xAB7373FF},
            {0xEBAA8CFF, 0xE19B7DFF, 0xC49E73FF, 0xD08A74FF},
            {0xF5B99BFF, 0xEBAA8CFF, 0xE19B7DFF, 0xCBAA89FF},
            {0xF6C8AFFF, 0xF5B99BFF, 0xEBAA8CFF, 0xCBAA89FF},
            {0xF5E1D2FF, 0xF6C8AFFF, 0xE3C7ABFF, 0xC9C9C9FF},
            {0xEDEDC7FF, 0xF5E1D2FF, 0xDBDBDBFF, 0xC9C9C9FF},
            {0x73413CFF, 0x573B3BFF, 0x373737FF, 0x123832FF},
            {0x98344DFF, 0x73413CFF, 0x573B3BFF, 0x463246FF},
            {0xA0695FFF, 0x8E5555FF, 0x73573BFF, 0x73413CFF},
            {0xC07872FF, 0xAB7373FF, 0xA0695FFF, 0x7E6E60FF},
            {0xE19B7DFF, 0xC78F8FFF, 0x929292FF, 0xAB7373FF},
            {0xF5B99BFF, 0xE3ABABFF, 0xD7A0BEFF, 0xC78FB9FF},
            {0xF5E1D2FF, 0xF8D2DAFF, 0xE3C7E3FF, 0xC9C9C9FF},
            {0xF6C8AFFF, 0xE3C7ABFF, 0xC7C78FFF, 0xB6B6B6FF},
            {0xE19B7DFF, 0xC49E73FF, 0xA2A255FF, 0x8F8F57FF},
            {0xA0695FFF, 0x8F7357FF, 0x7E6E60FF, 0x73733BFF},
            {0x8E5555FF, 0x73573BFF, 0x465032FF, 0x494949FF},
            {0x5F3214FF, 0x3B2D1FFF, 0x252525FF, 0x191E0FFF},
            {0x53500AFF, 0x414123FF, 0x373737FF, 0x3B2D1FFF},
            {0x8F7357FF, 0x73733BFF, 0x587D3EFF, 0x506450FF},
            {0xA2A255FF, 0x8F8F57FF, 0x738F57FF, 0x578F57FF},
            {0xC49E73FF, 0xA2A255FF, 0x8F8F57FF, 0x738F57FF},
            {0xCBAA89FF, 0xB5B572FF, 0x87B48EFF, 0xA2A255FF},
            {0xE3C7ABFF, 0xC7C78FFF, 0xABC78FFF, 0xB5B572FF},
            {0xF5E1D2FF, 0xDADAABFF, 0xC7E3ABFF, 0xC7C78FFF},
            {0xFFFFBFFF, 0xEDEDC7FF, 0xC7E3ABFF, 0xABE3C5FF},
            {0xDADAABFF, 0xC7E3ABFF, 0xA2D8A2FF, 0x8FC7C7FF},
            {0xC7C78FFF, 0xABC78FFF, 0x8FC78FFF, 0x87B48EFF},
            {0xB5B572FF, 0x8EBE55FF, 0x73AB73FF, 0x578F57FF},
            {0x8F8F57FF, 0x738F57FF, 0x578F57FF, 0x587D3EFF},
            {0x73733BFF, 0x587D3EFF, 0x3B7349FF, 0x0F6946FF},
            {0x73573BFF, 0x465032FF, 0x414123FF, 0x373737FF},
            {0x401811FF, 0x191E0FFF, 0x131313FF, 0x010101FF},
            {0x3B573BFF, 0x235037FF, 0x234146FF, 0x123832FF},
            {0x465032FF, 0x3B573BFF, 0x235037FF, 0x234146FF},
            {0x73733BFF, 0x506450FF, 0x3B573BFF, 0x235037FF},
            {0x587D3EFF, 0x3B7349FF, 0x0F6946FF, 0x055A5CFF},
            {0x738F57FF, 0x578F57FF, 0x507D5FFF, 0x3B7349FF},
            {0x87B48EFF, 0x73AB73FF, 0x578F57FF, 0x129880FF},
            {0x87B48EFF, 0x64C082FF, 0x64ABABFF, 0x129880FF},
            {0xABC78FFF, 0x8FC78FFF, 0x87B48EFF, 0x64C082FF},
            {0xC7E3ABFF, 0xA2D8A2FF, 0x8FC78FFF, 0x64C082FF},
            {0xFFFFFFFF, 0xE1F8FAFF, 0xC7F1F1FF, 0xABE3E3FF},
            {0xEDEDC7FF, 0xB4EECAFF, 0xABE3C5FF, 0x7DD7F0FF},
            {0xB4EECAFF, 0xABE3C5FF, 0x8FC7C7FF, 0x7DD7F0FF},
            {0x8FC78FFF, 0x87B48EFF, 0x73AB73FF, 0x64ABABFF},
            {0x578F57FF, 0x507D5FFF, 0x3B7349FF, 0x3B7373FF},
            {0x3B7349FF, 0x0F6946FF, 0x055A5CFF, 0x0F377DFF},
            {0x3B2D1FFF, 0x1E2D23FF, 0x191E0FFF, 0x0F192DFF},
            {0x235037FF, 0x234146FF, 0x123832FF, 0x162C52FF},
            {0x507D5FFF, 0x3B7373FF, 0x3B5773FF, 0x0F6946FF},
            {0x87B48EFF, 0x64ABABFF, 0x578FC7FF, 0x129880FF},
            {0xABC7E3FF, 0x8FC7C7FF, 0x64ABABFF, 0x3FBFBFFF},
            {0xB4EECAFF, 0xABE3E3FF, 0x7DD7F0FF, 0x5AC5FFFF},
            {0xE1F8FAFF, 0xC7F1F1FF, 0xABE3E3FF, 0x91EBFFFF},
            {0xD0DAF8FF, 0xBED2F0FF, 0xABC7E3FF, 0x7DD7F0FF},
            {0xBED2F0FF, 0xABC7E3FF, 0xA8B9DCFF, 0x8FC7C7FF},
            {0xABC7E3FF, 0xA8B9DCFF, 0x8FABC7FF, 0x90B0FFFF},
            {0xBCAFC0FF, 0x8FABC7FF, 0x699DC3FF, 0x578FC7FF},
            {0x699DC3FF, 0x578FC7FF, 0x4B7DC8FF, 0x2378DCFF},
            {0x6E8287FF, 0x57738FFF, 0x3B7373FF, 0x326496FF},
            {0x5B5B5BFF, 0x3B5773FF, 0x234146FF, 0x0F377DFF},
            {0x1F1F3BFF, 0x0F192DFF, 0x010101FF, 0x010101FF},
            {0x3C233CFF, 0x1F1F3BFF, 0x0F192DFF, 0x0F0F50FF},
            {0x494949FF, 0x3B3B57FF, 0x234146FF, 0x162C52FF},
            {0x724072FF, 0x494973FF, 0x3B3B57FF, 0x0F377DFF},
            {0x73578FFF, 0x57578FFF, 0x3B5773FF, 0x494973FF},
            {0xAB73ABFF, 0x736EAAFF, 0x57738FFF, 0x57578FFF},
            {0x8F8FC7FF, 0x7676CAFF, 0x4B7DC8FF, 0x326496FF},
            {0xAB8FC7FF, 0x8F8FC7FF, 0x7676CAFF, 0x578FC7FF},
            {0xA8B9DCFF, 0xABABE3FF, 0x8FABC7FF, 0x8F8FC7FF},
            {0xE3E3FFFF, 0xD0DAF8FF, 0xBED2F0FF, 0xABC7E3FF},
            {0xEDEDEDFF, 0xE3E3FFFF, 0xD0DAF8FF, 0xBED2F0FF},
            {0xC78FB9FF, 0xAB8FC7FF, 0x8F8FC7FF, 0x7676CAFF},
            {0xAB57ABFF, 0x8F57C7FF, 0x73578FFF, 0x57578FFF},
            {0x8F578FFF, 0x73578FFF, 0x57578FFF, 0x494973FF},
            {0x724072FF, 0x573B73FF, 0x3B3B57FF, 0x162C52FF},
            {0x4B2837FF, 0x3C233CFF, 0x252525FF, 0x321623FF},
            {0x573B3BFF, 0x463246FF, 0x373737FF, 0x3C233CFF},
            {0x8E5555FF, 0x724072FF, 0x573B73FF, 0x3B3B57FF},
            {0xC35A91FF, 0x8F578FFF, 0x73578FFF, 0x57578FFF},
            {0xC35A91FF, 0xAB57ABFF, 0x8F578FFF, 0x73578FFF},
            {0xC87DA0FF, 0xAB73ABFF, 0x736EAAFF, 0x8F578FFF},
            {0xFFC0CBFF, 0xEBACE1FF, 0xD7A5FFFF, 0xD7A0BEFF},
            {0xFFFFFFFF, 0xFFDCF5FF, 0xF8C6FCFF, 0xE3C7E3FF},
            {0xF8D2DAFF, 0xE3C7E3FF, 0xC9C9C9FF, 0xBEB9FAFF},
            {0xFFC0CBFF, 0xE1B9D2FF, 0xBCAFC0FF, 0xABABE3FF},
            {0xE3ABABFF, 0xD7A0BEFF, 0xC78FB9FF, 0xAB8FC7FF},
            {0xD7A0BEFF, 0xC78FB9FF, 0xAB8FC7FF, 0xAB73ABFF},
            {0xC78F8FFF, 0xC87DA0FF, 0xAB73ABFF, 0x736EAAFF},
            {0xC07872FF, 0xC35A91FF, 0xAB57ABFF, 0x8F578FFF},
            {0x573B3BFF, 0x4B2837FF, 0x3C233CFF, 0x321623FF},
            {0x401811FF, 0x321623FF, 0x280A1EFF, 0x131313FF},
            {0x321623FF, 0x280A1EFF, 0x131313FF, 0x010101FF},
            {0x551414FF, 0x401811FF, 0x321623FF, 0x280A1EFF},
            {0xA5140AFF, 0x621800FF, 0x551414FF, 0x401811FF},
            {0xDA2010FF, 0xA5140AFF, 0x621800FF, 0x7F0000FF},
            {0xFF3C0AFF, 0xDA2010FF, 0xA5140AFF, 0x911437FF},
            {0xF55A32FF, 0xD5524AFF, 0xBF3F3FFF, 0x8E5555FF},
            {0xF55A32FF, 0xFF3C0AFF, 0xDA2010FF, 0xBF3F3FFF},
            {0xFF6262FF, 0xF55A32FF, 0xD5524AFF, 0xBF3F3FFF},
            {0xFF8181FF, 0xFF6262FF, 0xD5524AFF, 0xC35A91FF},
            {0xFFBF81FF, 0xF6BD31FF, 0xBFBF3FFF, 0xD79B0FFF},
            {0xF6BD31FF, 0xFFA53CFF, 0xD79B0FFF, 0xBF7F3FFF},
            {0xFFA53CFF, 0xD79B0FFF, 0xAC9400FF, 0x8F8F57FF},
            {0xFF7F00FF, 0xDA6E0AFF, 0xB45A00FF, 0x73733BFF},
            {0xDA6E0AFF, 0xB45A00FF, 0xA04B05FF, 0x73573BFF},
            {0xB45A00FF, 0xA04B05FF, 0x7F3F00FF, 0x53500AFF},
            {0x7F3F00FF, 0x5F3214FF, 0x3B2D1FFF, 0x252525FF},
            {0x626200FF, 0x53500AFF, 0x414123FF, 0x204608FF},
            {0x7F7F00FF, 0x626200FF, 0x53500AFF, 0x414123FF},
            {0x8F8F57FF, 0x8C805AFF, 0x7E6E60FF, 0x6E6E6EFF},
            {0xD79B0FFF, 0xAC9400FF, 0x7F7F00FF, 0x73733BFF},
            {0xD79B0FFF, 0xB1B10AFF, 0xAC9400FF, 0x6AA805FF},
            {0xFFEA4AFF, 0xE6D55AFF, 0xBFBF3FFF, 0xB5B572FF},
            {0xFFEA4AFF, 0xFFD510FF, 0xF6BD31FF, 0xB1B10AFF},
            {0xFFFFBFFF, 0xFFEA4AFF, 0xE6D55AFF, 0x9BF046FF},
            {0xFFEA4AFF, 0xC8FF41FF, 0x9BF046FF, 0x7DFF73FF},
            {0xC8FF41FF, 0x9BF046FF, 0x4BF05AFF, 0x64C082FF},
            {0xBFBF3FFF, 0x96DC19FF, 0x73C805FF, 0x3FBF3FFF},
            {0x96DC19FF, 0x73C805FF, 0x6AA805FF, 0x3FBF3FFF},
            {0xB1B10AFF, 0x6AA805FF, 0x587D3EFF, 0x149605FF},
            {0x626200FF, 0x3C6E14FF, 0x0C5C0CFF, 0x235037FF},
            {0x5F3214FF, 0x283405FF, 0x191E0FFF, 0x131313FF},
            {0x53500AFF, 0x204608FF, 0x283405FF, 0x1E2D23FF},
            {0x3C6E14FF, 0x0C5C0CFF, 0x123832FF, 0x0C2148FF},
            {0x6AA805FF, 0x149605FF, 0x007F00FF, 0x0F6946FF},
            {0x14E60AFF, 0x0AD70AFF, 0x00C514FF, 0x05B450FF},
            {0x73C805FF, 0x14E60AFF, 0x0AD70AFF, 0x00C514FF},
            {0xAFFFAFFF, 0x7DFF73FF, 0x4BF05AFF, 0x2DEBA8FF},
            {0x7DFF73FF, 0x4BF05AFF, 0x00DE6AFF, 0x06C491FF},
            {0x0AD70AFF, 0x00C514FF, 0x05B450FF, 0x149605FF},
            {0x3FBF3FFF, 0x05B450FF, 0x129880FF, 0x1C8C4EFF},
            {0x578F57FF, 0x1C8C4EFF, 0x0F6946FF, 0x055A5CFF},
            {0x373737FF, 0x123832FF, 0x0F192DFF, 0x0C2148FF},
            {0x578F57FF, 0x129880FF, 0x007F7FFF, 0x004A9CFF},
            {0x3FBFBFFF, 0x06C491FF, 0x129880FF, 0x007F7FFF},
            {0x3FBF3FFF, 0x00DE6AFF, 0x06C491FF, 0x129880FF},
            {0x3CFEA5FF, 0x2DEBA8FF, 0x08DED5FF, 0x06C491FF},
            {0x6AFFCDFF, 0x3CFEA5FF, 0x2DEBA8FF, 0x08DED5FF},
            {0xB4EECAFF, 0x6AFFCDFF, 0x3CFEA5FF, 0x2DEBA8FF},
            {0xABE3E3FF, 0x91EBFFFF, 0x7DD7F0FF, 0x55E6FFFF},
            {0x7DD7F0FF, 0x55E6FFFF, 0x08DED5FF, 0x00BFFFFF},
            {0xABE3E3FF, 0x7DD7F0FF, 0x5AC5FFFF, 0x00BFFFFF},
            {0x2DEBA8FF, 0x08DED5FF, 0x00BFFFFF, 0x109CDEFF},
            {0x578FC7FF, 0x109CDEFF, 0x007FFFFF, 0x186ABDFF},
            {0x0F6946FF, 0x055A5CFF, 0x0F377DFF, 0x162C52FF},
            {0x3B3B57FF, 0x162C52FF, 0x0C2148FF, 0x0F0F50FF},
            {0x3B3B57FF, 0x0F377DFF, 0x0C2148FF, 0x0F0F50FF},
            {0x3F3FBFFF, 0x004A9CFF, 0x0F377DFF, 0x00007FFF},
            {0x57738FFF, 0x326496FF, 0x055A5CFF, 0x004A9CFF},
            {0x3C3CF5FF, 0x0052F6FF, 0x004A9CFF, 0x0010BDFF},
            {0x326496FF, 0x186ABDFF, 0x004A9CFF, 0x0F377DFF},
            {0x4B7DC8FF, 0x2378DCFF, 0x186ABDFF, 0x004A9CFF},
            {0x8F8FC7FF, 0x699DC3FF, 0x578FC7FF, 0x4B7DC8FF},
            {0x90B0FFFF, 0x4AA4FFFF, 0x109CDEFF, 0x007FFFFF},
            {0xABABE3FF, 0x90B0FFFF, 0x4AA4FFFF, 0x109CDEFF},
            {0x7DD7F0FF, 0x5AC5FFFF, 0x4AA4FFFF, 0x00BFFFFF},
            {0xD7C3FAFF, 0xBEB9FAFF, 0xABABE3FF, 0x90B0FFFF},
            {0x4AA4FFFF, 0x00BFFFFF, 0x109CDEFF, 0x007FFFFF},
            {0x2378DCFF, 0x007FFFFF, 0x0052F6FF, 0x004A9CFF},
            {0x578FC7FF, 0x4B7DC8FF, 0x2378DCFF, 0x186ABDFF},
            {0x7676CAFF, 0x786EF0FF, 0x4B7DC8FF, 0x4A5AFFFF},
            {0x786EF0FF, 0x4A5AFFFF, 0x3C3CF5FF, 0x0052F6FF},
            {0x9C41FFFF, 0x6241F6FF, 0x3C3CF5FF, 0x3F3FBFFF},
            {0x6241F6FF, 0x3C3CF5FF, 0x3F3FBFFF, 0x101CDAFF},
            {0x6010D0FF, 0x101CDAFF, 0x0010BDFF, 0x00007FFF},
            {0x101CDAFF, 0x0010BDFF, 0x00007FFF, 0x010101FF},
            {0x5010B0FF, 0x231094FF, 0x00007FFF, 0x010101FF},
            {0x1F1F3BFF, 0x0C2148FF, 0x0F0F50FF, 0x010101FF},
            {0x6010D0FF, 0x5010B0FF, 0x231094FF, 0x00007FFF},
            {0x8C14BEFF, 0x6010D0FF, 0x5010B0FF, 0x231094FF},
            {0xBF3FBFFF, 0x8732D2FF, 0x3F3FBFFF, 0x0F377DFF},
            {0xBD62FFFF, 0x9C41FFFF, 0x6241F6FF, 0x3C3CF5FF},
            {0xBD10C5FF, 0x7F00FFFF, 0x6010D0FF, 0x5010B0FF},
            {0xE673FFFF, 0xBD62FFFF, 0x786EF0FF, 0x9C41FFFF},
            {0xD7A5FFFF, 0xB991FFFF, 0x8181FFFF, 0x786EF0FF},
            {0xEBACE1FF, 0xD7A5FFFF, 0xB991FFFF, 0x8181FFFF},
            {0xE3C7E3FF, 0xD7C3FAFF, 0xBEB9FAFF, 0x90B0FFFF},
            {0xFFDCF5FF, 0xF8C6FCFF, 0xD7C3FAFF, 0xBEB9FAFF},
            {0xFD81FFFF, 0xE673FFFF, 0xBD62FFFF, 0x786EF0FF},
            {0xFF6AC5FF, 0xFF52FFFF, 0xBD62FFFF, 0x9C41FFFF},
            {0xFF50BFFF, 0xDA20E0FF, 0xBD10C5FF, 0x8C14BEFF},
            {0xDA20E0FF, 0xBD29FFFF, 0x8732D2FF, 0x6241F6FF},
            {0xDA20E0FF, 0xBD10C5FF, 0x8C14BEFF, 0x5010B0FF},
            {0xBD10C5FF, 0x8C14BEFF, 0x5010B0FF, 0x231094FF},
            {0x641464FF, 0x5A187BFF, 0x410062FF, 0x320A46FF},
            {0xA01982FF, 0x641464FF, 0x320A46FF, 0x0F0F50FF},
            {0x641464FF, 0x410062FF, 0x320A46FF, 0x0F0F50FF},
            {0x551937FF, 0x320A46FF, 0x0F0F50FF, 0x010101FF},
            {0x911437FF, 0x551937FF, 0x321623FF, 0x1F1F3BFF},
            {0xE61E78FF, 0xA01982FF, 0x641464FF, 0x5A187BFF},
            {0xE61E78FF, 0xC80078FF, 0xA01982FF, 0x7F007FFF},
            {0xFF6AC5FF, 0xFF50BFFF, 0xC35A91FF, 0xBF3FBFFF},
            {0xFF8181FF, 0xFF6AC5FF, 0xFF50BFFF, 0xC35A91FF},
            {0xF5B99BFF, 0xFAA0B9FF, 0xD7A0BEFF, 0xC78FB9FF},
            {0xFF6262FF, 0xFC3A8CFF, 0xE61E78FF, 0xBF3FBFFF},
            {0xFC3A8CFF, 0xE61E78FF, 0xA01982FF, 0x641464FF},
            {0xDA2010FF, 0xBD1039FF, 0x911437FF, 0x551937FF},
            {0xBF3F3FFF, 0x98344DFF, 0x73413CFF, 0x573B3BFF},
            {0xBD1039FF, 0x911437FF, 0x551937FF, 0x320A46FF},
    };

    /**
     * An experiment with the 64-color Flesurrect palette and ramps.
     * <a href="https://i.imgur.com/HV1NDhx.png">View this here</a>.
     */
    public static final byte[][] FLESURRECT_RAMPS = new byte[][]{
            { 0, 0, 0, 0 },
            { 3, 1, 1, 1 },
            { 5, 2, 1, 1 },
            { 5, 3, 1, 1 },
            { 5, 4, 1, 1 },
            { 6, 5, 4, 3 },
            { 7, 6, 5, 4 },
            { 9, 7, 6, 5 },
            { 9, 8, 6, 5 },
            { 9, 9, 8, 7 },
            { 11, 10, 1, 1 },
            { 62, 11, 4, 3 },
            { 13, 12, 18, 10 },
            { 26, 13, 12, 18 },
            { 23, 14, 61, 17 },
            { 16, 15, 60, 54 },
            { 14, 16, 15, 63 },
            { 19, 17, 60, 54 },
            { 12, 18, 3, 1 },
            { 14, 19, 17, 60 },
            { 23, 20, 19, 17 },
            { 24, 21, 15, 63 },
            { 26, 22, 18, 10 },
            { 29, 23, 20, 14 },
            { 27, 24, 21, 15 },
            { 27, 25, 21, 19 },
            { 29, 26, 13, 22 },
            { 29, 27, 25, 24 },
            { 29, 28, 24, 21 },
            { 9, 29, 26, 23 },
            { 26, 30, 22, 31 },
            { 13, 31, 33, 10 },
            { 7, 32, 34, 31 },
            { 35, 33, 3, 2 },
            { 32, 34, 33, 2 },
            { 6, 35, 33, 3 },
            { 37, 36, 43, 47 },
            { 44, 37, 36, 40 },
            { 39, 38, 36, 40 },
            { 9, 39, 44, 38 },
            { 37, 40, 47, 2 },
            { 44, 41, 46, 42 },
            { 41, 42, 45, 47 },
            { 42, 43, 47, 1 },
            { 39, 44, 41, 46 },
            { 48, 45, 47, 1 },
            { 41, 46, 48, 49 },
            { 45, 47, 1, 1 },
            { 46, 48, 45, 47 },
            { 46, 49, 50, 1 },
            { 49, 50, 1, 1 },
            { 56, 51, 1, 1 },
            { 53, 52, 51, 54 },
            { 57, 53, 52, 56 },
            { 56, 54, 1, 1 },
            { 59, 55, 51, 54 },
            { 53, 56, 51, 54 },
            { 8, 57, 53, 59 },
            { 62, 58, 60, 54 },
            { 57, 59, 55, 56 },
            { 58, 60, 54, 1 },
            { 14, 61, 58, 63 },
            { 26, 62, 58, 18 },
            { 61, 63, 60, 54 },
    };

    /**
     * An experiment with the 64-color Flesurrect palette and ramps (this has the RGBA color values).
     * <a href="https://i.imgur.com/HV1NDhx.png">View this here</a>.
     */
    public static final int[][] FLESURRECT_RAMP_VALUES = new int[][]{
            { 0x00000000, 0x00000000, 0x00000000, 0x00000000 },
            { 0x3E3546FF, 0x1F1833FF, 0x1F1833FF, 0x1F1833FF },
            { 0x68717AFF, 0x2B2E42FF, 0x1F1833FF, 0x1F1833FF },
            { 0x68717AFF, 0x3E3546FF, 0x1F1833FF, 0x1F1833FF },
            { 0x68717AFF, 0x414859FF, 0x1F1833FF, 0x1F1833FF },
            { 0x90A1A8FF, 0x68717AFF, 0x414859FF, 0x3E3546FF },
            { 0xB6CBCFFF, 0x90A1A8FF, 0x68717AFF, 0x414859FF },
            { 0xFFFFFFFF, 0xB6CBCFFF, 0x90A1A8FF, 0x68717AFF },
            { 0xFFFFFFFF, 0xD3E5EDFF, 0x90A1A8FF, 0x68717AFF },
            { 0xFFFFFFFF, 0xFFFFFFFF, 0xD3E5EDFF, 0xB6CBCFFF },
            { 0x826481FF, 0x5C3A41FF, 0x1F1833FF, 0x1F1833FF },
            { 0xC27182FF, 0x826481FF, 0x414859FF, 0x3E3546FF },
            { 0xAB947AFF, 0x966C6CFF, 0x8A503EFF, 0x5C3A41FF },
            { 0xE3C896FF, 0xAB947AFF, 0x966C6CFF, 0x8A503EFF },
            { 0xFCBF8AFF, 0xF68181FF, 0xF04F78FF, 0xAE4539FF },
            { 0xFF5A4AFF, 0xF53333FF, 0x7A3045FF, 0x630867FF },
            { 0xF68181FF, 0xFF5A4AFF, 0xF53333FF, 0xC93038FF },
            { 0xCD683DFF, 0xAE4539FF, 0x7A3045FF, 0x630867FF },
            { 0x966C6CFF, 0x8A503EFF, 0x3E3546FF, 0x1F1833FF },
            { 0xF68181FF, 0xCD683DFF, 0xAE4539FF, 0x7A3045FF },
            { 0xFCBF8AFF, 0xFBA458FF, 0xCD683DFF, 0xAE4539FF },
            { 0xFF9E17FF, 0xFB6B1DFF, 0xF53333FF, 0xC93038FF },
            { 0xE3C896FF, 0x9F8562FF, 0x8A503EFF, 0x5C3A41FF },
            { 0xFBFF86FF, 0xFCBF8AFF, 0xFBA458FF, 0xF68181FF },
            { 0xFBE626FF, 0xFF9E17FF, 0xFB6B1DFF, 0xF53333FF },
            { 0xFBE626FF, 0xF0B628FF, 0xFB6B1DFF, 0xCD683DFF },
            { 0xFBFF86FF, 0xE3C896FF, 0xAB947AFF, 0x9F8562FF },
            { 0xFBFF86FF, 0xFBE626FF, 0xF0B628FF, 0xFF9E17FF },
            { 0xFBFF86FF, 0xEDD500FF, 0xFF9E17FF, 0xFB6B1DFF },
            { 0xFFFFFFFF, 0xFBFF86FF, 0xE3C896FF, 0xFCBF8AFF },
            { 0xE3C896FF, 0xB4D645FF, 0x9F8562FF, 0x729446FF },
            { 0xAB947AFF, 0x729446FF, 0x358510FF, 0x5C3A41FF },
            { 0xB6CBCFFF, 0x91DB69FF, 0x51C43FFF, 0x729446FF },
            { 0x4BA14AFF, 0x358510FF, 0x3E3546FF, 0x2B2E42FF },
            { 0x91DB69FF, 0x51C43FFF, 0x358510FF, 0x2B2E42FF },
            { 0x90A1A8FF, 0x4BA14AFF, 0x358510FF, 0x3E3546FF },
            { 0x30E1B9FF, 0x1EBC73FF, 0x216981FF, 0x28306FFF },
            { 0x7FE8F2FF, 0x30E1B9FF, 0x1EBC73FF, 0x039F78FF },
            { 0xB8FDFFFF, 0x7FE0C2FF, 0x1EBC73FF, 0x039F78FF },
            { 0xFFFFFFFF, 0xB8FDFFFF, 0x7FE8F2FF, 0x7FE0C2FF },
            { 0x30E1B9FF, 0x039F78FF, 0x28306FFF, 0x2B2E42FF },
            { 0x7FE8F2FF, 0x63C2C9FF, 0x4D9BE6FF, 0x4F83BFFF },
            { 0x63C2C9FF, 0x4F83BFFF, 0x3B509FFF, 0x28306FFF },
            { 0x4F83BFFF, 0x216981FF, 0x28306FFF, 0x1F1833FF },
            { 0xB8FDFFFF, 0x7FE8F2FF, 0x63C2C9FF, 0x4D9BE6FF },
            { 0x4870CFFF, 0x3B509FFF, 0x28306FFF, 0x1F1833FF },
            { 0x63C2C9FF, 0x4D9BE6FF, 0x4870CFFF, 0x4D50D4FF },
            { 0x3B509FFF, 0x28306FFF, 0x1F1833FF, 0x1F1833FF },
            { 0x4D9BE6FF, 0x4870CFFF, 0x3B509FFF, 0x28306FFF },
            { 0x4D9BE6FF, 0x4D50D4FF, 0x180FCFFF, 0x1F1833FF },
            { 0x4D50D4FF, 0x180FCFFF, 0x1F1833FF, 0x1F1833FF },
            { 0x8032BCFF, 0x53207DFF, 0x1F1833FF, 0x1F1833FF },
            { 0xA884F3FF, 0x8657CCFF, 0x53207DFF, 0x630867FF },
            { 0xE4A8FAFF, 0xA884F3FF, 0x8657CCFF, 0x8032BCFF },
            { 0x8032BCFF, 0x630867FF, 0x1F1833FF, 0x1F1833FF },
            { 0xF34FE9FF, 0xA03EB2FF, 0x53207DFF, 0x630867FF },
            { 0xA884F3FF, 0x8032BCFF, 0x53207DFF, 0x630867FF },
            { 0xD3E5EDFF, 0xE4A8FAFF, 0xA884F3FF, 0xF34FE9FF },
            { 0xC27182FF, 0xB53D86FF, 0x7A3045FF, 0x630867FF },
            { 0xE4A8FAFF, 0xF34FE9FF, 0xA03EB2FF, 0x8032BCFF },
            { 0xB53D86FF, 0x7A3045FF, 0x630867FF, 0x1F1833FF },
            { 0xF68181FF, 0xF04F78FF, 0xB53D86FF, 0xC93038FF },
            { 0xE3C896FF, 0xC27182FF, 0xB53D86FF, 0x8A503EFF },
            { 0xF04F78FF, 0xC93038FF, 0x7A3045FF, 0x630867FF },
    };
}
