import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.MathUtils;
import squidpony.StringKit;
import squidpony.squidmath.NumberTools;
import warpwriter.PNG8;
import warpwriter.PaletteReducer;

import java.io.IOException;

/**
 * Created by Tommy Ettinger on 1/21/2018.
 */
public class OverkillPaletteGenerator extends ApplicationAdapter {

    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("EXTREME Palette Stuff");
        config.setWindowedMode(1000, 600);
        config.setIdleFPS(10);
        config.setResizable(false);
        new Lwjgl3Application(new OverkillPaletteGenerator(), config);
    }

    private static float hue(int rgba) {
        final float r = (rgba >>> 24 & 255) * 0.003921569f, g = (rgba >>> 16 & 255) * 0.003921569f,
                b = (rgba >>> 8 & 255) * 0.003921569f;//, a = (e >>> 24 & 254) / 254f;
        final float min = Math.min(Math.min(r, g), b);   //Min. value of RGB
        final float max = Math.max(Math.max(r, g), b);   //Max value of RGB
        final float delta = max - min;                           //Delta RGB value

        if (delta < 0.0001f)                     //This is a gray, no chroma...
        {
            return 0f;
        } else                                    //Chromatic data...
        {
            final float rDelta = (((max - r) / 6f) + (delta * 0.5f)) / delta;
            final float gDelta = (((max - g) / 6f) + (delta * 0.5f)) / delta;
            final float bDelta = (((max - b) / 6f) + (delta * 0.5f)) / delta;

            if (r == max) return (1f + bDelta - gDelta) % 1f;
            else if (g == max) return ((4f / 3f) + rDelta - bDelta) % 1f;
            else return ((5f / 3f) + gDelta - rDelta) % 1f;
        }
    }

    public static int difference(final int color1, final int color2) {
        // if one color is transparent and the other isn't, then this is max-different
        if (((color1 ^ color2) & 0x80) == 0x80) return 0x70000000;
        final int r1 = (color1 >>> 24), g1 = (color1 >>> 16 & 0xFF), b1 = (color1 >>> 8 & 0xFF),
                r2 = (color2 >>> 24), g2 = (color2 >>> 16 & 0xFF), b2 = (color2 >>> 8 & 0xFF),
                rmean = r1 + r2,
                r = r1 - r2,
                g = g1 - g2,
                b = b1 - b2,
                y = Math.max(r1, Math.max(g1, b1)) - Math.max(r2, Math.max(g2, b2));
//        return (((512 + rmean) * r * r) >> 8) + g * g + (((767 - rmean) * b * b) >> 8);
//        return (((0x580 + rmean) * r * r) >> 7) + g * g * 12 + (((0x5FF - rmean) * b * b) >> 8) + y * y * 8;
        return (((1024 + rmean) * r * r) >> 7) + g * g * 12 + (((1534 - rmean) * b * b) >> 8) + y * y * 14;
    }

    public final double fastGaussian() {
        long a = (state = (state << 29 | state >>> 35) * 0xAC564B05L) * 0x818102004182A025L,
                b = (state = (state << 29 | state >>> 35) * 0xAC564B05L) * 0x818102004182A025L;
        a = (a & 0x0003FF003FF003FFL) + ((a & 0x0FFC00FFC00FFC00L) >>> 10);
        b = (b & 0x0003FF003FF003FFL) + ((b & 0x0FFC00FFC00FFC00L) >>> 10);
        a = (a & 0x000000007FF007FFL) + ((a & 0x0007FF0000000000L) >>> 40);
        b = (b & 0x000000007FF007FFL) + ((b & 0x0007FF0000000000L) >>> 40);
        return ((((a & 0x0000000000000FFFL) + ((a & 0x000000007FF00000L) >>> 20))
                - ((b & 0x0000000000000FFFL) + ((b & 0x000000007FF00000L) >>> 20))) * 0x1p-10);
    }
    
    private long state = 64L;
    
    private double nextDouble()
    {
        return ((state = (state << 29 | state >>> 35) * 0xAC564B05L) * 0x818102004182A025L & 0x1FFFFFFFFFFFFFL) * 0x1p-53;
    }
    private double curvedDouble()
    {
        return 0.1 * (nextDouble() + nextDouble() + nextDouble()
                + nextDouble() + nextDouble() + nextDouble())
                + 0.2 * ((1.0 - nextDouble() * nextDouble()) + (1.0 - nextDouble() * nextDouble()));

    }
    
    public void create() {
        int[] PALETTE = new int[64];
//        double[] color = new double[3];
        double luma, warm, mild, hue;
        int ctr = 1;
        for (int i = 1; i < 64;) {
            if ((i & 7) == 7) {
                int ch = i << 2 | i >>> 3;
                PALETTE[i++] = ch << 24 | ch << 16 | ch << 8 | 0xFF;
                ctr++;
            } else {
                int r, g, b;
                do {
//                hue = i * (Math.PI * 1.6180339887498949);
                    hue = (ctr) * (Math.PI * 2.0 / 53.0);
                    mild = (NumberTools.sin(hue) * (NumberTools.cos(ctr * 1.963) * 0.45 + 0.8));
                    warm = (NumberTools.cos(hue) * (NumberTools.sin(ctr * 1.611) * 0.45 + 0.8));
                    luma = curvedDouble();
                    ctr++;
//                color[0] = i * (360.0 * 1.6180339887498949);
//                color[1] = Math.sqrt(1.0 - nextDouble() * nextDouble()) * 100.0;
//                color[2] = curvedDouble() * 100.0;
//                color[2] = i * (94.0 / 255.0) + 3.0;
//                System.out.println(StringKit.join(", ", color) + "  -> " + StringKit.join(", ", HSLUVColorConverter.hsluvToRgb(color)));                 
                  r = (int) ((luma + warm * 0.5) * 255);
                  g = (int) ((luma + mild * 0.5) * 255);
                  b = (int) ((luma - (warm + mild) * 0.25) * 255);
                }while (r < 0 || r > 255 || g < 0 || g > 255 || b < 0 || b > 255);
                PALETTE[i++] = r << 24 |
                        g << 16 |
                        b << 8 | 0xFF;
//                PALETTE[i++] = (int) (MathUtils.clamp(color[0], 0.0, 1.0) * 255.5) << 24 |
//                        (int) (MathUtils.clamp(color[1], 0.0, 1.0) * 255.5) << 16 |
//                        (int) (MathUtils.clamp(color[2], 0.0, 1.0) * 255.5) << 8 | 0xFF;
            }
        }
//        IntVLA base = new IntVLA(0x912);
////        base.addAll(Coloring.AURORA, 1, 255);
////        base.addAll(0x010101FF, 0x2D2D2DFF, 0x555555FF, 0x7B7B7BFF,
////                0x9F9F9FFF, 0xC1C1C1FF, 0xE1E1E1FF, 0xFFFFFFFF);
//        
//        int[] grayscale = {0x010101FF, 0x171717FF, 0x2D2D2DFF, 0x555555FF, 0x686868FF, 0x7B7B7BFF, 0x8D8D8DFF,
//                0x9F9F9FFF, 0xB0B0B0FF, 0xC1C1C1FF, 0xD1D1D1FF, 0xE1E1E1FF, 0xF0F0F0FF, 0xFFFFFFFF};
////        int[] grayscale = {0x010101FF, 0x212121FF, 0x414141FF, 0x616161FF,
////                0x818181FF, 0xA1A1A1FF, 0xC1C1C1FF, 0xE1E1E1FF, 0xFFFFFFFF};
////        int[] grayscale = {0x010101FF, 0x414141FF,
////                0x818181FF, 0xC1C1C1FF, 0xFFFFFFFF};
////        int[] grayscale = {0x010101FF, 0x2D2D2DFF, 0x555555FF, 0x7B7B7BFF,
////                0x9F9F9FFF, 0xC1C1C1FF, 0xE1E1E1FF, 0xFFFFFFFF};
//        base.addAll(grayscale);
////        DiverRNG rng = new DiverRNG("sixty-four");
////        MiniMover64RNG rng = new MiniMover64RNG(64);
//        for (int i = 1; i <= 2240; i++) {
////            double luma = Math.pow(i * 0x1.c7p-11, 0.875), // 0 to 1, more often near 1 than near 0
//            double luma = i / 2240.0;//, mild = 0.0, warm = 0.0;// 0.0 to 1.0
//            luma = (Math.sqrt(luma) + luma) * 128.0;
//            //0xC13FA9A902A6328FL * i
//            //0x91E10DA5C79E7B1DL * i
////                    mild = ((DiverRNG.determineDouble(i) + DiverRNG.randomizeDouble(-i) - DiverRNG.randomizeDouble(123456789L - i) - DiverRNG.determineDouble(987654321L + i) + 0.5 - DiverRNG.randomizeDouble(123456789L + i)) * 0.4), // -1 to 1, curved random
////                    warm = ((DiverRNG.determineDouble(-i) + DiverRNG.randomizeDouble((i^12345L)*i) - DiverRNG.randomizeDouble((i^99999L)*i) - DiverRNG.determineDouble((987654321L - i)*i) + 0.5  - DiverRNG.randomizeDouble((123456789L - i)*i)) * 0.4); // -1 to 1, curved random
////                    mild = ((DiverRNG.determineDouble(i) + DiverRNG.randomizeDouble(-i) + DiverRNG.randomizeDouble(987654321L - i) - DiverRNG.randomizeDouble(123456789L - i) - DiverRNG.randomizeDouble(987654321L + i) - DiverRNG.determineDouble(1234567890L + i)) / 3.0), // -1 to 1, curved random
////                    warm = ((DiverRNG.determineDouble(-i) + DiverRNG.randomizeDouble((i^12345L)*i) + DiverRNG.randomizeDouble((i^54321L)*i) - DiverRNG.randomizeDouble((i^99999L)*i) - DiverRNG.randomizeDouble((987654321L - i)*i) - DiverRNG.determineDouble((1234567890L - i)*i)) / 3.0); // -1 to 1, curved random
//
////            final double v1 = fastGaussian(rng), v2 = fastGaussian(rng), v3 = fastGaussian(rng);
////            double mag = v1 * v1 + v2 * v2 + v3 * v3 + 1.0 / (1.0 - ((rng.nextLong() & 0x1FFFFFFFFFFFFFL) * 0x1p-53) * ((rng.nextLong() & 0x1FFFFFFFFFFFFFL) * 0x1p-53) * ((rng.nextLong() & 0x1FFFFFFFFFFFFFL) * 0x1p-53)) - 1.0;
////            double mag = v1 * v1 + v2 * v2 + v3 * v3 + 1.0 / (1.0 - ((rng.nextLong() & 0x1FFFFFFFFFFFFFL) * 0x1p-53) * ((rng.nextLong() & 0x1FFFFFFFFFFFFFL) * 0x1p-53)) - 0.5;
////            double mag = v1 * v1 + v2 * v2 + v3 * v3 - 2.0 * Math.log(((rng.nextLong() & 0x1FFFFFFFFFFFFFL) * 0x1p-53));
////            final long t = rng.nextLong(), s = rng.nextLong(), angle = t >>> 48;
////            float mag = (((t & 0xFFFFFFL)) * 0x0.7p-24f + (0x1.9p0f - ((s & 0xFFFFFFL) * 0x1.4p-24f) * ((s >>> 40) * 0x1.4p-24f))) * 0.555555f;
////            mild = MathUtils.sin(angle) * mag;
////            warm = MathUtils.cos(angle) * mag;
////            double mag = ((t & 0xFFFFFFL) + (t >>> 40) + (s & 0xFFFFFFL) + (s >>> 40)) * 0x1p-26;
////            if (mag != 0.0) {
////                mag = 1.0 / Math.sqrt(mag);
////                mild = v1 * mag;
////                warm = v2 * mag;
////            }
//
////            double mild = (nextDouble() + nextDouble() + nextDouble() + nextDouble() + nextDouble() + nextDouble()
////                    - nextDouble() - nextDouble() - nextDouble() - nextDouble() - nextDouble() - nextDouble()) * 0.17 % 1.0, // -1 to 1, curved random
////                    warm = (nextDouble() + nextDouble() + nextDouble() + nextDouble() + nextDouble() + nextDouble()
////                            - nextDouble() - nextDouble()- nextDouble() - nextDouble() - nextDouble() - nextDouble()) * 0.17 % 1.0; // -1 to 1, curved random
//            double co = (nextDouble() + nextDouble() + nextDouble() + nextDouble() * nextDouble() + nextDouble() * nextDouble()
//                    - nextDouble() - nextDouble() - nextDouble() - nextDouble() * nextDouble() - nextDouble() * nextDouble()) * 32.0 % 128.0, // -256.0 to 256.0, curved random
//                    cg = (nextDouble() + nextDouble() + nextDouble() + nextDouble() * nextDouble() + nextDouble() * nextDouble()
//                            - nextDouble() - nextDouble()- nextDouble() - nextDouble() * nextDouble() - nextDouble() * nextDouble()) * 32.0 % 128.0; // -256.0 to 256.0, curved random
////            mild = Math.signum(mild) * Math.pow(Math.abs(mild), 1.05);
////            warm = Math.signum(warm) * Math.pow(Math.abs(warm), 0.8);
////            if (mild > 0 && warm < 0) warm += mild * 1.666;
////            else if (mild < -0.6) warm *= 0.4 - mild;
//            final double t = luma - cg;
//
////            int g = (int) ((luma + mild * 0.5) * 255);
////            int b = (int) ((luma - (warm + mild) * 0.25) * 255);
////            int r = (int) ((luma + warm * 0.5) * 255);
//            base.add(
//                    (int) MathUtils.clamp(t + co, 0.0, 255.0) << 24 |
//                            (int) MathUtils.clamp(luma + cg, 0.0, 255.0) << 16 |
//                            (int) MathUtils.clamp(t - co, 0.0, 255.0) << 8 | 0xFF);
//        }
//
////        base.addAll(Coloring.AURORA);
////        base.addAll(Colorizer.FlesurrectBonusPalette);
////        base.addAll(Coloring.VGA256);
////        base.addAll(Coloring.RINSED);
//        
////        for (int r = 0, rr = 0; r < 16; r++, rr += 0x11000000) {
////            for (int g = 0, gg = 0; g < 16; g++, gg += 0x110000) {
////                for (int b = 0, bb = 0; b < 16; b++, bb += 0x1100) {
////                    base.add(rr | gg | bb | 0xFF);
////                }
////            }
////        }
//        while (base.size >= 256) {
//            int t, ca = 0, cb = 1, d = 0xFFFFFF;
//            for (int i = 0; i < base.size; i++) {
//                int a = base.get(i);
//                for (int j = i + 1; j < base.size; j++) {
//                    int b = base.get(j);
//                    if ((t = difference(a, b)) < d) {
//                        d = t;
//                        ca = i;
//                        cb = j;
//                    }
//                }
//            }
//            d = ca;
//            ca = base.get(ca);
//            t = base.get(cb);
//            int ra = (ca >>> 24), ga = (ca >>> 16 & 0xFF), ba = (ca >>> 8 & 0xFF),
//                    rb = (t >>> 24), gb = (t >>> 16 & 0xFF), bb = (t >>> 8 & 0xFF);
////                    maxa = Math.max(ra, Math.max(ga, ba)), mina = Math.min(ra, Math.min(ga, ba)),
////                    maxb = Math.max(rb, Math.max(gb, bb)), minb = Math.min(rb, Math.min(gb, bb));
////            if (maxa - mina > 100)
////                base.set(cb, ca);
////            else if (maxb - minb > 100)
////                base.set(cb, t);
////            else
//                base.set(cb,
//                        (ra + rb + 1 << 23 & 0xFF000000)
//                                | (ga + gb + 1 << 15 & 0xFF0000)
//                                | (ba + bb + 1 << 7 & 0xFF00)
//                                | 0xFF);
//            base.removeIndex(d);
//        }
//        base.insert(0, 0);
//        System.arraycopy(grayscale, 0, base.items, 1, grayscale.length);
//        PALETTE = base.toArray();
//        
//        //// used for Uniform216 and SemiUniform256
////        PALETTE = new int[256];
////        int idx = 1;
////        for (int r = 0; r < 6; r++) {
////            for (int g = 0; g < 6; g++) {
////                for (int b = 0; b < 6; b++) {
////                    PALETTE[idx++] = r * 51 << 24 | g * 51 << 16 | b * 51 << 8 | 0xFF;
////                }
////            }
////        }
//////        for (int r = 0; r < 5; r++) {
//////            for (int g = 0; g < 5; g++) {
//////                for (int b = 0; b < 5; b++) {
//////                    PALETTE[idx++] = r * 60 + (1 << r) - 1 << 24 | g * 60 + (1 << g) - 1 << 16 | b * 60 + (1 << b) - 1 << 8 | 0xFF;
//////                }
//////            }
//////        }
////        IntSet is = new IntSet(256);
////        RNG rng = new RNG(new MiniMover64RNG(123456789));
////        while (idx < 256)
////        {
////            int pt = rng.next(9);
////            if(is.add(pt))
////            {
////                int r = pt & 7, g = (pt >>> 3) & 7, b = pt >>> 6;
//////                int r = pt % 5, g = (pt / 5) % 5, b = pt / 25;
//////                PALETTE[idx++] = r * 51 + 25 << 24 | g * 51 + 25 << 16 | b * 51 + 25 << 8 | 0xFF;
////                PALETTE[idx++] = r * 32 + 15 << 24 | g * 32 + 15 << 16 | b * 32 + 15 << 8 | 0xFF;
////            }
////        }
//        
//
        StringBuilder sb = new StringBuilder((1 + 12 * 8) * (PALETTE.length >>> 3));
        for (int i = 0; i < (PALETTE.length >>> 3); i++) {
            for (int j = 0; j < 8; j++) {
                sb.append("0x").append(StringKit.hex(PALETTE[i << 3 | j]).toUpperCase()).append(", ");
            }
            sb.append('\n');
        }
        String sbs = sb.toString();
        System.out.println(sbs);
        //Gdx.files.local("GeneratedPalette.txt").writeString(sbs, false);
        sb.setLength(0);

        Pixmap pix = new Pixmap(256, 1, Pixmap.Format.RGBA8888);
        for (int i = 0; i < PALETTE.length - 1; i++) {
            pix.drawPixel(i, 0, PALETTE[i + 1]);
        }
        //pix.drawPixel(255, 0, 0);
        PNG8 png8 = new PNG8();
        png8.palette = new PaletteReducer(PALETTE);
        try {
            png8.writePrecisely(Gdx.files.local("Curveball"+PALETTE.length+".png"), pix, false);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Pixmap p2 = new Pixmap(1024, 32, Pixmap.Format.RGBA8888);
        for (int r = 0; r < 32; r++) {
            for (int b = 0; b < 32; b++) {
                for (int g = 0; g < 32; g++) {
                    p2.drawPixel(r << 5 | b, g, PALETTE[png8.palette.paletteMapping[
                            ((r << 10) & 0x7C00)
                                    | ((g << 5) & 0x3E0)
                                    | b] & 0xFF]);
                }
            }
        }
        try {
            png8.writePrecisely(Gdx.files.local("Curveball"+PALETTE.length+"_GLSL.png"), p2, false);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int[][] CURVEBALL_BONUS_RAMP_VALUES = new int[256][4];
        for (int i = 1; i < 64; i++) {
            int color = CURVEBALL_BONUS_RAMP_VALUES[i | 128][2] = CURVEBALL_BONUS_RAMP_VALUES[i][2] =
                    PALETTE[i],
                    r = (color >>> 24),
                    g = (color >>> 16 & 0xFF),
                    b = (color >>> 8 & 0xFF);
            CURVEBALL_BONUS_RAMP_VALUES[i | 64][1] = CURVEBALL_BONUS_RAMP_VALUES[i | 64][2] =
                    CURVEBALL_BONUS_RAMP_VALUES[i | 64][3] = color;
            CURVEBALL_BONUS_RAMP_VALUES[i | 192][0] = CURVEBALL_BONUS_RAMP_VALUES[i | 192][2] = color;
            int co = r - b, t = b + (co >> 1), cg = g - t, y = t + (cg >> 1),
                    yBright = y * 21 >> 4, yDim = y * 11 >> 4, yDark = y * 6 >> 4, chromO, chromG;
            chromO = (co * 3) >> 2;
            chromG = (cg * 3) >> 2;
            t = yDim - (chromG >> 1);
            g = chromG + t;
            b = t - (chromO >> 1);
            r = b + chromO;
            CURVEBALL_BONUS_RAMP_VALUES[i | 192][1] = CURVEBALL_BONUS_RAMP_VALUES[i | 128][1] =
                    CURVEBALL_BONUS_RAMP_VALUES[i | 64][0] = CURVEBALL_BONUS_RAMP_VALUES[i][1] =
                            MathUtils.clamp(r, 0, 255) << 24 |
                                    MathUtils.clamp(g, 0, 255) << 16 |
                                    MathUtils.clamp(b, 0, 255) << 8 | 0xFF;
            chromO = (co * 3) >> 2;
            chromG = (cg * (256 - yBright) * 3) >> 9;
            t = yBright - (chromG >> 1);
            g = chromG + t;
            b = t - (chromO >> 1);
            r = b + chromO;
            CURVEBALL_BONUS_RAMP_VALUES[i | 192][3] = CURVEBALL_BONUS_RAMP_VALUES[i | 128][3] =
                    CURVEBALL_BONUS_RAMP_VALUES[i][3] =
                            MathUtils.clamp(r, 0, 255) << 24 |
                                    MathUtils.clamp(g, 0, 255) << 16 |
                                    MathUtils.clamp(b, 0, 255) << 8 | 0xFF;
            chromO = (co * 13) >> 4;
            chromG = (cg * (256 - yDark) * 13) >> 11;
            t = yDark - (chromG >> 1);
            g = chromG + t;
            b = t - (chromO >> 1);
            r = b + chromO;
            CURVEBALL_BONUS_RAMP_VALUES[i | 128][0] = CURVEBALL_BONUS_RAMP_VALUES[i][0] =
                    MathUtils.clamp(r, 0, 255) << 24 |
                            MathUtils.clamp(g, 0, 255) << 16 |
                            MathUtils.clamp(b, 0, 255) << 8 | 0xFF;
        }
        sb.setLength(0);
        sb.ensureCapacity(2800);
        sb.append("private static final int[][] CURVEBALL_BONUS_RAMP_VALUES = new int[][] {\n");
        for (int i = 0; i < 256; i++) {
            sb.append("{ 0x");
            StringKit.appendHex(sb, CURVEBALL_BONUS_RAMP_VALUES[i][0]);
            StringKit.appendHex(sb.append(", 0x"), CURVEBALL_BONUS_RAMP_VALUES[i][1]);
            StringKit.appendHex(sb.append(", 0x"), CURVEBALL_BONUS_RAMP_VALUES[i][2]);
            StringKit.appendHex(sb.append(", 0x"), CURVEBALL_BONUS_RAMP_VALUES[i][3]);
            sb.append(" },\n");

        }
        System.out.println(sb.append("};"));
        PALETTE = new int[256];
        for (int i = 0; i < 64; i++) {
            System.arraycopy(CURVEBALL_BONUS_RAMP_VALUES[i], 0, PALETTE, i << 2, 4);
        }
        sb.setLength(0);
        sb.ensureCapacity((1 + 12 * 8) * (PALETTE.length >>> 3));
        for (int i = 0; i < (PALETTE.length >>> 3); i++) {
            for (int j = 0; j < 8; j++) {
                sb.append("0x").append(StringKit.hex(PALETTE[i << 3 | j]).toUpperCase()).append(", ");
            }
            sb.append('\n');
        }
        System.out.println(sb.toString());
        sb.setLength(0);

        pix = new Pixmap(256, 1, Pixmap.Format.RGBA8888);
        for (int i = 0; i < PALETTE.length - 1; i++) {
            pix.drawPixel(i, 0, PALETTE[i + 1]);
        }
        //pix.drawPixel(255, 0, 0);
        png8.palette = new PaletteReducer(PALETTE);
        try {
            png8.writePrecisely(Gdx.files.local("Curveball"+PALETTE.length+".png"), pix, false);
        } catch (IOException e) {
            e.printStackTrace();
        }

        p2 = new Pixmap(1024, 32, Pixmap.Format.RGBA8888);
        for (int r = 0; r < 32; r++) {
            for (int b = 0; b < 32; b++) {
                for (int g = 0; g < 32; g++) {
                    p2.drawPixel(r << 5 | b, g, PALETTE[png8.palette.paletteMapping[
                            ((r << 10) & 0x7C00)
                                    | ((g << 5) & 0x3E0)
                                    | b] & 0xFF]);
                }
            }
        }
        try {
            png8.writePrecisely(Gdx.files.local("Curveball"+PALETTE.length+"_GLSL.png"), p2, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }

    /**
     * Van der Corput sequence for a given base; s must be greater than 0.
     *
     * @param base any prime number, with some optimizations used when base == 2
     * @param s    any int greater than 0
     * @return a sub-random float between 0f inclusive and 1f exclusive
     */
    public static float vdc(int base, int s) {
        if (base <= 2) {
            final int leading = Integer.numberOfLeadingZeros(s);
            return (Integer.reverse(s) >>> leading) / (float) (1 << (32 - leading));
        }
        int num = s % base, den = base;
        while (den <= s) {
            num *= base;
            num += (s % (den * base)) / den;
            den *= base;
        }
        return num / (float) den;
    }

    public static float vdc2_scrambled(int index) {
        int s = ((++index ^ index << 1 ^ index >> 1) & 0x7fffffff), leading = Integer.numberOfLeadingZeros(s);
        return (Integer.reverse(s) >>> leading) / (float) (1 << (32 - leading));
    }

    public static int difference2(int color1, int color2) {
        int rmean = ((color1 >>> 24 & 0xFF) + (color2 >>> 24 & 0xFF)) >> 1;
        int b = (color1 >>> 8 & 0xFF) - (color2 >>> 8 & 0xFF);
        int g = (color1 >>> 16 & 0xFF) - (color2 >>> 16 & 0xFF);
        int r = (color1 >>> 24 & 0xFF) - (color2 >>> 24 & 0xFF);
        return (((512 + rmean) * r * r) >> 8) + 4 * g * g + (((767 - rmean) * b * b) >> 8);
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
