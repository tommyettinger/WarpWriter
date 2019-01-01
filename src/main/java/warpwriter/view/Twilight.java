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
            final int length = RAMP_VALUES[voxel & 255].length - 1;
            return RAMP_VALUES[voxel & 255][
                    brightness < 0 ? 0 :
                            brightness > length ? length :
                                    brightness
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
                final int length = RAMP_VALUES[voxel & 255].length - 1;
                return RAMP_VALUES[voxel & 255][
                        brightness < 0 ? 0 :
                                brightness > length ? length :
                                        brightness
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
     * To visualize this, <a href="https://i.imgur.com/dvI4tmi.png">use this image</a>, with the first 32 items in RAMPS
     * corresponding to the first column from top to bottom, the next 32 items in the second column, etc.
     */
    public static final byte[][] RAMPS = new byte[][]{
            {0, 0, 0, 0},
            {87, 1, 1, 1},
            {103, 2, 1, 1},
            {73, 3, 1, 1},
            {89, 4, 87, 2},
            {90, 5, 103, 87},
            {7, 6, 73, 4},
            {50, 7, 6, 5},
            {49, 8, 90, 6},
            {11, 9, 7, 90},
            {12, 10, 50, 8},
            {12, 11, 49, 9},
            {14, 12, 11, 10},
            {97, 13, 11, 10},
            {15, 14, 12, 11},
            {15, 15, 13, 12},
            {-66, 16, -55, -67},
            {-58, 17, -66, 16},
            {-61, 18, -57, -42},
            {15, 19, 108, 107},
            {-45, 20, -38, 22},
            {-35, 21, 1, 1},
            {-38, 22, -53, -33},
            {-33, 23, 1, 1},
            {-54, 24, 1, 1},
            {-10, 25, -13, 1},
            {-120, 26, -10, 25},
            {-19, 27, -17, -9},
            {-24, 28, -26, -120},
            {60, 29, 66, -114},
            {56, 30, -100, -103},
            {-104, 31, -105, 33},
            {-103, 32, -2, -105},
            {-105, 33, 1, 1},
            {-93, 34, -108, 1},
            {-95, 35, -93, -106},
            {69, 36, -95, -94},
            {-98, 37, -96, -95},
            {60, 38, 57, 69},
            {15, 39, 80, 81},
            {15, 40, -86, -88},
            {-87, 41, -88, -89},
            {-89, 42, -91, -92},
            {-75, 43, -76, -77},
            {-71, 44, -75, 43},
            {-71, 45, -73, -74},
            {15, 46, -72, 96},
            {12, 47, 9, 8},
            {78, 48, 76, 75},
            {11, 49, 84, 8},
            {10, 50, 101, 7},
            {10, 51, 90, 6},
            {75, 52, 90, 71},
            {54, 53, 63, 71},
            {65, 54, 53, 63},
            {48, 55, 64, 53},
            {58, 56, 36, 70},
            {38, 57, 69, 55},
            {60, 58, 56, 69},
            {60, 59, 57, 48},
            {15, 60, 68, 78},
            {6, 61, 72, -107},
            {71, 62, -93, 72},
            {53, 63, 62, 61},
            {65, 64, 63, 71},
            {48, 65, 54, 64},
            {68, 66, 69, 65},
            {14, 67, 68, -115},
            {60, 68, 48, 69},
            {77, 69, 75, -90},
            {75, 70, 71, -92},
            {74, 71, -92, 73},
            {86, 72, 87, 2},
            {90, 73, 87, 2},
            {84, 74, -79, -92},
            {76, 75, 74, -79},
            {83, 76, 75, 84},
            {78, 77, 76, 75},
            {79, 78, 77, 76},
            {80, 79, 78, 82},
            {39, 80, 81, 79},
            {80, 81, 96, 82},
            {81, 82, 100, 93},
            {82, 83, -80, 42},
            {93, 84, 85, -79},
            {84, 85, -79, 89},
            {90, 86, -78, 87},
            {-78, 87, 1, 1},
            {102, 88, -67, 103},
            {91, 89, -77, -67},
            {101, 90, 88, -77},
            {92, 91, 102, -76},
            {93, 92, 91, -79},
            {94, 93, 92, 85},
            {95, 94, 93, 92},
            {99, 95, 94, 93},
            {98, 96, 94, 93},
            {15, 97, 13, 99},
            {19, 98, 96, 95},
            {19, 99, 95, 100},
            {82, 100, 92, 101},
            {93, 101, 102, 88},
            {91, 102, -76, -67},
            {104, 103, 2, 1},
            {116, 104, -32, 117},
            {51, 105, 102, 88},
            {107, 106, 51, 101},
            {108, 107, 113, 106},
            {19, 108, 107, 106},
            {15, 109, 107, 113},
            {127, 110, 112, 113},
            {108, 111, 113, 106},
            {110, 112, 50, 51},
            {107, 113, 106, 50},
            {113, 114, -51, -55},
            {50, 115, 116, 104},
            {105, 116, 104, -67},
            {103, 117, 1, 1},
            {119, 118, 1, 1},
            {116, 119, -54, 118},
            {115, 120, -54, 118},
            {115, 121, 119, -54},
            {124, 122, 121, 120},
            {124, 123, 121, 120},
            {113, 124, 51, 122},
            {111, 125, 124, 51},
            {109, 126, 111, 112},
            {15, 127, 110, 111},
            {125, -128, 8, 122},
            {123, -127, -126, -125},
            {122, -126, 120, -125},
            {121, -125, -124, 118},
            {61, -124, 2, 1},
            {5, -123, 3, 2},
            {-126, -122, -123, -110},
            {-119, -121, -122, -125},
            {-119, -120, -122, -125},
            {-128, -119, -121, -126},
            {-116, -118, -113, -112},
            {15, -117, -116, -115},
            {127, -116, 47, 10},
            {67, -115, 47, 10},
            {-115, -114, 65, -112},
            {-118, -113, -119, -121},
            {-114, -112, 64, 53},
            {-112, -111, -121, -2},
            {62, -110, -107, -109},
            {72, -109, 1, 1},
            {-124, -108, 1, 1},
            {-93, -107, 2, 1},
            {-93, -106, -108, 1},
            {-104, -105, 33, 1},
            {-102, -104, -105, 33},
            {-100, -103, 32, -2},
            {-101, -102, -104, -105},
            {37, -101, -102, 32},
            {30, -100, -103, 32},
            {-87, -99, -97, -89},
            {-99, -98, -97, -96},
            {-99, -97, -89, 42},
            {37, -96, -95, -94},
            {-96, -95, 35, -106},
            {-96, -94, 35, -106},
            {62, -93, -107, -108},
            {74, -92, -78, 87},
            {42, -91, -78, 87},
            {49, -90, 74, 71},
            {-88, -89, 42, -91},
            {41, -88, -89, 42},
            {-85, -87, 41, -88},
            {-85, -86, -99, -88},
            {39, -85, -87, -99},
            {15, -84, -83, -82},
            {-84, -83, -82, -81},
            {-83, -82, -81, -80},
            {-82, -81, -80, -75},
            {-81, -80, -75, -79},
            {85, -79, -76, -77},
            {73, -78, 87, 2},
            {-76, -77, 87, 1},
            {43, -76, -77, -78},
            {44, -75, 43, -76},
            {-71, -74, -75, 43},
            {45, -73, -70, -75},
            {46, -72, -71, 44},
            {-72, -71, -73, -74},
            {-74, -70, -75, 43},
            {-64, -69, -75, 43},
            {-69, -68, 102, -76},
            {88, -67, 87, 2},
            {-65, -66, 16, 102},
            {-63, -65, -69, -66},
            {-63, -64, -69, -75},
            {-62, -63, -64, -65},
            {-61, -62, -63, -64},
            {19, -61, -62, -63},
            {19, -60, -58, -44},
            {-60, -59, -44, 17},
            {-60, -58, -44, 17},
            {-63, -57, -65, -66},
            {-42, -56, -48, -49},
            {105, -55, -67, -32},
            {104, -54, 117, 24},
            {-52, -53, -32, 24},
            {-49, -52, -53, -32},
            {115, -51, -55, -53},
            {-41, -50, -35, -34},
            {-56, -49, -52, -53},
            {-56, -48, -52, -53},
            {113, -47, 115, 105},
            {-44, -46, -56, -48},
            {-43, -45, -47, 114},
            {-59, -44, 17, -46},
            {110, -43, 113, 124},
            {-57, -42, -56, -41},
            {-56, -41, -50, -52},
            {-47, -40, -51, -52},
            {-25, -39, -38, 22},
            {-39, -38, -36, 22},
            {-38, -37, 22, -31},
            {-38, -36, -35, -34},
            {-36, -35, 23, 1},
            {-36, -34, 23, 1},
            {22, -33, 23, 1},
            {104, -32, 1, 1},
            {-29, -31, -33, -13},
            {-29, -30, -33, 23},
            {-28, -29, -16, -31},
            {-26, -28, -29, -30},
            {-18, -27, -31, 23},
            {-21, -26, -127, -28},
            {-24, -25, 124, 123},
            {-23, -24, -25, -128},
            {126, -23, 125, -128},
            {-117, -22, -115, -118},
            {-24, -21, -26, -120},
            {-21, -20, 26, -19},
            {-20, -19, -17, 25},
            {-28, -18, -17, -16},
            {-19, -17, -16, 25},
            {-29, -16, 25, -13},
            {-125, -15, -12, -13},
            {-122, -14, -12, -13},
            {-15, -13, 1, 1},
            {118, -12, 1, 1},
            {61, -11, -108, 1},
            {26, -10, 25, -13},
            {-4, -9, 25, 33},
            {-7, -8, -5, -4},
            {28, -7, -8, -111},
            {29, -6, 65, -112},
            {-8, -5, -4, -3},
            {-5, -4, -9, -3},
            {-104, -3, 33, 1},
            {63, -2, -1, 34},
            {-2, -1, 33, -108},
    };

    /**
     * Color values as RGBA8888 ints to use when shading an Aurora-palette model.
     * The color in index 1 of each 4-element sub-array is the "twilight" color and the one that will match the voxel
     * color used in a model. The color at index 0 is "bright", index 2 is "dim", and index 3 is "dark".
     * To visualize this, <a href="https://i.imgur.com/dvI4tmi.png">use this image</a>, with the first 32 items in RAMPS
     * corresponding to the first column from top to bottom, the next 32 items in the second column, etc.
     */
    public static final int[][] RAMP_VALUES = new int[][]{
            {0x00000000, 0x00000000, 0x00000000, 0x00000000},
            {0x191E0FFF, 0x010101FF, 0x010101FF, 0x010101FF},
            {0x1E2D23FF, 0x131313FF, 0x010101FF, 0x010101FF},
            {0x414123FF, 0x252525FF, 0x010101FF, 0x010101FF},
            {0x3B573BFF, 0x373737FF, 0x191E0FFF, 0x131313FF},
            {0x506450FF, 0x494949FF, 0x1E2D23FF, 0x191E0FFF},
            {0x6E6E6EFF, 0x5B5B5BFF, 0x414123FF, 0x373737FF},
            {0x7E9494FF, 0x6E6E6EFF, 0x5B5B5BFF, 0x494949FF},
            {0xA6A090FF, 0x808080FF, 0x506450FF, 0x5B5B5BFF},
            {0xB6B6B6FF, 0x929292FF, 0x6E6E6EFF, 0x506450FF},
            {0xC9C9C9FF, 0xA4A4A4FF, 0x7E9494FF, 0x808080FF},
            {0xC9C9C9FF, 0xB6B6B6FF, 0xA6A090FF, 0x929292FF},
            {0xEDEDEDFF, 0xC9C9C9FF, 0xB6B6B6FF, 0xA4A4A4FF},
            {0xE1F8FAFF, 0xDBDBDBFF, 0xB6B6B6FF, 0xA4A4A4FF},
            {0xFFFFFFFF, 0xEDEDEDFF, 0xC9C9C9FF, 0xB6B6B6FF},
            {0xFFFFFFFF, 0xFFFFFFFF, 0xDBDBDBFF, 0xC9C9C9FF},
            {0x129880FF, 0x007F7FFF, 0x055A5CFF, 0x123832FF},
            {0x7DD7F0FF, 0x3FBFBFFF, 0x129880FF, 0x007F7FFF},
            {0x6AFFCDFF, 0x00FFFFFF, 0x08DED5FF, 0x00BFFFFF},
            {0xFFFFFFFF, 0xBFFFFFFF, 0xABE3E3FF, 0x8FC7C7FF},
            {0x90B0FFFF, 0x8181FFFF, 0x4A5AFFFF, 0x3F3FBFFF},
            {0x101CDAFF, 0x0000FFFF, 0x010101FF, 0x010101FF},
            {0x4A5AFFFF, 0x3F3FBFFF, 0x0F377DFF, 0x231094FF},
            {0x231094FF, 0x00007FFF, 0x010101FF, 0x010101FF},
            {0x162C52FF, 0x0F0F50FF, 0x010101FF, 0x010101FF},
            {0xA01982FF, 0x7F007FFF, 0x410062FF, 0x010101FF},
            {0xAB57ABFF, 0xBF3FBFFF, 0xA01982FF, 0x7F007FFF},
            {0xDA20E0FF, 0xF500F5FF, 0xBD10C5FF, 0xC80078FF},
            {0xD7A5FFFF, 0xFD81FFFF, 0xBD62FFFF, 0xAB57ABFF},
            {0xF5E1D2FF, 0xFFC0CBFF, 0xE3ABABFF, 0xD7A0BEFF},
            {0xE19B7DFF, 0xFF8181FF, 0xFF6262FF, 0xD5524AFF},
            {0xDA2010FF, 0xFF0000FF, 0xA5140AFF, 0x7F0000FF},
            {0xD5524AFF, 0xBF3F3FFF, 0x98344DFF, 0xA5140AFF},
            {0xA5140AFF, 0x7F0000FF, 0x010101FF, 0x010101FF},
            {0x5F3214FF, 0x551414FF, 0x280A1EFF, 0x010101FF},
            {0xB45A00FF, 0x7F3F00FF, 0x5F3214FF, 0x621800FF},
            {0xC49E73FF, 0xBF7F3FFF, 0xB45A00FF, 0xA04B05FF},
            {0xFFA53CFF, 0xFF7F00FF, 0xDA6E0AFF, 0xB45A00FF},
            {0xF5E1D2FF, 0xFFBF81FF, 0xEBAA8CFF, 0xC49E73FF},
            {0xFFFFFFFF, 0xFFFFBFFF, 0xEDEDC7FF, 0xC7E3ABFF},
            {0xFFFFFFFF, 0xFFFF00FF, 0xFFD510FF, 0xB1B10AFF},
            {0xE6D55AFF, 0xBFBF3FFF, 0xB1B10AFF, 0xAC9400FF},
            {0xAC9400FF, 0x7F7F00FF, 0x626200FF, 0x53500AFF},
            {0x149605FF, 0x007F00FF, 0x0C5C0CFF, 0x204608FF},
            {0x4BF05AFF, 0x3FBF3FFF, 0x149605FF, 0x007F00FF},
            {0x4BF05AFF, 0x00FF00FF, 0x14E60AFF, 0x0AD70AFF},
            {0xFFFFFFFF, 0xAFFFAFFF, 0x7DFF73FF, 0xA2D8A2FF},
            {0xC9C9C9FF, 0xBCAFC0FF, 0x929292FF, 0x808080FF},
            {0xC7C78FFF, 0xCBAA89FF, 0xA2A255FF, 0x8F8F57FF},
            {0xB6B6B6FF, 0xA6A090FF, 0x738F57FF, 0x808080FF},
            {0xA4A4A4FF, 0x7E9494FF, 0x507D5FFF, 0x6E6E6EFF},
            {0xA4A4A4FF, 0x6E8287FF, 0x506450FF, 0x5B5B5BFF},
            {0x8F8F57FF, 0x7E6E60FF, 0x506450FF, 0x73573BFF},
            {0xC07872FF, 0xA0695FFF, 0x8E5555FF, 0x73573BFF},
            {0xC78F8FFF, 0xC07872FF, 0xA0695FFF, 0x8E5555FF},
            {0xCBAA89FF, 0xD08A74FF, 0xAB7373FF, 0xA0695FFF},
            {0xF5B99BFF, 0xE19B7DFF, 0xBF7F3FFF, 0x8F7357FF},
            {0xFFBF81FF, 0xEBAA8CFF, 0xC49E73FF, 0xD08A74FF},
            {0xF5E1D2FF, 0xF5B99BFF, 0xE19B7DFF, 0xC49E73FF},
            {0xF5E1D2FF, 0xF6C8AFFF, 0xEBAA8CFF, 0xCBAA89FF},
            {0xFFFFFFFF, 0xF5E1D2FF, 0xE3C7ABFF, 0xC7C78FFF},
            {0x5B5B5BFF, 0x573B3BFF, 0x3B2D1FFF, 0x401811FF},
            {0x73573BFF, 0x73413CFF, 0x5F3214FF, 0x3B2D1FFF},
            {0xA0695FFF, 0x8E5555FF, 0x73413CFF, 0x573B3BFF},
            {0xC78F8FFF, 0xAB7373FF, 0x8E5555FF, 0x73573BFF},
            {0xCBAA89FF, 0xC78F8FFF, 0xC07872FF, 0xAB7373FF},
            {0xE3C7ABFF, 0xE3ABABFF, 0xC49E73FF, 0xC78F8FFF},
            {0xEDEDEDFF, 0xF8D2DAFF, 0xE3C7ABFF, 0xE1B9D2FF},
            {0xF5E1D2FF, 0xE3C7ABFF, 0xCBAA89FF, 0xC49E73FF},
            {0xB5B572FF, 0xC49E73FF, 0x8F8F57FF, 0x8C805AFF},
            {0x8F8F57FF, 0x8F7357FF, 0x73573BFF, 0x53500AFF},
            {0x73733BFF, 0x73573BFF, 0x53500AFF, 0x414123FF},
            {0x465032FF, 0x3B2D1FFF, 0x191E0FFF, 0x131313FF},
            {0x506450FF, 0x414123FF, 0x191E0FFF, 0x131313FF},
            {0x738F57FF, 0x73733BFF, 0x3C6E14FF, 0x53500AFF},
            {0xA2A255FF, 0x8F8F57FF, 0x73733BFF, 0x3C6E14FF},
            {0x8EBE55FF, 0xA2A255FF, 0x8F8F57FF, 0x738F57FF},
            {0xC7C78FFF, 0xB5B572FF, 0xA2A255FF, 0x8F8F57FF},
            {0xDADAABFF, 0xC7C78FFF, 0xB5B572FF, 0xA2A255FF},
            {0xEDEDC7FF, 0xDADAABFF, 0xC7C78FFF, 0xABC78FFF},
            {0xFFFFBFFF, 0xEDEDC7FF, 0xC7E3ABFF, 0xDADAABFF},
            {0xEDEDC7FF, 0xC7E3ABFF, 0xA2D8A2FF, 0xABC78FFF},
            {0xC7E3ABFF, 0xABC78FFF, 0x87B48EFF, 0x73AB73FF},
            {0xABC78FFF, 0x8EBE55FF, 0x6AA805FF, 0x7F7F00FF},
            {0x73AB73FF, 0x738F57FF, 0x587D3EFF, 0x3C6E14FF},
            {0x738F57FF, 0x587D3EFF, 0x3C6E14FF, 0x3B573BFF},
            {0x506450FF, 0x465032FF, 0x283405FF, 0x191E0FFF},
            {0x283405FF, 0x191E0FFF, 0x010101FF, 0x010101FF},
            {0x0F6946FF, 0x235037FF, 0x123832FF, 0x1E2D23FF},
            {0x3B7349FF, 0x3B573BFF, 0x204608FF, 0x123832FF},
            {0x507D5FFF, 0x506450FF, 0x235037FF, 0x204608FF},
            {0x578F57FF, 0x3B7349FF, 0x0F6946FF, 0x0C5C0CFF},
            {0x73AB73FF, 0x578F57FF, 0x3B7349FF, 0x3C6E14FF},
            {0x64C082FF, 0x73AB73FF, 0x578F57FF, 0x587D3EFF},
            {0x8FC78FFF, 0x64C082FF, 0x73AB73FF, 0x578F57FF},
            {0xABE3C5FF, 0x8FC78FFF, 0x64C082FF, 0x73AB73FF},
            {0xB4EECAFF, 0xA2D8A2FF, 0x64C082FF, 0x73AB73FF},
            {0xFFFFFFFF, 0xE1F8FAFF, 0xDBDBDBFF, 0xABE3C5FF},
            {0xBFFFFFFF, 0xB4EECAFF, 0xA2D8A2FF, 0x8FC78FFF},
            {0xBFFFFFFF, 0xABE3C5FF, 0x8FC78FFF, 0x87B48EFF},
            {0xABC78FFF, 0x87B48EFF, 0x578F57FF, 0x507D5FFF},
            {0x73AB73FF, 0x507D5FFF, 0x0F6946FF, 0x235037FF},
            {0x3B7349FF, 0x0F6946FF, 0x0C5C0CFF, 0x123832FF},
            {0x234146FF, 0x1E2D23FF, 0x131313FF, 0x010101FF},
            {0x3B5773FF, 0x234146FF, 0x0C2148FF, 0x0F192DFF},
            {0x6E8287FF, 0x3B7373FF, 0x0F6946FF, 0x235037FF},
            {0x8FC7C7FF, 0x64ABABFF, 0x6E8287FF, 0x507D5FFF},
            {0xABE3E3FF, 0x8FC7C7FF, 0x8FABC7FF, 0x64ABABFF},
            {0xBFFFFFFF, 0xABE3E3FF, 0x8FC7C7FF, 0x64ABABFF},
            {0xFFFFFFFF, 0xC7F1F1FF, 0x8FC7C7FF, 0x8FABC7FF},
            {0xE3E3FFFF, 0xBED2F0FF, 0xA8B9DCFF, 0x8FABC7FF},
            {0xABE3E3FF, 0xABC7E3FF, 0x8FABC7FF, 0x64ABABFF},
            {0xBED2F0FF, 0xA8B9DCFF, 0x7E9494FF, 0x6E8287FF},
            {0x8FC7C7FF, 0x8FABC7FF, 0x64ABABFF, 0x7E9494FF},
            {0x8FABC7FF, 0x578FC7FF, 0x326496FF, 0x055A5CFF},
            {0x7E9494FF, 0x57738FFF, 0x3B5773FF, 0x234146FF},
            {0x3B7373FF, 0x3B5773FF, 0x234146FF, 0x123832FF},
            {0x1E2D23FF, 0x0F192DFF, 0x010101FF, 0x010101FF},
            {0x3B3B57FF, 0x1F1F3BFF, 0x010101FF, 0x010101FF},
            {0x3B5773FF, 0x3B3B57FF, 0x162C52FF, 0x1F1F3BFF},
            {0x57738FFF, 0x494973FF, 0x162C52FF, 0x1F1F3BFF},
            {0x57738FFF, 0x57578FFF, 0x3B3B57FF, 0x162C52FF},
            {0x8F8FC7FF, 0x736EAAFF, 0x57578FFF, 0x494973FF},
            {0x8F8FC7FF, 0x7676CAFF, 0x57578FFF, 0x494973FF},
            {0x8FABC7FF, 0x8F8FC7FF, 0x6E8287FF, 0x736EAAFF},
            {0xABC7E3FF, 0xABABE3FF, 0x8F8FC7FF, 0x6E8287FF},
            {0xC7F1F1FF, 0xD0DAF8FF, 0xABC7E3FF, 0xA8B9DCFF},
            {0xFFFFFFFF, 0xE3E3FFFF, 0xBED2F0FF, 0xABC7E3FF},
            {0xABABE3FF, 0xAB8FC7FF, 0x808080FF, 0x736EAAFF},
            {0x7676CAFF, 0x8F57C7FF, 0x73578FFF, 0x573B73FF},
            {0x736EAAFF, 0x73578FFF, 0x494973FF, 0x573B73FF},
            {0x57578FFF, 0x573B73FF, 0x3C233CFF, 0x1F1F3BFF},
            {0x573B3BFF, 0x3C233CFF, 0x131313FF, 0x010101FF},
            {0x494949FF, 0x463246FF, 0x252525FF, 0x131313FF},
            {0x73578FFF, 0x724072FF, 0x463246FF, 0x4B2837FF},
            {0xAB73ABFF, 0x8F578FFF, 0x724072FF, 0x573B73FF},
            {0xAB73ABFF, 0xAB57ABFF, 0x724072FF, 0x573B73FF},
            {0xAB8FC7FF, 0xAB73ABFF, 0x8F578FFF, 0x73578FFF},
            {0xE3C7E3FF, 0xEBACE1FF, 0xC78FB9FF, 0xC87DA0FF},
            {0xFFFFFFFF, 0xFFDCF5FF, 0xE3C7E3FF, 0xE1B9D2FF},
            {0xE3E3FFFF, 0xE3C7E3FF, 0xBCAFC0FF, 0xA4A4A4FF},
            {0xF8D2DAFF, 0xE1B9D2FF, 0xBCAFC0FF, 0xA4A4A4FF},
            {0xE1B9D2FF, 0xD7A0BEFF, 0xC78F8FFF, 0xC87DA0FF},
            {0xEBACE1FF, 0xC78FB9FF, 0xAB73ABFF, 0x8F578FFF},
            {0xD7A0BEFF, 0xC87DA0FF, 0xAB7373FF, 0xA0695FFF},
            {0xC87DA0FF, 0xC35A91FF, 0x8F578FFF, 0x98344DFF},
            {0x73413CFF, 0x4B2837FF, 0x401811FF, 0x321623FF},
            {0x3B2D1FFF, 0x321623FF, 0x010101FF, 0x010101FF},
            {0x3C233CFF, 0x280A1EFF, 0x010101FF, 0x010101FF},
            {0x5F3214FF, 0x401811FF, 0x131313FF, 0x010101FF},
            {0x5F3214FF, 0x621800FF, 0x280A1EFF, 0x010101FF},
            {0xDA2010FF, 0xA5140AFF, 0x7F0000FF, 0x010101FF},
            {0xFF3C0AFF, 0xDA2010FF, 0xA5140AFF, 0x7F0000FF},
            {0xFF6262FF, 0xD5524AFF, 0xBF3F3FFF, 0x98344DFF},
            {0xF55A32FF, 0xFF3C0AFF, 0xDA2010FF, 0xA5140AFF},
            {0xFF7F00FF, 0xF55A32FF, 0xFF3C0AFF, 0xBF3F3FFF},
            {0xFF8181FF, 0xFF6262FF, 0xD5524AFF, 0xBF3F3FFF},
            {0xE6D55AFF, 0xF6BD31FF, 0xD79B0FFF, 0xAC9400FF},
            {0xF6BD31FF, 0xFFA53CFF, 0xD79B0FFF, 0xDA6E0AFF},
            {0xF6BD31FF, 0xD79B0FFF, 0xAC9400FF, 0x7F7F00FF},
            {0xFF7F00FF, 0xDA6E0AFF, 0xB45A00FF, 0xA04B05FF},
            {0xDA6E0AFF, 0xB45A00FF, 0x7F3F00FF, 0x621800FF},
            {0xDA6E0AFF, 0xA04B05FF, 0x7F3F00FF, 0x621800FF},
            {0x73413CFF, 0x5F3214FF, 0x401811FF, 0x280A1EFF},
            {0x73733BFF, 0x53500AFF, 0x283405FF, 0x191E0FFF},
            {0x7F7F00FF, 0x626200FF, 0x283405FF, 0x191E0FFF},
            {0xA6A090FF, 0x8C805AFF, 0x73733BFF, 0x73573BFF},
            {0xB1B10AFF, 0xAC9400FF, 0x7F7F00FF, 0x626200FF},
            {0xBFBF3FFF, 0xB1B10AFF, 0xAC9400FF, 0x7F7F00FF},
            {0xFFEA4AFF, 0xE6D55AFF, 0xBFBF3FFF, 0xB1B10AFF},
            {0xFFEA4AFF, 0xFFD510FF, 0xF6BD31FF, 0xB1B10AFF},
            {0xFFFFBFFF, 0xFFEA4AFF, 0xE6D55AFF, 0xF6BD31FF},
            {0xFFFFFFFF, 0xC8FF41FF, 0x9BF046FF, 0x96DC19FF},
            {0xC8FF41FF, 0x9BF046FF, 0x96DC19FF, 0x73C805FF},
            {0x9BF046FF, 0x96DC19FF, 0x73C805FF, 0x6AA805FF},
            {0x96DC19FF, 0x73C805FF, 0x6AA805FF, 0x149605FF},
            {0x73C805FF, 0x6AA805FF, 0x149605FF, 0x3C6E14FF},
            {0x587D3EFF, 0x3C6E14FF, 0x0C5C0CFF, 0x204608FF},
            {0x414123FF, 0x283405FF, 0x191E0FFF, 0x131313FF},
            {0x0C5C0CFF, 0x204608FF, 0x191E0FFF, 0x010101FF},
            {0x007F00FF, 0x0C5C0CFF, 0x204608FF, 0x283405FF},
            {0x3FBF3FFF, 0x149605FF, 0x007F00FF, 0x0C5C0CFF},
            {0x4BF05AFF, 0x0AD70AFF, 0x149605FF, 0x007F00FF},
            {0x00FF00FF, 0x14E60AFF, 0x00C514FF, 0x149605FF},
            {0xAFFFAFFF, 0x7DFF73FF, 0x4BF05AFF, 0x3FBF3FFF},
            {0x7DFF73FF, 0x4BF05AFF, 0x14E60AFF, 0x0AD70AFF},
            {0x0AD70AFF, 0x00C514FF, 0x149605FF, 0x007F00FF},
            {0x00DE6AFF, 0x05B450FF, 0x149605FF, 0x007F00FF},
            {0x05B450FF, 0x1C8C4EFF, 0x0F6946FF, 0x0C5C0CFF},
            {0x235037FF, 0x123832FF, 0x191E0FFF, 0x131313FF},
            {0x06C491FF, 0x129880FF, 0x007F7FFF, 0x0F6946FF},
            {0x2DEBA8FF, 0x06C491FF, 0x05B450FF, 0x129880FF},
            {0x2DEBA8FF, 0x00DE6AFF, 0x05B450FF, 0x149605FF},
            {0x3CFEA5FF, 0x2DEBA8FF, 0x00DE6AFF, 0x06C491FF},
            {0x6AFFCDFF, 0x3CFEA5FF, 0x2DEBA8FF, 0x00DE6AFF},
            {0xBFFFFFFF, 0x6AFFCDFF, 0x3CFEA5FF, 0x2DEBA8FF},
            {0xBFFFFFFF, 0x91EBFFFF, 0x7DD7F0FF, 0x5AC5FFFF},
            {0x91EBFFFF, 0x55E6FFFF, 0x5AC5FFFF, 0x3FBFBFFF},
            {0x91EBFFFF, 0x7DD7F0FF, 0x5AC5FFFF, 0x3FBFBFFF},
            {0x2DEBA8FF, 0x08DED5FF, 0x06C491FF, 0x129880FF},
            {0x00BFFFFF, 0x109CDEFF, 0x2378DCFF, 0x186ABDFF},
            {0x3B7373FF, 0x055A5CFF, 0x123832FF, 0x0C2148FF},
            {0x234146FF, 0x162C52FF, 0x0F192DFF, 0x0F0F50FF},
            {0x004A9CFF, 0x0F377DFF, 0x0C2148FF, 0x0F0F50FF},
            {0x186ABDFF, 0x004A9CFF, 0x0F377DFF, 0x0C2148FF},
            {0x57738FFF, 0x326496FF, 0x055A5CFF, 0x0F377DFF},
            {0x007FFFFF, 0x0052F6FF, 0x101CDAFF, 0x0010BDFF},
            {0x109CDEFF, 0x186ABDFF, 0x004A9CFF, 0x0F377DFF},
            {0x109CDEFF, 0x2378DCFF, 0x004A9CFF, 0x0F377DFF},
            {0x8FABC7FF, 0x699DC3FF, 0x57738FFF, 0x3B7373FF},
            {0x5AC5FFFF, 0x4AA4FFFF, 0x109CDEFF, 0x2378DCFF},
            {0xBEB9FAFF, 0x90B0FFFF, 0x699DC3FF, 0x578FC7FF},
            {0x55E6FFFF, 0x5AC5FFFF, 0x3FBFBFFF, 0x4AA4FFFF},
            {0xBED2F0FF, 0xBEB9FAFF, 0x8FABC7FF, 0x8F8FC7FF},
            {0x08DED5FF, 0x00BFFFFF, 0x109CDEFF, 0x007FFFFF},
            {0x109CDEFF, 0x007FFFFF, 0x0052F6FF, 0x004A9CFF},
            {0x699DC3FF, 0x4B7DC8FF, 0x326496FF, 0x004A9CFF},
            {0xB991FFFF, 0x786EF0FF, 0x4A5AFFFF, 0x3F3FBFFF},
            {0x786EF0FF, 0x4A5AFFFF, 0x3C3CF5FF, 0x3F3FBFFF},
            {0x4A5AFFFF, 0x6241F6FF, 0x3F3FBFFF, 0x5010B0FF},
            {0x4A5AFFFF, 0x3C3CF5FF, 0x101CDAFF, 0x0010BDFF},
            {0x3C3CF5FF, 0x101CDAFF, 0x00007FFF, 0x010101FF},
            {0x3C3CF5FF, 0x0010BDFF, 0x00007FFF, 0x010101FF},
            {0x3F3FBFFF, 0x231094FF, 0x00007FFF, 0x010101FF},
            {0x234146FF, 0x0C2148FF, 0x010101FF, 0x010101FF},
            {0x8732D2FF, 0x5010B0FF, 0x231094FF, 0x410062FF},
            {0x8732D2FF, 0x6010D0FF, 0x231094FF, 0x00007FFF},
            {0x9C41FFFF, 0x8732D2FF, 0x8C14BEFF, 0x5010B0FF},
            {0xBD62FFFF, 0x9C41FFFF, 0x8732D2FF, 0x6010D0FF},
            {0xBD29FFFF, 0x7F00FFFF, 0x5010B0FF, 0x00007FFF},
            {0xE673FFFF, 0xBD62FFFF, 0x8F57C7FF, 0x9C41FFFF},
            {0xD7A5FFFF, 0xB991FFFF, 0x8F8FC7FF, 0x7676CAFF},
            {0xD7C3FAFF, 0xD7A5FFFF, 0xB991FFFF, 0xAB8FC7FF},
            {0xD0DAF8FF, 0xD7C3FAFF, 0xABABE3FF, 0xAB8FC7FF},
            {0xFFDCF5FF, 0xF8C6FCFF, 0xE1B9D2FF, 0xEBACE1FF},
            {0xD7A5FFFF, 0xE673FFFF, 0xBD62FFFF, 0xAB57ABFF},
            {0xE673FFFF, 0xFF52FFFF, 0xBF3FBFFF, 0xDA20E0FF},
            {0xFF52FFFF, 0xDA20E0FF, 0xBD10C5FF, 0x7F007FFF},
            {0x9C41FFFF, 0xBD29FFFF, 0xBD10C5FF, 0x8C14BEFF},
            {0xDA20E0FF, 0xBD10C5FF, 0x8C14BEFF, 0x7F007FFF},
            {0x8732D2FF, 0x8C14BEFF, 0x7F007FFF, 0x410062FF},
            {0x573B73FF, 0x5A187BFF, 0x320A46FF, 0x410062FF},
            {0x724072FF, 0x641464FF, 0x320A46FF, 0x410062FF},
            {0x5A187BFF, 0x410062FF, 0x010101FF, 0x010101FF},
            {0x1F1F3BFF, 0x320A46FF, 0x010101FF, 0x010101FF},
            {0x573B3BFF, 0x551937FF, 0x280A1EFF, 0x010101FF},
            {0xBF3FBFFF, 0xA01982FF, 0x7F007FFF, 0x410062FF},
            {0xE61E78FF, 0xC80078FF, 0x7F007FFF, 0x7F0000FF},
            {0xFF6AC5FF, 0xFF50BFFF, 0xFC3A8CFF, 0xE61E78FF},
            {0xFD81FFFF, 0xFF6AC5FF, 0xFF50BFFF, 0xC35A91FF},
            {0xFFC0CBFF, 0xFAA0B9FF, 0xC78F8FFF, 0xC87DA0FF},
            {0xFF50BFFF, 0xFC3A8CFF, 0xE61E78FF, 0xBD1039FF},
            {0xFC3A8CFF, 0xE61E78FF, 0xC80078FF, 0xBD1039FF},
            {0xDA2010FF, 0xBD1039FF, 0x7F0000FF, 0x010101FF},
            {0x8E5555FF, 0x98344DFF, 0x911437FF, 0x551414FF},
            {0x98344DFF, 0x911437FF, 0x7F0000FF, 0x280A1EFF},
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
