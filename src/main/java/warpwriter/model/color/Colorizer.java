package warpwriter.model.color;

import com.badlogic.gdx.math.MathUtils;
import squidpony.squidmath.IRNG;
import warpwriter.Coloring;
import warpwriter.PaletteReducer;
import warpwriter.view.color.Dimmer;

/**
 * Created by Tommy Ettinger on 1/13/2019.
 */
public abstract class Colorizer extends Dimmer implements IColorizer {
    private Colorizer()
    {
        
    }
    protected Colorizer(PaletteReducer reducer)
    {
        this.reducer = reducer;
    }
    protected PaletteReducer reducer;

    /**
     * @param voxel      A color index
     * @param brightness An integer representing how many shades brighter (if positive) or darker (if negative) the result should be
     * @return A different shade of the same color
     */
    @Override
    public byte colorize(byte voxel, int brightness) {
        if(brightness > 0)
        {
            for (int i = 0; i < brightness; i++) {
                voxel = brighten(voxel);
            }
        }
        else if(brightness < 0)
        {
            for (int i = 0; i > brightness; i--) {
                voxel = darken(voxel);
            }
        }
        return voxel;
    }

    /**
     * @param color An RGBA8888 color
     * @return The nearest available color index in the palette
     */
    @Override
    public byte reduce(int color) {
        return reducer.reduceIndex(color);
    }

    /**
     * Uses {@link #colorize(byte, int)} to figure out what index has the correct brightness, then looks that index up
     * in the {@link #reducer}'s stored palette array to get an RGBA8888 int.
     * @param brightness 0 for dark, 1 for dim, 2 for medium and 3 for bright. Negative numbers are expected to normally be interpreted as black and numbers higher than 3 as white.
     * @param voxel      The color index of a voxel
     * @return An rgba8888 color
     */
    @Override
    public int dimmer(int brightness, byte voxel) {
        return reducer.paletteArray[colorize(voxel, brightness - 2) & 0xFF];
    }

    /**
     * Gets a PaletteReducer that contains the RGBA8888 int colors that the byte indices these deals with correspond to.
     * This PaletteReducer can be queried for random colors with {@link PaletteReducer#randomColor(IRNG)} (for an int
     * color) or {@link PaletteReducer#randomColorIndex(IRNG)} (for a byte this can use again).
     * @return the PaletteReducer this uses to store the corresponding RGBA8888 colors for the palette
     */
    public PaletteReducer getReducer() {
        return reducer;
    }

    /**
     * Sets the PaletteReducer this uses.
     * @param reducer a PaletteReducer that should not be null
     */
    protected void setReducer(PaletteReducer reducer) {
        this.reducer = reducer;
    }


    public static final Colorizer AuroraColorizer = new Colorizer(new PaletteReducer()) {
        private final byte[] primary = {
                -104,
                62,
                -98,
                40,
                -73,
                17,
                -52,
                -127
        }, grays = {
                1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15
        };

        @Override
        public byte[] mainColors() {
            return primary;
        }

        /**
         * @return An array of grayscale or close-to-grayscale color indices, with the darkest first and lightest last.
         */
        @Override
        public byte[] grayscale() {
            return grays;
        }

        @Override
        public byte brighten(byte voxel) {
            return Dimmer.AURORA_RAMPS[voxel & 0xFF][0];
        }

        @Override
        public byte darken(byte voxel) {
            return Dimmer.AURORA_RAMPS[voxel & 0xFF][2];
        }
    };
    public static final Colorizer FlesurrectColorizer = new Colorizer(Coloring.FLESURRECT_REDUCER) {
        private final byte[] primary = {
                63, 24, 27, 34, 42, 49, 55
        }, grays = {
                1, 2, 3, 4, 5, 6, 7, 8, 9
        };

        @Override
        public byte[] mainColors() {
            return primary;
        }

        /**
         * @return An array of grayscale or close-to-grayscale color indices, with the darkest first and lightest last.
         */
        @Override
        public byte[] grayscale() {
            return grays;
        }

        @Override
        public byte brighten(byte voxel) {
            return Dimmer.FLESURRECT_RAMPS[voxel & 0x3F][0]; // uses 0x3F, or 63, as the mask since there are 64 colors
        }

        @Override
        public byte darken(byte voxel) {
            return Dimmer.FLESURRECT_RAMPS[voxel & 0x3F][2]; // uses 0x3F, or 63, as the mask since there are 64 colors
        }
    };
    public static final Colorizer AuroraBonusColorizer = new Colorizer(new PaletteReducer()) {
        private final byte[] primary = {
                -104,
                62,
                -98,
                40,
                -73,
                17,
                -52,
                -127
        }, grays = {
                1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15
        };

        @Override
        public byte[] mainColors() {
            return primary;
        }

        /**
         * @return An array of grayscale or close-to-grayscale color indices, with the darkest first and lightest last.
         */
        @Override
        public byte[] grayscale() {
            return grays;
        }

        @Override
        public byte brighten(byte voxel) {
            return Dimmer.AURORA_RAMPS[voxel & 0xFF][0];
        }

        @Override
        public byte darken(byte voxel) {
            return Dimmer.AURORA_RAMPS[voxel & 0xFF][2];
        }
        private int[][] RAMP_VALUES = new int[256][4];

        {
            for (int i = 1; i < 256; i++) {
                int color = RAMP_VALUES[i][2] = Coloring.AURORA[i],
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
    public static final Colorizer FlesurrectBonusColorizer = new Colorizer(Coloring.FLESURRECT_REDUCER) {
        private final byte[] primary = {
                63, 24, 27, 34, 42, 49, 55
        }, grays = {
                1, 2, 3, 4, 5, 6, 7, 8, 9
        };

        @Override
        public byte[] mainColors() {
            return primary;
        }

        /**
         * @return An array of grayscale or close-to-grayscale color indices, with the darkest first and lightest last.
         */
        @Override
        public byte[] grayscale() {
            return grays;
        }

        @Override
        public byte brighten(byte voxel) {
            return Dimmer.FLESURRECT_RAMPS[voxel & 0x3F][0]; // uses 0x3F, or 63, as the mask since there are 64 colors
        }

        @Override
        public byte darken(byte voxel) {
            return Dimmer.FLESURRECT_RAMPS[voxel & 0x3F][2]; // uses 0x3F, or 63, as the mask since there are 64 colors
        }
        private int[][] RAMP_VALUES = new int[64][4];

        {
            for (int i = 1; i < 64; i++) {
                int color = RAMP_VALUES[i][2] = Coloring.FLESURRECT[i],
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
            return RAMP_VALUES[voxel & 63][
                    brightness <= 0
                            ? 0
                            : brightness >= 3
                            ? 3
                            : brightness
                    ];
        }

    };

}
