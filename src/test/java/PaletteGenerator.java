import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import squidpony.StringKit;
import squidpony.squidmath.IntIntOrderedMap;
import warpwriter.Coloring;

import java.util.Comparator;
import java.util.TreeSet;

/**
 * Created by Tommy Ettinger on 1/21/2018.
 */
public class PaletteGenerator extends ApplicationAdapter {

    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Palette Stuff");
        config.setWindowedMode(1000, 600);
        config.setIdleFPS(10);
        new Lwjgl3Application(new PaletteGenerator(), config);
    }
    private static float hue(int rgba)
    {
        final float r = (rgba >>> 24 & 255) * 0.003921569f, g = (rgba >>> 16 & 255) * 0.003921569f,
                b = (rgba >>> 8 & 255) * 0.003921569f;//, a = (e >>> 24 & 254) / 254f;
        final float min = Math.min(Math.min(r, g), b);   //Min. value of RGB
        final float max = Math.max(Math.max(r, g), b);   //Max value of RGB
        final float delta = max - min;                           //Delta RGB value

        if ( delta < 0.0001f )                     //This is a gray, no chroma...
        {
            return 0f;
        }
        else                                    //Chromatic data...
        {
            final float rDelta = (((max - r) / 6f) + (delta * 0.5f)) / delta;
            final float gDelta = (((max - g) / 6f) + (delta * 0.5f)) / delta;
            final float bDelta = (((max - b) / 6f) + (delta * 0.5f)) / delta;

            if      (r == max) return (1f + bDelta - gDelta) % 1f;
            else if (g == max) return ((4f / 3f) + rDelta - bDelta) % 1f;
            else               return ((5f / 3f) + gDelta - rDelta) % 1f;
        }
    }
    public void create() {         
        final float[] hues = {0.0f, 0.07179487f, 0.07749468f, 0.098445594f, 0.09782606f, 0.14184391f, 0.16522992f,
                0.20281118f, 0.20285714f, 0.21867621f, 0.25163394f, 0.3141666f, 0.3715499f, 0.37061405f, 0.44054055f,
                0.49561405f, 0.53289473f, 0.53312635f, 0.5931374f, 0.6494253f, 0.7192119f, 0.7562056f, 0.7564103f,
                0.8037036f, 0.8703703f, 0.9282946f, 0.92884994f};
        final float[] sats = {0.863354f, 0.2742616f, 0.8051282f, 0.7751004f, 0.519774f, 0.7768595f, 0.46031743f, 0.36086953f,
                        0.9067358f, 0.94630873f, 0.20731705f, 0.91324204f, 0.6946903f, 0.4691358f, 0.74596775f,
                        0.47698745f, 0.38f, 0.8846154f, 0.86624205f, 0.25777775f, 0.9575472f, 0.81385285f, 0.6453901f,
                        0.746888f, 0.46153846f, 0.48863637f, 0.9395605f};
        int[] PALETTE = new int[256];
//        System.arraycopy(new int[]{
//                0x00000000, 0x444444ff, 0x000000ff, 0x88ffff00, 0x212121ff, 0x00ff00ff, 0x0000ffff, 0x080808ff,
//                0xff574600, 0xffb14600, 0xfffd4600, 0x4bff4600, 0x51bf6c00, 0x4697ff00, 0x9146ff00, 0xff46ae00,
//                // unseven regular after this
//                0xFCFCFCFF, 0xC3CBDBFF, 0xA096D1FF, 0x62507EFF, 0x424556FF, 0x252A32FF, 0x14161FFF, 0x0A0B0FFF,
//                0x888C78FF, 0x585651FF, 0x453C3CFF, 0x32222EFF, 0xFF8F8FFF, 0xFF2245FF, 0xD50964FF, 0x9C0565FF,
//                0xFFD800FF, 0xFF9000FF, 0xE93100FF, 0xBF0000FF, 0xE5FF05FF, 0xA7ED00FF, 0x4AB907FF, 0x0A5D45FF,
//                0x00FFF0FF, 0x00B9FFFF, 0x008DF0FF, 0x1664C5FF, 0xFFE822FF, 0xFFA939FF, 0xE56335FF, 0xE5233EFF,
//                0xFFFC00FF, 0xEBB70AFF, 0xBE8420FF, 0x915816FF, 0xFFB35BFF, 0xD77E4BFF, 0xB15C51FF, 0x793D4EFF,
//                0xFF70DFFF, 0xFF22A9FF, 0x611381FF, 0x45064BFF, 0xCCFFF5FF, 0x6DF7B1FF, 0x00C19AFF, 0x017687FF,
//                0x7BD5F3FF, 0x6C88FFFF, 0x6440D8FF, 0x3D2E93FF, 0x85A3C7FF, 0x676CADFF, 0x683395FF, 0x323751FF,
//                0xFF59BEFF, 0xC51AEAFF, 0x6E10ABFF, 0x331685FF, 0xFB9585FF, 0xE97461FF, 0xB53772FF, 0x93278FFF,
//        }, 0, PALETTE, 0, 80);
//        int[] initial = {
//                0x00000000, 0x444444ff, 0x000000ff, 0x88ffff00, 0x212121ff, 0x00ff00ff, 0x0000ffff, 0x080808ff,
//                0xff574600, 0xffb14600, 0xfffd4600, 0x4bff4600, 0x51bf6c00, 0x4697ff00, 0x9146ff00, 0xff46ae00,
//                0xffffffff, 0xeeeeeeff, 0xddddddff, 0xccccccff, 0xbbbbbbff, 0xaaaaaaff, 0x999999ff, 0x888888ff,
//                0x777777ff, 0x666666ff, 0x555555ff, 0x444444ff, 0x333333ff, 0x222222ff, 0x111111ff, 0x000000ff,
//                0xff8b7fff, 0xff5746ff, 0xeb3623ff, 0xcc1c0aff, 0xa50f00ff, 0x720a00ff,
//                0xffcbb2ff, 0xebb093ff, 0xcc8a6bff, 0xa5684aff, 0x79462dff, 0x4c2816ff,
//                0xffc87fff, 0xffaf46ff, 0xeb9623ff, 0xcc790aff, 0xa55e00ff, 0x724100ff,
//                0xffe3bfff, 0xebcb9fff, 0xcca675ff, 0xa58252ff, 0x795b33ff, 0x4c371aff,
//                0xfffeacff, 0xfffd59ff, 0xf2f136ff, 0xdfdd1bff, 0xc5c409ff, 0xacab00ff,
//                0xf8e7cdff, 0xf2d09dff, 0xdfb26fff, 0xbf8f47ff, 0x996c2aff, 0x6c4915ff,
//                0x83ff7fff, 0x4bff46ff, 0x29eb23ff, 0x10cc0aff, 0x05a500ff, 0x037200ff,
//                0xc1ffbfff, 0xa1eb9fff, 0x78cc75ff, 0x55a552ff, 0x357933ff, 0x1c4c1aff,
//                0x8ecc9eff, 0x51bf6cff, 0x33ac51ff, 0x1d923aff, 0x0f7929ff, 0x06591bff,
//                0xbffffdff, 0x9febe9ff, 0x75ccc9ff, 0x52a5a3ff, 0x337977ff, 0x1a4c4bff,
//                0x7fb7ffff, 0x4697ffff, 0x237bebff, 0x0a5eccff, 0x0048a5ff, 0x003272ff,
//                0xbfdbffff, 0x9fc0ebff, 0x759bccff, 0x5277a5ff, 0x335179ff, 0x1a304cff,
//                0xb37fffff, 0x9146ffff, 0x7423ebff, 0x580accff, 0x4300a5ff, 0x2e0072ff,
//                0xd9bfffff, 0xbe9febff, 0x9875ccff, 0x7452a5ff, 0x4f3379ff, 0x2e1a4cff,
//                0xff7fc7ff, 0xff46aeff, 0xeb2394ff, 0xcc0a77ff, 0xa5005dff, 0x720040ff,
//                0xffbfe3ff, 0xeb9fcaff, 0xcc75a6ff, 0xa55281ff, 0x79335aff, 0x4c1a36ff,
//        };
//        int[] initial = {
//                0x62507EFF,
//                0xA096D1FF,
//                0xC3CBDBFF,
//                0xFCFCFCFF,
//                0x0A0B0FFF,
//                0x14161FFF,
//                0x252A32FF,
//                0x424556FF,
//                0x32222EFF,
//                0x453C3CFF,
//                0x585651FF,
//                0x888C78FF,
//                0x9C0565FF,
//                0xD50964FF,
//                0xFF2245FF,
//                0xFF8F8FFF,
//                0xBF0000FF,
//                0xE93100FF,
//                0xFF9000FF,
//                0xFFD800FF,
//                0x0A5D45FF,
//                0x4AB907FF,
//                0xA7ED00FF,
//                0xE5FF05FF,
//                0x1664C5FF,
//                0x008DF0FF,
//                0x00B9FFFF,
//                0x00FFF0FF,
//                0xE5233EFF,
//                0xE56335FF,
//                0xFFA939FF,
//                0xFFE822FF,
//                0x915816FF,
//                0xBE8420FF,
//                0xEBB70AFF,
//                0xFFFC00FF,
//                0x793D4EFF,
//                0xB15C51FF,
//                0xD77E4BFF,
//                0xFFB35BFF,
//                0x45064BFF,
//                0x611381FF,
//                0xFF22A9FF,
//                0xFF70DFFF,
//                0x017687FF,
//                0x00C19AFF,
//                0x6DF7B1FF,
//                0xCCFFF5FF,
//                0x3D2E93FF,
//                0x6440D8FF,
//                0x6C88FFFF,
//                0x7BD5F3FF,
//                0x323751FF,
//                0x683395FF,
//                0x676CADFF,
//                0x85A3C7FF,
//                0x331685FF,
//                0x6E10ABFF,
//                0xC51AEAFF,
//                0xFF59BEFF,
//                0x93278FFF,
//                0xB53772FF,
//                0xE97461FF,
//                0xFB9585FF,
//                0xFFFC2EFF,
//                0xFFD800FF,
//                0xFF9000FF,
//                0xE93100FF,
//                0xBF0000FF,
//                0xFFA939FF,
//                0xFCE945FF,
//                0xDAAF1CFF,
//                0xBE8420FF,
//                0x915816FF,
//                0x79390AFF,
//                0xE56335FF,
//                0xCEAF47FF,
//                0xE5FF05FF,
//                0xA7ED00FF,
//                0x4AB907FF,
//                0x01933FFF,
//                0x0A5D45FF,
//                0xCE2038FF,
//                0x957757FF,
//                0xC0B10AFF,
//                0x00FFF0FF,
//                0x00B9FFFF,
//                0x008DF0FF,
//                0x1664C5FF,
//                0x1F2E8EFF,
//                0x8A0B41FF,
//                0x5C3747FF,
//                0x5B6731FF,
//                0x6C88FFFF,
//                0xFF8F8FFF,
//                0xFF2245FF,
//                0xD50964FF,
//                0x9C0565FF,
//                0x69086AFF,
//                0x3C1E2BFF,
//                0x323751FF,
//                0x6440D8FF,
//                0xFF22A9FF,
//                0xFFB164FF,
//                0xD77E4BFF,
//                0xB15C51FF,
//                0x793D4EFF,
//                0x522D4BFF,
//                0x45064BFF,
//                0x6E10ABFF,
//                0xB53772FF,
//                0xC3CBDBFF,
//                0x424556FF,
//                0x331685FF,
//                0x93278FFF,
//                0xA096D1FF,
//                0x252A32FF,
//                0x442B61FF,
//                0x81709AFF,
//                0x14161FFF,
//                0x62507EFF,
//                0x0A0B0FFF,
//        };
//        int t;
//        for (int i = 0, e = initial.length - 1; i < e; i++, e--) {
//            t = initial[i];
//            initial[i] = initial[e];
//            initial[e] = t;
//        }
//        IntIntOrderedMap iiom = new IntIntOrderedMap(initial, initial);
//        initial = iiom.keysAsArray();
        //uncomment next line to actually use unseven full, with more colors
//        System.arraycopy(initial, 0, PALETTE, 16, initial.length);

        //System.arraycopy(initial, 0, PALETTE, 128, 128);
        System.arraycopy(Coloring.RINSED, 0, PALETTE, 0, 256);
        IntIntOrderedMap hueToIndex = new IntIntOrderedMap(32);
        for (int i = 18, s = 16; i < 256; i += 8, s += 8) {
            hueToIndex.put((int)(hue(Coloring.RINSED[i]) * 1024), s);
        }
        TreeSet<IntIntOrderedMap.MapEntry> sorted = new TreeSet<>(new Comparator<IntIntOrderedMap.MapEntry>(){
            /**
             * Compares its two arguments for order.  Returns a negative integer,
             * zero, or a positive integer as the first argument is less than, equal
             * to, or greater than the second.<p>
             */
            @Override
            public int compare(IntIntOrderedMap.MapEntry o1, IntIntOrderedMap.MapEntry o2) {
                return o1.getKey() - o2.getKey();
            }
        });
        sorted.addAll(hueToIndex.entrySet());
        System.out.println(sorted);
        int idx = 16;
        for (IntIntOrderedMap.MapEntry ent : sorted) {
            System.arraycopy(Coloring.RINSED, ent.getValue(), PALETTE, idx, 8);
            idx += 8;
        }
//        Color temp = Color.WHITE.cpy();
//        float[] hsv = new float[3];
//        for (int i = 0; i < 9; i++) {
//            PALETTE[7 + i] = Color.rgba8888(temp.fromHsv(0, 0, 0.125f * i));
//        }
//        float t;
//        for (int i = 16; i < 120; i++) {
//            hsv[0] = vdc(2, i) * 360f;
//            hsv[1] = 1f - vdc(2, i) * vdc(3, i) * 1.3f;
//            t = vdc(5, i) * vdc2_scrambled(i + 70);
//            hsv[2] = 1f - t * t;
//            PALETTE[i] = Color.rgba8888(temp.fromHsv(hsv));
//        }

//        final float[] vals = {0.2f, 0.3f, 0.42f, 0.58f, 0.74f, 0.86f, 0.96f, 1f};
//        float hue, sat;
//        for (int i = 0; i < 21; i++) {
//            Color.rgba8888ToColor(temp, Coloring.CW_PALETTE[21 + i * 8]);
//            temp.toHsv(hsv);
//            hue = hsv[0];
//            sat = hsv[1];
//            for (int j = 0; j < 8; j++) {
//                PALETTE[16 + j + i * 8] = Color.rgba8888(temp.fromHsv(hue, sat + 0.018f * (35f - (j+1) * (j+1)), vals[j]));
//            }
//        }
//        for (int i = 0; i < 16; i++) {
//            PALETTE[184 + i] = i * 0x10101000 | 0xFF;
//        }
//        System.arraycopy(PALETTE, 16 + 4 * 8, PALETTE, 208, 8);
//        System.arraycopy(PALETTE, 16 + 8, PALETTE, 216, 8);
//        System.arraycopy(PALETTE, 16 + 2 * 8, PALETTE, 224, 8);
//        System.arraycopy(PALETTE, 16, PALETTE, 232, 8);
        
//        System.arraycopy(PALETTE, 7, PALETTE, 127, 113);
        StringBuilder sb = new StringBuilder((1 + 12 * 8) * 32);
        for (int i = 0; i < 32; i++) {
            for (int j = 0; j < 8; j++) {
                sb.append("0x").append(StringKit.hex(PALETTE[i << 3 | j])).append(", ");
            }
            sb.append('\n');
        }
        String sbs = sb.toString();
        System.out.println(sbs);
        //Gdx.files.local("GeneratedPalette.txt").writeString(sbs, false);
        sb.setLength(0);
//        for (int i = 7; i < 120; i++) {
//            final int p = PALETTE[i];
//            int diff = Integer.MAX_VALUE;
//            for (int j = 7; j < 120; j++) {
//                if(i == j) continue;
//                diff = Math.min(difference2(p, PALETTE[j]), diff);
//            }
//            sb.append("0x").append(StringKit.hex(p)).append(' ').append(diff).append('\n');
//        }
//        Gdx.files.local("PaletteDifferences.txt").writeString(sb.toString(), false);
        Pixmap pix = new Pixmap(256, 1, Pixmap.Format.RGBA8888);
        for (int i = 0; i < 255; i++) {
            pix.drawPixel(i, 0, PALETTE[i+1]);
        }
        pix.drawPixel(255, 0, 0);
        PixmapIO.writePNG(Gdx.files.local("RinsedHueSorted.png"), pix);
        Gdx.app.exit();
    }

    /**
     * Van der Corput sequence for a given base; s must be greater than 0.
     * @param base any prime number, with some optimizations used when base == 2
     * @param s any int greater than 0
     * @return a sub-random float between 0f inclusive and 1f exclusive
     */
    public static float vdc(int base, int s)
    {
        if(base <= 2) {
            final int leading = Integer.numberOfLeadingZeros(s);
            return (Integer.reverse(s) >>> leading) / (float) (1 << (32 - leading));
        }
        int num = s % base, den = base;
        while (den <= s) {
            num *= base;
            num += (s % (den * base)) / den;
            den *= base;
        }
        return num / (float)den;
    }
    public static float vdc2_scrambled(int index)
    {
        int s = ((++index ^ index << 1 ^ index >> 1) & 0x7fffffff), leading = Integer.numberOfLeadingZeros(s);
        return (Integer.reverse(s) >>> leading) / (float)(1 << (32 - leading));
    }

    public static int difference2(int color1, int color2)
    {
        int rmean = ((color1 >>> 24 & 0xFF) + (color2 >>> 24 & 0xFF)) >> 1;
        int b = (color1 >>> 8 & 0xFF) - (color2 >>> 8 & 0xFF);
        int g = (color1 >>> 16 & 0xFF) - (color2 >>> 16 & 0xFF);
        int r = (color1 >>> 24 & 0xFF) - (color2 >>> 24 & 0xFF);
        return (((512+rmean)*r*r)>>8) + 4*g*g + (((767-rmean)*b*b)>>8);
    }

    // goes in squidlib, needs squidlib display/SColor
    /*
            final float pi = 3.14159265358979323846f, offset = 0.1f,
                pi1 = pi * 0.25f + offset, pi2 = pi * 0.75f + offset, pi3 = pi * -0.75f + offset, pi4 = pi * -0.25f + offset, ph = 0.7f,//0.6180339887498948482f,
                dark = 0.26f, mid = 0.3f, light = 0.22f;
//        dark = 0.11f, mid = 0.12f, light = 0.1f;
//        System.out.printf("0x%08X, ", NumberTools.floatToReversedIntBits(SColor.floatGetYCbCr(0f, 0f, 0f, 0f)));
        System.out.println("0x00000000, ");
        System.out.printf("0x%08X, ", 1|NumberTools.floatToReversedIntBits(SColor.floatGetYCbCr(0.15f, 0f, 0f, 1f)));
        System.out.printf("0x%08X, ", 1|NumberTools.floatToReversedIntBits(SColor.floatGetYCbCr(0.4f, 0f, 0f, 1f)));
        System.out.printf("0x%08X, ", 1|NumberTools.floatToReversedIntBits(SColor.floatGetYCbCr(0.65f, 0f, 0f, 1f)));
        System.out.printf("0x%08X, ", 1|NumberTools.floatToReversedIntBits(SColor.floatGetYCbCr(0.9f, 0f, 0f, 1f)));

        System.out.printf("0x%08X, ", 1|NumberTools.floatToReversedIntBits(SColor.floatGetYCbCr(0.2f, NumberTools.cos(pi1+ph) * dark, NumberTools.sin(pi1+ph) * dark, 1f)));
        System.out.printf("0x%08X, ", 1|NumberTools.floatToReversedIntBits(SColor.floatGetYCbCr(0.25f, NumberTools.cos(pi2+ph) * dark, NumberTools.sin(pi2+ph) * dark, 1f)));
        System.out.printf("0x%08X, ", 1|NumberTools.floatToReversedIntBits(SColor.floatGetYCbCr(0.3f, NumberTools.cos(pi3+ph) * dark, NumberTools.sin(pi3+ph) * dark, 1f)));
        System.out.printf("0x%08X, ", 1|NumberTools.floatToReversedIntBits(SColor.floatGetYCbCr(0.35f, NumberTools.cos(pi4+ph) * dark, NumberTools.sin(pi4+ph) * dark, 1f)));
        System.out.println();
        System.out.printf("0x%08X, ", 1|NumberTools.floatToReversedIntBits(SColor.floatGetYCbCr(0.45f, NumberTools.cos(pi1) * mid, NumberTools.sin(pi1) * mid, 1f)));
        System.out.printf("0x%08X, ", 1|NumberTools.floatToReversedIntBits(SColor.floatGetYCbCr(0.5f, NumberTools.cos(pi2) * mid, NumberTools.sin(pi2) * mid, 1f)));
        System.out.printf("0x%08X, ", 1|NumberTools.floatToReversedIntBits(SColor.floatGetYCbCr(0.55f, NumberTools.cos(pi3) * mid, NumberTools.sin(pi3) * mid, 1f)));
        System.out.printf("0x%08X, ", 1|NumberTools.floatToReversedIntBits(SColor.floatGetYCbCr(0.6f, NumberTools.cos(pi4) * mid, NumberTools.sin(pi4) * mid, 1f)));

        System.out.printf("0x%08X, ", 1|NumberTools.floatToReversedIntBits(SColor.floatGetYCbCr(0.7f, NumberTools.cos(pi1-ph) * light, NumberTools.sin(pi1-ph) * light, 1f)));
        System.out.printf("0x%08X, ", 1|NumberTools.floatToReversedIntBits(SColor.floatGetYCbCr(0.75f, NumberTools.cos(pi2-ph) * light, NumberTools.sin(pi2-ph) * light, 1f)));
        System.out.printf("0x%08X, ", 1|NumberTools.floatToReversedIntBits(SColor.floatGetYCbCr(0.8f, NumberTools.cos(pi3-ph) * light, NumberTools.sin(pi3-ph) * light, 1f)));
        System.out.printf("0x%08X, ", 1|NumberTools.floatToReversedIntBits(SColor.floatGetYCbCr(0.85f, NumberTools.cos(pi4-ph) * light, NumberTools.sin(pi4-ph) * light, 1f)));

     */
}
