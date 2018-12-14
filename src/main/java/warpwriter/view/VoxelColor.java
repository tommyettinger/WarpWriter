package warpwriter.view;

import com.badlogic.gdx.math.MathUtils;
import warpwriter.Coloring;

/**
 * @author Ben McLean
 */
public class VoxelColor implements IVoxelColor {
    public enum LightDirection {
        ABOVE_LEFT, ABOVE_RIGHT,
        LEFT_ABOVE, LEFT_BELOW, RIGHT_ABOVE, RIGHT_BELOW,
        BELOW_LEFT, BELOW_RIGHT;

        public boolean isAbove() {
            return this == ABOVE_RIGHT || this == ABOVE_LEFT;
        }

        public boolean isHorizontal() {
            return this == LEFT_ABOVE
                    || this == LEFT_BELOW
                    || this == RIGHT_ABOVE
                    || this == RIGHT_BELOW;
        }

        public boolean isBelow() {
            return this == BELOW_LEFT || this == BELOW_RIGHT;
        }

        public boolean isLeft() {
            return this == LEFT_ABOVE
                    || this == LEFT_BELOW
                    || this == ABOVE_LEFT
                    || this == BELOW_LEFT;
        }

        public boolean isRight() {
            return this == RIGHT_ABOVE
                    || this == RIGHT_BELOW
                    || this == ABOVE_RIGHT
                    || this == BELOW_RIGHT;
        }

        public LightDirection above() {
            switch (this) {
                case BELOW_LEFT:
                case LEFT_BELOW:
                case LEFT_ABOVE:
                    return ABOVE_LEFT;
                case BELOW_RIGHT:
                case RIGHT_BELOW:
                case RIGHT_ABOVE:
                    return ABOVE_RIGHT;
                default:
                    return this;
            }
        }

        public LightDirection horizontal() {
            switch (this) {
                case BELOW_LEFT:
                    return LEFT_BELOW;
                case ABOVE_LEFT:
                    return LEFT_ABOVE;
                case BELOW_RIGHT:
                    return RIGHT_BELOW;
                case ABOVE_RIGHT:
                    return RIGHT_ABOVE;
                default:
                    return this;
            }
        }

        public LightDirection below() {
            switch (this) {
                case ABOVE_LEFT:
                case LEFT_ABOVE:
                case LEFT_BELOW:
                    return BELOW_LEFT;
                case ABOVE_RIGHT:
                case RIGHT_ABOVE:
                case RIGHT_BELOW:
                    return BELOW_RIGHT;
                default:
                    return this;
            }
        }

        public LightDirection left() {
            switch (this) {
                case ABOVE_RIGHT:
                    return ABOVE_LEFT;
                case RIGHT_ABOVE:
                    return LEFT_ABOVE;
                case RIGHT_BELOW:
                    return LEFT_BELOW;
                case BELOW_RIGHT:
                    return BELOW_LEFT;
                default:
                    return this;
            }
        }

        public LightDirection right() {
            switch (this) {
                case ABOVE_LEFT:
                    return ABOVE_RIGHT;
                case LEFT_ABOVE:
                    return RIGHT_ABOVE;
                case LEFT_BELOW:
                    return RIGHT_BELOW;
                case BELOW_LEFT:
                    return BELOW_RIGHT;
                default:
                    return this;
            }
        }

        public LightDirection clock() {
            switch (this) {
                case ABOVE_RIGHT:
                    return RIGHT_ABOVE;
                case RIGHT_ABOVE:
                    return RIGHT_BELOW;
                case RIGHT_BELOW:
                    return BELOW_RIGHT;
                case BELOW_RIGHT:
                    return BELOW_LEFT;
                case BELOW_LEFT:
                    return LEFT_BELOW;
                case LEFT_BELOW:
                    return LEFT_ABOVE;
                case LEFT_ABOVE:
                    return ABOVE_LEFT;
                case ABOVE_LEFT:
                    return ABOVE_RIGHT;
                default:
                    return this;
            }
        }

        public LightDirection counter() {
            switch (this) {
                case ABOVE_RIGHT:
                    return ABOVE_LEFT;
                case ABOVE_LEFT:
                    return LEFT_ABOVE;
                case LEFT_ABOVE:
                    return LEFT_BELOW;
                case LEFT_BELOW:
                    return BELOW_LEFT;
                case BELOW_LEFT:
                    return BELOW_RIGHT;
                case BELOW_RIGHT:
                    return RIGHT_BELOW;
                case RIGHT_BELOW:
                    return RIGHT_ABOVE;
                case RIGHT_ABOVE:
                    return ABOVE_RIGHT;
                default:
                    return this;
            }
        }

        public LightDirection flipY() {
            switch (this) {
                case ABOVE_RIGHT:
                    return BELOW_RIGHT;
                case ABOVE_LEFT:
                    return BELOW_LEFT;
                case BELOW_RIGHT:
                    return ABOVE_RIGHT;
                case BELOW_LEFT:
                    return ABOVE_LEFT;
                case RIGHT_ABOVE:
                    return RIGHT_BELOW;
                case RIGHT_BELOW:
                    return RIGHT_ABOVE;
                case LEFT_ABOVE:
                    return LEFT_BELOW;
                case LEFT_BELOW:
                    return LEFT_ABOVE;
                default:
                    return this;
            }
        }

        public LightDirection flipZ() {
            switch (this) {
                case ABOVE_RIGHT:
                    return ABOVE_LEFT;
                case BELOW_RIGHT:
                    return BELOW_LEFT;
                case ABOVE_LEFT:
                    return ABOVE_RIGHT;
                case BELOW_LEFT:
                    return BELOW_RIGHT;
                case RIGHT_BELOW:
                    return LEFT_BELOW;
                case RIGHT_ABOVE:
                    return LEFT_ABOVE;
                case LEFT_BELOW:
                    return RIGHT_BELOW;
                case LEFT_ABOVE:
                    return RIGHT_ABOVE;
                default:
                    return this;
            }
        }

        public LightDirection opposite() {
            switch (this) {
                case ABOVE_RIGHT:
                    return BELOW_LEFT;
                case RIGHT_ABOVE:
                    return LEFT_BELOW;
                case RIGHT_BELOW:
                    return LEFT_ABOVE;
                case BELOW_RIGHT:
                    return ABOVE_LEFT;
                case ABOVE_LEFT:
                    return BELOW_RIGHT;
                case LEFT_BELOW:
                    return RIGHT_ABOVE;
                case LEFT_ABOVE:
                    return RIGHT_BELOW;
                case BELOW_LEFT:
                    return ABOVE_RIGHT;
                default:
                    return this;
            }
        }
    }

    protected LightDirection lightDirection = LightDirection.ABOVE_RIGHT;

    public LightDirection direction() {
        return lightDirection;
    }

    public VoxelColor set(LightDirection lightDirection) {
        this.lightDirection = lightDirection;
        return this;
    }

    public interface ITwilight {
        int dark(byte voxel);

        int dim(byte voxel);

        int twilight(byte voxel);

        int bright(byte voxel);

        int light(int brightness, byte voxel);
    }

    public static abstract class Twilight implements ITwilight {
        @Override
        public int bright(byte voxel) {
            return light(3, voxel);
        }

        @Override
        public int twilight(byte voxel) {
            return light(2, voxel);
        }

        @Override
        public int dim(byte voxel) {
            return light(1, voxel);
        }

        @Override
        public int dark(byte voxel) {
            return light(0, voxel);
        }

        @Override
        public int light(int brightness, byte voxel) {
            if (voxel == 0) return 0;// 0 is equivalent to Color.rgba8888(Color.CLEAR)
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

    /**
     * Bytes that correspond to palette indices to use when shading an Aurora-palette model.
     * The color in index 1 of each 4-element sub-array is the "twilight" color and the one that will match the voxel
     * color used in a model. The color at index 0 is "bright", index 2 is "dim", and index 3 is "dark". Normal usage
     * with a renderer will use {@link #RAMP_VALUES}; this array would be used to figure out what indices are related to
     * another color for the purpose of procedurally using similar colors with different lightness.
     * To visualize this, <a href="https://i.imgur.com/bkf0Eqt.png">use this image</a>, with the first 32 items in RAMPS
     * corresponding to the first column from top to bottom, the next 32 items in the second column, etc.
     */
    public static final byte[][] RAMPS = new byte[][]{
            {0, 0, 0, 0},
            {2, 1, 1, 1},
            {87, 2, 1, 1},
            {103, 3, 87, 2},
            {5, 4, 103, 3},
            {6, 5, 4, 103},
            {7, 6, 5, 4},
            {8, 7, 6, 5},
            {9, 8, 7, 6},
            {10, 9, 8, 7},
            {11, 10, 9, 8},
            {12, 11, 10, 9},
            {13, 12, 11, 10},
            {14, 13, 12, 11},
            {15, 14, 13, 12},
            {15, 15, 14, 13},
            {-66, 16, -55, 88},
            {-59, 17, 106, 51},
            {-59, 18, -57, 17},
            {15, 19, 109, 108},
            {-45, 20, 123, 122},
            {-35, 21, -34, 23},
            {-38, 22, -53, -54},
            {-33, 23, 24, 1},
            {-32, 24, 117, 1},
            {-10, 25, -14, -13},
            {-21, 26, -120, -121},
            {-19, 27, -17, -10},
            {-118, 28, -113, -119},
            {67, 29, -115, 66},
            {-6, 30, 55, 54},
            {-102, 31, -104, -105},
            {-103, 32, -2, 62},
            {-105, 33, -106, 33},
            {-93, 34, -107, -108},
            {-94, 35, -93, 72},
            {69, 36, 70, 74},
            {-98, 37, -96, 36},
            {59, 38, 58, 57},
            {15, 39, 80, 79},
            {39, 40, -85, -87},
            {-87, 41, 76, 75},
            {-89, 42, -91, -92},
            {-75, 43, -76, -77},
            {-71, 44, 92, 85},
            {-71, 45, -73, -74},
            {39, 46, 98, 96},
            {12, 47, 11, 10},
            {68, 48, 69, 49},
            {11, 49, 9, 8},
            {100, 50, 51, 7},
            {50, 51, 7, 6},
            {-90, 52, 6, 5},
            {64, 53, 63, 71},
            {55, 54, 64, 53},
            {56, 55, 54, 64},
            {57, 56, 69, 55},
            {58, 57, 48, 69},
            {59, 58, 57, 48},
            {60, 59, 68, 66},
            {80, 60, 13, 79},
            {5, 61, 72, 3},
            {71, 62, 61, 72},
            {53, 63, 71, 62},
            {54, 64, 53, 70},
            {48, 65, 64, 9},
            {68, 66, 48, 49},
            {60, 67, -116, 12},
            {79, 68, 78, 48},
            {48, 69, 75, -90},
            {-90, 70, 52, 71},
            {74, 71, 86, 61},
            {73, 72, 3, 87},
            {86, 73, -78, 103},
            {-90, 74, 71, 86},
            {76, 75, -90, 52},
            {77, 76, 75, -90},
            {78, 77, 76, 49},
            {79, 78, 82, 77},
            {80, 79, 78, 82},
            {39, 80, 79, 12},
            {80, 81, 96, 82},
            {81, 82, 95, 100},
            {82, 83, 93, 75},
            {93, 84, 85, 90},
            {84, 85, 90, 89},
            {90, 86, 73, 4},
            {103, 87, 2, 1},
            {89, 88, -67, 103},
            {90, 89, 88, 4},
            {101, 90, 89, 86},
            {101, 91, 89, 88},
            {93, 92, 85, 101},
            {100, 93, 84, 92},
            {95, 94, 93, 92},
            {96, 95, 100, 93},
            {99, 96, 95, 100},
            {15, 97, 109, 13},
            {109, 98, 99, 96},
            {98, 99, 96, 11},
            {95, 100, 93, 50},
            {92, 101, 91, 90},
            {-68, 102, 88, -67},
            {4, 103, 87, 2},
            {88, 104, -67, 103},
            {101, 105, 116, 89},
            {107, 106, 50, 51},
            {108, 107, 113, 100},
            {109, 108, 111, 107},
            {97, 109, 98, 99},
            {126, 110, 111, 112},
            {110, 111, 112, 113},
            {111, 112, 113, 10},
            {112, 113, 50, 51},
            {-47, 114, 115, 105},
            {51, 115, 105, 116},
            {105, 116, 104, -67},
            {118, 117, 2, 1},
            {104, 118, 117, 2},
            {5, 119, 4, -124},
            {121, 120, 119, 4},
            {115, 121, 120, 119},
            {124, 122, 115, -126},
            {124, 123, 122, 115},
            {113, 124, 50, 51},
            {112, 125, 113, 124},
            {127, 126, 110, 12},
            {97, 127, 126, 13},
            {125, -128, 9, 8},
            {-26, -127, -126, 121},
            {122, -126, 6, -122},
            {120, -125, 119, -123},
            {-123, -124, 3, -109},
            {5, -123, 4, -124},
            {-126, -122, -125, -123},
            {-119, -121, -126, -122},
            {-119, -120, -121, -126},
            {-113, -119, 8, -121},
            {-22, -118, -114, 47},
            {15, -117, 67, -116},
            {67, -116, -115, 12},
            {-116, -115, 47, 10},
            {-115, -114, -113, 49},
            {-114, -113, 9, 8},
            {-113, -112, 64, 8},
            {-112, -111, -121, 63},
            {61, -110, 72, -109},
            {72, -109, -108, 2},
            {-109, -108, 1, 1},
            {72, -107, -108, 2},
            {-93, -106, 34, -107},
            {32, -105, 33, -106},
            {-102, -104, -105, -1},
            {-100, -103, 32, 63},
            {-101, -102, -95, -104},
            {-100, -101, -103, 32},
            {30, -100, -103, 54},
            {-85, -99, 41, 69},
            {-99, -98, 56, 69},
            {-99, -97, -89, 36},
            {-97, -96, -95, -94},
            {-96, -95, -94, 35},
            {-95, -94, 35, -93},
            {62, -93, 72, -107},
            {-91, -92, 73, -78},
            {42, -91, -92, 73},
            {75, -90, 52, 90},
            {-88, -89, 42, 74},
            {41, -88, -89, 76},
            {-85, -87, 41, 77},
            {40, -86, -99, 41},
            {40, -85, -87, 78},
            {-85, -84, -83, 78},
            {-84, -83, 83, 100},
            {-83, -82, 83, 76},
            {-82, -81, -80, 84},
            {-81, -80, 85, 90},
            {85, -79, 86, -77},
            {-77, -78, 87, 2},
            {86, -77, -78, 87},
            {-79, -76, -77, 103},
            {44, -75, 43, -79},
            {-73, -74, -70, -75},
            {45, -73, -74, -70},
            {46, -72, 95, 100},
            {-72, -71, 94, 44},
            {-74, -70, -75, -68},
            {-64, -69, -68, 102},
            {-69, -68, 102, 88},
            {104, -67, 103, 117},
            {-65, -66, -68, 105},
            {-63, -65, -66, -68},
            {-63, -64, -69, -68},
            {-62, -63, 17, 94},
            {-61, -62, -63, 94},
            {19, -61, 107, 100},
            {19, -60, -58, 107},
            {-60, -59, -58, 106},
            {-60, -58, 107, 113},
            {18, -57, 17, -66},
            {-42, -56, -48, -49},
            {16, -55, -67, 117},
            {104, -54, -32, 117},
            {-51, -53, -54, -32},
            {-49, -52, -53, -54},
            {115, -51, 116, 104},
            {-41, -50, -52, -53},
            {-48, -49, -51, -55},
            {-56, -48, -49, -51},
            {113, -47, 51, 115},
            {-44, -46, 114, 115},
            {111, -45, 113, 124},
            {-59, -44, -47, 50},
            {-23, -43, 112, 125},
            {18, -42, -56, -66},
            {-56, -41, -48, -49},
            {114, -40, 115, -51},
            {20, -39, 123, 122},
            {-39, -38, 22, 120},
            {-39, -37, 22, -125},
            {-38, -36, 22, -53},
            {-36, -35, -34, 23},
            {-35, -34, 23, 24},
            {22, -33, 24, 1},
            {-54, -32, 117, 1},
            {-30, -31, -13, 24},
            {-29, -30, -31, -13},
            {-28, -29, -125, 119},
            {-26, -28, -29, -126},
            {-18, -27, -30, -31},
            {-21, -26, -127, 122},
            {-24, -25, -128, 124},
            {-23, -24, 125, -128},
            {126, -23, 12, 47},
            {-117, -22, -116, -115},
            {28, -21, -119, 8},
            {28, -20, 26, -120},
            {-20, -19, -17, 26},
            {-26, -18, -29, -122},
            {-19, -17, -10, -14},
            {-29, -16, -15, -12},
            {-125, -15, -12, -108},
            {-122, -14, -11, -12},
            {-15, -13, -12, -108},
            {-124, -12, -108, 1},
            {-110, -11, -109, -108},
            {26, -10, -14, -11},
            {-4, -9, -10, -1},
            {-7, -8, -111, -121},
            {28, -7, -112, -119},
            {29, -6, 66, -114},
            {-8, -5, -111, -2},
            {-5, -4, -2, -3},
            {32, -3, -1, 34},
            {63, -2, 62, 61},
            {-2, -1, 34, -107},
    };
    /**
     * Color values as RGBA8888 ints to use when shading an Aurora-palette model.
     * The color in index 1 of each 4-element sub-array is the "twilight" color and the one that will match the voxel
     * color used in a model. The color at index 0 is "bright", index 2 is "dim", and index 3 is "dark".
     * To visualize this, <a href="https://i.imgur.com/bkf0Eqt.png">use this image</a>, with the first 32 items in RAMPS
     * corresponding to the first column from top to bottom, the next 32 items in the second column, etc.
     */
    public static final int[][] RAMP_VALUES = new int[][]{
            {0x00000000, 0x00000000, 0x00000000, 0x00000000},
            {0x131313FF, 0x010101FF, 0x010101FF, 0x010101FF},
            {0x191E0FFF, 0x131313FF, 0x010101FF, 0x010101FF},
            {0x1E2D23FF, 0x252525FF, 0x191E0FFF, 0x131313FF},
            {0x494949FF, 0x373737FF, 0x1E2D23FF, 0x252525FF},
            {0x5B5B5BFF, 0x494949FF, 0x373737FF, 0x1E2D23FF},
            {0x6E6E6EFF, 0x5B5B5BFF, 0x494949FF, 0x373737FF},
            {0x808080FF, 0x6E6E6EFF, 0x5B5B5BFF, 0x494949FF},
            {0x929292FF, 0x808080FF, 0x6E6E6EFF, 0x5B5B5BFF},
            {0xA4A4A4FF, 0x929292FF, 0x808080FF, 0x6E6E6EFF},
            {0xB6B6B6FF, 0xA4A4A4FF, 0x929292FF, 0x808080FF},
            {0xC9C9C9FF, 0xB6B6B6FF, 0xA4A4A4FF, 0x929292FF},
            {0xDBDBDBFF, 0xC9C9C9FF, 0xB6B6B6FF, 0xA4A4A4FF},
            {0xEDEDEDFF, 0xDBDBDBFF, 0xC9C9C9FF, 0xB6B6B6FF},
            {0xFFFFFFFF, 0xEDEDEDFF, 0xDBDBDBFF, 0xC9C9C9FF},
            {0xFFFFFFFF, 0xFFFFFFFF, 0xEDEDEDFF, 0xDBDBDBFF},
            {0x129880FF, 0x007F7FFF, 0x055A5CFF, 0x235037FF},
            {0x55E6FFFF, 0x3FBFBFFF, 0x64ABABFF, 0x6E8287FF},
            {0x55E6FFFF, 0x00FFFFFF, 0x08DED5FF, 0x3FBFBFFF},
            {0xFFFFFFFF, 0xBFFFFFFF, 0xC7F1F1FF, 0xABE3E3FF},
            {0x90B0FFFF, 0x8181FFFF, 0x7676CAFF, 0x736EAAFF},
            {0x101CDAFF, 0x0000FFFF, 0x0010BDFF, 0x00007FFF},
            {0x4A5AFFFF, 0x3F3FBFFF, 0x0F377DFF, 0x162C52FF},
            {0x231094FF, 0x00007FFF, 0x0F0F50FF, 0x010101FF},
            {0x0C2148FF, 0x0F0F50FF, 0x0F192DFF, 0x010101FF},
            {0xA01982FF, 0x7F007FFF, 0x641464FF, 0x410062FF},
            {0xE673FFFF, 0xBF3FBFFF, 0xAB57ABFF, 0x8F578FFF},
            {0xDA20E0FF, 0xF500F5FF, 0xBD10C5FF, 0xA01982FF},
            {0xEBACE1FF, 0xFD81FFFF, 0xC78FB9FF, 0xAB73ABFF},
            {0xF8D2DAFF, 0xFFC0CBFF, 0xE1B9D2FF, 0xE3ABABFF},
            {0xFAA0B9FF, 0xFF8181FF, 0xD08A74FF, 0xC07872FF},
            {0xFF3C0AFF, 0xFF0000FF, 0xDA2010FF, 0xA5140AFF},
            {0xD5524AFF, 0xBF3F3FFF, 0x98344DFF, 0x73413CFF},
            {0xA5140AFF, 0x7F0000FF, 0x621800FF, 0x7F0000FF},
            {0x5F3214FF, 0x551414FF, 0x401811FF, 0x280A1EFF},
            {0xA04B05FF, 0x7F3F00FF, 0x5F3214FF, 0x3B2D1FFF},
            {0xC49E73FF, 0xBF7F3FFF, 0x8F7357FF, 0x73733BFF},
            {0xFFA53CFF, 0xFF7F00FF, 0xDA6E0AFF, 0xBF7F3FFF},
            {0xF6C8AFFF, 0xFFBF81FF, 0xF5B99BFF, 0xEBAA8CFF},
            {0xFFFFFFFF, 0xFFFFBFFF, 0xEDEDC7FF, 0xDADAABFF},
            {0xFFFFBFFF, 0xFFFF00FF, 0xFFEA4AFF, 0xE6D55AFF},
            {0xE6D55AFF, 0xBFBF3FFF, 0xA2A255FF, 0x8F8F57FF},
            {0xAC9400FF, 0x7F7F00FF, 0x626200FF, 0x53500AFF},
            {0x149605FF, 0x007F00FF, 0x0C5C0CFF, 0x204608FF},
            {0x4BF05AFF, 0x3FBF3FFF, 0x578F57FF, 0x587D3EFF},
            {0x4BF05AFF, 0x00FF00FF, 0x14E60AFF, 0x0AD70AFF},
            {0xFFFFBFFF, 0xAFFFAFFF, 0xB4EECAFF, 0xA2D8A2FF},
            {0xC9C9C9FF, 0xBCAFC0FF, 0xB6B6B6FF, 0xA4A4A4FF},
            {0xE3C7ABFF, 0xCBAA89FF, 0xC49E73FF, 0xA6A090FF},
            {0xB6B6B6FF, 0xA6A090FF, 0x929292FF, 0x808080FF},
            {0x87B48EFF, 0x7E9494FF, 0x6E8287FF, 0x6E6E6EFF},
            {0x7E9494FF, 0x6E8287FF, 0x6E6E6EFF, 0x5B5B5BFF},
            {0x8C805AFF, 0x7E6E60FF, 0x5B5B5BFF, 0x494949FF},
            {0xAB7373FF, 0xA0695FFF, 0x8E5555FF, 0x73573BFF},
            {0xD08A74FF, 0xC07872FF, 0xAB7373FF, 0xA0695FFF},
            {0xE19B7DFF, 0xD08A74FF, 0xC07872FF, 0xAB7373FF},
            {0xEBAA8CFF, 0xE19B7DFF, 0xC49E73FF, 0xD08A74FF},
            {0xF5B99BFF, 0xEBAA8CFF, 0xCBAA89FF, 0xC49E73FF},
            {0xF6C8AFFF, 0xF5B99BFF, 0xEBAA8CFF, 0xCBAA89FF},
            {0xF5E1D2FF, 0xF6C8AFFF, 0xE3C7ABFF, 0xE3ABABFF},
            {0xEDEDC7FF, 0xF5E1D2FF, 0xDBDBDBFF, 0xDADAABFF},
            {0x494949FF, 0x573B3BFF, 0x3B2D1FFF, 0x252525FF},
            {0x73573BFF, 0x73413CFF, 0x573B3BFF, 0x3B2D1FFF},
            {0xA0695FFF, 0x8E5555FF, 0x73573BFF, 0x73413CFF},
            {0xC07872FF, 0xAB7373FF, 0xA0695FFF, 0x8F7357FF},
            {0xCBAA89FF, 0xC78F8FFF, 0xAB7373FF, 0x929292FF},
            {0xE3C7ABFF, 0xE3ABABFF, 0xCBAA89FF, 0xA6A090FF},
            {0xF5E1D2FF, 0xF8D2DAFF, 0xE3C7E3FF, 0xC9C9C9FF},
            {0xDADAABFF, 0xE3C7ABFF, 0xC7C78FFF, 0xCBAA89FF},
            {0xCBAA89FF, 0xC49E73FF, 0x8F8F57FF, 0x8C805AFF},
            {0x8C805AFF, 0x8F7357FF, 0x7E6E60FF, 0x73573BFF},
            {0x73733BFF, 0x73573BFF, 0x465032FF, 0x573B3BFF},
            {0x414123FF, 0x3B2D1FFF, 0x252525FF, 0x191E0FFF},
            {0x465032FF, 0x414123FF, 0x283405FF, 0x1E2D23FF},
            {0x8C805AFF, 0x73733BFF, 0x73573BFF, 0x465032FF},
            {0xA2A255FF, 0x8F8F57FF, 0x8C805AFF, 0x7E6E60FF},
            {0xB5B572FF, 0xA2A255FF, 0x8F8F57FF, 0x8C805AFF},
            {0xC7C78FFF, 0xB5B572FF, 0xA2A255FF, 0xA6A090FF},
            {0xDADAABFF, 0xC7C78FFF, 0xABC78FFF, 0xB5B572FF},
            {0xEDEDC7FF, 0xDADAABFF, 0xC7C78FFF, 0xABC78FFF},
            {0xFFFFBFFF, 0xEDEDC7FF, 0xDADAABFF, 0xC9C9C9FF},
            {0xEDEDC7FF, 0xC7E3ABFF, 0xA2D8A2FF, 0xABC78FFF},
            {0xC7E3ABFF, 0xABC78FFF, 0x8FC78FFF, 0x87B48EFF},
            {0xABC78FFF, 0x8EBE55FF, 0x73AB73FF, 0x8F8F57FF},
            {0x73AB73FF, 0x738F57FF, 0x587D3EFF, 0x506450FF},
            {0x738F57FF, 0x587D3EFF, 0x506450FF, 0x3B573BFF},
            {0x506450FF, 0x465032FF, 0x414123FF, 0x373737FF},
            {0x1E2D23FF, 0x191E0FFF, 0x131313FF, 0x010101FF},
            {0x3B573BFF, 0x235037FF, 0x123832FF, 0x1E2D23FF},
            {0x506450FF, 0x3B573BFF, 0x235037FF, 0x373737FF},
            {0x507D5FFF, 0x506450FF, 0x3B573BFF, 0x465032FF},
            {0x507D5FFF, 0x3B7349FF, 0x3B573BFF, 0x235037FF},
            {0x73AB73FF, 0x578F57FF, 0x587D3EFF, 0x507D5FFF},
            {0x87B48EFF, 0x73AB73FF, 0x738F57FF, 0x578F57FF},
            {0x8FC78FFF, 0x64C082FF, 0x73AB73FF, 0x578F57FF},
            {0xA2D8A2FF, 0x8FC78FFF, 0x87B48EFF, 0x73AB73FF},
            {0xABE3C5FF, 0xA2D8A2FF, 0x8FC78FFF, 0x87B48EFF},
            {0xFFFFFFFF, 0xE1F8FAFF, 0xC7F1F1FF, 0xDBDBDBFF},
            {0xC7F1F1FF, 0xB4EECAFF, 0xABE3C5FF, 0xA2D8A2FF},
            {0xB4EECAFF, 0xABE3C5FF, 0xA2D8A2FF, 0xB6B6B6FF},
            {0x8FC78FFF, 0x87B48EFF, 0x73AB73FF, 0x7E9494FF},
            {0x578F57FF, 0x507D5FFF, 0x3B7349FF, 0x506450FF},
            {0x1C8C4EFF, 0x0F6946FF, 0x235037FF, 0x123832FF},
            {0x373737FF, 0x1E2D23FF, 0x191E0FFF, 0x131313FF},
            {0x235037FF, 0x234146FF, 0x123832FF, 0x1E2D23FF},
            {0x507D5FFF, 0x3B7373FF, 0x3B5773FF, 0x3B573BFF},
            {0x8FC7C7FF, 0x64ABABFF, 0x7E9494FF, 0x6E8287FF},
            {0xABE3E3FF, 0x8FC7C7FF, 0x8FABC7FF, 0x87B48EFF},
            {0xC7F1F1FF, 0xABE3E3FF, 0xABC7E3FF, 0x8FC7C7FF},
            {0xE1F8FAFF, 0xC7F1F1FF, 0xB4EECAFF, 0xABE3C5FF},
            {0xD0DAF8FF, 0xBED2F0FF, 0xABC7E3FF, 0xA8B9DCFF},
            {0xBED2F0FF, 0xABC7E3FF, 0xA8B9DCFF, 0x8FABC7FF},
            {0xABC7E3FF, 0xA8B9DCFF, 0x8FABC7FF, 0xA4A4A4FF},
            {0xA8B9DCFF, 0x8FABC7FF, 0x7E9494FF, 0x6E8287FF},
            {0x699DC3FF, 0x578FC7FF, 0x57738FFF, 0x3B7373FF},
            {0x6E8287FF, 0x57738FFF, 0x3B7373FF, 0x3B5773FF},
            {0x3B7373FF, 0x3B5773FF, 0x234146FF, 0x123832FF},
            {0x1F1F3BFF, 0x0F192DFF, 0x131313FF, 0x010101FF},
            {0x234146FF, 0x1F1F3BFF, 0x0F192DFF, 0x131313FF},
            {0x494949FF, 0x3B3B57FF, 0x373737FF, 0x3C233CFF},
            {0x57578FFF, 0x494973FF, 0x3B3B57FF, 0x373737FF},
            {0x57738FFF, 0x57578FFF, 0x494973FF, 0x3B3B57FF},
            {0x8F8FC7FF, 0x736EAAFF, 0x57738FFF, 0x73578FFF},
            {0x8F8FC7FF, 0x7676CAFF, 0x736EAAFF, 0x57738FFF},
            {0x8FABC7FF, 0x8F8FC7FF, 0x7E9494FF, 0x6E8287FF},
            {0xA8B9DCFF, 0xABABE3FF, 0x8FABC7FF, 0x8F8FC7FF},
            {0xE3E3FFFF, 0xD0DAF8FF, 0xBED2F0FF, 0xC9C9C9FF},
            {0xE1F8FAFF, 0xE3E3FFFF, 0xD0DAF8FF, 0xDBDBDBFF},
            {0xABABE3FF, 0xAB8FC7FF, 0x929292FF, 0x808080FF},
            {0xBD62FFFF, 0x8F57C7FF, 0x73578FFF, 0x57578FFF},
            {0x736EAAFF, 0x73578FFF, 0x5B5B5BFF, 0x724072FF},
            {0x494973FF, 0x573B73FF, 0x3B3B57FF, 0x463246FF},
            {0x463246FF, 0x3C233CFF, 0x252525FF, 0x321623FF},
            {0x494949FF, 0x463246FF, 0x373737FF, 0x3C233CFF},
            {0x73578FFF, 0x724072FF, 0x573B73FF, 0x463246FF},
            {0xAB73ABFF, 0x8F578FFF, 0x73578FFF, 0x724072FF},
            {0xAB73ABFF, 0xAB57ABFF, 0x8F578FFF, 0x73578FFF},
            {0xC78FB9FF, 0xAB73ABFF, 0x808080FF, 0x8F578FFF},
            {0xF8C6FCFF, 0xEBACE1FF, 0xD7A0BEFF, 0xBCAFC0FF},
            {0xFFFFFFFF, 0xFFDCF5FF, 0xF8D2DAFF, 0xE3C7E3FF},
            {0xF8D2DAFF, 0xE3C7E3FF, 0xE1B9D2FF, 0xC9C9C9FF},
            {0xE3C7E3FF, 0xE1B9D2FF, 0xBCAFC0FF, 0xA4A4A4FF},
            {0xE1B9D2FF, 0xD7A0BEFF, 0xC78FB9FF, 0xA6A090FF},
            {0xD7A0BEFF, 0xC78FB9FF, 0x929292FF, 0x808080FF},
            {0xC78FB9FF, 0xC87DA0FF, 0xAB7373FF, 0x808080FF},
            {0xC87DA0FF, 0xC35A91FF, 0x8F578FFF, 0x8E5555FF},
            {0x573B3BFF, 0x4B2837FF, 0x3B2D1FFF, 0x321623FF},
            {0x3B2D1FFF, 0x321623FF, 0x280A1EFF, 0x131313FF},
            {0x321623FF, 0x280A1EFF, 0x010101FF, 0x010101FF},
            {0x3B2D1FFF, 0x401811FF, 0x280A1EFF, 0x131313FF},
            {0x5F3214FF, 0x621800FF, 0x551414FF, 0x401811FF},
            {0xBF3F3FFF, 0xA5140AFF, 0x7F0000FF, 0x621800FF},
            {0xFF3C0AFF, 0xDA2010FF, 0xA5140AFF, 0x911437FF},
            {0xFF6262FF, 0xD5524AFF, 0xBF3F3FFF, 0x8E5555FF},
            {0xF55A32FF, 0xFF3C0AFF, 0xB45A00FF, 0xDA2010FF},
            {0xFF6262FF, 0xF55A32FF, 0xD5524AFF, 0xBF3F3FFF},
            {0xFF8181FF, 0xFF6262FF, 0xD5524AFF, 0xC07872FF},
            {0xFFEA4AFF, 0xF6BD31FF, 0xBFBF3FFF, 0xC49E73FF},
            {0xF6BD31FF, 0xFFA53CFF, 0xE19B7DFF, 0xC49E73FF},
            {0xF6BD31FF, 0xD79B0FFF, 0xAC9400FF, 0xBF7F3FFF},
            {0xD79B0FFF, 0xDA6E0AFF, 0xB45A00FF, 0xA04B05FF},
            {0xDA6E0AFF, 0xB45A00FF, 0xA04B05FF, 0x7F3F00FF},
            {0xB45A00FF, 0xA04B05FF, 0x7F3F00FF, 0x5F3214FF},
            {0x73413CFF, 0x5F3214FF, 0x3B2D1FFF, 0x401811FF},
            {0x626200FF, 0x53500AFF, 0x414123FF, 0x283405FF},
            {0x7F7F00FF, 0x626200FF, 0x53500AFF, 0x414123FF},
            {0x8F8F57FF, 0x8C805AFF, 0x7E6E60FF, 0x506450FF},
            {0xB1B10AFF, 0xAC9400FF, 0x7F7F00FF, 0x73733BFF},
            {0xBFBF3FFF, 0xB1B10AFF, 0xAC9400FF, 0xA2A255FF},
            {0xFFEA4AFF, 0xE6D55AFF, 0xBFBF3FFF, 0xB5B572FF},
            {0xFFFF00FF, 0xFFD510FF, 0xF6BD31FF, 0xBFBF3FFF},
            {0xFFFF00FF, 0xFFEA4AFF, 0xE6D55AFF, 0xC7C78FFF},
            {0xFFEA4AFF, 0xC8FF41FF, 0x9BF046FF, 0xC7C78FFF},
            {0xC8FF41FF, 0x9BF046FF, 0x8EBE55FF, 0x87B48EFF},
            {0x9BF046FF, 0x96DC19FF, 0x8EBE55FF, 0xA2A255FF},
            {0x96DC19FF, 0x73C805FF, 0x6AA805FF, 0x738F57FF},
            {0x73C805FF, 0x6AA805FF, 0x587D3EFF, 0x506450FF},
            {0x587D3EFF, 0x3C6E14FF, 0x465032FF, 0x204608FF},
            {0x204608FF, 0x283405FF, 0x191E0FFF, 0x131313FF},
            {0x465032FF, 0x204608FF, 0x283405FF, 0x191E0FFF},
            {0x3C6E14FF, 0x0C5C0CFF, 0x204608FF, 0x1E2D23FF},
            {0x3FBF3FFF, 0x149605FF, 0x007F00FF, 0x3C6E14FF},
            {0x14E60AFF, 0x0AD70AFF, 0x00C514FF, 0x149605FF},
            {0x00FF00FF, 0x14E60AFF, 0x0AD70AFF, 0x00C514FF},
            {0xAFFFAFFF, 0x7DFF73FF, 0x8FC78FFF, 0x87B48EFF},
            {0x7DFF73FF, 0x4BF05AFF, 0x64C082FF, 0x3FBF3FFF},
            {0x0AD70AFF, 0x00C514FF, 0x149605FF, 0x1C8C4EFF},
            {0x00DE6AFF, 0x05B450FF, 0x1C8C4EFF, 0x0F6946FF},
            {0x05B450FF, 0x1C8C4EFF, 0x0F6946FF, 0x235037FF},
            {0x234146FF, 0x123832FF, 0x1E2D23FF, 0x0F192DFF},
            {0x06C491FF, 0x129880FF, 0x1C8C4EFF, 0x3B7373FF},
            {0x2DEBA8FF, 0x06C491FF, 0x129880FF, 0x1C8C4EFF},
            {0x2DEBA8FF, 0x00DE6AFF, 0x05B450FF, 0x1C8C4EFF},
            {0x3CFEA5FF, 0x2DEBA8FF, 0x3FBFBFFF, 0x64C082FF},
            {0x6AFFCDFF, 0x3CFEA5FF, 0x2DEBA8FF, 0x64C082FF},
            {0xBFFFFFFF, 0x6AFFCDFF, 0x8FC7C7FF, 0x87B48EFF},
            {0xBFFFFFFF, 0x91EBFFFF, 0x7DD7F0FF, 0x8FC7C7FF},
            {0x91EBFFFF, 0x55E6FFFF, 0x7DD7F0FF, 0x64ABABFF},
            {0x91EBFFFF, 0x7DD7F0FF, 0x8FC7C7FF, 0x8FABC7FF},
            {0x00FFFFFF, 0x08DED5FF, 0x3FBFBFFF, 0x129880FF},
            {0x00BFFFFF, 0x109CDEFF, 0x2378DCFF, 0x186ABDFF},
            {0x007F7FFF, 0x055A5CFF, 0x123832FF, 0x0F192DFF},
            {0x234146FF, 0x162C52FF, 0x0C2148FF, 0x0F192DFF},
            {0x326496FF, 0x0F377DFF, 0x162C52FF, 0x0C2148FF},
            {0x186ABDFF, 0x004A9CFF, 0x0F377DFF, 0x162C52FF},
            {0x57738FFF, 0x326496FF, 0x3B5773FF, 0x234146FF},
            {0x007FFFFF, 0x0052F6FF, 0x004A9CFF, 0x0F377DFF},
            {0x2378DCFF, 0x186ABDFF, 0x326496FF, 0x055A5CFF},
            {0x109CDEFF, 0x2378DCFF, 0x186ABDFF, 0x326496FF},
            {0x8FABC7FF, 0x699DC3FF, 0x6E8287FF, 0x57738FFF},
            {0x5AC5FFFF, 0x4AA4FFFF, 0x578FC7FF, 0x57738FFF},
            {0xABC7E3FF, 0x90B0FFFF, 0x8FABC7FF, 0x8F8FC7FF},
            {0x55E6FFFF, 0x5AC5FFFF, 0x699DC3FF, 0x7E9494FF},
            {0xD7C3FAFF, 0xBEB9FAFF, 0xA8B9DCFF, 0xABABE3FF},
            {0x00FFFFFF, 0x00BFFFFF, 0x109CDEFF, 0x129880FF},
            {0x109CDEFF, 0x007FFFFF, 0x2378DCFF, 0x186ABDFF},
            {0x578FC7FF, 0x4B7DC8FF, 0x57738FFF, 0x326496FF},
            {0x8181FFFF, 0x786EF0FF, 0x7676CAFF, 0x736EAAFF},
            {0x786EF0FF, 0x4A5AFFFF, 0x3F3FBFFF, 0x494973FF},
            {0x786EF0FF, 0x6241F6FF, 0x3F3FBFFF, 0x573B73FF},
            {0x4A5AFFFF, 0x3C3CF5FF, 0x3F3FBFFF, 0x0F377DFF},
            {0x3C3CF5FF, 0x101CDAFF, 0x0010BDFF, 0x00007FFF},
            {0x101CDAFF, 0x0010BDFF, 0x00007FFF, 0x0F0F50FF},
            {0x3F3FBFFF, 0x231094FF, 0x0F0F50FF, 0x010101FF},
            {0x162C52FF, 0x0C2148FF, 0x0F192DFF, 0x010101FF},
            {0x6010D0FF, 0x5010B0FF, 0x410062FF, 0x0F0F50FF},
            {0x8732D2FF, 0x6010D0FF, 0x5010B0FF, 0x410062FF},
            {0x9C41FFFF, 0x8732D2FF, 0x573B73FF, 0x3B3B57FF},
            {0xBD62FFFF, 0x9C41FFFF, 0x8732D2FF, 0x73578FFF},
            {0xBD29FFFF, 0x7F00FFFF, 0x6010D0FF, 0x5010B0FF},
            {0xE673FFFF, 0xBD62FFFF, 0x8F57C7FF, 0x736EAAFF},
            {0xD7A5FFFF, 0xB991FFFF, 0xAB8FC7FF, 0x8F8FC7FF},
            {0xD7C3FAFF, 0xD7A5FFFF, 0xABABE3FF, 0xAB8FC7FF},
            {0xD0DAF8FF, 0xD7C3FAFF, 0xC9C9C9FF, 0xBCAFC0FF},
            {0xFFDCF5FF, 0xF8C6FCFF, 0xE3C7E3FF, 0xE1B9D2FF},
            {0xFD81FFFF, 0xE673FFFF, 0xAB73ABFF, 0x808080FF},
            {0xFD81FFFF, 0xFF52FFFF, 0xBF3FBFFF, 0xAB57ABFF},
            {0xFF52FFFF, 0xDA20E0FF, 0xBD10C5FF, 0xBF3FBFFF},
            {0xBD62FFFF, 0xBD29FFFF, 0x8732D2FF, 0x724072FF},
            {0xDA20E0FF, 0xBD10C5FF, 0xA01982FF, 0x641464FF},
            {0x8732D2FF, 0x8C14BEFF, 0x5A187BFF, 0x320A46FF},
            {0x573B73FF, 0x5A187BFF, 0x320A46FF, 0x280A1EFF},
            {0x724072FF, 0x641464FF, 0x551937FF, 0x320A46FF},
            {0x5A187BFF, 0x410062FF, 0x320A46FF, 0x280A1EFF},
            {0x3C233CFF, 0x320A46FF, 0x280A1EFF, 0x010101FF},
            {0x4B2837FF, 0x551937FF, 0x321623FF, 0x280A1EFF},
            {0xBF3FBFFF, 0xA01982FF, 0x641464FF, 0x551937FF},
            {0xE61E78FF, 0xC80078FF, 0xA01982FF, 0x911437FF},
            {0xFF6AC5FF, 0xFF50BFFF, 0xC35A91FF, 0x8F578FFF},
            {0xFD81FFFF, 0xFF6AC5FF, 0xC87DA0FF, 0xAB73ABFF},
            {0xFFC0CBFF, 0xFAA0B9FF, 0xE3ABABFF, 0xD7A0BEFF},
            {0xFF50BFFF, 0xFC3A8CFF, 0xC35A91FF, 0x98344DFF},
            {0xFC3A8CFF, 0xE61E78FF, 0x98344DFF, 0xBD1039FF},
            {0xBF3F3FFF, 0xBD1039FF, 0x911437FF, 0x551414FF},
            {0x8E5555FF, 0x98344DFF, 0x73413CFF, 0x573B3BFF},
            {0x98344DFF, 0x911437FF, 0x551414FF, 0x401811FF},
    };

    public static final ITwilight AuroraTwilight = new Twilight() {
        @Override
        public int dark(byte voxel) {
            return RAMP_VALUES[voxel & 255][3];
        }

        @Override
        public int dim(byte voxel) {
            return RAMP_VALUES[voxel & 255][2];
        }

        @Override
        public int twilight(byte voxel) {
            return RAMP_VALUES[voxel & 255][1];
        }

        @Override
        public int bright(byte voxel) {
            return RAMP_VALUES[voxel & 255][0];
        }
    };

    public static ITwilight arbitraryTwilight(final int[] rgbaPalette) {
        return new Twilight() {
            private int[][] RAMP_VALUES = new int[256][4];

            {
                for (int i = 1; i < 256 && i < rgbaPalette.length; i++) {
                    int color = RAMP_VALUES[i][1] = rgbaPalette[i];
                    float luma = ((color >>> 24) * 0.299f) +
                            ((color >>> 16 & 0xFF) * 0.587f) +
                            ((color >>> 8 & 0xFF) * 0.114f),
                            chromaB = (((color >>> 24) * -0.168736f) +
                                    ((color >>> 16 & 0xFF) * -0.331264f) +
                                    ((color >>> 8 & 0xFF) * 0.5f)) * 0.875f,
                            chromaR = (((color >>> 24) * 0.5f) +
                                    ((color >>> 16 & 0xFF) * -0.418688f) +
                                    ((color >>> 8 & 0xFF) * -0.081312f)) * 0.875f,
                            lumaBright = Math.min(luma * 1.125f, 1f),
                            lumaDim = luma * 0.875f,
                            lumaDark = luma * 0.75f;
                    RAMP_VALUES[i][2] =
                            (int) (MathUtils.clamp(lumaDim + chromaR * 1.402f, 0f, 255f)) << 24 |
                                    (int) (MathUtils.clamp(lumaDim - chromaB * 0.344136f - chromaR * 0.714136f, 0f, 255f)) << 16 |
                                    (int) (MathUtils.clamp(lumaDim + chromaB * 1.772f, 0f, 255f)) << 8 | 0xFF;
                    chromaB *= 0.875f;
                    chromaR *= 0.875f;
                    RAMP_VALUES[i][0] =
                            (int) (MathUtils.clamp(lumaBright + chromaR * 1.402f, 0f, 255f)) << 24 |
                                    (int) (MathUtils.clamp(lumaBright - chromaB * 0.344136f - chromaR * 0.714136f, 0f, 255f)) << 16 |
                                    (int) (MathUtils.clamp(lumaBright + chromaB * 1.772f, 0f, 255f)) << 8 | 0xFF;
                    chromaB *= 0.875f;
                    chromaR *= 0.875f;
                    RAMP_VALUES[i][3] =
                            (int) (MathUtils.clamp(lumaDark + chromaR * 1.402f, 0f, 255f)) << 24 |
                                    (int) (MathUtils.clamp(lumaDark - chromaB * 0.344136f - chromaR * 0.714136f, 0f, 255f)) << 16 |
                                    (int) (MathUtils.clamp(lumaDark + chromaB * 1.772f, 0f, 255f)) << 8 | 0xFF;
                }
            }

            @Override
            public int dark(byte voxel) {
                return RAMP_VALUES[voxel & 255][3];
            }

            @Override
            public int dim(byte voxel) {
                return RAMP_VALUES[voxel & 255][2];
            }

            @Override
            public int twilight(byte voxel) {
                return RAMP_VALUES[voxel & 255][1];
            }

            @Override
            public int bright(byte voxel) {
                return RAMP_VALUES[voxel & 255][0];
            }
        };
    }

    protected ITwilight twilight = RinsedTwilight;

    public ITwilight twilight() {
        return twilight;
    }

    public VoxelColor set(ITwilight twilight) {
        this.twilight = twilight;
        return this;
    }

    @Override
    public int topFace(byte voxel) {
        switch (lightDirection) {
            case ABOVE_RIGHT:
            case ABOVE_LEFT:
                return twilight.bright(voxel);
            case RIGHT_BELOW:
            case LEFT_BELOW:
                return twilight.dim(voxel);
            case BELOW_RIGHT:
            case BELOW_LEFT:
                return twilight.dark(voxel);
            default:
                return twilight.twilight(voxel);
        }
    }

    @Override
    public int bottomFace(byte voxel) {
        switch (lightDirection) {
            case BELOW_LEFT:
            case BELOW_RIGHT:
                return twilight.bright(voxel);
            case LEFT_ABOVE:
            case RIGHT_ABOVE:
                return twilight.dim(voxel);
            case ABOVE_LEFT:
            case ABOVE_RIGHT:
                return twilight.dark(voxel);
            default:
                return twilight.twilight(voxel);
        }
    }

    @Override
    public int leftFace(byte voxel) {
        switch (lightDirection) {
            case LEFT_ABOVE:
            case LEFT_BELOW:
                return twilight.bright(voxel);
            case ABOVE_RIGHT:
            case BELOW_RIGHT:
                return twilight.dim(voxel);
            case RIGHT_ABOVE:
            case RIGHT_BELOW:
                return twilight.dark(voxel);
            default:
                return twilight.twilight(voxel);
        }
    }

    @Override
    public int rightFace(byte voxel) {
        switch (lightDirection) {
            case RIGHT_ABOVE:
            case RIGHT_BELOW:
                return twilight.bright(voxel);
            case ABOVE_LEFT:
            case BELOW_LEFT:
                return twilight.dim(voxel);
            case LEFT_ABOVE:
            case LEFT_BELOW:
                return twilight.dark(voxel);
            default:
                return twilight.twilight(voxel);
        }
    }

    public static class SimpleVoxelColor implements IVoxelColor {
        public static int simple(byte voxel) {
            return Coloring.RINSED[voxel & 255];
        }

        @Override
        public int topFace(byte voxel) {
            return simple(voxel);
        }

        @Override
        public int bottomFace(byte voxel) {
            return simple(voxel);
        }

        @Override
        public int leftFace(byte voxel) {
            return simple(voxel);
        }

        @Override
        public int rightFace(byte voxel) {
            return simple(voxel);
        }
    }
}
