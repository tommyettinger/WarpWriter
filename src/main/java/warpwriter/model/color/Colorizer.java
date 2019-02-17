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

    private static int luma(final int r, final int g, final int b)
    {
        return r * 0x9C + g * 0xF6 + b * 0x65 + 0x18 - (Math.max(r, Math.max(g, b)) - Math.min(r, Math.min(g, b))) * 0x19;
//        return color.r * 0x8.Ap-5f + color.g * 0xF.Fp-5f + color.b * 0x6.1p-5f
//                + 0x1.6p-5f - (Math.max(color.r, Math.max(color.g, color.b))
//                - Math.min(color.r, Math.min(color.g, color.b))) * 0x1.6p-5f;
        // r * 0x8A + g * 0xFF + b * 0x61 + 0x15 - (Math.max(r, Math.max(g, b)) - Math.min(r, Math.min(g, b))) * 0x16;
        // 0x8A + 0xFF + 0x61 + 0x16 - 0x16
    }

    /**
     * Approximates the "color distance" between two colors defined by their YCoCg values. The luma in parameters y1 and
     * y2 should be calculated with {@link #luma(int, int, int)}, which is not the standard luminance value for YCoCg;
     * it will range from 0 to 63 typically. The chrominance orange values co1 and co2, and the chrominance green values
     * cg1 and cg2, should range from 0 to 31 typically by taking the standard range of -0.5 to 0.5, adding 0.5,
     * multiplying by 31 and rounding to an int.
     * @param y1 luma for color 1; from 0 to 63, calculated by {@link #luma(int, int, int)}
     * @param co1 chrominance orange for color 1; from 0 to 31, usually related to {@code red - blue}
     * @param cg1 chrominance green for color 1; from 0 to 31, usually related to {@code green - (red + blue) * 0.5}
     * @param y2 luma for color 2; from 0 to 63, calculated by {@link #luma(int, int, int)}
     * @param co2 chrominance orange for color 2; from 0 to 31, usually related to {@code red - blue}
     * @param cg2 chrominance green for color 2; from 0 to 31, usually related to {@code green - (red + blue) * 0.5}
     * @return a non-negative int that is larger for more-different colors; typically somewhat large
     */
    private static int difference(int y1, int co1, int cg1, int y2, int co2, int cg2) {
        return ((y1 - y2) * (y1 - y2) << 2) + (((co1 - co2) * (co1 - co2) + (cg1 - cg2) * (cg1 - cg2)) * 3);
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
            // the second half of voxels (with bit 0x40 set) don't shade visually, but Colorizer uses this method to
            // denote a structural change to the voxel's makeup, so this uses the first 64 voxel colors to shade both
            // halves, then marks voxels from the second half back to being an unshaded voxel as the last step.
            return (byte) (Dimmer.FLESURRECT_RAMPS[voxel & 0x3F][3] | (voxel & 0xC0));
        }

        @Override
        public byte darken(byte voxel) {
            // the second half of voxels (with bit 0x40 set) don't shade visually, but Colorizer uses this method to
            // denote a structural change to the voxel's makeup, so this uses the first 64 voxel colors to shade both
            // halves, then marks voxels from the second half back to being an unshaded voxel as the last step.
            return (byte) (Dimmer.FLESURRECT_RAMPS[voxel & 0x3F][1] | (voxel & 0xC0));
        }

        @Override
        public int dimmer(int brightness, byte voxel) {
            return Dimmer.FLESURRECT_RAMP_VALUES[voxel & 0xFF][
                    brightness <= 0
                            ? 0
                            : brightness >= 3
                            ? 3
                            : brightness
                    ];
        }

        @Override
        public int getShadeBit() {
            return 0x40;
        }
        @Override
        public int getWaveBit() {
            return 0x80;
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
        private int[][] RAMP_VALUES = new int[][]{
                { 0x00000000, 0x00000000, 0x00000000, 0x00000000 },
                { 0x000000FF, 0x000000FF, 0x010101FF, 0x010101FF },
                { 0x070707FF, 0x0D0D0DFF, 0x131313FF, 0x1B1B1BFF },
                { 0x0D0D0DFF, 0x191919FF, 0x252525FF, 0x353535FF },
                { 0x141414FF, 0x252525FF, 0x373737FF, 0x4F4F4FFF },
                { 0x1B1B1BFF, 0x323232FF, 0x494949FF, 0x686868FF },
                { 0x222222FF, 0x3E3E3EFF, 0x5B5B5BFF, 0x828282FF },
                { 0x292929FF, 0x4B4B4BFF, 0x6E6E6EFF, 0x9E9E9EFF },
                { 0x303030FF, 0x585858FF, 0x808080FF, 0xB8B8B8FF },
                { 0x363636FF, 0x646464FF, 0x929292FF, 0xD1D1D1FF },
                { 0x3D3D3DFF, 0x707070FF, 0xA4A4A4FF, 0xEBEBEBFF },
                { 0x444444FF, 0x7D7D7DFF, 0xB6B6B6FF, 0xFFFFFFFF },
                { 0x4B4B4BFF, 0x8A8A8AFF, 0xC9C9C9FF, 0xFFFFFFFF },
                { 0x525252FF, 0x969696FF, 0xDBDBDBFF, 0xFFFFFFFF },
                { 0x585858FF, 0xA2A2A2FF, 0xEDEDEDFF, 0xFFFFFFFF },
                { 0x5F5F5FFF, 0xAFAFAFFF, 0xFFFFFFFF, 0xFFFFFFFF },
                { 0x005728FF, 0x005661FF, 0x007F7FFF, 0x3EA29EFF },
                { 0x006945FF, 0x297D91FF, 0x3FBFBFFF, 0x9AFEFAFF },
                { 0x009E61FF, 0x009ED0FF, 0x00FFFFFF, 0x7EFFFFFF },
                { 0x2A6D62FF, 0x85A9B9FF, 0xBFFFFFFF, 0xFFFFFFFF },
                { 0x330FA2FF, 0x4B5EB2FF, 0x8181FFFF, 0xD1CCFFFF },
                { 0x1400F4FF, 0x0000C2FF, 0x0000FFFF, 0x2E26EEFF },
                { 0x1F008FFF, 0x222C8AFF, 0x3F3FBFFF, 0x726ED2FF },
                { 0x0C007CFF, 0x000063FF, 0x00007FFF, 0x161276FF },
                { 0x0C0045FF, 0x08083DFF, 0x0F0F50FF, 0x211F52FF },
                { 0x840084FF, 0x5A005AFF, 0x7F007FFF, 0x8E268EFF },
                { 0x910091FF, 0x7C327CFF, 0xBF3FBFFF, 0xEA82EAFF },
                { 0xEB00EBFF, 0x9C0B9CFF, 0xF500F5FF, 0xFF4BFFFF },
                { 0x9B009DFF, 0x9D689FFF, 0xFD81FFFF, 0xFFDFFFFF },
                { 0x7E3651FF, 0xAD8983FF, 0xFFC0CBFF, 0xFFFFFFFF },
                { 0xA10F33FF, 0xB15E4BFF, 0xFF8181FF, 0xFFCCD1FF },
                { 0xF40015FF, 0xC20000FF, 0xFF0000FF, 0xEE262FFF },
                { 0x8F001FFF, 0x8A2C22FF, 0xBF3F3FFF, 0xD26E72FF },
                { 0x7C000DFF, 0x630000FF, 0x7F0000FF, 0x761217FF },
                { 0x46000EFF, 0x3F0B0BFF, 0x551414FF, 0x582628FF },
                { 0x4F1700FF, 0x5F2B00FF, 0x7F3F00FF, 0x8A5A2BFF },
                { 0x672F00FF, 0x8B5723FF, 0xBF7F3FFF, 0xE6B686FF },
                { 0x9F2F00FF, 0xBF5700FF, 0xFF7F00FF, 0xFFB657FF },
                { 0x7F4611FF, 0xB78351FF, 0xFFBF81FF, 0xFFFFE4FF },
                { 0x626D2AFF, 0xB9A985FF, 0xFFFFBFFF, 0xFFFFFFFF },
                { 0x619E00FF, 0xD09E01FF, 0xFFFF00FF, 0xFFFF7FFF },
                { 0x456900FF, 0x917D29FF, 0xBFBF3FFF, 0xFAFE9AFF },
                { 0x285700FF, 0x615600FF, 0x7F7F00FF, 0x9EA23FFF },
                { 0x008300FF, 0x005900FF, 0x007F00FF, 0x278E27FF },
                { 0x009100FF, 0x337C33FF, 0x3FBF3FFF, 0x82EA82FF },
                { 0x00F200FF, 0x0EA10EFF, 0x00FF00FF, 0x4FFF4FFF },
                { 0x1D841DFF, 0x85A285FF, 0xAFFFAFFF, 0xFFFFFFFF },
                { 0x4D3A51FF, 0x7F7A83FF, 0xBCAFC0FF, 0xFFFFFFFF },
                { 0x5C3F23FF, 0x8F745AFF, 0xCBAA89FF, 0xFFF4DCFF },
                { 0x413E2EFF, 0x736C62FF, 0xA6A090FF, 0xE7E3D7FF },
                { 0x233D37FF, 0x566468FF, 0x7E9494FF, 0xC0D0D1FF },
                { 0x1E3534FF, 0x4A585FFF, 0x6E8287FF, 0xA9B8BCFF },
                { 0x37281DFF, 0x584B40FF, 0x7E6E60FF, 0xAA9E94FF },
                { 0x591A21FF, 0x70483CFF, 0xA0695FFF, 0xC79D97FF },
                { 0x6E1A2AFF, 0x875448FF, 0xC07872FF, 0xEEB6B4FF },
                { 0x722622FF, 0x936149FF, 0xD08A74FF, 0xFFCDBFFF },
                { 0x782F21FF, 0x9F6C4EFF, 0xE19B7DFF, 0xFFE5D1FF },
                { 0x783625FF, 0xA57658FF, 0xEBAA8CFF, 0xFFF8E3FF },
                { 0x7A3E2CFF, 0xAD8164FF, 0xF5B99BFF, 0xFFFFFAFF },
                { 0x724534FF, 0xAB8A72FF, 0xF6C8AFFF, 0xFFFFFFFF },
                { 0x655347FF, 0xAA9B8EFF, 0xF5E1D2FF, 0xFFFFFFFF },
                { 0x300C18FF, 0x3E2828FF, 0x573B3BFF, 0x6F585AFF },
                { 0x470A17FF, 0x522C26FF, 0x73413CFF, 0x8B6462FF },
                { 0x550F24FF, 0x643B36FF, 0x8E5555FF, 0xAF8385FF },
                { 0x5F1B2EFF, 0x78504BFF, 0xAB7373FF, 0xDAAEB0FF },
                { 0x682637FF, 0x8A645DFF, 0xC78F8FFF, 0xFFD6D8FF },
                { 0x723241FF, 0x9D7970FF, 0xE3ABABFF, 0xFFFEFFFF },
                { 0x6E4354FF, 0xA89390FF, 0xF8D2DAFF, 0xFFFFFFFF },
                { 0x634A32FF, 0x9F8872FF, 0xE3C7ABFF, 0xFFFFFFFF },
                { 0x5B3C15FF, 0x8C6C4BFF, 0xC49E73FF, 0xFDE1C1FF },
                { 0x442B13FF, 0x664F39FF, 0x8F7357FF, 0xBAA590FF },
                { 0x392008FF, 0x523B25FF, 0x73573BFF, 0x927D68FF },
                { 0x1C1004FF, 0x291E13FF, 0x3B2D1FFF, 0x4B4036FF },
                { 0x162200FF, 0x2E2D16FF, 0x414123FF, 0x565740FF },
                { 0x283B00FF, 0x544E27FF, 0x73733BFF, 0x9B9C71FF },
                { 0x344503FF, 0x67603AFF, 0x8F8F57FF, 0xC3C499FF },
                { 0x3B5200FF, 0x766C38FF, 0xA2A255FF, 0xDADCA1FF },
                { 0x425508FF, 0x83784DFF, 0xB5B572FF, 0xF7F9C5FF },
                { 0x4B581AFF, 0x908563FF, 0xC7C78FFF, 0xFFFFE9FF },
                { 0x535D2AFF, 0x9C9276FF, 0xDADAABFF, 0xFFFFFFFF },
                { 0x5B613AFF, 0xA89F8AFF, 0xEDEDC7FF, 0xFFFFFFFF },
                { 0x3E6926FF, 0x90957AFF, 0xC7E3ABFF, 0xFFFFFFFF },
                { 0x315F19FF, 0x7C8466FF, 0xABC78FFF, 0xF9FFE4FF },
                { 0x197000FF, 0x6A7B3CFF, 0x8EBE55FF, 0xCFF8A5FF },
                { 0x194D01FF, 0x525F3CFF, 0x738F57FF, 0xA9C094FF },
                { 0x084D00FF, 0x3F542AFF, 0x587D3EFF, 0x85A372FF },
                { 0x132B02FF, 0x313721FF, 0x465032FF, 0x646C55FF },
                { 0x041200FF, 0x111509FF, 0x191E0FFF, 0x23271CFF },
                { 0x003503FF, 0x163727FF, 0x235037FF, 0x446753FF },
                { 0x043204FF, 0x293C29FF, 0x3B573BFF, 0x5D735DFF },
                { 0x113111FF, 0x374437FF, 0x506450FF, 0x798979FF },
                { 0x004900FF, 0x274D33FF, 0x3B7349FF, 0x699574FF },
                { 0x005700FF, 0x3F603FFF, 0x578F57FF, 0x8FBC8FFF },
                { 0x0C5F0CFF, 0x537153FF, 0x73AB73FF, 0xB7E4B7FF },
                { 0x00710FFF, 0x4A7D63FF, 0x64C082FF, 0xB1FAC8FF },
                { 0x196719FF, 0x688268FF, 0x8FC78FFF, 0xDFFFDFFF },
                { 0x226B22FF, 0x768D76FF, 0xA2D8A2FF, 0xFAFFFAFF },
                { 0x49615FFF, 0x9BA8B0FF, 0xE1F8FAFF, 0xFFFFFFFF },
                { 0x286E3CFF, 0x829C94FF, 0xB4EECAFF, 0xFFFFFFFF },
                { 0x25683CFF, 0x79958FFF, 0xABE3C5FF, 0xFFFFFFFF },
                { 0x1A5921FF, 0x607866FF, 0x87B48EFF, 0xD0F5D6FF },
                { 0x024510FF, 0x375444FF, 0x507D5FFF, 0x83A78FFF },
                { 0x005000FF, 0x064833FF, 0x0F6946FF, 0x3A8264FF },
                { 0x011A06FF, 0x131F18FF, 0x1E2D23FF, 0x2F3B33FF },
                { 0x00201AFF, 0x152C32FF, 0x234146FF, 0x41585CFF },
                { 0x003B28FF, 0x264E54FF, 0x3B7373FF, 0x719C9BFF },
                { 0x00533FFF, 0x43727DFF, 0x64ABABFF, 0xB2EAE8FF },
                { 0x1A584BFF, 0x628590FF, 0x8FC7C7FF, 0xE9FFFFFF },
                { 0x256156FF, 0x7697A4FF, 0xABE3E3FF, 0xFFFFFFFF },
                { 0x37635CFF, 0x8AA2ADFF, 0xC7F1F1FF, 0xFFFFFFFF },
                { 0x3D4C69FF, 0x7E90A7FF, 0xBED2F0FF, 0xFFFFFFFF },
                { 0x324A63FF, 0x71889FFF, 0xABC7E3FF, 0xFFFFFFFF },
                { 0x364064FF, 0x6E7F99FF, 0xA8B9DCFF, 0xFFFFFFFF },
                { 0x284059FF, 0x5E758CFF, 0x8FABC7FF, 0xE0F5FFFF },
                { 0x043566FF, 0x356290FF, 0x578FC7FF, 0xA3CDF7FF },
                { 0x132B44FF, 0x384F66FF, 0x57738FFF, 0x90A5BAFF },
                { 0x082039FF, 0x243B52FF, 0x3B5773FF, 0x687D92FF },
                { 0x02051DFF, 0x091022FF, 0x0F192DFF, 0x1E2435FF },
                { 0x0F0228FF, 0x15142CFF, 0x1F1F3BFF, 0x323047FF },
                { 0x180C31FF, 0x28283FFF, 0x3B3B57FF, 0x5A586FFF },
                { 0x1F0E44FF, 0x303253FF, 0x494973FF, 0x706E90FF },
                { 0x240F55FF, 0x373C65FF, 0x57578FFF, 0x8886B2FF },
                { 0x301761FF, 0x4A4D77FF, 0x736EAAFF, 0xADA8D7FF },
                { 0x2F1579FF, 0x49538EFF, 0x7676CAFF, 0xBAB6F9FF },
                { 0x372668FF, 0x5C648AFF, 0x8F8FC7FF, 0xD8D6FFFF },
                { 0x413272FF, 0x6F799DFF, 0xABABE3FF, 0xFFFEFFFF },
                { 0x494D6CFF, 0x8B97ACFF, 0xD0DAF8FF, 0xFFFFFFFF },
                { 0x544E6DFF, 0x989EAFFF, 0xE3E3FFFF, 0xFFFFFFFF },
                { 0x4F1F68FF, 0x706687FF, 0xAB8FC7FF, 0xF3DAFFFF },
                { 0x580089FF, 0x5A4088FF, 0x8F57C7FF, 0xC797F1FF },
                { 0x3E0757FF, 0x4D3D64FF, 0x73578FFF, 0xA38AB8FF },
                { 0x35004EFF, 0x3B2952FF, 0x573B73FF, 0x7B6290FF },
                { 0x270027FF, 0x2A162AFF, 0x3C233CFF, 0x4E394EFF },
                { 0x280528FF, 0x312231FF, 0x463246FF, 0x5F4E5FFF },
                { 0x4A004AFF, 0x4E2C4EFF, 0x724072FF, 0x946B94FF },
                { 0x570057FF, 0x603E60FF, 0x8F578FFF, 0xBC8EBCFF },
                { 0x700070FF, 0x714071FF, 0xAB57ABFF, 0xDC97DCFF },
                { 0x5F0B5FFF, 0x725372FF, 0xAB73ABFF, 0xE4B6E4FF },
                { 0x76256EFF, 0x9A7E92FF, 0xEBACE1FF, 0xFFFFFFFF },
                { 0x6F4667FF, 0xAA9CA2FF, 0xFFDCF5FF, 0xFFFFFFFF },
                { 0x623D62FF, 0x988D98FF, 0xE3C7E3FF, 0xFFFFFFFF },
                { 0x68365BFF, 0x97848BFF, 0xE1B9D2FF, 0xFFFFFFFF },
                { 0x6C2657FF, 0x90737CFF, 0xD7A0BEFF, 0xFFF3FFFF },
                { 0x671B5BFF, 0x846679FF, 0xC78FB9FF, 0xFFDCFFFF },
                { 0x741151FF, 0x875A67FF, 0xC87DA0FF, 0xFFC4E2FF },
                { 0x830058FF, 0x84425CFF, 0xC35A91FF, 0xEE9AC9FF },
                { 0x320021FF, 0x351A25FF, 0x4B2837FF, 0x5D404EFF },
                { 0x250018FF, 0x240E18FF, 0x321623FF, 0x3D2632FF },
                { 0x23001BFF, 0x1E0516FF, 0x280A1EFF, 0x2E1527FF },
                { 0x300007FF, 0x30100AFF, 0x401811FF, 0x472824FF },
                { 0x4F0000FF, 0x4A0E00FF, 0x621800FF, 0x63291AFF },
                { 0x92000BFF, 0x7D0B00FF, 0xA5140AFF, 0xA2312EFF },
                { 0xBB000BFF, 0xA21400FF, 0xDA2010FF, 0xD94742FF },
                { 0x97001EFF, 0x983A28FF, 0xD5524AFF, 0xEE8886FF },
                { 0xCA0000FF, 0xBE2B00FF, 0xFF3C0AFF, 0xFF6D4DFF },
                { 0xAE0004FF, 0xB24014FF, 0xF55A32FF, 0xFF9278FF },
                { 0xB3002AFF, 0xB44835FF, 0xFF6262FF, 0xFFA4AAFF },
                { 0x785D00FF, 0xB97D19FF, 0xF6BD31FF, 0xFFFF97FF },
                { 0x8C4200FF, 0xBC701EFF, 0xFFA53CFF, 0xFFEA9BFF },
                { 0x6C5000FF, 0xA26700FF, 0xD79B0FFF, 0xFDD267FF },
                { 0x892700FF, 0xA44C00FF, 0xDA6E0AFF, 0xF19F55FF },
                { 0x702100FF, 0x863D00FF, 0xB45A00FF, 0xC5813EFF },
                { 0x671700FF, 0x773300FF, 0xA04B05FF, 0xAD6D39FF },
                { 0x3B0D00FF, 0x452109FF, 0x5F3214FF, 0x6B4933FF },
                { 0x1A3400FF, 0x3D3702FF, 0x53500AFF, 0x686832FF },
                { 0x1D4400FF, 0x494300FF, 0x626200FF, 0x7A7C31FF },
                { 0x39370EFF, 0x64573CFF, 0x8C805AFF, 0xBBB296FF },
                { 0x465B00FF, 0x846300FF, 0xAC9400FF, 0xD0C14FFF },
                { 0x3C7100FF, 0x897402FF, 0xB1B10AFF, 0xDFE462FF },
                { 0x5E6900FF, 0xAD8B3CFF, 0xE6D55AFF, 0xFFFFC2FF },
                { 0x747800FF, 0xC68904FF, 0xFFD510FF, 0xFFFF82FF },
                { 0x6B7900FF, 0xC49631FF, 0xFFEA4AFF, 0xFFFFBFFF },
                { 0x319B00FF, 0xA19E34FF, 0xC8FF41FF, 0xFFFFB2FF },
                { 0x0E9B00FF, 0x7F973AFF, 0x9BF046FF, 0xE9FFAAFF },
                { 0x109A00FF, 0x7A8B15FF, 0x96DC19FF, 0xD4FF77FF },
                { 0x009B00FF, 0x5F8106FF, 0x73C805FF, 0xABF359FF },
                { 0x008200FF, 0x536F01FF, 0x6AA805FF, 0x98CD4DFF },
                { 0x005600FF, 0x2B4C0BFF, 0x3C6E14FF, 0x5E8740FF },
                { 0x022800FF, 0x1C2500FF, 0x283405FF, 0x36411CFF },
                { 0x003C00FF, 0x153102FF, 0x204608FF, 0x355423FF },
                { 0x005800FF, 0x054105FF, 0x0C5C0CFF, 0x2A6B2AFF },
                { 0x009000FF, 0x0E6602FF, 0x149605FF, 0x42AC37FF },
                { 0x00CB00FF, 0x0F8C0FFF, 0x0AD70AFF, 0x4EF44EFF },
                { 0x00D300FF, 0x189310FF, 0x14E60AFF, 0x5CFF55FF },
                { 0x00A200FF, 0x679E5FFF, 0x7DFF73FF, 0xDAFFD3FF },
                { 0x00AD00FF, 0x41954EFF, 0x4BF05AFF, 0xA1FFADFF },
                { 0x00BB00FF, 0x038114FF, 0x00C514FF, 0x42E051FF },
                { 0x009600FF, 0x047641FF, 0x05B450FF, 0x4BD784FF },
                { 0x006A00FF, 0x125E3BFF, 0x1C8C4EFF, 0x54AD7AFF },
                { 0x00230BFF, 0x092623FF, 0x123832FF, 0x2C4944FF },
                { 0x00681EFF, 0x086562FF, 0x129880FF, 0x58C1ABFF },
                { 0x008D14FF, 0x037E74FF, 0x06C491FF, 0x5DF3C6FF },
                { 0x00B200FF, 0x058D5CFF, 0x00DE6AFF, 0x58FFA8FF },
                { 0x009A1AFF, 0x249388FF, 0x2DEBA8FF, 0x92FFEFFF },
                { 0x00A611FF, 0x339D89FF, 0x3CFEA5FF, 0xA7FFF6FF },
                { 0x008E37FF, 0x51A0A2FF, 0x6AFFCDFF, 0xD9FFFFFF },
                { 0x0B676CFF, 0x629BBCFF, 0x91EBFFFF, 0xFFFFFFFF },
                { 0x00736FFF, 0x3895C3FF, 0x55E6FFFF, 0xC7FFFFFF },
                { 0x036068FF, 0x538FB1FF, 0x7DD7F0FF, 0xE4FFFFFF },
                { 0x008E49FF, 0x048DABFF, 0x08DED5FF, 0x74FFFFFF },
                { 0x004F73FF, 0x0068A8FF, 0x109CDEFF, 0x68D3FFFF },
                { 0x003D1DFF, 0x003E44FF, 0x055A5CFF, 0x317473FF },
                { 0x000B34FF, 0x0D1E3EFF, 0x162C52FF, 0x334260FF },
                { 0x000A55FF, 0x03255DFF, 0x0F377DFF, 0x37538AFF },
                { 0x001965FF, 0x003376FF, 0x004A9CFF, 0x356BAAFF },
                { 0x002551FF, 0x1B446DFF, 0x326496FF, 0x6A8FB5FF },
                { 0x0005B3FF, 0x0039B8FF, 0x0052F6FF, 0x4781FFFF },
                { 0x002770FF, 0x05488CFF, 0x186ABDFF, 0x5A98D6FF },
                { 0x002985FF, 0x0C52A3FF, 0x2378DCFF, 0x6EADF9FF },
                { 0x0D3E5CFF, 0x436B8DFF, 0x699DC3FF, 0xB7DEFBFF },
                { 0x003D8DFF, 0x2670BAFF, 0x4AA4FFFF, 0xA7EBFFFF },
                { 0x253687FF, 0x597BB4FF, 0x90B0FFFF, 0xECFFFFFF },
                { 0x00567DFF, 0x3684BDFF, 0x5AC5FFFF, 0xC0FFFFFF },
                { 0x49347EFF, 0x7A83ABFF, 0xBEB9FAFF, 0xFFFFFFFF },
                { 0x00697DFF, 0x007DC5FF, 0x00BFFFFF, 0x6AFEFFFF },
                { 0x002F9FFF, 0x0057BFFF, 0x007FFFFF, 0x56B6FFFF },
                { 0x042872FF, 0x2B5791FF, 0x4B7DC8FF, 0x92B7F0FF },
                { 0x36029FFF, 0x4550A7FF, 0x786EF0FF, 0xC0B4FFFF },
                { 0x1900B8FF, 0x2342B7FF, 0x4A5AFFFF, 0x9198FFFF },
                { 0x3F00C1FF, 0x3631AFFF, 0x6241F6FF, 0x9E7EFFFF },
                { 0x2000C2FF, 0x1A2BB1FF, 0x3C3CF5FF, 0x7973FFFF },
                { 0x0E00BFFF, 0x0012A4FF, 0x101CDAFF, 0x4043D8FF },
                { 0x0500ABFF, 0x000890FF, 0x0010BDFF, 0x282FB6FF },
                { 0x240087FF, 0x13086FFF, 0x231094FF, 0x412E96FF },
                { 0x000531FF, 0x051536FF, 0x0C2148FF, 0x233150FF },
                { 0x4F00A3FF, 0x320980FF, 0x5010B0FF, 0x713AB9FF },
                { 0x5F00C1FF, 0x3B0B96FF, 0x6010D0FF, 0x8641DAFF },
                { 0x6900ABFF, 0x542691FF, 0x8732D2FF, 0xB56DEEFF },
                { 0x7100C8FF, 0x5C35ADFF, 0x9C41FFFF, 0xD689FFFF },
                { 0x8600F6FF, 0x4C03B4FF, 0x7F00FFFF, 0xA63AFFFF },
                { 0x7900B3FF, 0x724FA8FF, 0xBD62FFFF, 0xFFB4FFFF },
                { 0x591097FF, 0x726CABFF, 0xB991FFFF, 0xFFE7FFFF },
                { 0x691D8CFF, 0x887BA9FF, 0xD7A5FFFF, 0xFFFFFFFF },
                { 0x583777FF, 0x8B8BA8FF, 0xD7C3FAFF, 0xFFFFFFFF },
                { 0x733377FF, 0xA291A6FF, 0xF8C6FCFF, 0xFFFFFFFF },
                { 0x9000A6FF, 0x8E5DA3FF, 0xE673FFFF, 0xFFCBFFFF },
                { 0xBA00BAFF, 0x9D499DFF, 0xFF52FFFF, 0xFFABFFFF },
                { 0xBD00C3FF, 0x8B1F90FF, 0xDA20E0FF, 0xFF68FFFF },
                { 0x9E00D8FF, 0x7327A9FF, 0xBD29FFFF, 0xED72FFFF },
                { 0xB100B8FF, 0x7C0F83FF, 0xBD10C5FF, 0xDA4DE0FF },
                { 0x8200AEFF, 0x5B0F84FF, 0x8C14BEFF, 0xAC49D2FF },
                { 0x50006DFF, 0x3D0E58FF, 0x5A187BFF, 0x733C8CFF },
                { 0x5B005BFF, 0x470C47FF, 0x641464FF, 0x773677FF },
                { 0x490066FF, 0x2E0049FF, 0x410062FF, 0x4E1867FF },
                { 0x310043FF, 0x240435FF, 0x320A46FF, 0x401E4FFF },
                { 0x46002CFF, 0x3E0F26FF, 0x551937FF, 0x61314BFF },
                { 0x8F0075FF, 0x6F1157FF, 0xA01982FF, 0xB649A0FF },
                { 0xC60080FF, 0x8F004EFF, 0xC80078FF, 0xD23296FF },
                { 0xBD0085FF, 0xA74273FF, 0xFF50BFFF, 0xFF9FFCFF },
                { 0xAD007BFF, 0xA85579FF, 0xFF6AC5FF, 0xFFBDFFFF },
                { 0x891F51FF, 0xA87574FF, 0xFAA0B9FF, 0xFFF7FFFF },
                { 0xCA0068FF, 0xAD2F52FF, 0xFC3A8CFF, 0xFF7EC5FF },
                { 0xCA006AFF, 0xA21849FF, 0xE61E78FF, 0xF657A4FF },
                { 0xAE003BFF, 0x8C0921FF, 0xBD1039FF, 0xC1385EFF },
                { 0x740033FF, 0x6D2431FF, 0x98344DFF, 0xAD5E75FF },
                { 0x820034FF, 0x6C0C23FF, 0x911437FF, 0x993656FF },
        };

//        {
//            StringBuilder sb = new StringBuilder(1024).append("{\n{ 0x00000000, 0x00000000, 0x00000000, 0x00000000 },\n");
//            for (int i = 1; i < 256; i++) {
//                int color = RAMP_VALUES[i][2] = Coloring.AURORA[i],
//                        r = (color >>> 24),
//                        g = (color >>> 16 & 0xFF),
//                        b = (color >>> 8 & 0xFF);
//                int co = r - b, t = b + (co >> 1), cg = g - t, y = t + (cg >> 1),
//                        yBright = y * 23 >> 4, yDim = y * 11 >> 4, yDark = y * 6 >> 4, chromO, chromG;
//                chromO = (co * 13) >> 4;
//                chromG = (cg * (256 - yDim) * 14) >> 12;
//                t = yDim - (chromG >> 1);
//                g = chromG + t;
//                b = t - (chromO >> 1);
//                r = b + chromO;
//                RAMP_VALUES[i][1] =
//                        MathUtils.clamp(r, 0, 255) << 24 |
//                                MathUtils.clamp(g, 0, 255) << 16 |
//                                MathUtils.clamp(b, 0, 255) << 8 | 0xFF;
//                chromO = (co * 12) >> 4;
//                chromG = (cg * 13) >> 4;
//                t = yBright - (chromG >> 1);
//                g = chromG + t;
//                b = t - (chromO >> 1);
//                r = b + chromO;
//                RAMP_VALUES[i][3] =
//                        MathUtils.clamp(r, 0, 255) << 24 |
//                                MathUtils.clamp(g, 0, 255) << 16 |
//                                MathUtils.clamp(b, 0, 255) << 8 | 0xFF;
//                chromO = (co * 14) >> 4;
//                chromG = (cg * (256 - yDark) * 15) >> 11;
//                t = yDark - (chromG >> 1);
//                g = chromG + t;
//                b = t - (chromO >> 1);
//                r = b + chromO;
//                RAMP_VALUES[i][0] =
//                        MathUtils.clamp(r, 0, 255) << 24 |
//                                MathUtils.clamp(g, 0, 255) << 16 |
//                                MathUtils.clamp(b, 0, 255) << 8 | 0xFF;
//                sb.append("{ 0x");
//                StringKit.appendHex(sb, RAMP_VALUES[i][0]);
//                StringKit.appendHex(sb.append(", 0x"), RAMP_VALUES[i][1]);
//                StringKit.appendHex(sb.append(", 0x"), RAMP_VALUES[i][2]);
//                StringKit.appendHex(sb.append(", 0x"), RAMP_VALUES[i][3]);
//                sb.append(" },\n");
//            }
//            System.out.println(sb.append("};"));
//        }

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
    
    public static final int[] FlesurrectBonusPalette = new int[256];
    private static final int[][] FLESURRECT_BONUS_RAMP_VALUES = new int[][] {
            { 0x00000000, 0x00000000, 0x00000000, 0x00000000 },
            { 0x120023FF, 0x161025FF, 0x1F1833FF, 0x2E1F3DFF },
            { 0x100C23FF, 0x1C1F2EFF, 0x2B2E42FF, 0x3D3D4FFF },
            { 0x1D0C24FF, 0x2A2330FF, 0x3E3546FF, 0x514657FF },
            { 0x151729FF, 0x2B303DFF, 0x414859FF, 0x5B5F6DFF },
            { 0x232A32FF, 0x464D54FF, 0x68717AFF, 0x8D949BFF },
            { 0x2E3E42FF, 0x626E74FF, 0x90A1A8FF, 0xC6D0D8FF },
            { 0x3B4F50FF, 0x7C8B8FFF, 0xB6CBCFFF, 0xFBFFFFFF },
            { 0x47575DFF, 0x909DA4FF, 0xD3E5EDFF, 0xFFFFFFFF },
            { 0x5F5F5FFF, 0xAFAFAFFF, 0xFFFFFFFF, 0xFFFFFFFF },
            { 0x330A1EFF, 0x40272CFF, 0x5C3A41FF, 0x6D4F59FF },
            { 0x3E163EFF, 0x594359FF, 0x826481FF, 0x9F8C9FFF },
            { 0x4C1E2AFF, 0x69494AFF, 0x966C6CFF, 0xB19492FF },
            { 0x4A3823FF, 0x776653FF, 0xAB947AFF, 0xD2C0AEFF },
            { 0x901731FF, 0xAE5657FF, 0xF68181FF, 0xFFC7ADFF },
            { 0xB8001BFF, 0xB22021FF, 0xF53333FF, 0xEF5D5EFF },
            { 0xAB0018FF, 0xB73B30FF, 0xFF5A4AFF, 0xFE9277FF },
            { 0x730214FF, 0x7D2E26FF, 0xAE4539FF, 0xB76660FF },
            { 0x4F1312FF, 0x623629FF, 0x8A503EFF, 0x9C6E63FF },
            { 0x7B1806FF, 0x924626FF, 0xCD683DFF, 0xD9916DFF },
            { 0x843B00FF, 0xB17037FF, 0xFBA458FF, 0xFFD89DFF },
            { 0x9E1800FF, 0xB4480EFF, 0xFB6B1DFF, 0xFE9858FF },
            { 0x463315FF, 0x6F5B42FF, 0x9F8562FF, 0xC0AB93FF },
            { 0x79461DFF, 0xB1835CFF, 0xFCBF8AFF, 0xFFFDD4FF },
            { 0x894300FF, 0xB56C07FF, 0xFF9E17FF, 0xFFC568FF },
            { 0x735600FF, 0xAA7E14FF, 0xF0B628FF, 0xFFD983FF },
            { 0x604F22FF, 0x9E8A65FF, 0xE3C896FF, 0xFFFEE2FF },
            { 0x6B7900FF, 0xB0A011FF, 0xFBE626FF, 0xFFF8A4FF },
            { 0x637800FF, 0xA79500FF, 0xEDD500FF, 0xFFE375FF },
            { 0x617602FF, 0xAEB157FF, 0xFBFF86FF, 0xFFFFFFFF },
            { 0x357600FF, 0x7D962AFF, 0xB4D645FF, 0xFEE6ABFF },
            { 0x1A5300FF, 0x4E672DFF, 0x729446FF, 0x9EAD7DFF },
            { 0x177900FF, 0x629944FF, 0x91DB69FF, 0xE8EACAFF },
            { 0x006600FF, 0x225E07FF, 0x358510FF, 0x509735FF },
            { 0x008300FF, 0x358B28FF, 0x51C43FFF, 0x99CC8CFF },
            { 0x006600FF, 0x307130FF, 0x4BA14AFF, 0x7FB37FFF },
            { 0x007D07FF, 0x0E854EFF, 0x1EBC73FF, 0x6DC7ADFF },
            { 0x008235FF, 0x199D80FF, 0x30E1B9FF, 0xA2EAFFFF },
            { 0x086D3FFF, 0x539C86FF, 0x7FE0C2FF, 0xE3FDFFFF },
            { 0x296A63FF, 0x7BAFB1FF, 0xB8FDFFFF, 0xFFFFFFFF },
            { 0x006C17FF, 0x007053FF, 0x039F78FF, 0x44B09CFF },
            { 0x005B50FF, 0x40878DFF, 0x63C2C9FF, 0xB8E5FFFF },
            { 0x072E62FF, 0x335A87FF, 0x4F83BFFF, 0x85ADD9FF },
            { 0x003339FF, 0x12485AFF, 0x216981FF, 0x4D8395FF },
            { 0x046962FF, 0x52A1A9FF, 0x7FE8F2FF, 0xE9FFFFFF },
            { 0x0F0F61FF, 0x263571FF, 0x3B509FFF, 0x6270ADFF },
            { 0x003A77FF, 0x306AA3FF, 0x4D9BE6FF, 0x91CAFFFF },
            { 0x0E0248FF, 0x191F4FFF, 0x28306FFF, 0x434279FF },
            { 0x091C77FF, 0x2D4B93FF, 0x4870CFFF, 0x799DDFFF },
            { 0x1F008DFF, 0x323598FF, 0x4D50D4FF, 0x757FDBFF },
            { 0x1800ADFF, 0x0D0797FF, 0x180FCFFF, 0x4323CDFF },
            { 0x3D0060FF, 0x391359FF, 0x53207DFF, 0x6D368DFF },
            { 0x4B0084FF, 0x5D3992FF, 0x8657CCFF, 0xA493D9FF },
            { 0x4E128BFF, 0x7358ACFF, 0xA884F3FF, 0xC8D4FFFF },
            { 0x58005CFF, 0x47024AFF, 0x630867FF, 0x78137BFF },
            { 0x6D007CFF, 0x71277FFF, 0xA03EB2FF, 0xB175BFFF },
            { 0x5A008BFF, 0x5A1F87FF, 0x8032BCFF, 0x9862C5FF },
            { 0x6C237EFF, 0x9E70AFFF, 0xE4A8FAFF, 0xFFFFFFFF },
            { 0x7D0057FF, 0x80265DFF, 0xB53D86FF, 0xC16F9EFF },
            { 0xA3009BFF, 0xAC30A5FF, 0xF34FE9FF, 0xEAB8E3FF },
            { 0x530028FF, 0x561E2FFF, 0x7A3045FF, 0x884761FF },
            { 0xA40043FF, 0xAB3251FF, 0xF04F78FF, 0xF08F96FF },
            { 0x6D1339FF, 0x894C59FF, 0xC27182FF, 0xD7A8A7FF },
            { 0x950020FF, 0x901E24FF, 0xC93038FF, 0xCB525FFF },
            { 0x00000000, 0x00000000, 0x00000000, 0x00000000, },
            { 0x1F1833FF, 0x1F1833FF, 0x1F1833FF, 0x1F1833FF, },
            { 0x2B2E42FF, 0x2B2E42FF, 0x2B2E42FF, 0x2B2E42FF, },
            { 0x3E3546FF, 0x3E3546FF, 0x3E3546FF, 0x3E3546FF, },
            { 0x414859FF, 0x414859FF, 0x414859FF, 0x414859FF, },
            { 0x68717AFF, 0x68717AFF, 0x68717AFF, 0x68717AFF, },
            { 0x90A1A8FF, 0x90A1A8FF, 0x90A1A8FF, 0x90A1A8FF, },
            { 0xB6CBCFFF, 0xB6CBCFFF, 0xB6CBCFFF, 0xB6CBCFFF, },
            { 0xD3E5EDFF, 0xD3E5EDFF, 0xD3E5EDFF, 0xD3E5EDFF, },
            { 0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF, },
            { 0x5C3A41FF, 0x5C3A41FF, 0x5C3A41FF, 0x5C3A41FF, },
            { 0x826481FF, 0x826481FF, 0x826481FF, 0x826481FF, },
            { 0x966C6CFF, 0x966C6CFF, 0x966C6CFF, 0x966C6CFF, },
            { 0xAB947AFF, 0xAB947AFF, 0xAB947AFF, 0xAB947AFF, },
            { 0xF68181FF, 0xF68181FF, 0xF68181FF, 0xF68181FF, },
            { 0xF53333FF, 0xF53333FF, 0xF53333FF, 0xF53333FF, },
            { 0xFF5A4AFF, 0xFF5A4AFF, 0xFF5A4AFF, 0xFF5A4AFF, },
            { 0xAE4539FF, 0xAE4539FF, 0xAE4539FF, 0xAE4539FF, },
            { 0x8A503EFF, 0x8A503EFF, 0x8A503EFF, 0x8A503EFF, },
            { 0xCD683DFF, 0xCD683DFF, 0xCD683DFF, 0xCD683DFF, },
            { 0xFBA458FF, 0xFBA458FF, 0xFBA458FF, 0xFBA458FF, },
            { 0xFB6B1DFF, 0xFB6B1DFF, 0xFB6B1DFF, 0xFB6B1DFF, },
            { 0x9F8562FF, 0x9F8562FF, 0x9F8562FF, 0x9F8562FF, },
            { 0xFCBF8AFF, 0xFCBF8AFF, 0xFCBF8AFF, 0xFCBF8AFF, },
            { 0xFF9E17FF, 0xFF9E17FF, 0xFF9E17FF, 0xFF9E17FF, },
            { 0xF0B628FF, 0xF0B628FF, 0xF0B628FF, 0xF0B628FF, },
            { 0xE3C896FF, 0xE3C896FF, 0xE3C896FF, 0xE3C896FF, },
            { 0xFBE626FF, 0xFBE626FF, 0xFBE626FF, 0xFBE626FF, },
            { 0xEDD500FF, 0xEDD500FF, 0xEDD500FF, 0xEDD500FF, },
            { 0xFBFF86FF, 0xFBFF86FF, 0xFBFF86FF, 0xFBFF86FF, },
            { 0xB4D645FF, 0xB4D645FF, 0xB4D645FF, 0xB4D645FF, },
            { 0x729446FF, 0x729446FF, 0x729446FF, 0x729446FF, },
            { 0x91DB69FF, 0x91DB69FF, 0x91DB69FF, 0x91DB69FF, },
            { 0x358510FF, 0x358510FF, 0x358510FF, 0x358510FF, },
            { 0x51C43FFF, 0x51C43FFF, 0x51C43FFF, 0x51C43FFF, },
            { 0x4BA14AFF, 0x4BA14AFF, 0x4BA14AFF, 0x4BA14AFF, },
            { 0x1EBC73FF, 0x1EBC73FF, 0x1EBC73FF, 0x1EBC73FF, },
            { 0x30E1B9FF, 0x30E1B9FF, 0x30E1B9FF, 0x30E1B9FF, },
            { 0x7FE0C2FF, 0x7FE0C2FF, 0x7FE0C2FF, 0x7FE0C2FF, },
            { 0xB8FDFFFF, 0xB8FDFFFF, 0xB8FDFFFF, 0xB8FDFFFF, },
            { 0x039F78FF, 0x039F78FF, 0x039F78FF, 0x039F78FF, },
            { 0x63C2C9FF, 0x63C2C9FF, 0x63C2C9FF, 0x63C2C9FF, },
            { 0x4F83BFFF, 0x4F83BFFF, 0x4F83BFFF, 0x4F83BFFF, },
            { 0x216981FF, 0x216981FF, 0x216981FF, 0x216981FF, },
            { 0x7FE8F2FF, 0x7FE8F2FF, 0x7FE8F2FF, 0x7FE8F2FF, },
            { 0x3B509FFF, 0x3B509FFF, 0x3B509FFF, 0x3B509FFF, },
            { 0x4D9BE6FF, 0x4D9BE6FF, 0x4D9BE6FF, 0x4D9BE6FF, },
            { 0x28306FFF, 0x28306FFF, 0x28306FFF, 0x28306FFF, },
            { 0x4870CFFF, 0x4870CFFF, 0x4870CFFF, 0x4870CFFF, },
            { 0x4D50D4FF, 0x4D50D4FF, 0x4D50D4FF, 0x4D50D4FF, },
            { 0x180FCFFF, 0x180FCFFF, 0x180FCFFF, 0x180FCFFF, },
            { 0x53207DFF, 0x53207DFF, 0x53207DFF, 0x53207DFF, },
            { 0x8657CCFF, 0x8657CCFF, 0x8657CCFF, 0x8657CCFF, },
            { 0xA884F3FF, 0xA884F3FF, 0xA884F3FF, 0xA884F3FF, },
            { 0x630867FF, 0x630867FF, 0x630867FF, 0x630867FF, },
            { 0xA03EB2FF, 0xA03EB2FF, 0xA03EB2FF, 0xA03EB2FF, },
            { 0x8032BCFF, 0x8032BCFF, 0x8032BCFF, 0x8032BCFF, },
            { 0xE4A8FAFF, 0xE4A8FAFF, 0xE4A8FAFF, 0xE4A8FAFF, },
            { 0xB53D86FF, 0xB53D86FF, 0xB53D86FF, 0xB53D86FF, },
            { 0xF34FE9FF, 0xF34FE9FF, 0xF34FE9FF, 0xF34FE9FF, },
            { 0x7A3045FF, 0x7A3045FF, 0x7A3045FF, 0x7A3045FF, },
            { 0xF04F78FF, 0xF04F78FF, 0xF04F78FF, 0xF04F78FF, },
            { 0xC27182FF, 0xC27182FF, 0xC27182FF, 0xC27182FF, },
            { 0xC93038FF, 0xC93038FF, 0xC93038FF, 0xC93038FF, },
            { 0x00000000, 0x00000000, 0x00000000, 0x00000000 },
            { 0x120023FF, 0x161025FF, 0x1F1833FF, 0x2E1F3DFF },
            { 0x100C23FF, 0x1C1F2EFF, 0x2B2E42FF, 0x3D3D4FFF },
            { 0x1D0C24FF, 0x2A2330FF, 0x3E3546FF, 0x514657FF },
            { 0x151729FF, 0x2B303DFF, 0x414859FF, 0x5B5F6DFF },
            { 0x232A32FF, 0x464D54FF, 0x68717AFF, 0x8D949BFF },
            { 0x2E3E42FF, 0x626E74FF, 0x90A1A8FF, 0xC6D0D8FF },
            { 0x3B4F50FF, 0x7C8B8FFF, 0xB6CBCFFF, 0xFBFFFFFF },
            { 0x47575DFF, 0x909DA4FF, 0xD3E5EDFF, 0xFFFFFFFF },
            { 0x5F5F5FFF, 0xAFAFAFFF, 0xFFFFFFFF, 0xFFFFFFFF },
            { 0x330A1EFF, 0x40272CFF, 0x5C3A41FF, 0x6D4F59FF },
            { 0x3E163EFF, 0x594359FF, 0x826481FF, 0x9F8C9FFF },
            { 0x4C1E2AFF, 0x69494AFF, 0x966C6CFF, 0xB19492FF },
            { 0x4A3823FF, 0x776653FF, 0xAB947AFF, 0xD2C0AEFF },
            { 0x901731FF, 0xAE5657FF, 0xF68181FF, 0xFFC7ADFF },
            { 0xB8001BFF, 0xB22021FF, 0xF53333FF, 0xEF5D5EFF },
            { 0xAB0018FF, 0xB73B30FF, 0xFF5A4AFF, 0xFE9277FF },
            { 0x730214FF, 0x7D2E26FF, 0xAE4539FF, 0xB76660FF },
            { 0x4F1312FF, 0x623629FF, 0x8A503EFF, 0x9C6E63FF },
            { 0x7B1806FF, 0x924626FF, 0xCD683DFF, 0xD9916DFF },
            { 0x843B00FF, 0xB17037FF, 0xFBA458FF, 0xFFD89DFF },
            { 0x9E1800FF, 0xB4480EFF, 0xFB6B1DFF, 0xFE9858FF },
            { 0x463315FF, 0x6F5B42FF, 0x9F8562FF, 0xC0AB93FF },
            { 0x79461DFF, 0xB1835CFF, 0xFCBF8AFF, 0xFFFDD4FF },
            { 0x894300FF, 0xB56C07FF, 0xFF9E17FF, 0xFFC568FF },
            { 0x735600FF, 0xAA7E14FF, 0xF0B628FF, 0xFFD983FF },
            { 0x604F22FF, 0x9E8A65FF, 0xE3C896FF, 0xFFFEE2FF },
            { 0x6B7900FF, 0xB0A011FF, 0xFBE626FF, 0xFFF8A4FF },
            { 0x637800FF, 0xA79500FF, 0xEDD500FF, 0xFFE375FF },
            { 0x617602FF, 0xAEB157FF, 0xFBFF86FF, 0xFFFFFFFF },
            { 0x357600FF, 0x7D962AFF, 0xB4D645FF, 0xFEE6ABFF },
            { 0x1A5300FF, 0x4E672DFF, 0x729446FF, 0x9EAD7DFF },
            { 0x177900FF, 0x629944FF, 0x91DB69FF, 0xE8EACAFF },
            { 0x006600FF, 0x225E07FF, 0x358510FF, 0x509735FF },
            { 0x008300FF, 0x358B28FF, 0x51C43FFF, 0x99CC8CFF },
            { 0x006600FF, 0x307130FF, 0x4BA14AFF, 0x7FB37FFF },
            { 0x007D07FF, 0x0E854EFF, 0x1EBC73FF, 0x6DC7ADFF },
            { 0x008235FF, 0x199D80FF, 0x30E1B9FF, 0xA2EAFFFF },
            { 0x086D3FFF, 0x539C86FF, 0x7FE0C2FF, 0xE3FDFFFF },
            { 0x296A63FF, 0x7BAFB1FF, 0xB8FDFFFF, 0xFFFFFFFF },
            { 0x006C17FF, 0x007053FF, 0x039F78FF, 0x44B09CFF },
            { 0x005B50FF, 0x40878DFF, 0x63C2C9FF, 0xB8E5FFFF },
            { 0x072E62FF, 0x335A87FF, 0x4F83BFFF, 0x85ADD9FF },
            { 0x003339FF, 0x12485AFF, 0x216981FF, 0x4D8395FF },
            { 0x046962FF, 0x52A1A9FF, 0x7FE8F2FF, 0xE9FFFFFF },
            { 0x0F0F61FF, 0x263571FF, 0x3B509FFF, 0x6270ADFF },
            { 0x003A77FF, 0x306AA3FF, 0x4D9BE6FF, 0x91CAFFFF },
            { 0x0E0248FF, 0x191F4FFF, 0x28306FFF, 0x434279FF },
            { 0x091C77FF, 0x2D4B93FF, 0x4870CFFF, 0x799DDFFF },
            { 0x1F008DFF, 0x323598FF, 0x4D50D4FF, 0x757FDBFF },
            { 0x1800ADFF, 0x0D0797FF, 0x180FCFFF, 0x4323CDFF },
            { 0x3D0060FF, 0x391359FF, 0x53207DFF, 0x6D368DFF },
            { 0x4B0084FF, 0x5D3992FF, 0x8657CCFF, 0xA493D9FF },
            { 0x4E128BFF, 0x7358ACFF, 0xA884F3FF, 0xC8D4FFFF },
            { 0x58005CFF, 0x47024AFF, 0x630867FF, 0x78137BFF },
            { 0x6D007CFF, 0x71277FFF, 0xA03EB2FF, 0xB175BFFF },
            { 0x5A008BFF, 0x5A1F87FF, 0x8032BCFF, 0x9862C5FF },
            { 0x6C237EFF, 0x9E70AFFF, 0xE4A8FAFF, 0xFFFFFFFF },
            { 0x7D0057FF, 0x80265DFF, 0xB53D86FF, 0xC16F9EFF },
            { 0xA3009BFF, 0xAC30A5FF, 0xF34FE9FF, 0xEAB8E3FF },
            { 0x530028FF, 0x561E2FFF, 0x7A3045FF, 0x884761FF },
            { 0xA40043FF, 0xAB3251FF, 0xF04F78FF, 0xF08F96FF },
            { 0x6D1339FF, 0x894C59FF, 0xC27182FF, 0xD7A8A7FF },
            { 0x950020FF, 0x901E24FF, 0xC93038FF, 0xCB525FFF },
            { 0x00000000, 0x00000000, 0x00000000, 0x00000000 },
            { 0x1F1833FF, 0x161025FF, 0x1F1833FF, 0x2E1F3DFF },
            { 0x2B2E42FF, 0x1C1F2EFF, 0x2B2E42FF, 0x3D3D4FFF },
            { 0x3E3546FF, 0x2A2330FF, 0x3E3546FF, 0x514657FF },
            { 0x414859FF, 0x2B303DFF, 0x414859FF, 0x5B5F6DFF },
            { 0x68717AFF, 0x464D54FF, 0x68717AFF, 0x8D949BFF },
            { 0x90A1A8FF, 0x626E74FF, 0x90A1A8FF, 0xC6D0D8FF },
            { 0xB6CBCFFF, 0x7C8B8FFF, 0xB6CBCFFF, 0xFBFFFFFF },
            { 0xD3E5EDFF, 0x909DA4FF, 0xD3E5EDFF, 0xFFFFFFFF },
            { 0xFFFFFFFF, 0xAFAFAFFF, 0xFFFFFFFF, 0xFFFFFFFF },
            { 0x5C3A41FF, 0x40272CFF, 0x5C3A41FF, 0x6D4F59FF },
            { 0x826481FF, 0x594359FF, 0x826481FF, 0x9F8C9FFF },
            { 0x966C6CFF, 0x69494AFF, 0x966C6CFF, 0xB19492FF },
            { 0xAB947AFF, 0x776653FF, 0xAB947AFF, 0xD2C0AEFF },
            { 0xF68181FF, 0xAE5657FF, 0xF68181FF, 0xFFC7ADFF },
            { 0xF53333FF, 0xB22021FF, 0xF53333FF, 0xEF5D5EFF },
            { 0xFF5A4AFF, 0xB73B30FF, 0xFF5A4AFF, 0xFE9277FF },
            { 0xAE4539FF, 0x7D2E26FF, 0xAE4539FF, 0xB76660FF },
            { 0x8A503EFF, 0x623629FF, 0x8A503EFF, 0x9C6E63FF },
            { 0xCD683DFF, 0x924626FF, 0xCD683DFF, 0xD9916DFF },
            { 0xFBA458FF, 0xB17037FF, 0xFBA458FF, 0xFFD89DFF },
            { 0xFB6B1DFF, 0xB4480EFF, 0xFB6B1DFF, 0xFE9858FF },
            { 0x9F8562FF, 0x6F5B42FF, 0x9F8562FF, 0xC0AB93FF },
            { 0xFCBF8AFF, 0xB1835CFF, 0xFCBF8AFF, 0xFFFDD4FF },
            { 0xFF9E17FF, 0xB56C07FF, 0xFF9E17FF, 0xFFC568FF },
            { 0xF0B628FF, 0xAA7E14FF, 0xF0B628FF, 0xFFD983FF },
            { 0xE3C896FF, 0x9E8A65FF, 0xE3C896FF, 0xFFFEE2FF },
            { 0xFBE626FF, 0xB0A011FF, 0xFBE626FF, 0xFFF8A4FF },
            { 0xEDD500FF, 0xA79500FF, 0xEDD500FF, 0xFFE375FF },
            { 0xFBFF86FF, 0xAEB157FF, 0xFBFF86FF, 0xFFFFFFFF },
            { 0xB4D645FF, 0x7D962AFF, 0xB4D645FF, 0xFEE6ABFF },
            { 0x729446FF, 0x4E672DFF, 0x729446FF, 0x9EAD7DFF },
            { 0x91DB69FF, 0x629944FF, 0x91DB69FF, 0xE8EACAFF },
            { 0x358510FF, 0x225E07FF, 0x358510FF, 0x509735FF },
            { 0x51C43FFF, 0x358B28FF, 0x51C43FFF, 0x99CC8CFF },
            { 0x4BA14AFF, 0x307130FF, 0x4BA14AFF, 0x7FB37FFF },
            { 0x1EBC73FF, 0x0E854EFF, 0x1EBC73FF, 0x6DC7ADFF },
            { 0x30E1B9FF, 0x199D80FF, 0x30E1B9FF, 0xA2EAFFFF },
            { 0x7FE0C2FF, 0x539C86FF, 0x7FE0C2FF, 0xE3FDFFFF },
            { 0xB8FDFFFF, 0x7BAFB1FF, 0xB8FDFFFF, 0xFFFFFFFF },
            { 0x039F78FF, 0x007053FF, 0x039F78FF, 0x44B09CFF },
            { 0x63C2C9FF, 0x40878DFF, 0x63C2C9FF, 0xB8E5FFFF },
            { 0x4F83BFFF, 0x335A87FF, 0x4F83BFFF, 0x85ADD9FF },
            { 0x216981FF, 0x12485AFF, 0x216981FF, 0x4D8395FF },
            { 0x7FE8F2FF, 0x52A1A9FF, 0x7FE8F2FF, 0xE9FFFFFF },
            { 0x3B509FFF, 0x263571FF, 0x3B509FFF, 0x6270ADFF },
            { 0x4D9BE6FF, 0x306AA3FF, 0x4D9BE6FF, 0x91CAFFFF },
            { 0x28306FFF, 0x191F4FFF, 0x28306FFF, 0x434279FF },
            { 0x4870CFFF, 0x2D4B93FF, 0x4870CFFF, 0x799DDFFF },
            { 0x4D50D4FF, 0x323598FF, 0x4D50D4FF, 0x757FDBFF },
            { 0x180FCFFF, 0x0D0797FF, 0x180FCFFF, 0x4323CDFF },
            { 0x53207DFF, 0x391359FF, 0x53207DFF, 0x6D368DFF },
            { 0x8657CCFF, 0x5D3992FF, 0x8657CCFF, 0xA493D9FF },
            { 0xA884F3FF, 0x7358ACFF, 0xA884F3FF, 0xC8D4FFFF },
            { 0x630867FF, 0x47024AFF, 0x630867FF, 0x78137BFF },
            { 0xA03EB2FF, 0x71277FFF, 0xA03EB2FF, 0xB175BFFF },
            { 0x8032BCFF, 0x5A1F87FF, 0x8032BCFF, 0x9862C5FF },
            { 0xE4A8FAFF, 0x9E70AFFF, 0xE4A8FAFF, 0xFFFFFFFF },
            { 0xB53D86FF, 0x80265DFF, 0xB53D86FF, 0xC16F9EFF },
            { 0xF34FE9FF, 0xAC30A5FF, 0xF34FE9FF, 0xEAB8E3FF },
            { 0x7A3045FF, 0x561E2FFF, 0x7A3045FF, 0x884761FF },
            { 0xF04F78FF, 0xAB3251FF, 0xF04F78FF, 0xF08F96FF },
            { 0xC27182FF, 0x894C59FF, 0xC27182FF, 0xD7A8A7FF },
            { 0xC93038FF, 0x901E24FF, 0xC93038FF, 0xCB525FFF },
    };

    //        public final int[] ALL_COLORS = new int[256];
    static {
        for (int i = 0; i < 64; i++) {
//                for (int j = 0; j < 4; j++) {
//                    FlesurrectBonusPalette[i << 2 | j] = FLESURRECT_BONUS_RAMP_VALUES[i][j];
//                }
            System.arraycopy(FLESURRECT_BONUS_RAMP_VALUES[i], 0, FlesurrectBonusPalette, i << 2, 4);
        }
    }
    //new PaletteReducer(FlesurrectBonusPalette)
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
            // the second half of voxels (with bit 0x40 set) don't shade visually, but Colorizer uses this method to
            // denote a structural change to the voxel's makeup, so this uses the first 64 voxel colors to shade both
            // halves, then marks voxels from the second half back to being an unshaded voxel as the last step.
            return (byte) (Dimmer.FLESURRECT_RAMPS[voxel & 0x3F][3] | (voxel & 0xC0));
        }

        @Override
        public byte darken(byte voxel) {
            // the second half of voxels (with bit 0x40 set) don't shade visually, but Colorizer uses this method to
            // denote a structural change to the voxel's makeup, so this uses the first 64 voxel colors to shade both
            // halves, then marks voxels from the second half back to being an unshaded voxel as the last step.
            return (byte) (Dimmer.FLESURRECT_RAMPS[voxel & 0x3F][1] | (voxel & 0xC0));
        }


//        {
//            StringBuilder sb = new StringBuilder(1024).append("{\n{ 0x00000000, 0x00000000, 0x00000000, 0x00000000 },\n");
//            for (int i = 1; i < 64; i++) {
//                int color = FLESURRECT_BONUS_RAMP_VALUES[i][2] = Coloring.FLESURRECT[i],
//                        r = (color >>> 24),
//                        g = (color >>> 16 & 0xFF),
//                        b = (color >>> 8 & 0xFF);
//                int co = r - b, t = b + (co >> 1), cg = g - t, y = t + (cg >> 1),
//                        yBright = y * 21 >> 4, yDim = y * 11 >> 4, yDark = y * 6 >> 4, chromO, chromG;
//                chromO = (co * 3) >> 2;
//                chromG = (cg * 3) >> 2;
//                t = yDim - (chromG >> 1);
//                g = chromG + t;
//                b = t - (chromO >> 1);
//                r = b + chromO;
//                FLESURRECT_BONUS_RAMP_VALUES[i][1] =
//                        MathUtils.clamp(r, 0, 255) << 24 |
//                                MathUtils.clamp(g, 0, 255) << 16 |
//                                MathUtils.clamp(b, 0, 255) << 8 | 0xFF;
//                chromO = (co * 3) >> 2;
//                chromG = (cg * (256 - yBright) * 3) >> 9;
//                t = yBright - (chromG >> 1);
//                g = chromG + t;
//                b = t - (chromO >> 1);
//                r = b + chromO;
//                FLESURRECT_BONUS_RAMP_VALUES[i][3] =
//                        MathUtils.clamp(r, 0, 255) << 24 |
//                                MathUtils.clamp(g, 0, 255) << 16 |
//                                MathUtils.clamp(b, 0, 255) << 8 | 0xFF;
//                chromO = (co * 13) >> 4;
//                chromG = (cg * (256 - yDark) * 13) >> 11;
//                t = yDark - (chromG >> 1);
//                g = chromG + t;
//                b = t - (chromO >> 1);
//                r = b + chromO;
//                FLESURRECT_BONUS_RAMP_VALUES[i][0] =
//                        MathUtils.clamp(r, 0, 255) << 24 |
//                                MathUtils.clamp(g, 0, 255) << 16 |
//                                MathUtils.clamp(b, 0, 255) << 8 | 0xFF;
//                sb.append("{ 0x");
//                StringKit.appendHex(sb, FLESURRECT_BONUS_RAMP_VALUES[i][0]);
//                StringKit.appendHex(sb.append(", 0x"), FLESURRECT_BONUS_RAMP_VALUES[i][1]);
//                StringKit.appendHex(sb.append(", 0x"), FLESURRECT_BONUS_RAMP_VALUES[i][2]);
//                StringKit.appendHex(sb.append(", 0x"), FLESURRECT_BONUS_RAMP_VALUES[i][3]);
//                sb.append(" },\n");
//            }
//            System.out.println(sb.append("};"));
//        }

        @Override
        public int dimmer(int brightness, byte voxel) {
            return FLESURRECT_BONUS_RAMP_VALUES[voxel & 0xFF][
                    brightness <= 0
                            ? 0
                            : brightness >= 3
                            ? 3
                            : brightness
                    ];
        }

        @Override
        public int getShadeBit() {
            return 0x40;
        }
        @Override
        public int getWaveBit() {
            return 0x80;
        }
    };

    public static Colorizer arbitraryColorizer(final int[] palette) {
        final int COUNT = palette.length;
        PaletteReducer reducer = new PaletteReducer(palette);

        final byte[] primary = {
                reducer.reduceIndex(0xFF0000FF), reducer.reduceIndex(0xFFFF00FF), reducer.reduceIndex(0x00FF00FF),
                reducer.reduceIndex(0x00FFFFFF), reducer.reduceIndex(0x0000FFFF), reducer.reduceIndex(0xFF00FFFF)
        }, grays = {
                reducer.reduceIndex(0x000000FF), reducer.reduceIndex(0x444444FF), reducer.reduceIndex(0x888888FF),
                reducer.reduceIndex(0xCCCCCCFF), reducer.reduceIndex(0xFFFFFFFF)
        };
        final int THRESHOLD = 64;//0.011; // threshold controls the "stark-ness" of color changes; must not be negative.
        final byte[] paletteMapping = new byte[1 << 16];
        final int[] reverse = new int[COUNT];
        final byte[][] ramps = new byte[COUNT][4];
        final int[] lumas = new int[COUNT], cos = new int[COUNT], cgs = new int[COUNT];
        final int yLim = 63, coLim = 31, cgLim = 31, shift1 = 6, shift2 = 11;
        int color, r, g, b, co, cg, t;
        for (int i = 1; i < COUNT; i++) {
            color = palette[i];
            if((color & 0x80) == 0)
            {
                lumas[i] = -0x70000000; // very very negative, blocks transparent colors from mixing into opaque ones
                continue;
            }
            r = (color >>> 24);
            g = (color >>> 16 & 0xFF);
            b = (color >>> 8 & 0xFF);
            co = r - b;
            t = b + (co >> 1);
            cg = g - t;
            paletteMapping[
                    reverse[i] = 
                              (lumas[i] = luma(r, g, b) >>> 11)
                            | (cos[i] = co + 255 >>> 4) << shift1
                            | (cgs[i] = cg + 255 >>> 4) << shift2] = (byte) i;
        }

        for (int icg = 0; icg <= cgLim; icg++) {
            for (int ico = 0; ico <= coLim; ico++) {
                for (int iy = 0; iy <= yLim; iy++) {
                    final int c2 = icg << shift2 | ico << shift1 | iy;
                    if (paletteMapping[c2] == 0) {
                        int dist = 0x7FFFFFFF;
                        for (int i = 1; i < COUNT; i++) {
                            if (Math.abs(lumas[i] - iy) < 28 && dist > (dist = Math.min(dist, difference(lumas[i], cos[i], cgs[i], iy, ico, icg))))
                                paletteMapping[c2] = (byte) i;
                        }
                    }
                }
            }
        }

        float adj, cof, cgf;
        int idx2;
//        System.out.println("{\n{ 0, 0, 0, 0 },");
        for (int i = 1; i < COUNT; i++) {
            int rev = reverse[i], y = rev & yLim, match = i;
            cof = ((co = cos[i]) - 16) * 0x1.111112p-5f;
            cgf = ((cg = cgs[i]) - 16) * 0x1.111112p-5f;
            ramps[i][2] = (byte)i;
            ramps[i][3] = grays[4];//15;  //0xFFFFFFFF, white
            ramps[i][1] = grays[0];//0x010101FF, black
            ramps[i][0] = grays[0];//0x010101FF, black
            for (int yy = y + 2, rr = rev + 2; yy <= yLim; yy++, rr++) {
                if ((idx2 = paletteMapping[rr] & 255) != i && difference(lumas[idx2], cos[idx2], cgs[idx2], y, co, cg) > THRESHOLD) {
                    ramps[i][3] = paletteMapping[rr];
                    break;
                }
                adj = 1f + ((yLim + 1 >>> 1) - yy) * 0x1p-10f;
                cof = MathUtils.clamp(cof * adj, -0.5f, 0.5f);
                cgf = MathUtils.clamp(cgf * adj + 0x1.8p-10f, -0.5f, 0.5f);

                rr = yy
                        | (co = (int) ((cof + 0.5f) * coLim)) << shift1
                        | (cg = (int) ((cgf + 0.5f) * cgLim)) << shift2;
            }
            cof = ((co = cos[i]) - 16) * 0x0.Bp-5f;
            cgf = ((cg = cgs[i]) - 16) * 0x0.Bp-5f;
            for (int yy = y - 2, rr = rev - 2; yy > 0; rr--) {
                if ((idx2 = paletteMapping[rr] & 255) != i && difference(lumas[idx2], cos[idx2], cgs[idx2], y, co, cg) > THRESHOLD) {
                    ramps[i][1] = paletteMapping[rr];
                    rev = rr;
                    y = yy;
                    match = paletteMapping[rr] & 255;
                    break;
                }
                adj = 1f + (yy - (yLim + 1 >>> 1)) * 0x1p-10f;
                cof = MathUtils.clamp(cof * adj, -0.5f, 0.5f);
                cgf = MathUtils.clamp(cgf * adj - 0x1.8p-10f, -0.5f, 0.5f);

//                cof = (cof - 0.5f) * 0.984375f + 0.5f;
//                cgf = (cgf + 0.5f) * 0.984375f - 0.5f;
                rr = yy
                        | (co = (int) ((cof + 0.5f) * coLim)) << shift1
                        | (cg = (int) ((cgf + 0.5f) * cgLim)) << shift2;

//                cof = MathUtils.clamp(cof * 0.9375f, -0.5f, 0.5f);
//                cgf = MathUtils.clamp(cgf * 0.9375f, -0.5f, 0.5f);
//                rr = yy
//                        | (int) ((cof + 0.5f) * 63) << 7
//                        | (int) ((cgf + 0.5f) * 63) << 13;
                if (--yy == 0) {
                    match = -1;
                }
            }
            if (match >= 0) {
                cof = ((co = cos[match]) - 16) * 0x1.111112p-5f;
                cgf = ((cg = cgs[match]) - 16) * 0x1.111112p-5f;
                for (int yy = y - 3, rr = rev - 3; yy > 0; yy--, rr--) {
                    if ((idx2 = paletteMapping[rr] & 255) != match && difference(lumas[idx2], cos[idx2], cgs[idx2], y, co, cg) > THRESHOLD) {
                        ramps[i][0] = paletteMapping[rr];
                        break;
                    }
                    adj = 1f + (yy - (yLim + 1 >>> 1)) * 0x1p-10f;
                    cof = MathUtils.clamp(cof * adj, -0.5f, 0.5f);
                    cgf = MathUtils.clamp(cgf * adj - 0x1.8p-10f, -0.5f, 0.5f);

//                    cof = (cof - 0.5f) * 0.96875f + 0.5f;
//                    cgf = (cgf + 0.5f) * 0.96875f - 0.5f;
                    rr = yy
                            | (co = (int) ((cof + 0.5f) * coLim)) << shift1
                            | (cg = (int) ((cgf + 0.5f) * cgLim)) << shift2;

//                    cof = MathUtils.clamp(cof * 0.9375f, -0.5f, 0.5f);
//                    cgf = MathUtils.clamp(cgf * 0.9375f, -0.5f, 0.5f);
//                    rr = yy
//                            | (int) ((cof + 0.5f) * 63) << 7
//                            | (int) ((cgf + 0.5f) * 63) << 13;
                }
            }
//            System.out.println("{ " + ramps[i][0] + ", " + ramps[i][1] + ", " + ramps[i][2] + ", " + ramps[i][3] + " },");
        }
//        System.out.println("};");


        return new Colorizer(reducer) {

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
                return ramps[(voxel & 0xFF) % COUNT][3];
            }

            @Override
            public byte darken(byte voxel) {
                // the second half of voxels (with bit 0x40 set) don't shade visually, but Colorizer uses this method to
                // denote a structural change to the voxel's makeup, so this uses the first 64 voxel colors to shade both
                // halves, then marks voxels from the second half back to being an unshaded voxel as the last step.
                return ramps[(voxel & 0xFF) % COUNT][1];
            }

            @Override
            public int dimmer(int brightness, byte voxel) {
                return palette[ramps[(voxel & 0xFF) % COUNT][
                        brightness <= 0
                                ? 0
                                : brightness >= 3
                                ? 3
                                : brightness
                        ] & 0xFF];
            }

            @Override
            public int getShadeBit() {
                return 0;
            }

            @Override
            public int getWaveBit() {
                return 0;
            }
        };
    }
    public static Colorizer arbitraryBonusColorizer(final int[] palette) {
        final int COUNT = palette.length;
        PaletteReducer reducer = new PaletteReducer(palette);

        final byte[] primary = {
                reducer.reduceIndex(0xFF0000FF), reducer.reduceIndex(0xFFFF00FF), reducer.reduceIndex(0x00FF00FF),
                reducer.reduceIndex(0x00FFFFFF), reducer.reduceIndex(0x0000FFFF), reducer.reduceIndex(0xFF00FFFF)
        }, grays = {
                reducer.reduceIndex(0x000000FF), reducer.reduceIndex(0x444444FF), reducer.reduceIndex(0x888888FF),
                reducer.reduceIndex(0xCCCCCCFF), reducer.reduceIndex(0xFFFFFFFF)
        };
        final int THRESHOLD = 64;//0.011; // threshold controls the "stark-ness" of color changes; must not be negative.
        final byte[] paletteMapping = new byte[1 << 16];
        final int[] reverse = new int[COUNT];
        final byte[][] ramps = new byte[COUNT][4];
        final int[][] values = new int[COUNT][4];
        
        final int[] lumas = new int[COUNT], cos = new int[COUNT], cgs = new int[COUNT];
        final int yLim = 63, coLim = 31, cgLim = 31, shift1 = 6, shift2 = 11;
        int color, r, g, b, co, cg, t;
        for (int i = 1; i < COUNT; i++) {
            color = palette[i];
            if((color & 0x80) == 0)
            {
                lumas[i] = -0x70000000; // very very negative, blocks transparent colors from mixing into opaque ones
                continue;
            }
            r = (color >>> 24);
            g = (color >>> 16 & 0xFF);
            b = (color >>> 8 & 0xFF);
            co = r - b;
            t = b + (co >> 1);
            cg = g - t;
            paletteMapping[
                    reverse[i] = 
                              (lumas[i] = luma(r, g, b) >>> 11)
                            | (cos[i] = co + 255 >>> 4) << shift1
                            | (cgs[i] = cg + 255 >>> 4) << shift2] = (byte) i;
        }

        for (int icg = 0; icg <= cgLim; icg++) {
            for (int ico = 0; ico <= coLim; ico++) {
                for (int iy = 0; iy <= yLim; iy++) {
                    final int c2 = icg << shift2 | ico << shift1 | iy;
                    if (paletteMapping[c2] == 0) {
                        int dist = 0x7FFFFFFF;
                        for (int i = 1; i < COUNT; i++) {
                            if (Math.abs(lumas[i] - iy) < 28 && dist > (dist = Math.min(dist, difference(lumas[i], cos[i], cgs[i], iy, ico, icg))))
                                paletteMapping[c2] = (byte) i;
                        }
                    }
                }
            }
        }

        float adj, cof, cgf;
        int idx2;
        for (int i = 1; i < COUNT; i++) {
            int rev = reverse[i], y = rev & yLim, match = i,
                    yBright = y * 13 >> 1, yDim = y * 5 >> 1, yDark = y << 1, chromO, chromG;

            cof = ((co = cos[i]) - 16) * 0x1.111112p-5f;
            cgf = ((cg = cgs[i]) - 16) * 0x1.111112p-5f;

            //values[i][0] = values[i][1] = values[i][3] = 
            values[i][2] = palette[i];             

            chromO = (co * 395 + 31 >> 5) - 192;
            chromG = (cg * 395 + 31 >> 5) - 192;
            t = yDim - (chromG >> 1);
            g = chromG + t;
            b = t - (chromO >> 1);
            r = b + chromO;

            values[i][1] =
                    MathUtils.clamp(r, 0, 255) << 24 |
                            MathUtils.clamp(g, 0, 255) << 16 |
                            MathUtils.clamp(b, 0, 255) << 8 | 0xFF;
            chromO = (co * 333 + 31 >> 5) - 162;
            chromG = (cg * 333 + 31 >> 5) - 162;//(cg * (256 - yBright) * 395 + 4095 >> 12) - 192;
            t = yBright - (chromG >> 1);
            g = chromG + t;
            b = t - (chromO >> 1);
            r = b + chromO;
            values[i][3] =
                    MathUtils.clamp(r, 0, 255) << 24 |
                            MathUtils.clamp(g, 0, 255) << 16 |
                            MathUtils.clamp(b, 0, 255) << 8 | 0xFF;
            chromO = (co * 215 >> 4) - 208;
            chromG = (cg * 215 >> 4) - 208;//(cg * (256 - yDark) * 215 >> 11) - 208;
            t = yDark - (chromG >> 1);
            g = chromG + t;
            b = t - (chromO >> 1);
            r = b + chromO;
            values[i][0] =
                    MathUtils.clamp(r, 0, 255) << 24 |
                            MathUtils.clamp(g, 0, 255) << 16 |
                            MathUtils.clamp(b, 0, 255) << 8 | 0xFF;

            ramps[i][2] = (byte)i;
            ramps[i][3] = grays[4];//15;  //0xFFFFFFFF, white
            ramps[i][1] = grays[0];//0x010101FF, black
            ramps[i][0] = grays[0];//0x010101FF, black
            for (int yy = y + 2, rr = rev + 2; yy <= yLim; yy++, rr++) {
                if ((idx2 = paletteMapping[rr] & 255) != i && difference(lumas[idx2], cos[idx2], cgs[idx2], y, co, cg) > THRESHOLD) {
                    ramps[i][3] = paletteMapping[rr];
                    break;
                }
                adj = 1f + ((yLim + 1 >>> 1) - yy) * 0x1p-10f;
                cof = MathUtils.clamp(cof * adj, -0.5f, 0.5f);
                cgf = MathUtils.clamp(cgf * adj + 0x1.8p-10f, -0.5f, 0.5f);

                rr = yy
                        | (co = (int) ((cof + 0.5f) * coLim)) << shift1
                        | (cg = (int) ((cgf + 0.5f) * cgLim)) << shift2;
            }
            cof = ((co = cos[i]) - 16) * 0x0.Bp-5f;
            cgf = ((cg = cgs[i]) - 16) * 0x0.Bp-5f;
            for (int yy = y - 2, rr = rev - 2; yy > 0; rr--) {
                if ((idx2 = paletteMapping[rr] & 255) != i && difference(lumas[idx2], cos[idx2], cgs[idx2], y, co, cg) > THRESHOLD) {
                    ramps[i][1] = paletteMapping[rr];
                    rev = rr;
                    y = yy;
                    match = paletteMapping[rr] & 255;
                    break;
                }
                adj = 1f + (yy - (yLim + 1 >>> 1)) * 0x1p-10f;
                cof = MathUtils.clamp(cof * adj, -0.5f, 0.5f);
                cgf = MathUtils.clamp(cgf * adj - 0x1.8p-10f, -0.5f, 0.5f);

                rr = yy
                        | (co = (int) ((cof + 0.5f) * coLim)) << shift1
                        | (cg = (int) ((cgf + 0.5f) * cgLim)) << shift2;

                if (--yy == 0) {
                    match = -1;
                }
            }
            if (match >= 0) {
                cof = ((co = cos[match]) - 16) * 0x1.111112p-5f;
                cgf = ((cg = cgs[match]) - 16) * 0x1.111112p-5f;
                for (int yy = y - 3, rr = rev - 3; yy > 0; yy--, rr--) {
                    if ((idx2 = paletteMapping[rr] & 255) != match && difference(lumas[idx2], cos[idx2], cgs[idx2], y, co, cg) > THRESHOLD) {
                        ramps[i][0] = paletteMapping[rr];
                        break;
                    }
                    adj = 1f + (yy - (yLim + 1 >>> 1)) * 0x1p-10f;
                    cof = MathUtils.clamp(cof * adj, -0.5f, 0.5f);
                    cgf = MathUtils.clamp(cgf * adj - 0x1.8p-10f, -0.5f, 0.5f);
                    
                    rr = yy
                            | (co = (int) ((cof + 0.5f) * coLim)) << shift1
                            | (cg = (int) ((cgf + 0.5f) * cgLim)) << shift2;
                }
            }
        }


        return new Colorizer(reducer) {

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
                return ramps[(voxel & 0xFF) % COUNT][3];
            }

            @Override
            public byte darken(byte voxel) {
                return ramps[(voxel & 0xFF) % COUNT][1];
            }

            @Override
            public int dimmer(int brightness, byte voxel) {
                return values[voxel & 0xFF][
                        brightness <= 0
                                ? 0
                                : brightness >= 3
                                ? 3
                                : brightness
                        ];
            }

            @Override
            public int getShadeBit() {
                return 0;
            }

            @Override
            public int getWaveBit() {
                return 0;
            }
        };
    }
    public static final Colorizer RinsedColorizer = new Colorizer(new PaletteReducer(Coloring.RINSED,
            "\027\027\027\027\027\027¿¿¿¿¿¿¿¿¿¿ÝÝÝÝÝÝÝÝÝÝÜÜÜÜÜÜ\027\027\027\02777¿¿¿¿¿¿¿¿¿¾¾ÝÝÝÝÝÝÝÝÝÜÜÜÜÜÜ\027\027\02777¿¿¿¿¿¿¿¿¿¾¾¾ÝÝÝÝÝÝÝÝÜÜÜÜÜÜÜ\027\0277¯¿¿¿¿¿¿¿¾¾¾¾½ÝÝÝÝÝÝÝÜÜÜÜÜÜÜ"+
                    "¯¯¿¿¿¿¾¾¾¾¾½½½½ÝÝÝÝÜÜÜÜÜÜÜÛ¯¯§§¾¾¾¾¾¾½½½½½ÝÝÝÜÜÜÜÜÜÜÛ¯¯§§¾¾¾¾¾½½½½½½¼¼¼¼ÜÜÜÜÜÜÛ§§®¾¾¾¾½½½½½½¼¼¼¼¼¼ÜÜÜÜÜÛ"+
                    "®¦¦¦¦­½½½½½½¼¼¼¼¼¼¼¼¼ÜÜÛÛ¦¦¦¦­­­½½½½¼¼¼¼¼¼¼¼¼»»»»Û¦¦¦¥¥­­½½½¼¼¼¼¼¼¼¼¼»»»»»»¦¥¥¥¥¥¥­½¼¼¼¼¼¼¼¼¼»»»»»»»"+
                    "¥¥¥¥¥¥¤¤««¼¼¼¼¼¼¼»»»»»»»»¥¥¥¥¤¤¤¤«««¼¼¼¼»»»»»»»»»»¥¥¥¤¤¤¤¤««««¼¼»»»»»»»»»»»¤¤¤¤¤¤¤«««««»»»»»»»»»»»»"+
                    "¤¤¤¤¤££££££««»»»»»»»»»»»»££££££££££££»»»»»»»»»»»º£££££££££££¢¢¢»»»»»»»»»ºº££££££££¢¢¢¢¢¢¢»»»»»»©©ºº"+
                    "££££££¢¢¢¢¢¢¢¢¢¢¡¡©©©©©©©©££££¢¢¢¢¢¢¢¢¢¢¢¡¡¡¡©©©©©©©£¢¢¢¢¢¢¢¢¢¢¢¢¡¡¡¡¡¡¡©©©©©©¢¢¢¢¢¢¢¢¢¢¢¢¡¡¡¡¡¡¡¡¡©©©© "+
                    "¢¢¢¢¢¢¢¢¢¢¢¡¡¡¡¡¡¡¡¡¡      ¢¢¢¢¢¢¢¢¢¢¡¡¡¡¡¡¡¡¡¡       ¢¢¢¢¢¢¢¢¢¢¡¡¡¡¡¡¡¡¡¡        ¢¢¢¢¢¢¢¢¡¡¡¡¡¡¡¡¡¡¡         "+
                    "¢¢¢¢¢¢¢¢¡¡¡¡¡¡¡¡¡¡¡          ¢¢¢¢¢¢¡¡¡¡¡¡¡¡¡¡¡¡           ¢¢¢¢¢¡¡¡¡¡¡¡¡¡¡¡¡            ¢¢¢¡¡¡¡¡¡¡¡¡¡¡¡¡             "+
                    "\027\027\027\027\0277¿¿¿¿¿¿¿¿¿ÞÝÝÝÝÝÝÝÝÝÝÜÜÜÜÜÜ\027\027\027\02777¿¿¿¿¿¿¿¿¿¾ÝÝÝÝÝÝÝÝÝÜÜÜÜÜÜÜ\027\027777·¿¿¿¿¿¿¿¿¾¾ÝÝÝÝÝÝÝÝÝÜÜÜÜÜÜÜ\0277777·¿¿¿¿¿¿¿¾¾¾¾ÝÝÝÝÝÝÝÝÜÜÜÜÜÜÜ"+
                    "\177¯¯¿¿¿¿¾¾¾¾¾½½ÝÝÝÝÝÝÜÜÜÜÜÜÜÛ\177¯¯¯§§¾¾¾¾¾¾½½½½ÝÝÝÝÜÜÜÜÜÜÜÛ\177¯§§§¾¾¾¾¾½½½½½½¼¼¼ÜÜÜÜÜÜÜÛ\177§®®¾¾¾¾½½½½½½¼¼¼¼¼ÜÜÜÜÜÛÛ"+
                    "®¦¦¦¦­½½½½½½¼¼¼¼¼¼¼¼ÜÜÜÛÛ¦¦¦¦­­½½½½½¼¼¼¼¼¼¼¼¼»»»»Û¦¦¦¥­­­½½½¼¼¼¼¼¼¼¼¼»»»»»»¥¥¥¥¥­­½¼¼¼¼¼¼¼¼¼»»»»»»»"+
                    "¥¥¥¥¥¥¤¤««¼¼¼¼¼¼»»»»»»»»»¥¥¥¤¤¤¤«««¼¼¼¼»»»»»»»»»»¥¤¤¤¤¤¤««««¼¼»»»»»»»»»»»¤¤¤¤¤¤¤«««««»»»»»»»»»»»»"+
                    "¤¤¤¤£££££«««»»»»»»»»»»»º££££££££££££»»»»»»»»»»ºº££££££££££¢¢»»»»»»»»»ººº££££££££¢¢¢¢¢¢¢»»»»»©©ººº"+
                    "£££££¢¢¢¢¢¢¢¢¢¢¡©©©©©©©©©£££¢¢¢¢¢¢¢¢¢¢¢¡¡¡©©©©©©©©¢¢¢¢¢¢¢¢¢¢¢¢¡¡¡¡¡¡©©©©©©©¢¢¢¢¢¢¢¢¢¢¢¢¡¡¡¡¡¡¡¡©©©©©©"+
                    "¢¢¢¢¢¢¢¢¢¢¡¡¡¡¡¡¡¡¡¡      ¢¢¢¢¢¢¢¢¢¢¡¡¡¡¡¡¡¡¡¡       ¢¢¢¢¢¢¢¢¡¡¡¡¡¡¡¡¡¡¡        ¢¢¢¢¢¢¢¡¡¡¡¡¡¡¡¡¡¡         "+
                    "¢¢¢¢¢¢¡¡¡¡¡¡¡¡¡¡¡¡          ¢¢¢¢¢¡¡¡¡¡¡¡¡¡¡¡¡           ¢¢¢¢¡¡¡¡¡¡¡¡¡¡¡¡            ¢¢¡¡¡¡¡¡¡¡¡¡¡¡¡             "+
                    "\027\027\027\0277çç¿¿¿¿¿¿¿ÞÞÝÝÝÝÝÝÝÝÝÜÜÜÜÜÜÜ\027\027\027777·¿¿¿¿¿¿¿ÞÞÝÝÝÝÝÝÝÝÝÜÜÜÜÜÜÜ\027\027777···¿¿¿¿¿ÏÞÞÝÝÝÝÝÝÝÝÝÜÜÜÜÜÜÜ77777···¿¿¿¿¾¾¾¾ÝÝÝÝÝÝÝÝÜÜÜÜÜÜÜÛ"+
                    "\177\1777···¿¿¿¾¾¾¾¾½ÝÝÝÝÝÝÝÜÜÜÜÜÜÜÛ\177\177¯¯¯§§¾¾¾¾¾½½½½ÝÝÝÝÝÜÜÜÜÜÜÜÛ\177\177¯§§®¾¾¾¾¾½½½½½ÝÝÝÜÜÜÜÜÜÜÛÛ\177\177§®®¾¾¾¾½½½½½½¼¼¼¼ÜÜÜÜÜÜÛÛ"+
                    "\177®®¦¦¦­½½½½½½¼¼¼¼¼¼¼ÜÜÜÜÛÛ~¦¦¦¦­­½½½½½¼¼¼¼¼¼¼¼¼»»»ÛÛ~¦¦­­­­½½½¼¼¼¼¼¼¼¼¼»»»»»Û~¥¥¥¥­­¬¬¼¼¼¼¼¼¼¼»»»»»»»»"+
                    "~¥¥¥¥¥¬¬¬¼¼¼¼¼¼¼»»»»»»»»»¥¥¥¤¤¤««««¼¼¼¼»»»»»»»»»»¤¤¤¤¤«««««¼¼»»»»»»»»»»»¤¤¤¤¤¤««««««»»»»»»»»»»»º"+
                    "¤¤¤¤£££££««»»»»»»»»»»»ºº£££££££££££ª»»»»»»»»»ººº££££££££££¢¢»»»»»»»»ºººº£££££££¢¢¢¢¢¢ª»»»»©©©ººº"+
                    "£££££¢¢¢¢¢¢¢¢¢¢©©©©©©©©©º££¢¢¢¢¢¢¢¢¢¢¢¡¡©©©©©©©©©¢¢¢¢¢¢¢¢¢¢¢¡¡¡¡¡©©©©©©©©¢¢¢¢¢¢¢¢¢¢¢¡¡¡¡¡¡¡©©©©©©©"+
                    "¢¢¢¢¢¢¢¢¢¡¡¡¡¡¡¡¡¡¡©     ¢¢¢¢¢¢¢¢¢¡¡¡¡¡¡¡¡¡¡       ¢¢¢¢¢¢¢¡¡¡¡¡¡¡¡¡¡¡        ¢¢¢¢¢¢¢¡¡¡¡¡¡¡¡¡¡¡         "+
                    "¢¢¢¢¢¡¡¡¡¡¡¡¡¡¡¡¡          ¢¢¢¢¡¡¡¡¡¡¡¡¡¡¡¡           ¢¢¡¡¡¡¡¡¡¡¡¡¡¡¡            ¢¡¡¡¡¡¡¡¡¡¡¡¡¡             "+
                    "\027\027\0277çççç·¿¿¿ÏÞÞÞÝÝÝÝÝÝÝÝÝÜÜÜÜÜÜÜ\027\027777çç··¿¿ÏÏÞÞÞÝÝÝÝÝÝÝÝÝÜÜÜÜÜÜÜ\0277777····¿ÏÏÏÞÞÞÝÝÝÝÝÝÝÝÜÜÜÜÜÜÜÛ77777····ÏÏÏÏÎÎÞÝÝÝÝÝÝÝÝÜÜÜÜÜÜÜÛ"+
                    "\177\17777\026···'ÏÇÇÎÎÎÎÝÝÝÝÝÝÝÝÜÜÜÜÜÜÜÛ\177\177\177\026\026\026··Ç¾¾ÎÎÍÍ½ÝÝÝÝÝÜÜÜÜÜÜÜÛÛ\177\177\177®¾¾¾¾Í½½½½ÝÝÝÝÜÜÜÜÜÜÜÛÛ\177\177\177®®®¾¾½½½½½½½¼¼¼ÜÜÜÜÜÜÜÛÛ"+
                    "\177®®¦¾µ½½½½½½¼¼¼¼¼¼ÜÜÜÜÛÛÛ~~¦¦¦­­½½½½½¼¼¼¼¼¼¼¼»»ÜÛÛÛ~~¦¦­­­­½½½¼¼¼¼¼¼¼¼»»»»»ÛÛ~~¥­­­¬¬¬¼¼¼¼¼¼¼¼»»»»»»»»"+
                    "~¥¥¥¬¬¬¬¼¼¼¼¼¼¼»»»»»»»»»¥¤¬¬¬««¼¼¼¼¼»»»»»»»»»»¤¤¤¤««««¼¼»»»»»»»»»»»º¤¤¤¤¤«««««»»»»»»»»»»»ºº"+
                    "¤¤¤££££«««»»»»»»»»»»ººº£££££££££ªª»»»»»»»»ºººº££££££££££ªªª»»»»»»ººººº£££££££¢¢¢¢ªªª»»»©©©ºººº"+
                    "££££¢¢¢¢¢¢¢¢¢ª©©©©©©©©ºº££¢¢¢¢¢¢¢¢¢¢¢¡©©©©©©©©©©¢¢¢¢¢¢¢¢¢¢¢¡¡¡¡©©©©©©©©©¢¢¢¢¢¢¢¢¢¢¡¡¡¡¡¡©©©©©©©©"+
                    "¢¢¢¢¢¢¢¢¢¡¡¡¡¡¡¡¡¡©©©©©©©¢¢¢¢¢¢¢¢¡¡¡¡¡¡¡¡¡¡       ¢¢¢¢¢¢¢¡¡¡¡¡¡¡¡¡¡¡        ¢¢¢¢¢¢¡¡¡¡¡¡¡¡¡¡¡         "+
                    "¢¢¢¢¡¡¡¡¡¡¡¡¡¡¡¡          ¢¢¢¡¡¡¡¡¡¡¡¡¡¡¡           ¢¡¡¡¡¡¡¡¡¡¡¡¡¡            ¡¡¡¡¡¡¡¡¡¡¡¡             "+
                    "\027\027\0277çççççÏÏÏÞÞÞÞÝÝÝÝÝÝÝÝÝÜÜÜÜÜÜÜ\027777çççç·ÏÏÏÞÞÞÞÝÝÝÝÝÝÝÝÜÜÜÜÜÜÜÛ7777ççç··ÏÏÏÞÞÞÞÝÝÝÝÝÝÝÝÜÜÜÜÜÜÜÛ77777···'ÏÏÏÏÞÞÞÝÝÝÝÝÝÝÝÜÜÜÜÜÜÜÛ"+
                    "\177\177\177\026\026\026··'ÏÏÇÎÎÎÍÝÝÝÝÝÝÝÜÜÜÜÜÜÜÛÛ\177\177\177\026\026\026\026'ÇÇÎÎÎÍÍÍÝÝÝÝÝÝÜÜÜÜÜÜÜÛÛ\177\177\177\026\026¶¶ÎÎÎÍÍÍÍÝÝÝÝÝÜÜÜÜÜÜÜÛÛ\177\177\177\177¶¶¶ÎÎÍÍÍ½½½ÌÝÝÜÜÜÜÜÜÜÜÛÛ"+
                    "\177~µµµ½½½½½¼¼¼¼¼ÜÜÜÜÜÛÛÛ~~~µµµµ½½½½¼¼¼¼¼¼¼ÜÜÜÜÛÛÛ~~~µµµ¬½½¼¼¼¼¼¼¼¼»»»»ÛÛÛ~~~­¬¬¬¬¬¼¼¼¼¼¼¼»»»»»»»Û"+
                    "~¬¬¬¬¬¬¼¼¼¼¼¼»»»»»»»»»¬¬¬¬¬¬¼¼¼¼»»»»»»»»»»º¤¬¬¬«««¼¼»»»»»»»»»»ºº¤¤¤«««««»»»»»»»»»»ººº"+
                    "¤£££«««ªª»»»»»»»»ºººº££££££ªªªª»»»»»»ººººº££££££ªªªªª»»»»ºººººº££££¢¢¢ªªªªª©©©©ººººº"+
                    "£¢¢¢¢¢¢¢ªªª©©©©©©©©ºº¢¢¢¢¢¢¢¢¢¢ª©©©©©©©©©©©¢¢¢¢¢¢¢¢¢¢¡¡¡©©©©©©©©©©¢¢¢¢¢¢¢¢¢¡¡¡¡¡©©©©©©©©©"+
                    "¢¢¢¢¢¢¢¢¡¡¡¡¡¡¡¡©©©©©©©©¢¢¢¢¢¢¢¡¡¡¡¡¡¡¡¡¡©      ¢¢¢¢¢¢¡¡¡¡¡¡¡¡¡¡¡        ¢¢¢¢¢¡¡¡¡¡¡¡¡¡¡¡         "+
                    "¢¢¢¡¡¡¡¡¡¡¡¡¡¡¡          ¡¡¡¡¡¡¡¡¡¡¡¡           ¡¡¡¡¡¡¡¡¡¡¡¡            ¡¡¡¡¡¡¡¡¡¡¡             "+
                    "\027\0277çççççççÏÏÞÞÞÞÝÝÝÝÝÝÝÝÝÜÜÜÜÜÜÛ777çççççßÏÏÏÞÞÞÞÝÝÝÝÝÝÝÝÜÜÜÜÜÜÜÛ7777çççßßÏÏÏÞÞÞÞÝÝÝÝÝÝÝÝÜÜÜÜÜÜÜÛ\177777ççßß'ÏÏÏÞÞÞÞÝÝÝÝÝÝÝÝÜÜÜÜÜÜÛÛ"+
                    "\177\177\17766\026Wß'ÏÏÇÞÞÞÞÝÝÝÝÝÝÝÜÜÜÜÜÜÜÛÛ\177\177\1776\026\026W'ÇÇÎÎÎÍÍÍÝÝÝÝÝÝÜÜÜÜÜÜÜÛÛ\177\177\177\177\026¶¶ÎÎÍÍÍÍÝÝÝÝÝÜÜÜÜÜÜÜÜÛÛ\177\177\177\177¶¶ÎÎÍÍÍÍÍÌÌÝÝÜÜÜÜÜÜÜÛÛÛ"+
                    "~~~µµµÍÍÌÌÌÌ¼¼¼ÜÜÜÜÜÜÛÛÛ~~~~µµµµ½ÌÌÌ¼¼¼¼¼ÜÜÜÜÜÛÛÛ~~~~µµµµ¬Ì¼¼¼¼¼¼¼¼»»»»ÛÛÛ~~~~¬¬¬¬¬¼¼¼¼¼¼¼»»»»»»ÛÛ"+
                    "~~¬¬¬¬¬¬¼¼¼¼¼»»»»»»»»»»¬¬¬¬¬´´¼¼¼»»»»»»»»»»º¬¬¬¬´´¼¼»»»»»»»»»»ºº¬«««´ª»»»»»»»»»»ººº"+
                    "££««ªªª»»»»»»»»ºººº£££ªªªªª»»»»»»ººººº£££ªªªªªª»»»ººººººº£¢ªªªªªªª©©©©ººººº"+
                    "¢¢¢¢¢ªªªª©©©©©©©ººº¢¢¢¢¢¢¢ªª©©©©©©©©©©º¢¢¢¢¢¢¢¢¡¡©©©©©©©©©©©¢¢¢¢¢¢¢¢¡¡¡¡©©©©©©©©©¹"+
                    "¢¢¢¢¢¢¢¡¡¡¡¡¡¡©©©©©©©©¹¢¢¢¢¢¢¡¡¡¡¡¡¡¡¡¡©©©©©©±±¢¢¢¢¢¡¡¡¡¡¡¡¡¡¡¡       ±¡¡¡¡¡¡¡¡¡¡¡        ±"+
                    "¡¡¡¡¡¡¡¡¡¡¡          ¡¡¡¡¡¡¡¡¡¡           ¡¡¡¡¡¡¡¡¡¡            ¡¡¡¡¡¡¡¡¡             "+
                    "\0277çççççççïÏÞÞÞÞÞÝÝÝÝÝÝÝÝÜÜÜÜÜÜÜÛ77ççççççßßÏÞÞÞÞÞÝÝÝÝÝÝÝÝÜÜÜÜÜÜÜÛ777ççççßßÏÏÞÞÞÞÞÝÝÝÝÝÝÝÝÜÜÜÜÜÜÜÛ\17777çççßßßÏÏÞÞÞÞÞÝÝÝÝÝÝÝÜÜÜÜÜÜÜÛÛ"+
                    "\177\177\17766WWß'ÏÏÞÞÞÞÞÝÝÝÝÝÝÝÜÜÜÜÜÜÜÛÛ\177\177\177666Wß'ÇÇ×ÎÎÍÍÝÝÝÝÝÝÜÜÜÜÜÜÜÜÛÛ\177\177\1776665¶¶××ÎÍÍÍÍÝÝÝÝÝÜÜÜÜÜÜÜÛÛÛ\177\177\177\177×ÍÍÍÍÍÌÌÝÝÝÜÜÜÜÜÜÜÛÛÛ"+
                    "~~~~µµÍÍÍÌÌÌÌÌÌÜÜÜÜÜÜÜÛÛÛ~~~~~µµµµÌÌÌÌÌ¼¼¼ËÜÜÜÜÛÛÛÛ~~~~~µµµµÌÌÌÌ¼¼¼¼ËËËÊÜÛÛÛÛ~~~~¬¬¬¬´´¼¼¼¼¼Ë»»»»»ÛÛÛ"+
                    "}~¬¬¬´´´¼¼¼¼»»»»»»»»»Û}¬¬¬´´´´¼¼»»»»»»»»»ºº¬´´´´´´»»»»»»»»»ººº´´´´ªª»»»»»»»»ºººº"+
                    "´ªªªªª»»»»»»ºººººªªªªªª»»»»ººººººªªªªªªª»»ºººººººªªªªªªªª©©©ºººººº"+
                    "¢ªªªªªª©©©©©©ºººº¢¢¢ªªªª©©©©©©©©©º¹¢¢¢¢¢¢ª©©©©©©©©©©©¹¢¢¢¢¢¡¡¡¡©©©©©©©©©¹¹"+
                    "¡¡¡¡¡¡©©©©©©©©¹¹¡¡¡¡¡¡¡¡©©©©©©©±±¡¡¡¡¡¡¡¡¡     ±±±¡¡¡¡¡¡¡¡¡       ±±"+
                    "¡¡¡¡¡¡¡¡        ±±¡¡¡¡¡¡¡¡          ±¡¡¡¡¡¡¡            ¡¡¡¡¡¡¡             "+
                    "ÿÿÿÿçççïïïÞÞÞÞÞÞÝÝÝÝÝÝÝÝÜÜÜÜÜÜÜÛÿÿÿçççïïïßÞÞÞÞÞÞÝÝÝÝÝÝÝÝÜÜÜÜÜÜÜÛ77çççïïïßÏÞÞÞÞÞÞÝÝÝÝÝÝÝÜÜÜÜÜÜÜÛÛ\177\177ççïïïßßÏÞÞÞÞÞÞÝÝÝÝÝÝÝÜÜÜÜÜÜÜÛÛ"+
                    "\177\177\1776///ßæÏÞÞÞÞÞÞÝÝÝÝÝÝÜÜÜÜÜÜÜÜÛÛ\177\177\17766//ææÏ××ÞÞÞÍÝÝÝÝÝÝÜÜÜÜÜÜÜÛÛÛ\177\177\1776gg55V&&××ÍÍÍÍÝÝÝÝÝÜÜÜÜÜÜÜÛÛÛ\177\177\177gg555&&ÆÍÍÍÍÌÌÝÝÜÜÜÜÜÜÜÜÛÛÛ"+
                    "~~~~55ÆÆÆÍÌÌÌÌÌÌÜÜÜÜÜÜÜÛÛÛÛ~~~~~ÆÆµµÌÌÌÌÌÌËËËÜÜÜÜÛÛÛÛ~~~~~µµÌÌÌÌÌÌËËËËËÊÊÊÛÛÛÛ}~~~¬¬´´´ÌËËËËËËÊÊÊÊÛÛÛ"+
                    "}}}¬´´´´´´ËËË»»ÊÊÊÊÊÊÛ}}´´´´´´Ë»»»»»»»Êººº´´´´´´»»»»»»»»ºººº´´´´ªª»»»»»»»ººººº"+
                    "ªªªªª³»»»»ººººººªªªªªª³»»ºººººººªªªªªªª³ººººººººªªªªªªª©©ººººººº"+
                    "ªªªªªª©©©©©©ºººº¢ªªªªª©©©©©©©©©¹¹ªª©©©©©©©©©©¹¹¡©©©©©©©©©©¹¹"+
                    "¡¡¡©©©©©©©©¹¹±¡¡¡¡©©©©©©©±±±¡¡¡¡¡¡©©©©©±±±±¡¡¡¡¡¡     ±±±±"+
                    "¡¡¡¡¡¡       ±±±¡¡¡¡         ±±±¡¡¡¡¡          ±±¡¡¡¡             ±"+
                    "ÿÿÿÿ??????ÞÞÞÞÞÝÝÝÝÝÝÝÝÝÜÜÜÜÜÜÜÛÿÿÿÿÿïïïïïÞÞÞÞÞÝÝÝÝÝÝÝÝÜÜÜÜÜÜÜÛÛÿÿÿÿïïïïïæÞÞÞÞÞÝÝÝÝÝÝÝÝÜÜÜÜÜÜÜÛÛoo÷÷÷÷ïïææÞÞÞÞÞÝÝÝÝÝÝÝÝÜÜÜÜÜÜÜÛÛ"+
                    "ooo\037\037÷÷æææÞÞÞÞÞÝÝÝÝÝÝÝÜÜÜÜÜÜÜÛÛÛooo\037\037ggæææ&ÞÞÞÞÝÝÝÝÝÝÝÜÜÜÜÜÜÜÛÛÛoooggg55V&&&×ÍÍÍÝÝÝÝÝÜÜÜÜÜÜÜÜÛÛÛ\177oogg555V&&&ÆÆÍÍÌÌÌÝÝÜÜÜÜÜÜÜÛÛÛÛ"+
                    "~~~~55544\025\025ÆÆÆÆÌÌÌÌÌÌÜÜÜÜÜÜÜÛÛÛÛ~~~~~44\025\025ÆÆÆÖÌÌÌÌÌËËËÜÜÜÜÜÛÛÛÛ}~~~~%%ÖÌÌÌÌËËËËËËÊÊÊÛÛÛÛ}}}~~Ö´ÌÌËËËËËËÊÊÊÊÊÛÛÛ"+
                    "}}}}´´´´´ËËËËÊÊÊÊÊÊÊÛÛ}}}´´´´´ËËËÊÊÊÊÊÊÊÊºº}}´´´´´´Ë»ÊÊÊÊÊÊºººº´´´´´³³³³»»Êºººººº"+
                    "ªªªª³³³³»ºººººººªªªªª³³³ººººººººªªªªª³³³ººººººººªªªªªª³©ºººººººº"+
                    "ªªªªªª©©©©©ºººº¹ªªªª©©©©©©©©¹¹¹ª©©©©©©©©¹¹¹¹©©©©©©©©¹¹¹¹"+
                    "©©©©©©©©¹¹¹±©©©©©©©±±±±¡©©©©©©±±±±±¡¡©©  ±±±±±±"+
                    "¡¡      ±±±±±¡¡        ±±±±¡         ±±±±          ±±±"+
                    "ÿÿÿÿ???????ÞÞÞÞÝÝÝÝÝÝÝÝÜÜÜÜÜÜÜÛÛÿÿÿÿÿ?????ÞÞÞÞÞÝÝÝÝÝÝÝÝÜÜÜÜÜÜÜÛÛÿÿÿÿïïïïææÞÞÞÞÞÝÝÝÝÝÝÝÝÜÜÜÜÜÜÜÛÛooo÷÷÷÷æææÞÞÞÞÞÝÝÝÝÝÝÝÜÜÜÜÜÜÜÛÛÛ"+
                    "ooo\037\037\037\037æææÞÞÞÞÞÝÝÝÝÝÝÝÜÜÜÜÜÜÜÛÛÛoooo\037ggVææ&ÞÞÞÞÝÝÝÝÝÝÜÜÜÜÜÜÜÜÛÛÛoooogggVVV&&&GÍÍÝÝÝÝÝÜÜÜÜÜÜÜÛÛÛÛoooggg5..ö&&GGGÖÌÌÌÝÝÜÜÜÜÜÜÜÛÛÛÛ"+
                    "nnngg4444\025\025GGGGÖÌÌÌÌÌÜÜÜÜÜÜÜÛÛÛÛ}~~~4444\025\025\025G%%ÖÖÌÌÌÌËËËÜÜÜÜÜÛÛÛÛ}}}~~\025%%%ÖÖÌÌÌËËËËËËÊÊÊÛÛÛÛ}}}}}ÖÖÖÌËËËËËËÊÊÊÊÊÛÛÛ"+
                    "}}}}}´´´´ËËËËËÊÊÊÊÊÊÊÚÚ}}}}´´´´´ËËËÊÊÊÊÊÊÊÊºÚ}}}´´´´´ËËÊÊÊÊÊÊÊºººº}´´´³³³³ÊÊÊÊººººº"+
                    "ªª³³³³³³ººººººº|ªªª³³³³³ºººººººº|ªªªª³³³³ºººººººº|ªªªªª³³³ºººººººº"+
                    "ªªªªª³©©©©ºººº¹¹ªªª©©©©©©²¹¹¹¹©©©©©©©²¹¹¹¹©©©©©©²²¹¹¹¹"+
                    "©©©©©²²¹¹¹±©©©©©²²±±±±©©©©©²±±±±±©©©±±±±±±±"+
                    "   ±±±±±±±     ±±±±±±     ±±±±±±      ±±±±±"+
                    "ÿÿÿÿ???????ÞÞÞÞÝÝÝÝÝÝÝÝÜÜÜÜÜÜÜÛÛÿÿÿÿÿ?????ÞÞÞÞÞÝÝÝÝÝÝÝÝÜÜÜÜÜÜÜÛÛwwÿÿÿ??ïææÞÞÞÞÞÝÝÝÝÝÝÝÜÜÜÜÜÜÜÛÛÛwwoÿ÷÷÷æææÞÞÞÞÞÝÝÝÝÝÝÝÜÜÜÜÜÜÜÛÛÛ"+
                    "oooo\037\037OæææÞÞÞÞÞÝÝÝÝÝÝÜÜÜÜÜÜÜÜÛÛÛoooo\037ggVææååÞÞÞÝÝÝÝÝÝÜÜÜÜÜÜÜÛÛÛÛoooogg\036..öåååGGGÝÝÝÝÝÜÜÜÜÜÜÜÛÛÛÛoooogg...ööGGGGGÖÌÝÝÜÜÜÜÜÜÜÜÛÛÛÛ"+
                    "nnnng4444\025öGGGGÖÖÌÌÌËÜÜÜÜÜÜÜÛÛÛÛ}nnn4444\025\025\025%%%ÖÖÖÖÌËËËËÜÜÜÜÛÛÛÛÛ}}}}}fff\025\025\025%%%ÖÖÖÖËËËËËËËÊÊÛÛÛÛÛ}}}}}_%ÅÅÅÅËËËËËËÊÊÊÊÊÛÛÚ"+
                    "}}}}}ÅÅ´´ËËËËËÊÊÊÊÊÊÊÚÚ}}}}|´´´´ËËËËÊÊÊÊÊÊÊÊÚÚ}}|||´´´ËËÊÊÊÊÊÊÊÊººº|||||´³³³³ÊÊÊÊÊººººº"+
                    "|||||³³³³³³Êººººººº|||ª³³³³³³ºººººººº||ªª³³³³³ºººººººº|ªªª³³³³ºººººººº"+
                    "ªªª³³©©²²²º¹¹¹¹ª©©©²²²²¹¹¹¹©©©²²²²¹¹¹¹©©©²²²²¹¹¹¹"+
                    "©©©²²²²¹¹¹±©©²²²²±±±±©©²²±±±±±±©©²±±±±±±±"+
                    "±±±±±±±± ±±±±±±±±  ±±±±±±±  ±±±±±±±"+
                    "ÿÿÿÿ???????ÞÞÞÞÝÝÝÝÝÝÝÝÜÜÜÜÜÜÜÛÛwÿÿÿÿ?????ÞÞÞÞÞÝÝÝÝÝÝÝÜÜÜÜÜÜÜÛÛÛwwwÿÿ??>>>îÞÞÞÞÝÝÝÝÝÝÝÜÜÜÜÜÜÜÛÛÛwwwwOOO>>îîÞÞÞÞÝÝÝÝÝÝÝÜÜÜÜÜÜÜÛÛÛ"+
                    "ooooOOO>îîååÞÞÞÝÝÝÝÝÝÜÜÜÜÜÜÜÛÛÛÛooooOOO\036îåååååÞÝÝÝÝÝÝÜÜÜÜÜÜÜÛÛÛÛoooo\036\036\036\036.åååååGGÝÝÝÝÜÜÜÜÜÜÜÜÛÛÛÛnnnnn\036\036..ööåGGGGÖÖÝÝÜÜÜÜÜÜÜÜÛÛÛÛ"+
                    "nnnnnffffUUGGGGÖÖÖÖÌËÜÜÜÜÜÜÛÛÛÛÛnnnnnffffUU%%%%ÖÖÖÖËËËËÜÜÜÜÛÛÛÛÛ}}}}fffffU__%%ÅÅÅÅÕËËËËËÊÊÊÛÛÛÛÛ}}}}}___\024\024ÅÅÅÅÕËËËËËÊÊÊÊÚÚÚÚ"+
                    "}}}}}__\024\024\024ÅÅÅÅËËËËÊÊÊÊÊÊÊÚÚÚ}}}||\024\024\024\024ÅÅÕËËËÊÊÊÊÊÊÊÊÊÚÚ|||||´ËËËÊÊÊÊÊÊÊÉÉÉº|||||³³³ÊÊÊÊÊÉÉÉººº"+
                    "|||||³³³³³ÊÉÉÉººººº||||³³³³³³Éººººººº||³³³³³³ºººººººº|³³³³³ººººººº¹"+
                    "³³³³²²²²²¹¹¹¹©²²²²²²¹¹¹¹©²²²²²²¹¹¹¹©²²²²²²¹¹¹¹"+
                    "²²²²²¹¹¹±±²²²²²¹±±±±²²²²±±±±±±²²±±±±±±±"+
                    "±±±±±±±±±±±±±±±±±±±±±±±±±±±±±±±±"+
                    "wÿÿÿ???????ÞÞÞÞÝÝÝÝÝÝÝÜÜÜÜÜÜÜÛÛÛwwwÿÿ???>>îîÞÞÞÝÝÝÝÝÝÝÜÜÜÜÜÜÜÛÛÛwwwwÿþ>>>>îîÞÞÞÝÝÝÝÝÝÝÜÜÜÜÜÜÜÛÛÛwwwwþO>>>îîîåÞÞÝÝÝÝÝÝÜÜÜÜÜÜÜÛÛÛÛ"+
                    "wwooOOO>îîîååååÝÝÝÝÝÝÜÜÜÜÜÜÜÛÛÛÛooooOOOîîîåååååÝÝÝÝÝÜÜÜÜÜÜÜÜÛÛÛÛonnn\036\036\036\036\036ååååååGÝÝÝÝÜÜÜÜÜÜÜÜÛÛÛÛnnnnn\036\036\036ööåååGGGÖÖÖÝÜÜÜÜÜÜÜÛÛÛÛÛ"+
                    "nnnnnffffUUUG%%ÖÖÖÖÕÕÜÜÜÜÜÜÛÛÛÛÛnnnnnffffUUõ%%%ÅÅÅÕÕÕÕËÜÜÜÜÛÛÛÛÛ}nnmmffffTTT%%ÅÅÅÅÕÕÕÕËËÊÊÊÛÚÚÚÚ}}}}m____\024\024ÅÅÅÅÕÕÕÕËÊÊÊÊÊÚÚÚÚ"+
                    "}}}||__\024\024\024ÅÅÅÕÕÕÕËÊÊÊÊÊÊÊÚÚÚ}||||_\024\024\024$\023ÅÕÕÕÕÊÊÊÊÊÊÊÉÉÚÚ|||||\024\023ÄÄÄÊÊÊÊÊÊÊÉÉÉÉÙ||||||ÄÄÊÊÊÊÊÉÉÉÉÉÉº"+
                    "|||||³³³³ÉÉÉÉÉÉÉººº||||³³³³ÉÉÉÉººººº|||³³³³ÉÉººººººº|³³³Èººººº¹¹¹"+
                    "³³²²²²²¹¹¹¹¹²²²²²²¹¹¹¹¹²²²²²²¹¹¹¹¹²²²²²¹¹¹¹¹"+
                    "²²²²²¹¹¹±±²²²²²¹±±±±²²²±±±±±±±±±±±±±±"+
                    "±±±±±±±±±±±±±±±±±±±±±±±°°°°°°±"+
                    "wwwÿ??????>îÞÞÞÝÝÝÝÝÝÝÜÜÜÜÜÜÜÛÛÛwwwwþþþþ>>îîîÞÞÝÝÝÝÝÝÝÜÜÜÜÜÜÜÛÛÛwwwwþþþ>>îîîîÞÞÝÝÝÝÝÝÜÜÜÜÜÜÜÛÛÛÛwwwwþþþ>>îîîåååÝÝÝÝÝÝÜÜÜÜÜÜÜÛÛÛÛ"+
                    "wwwþþþ>îîîîååååÝÝÝÝÝÜÜÜÜÜÜÜÜÛÛÛÛooooOOOîîîååååååÝÝÝÝÜÜÜÜÜÜÜÜÛÛÛÛnnnnn\036\036\036îåååååååÝÝÝÝÜÜÜÜÜÜÜÛÛÛÛÛnnnnn\036\036\036\036åååååGGÖÖÖÜÜÜÜÜÜÜÜÛÛÛÛÛ"+
                    "nnnnnfff----õ%%ÖÖÖÕÕÕÜÜÜÜÜÜÛÛÛÛÛnnnnmmff--õõõ%ÅÅÅÅÕÕÕÕÕÜÜÜÛÛÛÛÛÛnnmmmmff-TTõõõFFÅÅÕÕÕÕÕÕÊÊÚÚÚÚÚÚ}}mmmmme333T\024$FFFÕÕÕÕÕÕÊÊÊÊÚÚÚÚÚ"+
                    "}||||e33\024\024$$FFÕÕÕÕÕÊÊÊÊÊÊÚÚÚÚ|||||e_\024\024$$\023\023ÄÄÕÕÔÊÊÊÊÊÉÉÙÙÙ||||||SS\023\023\023\023\023ÄÄÄÔÔÊÊÊÉÉÉÉÉÙÙ||||||\023\023ÄÄÄÔÔÊÉÉÉÉÉÉÉÉÙ"+
                    "|||||ÄÄÔÔÉÉÉÉÉÉÉÉÉº||||³ÃÉÉÉÉÉÉÉººº|||{³ÈÈÈÈÉÉºººº¹||ÈÈÈÈÈ²º¹¹¹¹"+
                    "ÈÈ²²²²¹¹¹¹¹²²²²²²¹¹¹¹¹²²²²²¹¹¹¹¹²²²²²¹¹¹¹¹"+
                    "²²²²²¹¹¸¸¸²²²²¸±±±±²±±±±±±±±±±±±±±"+
                    "±±±±±±±±±±±±±±°±±±±±±°°°°°°°"+
                    "wwwwþþþþþ>>îîîÞÝÝÝÝÝÝÝÜÜÜÜÜÜÜÛÛÛwwwwþþþþ>>îîîîÞÝÝÝÝÝÝÜÜÜÜÜÜÜÛÛÛÛwwwwþþþþ>îîîîååÝÝÝÝÝÝÜÜÜÜÜÜÜÛÛÛÛwwwþþþþ>îîîîåååÝÝÝÝÝÜÜÜÜÜÜÜÜÛÛÛÛ"+
                    "wwwþþþþ>îîîåååååÝÝÝÝÜÜÜÜÜÜÜÜÛÛÛÛwwnþþþ>îîîååååååÝÝÝÝÜÜÜÜÜÜÜÛÛÛÛÛnnnnn\036NNNNååååååäÝÝÜÜÜÜÜÜÜÜÛÛÛÛÛnnnnn\036NNN==ååååääääÕÜÜÜÜÜÜÜÛÛÛÛÛ"+
                    "nnnnmmf---\035\035õõFFFFÕÕÕÕÜÜÜÜÛÛÛÛÛÛnnnmmmm----õõõFFFFÕÕÕÕÕÕÜÜÚÚÚÚÚÚnmmmmmm--õõõõFFFFFÕÕÕÕÕÕÔÊÚÚÚÚÚÚmmmmmmmeee333$FFFFÕÕÕÕÕÔÔÊÊÚÚÚÚÚ"+
                    "|||mmmeeeee3$$$FFFÕÕÕÔÔÔÔÊÊÊÚÚÚÚ||||||eeSSS$$\023\023ÄÄÄÔÔÔÔÔÊÉÉÉÙÙÙ||||||SSS^\023\023\023\023ÄÄÄÔÔÔÔÉÉÉÉÉÉÙÙ||||||SS\023\023\023\023\023ÄÄÄÔÔÔÉÉÉÉÉÉÉÉÙ"+
                    "||||{{\023\023ÄÄÄÃÃÉÉÉÉÉÉÉÉÉÓ|||{{{ÃÃÃÈÈÉÉÉÉÉÉÓÓ||{{{{ÃÃÈÈÈÈÈÉÉ¹¹¹|{{{ÃÈÈÈÈÈÈ¹¹¹¹¹"+
                    "ÈÈÈÈ²²¹¹¹¹¹È²²²²²¹¹¹¹¹²²²²²¹¹¹¹¹²²²²²¹¹¹¹¸"+
                    "²²²²¹¸¸¸¸²²¸¸¸¸¸¸¸¸¸¸¸±±¸±±±±±±"+
                    "±±±±±±±±±±±±±±°°°°°°°°°°°°°°"+
                    "wwwwþþþþþþîîîîîÝÝÝÝÝÝÜÜÜÜÜÜÜÛÛÛÛwwwþþþþþþ>îîîîåÝÝÝÝÝÝÜÜÜÜÜÜÜÛÛÛÛwwwþþþþþþîîîîååÝÝÝÝÝÝÜÜÜÜÜÜÜÛÛÛÛwwwþþþþþîîîîååååÝÝÝÝÜÜÜÜÜÜÜÜÛÛÛÛ"+
                    "wwwþþþþþNNNåååííÝÝÝÝÜÜÜÜÜÜÜÛÛÛÛÛwwnvvvvNNNNåííííäÝÝÜÜÜÜÜÜÜÜÛÛÛÛÛnnnnvvNNNN==íííääääÜÜÜÜÜÜÜÜÛÛÛÛÛnnnnnvNN=====íääääääÜÜÜÜÜÜÛÛÛÛÛÛ"+
                    "nnnmmmm==\035\035\035\035ääääääÕÕÕÜÜÜÜÛÛÛÛÚÚnmmmmmm--\035\035õõõäFFFÕÕÕÕÕÕÜÚÚÚÚÚÚÚmmmmmmm---õõõFFFFFÕÕÕÕÕÔÔÔÚÚÚÚÚÚmmmmmmmeeee3$$FFFFÕÕÕÕÔÔÔÔÚÚÚÚÚÚ"+
                    "|mmmmlleeeeSS$$FFÄÄÄÔÔÔÔÔÔÔÙÙÙÙÙ||||llleeeSSS^$\023ÄÄÄÄÔÔÔÔÔÔÉÉÙÙÙÙ|||||llleSSS^^\023\023\023ÄÄÄÔÔÔÔÔÉÉÉÉÙÙÙ|||||{{{SSS^\023\023\023\023ÄÄÄÔÔÔÔÉÉÉÉÉÓÙÙ"+
                    "|||{{{{{dd2\023\023\023\023ÄÄÄÃÃÃÉÉÉÉÉÓÓÓÓ||{{{{{{dÃÃÃÃÈÈÈÉÉÓÓÓÒ|{{{{{{{{ÃÃÃÈÈÈÈÈÈÓÓÒÒ{{{{{ÃÃÈÈÈÈÈÈ¹¹¹¹"+
                    "ÃÈÈÈÈÈÈ¹¹¹¹¹ÈÈ²²¹¹¹¹¹¹²²²¹¹¹¹¹¹²²²¹¹¹¸¸¸"+
                    "²²¹¸¸¸¸¸¸¸¸¸¸¸¸¸¸¸¸¸¸¸¸¸¸¸¸¸¸"+
                    "¸±±±±±°°°°°°°°°°°°°°°°°°°°°"+
                    "wwwþþþþþþþþîîíííÝÝÝÝÝÜÜÜÜÜÜÜÛÛÛÛwwwþþþþþþþîîííííÝÝÝÝÝÜÜÜÜÜÜÜÛÛÛÛwwwþþþþþþîîííííííÝÝÝÜÜÜÜÜÜÜÜÛÛÛÛwwþþþþþþþNíííííííÝÝÝÜÜÜÜÜÜÜÛÛÛÛÛ"+
                    "wwþvvvvvNNíííííííäÝÜÜÜÜÜÜÜÜÛÛÛÛÛwvvvvvvNNNííííííäääÜÜÜÜÜÜÜÜÛÛÛÛÛnnvvvvvNNNíííííäääääÜÜÜÜÜÜÛÛÛÛÛÛnnnvvvNN====ííäääääääÜÜÜÜÜÛÛÛÛÛÛ"+
                    "nnmmmmm=====ääääääääÕÕÕÜÜÚÚÚÚÚÚÚmmmmmmm=\035\035\035\035\034ääääääÕÕÕÕÕÔÚÚÚÚÚÚÚmmmmmmmmee,\034\034ôôäFFFÕÕÕÔÔÔÔÚÚÚÚÚÚmmmmmmleeee,\034ôôFFFFÕÕÔÔÔÔÔÚÚÚÚÚÚ"+
                    "mmmllllleeeSSôôôFÄÄÄÔÔÔÔÔÔÔÙÙÙÙÙ||lllllleeSS^^^2EÄÄÄÔÔÔÔÔÔÉÙÙÙÙÙ|||llllllSSS^^2##ÄÄÄÔÔÔÔÔÉÉÓÓÙÙÙ|||{{{{{{ddd222###ÄÄÔÔÔÔÉÉÉÓÓÓÙÙ"+
                    "||{{{{{{{ddd222###ÄÃÃÃÃÈÈÉÓÓÓÓÓØ|{{{{{{{{{ddR]]]ÃÃÃÃÃÈÈÈÓÓÓÓÒÒ{{{{{{{{{{zRRÃÃÃÈÈÈÈÈÓÒÒÒÒ{{{{{{\022ÃÃÈÈÈÈÈÒÒÒÒÒ"+
                    "\022\022\022ÈÈÈÈÂÂÒÒÒ¹ÈÈÂÂÂÂÂ¹¹¹ÂÂÂÂÂ¹¹¸¸ÂÂÂÂ¸¸¸¸"+
                    "Â¸¸¸¸¸¸¸¸¸¸¸¸¸¸¸¸¸¸¸¸¸¸¸¸"+
                    "¨¨¨¨¸¸°°°°°°°°°°°°°°°°°°°°"+
                    "wwwþþþþþþþþííííííÝÝÝÝÜÜÜÜÜÜÜÛÛÛÛwwwþþþþþþþíííííííÝÝÝÜÜÜÜÜÜÜÜÛÛÛÛwwþþþþþþþþííííííííÝÝÜÜÜÜÜÜÜÛÛÛÛÛwwþþþvvvvíííííííííÝÜÜÜÜÜÜÜÜÛÛÛÛÛ"+
                    "wvvvvvvvNííííííííäääÜÜÜÜÜÜÜÛÛÛÛÛvvvvvvvvNíííííííääääÜÜÜÜÜÜÛÛÛÛÛÛnvvvvvvvNííííííääääääÜÜÜÜÜÛÛÛÛÛÛnnvvvvvN==ííííäääääääÜÜÜÜÜÛÚÚÚÚÚ"+
                    "mmmmmmm=====äääääääääÕÕÜÜÚÚÚÚÚÚÚmmmmmmm====\034\034äääääääÕÕÕÔÔÚÚÚÚÚÚÚmmmmmmmm<<,\034\034\034ääääääÕÔÔÔÔÚÚÚÚÚÚÚmmmmmllle,,,\034ôôôôFEÄÄÔÔÔÔÔÙÙÙÙÙÙ"+
                    "mmllllllle,,ôôôôEEEÄÄÔÔÔÔÔÙÙÙÙÙÙllllllllleSS^^2EEEEÄÄÔÔÔÔÔÓÙÙÙÙÙ|llllllllddd222##EEÄÄÔÔÔÔÓÓÓÓÙÙÙ||{{{{{{dddd222###EÄÃÃÔÔÓÓÓÓÓÓØØ"+
                    "|{{{{{{{{dddd2]]###ÃÃÃÃÃÈÓÓÓÓÓÓØ{{{{{{{{{{ddRR]]]]ÃÃÃÃÃÈÈÓÓÓÓÒÒÒ{{{{{{{{zzzRRR]]]\022\022\022ÃÃÃÈÈÈÓÓÒÒÒÒ{{{{zzzzzzzzRR\022\022\022\022ÃÈÈÈÈÒÒÒÒÒÒ"+
                    "zz\022\022\022ÈÈÈÂÂÂÂÒÒÒ\022\022ÂÂÂÂÂÂÂÂÂÂÂÂÂÂÂÂ¸¸ÂÂÂÂÂ¸¸¸"+
                    "ÂÂÂ¸¸¸¸¸¨¨¨¸¸¸¨¨¨¨¨¨¨¨¨¨¨"+
                    "¨¨¨¨¨°°°°°°°°°°°°°°°°°°°°"+
                    "wwþþþþþþþþþíííííííÝÝÜÜÜÜÜÜÜÜÛÛÛÛwwþþþþþþþþíííííííííÝÜÜÜÜÜÜÜÛÛÛÛÛwwþþþþvvýýíííííííííÝÜÜÜÜÜÜÜÛÛÛÛÛwvvvvvvvýííííííííííäÜÜÜÜÜÜÜÛÛÛÛÛ"+
                    "vvvvvvvvýííííííííäääÜÜÜÜÜÜÛÛÛÛÛÛvvvvvvvýýíííííííäääääÜÜÜÜÜÛÛÛÛÛÛvvvvvvvýýííííííääääääÜÜÜÜÜÛÛÛÛÛÛnvvvvvýýýíííííääääääääÜÜÜÚÚÚÚÚÚÚ"+
                    "mmmmmmMMMMMíääääääääääããÚÚÚÚÚÚÚÚmmmmmmMMMMM\034\034äääääääãããÔÔÚÚÚÚÚÚÚmmmmmmlM<<<\034\034\034äääääãããÔÔÔÙÙÚÚÚÚÚmmmlllll<<<,\034ôôôäEãããÔÔÔÔÔÙÙÙÙÙÙ"+
                    "lllllllll<,,,ôôôEEEEEÔÔÔÔÔÙÙÙÙÙÙllllllllldd+++\033EEEEEEÔÔÔÔÔÓÙÙÙÙÙllllllllddddd22#EEEEÄÔÔÔÓÓÓÓØØØØllllllllddddd2####EEÃÃÃÃÓÓÓÓÓØØØ"+
                    "{{{{{{{{dddddR]]]]#ÃÃÃÃÃÓÓÓÓÓÓÒØ{{{{{{{{zzddRR]]]]\022ÃÃÃÃÃÓÓÓÓÒÒÒÒ{{{{{zzzzzzRRRR]]\022\022\022\022ÃÃÈÈÓÓÒÒÒÒÒ{{zzzzzzzzzzRRR]\022\022\022\022\022\022ÃÈÈÒÒÒÒÒÒÒ"+
                    "zzzzzzzzzzzzcR\022\022\022\022\022ÈÂÂÂÂÂÒÒÒzzzzzzzz\022\022\022ÂÂÂÂÂÂÂÂÑÂÂÂÂÂÂÂÂ¸ÂÂÂÂÂÂÂ¨¨"+
                    "ÂÂ¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨"+
                    "¨¨¨¨¨°°°°°°°°°°°°°°°°°°"+
                    "wwþþþþþþþýíííííííííÝÜÜÜÜÜÜÜÛÛÛÛÛwwþþþþþþýýííííííííííÜÜÜÜÜÜÜÛÛÛÛÛwvvvvvvýýýííííííííííÜÜÜÜÜÜÜÛÛÛÛÛvvvvvvvýýýíííííííííäÜÜÜÜÜÜÛÛÛÛÛÛ"+
                    "vvvvvvýýýýíííííííääääÜÜÜÜÜÛÛÛÛÛÛvvvvvvýýýýííííííääääääÜÜÜÜÛÛÛÛÛÛvvvvvýýýýýíííííäääääääÜÜÜÚÚÚÚÚÚÚvvvvvýýýýíííííääääääääãÜÚÚÚÚÚÚÚÚ"+
                    "mmmmýýMMMMMMääääääääããããÚÚÚÚÚÚÚÚmmmmmMMMMMMMäääääääãããããÔÚÚÚÚÚÚÚmmmmmlMMM<<<\034äääääããããããÔÙÙÙÙÙÙÙmmllllll<<<<\034\034ôôãããããããÔÔÙÙÙÙÙÙÙ"+
                    "llllllll<<<+++\033\033EEããããâÔÔÔÙÙÙÙÙÙlllllllll++++\033\033\033EEEEEââÔÔÓÓØØØØØlllllllldddd+\033\033óóEEEEâÔÔÓÓÓÓØØØØlllllllkdddddRRóóóEEÃÃÃÓÓÓÓÓÓØØØ"+
                    "{{{{{{kkkdddRRR]]]]òÃÃÃÃÓÓÓÓÓÒØØ{{{{{zzzkkdRRRR]]]\022\022ÃÃÃÃÓÓÓÓÒÒÒÒ{{zzzzzzzzzcRRR]]\022\022\022\022\022ÃÈÓÓÒÒÒÒÒÒzzzzzzzzzzzcccR1\\\022\022\022\022\022\022ÈÂÒÒÒÒÒÒÒ"+
                    "zzzzzzzzzzzycc\\\\\\\\\022\022\022\022\022ÂÂÂÂÂÂÒÒÑzzzzzzzzzyyyy\022\022\022ÂÂÂÂÂÂÂÑÑzzzzzzzzyyy[ÂÂÂÂÂÂÂÂÑzzzyÂÂÂÂÂÂÂ¨¨"+
                    "ÂÂÂ¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨"+
                    "¨¨¨¨¨°°°°°°°°°°°°°°°°°°"+
                    "wþþþþþþýýýýííííííííììÜÜÜÜÜÜÛÛÛÛÛwvvvvvýýýýýíííííííìììÜÜÜÜÜÜÛÛÛÛÛvvvvvvýýýýýííííííììììÜÜÜÜÜÛÛÛÛÛÛvvvvvýýýýýíííííííììììÜÜÜÜÜÛÛÛÛÛÛ"+
                    "vvvvvýýýýýííííííìììììäÜÜÜÛÛÛÛÛÛÛvvvvýýýýýýíííííìììäääääÜÜÛÛÚÚÚÚÚvvvvýýýýýýíííííìääääääããÚÚÚÚÚÚÚÚvvvýýýýýýMMíííäääääääãããÚÚÚÚÚÚÚÚ"+
                    "mmmýýMMMMMMMMääääääããããããÚÚÚÚÚÚÚmmmmMMMMMMMMääääääããããããâÙÙÙÙÙÙÙmmmlluuuuu<<<äääãããããããââÙÙÙÙÙÙÙlllllluuu<<<<+\033ãããããããââââÙÙÙÙÙÙ"+
                    "llllllllu<<++\033\033\033ãããããâââââØØØØØØlllllllll++++\033\033\033óãããâââââÓÓØØØØØlllllllldd+++\033\033óóóóEââââÓÓÓÓØØØØlllllkkkkdddd;\032óóóóDDâââÓÓÓÓØØØØ"+
                    "{kkkkkkkkkddRRR]]]òòòòÃÓÓÓÓÓÒÒØØ{kkkkkkkkkkccRRR]1òòòòÃÓÓÓÒÒÒÒÒÒzzzzzzzzzkccccc111\"\022\022\022\022ÃÓÒÒÒÒÒÒÒzzzzzzzzzzzcccc11\\\\\022\022\022\022\022ÂÒÒÒÒÒÒÑ"+
                    "zzzzzzzzzzyycc\\\\\\\\\\\\\022\022[ÂÂÂÂÂÂÑÑÑzzzzzzzzyyyyyy\\\\\\\\\\\\[[[ÂÂÂÂÂÂÂÑÑzzzzzzyyyyyyyy[[[ÂÂÂÂÂÂÂÑÑzzzzyyyyyyyy[ÂÂÂÂÂÂÂÑÑ"+
                    "ÂÂÂ¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨"+
                    "¨¨¨¨°°°°°°°°°°°°°°°"+
                    "wvvvvvýýýýýííííííìììììÜÜÜÜÛÛÛÛÛÛvvvvvýýýýýýíííííììììììÜÜÜÜÛÛÛÛÛÛvvvvvýýýýýýííííììììììììÜÜÜÛÛÛÛÛÛvvvvýýýýýýýííííììììììììÜÜÛÛÛÛÛÛÛ"+
                    "vvvvýýýýýýýíííìììììììììÜÜÛÛÛÛÛÛÛvvvýýýýýýýýííììììììììììãÜÚÚÚÚÚÚÚvvvýýýýýýýýíìììììììììããããÚÚÚÚÚÚÚvvýýýýýýMMMMìììììììäãããããÚÚÚÚÚÚÚ"+
                    "mmýýýMMMMMMMììììääããããããâÚÚÚÚÚÚÚmmmuuuuuuMMMüìäääãããããããââÙÙÙÙÙÙmlluuuuuuuLLüüäããããããããâââÙÙÙÙÙÙllllluuuuLLL+\033\033ãããããããââââÙÙÙÙÙÙ"+
                    "lllllluuuL+++\033\033\033ãããããâââââØØØØØØllllllllL++++\033\033\033ããããââââââØØØØØØllllllkkk+++;;;\032óóãââââââÓÓØØØØØllkkkkkkkkd**;\032\032\032óDDââââÓÓÓÓØØØØ"+
                    "kkkkkkkkkkkc**\032\032\032DDDDDDâÓÓÓÒÒÒØØkkkkkkkkkkkccccR11òòòòòòÓÒÒÒÒÒÒÒzzkkkkkkkkccccc111\"òòòòòÒÒÒÒÒÒÒÒzzzzzzzzzzccccc111\"\"\022\022\022[ÂÒÒÒÒÒÑÑ"+
                    "zzzzzzzzyyyyccc\\\\\\\\\\[[[[ÂÂÂÂÂÑÑÑzzzzzzyyyyyyybb\\\\\\\\\\[[[[ÂÂÂÂÂÑÑÑzzzzyyyyyyyyyybb0000[[[[ÂÂÂÂÂÑÑÑzzyyyyyyyyyyyybb0000[[[\021\021ÂÂÂÂÑÑÑ"+
                    "\021\021\021\021\021Â¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨"+
                    "¨¨¨¨°°°°°°°°°°°°°°°"+
                    "vvvvýýýýýýýýííìììììììììÜÜÜÛÛÛÛÛÛvvvvýýýýýýýýííììììììììììÜÜÛÛÛÛÛÛvvvýýýýýýýýýíìììììììììììÜÛÛÛÛÛÛÛvvvýýýýýýýýýììììììììììììÜÛÛÛÛÛÛÛ"+
                    "vvýýýýýýýýýììììììììììììììÚÚÚÚÚÚÚvvýýýýýýýýýìììììììììììììãÚÚÚÚÚÚÚvýýýýýýýýýììììììììììììãããÚÚÚÚÚÚÚvýýýýýýýMMMìììììììììãããããÚÚÚÚÚÚÚ"+
                    "ýýýýuuMMMMMìììììììããããããââÙÙÙÙÙÙmuuuuuuuuuLLüüììãããããããâââÙÙÙÙÙÙluuuuuuuuLLLüüüüããããããââââÙÙÙÙÙÙllluuuuuLLLLüüüããããããââââââØØØØØ"+
                    "llllluuLLLLL+\033\033ãããããâââââââØØØØØllllllKKKK++;;;;ãããââââââââØØØØØllllkkkKKKK;;;;\032\032\032ââââââââÓØØØØØkkkkkkkkkk****\032\032\032\032DDâââââÓÓÓØØØØ"+
                    "kkkkkkkkkkk***\032\032\032DDDDDDâÓÓÒÒÒÒØØkkkkkkkkkkkcccc\0311òòòòòòòáÒÒÒÒÒÒÒkkkkkkkkkkccccc111\"òòòòòÒÒÒÒÒÒÑÑzzzzkkkkkkccccc11\"\"\"\"[[[ÂÒÒÒÒÑÑÑ"+
                    "zzzzzzyyyyyyccbQQQ\\0[[[[ÂÂÂÂÑÑÑÑzzzzyyyyyyyyybbbQ000[[[[ÂÂÂÂÑÑÑÑzzyyyyyyyyyyybbb0000[[[[\021ÂÂÂÑÑÑÑyyyyyyyyyyyyybbb0000[[[\021\021\021\021ÂÑÑÑÑ"+
                    "bb0000[\021\021\021\021\021\021\021\021ÁÁÁ\021\021\021\021\021ÁÁÁÁÁÁÁÁÁÁÁÁ"+
                    "ÀÀÀÀÀÀÀÀÀÀÀÀÀÀÀÀÀÀ"+
                    "vvvýýýýýýýýýìììììììììììììÜÛÛÛÛÛÛvvýýýýýýýýýýìììììììììììììÛÛÛÛÛÛÛvvýýýýýýýýýììììììììììììììÛÛÛÛÛÛÛvýýýýýýýýýýììììììììììììììÛÛÛÛÛÚÚ"+
                    "výýýýýýýýýìììììììììììììììÚÚÚÚÚÚÚýýýýýýýýýýììììììììììììììãÚÚÚÚÚÚÚýýýýýýýýýìììììììììììììãããâÚÚÚÚÚÚýýýýýýýMMMìììììììììììãããââÙÙÙÙÙÙ"+
                    "ýuuuuuuuuuìììììììììããããââââÙÙÙÙÙuuuuuuuuuLLüüüüììãããããâââââÙÙÙÙÙuuuuuuuuLLLüüüüüããããããâââââØØØØØluuuuuuLLLLüüüüüãããããââââââØØØØØ"+
                    "llluuuLLLLLüüüüãããããâââââââØØØØØlllKKKKKKKKK;;;;ãããââââââââØØØØØlkkkKKKKKKK;;;;\032\032\032âââââââââØØØØØkkkkkkkkKKK****\032\032DDDââââââáØØØØØ"+
                    "kkkkkkkkkkk***\031\031\031DDDDDDâáááÒÒØØØkkkkkkkkkkkccc\031\031\031DDDDDDááááÒÒÒÒÑkkkkkkkkkkccccc:1\"\"òòòòááááÒÒÑÑÑkkkkkkkkkjjccc::1Q\"CCC[[áááÑÑÑÑÑ"+
                    "zzzzzyyyyyjjjbbbQQQ0[[[[[ÂÂÑÑÑÑÑzzzyyyyyyyyjjbbbQQ00[[[[[\021ÂÑÑÑÑÑsyyyyyyyyyyyjbbb0000[[[[\021\021\021ÑÑÑÑÑsyyyyyyyyyyyybbb0000[[\021\021\021\021\021\021ÑÑÑÑ"+
                    "sxxxxxxbb0000ZZ\021\021\021\021\021\021\021ÁÁÁxxxxxxaa0!ZZZ\021\021\021\021\021\021ÁÁÁÁrxxxxxxxaaaZ\021\021ÁÁÁÁrxxxxxxaaÁÁÁÁ"+
                    "rxxxxxÀÀÀÀÀrxxxxxÀÀÀÀÀrxxxÀÀÀÀÀÀqqqxxÀÀÀÀÀÀ"+
                    "výýýýýýýýýýìììììììììììììììÛÛÛÛÛÛvýýýýýýýýýýìììììììììììììììÛÛÛÛÛÛýýýýýýýýýýììììììììììììììììÛÛÛÛÛÛýýýýýýýýýýìììììììììììììììëÚÚÚÚÚÚ"+
                    "ýýýýýýýýýììììììììììììììëëëÚÚÚÚÚÚýýýýýýýýýìììììììììììììëëëëÚÚÚÚÚÚýýýýýýýýìììììììììììììëëëëâÚÚÚÚÚÚýýýýýýuuììììììììììììëëëãâââÙÙÙÙÙ"+
                    "uuuuuuuuLLìììììììììëãããââââÙÙÙÙÙuuuuuuuuLLüüüüüìììããããâââââØØØØØuuuuuuuLLLüüüüüüüããããââââââØØØØØuuuuuuLLLLLüüüüüããããâââââââØØØØØ"+
                    "KKKKKKKKKKKüüüüüãããââââââââØØØØØtKKKKKKKKKKK;;;;ããâââââââââØØØØØttttKKKKKKKK;;;\032\032\032âââââââââØØØØØtttttttKKKK****\031\031\031DDâââââáááØØØØ"+
                    "ttttttttttK**\031\031\031\031\031DDDDáááááááØØØttttttttttkc\031\031\031\031\031)DDDDáááááááÑÑÑttttttkkkkccc:::::CCCCáááááááÑÑÑttttkkkkjjjjc::::QCCCCñáááááÑÑÑÑ"+
                    "ssssyyjjjjjjjbbbQQQCCñññññáÑÑÑÑÑssssyyyjjjjjjbbbbQ00[ññññ\021\021ÑÑÑÑÑssssyyyyjjjjjbbbb0!!![[\021\021\021\021\021ÑÑÑÑssssyyyyyjjjjbbbb!!!!ZZ\021\021\021\021\021ÑÑÑÑ"+
                    "sssxxxxxxxxxxxbba!!!ZZZ\021\021\021\021\021ÁÁÁÁrrrxxxxxxxxxxxxaaaaZZZZZ\021\021\021\021ÁÁÁÁrrrxxxxxxxxxxxxaaaaZZZZZ\021\021\021ÁÁÁÁÁrrrxxxxxxxxxxxxaaaaZZZÁÁÁÁÁ"+
                    "rrrxxxxxxxxxxxxaaaa8ÀÀÀÀÀÀrrrxxxxxxxxxxxxiaa8ÀÀÀÀÀÀrrqqqxxxxxxxxxiia88ÀÀÀÀÀÀÀqqqqqqqqxxxxxhhhh@@@ÀÀÀÀÀ"+
                    "ýýýýýýýýýýììììììììììììììììëÛÛÛÛÛýýýýýýýýýýìììììììììììììììëëÛÛÛÛÛýýýýýýýýýìììììììììììììììëëëÚÚÚÚÚýýýýýýýýýììììììììììììììëëëëÚÚÚÚÚ"+
                    "ýýýýýýýýìììììììììììììëëëëëëÚÚÚÚÚýýýýýýýýììììììììììììëëëëëëëÚÚÚÚÚýýýýýýýýìììììììììììëëëëëëëëÙÙÙÙÙýýýuuuuuììììììììììëëëëëëëââÙÙÙÙÙ"+
                    "uuuuuuuLLììììììììëëëëëëâââââØØØØuuuuuuLLLLüüüüüüëëëëëëââââââØØØØuuuuuLLLLLüüüüüüëëëãââââââââØØØØuuuuuLLLLLüüüüüüüãããââââââââØØØØ"+
                    "KKKKKKKKKKKüüüüüããââââââââââØØØØttKKKKKKKKKKK;;;\032âââââââââââØØØØtttttKKKKKKKK*;\031\031\031ââââââââááØØØØttttttttKKKK**\031\031\031\031DââââááááááØØØ"+
                    "tttttttttttK\031\031\031\031\031))DDááááááááááØtttttttttttt\031\031)))))DDáááááááááÑÑtttttttttttj::::)\030\030CCCáááááááÑÑÑstttttttjjjjj::::\030CCCCñáááááÑÑÑÑ"+
                    "sssssjjjjjjjjbbbbQCCCññññááÑÑÑÑÑsssssjjjjjjjjbbbb!!!ññññññ\021ÑÑÑÑÑsssssjjjjjjjjbbbb!!!!ñññ\021\021\021\021ÑÑÑÑssssssjjjjjjjbbbb!!!PZZZ\021\021\021\021\021ÑÑÑ"+
                    "ssssxxxxxxxxxxaaaaPPZZZZ\021\021\021\021ÁÁÁÁrrrrxxxxxxxxxxaaaaaPZZZZZ\021\021\021ÁÁÁÁrrrrxxxxxxxxxxaaaaaaZZZZZ\021\021ÁÁÁÁÁrrrrxxxxxxxxxxiaaaa88ZZZYYYYÁÁÁÁ"+
                    "rrrrxxxxxxxxxiiaaa8888YYYYYÀÀÀÀÀrrrrrxxxxxxxxiiiaa8888YYYYYÀÀÀÀÀrrqqqqqqxxxxiiihh@@@@pYYYYÀÀÀÀÀqqqqqqqqqqqHhhhhh@@@@pppÀÀÀ"+
                    "ýýýýýýýýýìììììììììììììììëëëëÛÛÛÛýýýýýýýýýììììììììììììììëëëëëÛÚÚÚýýýýýýýýììììììììììììììëëëëëëÚÚÚÚýýýýýýýýììììììììììììëëëëëëëëÚÚÚÚ"+
                    "ýýýýýýýììììììììììììëëëëëëëëëÚÚÚÚýýýýýýýìììììììììììëëëëëëëëëëÙÚÚÚýýýýýýýììììììììììëëëëëëëëëëëÙÙÙÙuuuuuuuìììììììììëëëëëëëëëëââÙÙÙÙ"+
                    "uuuuuuLLLüììììëëëëëëëëëëââââØØØØuuuuuLLLLüüüüüëëëëëëëëëâââââØØØØuuuuLLLLLüüüüüüëëëëëëâââââââØØØØKKKKKKKKKKüüüüüëëëëëââââââââØØØØ"+
                    "KKKKKKKKKKKKüüüüûûââââââââââØØØØtttKKKKKKKKKKKûûûûââââââââááØØØØttttttKKKKKKK*\031\031\031ûââââââáááááØØØtttttttttKKKK\031\031\031\031\031)âââáááááááááØ"+
                    "tttttttttttt\031\031\031))))\030Dááááááááááátttttttttttt)))))\030\030\030\030ááááááááááÑtttttttttttJ))))\030\030\030\030CáááááááááÑÑssstttttjjjjj:::\030\030\030CCññááááááÑÑÑ"+
                    "ssssssjjjjjjjbbb\030\030CCñññññáááÑÑÑÑssssssjjjjjjjbbbb!!!ñññññññÑÑÑÑÑssssssjjjjjjjbbb9!!!PPñññ\021\021\021ÑÑÑÑsssssssjjjjjjbbb9!PPPPZZZ\021\021\021ÐÐÐÐ"+
                    "ssssssxxxxxxxaaaaaPPPZZZZZ\021\021ÐÐÐÐrrrrrxxxxxxxxiaaaaaaZZZZZZ\021YÐÐÐÐrrrrrxxxxxxxiiaaaaa88ZZZZYYYÁÁÁÁrrrrrrxxxxxxiiiaaaa8888YYYYYÁÁÁÁ"+
                    "rrrrrrxxxxxiiiiiaa8888YYYYYYÀÀÀÀrrrrrqqxxxxiiiiia88888YYYYYÀÀÀÀÀrrqqqqqqqqqiHHhhh@@@@ppYYYÀÀÀqqqqqqqqqqqHhhhhh@@@@pppÀ"+
                    "ýýýýýýýýììììììììììììììëëëëëëëÛÛÛýýýýýýýýìììììììììììììëëëëëëëëÚÚÚýýýýýýýìììììììììììììëëëëëëëëëÚÚÚýýýýýýýìììììììììììëëëëëëëëëëëÚÚÚ"+
                    "ýýýýýýìììììììììììëëëëëëëëëëëëÚÚÚýýýýýýììììììììììëëëëëëëëëëëëëÙÙÙýýýýuuìììììììììëëëëëëëëëëëëëëÙÙÙuuuuuuLììììììëëëëëëëëëëëëëëëØØØØ"+
                    "uuuuuLLLüüììëëëëëëëëëëëëëëêêØØØØuuuuLLLLüüüüëëëëëëëëëëëëêêêêØØØØuKKKLLLLüüüüüëëëëëëëëëêêêêêêØØØØKKKKKKKKKKüüüüëëëëëëëêêêêêêêØØØØ"+
                    "ttKKKKKKKKKKüûûûûûûêêêêêêêêêØØØØttttKKKKKKKKKûûûûûûêêêêêêêááááØØtttttttKKKKKKûûûûûûêêêêêáááááááØttttttttttKKKûûûûûûêêêáááááááááá"+
                    "ttttttttttttJ))))\030\030\030áááááááááááátttttttttttJJ)))\030\030\030\030\030ááááááááááástttttttttJJJJ)\030\030\030\030\030\030ááááááááááÑssssstttJJJJJJ\030\030\030\030\030\030CñááááááááÑÑ"+
                    "sssssssjjjjjJbb(((((ñññññááááÑÑÑsssssssjjjjjjbb9999!PññññññàààÑÑssssssssjjjjjbb9999PPPPññZàààààÐssssssssjjjjjba999PPPPPZZZààààÐÐ"+
                    "sssssssxxxxiiaaaaaaPPPZZZZZàÐÐÐÐrrrrrrrxxxxiiiaaaaa88ZZZZYYYÐÐÐÐrrrrrrrxxxiiiiiaaaa8888YYYYYÐÐÐÐrrrrrrrxxiiiiiiaaa88888YYYYYYÐÐÐ"+
                    "rrrrrrrxxiiiiiiia888888YYYYYYÀÀÀrrrrqqqqqiiiiiiHh8888`YYYYYYÀÀÀrrqqqqqqqqqHHHhhh@@@`pppYYÀqqqqqqqqqqqHhhhhh@@@@pppp"+
                    "ýýýýýýýììììììììììììëëëëëëëëëëëëÚýýýýýýýìììììììììììëëëëëëëëëëëëëÚýýýýýýìììììììììììëëëëëëëëëëëëëÚÚýýýýýýììììììììììëëëëëëëëëëëëëëÚÚ"+
                    "ýýýýýìììììììììëëëëëëëëëëëëëëëëÙÙýýýýýììììììììëëëëëëëëëëëëëëëëëÙÙýuuuuìììììììëëëëëëëëëëëëëëëëëêØØuuuuLLìììììëëëëëëëëëëëëëëëëêêêØØ"+
                    "uuuLLLLüüüëëëëëëëëëëëëëëëêêêêêØØuuLLLLLüüüëëëëëëëëëëëëëêêêêêêêØØKKKKKKKKüüüëëëëëëëëëëëêêêêêêêêØØKKKKKKKKKKüüëëëëëëëëêêêêêêêêêêØØ"+
                    "tttKKKKKKKKKûûûûûûûêêêêêêêêêêêØØttttttKKKKKKûûûûûûûêêêêêêêêêáááØtttttttttKKKûûûûûûûêêêêêêêááááááttttttttttttûûûûûûûêêêêêáááááááá"+
                    "tttttttttttJJJû)\030\030\030\030êêááááááááááttttttttttJJJJ)\030\030\030\030\030úúáááááááááássstttttJJJJJJJ\030\030\030((úúáááááááááásssssssJJJJJJJJ((((((úúááááááááÑ"+
                    "ssssssssJJJJJJ(((((((ññééééééàààsssssssssjJJJJ999999BBBñùùùàààààsssssssssjjIII99999BBBBBùùùàààààsssssssssIIIII99999BBBBBùùàààààÐ"+
                    "ssssssssiiiiiiaaaaaBBBBZZààààÐÐÐrrrrrrrriiiiiiaaaaa8888 YYYYÐÐÐÐrrrrrrrriiiiiiiaaa88888 YYYYÐÐÐÐrrrrrrrriiiiiiiaaa8888  YYYYYÐÐÐ"+
                    "rrrrrrrqqiiiiiiia88888 YYYYYYYÀÀrrrrqqqqqqHHHHHHh@`````YYYYY\020rqqqqqqqqqqHHHhhh@@@`ppppXqqqqqqqqqqqHhhhhh@@@@pppp"+
                    "ýýýýýýìììììììììììëëëëëëëëëëëëëëëýýýýýýììììììììììëëëëëëëëëëëëëëëëýýýýýììììììììììëëëëëëëëëëëëëëëëëýýýýýììììììììëëëëëëëëëëëëëëëëëëë"+
                    "ýýýýììììììììëëëëëëëëëëëëëëëëëëëÙýýýýìììììììëëëëëëëëëëëëëëëëëëëêØuuuuìììììëëëëëëëëëëëëëëëëëëëêêêØuuuLLLììëëëëëëëëëëëëëëëëëëêêêêêØ"+
                    "uuLLLLüüëëëëëëëëëëëëëëëëêêêêêêêØKKKKKKKüëëëëëëëëëëëëëëëêêêêêêêêØKKKKKKKKüëëëëëëëëëëëëêêêêêêêêêêØtKKKKKKKKKëëëëëëëëëêêêêêêêêêêêêØ"+
                    "ttttKKKKKKKûûûûûûûûêêêêêêêêêêêêØtttttttKKKKûûûûûûûûêêêêêêêêêêêááttttttttttKûûûûûûûûêêêêêêêêêáááátttttttttttûûûûûûûêêêêêêêêáááááá"+
                    "ttttttttttJJJJûûûûêêêêêááááááááástttttttJJJJJJJ\030(((úúúúúáááááááásssssttJJJJJJJJ((((úúúúééééééééésssssssJJJJJJJJ(((((úúúééééééééà"+
                    "ssssssssJJJJJJ(((((((úéééééééàààsssssssssJJJJI999999BBBùùùùàààààsssssssssIIIII99999BBBBùùùùààààèsssssssssIIIIII999BBBBBùùùàààààè"+
                    "rrrrrrrrIIIIIIaaaaaBBBBùùùààààèèrrrrrrrrriiiiiaaaa8888   YYààÐÐÐrrrrrrrrriiiiiiaaa8888  YYYYðøøÐrrrrrrrrriiiiiiaa88888  YYYYðøøø"+
                    "rrrrrrrqqiiiHHHHH8888   YYYYYøøørrrrqqqqqqHHHHHHh@`````pYYXXX\020\020\020rqqqqqqqqqqHHHhhh@@@``pppXXX\020\020qqqqqqqqqqqHhhhhh@@@@pppppX\020"+
                    "ýýýýýììììììììììëëëëëëëëëëëëëëëëëýýýýýììììììììëëëëëëëëëëëëëëëëëëëýýýýììììììììëëëëëëëëëëëëëëëëëëëëýýýýìììììììëëëëëëëëëëëëëëëëëëëëë"+
                    "ýýýììììììëëëëëëëëëëëëëëëëëëëëëëêýýýìììììëëëëëëëëëëëëëëëëëëëëëêêêuuuLììëëëëëëëëëëëëëëëëëëëëëêêêêêuuLLLëëëëëëëëëëëëëëëëëëëëëêêêêêê"+
                    "LLLLLLëëëëëëëëëëëëëëëëëëêêêêêêêêKKKKKKKëëëëëëëëëëëëëëëêêêêêêêêêêKKKKKKKKëëëëëëëëëëëëêêêêêêêêêêêêttKKKKKKKûûûûûûûûûûêêêêêêêêêêêêê"+
                    "tttttKKKKûûûûûûûûûûêêêêêêêêêêêêêttttttttKKûûûûûûûûûêêêêêêêêêêêêêttttttttttûûûûûûûûêêêêêêêêêêêêááttttttttttJûûûûûûûêêêêêêêêêááááá"+
                    "tttttttttJJJJJûûûûêêêêêúéééééééésssttttJJJJJJJJ(((úúúúúúééééééééssssssJJJJJJJJJ(((úúúúúééééééééésssssssJJJJJJJJ((((úúúúééééééééà"+
                    "ssssssssJJJJJJ((((((úúéééééééàààsssssssssJIIIII99999BBùùùùùùààààsssssssssIIIIII9999BBBùùùùùààààèssssssssIIIIIIII99BBBBùùùùààààèè"+
                    "rrrrrrrrIIIIIIIaaaBBBBAùùùààààèèrrrrrrrrrIIIIIaaaa88AAA  ððààèèèrrrrrrrrriiiiiiaa888AA   ððððøøørrrrrrrrriiiiiiia888A    ððððøøø"+
                    "rrrrrrrqqqHHHHHHH8````   YðððøøørrrrqqqqqqHHHHHHh@`````ppXXXX\020\020\020rqqqqqqqqqqHHHhhh@@@``pppXXXX\020\020\020qqqqqqqqqqqHhhhhh@@@@pppppXXX\020\020\020"+
                    "ýýýýììììììììëëëëëëëëëëëëëëëëëëëëýýýýìììììììëëëëëëëëëëëëëëëëëëëëëýýýììììììëëëëëëëëëëëëëëëëëëëëëëëýýýìììììëëëëëëëëëëëëëëëëëëëëëëëë"+
                    "ýýììììëëëëëëëëëëëëëëëëëëëëëëëëêêuuìììëëëëëëëëëëëëëëëëëëëëëëëêêêêuLLìëëëëëëëëëëëëëëëëëëëëëëëêêêêêLLLLëëëëëëëëëëëëëëëëëëëëëêêêêêêê"+
                    "KKKKKëëëëëëëëëëëëëëëëëëêêêêêêêêêKKKKKKëëëëëëëëëëëëëëëêêêêêêêêêêêKKKKKKKKëëëëëëëëëëëëêêêêêêêêêêêêtttKKKKKûûûûûûûûûûûêêêêêêêêêêêêê"+
                    "ttttttKKûûûûûûûûûûêêêêêêêêêêêêêêtttttttttûûûûûûûûûêêêêêêêêêêêêêêtttttttttûûûûûûûûûêêêêêêêêêêêêêétttttttttJJûûûûûûûêêêêêêêêêêéééé"+
                    "stttttttJJJJJûûûûûêêúúúúééééééééssssttJJJJJJJJJJ(úúúúúúúééééééééssssssJJJJJJJJJ((úúúúúúééééééééésssssssJJJJJJJJ(((úúúúúééééééééé"+
                    "ssssssssJJJJJJJ((((úúúéééééééàààsssssssssIIIIII9999(úùùùùùùùàààèsssssssssIIIIIII999BBùùùùùùààààèssssssssIIIIIIII99BBBùùùùùùàààèè"+
                    "rrrrrrrrIIIIIIIIaaAAAAAùùùàààèèèrrrrrrrrrIIIIIIaaAAAAAA  ðððàèèèrrrrrrrrriiiiiiaa8AAAAA  ððððøøørrrrrrrrriiHHHHH88AAAA   ððððøøø"+
                    "rrrrrrrqqqHHHHHHH`````   ððððøøørrrrqqqqqqHHHHHhh@`````ppXXXXX\020\020rqqqqqqqqqqHHHhhh@@@``pppXXXX\020\020\020qqqqqqqqqqqHhhhhh@@@@pppppXXX\020\020\020"
    )) {
        private final byte[] primary = {
                28, 36, 44, 52, 60, 68, 76, 84, 92, 100, 108, 116, 124, -124, -116, -108, -100, -92, -84, -76, -68, -52, -44, -36, -28, -20, -12, -4
        }, grays = {
                23, 22, 21, 20, 19, 18, 17, 16
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
            if((voxel & 7) == 0)
                return 16;
            return (byte) ((voxel & 248) + (voxel & 7) - 1);
        }

        @Override
        public byte darken(byte voxel) {
            if((voxel & 7) >= 6)
                return 23;
            return (byte)((voxel & 248) + (voxel & 7) + 2);
        }

        @Override
        public int bright(byte voxel) {
            if((voxel & 7) == 0)
                return Coloring.RINSED[16];
            return Coloring.RINSED[(voxel & 248) + (voxel & 7) - 1];
        }

        @Override
        public int medium(byte voxel) {
            return Coloring.RINSED[(voxel & 255)];
        }

        @Override
        public int dim(byte voxel) {
            if((voxel & 7) == 7)
                return Coloring.RINSED[23];
            return Coloring.RINSED[(voxel & 248) + (voxel & 7) + 1];
        }

        @Override
        public int dark(byte voxel)
        {
            if((voxel & 7) >= 6)
                return Coloring.RINSED[23];
            return Coloring.RINSED[(voxel & 248) + (voxel & 7) + 2];
        }

        @Override
        public int getShadeBit() {
            return 0;
        }
        @Override
        public int getWaveBit() {
            return 0;
        }
    };
}
