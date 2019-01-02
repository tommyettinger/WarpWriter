package warpwriter.view;

import com.badlogic.gdx.math.MathUtils;
import warpwriter.Coloring;

/**
 * Twilight provides default implementations of bright, twilight, dim and dark which refer to the light method, and a default implementation of the light method which refers to the other four. Extension classes can overload just the light method or alternatively can overload the other four. Either way will work.
 * <p>
 * Also contained here are various extensions as member classes.
 *
 * @author Ben McLean
 */
public abstract class Twilight implements ITwilight {
    /**
     * Refers to the light method to get the answer.
     */
    @Override
    public int bright(byte voxel) {
        return light(3, voxel);
    }

    /**
     * Refers to the light method to get the answer.
     */
    @Override
    public int twilight(byte voxel) {
        return light(2, voxel);
    }

    /**
     * Refers to the light method to get the answer.
     */
    @Override
    public int dim(byte voxel) {
        return light(1, voxel);
    }

    /**
     * Refers to the light method to get the answer.
     */
    @Override
    public int dark(byte voxel) {
        return light(0, voxel);
    }

    /**
     * Refers to the dark, dim, twilight and bright methods to get the answer.
     *
     * @param brightness 0 for dark, 1 for dim, 2 for twilight and 3 for bright. Negative numbers are expected to normally be interpreted as black and numbers higher than 3 as white.
     * @param voxel      The color index of a voxel
     * @return An rgba8888 color
     */
    @Override
    public int light(int brightness, byte voxel) {
        if (voxel == 0) return 0; // 0 is equivalent to Color.rgba8888(Color.CLEAR)
        switch (brightness) {
            case 0:
                return dark(voxel);
            case 1:
                return dim(voxel);
            case 2:
                return twilight(voxel);
            case 3:
                return bright(voxel);
        }
        return brightness > 3
                ? 0xffffffff //rgba8888 value of Color.WHITE
                : 0xff;      //rgba8888 value of Color.BLACK
    }

    /**
     * Renders arbitrarily brighter or darker using the colors available in another Twilight.
     *
     * @author Ben McLean
     */
    public static class OffsetTwilight extends Twilight {
        public OffsetTwilight(ITwilight twilight) {
            super();
            set(twilight);
        }

        protected int offset = 0;

        public int offset() {
            return offset;
        }

        public OffsetTwilight set(int offset) {
            this.offset = offset;
            return this;
        }

        public OffsetTwilight add(int offset) {
            this.offset += offset;
            return this;
        }

        protected ITwilight twilight;

        public ITwilight twilight() {
            return twilight;
        }

        public OffsetTwilight set(ITwilight twilight) {
            this.twilight = twilight;
            return this;
        }

        @Override
        public int light(int brightness, byte voxel) {
            return twilight.light(brightness + offset, voxel);
        }
    }

    public static final ITwilight RinsedTwilight = new Twilight() {
        protected int[] palette = Coloring.RINSED;

        @Override
        public int bright(byte voxel) {
            return palette[(voxel & 248) + Math.max((voxel & 7) - 1, 0)];
        }

        @Override
        public int twilight(byte voxel) {
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

    public static final ITwilight AuroraTwilight = new Twilight() {
        @Override
        public int light(int brightness, byte voxel) {
            return RAMP_VALUES[voxel & 255][
                    brightness <= 0
                            ? 3
                            : brightness >= 3
                            ? 0
                            : 3 - brightness
                    ];
        }
    };

    public static final ITwilight AuroraWarmthTwilight = new Twilight() {
        @Override
        public int dark(byte voxel) {
            return WARMTH_RAMP_VALUES[voxel & 255][3];
        }

        @Override
        public int dim(byte voxel) {
            return WARMTH_RAMP_VALUES[voxel & 255][2];
        }

        @Override
        public int twilight(byte voxel) {
            return WARMTH_RAMP_VALUES[voxel & 255][1];
        }

        @Override
        public int bright(byte voxel) {
            return WARMTH_RAMP_VALUES[voxel & 255][0];
        }
    };

    public static ITwilight arbitraryTwilight(final int[] rgbaPalette) {
        return new Twilight() {
            private int[][] RAMP_VALUES = new int[256][4];

            {
                for (int i = 1; i < 256 && i < rgbaPalette.length; i++) {
                    int color = RAMP_VALUES[i][1] = rgbaPalette[i],
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
                    RAMP_VALUES[i][2] =
                            MathUtils.clamp(r, 0, 255) << 24 |
                                    MathUtils.clamp(g, 0, 255) << 16 |
                                    MathUtils.clamp(b, 0, 255) << 8 | 0xFF;
                    chromO = (co * 3) >> 2;
                    chromG = (cg * (256 - yBright) * 3) >> 9;
                    t = yBright - (chromG >> 1);
                    g = chromG + t;
                    b = t - (chromO >> 1);
                    r = b + chromO;
                    RAMP_VALUES[i][0] =
                            MathUtils.clamp(r, 0, 255) << 24 |
                                    MathUtils.clamp(g, 0, 255) << 16 |
                                    MathUtils.clamp(b, 0, 255) << 8 | 0xFF;
                    chromO = (co * 13) >> 4;
                    chromG = (cg * (256 - yDark) * 13) >> 11;
                    t = yDark - (chromG >> 1);
                    g = chromG + t;
                    b = t - (chromO >> 1);
                    r = b + chromO;
                    RAMP_VALUES[i][3] =
                            MathUtils.clamp(r, 0, 255) << 24 |
                                    MathUtils.clamp(g, 0, 255) << 16 |
                                    MathUtils.clamp(b, 0, 255) << 8 | 0xFF;
                }
            }

            @Override
            public int light(int brightness, byte voxel) {
                return RAMP_VALUES[voxel & 255][
                        brightness <= 0
                                ? 3
                                : brightness >= 3
                                ? 0
                                : 3 - brightness
                        ];
            }
        };
    }

    /**
     * Bytes that correspond to palette indices to use when shading an Aurora-palette model.
     * The color in index 1 of each 4-element sub-array is the "twilight" color and the one that will match the voxel
     * color used in a model. The color at index 0 is "bright", index 2 is "dim", and index 3 is "dark". Normal usage
     * with a renderer will use {@link #RAMP_VALUES}; this array would be used to figure out what indices are related to
     * another color for the purpose of procedurally using similar colors with different lightness.
     * To visualize this, <a href="https://i.imgur.com/dX0tgTF.png">use this image</a>, with the first 32 items in RAMPS
     * corresponding to the first column from top to bottom, the next 32 items in the second column, etc.
     */
    public static final byte[][] RAMPS = new byte[][]{
            { 0, 0, 0, 0 },
            { 117, 1, 1, 1 },
            { -124, 2, 1, 1 },
            { -123, 3, 1, 1 },
            { 6, 4, 117, 2 },
            { 7, 5, -124, 3 },
            { 8, 6, -123, 4 },
            { 9, 7, 5, -123 },
            { 10, 8, 6, 5 },
            { 47, 9, 7, 6 },
            { 12, 10, 8, 7 },
            { -116, 11, 9, 8 },
            { 14, 12, 10, 9 },
            { 15, 13, 47, 10 },
            { 15, 14, -116, 12 },
            { 15, 15, 13, -116 },
            { -65, 16, -55, -53 },
            { -44, 17, -65, -66 },
            { -59, 18, -57, -42 },
            { 15, 19, 108, 111 },
            { -45, 20, -38, -37 },
            { -36, 21, -34, 23 },
            { -37, 22, -31, -33 },
            { -33, 23, 117, 1 },
            { -53, 24, 1, 1 },
            { -10, 25, -13, -12 },
            { -20, 26, -17, -16 },
            { -20, 27, -17, -16 },
            { -22, 28, -20, 26 },
            { -117, 29, -6, -114 },
            { -6, 30, -100, -103 },
            { -102, 31, -105, 33 },
            { -100, 32, -3, -1 },
            { -105, 33, -108, 1 },
            { 62, 34, -108, 1 },
            { 32, 35, -106, -107 },
            { 55, 36, -103, -94 },
            { -98, 37, -95, -94 },
            { 59, 38, 56, 55 },
            { 15, 39, 79, 78 },
            { -85, 40, -86, -88 },
            { -87, 41, -88, -89 },
            { -89, 42, -91, -92 },
            { -70, 43, -77, 87 },
            { -71, 44, -68, -75 },
            { -71, 45, -74, -70 },
            { 97, 46, 96, 95 },
            { -116, 47, 9, 8 },
            { 68, 48, 54, 64 },
            { 11, 49, 8, 7 },
            { 10, 50, 7, 101 },
            { 10, 51, 121, 116 },
            { 9, 52, 5, 61 },
            { 65, 53, 71, 62 },
            { 56, 54, 63, -2 },
            { 48, 55, 53, 63 },
            { 58, 56, 54, 53 },
            { 59, 57, 55, 54 },
            { 29, 58, 56, 55 },
            { 60, 59, 48, 65 },
            { 15, 60, -115, 12 },
            { -122, 61, -109, -108 },
            { 63, 62, -110, -11 },
            { 64, 63, 62, 61 },
            { -112, 64, 63, 62 },
            { -114, 65, 64, 53 },
            { 29, 66, -113, 65 },
            { 15, 67, -114, -113 },
            { 67, 68, 48, 49 },
            { 57, 69, 54, 64 },
            { 65, 70, 71, 62 },
            { 52, 71, 61, 73 },
            { 5, 72, -108, 2 },
            { 90, 73, 87, 2 },
            { 75, 74, 86, 73 },
            { 49, 75, 74, 71 },
            { 78, 76, -90, 74 },
            { 79, 77, 75, -90 },
            { 80, 78, 49, 76 },
            { 14, 79, 48, 49 },
            { 15, 80, 12, 11 },
            { 80, 81, 82, 95 },
            { 81, 82, 93, 84 },
            { 82, 83, 84, 85 },
            { 93, 84, 90, 86 },
            { 93, 85, 89, 86 },
            { 6, 86, 103, 3 },
            { 4, 87, 1, 1 },
            { 90, 88, 103, 117 },
            { 101, 89, -67, 103 },
            { 51, 90, 4, 103 },
            { 92, 91, 88, -67 },
            { 93, 92, 91, 89 },
            { 95, 93, 51, 92 },
            { 107, 94, 92, -68 },
            { 99, 95, 93, 51 },
            { 98, 96, 100, 93 },
            { 15, 97, 110, 111 },
            { 97, 98, 96, 95 },
            { 109, 99, 107, 113 },
            { 96, 100, 8, 51 },
            { 51, 101, 89, 88 },
            { -68, 102, -67, 117 },
            { 5, 103, 1, 1 },
            { 116, 104, -32, 117 },
            { 51, 105, -55, 104 },
            { 107, 106, 115, 105 },
            { 108, 107, 106, 50 },
            { 19, 108, 107, 113 },
            { 15, 109, 111, 112 },
            { 97, 110, 112, 125 },
            { 126, 111, 113, 124 },
            { 110, 112, 124, 123 },
            { 111, 113, 50, 122 },
            { -45, 114, -51, -52 },
            { 50, 115, 116, 120 },
            { 115, 116, 119, 104 },
            { -54, 117, 1, 1 },
            { 119, 118, 1, 1 },
            { 121, 119, 118, 117 },
            { 115, 120, -54, 118 },
            { 122, 121, -125, 119 },
            { 124, 122, 121, 120 },
            { 20, 123, 121, 22 },
            { 125, 124, 122, -126 },
            { 110, 125, -128, 124 },
            { 15, 126, -43, 112 },
            { 15, 127, -43, 112 },
            { 125, -128, -119, -127 },
            { -26, -127, -29, -16 },
            { 123, -126, -125, -15 },
            { 121, -125, -13, -12 },
            { 5, -124, -108, 1 },
            { -125, -123, 118, -108 },
            { -121, -122, -14, -124 },
            { -119, -121, -122, -125 },
            { -113, -120, -122, -15 },
            { -128, -119, -121, -126 },
            { -22, -118, -113, -119 },
            { 15, -117, -115, -114 },
            { 15, -116, 47, -113 },
            { -22, -115, -113, -112 },
            { -116, -114, -112, -119 },
            { -118, -113, -119, -120 },
            { -114, -112, -111, -121 },
            { -112, -111, -121, -10 },
            { 5, -110, -108, 1 },
            { -123, -109, 1, 1 },
            { -124, -108, 1, 1 },
            { -110, -107, 1, 1 },
            { 35, -106, 1, 1 },
            { -3, -105, 33, -108 },
            { -102, -104, -105, 33 },
            { -100, -103, -3, -1 },
            { -101, -102, -104, -105 },
            { -100, -101, -102, -104 },
            { 30, -100, -103, 32 },
            { -85, -99, -97, -96 },
            { 38, -98, -97, -96 },
            { -98, -97, -96, -95 },
            { -97, -96, -95, -94 },
            { -96, -95, 35, -106 },
            { -96, -94, -105, -106 },
            { 62, -93, -107, -108 },
            { 71, -92, -78, 87 },
            { 74, -91, -78, -107 },
            { 49, -90, 71, 86 },
            { 41, -89, 42, -91 },
            { 41, -88, 42, -91 },
            { -85, -87, 41, -88 },
            { -85, -86, -97, -89 },
            { 39, -85, -99, 41 },
            { 39, -84, -83, -82 },
            { -84, -83, 83, -81 },
            { -83, -82, -81, -80 },
            { -82, -81, 42, -91 },
            { 83, -80, 42, -91 },
            { 85, -79, -76, -77 },
            { 86, -78, 2, 1 },
            { 88, -77, 87, 2 },
            { 89, -76, 87, 1 },
            { -70, -75, -76, -77 },
            { -71, -74, -75, 43 },
            { -71, -73, -70, -75 },
            { 46, -72, -71, 44 },
            { -72, -71, 44, -70 },
            { -73, -70, -75, 43 },
            { -64, -69, -68, 102 },
            { -69, -68, 102, -76 },
            { 116, -67, 2, 1 },
            { -65, -66, -55, -32 },
            { 17, -65, -66, 16 },
            { -63, -64, -69, -68 },
            { -61, -63, -65, -66 },
            { -61, -62, -65, -69 },
            { -60, -61, -62, -63 },
            { 19, -60, -44, 17 },
            { -60, -59, -57, -56 },
            { 19, -58, 17, -47 },
            { 18, -57, -56, 16 },
            { -46, -56, -41, -49 },
            { 16, -55, -54, -32 },
            { 116, -54, 1, 1 },
            { 22, -53, 23, 24 },
            { -49, -52, 23, 117 },
            { -40, -51, -52, -53 },
            { -41, -50, -35, -34 },
            { -46, -49, -52, -53 },
            { -46, -48, -52, -34 },
            { 107, -47, -40, 115 },
            { -59, -46, -56, -48 },
            { -60, -45, 20, -39 },
            { -60, -44, -46, -56 },
            { 127, -43, 124, 123 },
            { 18, -42, -56, -41 },
            { -42, -41, -50, -35 },
            { 20, -40, -49, 22 },
            { -25, -39, -37, 22 },
            { 20, -38, -35, -34 },
            { -39, -37, 22, -35 },
            { 20, -36, -35, -34 },
            { -36, -35, 23, 117 },
            { -36, -34, 23, 117 },
            { -31, -33, 23, 117 },
            { -53, -32, 1, 1 },
            { -29, -31, -33, 23 },
            { -29, -30, -33, 23 },
            { -28, -29, -30, -31 },
            { -26, -28, -29, -27 },
            { -18, -27, -31, -33 },
            { -21, -26, -28, -29 },
            { -23, -25, -26, -39 },
            { -22, -24, -26, -127 },
            { 15, -23, 125, -128 },
            { 15, -22, -118, -114 },
            { -22, -21, -26, -18 },
            { 28, -20, -19, -17 },
            { -20, -19, 25, -13 },
            { -26, -18, -27, -16 },
            { -19, -17, 25, -13 },
            { -18, -16, -31, 25 },
            { -16, -15, -13, -12 },
            { -122, -14, -12, -108 },
            { -15, -13, -108, 1 },
            { -14, -12, 1, 1 },
            { 62, -11, -108, 1 },
            { 26, -10, 25, -13 },
            { -4, -9, -1, 33 },
            { 28, -8, -5, -4 },
            { 28, -7, -111, -4 },
            { 29, -6, -7, -112 },
            { -8, -5, -4, -9 },
            { -5, -4, -9, -3 },
            { -4, -3, 33, -108 },
            { 53, -2, -1, -11 },
            { -2, -1, 34, 33 },
    };


    /**
     * Color values as RGBA8888 ints to use when shading an Aurora-palette model.
     * The color in index 1 of each 4-element sub-array is the "twilight" color and the one that will match the voxel
     * color used in a model. The color at index 0 is "bright", index 2 is "dim", and index 3 is "dark".
     * To visualize this, <a href="https://i.imgur.com/dX0tgTF.png">use this image</a>, with the first 32 items in RAMPS
     * corresponding to the first column from top to bottom, the next 32 items in the second column, etc.
     */
    public static final     int[][] RAMP_VALUES = new int[][]{
            { 0x00000000, 0x00000000, 0x00000000, 0x00000000 },
            { 0x0F192DFF, 0x010101FF, 0x010101FF, 0x010101FF },
            { 0x3C233CFF, 0x131313FF, 0x010101FF, 0x010101FF },
            { 0x463246FF, 0x252525FF, 0x010101FF, 0x010101FF },
            { 0x5B5B5BFF, 0x373737FF, 0x0F192DFF, 0x131313FF },
            { 0x6E6E6EFF, 0x494949FF, 0x3C233CFF, 0x252525FF },
            { 0x808080FF, 0x5B5B5BFF, 0x463246FF, 0x373737FF },
            { 0x929292FF, 0x6E6E6EFF, 0x494949FF, 0x463246FF },
            { 0xA4A4A4FF, 0x808080FF, 0x5B5B5BFF, 0x494949FF },
            { 0xBCAFC0FF, 0x929292FF, 0x6E6E6EFF, 0x5B5B5BFF },
            { 0xC9C9C9FF, 0xA4A4A4FF, 0x808080FF, 0x6E6E6EFF },
            { 0xE3C7E3FF, 0xB6B6B6FF, 0x929292FF, 0x808080FF },
            { 0xEDEDEDFF, 0xC9C9C9FF, 0xA4A4A4FF, 0x929292FF },
            { 0xFFFFFFFF, 0xDBDBDBFF, 0xBCAFC0FF, 0xA4A4A4FF },
            { 0xFFFFFFFF, 0xEDEDEDFF, 0xE3C7E3FF, 0xC9C9C9FF },
            { 0xFFFFFFFF, 0xFFFFFFFF, 0xDBDBDBFF, 0xE3C7E3FF },
            { 0x06C491FF, 0x007F7FFF, 0x055A5CFF, 0x0F377DFF },
            { 0x5AC5FFFF, 0x3FBFBFFF, 0x06C491FF, 0x129880FF },
            { 0x55E6FFFF, 0x00FFFFFF, 0x08DED5FF, 0x00BFFFFF },
            { 0xFFFFFFFF, 0xBFFFFFFF, 0xABE3E3FF, 0xABC7E3FF },
            { 0x90B0FFFF, 0x8181FFFF, 0x4A5AFFFF, 0x6241F6FF },
            { 0x3C3CF5FF, 0x0000FFFF, 0x0010BDFF, 0x00007FFF },
            { 0x6241F6FF, 0x3F3FBFFF, 0x5010B0FF, 0x231094FF },
            { 0x231094FF, 0x00007FFF, 0x0F192DFF, 0x010101FF },
            { 0x0F377DFF, 0x0F0F50FF, 0x010101FF, 0x010101FF },
            { 0xA01982FF, 0x7F007FFF, 0x410062FF, 0x320A46FF },
            { 0xFF52FFFF, 0xBF3FBFFF, 0xBD10C5FF, 0x8C14BEFF },
            { 0xFF52FFFF, 0xF500F5FF, 0xBD10C5FF, 0x8C14BEFF },
            { 0xF8C6FCFF, 0xFD81FFFF, 0xFF52FFFF, 0xBF3FBFFF },
            { 0xFFDCF5FF, 0xFFC0CBFF, 0xFAA0B9FF, 0xD7A0BEFF },
            { 0xFAA0B9FF, 0xFF8181FF, 0xFF6262FF, 0xD5524AFF },
            { 0xFF3C0AFF, 0xFF0000FF, 0xA5140AFF, 0x7F0000FF },
            { 0xFF6262FF, 0xBF3F3FFF, 0xBD1039FF, 0x911437FF },
            { 0xA5140AFF, 0x7F0000FF, 0x280A1EFF, 0x010101FF },
            { 0x73413CFF, 0x551414FF, 0x280A1EFF, 0x010101FF },
            { 0xBF3F3FFF, 0x7F3F00FF, 0x621800FF, 0x401811FF },
            { 0xD08A74FF, 0xBF7F3FFF, 0xD5524AFF, 0xA04B05FF },
            { 0xFFA53CFF, 0xFF7F00FF, 0xB45A00FF, 0xA04B05FF },
            { 0xF6C8AFFF, 0xFFBF81FF, 0xE19B7DFF, 0xD08A74FF },
            { 0xFFFFFFFF, 0xFFFFBFFF, 0xDADAABFF, 0xC7C78FFF },
            { 0xFFEA4AFF, 0xFFFF00FF, 0xFFD510FF, 0xB1B10AFF },
            { 0xE6D55AFF, 0xBFBF3FFF, 0xB1B10AFF, 0xAC9400FF },
            { 0xAC9400FF, 0x7F7F00FF, 0x626200FF, 0x53500AFF },
            { 0x00C514FF, 0x007F00FF, 0x204608FF, 0x191E0FFF },
            { 0x4BF05AFF, 0x3FBF3FFF, 0x1C8C4EFF, 0x149605FF },
            { 0x4BF05AFF, 0x00FF00FF, 0x0AD70AFF, 0x00C514FF },
            { 0xE1F8FAFF, 0xAFFFAFFF, 0xA2D8A2FF, 0x8FC78FFF },
            { 0xE3C7E3FF, 0xBCAFC0FF, 0x929292FF, 0x808080FF },
            { 0xE3C7ABFF, 0xCBAA89FF, 0xC07872FF, 0xAB7373FF },
            { 0xB6B6B6FF, 0xA6A090FF, 0x808080FF, 0x6E6E6EFF },
            { 0xA4A4A4FF, 0x7E9494FF, 0x6E6E6EFF, 0x507D5FFF },
            { 0xA4A4A4FF, 0x6E8287FF, 0x57578FFF, 0x3B5773FF },
            { 0x929292FF, 0x7E6E60FF, 0x494949FF, 0x573B3BFF },
            { 0xC78F8FFF, 0xA0695FFF, 0x73573BFF, 0x73413CFF },
            { 0xE19B7DFF, 0xC07872FF, 0x8E5555FF, 0x98344DFF },
            { 0xCBAA89FF, 0xD08A74FF, 0xA0695FFF, 0x8E5555FF },
            { 0xF5B99BFF, 0xE19B7DFF, 0xC07872FF, 0xA0695FFF },
            { 0xF6C8AFFF, 0xEBAA8CFF, 0xD08A74FF, 0xC07872FF },
            { 0xFFC0CBFF, 0xF5B99BFF, 0xE19B7DFF, 0xD08A74FF },
            { 0xF5E1D2FF, 0xF6C8AFFF, 0xCBAA89FF, 0xC78F8FFF },
            { 0xFFFFFFFF, 0xF5E1D2FF, 0xE1B9D2FF, 0xC9C9C9FF },
            { 0x724072FF, 0x573B3BFF, 0x321623FF, 0x280A1EFF },
            { 0x8E5555FF, 0x73413CFF, 0x4B2837FF, 0x551937FF },
            { 0xAB7373FF, 0x8E5555FF, 0x73413CFF, 0x573B3BFF },
            { 0xC87DA0FF, 0xAB7373FF, 0x8E5555FF, 0x73413CFF },
            { 0xD7A0BEFF, 0xC78F8FFF, 0xAB7373FF, 0xA0695FFF },
            { 0xFFC0CBFF, 0xE3ABABFF, 0xC78FB9FF, 0xC78F8FFF },
            { 0xFFFFFFFF, 0xF8D2DAFF, 0xD7A0BEFF, 0xC78FB9FF },
            { 0xF8D2DAFF, 0xE3C7ABFF, 0xCBAA89FF, 0xA6A090FF },
            { 0xEBAA8CFF, 0xC49E73FF, 0xC07872FF, 0xAB7373FF },
            { 0xC78F8FFF, 0x8F7357FF, 0x73573BFF, 0x73413CFF },
            { 0x7E6E60FF, 0x73573BFF, 0x573B3BFF, 0x414123FF },
            { 0x494949FF, 0x3B2D1FFF, 0x280A1EFF, 0x131313FF },
            { 0x506450FF, 0x414123FF, 0x191E0FFF, 0x131313FF },
            { 0x8F8F57FF, 0x73733BFF, 0x465032FF, 0x414123FF },
            { 0xA6A090FF, 0x8F8F57FF, 0x73733BFF, 0x73573BFF },
            { 0xC7C78FFF, 0xA2A255FF, 0x8C805AFF, 0x73733BFF },
            { 0xDADAABFF, 0xB5B572FF, 0x8F8F57FF, 0x8C805AFF },
            { 0xEDEDC7FF, 0xC7C78FFF, 0xA6A090FF, 0xA2A255FF },
            { 0xEDEDEDFF, 0xDADAABFF, 0xCBAA89FF, 0xA6A090FF },
            { 0xFFFFFFFF, 0xEDEDC7FF, 0xC9C9C9FF, 0xB6B6B6FF },
            { 0xEDEDC7FF, 0xC7E3ABFF, 0xABC78FFF, 0x8FC78FFF },
            { 0xC7E3ABFF, 0xABC78FFF, 0x73AB73FF, 0x738F57FF },
            { 0xABC78FFF, 0x8EBE55FF, 0x738F57FF, 0x587D3EFF },
            { 0x73AB73FF, 0x738F57FF, 0x506450FF, 0x465032FF },
            { 0x73AB73FF, 0x587D3EFF, 0x3B573BFF, 0x465032FF },
            { 0x5B5B5BFF, 0x465032FF, 0x1E2D23FF, 0x252525FF },
            { 0x373737FF, 0x191E0FFF, 0x010101FF, 0x010101FF },
            { 0x506450FF, 0x235037FF, 0x1E2D23FF, 0x0F192DFF },
            { 0x507D5FFF, 0x3B573BFF, 0x123832FF, 0x1E2D23FF },
            { 0x6E8287FF, 0x506450FF, 0x373737FF, 0x1E2D23FF },
            { 0x578F57FF, 0x3B7349FF, 0x235037FF, 0x123832FF },
            { 0x73AB73FF, 0x578F57FF, 0x3B7349FF, 0x3B573BFF },
            { 0x8FC78FFF, 0x73AB73FF, 0x6E8287FF, 0x578F57FF },
            { 0x8FC7C7FF, 0x64C082FF, 0x578F57FF, 0x1C8C4EFF },
            { 0xABE3C5FF, 0x8FC78FFF, 0x73AB73FF, 0x6E8287FF },
            { 0xB4EECAFF, 0xA2D8A2FF, 0x87B48EFF, 0x73AB73FF },
            { 0xFFFFFFFF, 0xE1F8FAFF, 0xBED2F0FF, 0xABC7E3FF },
            { 0xE1F8FAFF, 0xB4EECAFF, 0xA2D8A2FF, 0x8FC78FFF },
            { 0xC7F1F1FF, 0xABE3C5FF, 0x8FC7C7FF, 0x8FABC7FF },
            { 0xA2D8A2FF, 0x87B48EFF, 0x808080FF, 0x6E8287FF },
            { 0x6E8287FF, 0x507D5FFF, 0x3B573BFF, 0x235037FF },
            { 0x1C8C4EFF, 0x0F6946FF, 0x123832FF, 0x0F192DFF },
            { 0x494949FF, 0x1E2D23FF, 0x010101FF, 0x010101FF },
            { 0x3B5773FF, 0x234146FF, 0x0C2148FF, 0x0F192DFF },
            { 0x6E8287FF, 0x3B7373FF, 0x055A5CFF, 0x234146FF },
            { 0x8FC7C7FF, 0x64ABABFF, 0x57738FFF, 0x3B7373FF },
            { 0xABE3E3FF, 0x8FC7C7FF, 0x64ABABFF, 0x7E9494FF },
            { 0xBFFFFFFF, 0xABE3E3FF, 0x8FC7C7FF, 0x8FABC7FF },
            { 0xFFFFFFFF, 0xC7F1F1FF, 0xABC7E3FF, 0xA8B9DCFF },
            { 0xE1F8FAFF, 0xBED2F0FF, 0xA8B9DCFF, 0xABABE3FF },
            { 0xD0DAF8FF, 0xABC7E3FF, 0x8FABC7FF, 0x8F8FC7FF },
            { 0xBED2F0FF, 0xA8B9DCFF, 0x8F8FC7FF, 0x7676CAFF },
            { 0xABC7E3FF, 0x8FABC7FF, 0x7E9494FF, 0x736EAAFF },
            { 0x90B0FFFF, 0x578FC7FF, 0x326496FF, 0x004A9CFF },
            { 0x7E9494FF, 0x57738FFF, 0x3B5773FF, 0x494973FF },
            { 0x57738FFF, 0x3B5773FF, 0x3B3B57FF, 0x234146FF },
            { 0x162C52FF, 0x0F192DFF, 0x010101FF, 0x010101FF },
            { 0x3B3B57FF, 0x1F1F3BFF, 0x010101FF, 0x010101FF },
            { 0x57578FFF, 0x3B3B57FF, 0x1F1F3BFF, 0x0F192DFF },
            { 0x57738FFF, 0x494973FF, 0x162C52FF, 0x1F1F3BFF },
            { 0x736EAAFF, 0x57578FFF, 0x573B73FF, 0x3B3B57FF },
            { 0x8F8FC7FF, 0x736EAAFF, 0x57578FFF, 0x494973FF },
            { 0x8181FFFF, 0x7676CAFF, 0x57578FFF, 0x3F3FBFFF },
            { 0xABABE3FF, 0x8F8FC7FF, 0x736EAAFF, 0x73578FFF },
            { 0xBED2F0FF, 0xABABE3FF, 0xAB8FC7FF, 0x8F8FC7FF },
            { 0xFFFFFFFF, 0xD0DAF8FF, 0xBEB9FAFF, 0xA8B9DCFF },
            { 0xFFFFFFFF, 0xE3E3FFFF, 0xBEB9FAFF, 0xA8B9DCFF },
            { 0xABABE3FF, 0xAB8FC7FF, 0xAB73ABFF, 0x8F57C7FF },
            { 0xBD62FFFF, 0x8F57C7FF, 0x8732D2FF, 0x8C14BEFF },
            { 0x7676CAFF, 0x73578FFF, 0x573B73FF, 0x5A187BFF },
            { 0x57578FFF, 0x573B73FF, 0x410062FF, 0x320A46FF },
            { 0x494949FF, 0x3C233CFF, 0x280A1EFF, 0x010101FF },
            { 0x573B73FF, 0x463246FF, 0x1F1F3BFF, 0x280A1EFF },
            { 0x8F578FFF, 0x724072FF, 0x641464FF, 0x3C233CFF },
            { 0xAB73ABFF, 0x8F578FFF, 0x724072FF, 0x573B73FF },
            { 0xC78FB9FF, 0xAB57ABFF, 0x724072FF, 0x5A187BFF },
            { 0xAB8FC7FF, 0xAB73ABFF, 0x8F578FFF, 0x73578FFF },
            { 0xF8C6FCFF, 0xEBACE1FF, 0xC78FB9FF, 0xAB73ABFF },
            { 0xFFFFFFFF, 0xFFDCF5FF, 0xE1B9D2FF, 0xD7A0BEFF },
            { 0xFFFFFFFF, 0xE3C7E3FF, 0xBCAFC0FF, 0xC78FB9FF },
            { 0xF8C6FCFF, 0xE1B9D2FF, 0xC78FB9FF, 0xC87DA0FF },
            { 0xE3C7E3FF, 0xD7A0BEFF, 0xC87DA0FF, 0xAB73ABFF },
            { 0xEBACE1FF, 0xC78FB9FF, 0xAB73ABFF, 0xAB57ABFF },
            { 0xD7A0BEFF, 0xC87DA0FF, 0xC35A91FF, 0x8F578FFF },
            { 0xC87DA0FF, 0xC35A91FF, 0x8F578FFF, 0xA01982FF },
            { 0x494949FF, 0x4B2837FF, 0x280A1EFF, 0x010101FF },
            { 0x463246FF, 0x321623FF, 0x010101FF, 0x010101FF },
            { 0x3C233CFF, 0x280A1EFF, 0x010101FF, 0x010101FF },
            { 0x4B2837FF, 0x401811FF, 0x010101FF, 0x010101FF },
            { 0x7F3F00FF, 0x621800FF, 0x010101FF, 0x010101FF },
            { 0xBD1039FF, 0xA5140AFF, 0x7F0000FF, 0x280A1EFF },
            { 0xFF3C0AFF, 0xDA2010FF, 0xA5140AFF, 0x7F0000FF },
            { 0xFF6262FF, 0xD5524AFF, 0xBD1039FF, 0x911437FF },
            { 0xF55A32FF, 0xFF3C0AFF, 0xDA2010FF, 0xA5140AFF },
            { 0xFF6262FF, 0xF55A32FF, 0xFF3C0AFF, 0xDA2010FF },
            { 0xFF8181FF, 0xFF6262FF, 0xD5524AFF, 0xBF3F3FFF },
            { 0xFFEA4AFF, 0xF6BD31FF, 0xD79B0FFF, 0xDA6E0AFF },
            { 0xFFBF81FF, 0xFFA53CFF, 0xD79B0FFF, 0xDA6E0AFF },
            { 0xFFA53CFF, 0xD79B0FFF, 0xDA6E0AFF, 0xB45A00FF },
            { 0xD79B0FFF, 0xDA6E0AFF, 0xB45A00FF, 0xA04B05FF },
            { 0xDA6E0AFF, 0xB45A00FF, 0x7F3F00FF, 0x621800FF },
            { 0xDA6E0AFF, 0xA04B05FF, 0xA5140AFF, 0x621800FF },
            { 0x73413CFF, 0x5F3214FF, 0x401811FF, 0x280A1EFF },
            { 0x73573BFF, 0x53500AFF, 0x283405FF, 0x191E0FFF },
            { 0x73733BFF, 0x626200FF, 0x283405FF, 0x401811FF },
            { 0xA6A090FF, 0x8C805AFF, 0x73573BFF, 0x465032FF },
            { 0xBFBF3FFF, 0xAC9400FF, 0x7F7F00FF, 0x626200FF },
            { 0xBFBF3FFF, 0xB1B10AFF, 0x7F7F00FF, 0x626200FF },
            { 0xFFEA4AFF, 0xE6D55AFF, 0xBFBF3FFF, 0xB1B10AFF },
            { 0xFFEA4AFF, 0xFFD510FF, 0xD79B0FFF, 0xAC9400FF },
            { 0xFFFFBFFF, 0xFFEA4AFF, 0xF6BD31FF, 0xBFBF3FFF },
            { 0xFFFFBFFF, 0xC8FF41FF, 0x9BF046FF, 0x96DC19FF },
            { 0xC8FF41FF, 0x9BF046FF, 0x8EBE55FF, 0x73C805FF },
            { 0x9BF046FF, 0x96DC19FF, 0x73C805FF, 0x6AA805FF },
            { 0x96DC19FF, 0x73C805FF, 0x7F7F00FF, 0x626200FF },
            { 0x8EBE55FF, 0x6AA805FF, 0x7F7F00FF, 0x626200FF },
            { 0x587D3EFF, 0x3C6E14FF, 0x0C5C0CFF, 0x204608FF },
            { 0x465032FF, 0x283405FF, 0x131313FF, 0x010101FF },
            { 0x235037FF, 0x204608FF, 0x191E0FFF, 0x131313FF },
            { 0x3B573BFF, 0x0C5C0CFF, 0x191E0FFF, 0x010101FF },
            { 0x00C514FF, 0x149605FF, 0x0C5C0CFF, 0x204608FF },
            { 0x4BF05AFF, 0x0AD70AFF, 0x149605FF, 0x007F00FF },
            { 0x4BF05AFF, 0x14E60AFF, 0x00C514FF, 0x149605FF },
            { 0xAFFFAFFF, 0x7DFF73FF, 0x4BF05AFF, 0x3FBF3FFF },
            { 0x7DFF73FF, 0x4BF05AFF, 0x3FBF3FFF, 0x00C514FF },
            { 0x14E60AFF, 0x00C514FF, 0x149605FF, 0x007F00FF },
            { 0x00DE6AFF, 0x05B450FF, 0x1C8C4EFF, 0x0F6946FF },
            { 0x05B450FF, 0x1C8C4EFF, 0x0F6946FF, 0x0C5C0CFF },
            { 0x3B5773FF, 0x123832FF, 0x131313FF, 0x010101FF },
            { 0x06C491FF, 0x129880FF, 0x055A5CFF, 0x0C2148FF },
            { 0x3FBFBFFF, 0x06C491FF, 0x129880FF, 0x007F7FFF },
            { 0x2DEBA8FF, 0x00DE6AFF, 0x05B450FF, 0x1C8C4EFF },
            { 0x6AFFCDFF, 0x2DEBA8FF, 0x06C491FF, 0x129880FF },
            { 0x6AFFCDFF, 0x3CFEA5FF, 0x06C491FF, 0x05B450FF },
            { 0x91EBFFFF, 0x6AFFCDFF, 0x3CFEA5FF, 0x2DEBA8FF },
            { 0xBFFFFFFF, 0x91EBFFFF, 0x5AC5FFFF, 0x3FBFBFFF },
            { 0x91EBFFFF, 0x55E6FFFF, 0x08DED5FF, 0x109CDEFF },
            { 0xBFFFFFFF, 0x7DD7F0FF, 0x3FBFBFFF, 0x699DC3FF },
            { 0x00FFFFFF, 0x08DED5FF, 0x109CDEFF, 0x007F7FFF },
            { 0x4AA4FFFF, 0x109CDEFF, 0x007FFFFF, 0x186ABDFF },
            { 0x007F7FFF, 0x055A5CFF, 0x162C52FF, 0x0C2148FF },
            { 0x3B5773FF, 0x162C52FF, 0x010101FF, 0x010101FF },
            { 0x3F3FBFFF, 0x0F377DFF, 0x00007FFF, 0x0F0F50FF },
            { 0x186ABDFF, 0x004A9CFF, 0x00007FFF, 0x0F192DFF },
            { 0x4B7DC8FF, 0x326496FF, 0x004A9CFF, 0x0F377DFF },
            { 0x007FFFFF, 0x0052F6FF, 0x101CDAFF, 0x0010BDFF },
            { 0x4AA4FFFF, 0x186ABDFF, 0x004A9CFF, 0x0F377DFF },
            { 0x4AA4FFFF, 0x2378DCFF, 0x004A9CFF, 0x0010BDFF },
            { 0x8FC7C7FF, 0x699DC3FF, 0x4B7DC8FF, 0x57738FFF },
            { 0x55E6FFFF, 0x4AA4FFFF, 0x109CDEFF, 0x2378DCFF },
            { 0x91EBFFFF, 0x90B0FFFF, 0x8181FFFF, 0x786EF0FF },
            { 0x91EBFFFF, 0x5AC5FFFF, 0x4AA4FFFF, 0x109CDEFF },
            { 0xE3E3FFFF, 0xBEB9FAFF, 0x8F8FC7FF, 0x7676CAFF },
            { 0x00FFFFFF, 0x00BFFFFF, 0x109CDEFF, 0x007FFFFF },
            { 0x00BFFFFF, 0x007FFFFF, 0x0052F6FF, 0x101CDAFF },
            { 0x8181FFFF, 0x4B7DC8FF, 0x186ABDFF, 0x3F3FBFFF },
            { 0xB991FFFF, 0x786EF0FF, 0x6241F6FF, 0x3F3FBFFF },
            { 0x8181FFFF, 0x4A5AFFFF, 0x101CDAFF, 0x0010BDFF },
            { 0x786EF0FF, 0x6241F6FF, 0x3F3FBFFF, 0x101CDAFF },
            { 0x8181FFFF, 0x3C3CF5FF, 0x101CDAFF, 0x0010BDFF },
            { 0x3C3CF5FF, 0x101CDAFF, 0x00007FFF, 0x0F192DFF },
            { 0x3C3CF5FF, 0x0010BDFF, 0x00007FFF, 0x0F192DFF },
            { 0x5010B0FF, 0x231094FF, 0x00007FFF, 0x0F192DFF },
            { 0x0F377DFF, 0x0C2148FF, 0x010101FF, 0x010101FF },
            { 0x8732D2FF, 0x5010B0FF, 0x231094FF, 0x00007FFF },
            { 0x8732D2FF, 0x6010D0FF, 0x231094FF, 0x00007FFF },
            { 0x9C41FFFF, 0x8732D2FF, 0x6010D0FF, 0x5010B0FF },
            { 0xBD62FFFF, 0x9C41FFFF, 0x8732D2FF, 0x7F00FFFF },
            { 0xBD29FFFF, 0x7F00FFFF, 0x5010B0FF, 0x231094FF },
            { 0xE673FFFF, 0xBD62FFFF, 0x9C41FFFF, 0x8732D2FF },
            { 0xD7C3FAFF, 0xB991FFFF, 0xBD62FFFF, 0x786EF0FF },
            { 0xF8C6FCFF, 0xD7A5FFFF, 0xBD62FFFF, 0x8F57C7FF },
            { 0xFFFFFFFF, 0xD7C3FAFF, 0xABABE3FF, 0xAB8FC7FF },
            { 0xFFFFFFFF, 0xF8C6FCFF, 0xEBACE1FF, 0xD7A0BEFF },
            { 0xF8C6FCFF, 0xE673FFFF, 0xBD62FFFF, 0xBD29FFFF },
            { 0xFD81FFFF, 0xFF52FFFF, 0xDA20E0FF, 0xBD10C5FF },
            { 0xFF52FFFF, 0xDA20E0FF, 0x7F007FFF, 0x410062FF },
            { 0xBD62FFFF, 0xBD29FFFF, 0x7F00FFFF, 0x8C14BEFF },
            { 0xDA20E0FF, 0xBD10C5FF, 0x7F007FFF, 0x410062FF },
            { 0xBD29FFFF, 0x8C14BEFF, 0x5010B0FF, 0x7F007FFF },
            { 0x8C14BEFF, 0x5A187BFF, 0x410062FF, 0x320A46FF },
            { 0x724072FF, 0x641464FF, 0x320A46FF, 0x280A1EFF },
            { 0x5A187BFF, 0x410062FF, 0x280A1EFF, 0x010101FF },
            { 0x641464FF, 0x320A46FF, 0x010101FF, 0x010101FF },
            { 0x73413CFF, 0x551937FF, 0x280A1EFF, 0x010101FF },
            { 0xBF3FBFFF, 0xA01982FF, 0x7F007FFF, 0x410062FF },
            { 0xE61E78FF, 0xC80078FF, 0x911437FF, 0x7F0000FF },
            { 0xFD81FFFF, 0xFF50BFFF, 0xFC3A8CFF, 0xE61E78FF },
            { 0xFD81FFFF, 0xFF6AC5FF, 0xC35A91FF, 0xE61E78FF },
            { 0xFFC0CBFF, 0xFAA0B9FF, 0xFF6AC5FF, 0xC87DA0FF },
            { 0xFF50BFFF, 0xFC3A8CFF, 0xE61E78FF, 0xC80078FF },
            { 0xFC3A8CFF, 0xE61E78FF, 0xC80078FF, 0xBD1039FF },
            { 0xE61E78FF, 0xBD1039FF, 0x7F0000FF, 0x280A1EFF },
            { 0xA0695FFF, 0x98344DFF, 0x911437FF, 0x551937FF },
            { 0x98344DFF, 0x911437FF, 0x551414FF, 0x7F0000FF },
    };

    /**
     * Bytes that correspond to palette indices to use when shading an Aurora-palette model and ramps should consider
     * the aesthetic warmth or coolness of a color, brightening to a warmer color and darkening to a cooler one.
     * The color in index 1 of each 4-element sub-array is the "twilight" color and the one that will match the voxel
     * color used in a model. The color at index 0 is "bright", index 2 is "dim", and index 3 is "dark". Normal usage
     * with a renderer will use {@link #WARMTH_RAMP_VALUES}; this array would be used to figure out what indices are
     * related to another color for the purpose of procedurally using similar colors with different lightness.
     * To visualize this, <a href="https://i.imgur.com/zeJpyQ4.png">use this image</a>, with the first 32 items in
     * WARMTH_RAMPS corresponding to the first column from top to bottom, the next 32 items in the second column, etc.
     */
    public static final byte[][] WARMTH_RAMPS = new byte[][]{
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
     * The color in index 1 of each 4-element sub-array is the "twilight" color and the one that will match the voxel
     * color used in a model. The color at index 0 is "bright", index 2 is "dim", and index 3 is "dark".
     * To visualize this, <a href="https://i.imgur.com/zeJpyQ4.png">use this image</a>, with the first 32 items in
     * WARMTH_RAMPS corresponding to the first column from top to bottom, the next 32 items in the second column, etc.
     * Color values as RGBA8888 ints to use when shading an Aurora-palette model.
     */
    public static final int[][] WARMTH_RAMP_VALUES = {
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
}
