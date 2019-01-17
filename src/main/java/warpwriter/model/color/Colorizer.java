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
                        yBright = y * 5 >> 2, yDim = y * 7 >> 3, yDark = y >> 1,
                        //yBright = y * 21 >> 4, yDim = y * 5 >> 3, yDark = y >> 1,
                        chromO, chromG;
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
        private int[][] RAMP_VALUES = new int[][] {
                { 0x1B092CFF, 0x1C162BFF, 0x1F1833FF, 0x2C1D3BFF },
                { 0x1F1C32FF, 0x252837FF, 0x2B2E42FF, 0x3A394CFF },
                { 0x2E1F35FF, 0x352E3BFF, 0x3E3546FF, 0x4D4253FF },
                { 0x2C2F40FF, 0x393E4BFF, 0x414859FF, 0x565A68FF },
                { 0x464D55FF, 0x5B6269FF, 0x68717AFF, 0x868D94FF },
                { 0x606E74FF, 0x808C92FF, 0x90A1A8FF, 0xBCC6CEFF },
                { 0x7B8B90FF, 0xA1B0B4FF, 0xB6CBCFFF, 0xEEF7FFFF },
                { 0x8F9DA5FF, 0xBAC7CEFF, 0xD3E5EDFF, 0xFFFFFFFF },
                { 0xAFAFAFFF, 0xDFDFDFFF, 0xFFFFFFFF, 0xFFFFFFFF },
                { 0x472132FF, 0x4D3439FF, 0x5C3A41FF, 0x6A4B56FF },
                { 0x5F3E5FFF, 0x6E586EFF, 0x826481FF, 0x988498FF },
                { 0x6E454CFF, 0x7F5F60FF, 0x966C6CFF, 0xAA8C8BFF },
                { 0x796652FF, 0x92816EFF, 0xAB947AFF, 0xC9B7A5FF },
                { 0xB85159FF, 0xCC7475FF, 0xF68181FF, 0xFCBBA5FF },
                { 0xCD0A30FF, 0xC43233FF, 0xF53333FF, 0xEA5559FF },
                { 0xC92F36FF, 0xCF5348FF, 0xFF5A4AFF, 0xF88971FF },
                { 0x8C232DFF, 0x8E3F37FF, 0xAE4539FF, 0xB3605CFF },
                { 0x69312CFF, 0x73473AFF, 0x8A503EFF, 0x96685DFF },
                { 0x9D4128FF, 0xA85C3CFF, 0xCD683DFF, 0xD38A67FF },
                { 0xB77033FF, 0xD08F56FF, 0xFBA458FF, 0xFFCE93FF },
                { 0xC1420DFF, 0xCB5F25FF, 0xFB6B1DFF, 0xF68F50FF },
                { 0x705C3FFF, 0x87735AFF, 0x9F8562FF, 0xB8A38BFF },
                { 0xB48258FF, 0xD5A780FF, 0xFCBF8AFF, 0xFFF1C8FF },
                { 0xBA6E00FF, 0xD18823FF, 0xFF9E17FF, 0xFFBD5FFF },
                { 0xAC810AFF, 0xC89C32FF, 0xF0B628FF, 0xFFD078FF },
                { 0xA08A62FF, 0xC2AE89FF, 0xE3C896FF, 0xFFF2D6FF },
                { 0xB5A308FF, 0xD3C334FF, 0xFBE626FF, 0xFFEF95FF },
                { 0xA69C00FF, 0xC6B415FF, 0xEDD500FF, 0xFFDC69FF },
                { 0xB5AE56FF, 0xD8DB81FF, 0xFBFF86FF, 0xFFFFF0FF },
                { 0x7A9C20FF, 0x9CB549FF, 0xB4D645FF, 0xF2DF9FFF },
                { 0x467123FF, 0x657E44FF, 0x729446FF, 0x96A775FF },
                { 0x5D9F3DFF, 0x82B964FF, 0x91DB69FF, 0xDBE2BDFF },
                { 0x0A7800FF, 0x316D16FF, 0x358510FF, 0x49932EFF },
                { 0x239D15FF, 0x4EA441FF, 0x51C43FFF, 0x8EC781FF },
                { 0x208120FF, 0x468746FF, 0x4BA14AFF, 0x76AE76FF },
                { 0x00963FFF, 0x269D66FF, 0x1EBC73FF, 0x63C2A3FF },
                { 0x0CA47CFF, 0x39BDA0FF, 0x30E1B9FF, 0x93E2FAFF },
                { 0x509D87FF, 0x77C0AAFF, 0x7FE0C2FF, 0xD4F3FFFF },
                { 0x7BACB5FF, 0xA7DBDDFF, 0xB8FDFFFF, 0xFFFFFFFF },
                { 0x008343FF, 0x108568FF, 0x039F78FF, 0x3BAB93FF },
                { 0x3A898DFF, 0x60A7ADFF, 0x63C2C9FF, 0xACDCF9FF },
                { 0x31598CFF, 0x4C73A0FF, 0x4F83BFFF, 0x7EA5D2FF },
                { 0x0A4E58FF, 0x245A6CFF, 0x216981FF, 0x477E8FFF },
                { 0x4FA0ADFF, 0x79C8D0FF, 0x7FE8F2FF, 0xDAFFFFFF },
                { 0x292E7BFF, 0x384783FF, 0x3B509FFF, 0x5C69A7FF },
                { 0x2B6AA8FF, 0x4D87C0FF, 0x4D9BE6FF, 0x87C0FAFF },
                { 0x1F1759FF, 0x252B5BFF, 0x28306FFF, 0x403E76FF },
                { 0x2D469BFF, 0x4563ABFF, 0x4870CFFF, 0x7194D7FF },
                { 0x3B29A9FF, 0x474AADFF, 0x4D50D4FF, 0x6F76D5FF },
                { 0x2600BBFF, 0x1913A3FF, 0x180FCFFF, 0x401ECAFF },
                { 0x4D0070FF, 0x462066FF, 0x53207DFF, 0x6A318AFF },
                { 0x682CA1FF, 0x7551AAFF, 0x8657CCFF, 0x9E89D3FF },
                { 0x7652B3FF, 0x9378CCFF, 0xA884F3FF, 0xC0C8F9FF },
                { 0x640068FF, 0x510C54FF, 0x630867FF, 0x761079FF },
                { 0x851394FF, 0x863C94FF, 0xA03EB2FF, 0xAC6CBAFF },
                { 0x6F08A0FF, 0x6E339BFF, 0x8032BCFF, 0x945AC1FF },
                { 0x9D71AFFF, 0xC496D5FF, 0xE4A8FAFF, 0xF6FCFFFF },
                { 0x95136FFF, 0x953B72FF, 0xB53D86FF, 0xBC6699FF },
                { 0xBB21B3FF, 0xCA4EC3FF, 0xF34FE9FF, 0xE5AADEFF },
                { 0x66113BFF, 0x642C3DFF, 0x7A3045FF, 0x84415DFF },
                { 0xBF225EFF, 0xC34A69FF, 0xF04F78FF, 0xEB8591FF },
                { 0x92455EFF, 0xA26572FF, 0xC27182FF, 0xD09FA0FF },
                { 0xA90A34FF, 0xA12F35FF, 0xC93038FF, 0xC74C5BFF },
        };

//        {
            ////StringBuilder sb = new StringBuilder(1024).append("{\n");
//            for (int i = 1; i < 64; i++) {
//                int color = RAMP_VALUES[i][2] = Coloring.FLESURRECT[i],
//                        r = (color >>> 24),
//                        g = (color >>> 16 & 0xFF),
//                        b = (color >>> 8 & 0xFF);
//                int co = r - b, t = b + (co >> 1), cg = g - t, y = t + (cg >> 1),
//                        yBright = y * 5 >> 2, yDim = y * 7 >> 3, yDark = y * 11 >> 4, chromO, chromG;
//                chromO = (co * 3) >> 2;
//                chromG = (cg * 3) >> 2;
//                t = yDim - (chromG >> 1);
//                g = chromG + t;
//                b = t - (chromO >> 1);
//                r = b + chromO;
//                RAMP_VALUES[i][1] =
//                        MathUtils.clamp(r, 0, 255) << 24 |
//                                MathUtils.clamp(g, 0, 255) << 16 |
//                                MathUtils.clamp(b, 0, 255) << 8 | 0xFF;
//                chromO = (co * 3) >> 2;
//                chromG = (cg * (256 - yBright) * 3) >> 9;
//                t = yBright - (chromG >> 1);
//                g = chromG + t;
//                b = t - (chromO >> 1);
//                r = b + chromO;
//                RAMP_VALUES[i][3] =
//                        MathUtils.clamp(r, 0, 255) << 24 |
//                                MathUtils.clamp(g, 0, 255) << 16 |
//                                MathUtils.clamp(b, 0, 255) << 8 | 0xFF;
//                chromO = (co * 13) >> 4;
//                chromG = (cg * (256 - yDark) * 13) >> 11;
//                t = yDark - (chromG >> 1);
//                g = chromG + t;
//                b = t - (chromO >> 1);
//                r = b + chromO;
//                RAMP_VALUES[i][0] =
//                        MathUtils.clamp(r, 0, 255) << 24 |
//                                MathUtils.clamp(g, 0, 255) << 16 |
//                                MathUtils.clamp(b, 0, 255) << 8 | 0xFF;
////                sb.append("{ 0x");
////                StringKit.appendHex(sb, RAMP_VALUES[i][0]);
////                StringKit.appendHex(sb.append(", 0x"), RAMP_VALUES[i][1]);
////                StringKit.appendHex(sb.append(", 0x"), RAMP_VALUES[i][2]);
////                StringKit.appendHex(sb.append(", 0x"), RAMP_VALUES[i][3]);
////                sb.append(" },\n");
//            }
////            System.out.println(sb.append("};"));
//        }

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
