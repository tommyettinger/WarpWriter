import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Pixmap;
import org.cie.CIELABConverter;
import squidpony.StringKit;
import squidpony.squidmath.IntVLA;
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
                rmean = (r1 + r2) * 53 >> 5,
                r = r1 - r2,
                g = g1 - g2,
                b = b1 - b2,
                y = Math.max(r1, Math.max(g1, b1)) - Math.max(r2, Math.max(g2, b2));
//        return (((512 + rmean) * r * r) >> 8) + g * g + (((767 - rmean) * b * b) >> 8);
//        return (((0x580 + rmean) * r * r) >> 7) + g * g * 12 + (((0x5FF - rmean) * b * b) >> 8) + y * y * 8;
        return (((1024 + rmean) * r * r) >> 7) + g * g * 13 + (((1534 - rmean) * b * b) >> 8) + y * y * 12;
//        return (((1024 + rmean) * r * r) >> 7) + g * g * 12 + (((1534 - rmean) * b * b) >> 8) + y * y * 14;
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
    
    private long state = 9005L;
    
    private double nextDouble()
    {
        return ((state = (state << 29 | state >>> 35) * 0xAC564B05L) * 0x818102004182A025L & 0x1FFFFFFFFFFFFFL) * 0x1p-53;
    }
    private double curvedDouble()
    {
        // averages about 0.6
//        return 0.1 * (nextDouble() + nextDouble() + nextDouble()
//                + nextDouble() + nextDouble() + nextDouble())
//                + 0.2 * ((1.0 - nextDouble() * nextDouble()) + (1.0 - nextDouble() * nextDouble()));
        // averages about 0.685
        return 0.25 * (0.5 * (nextDouble() + nextDouble() + nextDouble() + nextDouble()) +
                (3.0 - nextDouble() * nextDouble() - nextDouble() * nextDouble() - nextDouble() * nextDouble()));

    }
    private static double difference(double y1, double w1, double m1, double y2, double w2, double m2) {
        return (y1 - y2) * (y1 - y2) + ((w1 - w2) * (w1 - w2) + (m1 - m2) * (m1 - m2)) * 0.1625;
    }
    
    public void create() {
//        int[] PALETTE = {0x00000000, 0xD73700FF, 0xAF92EBFF, 0x00E4DAFF, 0xD78200FF, 0x826B86FF, 0x00BDD4FF, 0xE7C33AFF,
//                0xAE8DA4FF, 0x0091DDFF, 0x9FA620FF, 0xFFA2D5FF, 0x00BCFFFF, 0x5A7D21FF, 0xFF6693FF, 0x00E0FFFF,
//                0x879E80FF, 0xD4324AFF, 0x00A7FFFF, 0xAECEB7FF, 0xFF4A34FF, 0x0071DEFF, 0x0DB28AFF, 0xFF8400FF,
//                0x988EEBFF, 0x008A78FF, 0xEE8000FF, 0xD5BFE2FF, 0x00B6C0FF, 0x936C00FF, 0xAF94ABFF, 0x00ECFFFF,
//                0xA69E13FF, 0xB15088FF, 0x00C2FFFF, 0xBAD06AFF, 0xFF639DFF, 0x008DFFFF, 0x8CA17EFF, 0xFF8FA7FF,
//                0x00A9FFFF, 0x607863FF, 0xFF4349FF, 0x6BC8FFFF, 0x39B082FF, 0xEE0000FF, 0x8691F1FF, 0x00E4BFFF,
//                0xFE7700FF, 0x7B6C8FFF, 0x00B4B1FF, 0xFFB93CFF, 0xAE98AFFF, 0x0091BAFF, 0xAE970AFF, 0xFFA9E6FF,
//                0x00C7FFFF, 0x6D7908FF, 0xF760A4FF, 0x00E8FFFF, 0x96A67EFF, 0xD42E63FF, 0x00A6FFFF, 0xB7CCB4FF,
//        };//new int[64];

//        // used for NonUniform256
//        int[] PALETTE = new int[256];
//        PALETTE[1] = 0x1F1F1FFF;
//        PALETTE[2] = 0x3F3F3FFF;
//        PALETTE[3] = 0x5F5F5FFF;
//        PALETTE[4] = 0x7F7F7FFF;
//        PALETTE[5] = 0x9F9F9FFF;
//        PALETTE[6] = 0xBFBFBFFF;
//        PALETTE[7] = 0xDFDFDFFF;
//        int idx = 8;
//        for (int rr = 0; rr < 3; rr++) {
//            for (int gg = 0; gg < 3; gg++) {
//                for (int bb = 0; bb < 3; bb++) {
//                    PALETTE[idx++] = rr * 127 + (rr >> 1) << 24 |
//                            gg * 127 + (gg >> 1) << 16 |
//                            bb * 127 + (bb >> 1) << 8 | 0xFF;
//                }
//            }
//        }
//        for (int n = 21; idx < 64; n++) {
//            PALETTE[idx++] = (int)(Math.pow(vdc(5, n), 0.625) * 255) << 24 |
//                    (int)(Math.pow(vdc(7, n), 0.625) * 255) << 16 |
//                    (int)(Math.pow(vdc(2, n), 0.625) * 255) << 8 | 0xFF;
//        }

//        int[] PALETTE = 
//                new int[64];
//        {
//            int i = 1;
//            for (int j = 0; j < 9; j++) {
//                int v = 256 - (8-j) * (8-j) * 4 + (-j >> 31);
//                PALETTE[i++] = v << 24 | v << 16 | v << 8 | 0xFF;
//            }
//            int[] rgb = {0, 0, 0};
//            for (int sel = 0; sel < 3; sel++) {
//                int o1 = (sel + 1) % 3, o2 = (sel + 2) % 3;
//                for (int j = 0; j < 9; j++) {
//                    if((j & 1) == 0)
//                    {
//                        rgb[sel] = MathUtils.clamp(j * 60 + 50, 0, 255);
//                        rgb[o1] = rgb[o2] = MathUtils.clamp(-100 + j * 40, 0, 240);
//                    }
//                    else
//                    {
//                        rgb[sel] = MathUtils.clamp(j * 44 + 56, 0, 255);
//                        rgb[o1] = rgb[o2] = MathUtils.clamp(-16 + j * 26, 0, 220);
//                    }
//                    PALETTE[i++] = rgb[0] << 24 | rgb[1] << 16 | rgb[2] << 8 | 0xFF;
//                }
//                for (int j = 0; j < 9; j++) {
//                    if((j & 1) == 1)
//                    {
//                        rgb[o1] = rgb[o2] = MathUtils.clamp(j * 56 + 50, 0, 255);
//                        rgb[sel] = MathUtils.clamp(-100 + j * 42, 0, 240);
//                    }
//                    else
//                    {
//                        rgb[o1] = rgb[o2] = MathUtils.clamp(j * 38 + 56, 0, 255);
//                        rgb[sel] = MathUtils.clamp(-16 + j * 32, 0, 220);
//                    }
//                    PALETTE[i++] = rgb[0] << 24 | rgb[1] << 16 | rgb[2] << 8 | 0xFF;
//                }
//            }
//            
////            int i = 1, r, g, b;
////            for (int cw : new int[] {0, -1, 1}) {
////                for (int cm : new int[]{0, -1, 1}) {
////                    for (int lu = 0; lu <= 6; lu++) {
////                        double luma, warm, mild;
////                        if ((cm | cw) == 0)
////                            luma = lu / 6.0;
////                        else
////                            luma = 0.9 - Math.pow((6.0 - lu) / 6.0, 1.125) * 0.8;
////                        if ((cm & cw) == 0) {
////                            if (cw == 1) {
////                                warm = 0.7;
////                                mild = 0.0625;
////                            } else if(cm == -1){
////                                warm = 0.25;
////                                mild = -0.65;
////                            }else {
////                                warm = cw * 0.5;
////                                mild = cm * 0.5;
////                            }
////                        } else if(cw == 1 && cm == 1) {
////                            warm = 1.0;
////                            mild = 0.75 + luma * 0.3;
////                            luma = Math.pow(luma, 0.6);
////                        }
////                        else {
////                            warm = cw;
////                            mild = cm;
////                        }
////                        if((lu & 1) == 1)
////                        {
////                            warm *= 0.7;
////                            mild *= 0.7;
////                        }
////                        r = MathUtils.clamp((int) ((luma + warm * 0.625 - mild * 0.5) * 255.5), 0, 255);
////                        g = MathUtils.clamp((int) ((luma + mild * 0.5 - warm * 0.375) * 255.5), 0, 255);
////                        b = MathUtils.clamp((int) ((luma - warm * 0.375 - mild * 0.5) * 255.5), 0, 255);
////                        PALETTE[i++] = r << 24 | g << 16 | b << 8 | 0xFF;
////
////                    }
////                }
////            }
//        }
////                {
////                    // vinik24
////                        0x00000000,
////                        0x000000FF,
////                        0x6F6776FF,
////                        0x9A9A97FF,
////                        0xC5CCB8FF,
////                        0x8B5580FF,
////                        0xC38890FF,
////                        0xA593A5FF,
////                        0x666092FF,
////                        0x9A4F50FF,
////                        0xC28D75FF,
////                        0x7CA1C0FF,
////                        0x416AA3FF,
////                        0x8D6268FF,
////                        0xBE955CFF,
////                        0x68ACA9FF,
////                        0x387080FF,
////                        0x6E6962FF,
////                        0x93A167FF,
////                        0x6EAA78FF,
////                        0x557064FF,
////                        0x9D9F7FFF,
////                        0x7E9E99FF,
////                        0x5D6872FF,
////                        0x433455FF,
////
//////                    // vine's flexible linear color ramps
//////                        0x00000000,
//////                        0x280B26FF,
//////                        0x361027FF,
//////                        0x681824FF,
//////                        0xB42313FF,
//////                        0xF4680BFF,
//////                        0xF4C047FF,
//////                        0xFFFDF0FF,
//////                        0x0C1327FF,
//////                        0x03282BFF,
//////                        0x09493FFF,
//////                        0x118337FF,
//////                        0x57C52BFF,
//////                        0xB9ED5EFF,
//////                        0x1A112EFF,
//////                        0x291945FF,
//////                        0x5E1C5AFF,
//////                        0x8F1767FF,
//////                        0xF45D92FF,
//////                        0xFEB58BFF,
//////                        0x0E092FFF,
//////                        0x1B1853FF,
//////                        0x222D81FF,
//////                        0x465BE7FF,
//////                        0x2AC0F2FF,
//////                        0x7DF2CFFF,
//////                        0x220C27FF,
//////                        0x2F1316FF,
//////                        0x431E1EFF,
//////                        0x74341AFF,
//////                        0xAF5D23FF,
//////                        0xF8993AFF,
//////                        0x19102EFF,
//////                        0x241E44FF,
//////                        0x25315EFF,
//////                        0x3A5C85FF,
//////                        0x56A1BFFF,
//////                        0x97DBD2FF,
////
//////                    // linear basic color ramps
//////                        0x00000000,
//////                        0x0E0C0CFF,
//////                        0x5F2D56FF,
//////                        0x993970FF,
//////                        0xDC4A7BFF,
//////                        0xF78697FF,
//////                        0x9F294EFF,
//////                        0x62232FFF,
//////                        0x8F4029FF,
//////                        0xC56025FF,
//////                        0xEE8E2EFF,
//////                        0xFCCBA3FF,
//////                        0xDA4E38FF,
//////                        0xFACB3EFF,
//////                        0x97DA3FFF,
//////                        0x4BA747FF,
//////                        0x3D734FFF,
//////                        0x314152FF,
//////                        0x417089FF,
//////                        0x49A790FF,
//////                        0x72D6CEFF,
//////                        0x5698CCFF,
//////                        0x5956BDFF,
//////                        0x473579FF,
//////                        0x8156AAFF,
//////                        0xC278D0FF,
//////                        0xF0B3DDFF,
//////                        0xFDF7EDFF,
//////                        0xD3BFA9FF,
//////                        0xAA8D7AFF,
//////                        0x775C55FF,
//////                        0x483B3AFF,
////                };
////                {
////                        0x00000000,
//////                0x8c0000ff, 0x809ce4ff, 0xb1ffc0ff, 0xff008cff, 0x00465eff, 0xb99d00ff, 0xffe8ffff, 0x4ead9aff, 0xa30000ff, 0x08a6f7ff, 0xefffb6ff, 0xff00c9ff, 0x004854ff, 0xde8b00ff, 0xf4fcffff, 0x6faa89ff, 0xac0014ff, 0x00b1ffff, 0xffffbdff, 0xff48ffff, 0x004945ff, 0xf17e42ff, 0x00ffffff, 0x84a77bff, 0xa40040ff, 0x00baf6ff, 0xffffccff, 0xb278ffff, 0x004a2cff, 0xf37a6eff, 0x00ffffff, 0x97a46bff, 0x8a0067ff, 0x00bfd5ff, 0xffffdbff, 0x0098ffff, 0x004a02ff, 0xe77f90ff, 0x00ffffff, 0xac9e58ff, 0x61107fff, 0x00c1a0ff, 0xfffee8ff, 0x00abffff, 0x004900ff, 0xd487a8ff, 0x00ffffff, 0xc89549ff, 0x2e3185ff, 0x00c05fff, 0xfff8f5ff, 0x00b4ffff, 0x2e4500ff, 0xc08eb7ff, 0x00ffffff, 0xe78445ff, 0x003e7fff, 0x00bc00ff, 0xfff0ffff, 0x00b7f5ff, 0x573a00ff, 0xad94c3ff, 0x00fff4ff, 0xff6b56ff, 0x004472ff, 0x59b300ff, 0xffeaffff, 0x00b6c9ff, 0x7d1d00ff, 0x9699d0ff, 0x60ffbeff, 0xff497aff, 0x004866ff, 0xa0a400ff, 0xffe9ffff, 0x00b3a5ff, 0x9e0000ff, 0x6ea0deff, 0xc6ffa0ff, 0xff28aeff, 0x004b5bff, 0xd29000ff, 0xfff0ffff, 0x55af8eff, 0xb60000ff, 0x00a8e9ff, 0xfcff9aff, 0xff3bebff, 0x004f51ff, 0xf37b00ff, 0xb1ffffff, 0x7bab80ff, 0xbf0035ff, 0x00b0e9ff, 0xffffa6ff, 0xdc68ffff, 0x005243ff, 0xff6d4cff, 0x00ffffff, 0x92a877ff, 0xb10067ff, 0x00b7d8ff, 0xfff5baff, 0x638effff, 0x00562dff, 0xfa6c79ff, 0x00ffffff, 0xa6a46eff, 0x8e0091ff, 0x00bab2ff, 0xffecccff, 0x00a9ffff, 0x005903ff, 0xe6759bff, 0x00ffffff, 0xbc9f64ff, 0x5833a8ff, 0x00b97aff, 0xffe6dbff, 0x00baffff, 0x305900ff, 0xcb80b0ff, 0x00ffffff, 0xd7965eff, 0x004cacff, 0x00b634ff, 0xffdfe8ff, 0x00c1ffff, 0x595400ff, 0xb089baff, 0x00fff6ff, 0xf58863ff, 0x0059a1ff, 0x0bae00ff, 0xffd7f8ff, 0x00c4f1ff, 0x844600ff, 0x988ec0ff, 0x00ffb1ff, 0xff7479ff, 0x006090ff, 0x79a100ff, 0xffd2ffff, 0x00c4c3ff, 0xae2100ff, 0x7d92c4ff, 0x6aff7cff, 0xff5ea2ff, 0x006680ff, 0xb28e00ff, 0xffd1ffff, 0x00c2a1ff, 0xd20000ff, 0x5396c9ff, 0xbdf561ff, 0xff58daff, 0x006a72ff, 0xdc7400ff, 0xcad8ffff, 0x6dbe8dff, 0xe90037ff, 0x009bcaff, 0xebe364ff, 0xff6effff, 0x016f67ff, 0xf4580aff, 0x1ae4ffff, 0x96bb85ff, 0xea0072ff, 0x009fc2ff, 0xffd379ff, 0xba92ffff, 0x1f7359ff, 0xf8474dff, 0x00efffff, 0xb0b883ff, 0xd000abff, 0x00a1aaff, 0xffc793ff, 0x00b3ffff, 0x357744ff, 0xe94b78ff, 0x00f7ffff, 0xc7b683ff, 0x9d3bd5ff, 0x00a080ff, 0xffbfa9ff, 0x00cbffff, 0x4f7a25ff, 0xcd5b96ff, 0x00f9ffff, 0xdeb383ff, 0x4d63e8ff, 0x009d46ff, 0xffbabaff, 0x00dbffff, 0x707800ff, 0xaa68a6ff, 0x00f5eeff, 0xf8ad86ff, 0x0078e4ff, 0x009600ff, 0xfcb5c8ff, 0x00e3ffff, 0x977100ff, 0x8971a9ff, 0x00efa4ff, 0xffa393ff, 0x0083d1ff, 0x3d8b00ff, 0xf2b1d8ff, 0x00e7fdff, 0xc25e00ff, 0x6c75a7ff, 0x00e461ff, 0xff97b0ff, 0x008ab8ff, 0x7c7b00ff, 0xe2afedff, 0x00e7cdff, 0xed3800ff, 0x4f77a3ff, 0x68d727ff, 0xff8edeff, 0x008ea1ff, 0xa96100ff, 0xc1b1ffff, 0x4fe6adff, 0xff003dff, 0x1d789fff, 0xaec603ff, 0xff93ffff, 0x2a9190ff, 0xc93c00ff, 0x7bb8ffff, 0x9ce29dff, 0xff0079ff, 0x007999ff, 0xd7b42aff, 0xffaaffff, 0x489381ff, 0xda000aff, 0x00c3ffff, 0xc7df9cff, 0xff00b9ff, 0x007a8aff, 0xeea450ff, 0x9dc9ffff, 0x5b9672ff, 0xd60042ff, 0x00ccffff, 0xe3dda3ff, 0xe23cf2ff, 0x00796fff, 0xf49a72ff, 0x00e7ffff, 0x6e975eff, 0xc1006aff, 0x00d2ffff, 0xfadbabff, 0x9670ffff, 0x007645ff, 0xee968eff, 0x00fdffff, 0x869644ff, 0x9e3184ff, 0x00d3e7ff, 0xffd9b2ff, 0x008dffff, 0x007100ff, 0xe296a3ff, 0x00ffffff, 0xa59126ff, 0x77468eff, 0x00d0a5ff, 0xffd5bdff, 0x009effff, 0x006a00ff, 0xd597b3ff, 0x00ffffff, 0xca8509ff, 0x52508cff, 0x00cb5fff, 0xffcecfff, 0x00a6f3ff, 0x435e00ff, 0xc897c2ff, 0x00ffffff, 0xf26f1aff, 0x325484ff, 0x00c303ff, 0xffc7f0ff, 0x00a9d1ff, 0x714b00ff, 0xb599d5ff, 0x00ffdcff, 0xff4842ff, 0x06557bff, 0x73b600ff, 0xffc5ffff, 0x00aab3ff, 0x952800ff, 0x919febff, 0x91ffbcff, 0xff0074ff, 0x005572ff, 0xafa700ff, 0xffceffff, 0x47a99cff, 0xae0000ff, 0x38a8ffff, 0xd4ffb1ff, 0xff00b2ff, 0x005568ff, 0xd69500ff, 0xffe4ffff, 0x67a78cff, 0xb8000eff, 0x00b2ffff, 0xfdffb6ff, 0xff2df1ff, 0x005558ff, 0xea8740ff, 0x6affffff, 0x7ba67cff, 0xb1003cff, 0x00bcffff, 0xffffc2ff, 0xce69ffff, 0x00553dff, 0xee8169ff, 0x00ffffff, 0x8da46aff, 0x980061ff, 0x00c1e8ff, 0xfffcd0ff, 0x568effff, 0x005315ff, 0xe5838aff, 0x00ffffff, 0xa4a055ff, 0x720e79ff, 0x00c3b5ff, 0xfff9ddff, 0x00a5ffff, 0x005000ff, 0xd588a1ff, 0x00ffffff, 0xc09740ff, 0x453081ff, 0x00c275ff, 0xfff5eaff, 0x00b0ffff, 0x1d4a00ff, 0xc58db2ff, 0x00ffffff, 0xe28838ff, 0x103c7bff, 0x00be29ff, 0xffeeffff, 0x00b4faff, 0x4b3f00ff, 0xb492c0ff, 0x00ffffff, 0xff6e46ff, 0x004170ff, 0x37b600ff, 0xffe8ffff, 0x00b4d0ff, 0x702700ff, 0x9e97d0ff, 0x42ffd5ff, 0xff4968ff, 0x004363ff, 0x8fa900ff, 0xffe8ffff, 0x00b2adff, 0x900000ff, 0x799ee1ff, 0xbeffb6ff, 0xff139bff, 0x004559ff, 0xc59800ff, 0xfff1ffff, 0x50ae94ff, 0xa60000ff, 0x00a8f1ff, 0xf9ffaeff, 0xff1dd8ff, 0x00464eff, 0xe88500ff, 0xdeffffff, 0x73aa84ff, 0xaf001eff, 0x00b2f6ff, 0xffffb7ff, 0xf256ffff, 0x00483fff, 0xfa7846ff, 0x00ffffff, 0x89a678ff, 0xa5004cff, 0x00bae9ff, 0xffffc9ff, 0x9481ffff, 0x004a28ff, 0xf87573ff, 0x00ffffff, 0x9ca26bff, 0x870073ff, 0x00bfc7ff, 0xffffdaff, 0x009effff, 0x004b00ff, 0xe97c96ff, 0x00ffffff, 0xb19d5cff, 0x591a8bff, 0x00c091ff, 0xfffae9ff, 0x00b0ffff, 0x074a00ff, 0xd386adff, 0x00ffffff, 0xcc9350ff, 0x143890ff, 0x00be4fff, 0xfff4f7ff, 0x00b8ffff, 0x3c4600ff, 0xbc8ebbff, 0x00ffffff, 0xea8351ff, 0x004488ff, 0x00b900ff, 0xffedffff, 0x00baf1ff, 0x633a00ff, 0xa794c5ff, 0x00ffdeff, 0xff6c63ff, 0x004a79ff, 0x6caf00ff, 0xffe7ffff, 0x00b9c4ff, 0x8a1600ff, 0x8e9aceff, 0x6dffa8ff, 0xff4e88ff, 0x004e6bff, 0xad9e00ff, 0xffe6ffff, 0x00b6a0ff, 0xac0000ff, 0x67a0d9ff, 0xc9ff8bff, 0xff3abdff, 0x00525fff, 0xdc8800ff, 0xfcedffff, 0x5ab289ff, 0xc40013ff, 0x00a7dfff, 0xfcff89ff, 0xff4efbff, 0x005654ff, 0xfa7107ff, 0x8cfbffff, 0x81ad7eff, 0xca0048ff, 0x00aedcff, 0xfff498ff, 0xca76ffff, 0x005a46ff, 0xff6250ff, 0x00ffffff, 0x9aaa77ff, 0xb8007dff, 0x00b3c8ff, 0xffe8afff, 0x0099ffff, 0x005e31ff, 0xfa637dff, 0x00ffffff, 0xafa672ff, 0x8f00a8ff, 0x00b4a1ff, 0xffe0c3ff, 0x00b3ffff, 0x23610bff, 0xe26f9eff, 0x00ffffff, 0xc5a26cff, 0x4f42beff, 0x00b369ff, 0xffd9d3ff, 0x00c2ffff, 0x466000ff, 0xc47bb1ff, 0x00ffffff, 0xdf9a6aff, 0x0059bfff, 0x00ae1bff, 0xffd3e1ff, 0x00caffff, 0x6d5b00ff, 0xa684b9ff, 0xccccccff, 0x999999ff, 0x666666ff, 0x333333ff
//////                0x6f0000ff, 0x809de5ff, 0xd1ffdeff, 0xff008cff, 0x002e45ff, 0xb99d00ff, 0xffffffff, 0x4ead9aff, 0x820000ff, 0x08a6f7ff, 0xffffd4ff, 0xff00c9ff, 0x002f3bff, 0xde8b00ff, 0xffffffff, 0x6faa89ff, 0x890000ff, 0x00b1ffff, 0xffffdcff, 0xff48ffff, 0x00302dff, 0xf17e42ff, 0x6fffffff, 0x84a77bff, 0x820029ff, 0x00baf6ff, 0xffffebff, 0xb278ffff, 0x003116ff, 0xf37a6eff, 0x00ffffff, 0x97a46bff, 0x6a004cff, 0x00bfd5ff, 0xfffffaff, 0x0098ffff, 0x003100ff, 0xe77f90ff, 0x00ffffff, 0xac9e58ff, 0x440063ff, 0x00c1a0ff, 0xffffffff, 0x00abffff, 0x002f00ff, 0xd487a8ff, 0x00ffffff, 0xc89549ff, 0x021a69ff, 0x00c05fff, 0xffffffff, 0x00b4ffff, 0x252b00ff, 0xc08eb7ff, 0x00ffffff, 0xe78445ff, 0x002763ff, 0x00bb00ff, 0xffffffff, 0x00b7f5ff, 0x432200ff, 0xad94c3ff, 0x00ffffff, 0xff6c56ff, 0x002c58ff, 0x59b300ff, 0xffffffff, 0x00b6c9ff, 0x620000ff, 0x9599d0ff, 0x87ffdcff, 0xff497aff, 0x00304cff, 0xa0a400ff, 0xffffffff, 0x00b3a6ff, 0x7f0000ff, 0x6ea0deff, 0xe4ffbdff, 0xff29aeff, 0x003343ff, 0xd29000ff, 0xffffffff, 0x55af8eff, 0x950000ff, 0x00a8e8ff, 0xffffb6ff, 0xff3cecff, 0x00373aff, 0xf27b00ff, 0xd3ffffff, 0x7bac81ff, 0x9e0021ff, 0x00b0e8ff, 0xffffc2ff, 0xdd68ffff, 0x003b2dff, 0xff6d4bff, 0x00ffffff, 0x93a877ff, 0x930050ff, 0x00b6d7ff, 0xffffd4ff, 0x648fffff, 0x003f18ff, 0xf96c78ff, 0x00ffffff, 0xa7a56fff, 0x740079ff, 0x00b9b1ff, 0xffffe6ff, 0x00abffff, 0x004200ff, 0xe47499ff, 0x00ffffff, 0xbda065ff, 0x3f1e90ff, 0x00b879ff, 0xfffff4ff, 0x00bbffff, 0x1e4400ff, 0xc97faeff, 0x00ffffff, 0xd9985fff, 0x003995ff, 0x00b432ff, 0xfff7ffff, 0x00c3ffff, 0x474100ff, 0xae87b8ff, 0x00ffffff, 0xf88a65ff, 0x00468bff, 0x00ac00ff, 0xffeeffff, 0x00c7f4ff, 0x6e3400ff, 0x958cbdff, 0x00ffc6ff, 0xff777cff, 0x004e7cff, 0x769e00ff, 0xffe7ffff, 0x00c7c6ff, 0x970000ff, 0x798fc1ff, 0x82ff90ff, 0xff62a5ff, 0x00546dff, 0xae8a00ff, 0xffe6ffff, 0x00c5a5ff, 0xbb0000ff, 0x4f92c5ff, 0xd1ff75ff, 0xff5ddeff, 0x005961ff, 0xd67000ff, 0xdeebffff, 0x71c391ff, 0xd30029ff, 0x0096c6ff, 0xfff676ff, 0xff73ffff, 0x005f57ff, 0xee5302ff, 0x4ff5ffff, 0x9bc08aff, 0xd60063ff, 0x0099bcff, 0xffe489ff, 0xc197ffff, 0x05644bff, 0xf14048ff, 0x00ffffff, 0xb7bf89ff, 0xbf009bff, 0x009ba3ff, 0xffd6a2ff, 0x00b9ffff, 0x266937ff, 0xe24472ff, 0x00ffffff, 0xcebd8aff, 0x8d2bc6ff, 0x009979ff, 0xffcdb7ff, 0x00d3ffff, 0x426d18ff, 0xc4538eff, 0x00ffffff, 0xe6bb8aff, 0x3b57daff, 0x00943fff, 0xffc7c8ff, 0x00e3ffff, 0x636d00ff, 0xa1609dff, 0x00fffbff, 0xffb68eff, 0x006dd7ff, 0x008d00ff, 0xffc1d5ff, 0x00edffff, 0x8a6600ff, 0x8068a0ff, 0x00fbafff, 0xffad9dff, 0x0079c6ff, 0x318100ff, 0xfdbce3ff, 0x00f2ffff, 0xb65500ff, 0x626b9cff, 0x00ef6aff, 0xffa2bbff, 0x0081aeff, 0x717000ff, 0xecb8f7ff, 0x00f4daff, 0xe12c00ff, 0x436c97ff, 0x72e033ff, 0xff9bebff, 0x008699ff, 0x9c5600ff, 0xcab9ffff, 0x5ff3baff, 0xff0036ff, 0x006c92ff, 0xb6ce17ff, 0xffa2ffff, 0x1f8988ff, 0xb92d00ff, 0x83bfffff, 0xaaf1acff, 0xff0073ff, 0x006c8bff, 0xdfbb32ff, 0xffb9ffff, 0x418d7bff, 0xc70000ff, 0x00c9ffff, 0xd7efacff, 0xff00b3ff, 0x006b7cff, 0xf4aa56ff, 0xb2daffff, 0x55906dff, 0xc30035ff, 0x00d2ffff, 0xf5eeb3ff, 0xdc34ecff, 0x006960ff, 0xf99f77ff, 0x00f9ffff, 0x69925aff, 0xad005aff, 0x00d7ffff, 0xffeebdff, 0x916cffff, 0x006436ff, 0xf29b92ff, 0x00ffffff, 0x819240ff, 0x8b1c72ff, 0x00d7ecff, 0xffedc6ff, 0x008affff, 0x005f00ff, 0xe69aa6ff, 0x00ffffff, 0xa18e22ff, 0x64347bff, 0x00d4a9ff, 0xffead1ff, 0x009bffff, 0x005700ff, 0xd89ab6ff, 0x00ffffff, 0xc78203ff, 0x3e3e78ff, 0x00ce62ff, 0xffe5e6ff, 0x00a3f0ff, 0x354b00ff, 0xca9ac5ff, 0x00ffffff, 0xef6c17ff, 0x19416fff, 0x00c509ff, 0xffdfffff, 0x00a7cfff, 0x5d3800ff, 0xb79bd7ff, 0x00fff4ff, 0xff4640ff, 0x004165ff, 0x74b800ff, 0xffdeffff, 0x00a8b1ff, 0x7c0a00ff, 0x93a0edff, 0xabffd5ff, 0xff0073ff, 0x00405cff, 0xb0a800ff, 0xffe8ffff, 0x46a79bff, 0x910000ff, 0x3aa9ffff, 0xeeffcbff, 0xff00b1ff, 0x003f51ff, 0xd79600ff, 0xffffffff, 0x66a68bff, 0x990000ff, 0x00b3ffff, 0xffffd0ff, 0xff2cf0ff, 0x003e41ff, 0xeb8840ff, 0x98ffffff, 0x7aa57cff, 0x920027ff, 0x00bcffff, 0xffffdeff, 0xce69ffff, 0x003c28ff, 0xef816aff, 0x00ffffff, 0x8da46aff, 0x7a0049ff, 0x00c2e9ff, 0xffffedff, 0x558effff, 0x003a00ff, 0xe5838aff, 0x00ffffff, 0xa49f55ff, 0x56005fff, 0x00c3b5ff, 0xfffffaff, 0x00a5ffff, 0x003600ff, 0xd588a2ff, 0x00ffffff, 0xc09740ff, 0x291966ff, 0x00c275ff, 0xffffffff, 0x00b0ffff, 0x1a3000ff, 0xc58eb2ff, 0x00ffffff, 0xe28838ff, 0x002560ff, 0x00be29ff, 0xffffffff, 0x00b4faff, 0x3a2600ff, 0xb492c0ff, 0x00ffffff, 0xff6e46ff, 0x002955ff, 0x37b600ff, 0xffffffff, 0x00b4d0ff, 0x570a00ff, 0x9e97d0ff, 0x72fff4ff, 0xff4968ff, 0x002b49ff, 0x8fa900ff, 0xffffffff, 0x00b2adff, 0x710000ff, 0x799ee1ff, 0xdeffd5ff, 0xff139bff, 0x002c3fff, 0xc59800ff, 0xffffffff, 0x50ae94ff, 0x840000ff, 0x00a8f1ff, 0xffffcdff, 0xff1dd8ff, 0x002d35ff, 0xe88500ff, 0xffffffff, 0x73aa84ff, 0x8b0002ff, 0x00b2f6ff, 0xffffd6ff, 0xf256ffff, 0x002f27ff, 0xfa7846ff, 0x00ffffff, 0x89a678ff, 0x830033ff, 0x00bae9ff, 0xffffe8ff, 0x9481ffff, 0x003111ff, 0xf87573ff, 0x00ffffff, 0x9ca26bff, 0x680058ff, 0x00bfc7ff, 0xfffff9ff, 0x009effff, 0x003100ff, 0xe97c96ff, 0x00ffffff, 0xb29d5cff, 0x3c006fff, 0x00c091ff, 0xffffffff, 0x00b0ffff, 0x063100ff, 0xd386adff, 0x00ffffff, 0xcc9351ff, 0x002175ff, 0x00be4fff, 0xffffffff, 0x00b8ffff, 0x2e2e00ff, 0xbc8ebbff, 0x00ffffff, 0xea8351ff, 0x002d6dff, 0x00b900ff, 0xffffffff, 0x00baf2ff, 0x4e2200ff, 0xa794c5ff, 0x00fffbff, 0xff6c63ff, 0x003360ff, 0x6bae00ff, 0xffffffff, 0x00b9c4ff, 0x6f0000ff, 0x8e99ceff, 0x8effc4ff, 0xff4f89ff, 0x003853ff, 0xac9e00ff, 0xffffffff, 0x00b6a1ff, 0x8e0000ff, 0x669fd8ff, 0xe5ffa6ff, 0xff3bbeff, 0x003c48ff, 0xdb8700ff, 0xffffffff, 0x5bb28aff, 0xa50000ff, 0x00a6deff, 0xffffa2ff, 0xff50fcff, 0x00403fff, 0xf87005ff, 0xadffffff, 0x83af7fff, 0xac0034ff, 0x00acdbff, 0xffffb1ff, 0xcb77ffff, 0x004532ff, 0xff614eff, 0x00ffffff, 0x9cab79ff, 0x9e0068ff, 0x00b1c6ff, 0xffffc6ff, 0x009bffff, 0x00491eff, 0xf8617bff, 0x00ffffff, 0xb1a974ff, 0x780091ff, 0x00b29eff, 0xfff7daff, 0x00b5ffff, 0x084d00ff, 0xdf6c9bff, 0x00ffffff, 0xc7a46fff, 0x3630a8ff, 0x00b066ff, 0xffefe9ff, 0x00c5ffff, 0x344e00ff, 0xc178aeff, 0x00ffffff, 0xe29d6dff, 0x0048aaff, 0x00ab16ff, 0xffe8f6ff, 0x00cdffff, 0x5b4a00ff, 0xa280b6ff, 0xccccccff, 0x999999ff, 0x666666ff, 0x333333ff
//////                0x9a0400ff, 0x809ce4ff, 0xa3ffb2ff, 0xff008cff, 0x00526aff, 0xb99d00ff, 0xffdaffff, 0x4ead9aff, 0xb30000ff, 0x07a6f7ff, 0xe1ffa8ff, 0xff00c9ff, 0x005460ff, 0xde8b00ff, 0xe5eeffff, 0x6faa89ff, 0xbd001dff, 0x00b1ffff, 0xffffafff, 0xff48ffff, 0x005550ff, 0xf17e42ff, 0x00ffffff, 0x84a77bff, 0xb4004cff, 0x00baf6ff, 0xfffbbeff, 0xb278ffff, 0x005737ff, 0xf37a6eff, 0x00ffffff, 0x97a46bff, 0x980073ff, 0x00bfd5ff, 0xfff5cdff, 0x0098ffff, 0x00570fff, 0xe77f90ff, 0x00ffffff, 0xac9e58ff, 0x6f208cff, 0x00c1a0ff, 0xfff0daff, 0x00abffff, 0x005500ff, 0xd487a8ff, 0x00ffffff, 0xc89549ff, 0x3c3c93ff, 0x00c05fff, 0xffeae7ff, 0x00b4ffff, 0x375100ff, 0xc08eb7ff, 0x00ffffff, 0xe78445ff, 0x00498cff, 0x00bc00ff, 0xffe2faff, 0x00b7f5ff, 0x624500ff, 0xad94c3ff, 0x00ffe6ff, 0xff6b56ff, 0x004f7fff, 0x59b300ff, 0xffdcffff, 0x00b6c9ff, 0x8a2a00ff, 0x9699d0ff, 0x4cffb1ff, 0xff497aff, 0x005372ff, 0xa0a400ff, 0xffdbffff, 0x00b3a5ff, 0xad0000ff, 0x6fa0deff, 0xb8ff93ff, 0xff28aeff, 0x005667ff, 0xd39000ff, 0xffe3ffff, 0x55af8eff, 0xc60005ff, 0x00a8e9ff, 0xeeff8eff, 0xff3bebff, 0x005a5cff, 0xf37c00ff, 0xa1f2ffff, 0x7aab80ff, 0xcd003eff, 0x00b1e9ff, 0xfff39aff, 0xdc68ffff, 0x005d4dff, 0xff6e4cff, 0x00ffffff, 0x92a776ff, 0xbf0072ff, 0x00b7d8ff, 0xffe8aeff, 0x628effff, 0x006136ff, 0xfa6d79ff, 0x00ffffff, 0xa6a46dff, 0x9a009cff, 0x00bab2ff, 0xffe1c0ff, 0x00a9ffff, 0x0b6310ff, 0xe6769bff, 0x00ffffff, 0xbb9e63ff, 0x643db4ff, 0x00ba7bff, 0xffdacfff, 0x00b9ffff, 0x3a6300ff, 0xcc81b0ff, 0x00ffffff, 0xd6955dff, 0x0755b7ff, 0x00b735ff, 0xffd4ddff, 0x00c0ffff, 0x635d00ff, 0xb18abbff, 0x00ffebff, 0xf48762ff, 0x0062abff, 0x0faf00ff, 0xffcdeeff, 0x00c3f0ff, 0x8e4f00ff, 0x998fc1ff, 0x00ffa7ff, 0xff7378ff, 0x006999ff, 0x7aa300ff, 0xffc8ffff, 0x00c2c2ff, 0xb82c00ff, 0x7e93c6ff, 0x5ffc73ff, 0xff5da1ff, 0x006e88ff, 0xb48f00ff, 0xfbc8ffff, 0x00c09fff, 0xdd0000ff, 0x5598cbff, 0xb4ec59ff, 0xff55d8ff, 0x00727aff, 0xde7500ff, 0xc1cfffff, 0x6bbc8bff, 0xf4003dff, 0x009dcdff, 0xe3db5cff, 0xff6bffff, 0x13766eff, 0xf75a0eff, 0x00dbffff, 0x93b983ff, 0xf30079ff, 0x00a1c4ff, 0xfdcb72ff, 0xb78fffff, 0x287a5fff, 0xfc4a4fff, 0x00e8ffff, 0xaeb681ff, 0xd800b2ff, 0x00a4adff, 0xffbf8cff, 0x00b0ffff, 0x3c7e4aff, 0xed4f7bff, 0x00f0ffff, 0xc3b380ff, 0xa442dcff, 0x00a483ff, 0xffb8a3ff, 0x00c8ffff, 0x55802bff, 0xd05e99ff, 0x00f2ffff, 0xdab07fff, 0x5568eeff, 0x00a14aff, 0xfeb4b5ff, 0x00d7ffff, 0x757e00ff, 0xae6ca9ff, 0x00efe8ff, 0xf4a982ff, 0x007deaff, 0x009a00ff, 0xf6b0c3ff, 0x00deffff, 0x9c7500ff, 0x8e75aeff, 0x00e99fff, 0xff9e8fff, 0x0088d6ff, 0x429000ff, 0xedacd3ff, 0x00e2f8ff, 0xc86300ff, 0x717aacff, 0x00df5cff, 0xff91abff, 0x008ebdff, 0x818000ff, 0xddaae8ff, 0x00e2c8ff, 0xf23d00ff, 0x547ca9ff, 0x63d221ff, 0xff88d8ff, 0x0092a5ff, 0xb06700ff, 0xbdadffff, 0x47dfa7ff, 0xff0040ff, 0x277da5ff, 0xaac200ff, 0xff8cffff, 0x2e9493ff, 0xd14300ff, 0x77b5ffff, 0x95dc97ff, 0xff007cff, 0x007f9fff, 0xd4b126ff, 0xffa3ffff, 0x4b9684ff, 0xe20311ff, 0x00c0ffff, 0xbfd895ff, 0xff00bcff, 0x008191ff, 0xeaa14eff, 0x93c2ffff, 0x5d9875ff, 0xdf0049ff, 0x00caffff, 0xdbd59bff, 0xe43ff4ff, 0x008176ff, 0xf19870ff, 0x00dfffff, 0x709960ff, 0xca1271ff, 0x00d0ffff, 0xf1d3a3ff, 0x9872ffff, 0x007e4cff, 0xeb958cff, 0x00f5ffff, 0x879846ff, 0xa73a8cff, 0x00d1e6ff, 0xffd0aaff, 0x008fffff, 0x007a09ff, 0xe095a1ff, 0x00ffffff, 0xa69327ff, 0x804e97ff, 0x00cfa4ff, 0xffcbb3ff, 0x009fffff, 0x007300ff, 0xd495b1ff, 0x00ffffff, 0xcb860cff, 0x5b5896ff, 0x00ca5eff, 0xffc4c5ff, 0x00a7f4ff, 0x4b6700ff, 0xc796c1ff, 0x00ffffff, 0xf3701bff, 0x3c5c8eff, 0x00c200ff, 0xffbce5ff, 0x00aad2ff, 0x7b5400ff, 0xb498d4ff, 0x00ffd1ff, 0xff4942ff, 0x1b5e85ff, 0x72b600ff, 0xffb9ffff, 0x00aab3ff, 0xa03200ff, 0x909eebff, 0x85ffb1ff, 0xff0075ff, 0x005f7cff, 0xaea600ff, 0xffc2ffff, 0x48a99dff, 0xbb0000ff, 0x37a7ffff, 0xc8fea5ff, 0xff00b2ff, 0x006072ff, 0xd59500ff, 0xf4d8ffff, 0x67a88cff, 0xc60016ff, 0x00b2ffff, 0xf1f8aaff, 0xff2ef1ff, 0x006062ff, 0xea873fff, 0x50f3ffff, 0x7ba67dff, 0xbf0046ff, 0x00bbffff, 0xfff3b6ff, 0xce6affff, 0x006048ff, 0xee8169ff, 0x00ffffff, 0x8ea46aff, 0xa5006dff, 0x00c1e8ff, 0xffefc3ff, 0x568fffff, 0x005f1fff, 0xe5828aff, 0x00ffffff, 0xa4a055ff, 0x7e1f85ff, 0x00c3b5ff, 0xffeccfff, 0x00a5ffff, 0x005b00ff, 0xd588a1ff, 0x00ffffff, 0xc09741ff, 0x523b8dff, 0x00c274ff, 0xffe7ddff, 0x00b0ffff, 0x245600ff, 0xc48db2ff, 0x00ffffff, 0xe28838ff, 0x234788ff, 0x00be29ff, 0xffe0f1ff, 0x00b4faff, 0x554a00ff, 0xb492c0ff, 0x00fffbff, 0xff6e46ff, 0x004c7cff, 0x37b600ff, 0xffdaffff, 0x00b4d0ff, 0x7c3300ff, 0x9e97d0ff, 0x1fffc7ff, 0xff4968ff, 0x004f70ff, 0x8fa900ff, 0xffdaffff, 0x00b2adff, 0x9f0000ff, 0x799ee1ff, 0xafffa8ff, 0xff139bff, 0x005065ff, 0xc59800ff, 0xffe3ffff, 0x50ae94ff, 0xb70000ff, 0x00a8f1ff, 0xebffa0ff, 0xff1dd8ff, 0x00525aff, 0xe88500ff, 0xcef5ffff, 0x73aa84ff, 0xc00028ff, 0x00b2f6ff, 0xffffaaff, 0xf256ffff, 0x00554bff, 0xfa7846ff, 0x00ffffff, 0x89a678ff, 0xb40058ff, 0x00bae9ff, 0xfff8bbff, 0x9481ffff, 0x005632ff, 0xf87573ff, 0x00ffffff, 0x9ca26bff, 0x950080ff, 0x00bfc7ff, 0xfff2ccff, 0x009effff, 0x005709ff, 0xe97c96ff, 0x00ffffff, 0xb19d5cff, 0x672798ff, 0x00c091ff, 0xffecdbff, 0x00b0ffff, 0x145600ff, 0xd386adff, 0x00ffffff, 0xcc9350ff, 0x29429dff, 0x00be4fff, 0xffe6e9ff, 0x00b8ffff, 0x455100ff, 0xbd8ebbff, 0x00ffffff, 0xea8351ff, 0x004e94ff, 0x00b900ff, 0xffdffbff, 0x00baf1ff, 0x6f4400ff, 0xa794c5ff, 0x00ffd1ff, 0xff6c63ff, 0x005585ff, 0x6caf00ff, 0xffdaffff, 0x00b9c4ff, 0x972400ff, 0x8e9acfff, 0x5cff9cff, 0xff4e88ff, 0x005976ff, 0xad9e00ff, 0xffdaffff, 0x00b5a0ff, 0xbb0000ff, 0x67a0d9ff, 0xbcff7fff, 0xff39bdff, 0x005c69ff, 0xdc8900ff, 0xefe1ffff, 0x5ab189ff, 0xd3001aff, 0x00a7e0ff, 0xf0f77dff, 0xff4efaff, 0x00605eff, 0xfa7208ff, 0x7cefffff, 0x81ad7dff, 0xd80051ff, 0x00aeddff, 0xffe88dff, 0xc975ffff, 0x00644fff, 0xff6350ff, 0x00feffff, 0x99a977ff, 0xc40088ff, 0x00b3c9ff, 0xffdda4ff, 0x0099ffff, 0x056839ff, 0xfb647eff, 0x00ffffff, 0xaea671ff, 0x9a12b2ff, 0x00b5a2ff, 0xffd5b9ff, 0x00b2ffff, 0x2d6a16ff, 0xe3709fff, 0x00ffffff, 0xc3a16bff, 0x5a4ac8ff, 0x00b46aff, 0xffcfc9ff, 0x00c1ffff, 0x4f6900ff, 0xc57db3ff, 0x00ffffff, 0xdd9969ff, 0x0061c8ff, 0x00b01dff, 0xffcad8ff, 0x00c8ffff, 0x766300ff, 0xa886bbff, 0xccccccff, 0x999999ff, 0x666666ff, 0x333333ff
//////                0x9d0b00ff, 0x89a4edff, 0xb1ffc0ff, 0xff2194ff, 0x00546dff, 0xc2a500ff, 0xffe9ffff, 0x57b6a2ff, 0xb60000ff, 0x26aeffff, 0xefffb6ff, 0xff18d1ff, 0x005662ff, 0xe89300ff, 0xf5fcffff, 0x77b291ff, 0xc0001fff, 0x00b9ffff, 0xffffbdff, 0xff52ffff, 0x005853ff, 0xfb8649ff, 0x00ffffff, 0x8caf82ff, 0xb7004eff, 0x00c2feff, 0xffffccff, 0xbb80ffff, 0x00593aff, 0xfc8276ff, 0x00ffffff, 0x9fac72ff, 0x9b0076ff, 0x00c8ddff, 0xffffdbff, 0x00a0ffff, 0x005912ff, 0xf08798ff, 0x00ffffff, 0xb5a660ff, 0x71238eff, 0x00caa8ff, 0xfffee8ff, 0x00b3ffff, 0x005800ff, 0xdc8fb0ff, 0x00ffffff, 0xd09c50ff, 0x3f3f95ff, 0x00c967ff, 0xfff8f5ff, 0x00bdffff, 0x395300ff, 0xc996bfff, 0x00ffffff, 0xf18c4dff, 0x004b8fff, 0x00c40aff, 0xfff0ffff, 0x00bffeff, 0x644700ff, 0xb59cccff, 0x00fff4ff, 0xff745dff, 0x005182ff, 0x62bb00ff, 0xffeaffff, 0x00bfd1ff, 0x8d2d00ff, 0x9ea1d8ff, 0x61ffbfff, 0xff5381ff, 0x005574ff, 0xa9ac00ff, 0xffe9ffff, 0x00bbadff, 0xb10000ff, 0x77a8e6ff, 0xc6ffa0ff, 0xff38b6ff, 0x005969ff, 0xdc9800ff, 0xfff1ffff, 0x5db796ff, 0xca0008ff, 0x00b0f1ff, 0xfcff9bff, 0xff47f4ff, 0x005c5fff, 0xfd830eff, 0xb2ffffff, 0x82b388ff, 0xd10041ff, 0x00b9f2ff, 0xffffa7ff, 0xe570ffff, 0x006050ff, 0xff7653ff, 0x00ffffff, 0x9aaf7eff, 0xc30075ff, 0x00bfe0ff, 0xfff6bbff, 0x6f96ffff, 0x006439ff, 0xff7581ff, 0x00ffffff, 0xaeac75ff, 0x9e00a0ff, 0x00c3baff, 0xffeecdff, 0x00b1ffff, 0x116614ff, 0xef7ea3ff, 0x00ffffff, 0xc4a76bff, 0x6840b7ff, 0x00c382ff, 0xffe7dcff, 0x00c1ffff, 0x3d6600ff, 0xd489b8ff, 0x00ffffff, 0xdf9e65ff, 0x1558bbff, 0x00bf3dff, 0xffe1eaff, 0x00c9ffff, 0x666100ff, 0xb991c3ff, 0x00fff8ff, 0xfe906aff, 0x0065afff, 0x24b700ff, 0xffdafbff, 0x00ccf9ff, 0x925300ff, 0xa097c9ff, 0x00ffb3ff, 0xff7c80ff, 0x006d9eff, 0x83aa00ff, 0xffd4ffff, 0x00cccbff, 0xbe3100ff, 0x859bceff, 0x6eff7eff, 0xff67a9ff, 0x00728dff, 0xbc9600ff, 0xffd4ffff, 0x00c9a8ff, 0xe30000ff, 0x5d9fd3ff, 0xc0f864ff, 0xff61e1ff, 0x00767fff, 0xe77c00ff, 0xcddbffff, 0x74c594ff, 0xfa0041ff, 0x00a4d4ff, 0xefe767ff, 0xff75ffff, 0x1b7b73ff, 0xff6217ff, 0x2ae7ffff, 0x9cc28bff, 0xf9007eff, 0x00a8ccff, 0xffd77dff, 0xc298ffff, 0x2d7f64ff, 0xff5255ff, 0x00f3ffff, 0xb7bf8aff, 0xde00b7ff, 0x00abb3ff, 0xffcb97ff, 0x00b9ffff, 0x41834fff, 0xf55681ff, 0x00fbffff, 0xcdbd89ff, 0xaa48e2ff, 0x00ab89ff, 0xffc3adff, 0x00d1ffff, 0x5b8530ff, 0xd865a0ff, 0x00feffff, 0xe4b988ff, 0x5c6df4ff, 0x00a750ff, 0xffbebfff, 0x00e1ffff, 0x7b8300ff, 0xb572b0ff, 0x00fbf3ff, 0xffb38bff, 0x0082f0ff, 0x00a100ff, 0xffbaceff, 0x00e9ffff, 0xa37b00ff, 0x947bb4ff, 0x00f4a9ff, 0xffa998ff, 0x008eddff, 0x499600ff, 0xf8b6ddff, 0x00edffff, 0xcf6900ff, 0x777fb2ff, 0x00ea66ff, 0xff9cb5ff, 0x0094c3ff, 0x888500ff, 0xe8b4f3ff, 0x00edd3ff, 0xfa4501ff, 0x5a81aeff, 0x6fdd2fff, 0xff93e3ff, 0x0098acff, 0xb66c00ff, 0xc7b6ffff, 0x55eab1ff, 0xff0046ff, 0x2f83abff, 0xb4cc13ff, 0xff98ffff, 0x379b99ff, 0xd84800ff, 0x82beffff, 0xa0e7a2ff, 0xff0083ff, 0x0085a5ff, 0xdeba31ff, 0xffaeffff, 0x529d8bff, 0xe91516ff, 0x00c9ffff, 0xcbe3a0ff, 0xff00c3ff, 0x008696ff, 0xf5ab56ff, 0xa2cdffff, 0x649f7bff, 0xe5004dff, 0x00d3ffff, 0xe7e0a6ff, 0xec48fcff, 0x00867bff, 0xfba179ff, 0x00ebffff, 0x77a167ff, 0xcf1d76ff, 0x00d9ffff, 0xfddeaeff, 0xa179ffff, 0x008350ff, 0xf59d95ff, 0x00ffffff, 0x8fa04dff, 0xac3f90ff, 0x00dbefff, 0xffdcb5ff, 0x0096ffff, 0x007e0fff, 0xe99daaff, 0x00ffffff, 0xae9a2fff, 0x84529cff, 0x00d8adff, 0xffd8bfff, 0x00a6ffff, 0x007700ff, 0xdd9ebaff, 0x00ffffff, 0xd48e18ff, 0x5f5c9aff, 0x00d366ff, 0xffd0d2ff, 0x00affcff, 0x4f6b00ff, 0xd09fcaff, 0x00ffffff, 0xfd7724ff, 0x406092ff, 0x00cb15ff, 0xffc9f2ff, 0x00b2daff, 0x7f5800ff, 0xbca1ddff, 0x00ffdeff, 0xff5349ff, 0x206288ff, 0x7bbe00ff, 0xffc6ffff, 0x00b2bbff, 0xa53600ff, 0x99a6f4ff, 0x92ffbeff, 0xff0e7cff, 0x006280ff, 0xb7ae00ff, 0xffd0ffff, 0x50b1a4ff, 0xbf0000ff, 0x45afffff, 0xd5ffb2ff, 0xff00baff, 0x006376ff, 0xdf9d03ff, 0xffe5ffff, 0x6fb094ff, 0xcb0019ff, 0x00baffff, 0xffffb7ff, 0xff3cfaff, 0x006365ff, 0xf38f47ff, 0x6cffffff, 0x83ae84ff, 0xc30048ff, 0x00c4ffff, 0xffffc3ff, 0xd772ffff, 0x00634aff, 0xf78971ff, 0x00ffffff, 0x96ac72ff, 0xa9006fff, 0x00caf1ff, 0xfffdd1ff, 0x6396ffff, 0x006122ff, 0xee8a92ff, 0x00ffffff, 0xaca85cff, 0x822388ff, 0x00ccbdff, 0xfffaddff, 0x00adffff, 0x005e00ff, 0xde90a9ff, 0x00ffffff, 0xc99f48ff, 0x553e90ff, 0x00cb7cff, 0xfff5ebff, 0x00b8ffff, 0x275800ff, 0xcd95baff, 0x00ffffff, 0xeb8f3fff, 0x27498bff, 0x00c732ff, 0xffeeffff, 0x00bdffff, 0x574d00ff, 0xbc9ac8ff, 0x00ffffff, 0xff774dff, 0x004e7fff, 0x43bf00ff, 0xffe9ffff, 0x00bdd8ff, 0x7f3500ff, 0xa69fd8ff, 0x42ffd5ff, 0xff5370ff, 0x005172ff, 0x98b200ff, 0xffe8ffff, 0x00bab5ff, 0xa20000ff, 0x81a6eaff, 0xbeffb6ff, 0xff2ba3ff, 0x005368ff, 0xcea000ff, 0xfff1ffff, 0x58b69cff, 0xba0000ff, 0x19b0faff, 0xf9ffaeff, 0xff30e0ff, 0x00555cff, 0xf28d00ff, 0xdeffffff, 0x7bb28cff, 0xc3002aff, 0x00baffff, 0xffffb7ff, 0xfb5fffff, 0x00574dff, 0xff804dff, 0x00ffffff, 0x91ae7fff, 0xb8005aff, 0x00c2f2ff, 0xffffc9ff, 0x9e89ffff, 0x005935ff, 0xff7d7bff, 0x00ffffff, 0xa4aa72ff, 0x980083ff, 0x00c7cfff, 0xffffdaff, 0x00a6ffff, 0x005a0cff, 0xf2849eff, 0x00ffffff, 0xbaa563ff, 0x692a9bff, 0x00c999ff, 0xfffae9ff, 0x00b8ffff, 0x175900ff, 0xdc8eb5ff, 0x00ffffff, 0xd59b58ff, 0x2d45a0ff, 0x00c757ff, 0xfff4f7ff, 0x00c0ffff, 0x485400ff, 0xc596c4ff, 0x00ffffff, 0xf38b58ff, 0x005197ff, 0x00c100ff, 0xffedffff, 0x00c2faff, 0x724700ff, 0xaf9ccdff, 0x00ffdeff, 0xff746aff, 0x005888ff, 0x75b700ff, 0xffe8ffff, 0x00c1ccff, 0x9b2800ff, 0x96a2d7ff, 0x6effa9ff, 0xff5890ff, 0x005c79ff, 0xb6a600ff, 0xffe7ffff, 0x00bea8ff, 0xbf0000ff, 0x70a8e1ff, 0xcaff8cff, 0xff46c5ff, 0x005f6dff, 0xe69000ff, 0xfdefffff, 0x62ba91ff, 0xd7001dff, 0x00afe8ff, 0xfeff8aff, 0xff58ffff, 0x006361ff, 0xff7916ff, 0x8efdffff, 0x89b585ff, 0xdc0055ff, 0x00b6e5ff, 0xfff59aff, 0xd27effff, 0x006853ff, 0xff6b57ff, 0x00ffffff, 0xa2b17fff, 0xc9008bff, 0x00bbd1ff, 0xffeab0ff, 0x27a1ffff, 0x0d6b3cff, 0xff6c85ff, 0x00ffffff, 0xb6ae79ff, 0x9e1ab6ff, 0x00bea9ff, 0xffe2c5ff, 0x00baffff, 0x326e1aff, 0xec78a7ff, 0x00ffffff, 0xcca973ff, 0x5f4eccff, 0x00bc71ff, 0xffdcd6ff, 0x00c9ffff, 0x536d00ff, 0xcd84baff, 0x00ffffff, 0xe7a171ff, 0x0065cdff, 0x00b826ff, 0xffd6e4ff, 0x00d1ffff, 0x7a6700ff, 0xaf8dc3ff, 0xccccccff, 0x999999ff, 0x666666ff, 0x333333ff
//////                0xb82d00ff, 0x97b2fcff, 0xb5ffc3ff, 0xff3ea1ff, 0x006a83ff, 0xd1b200ff, 0xffecffff, 0x65c3afff, 0xd30000ff, 0x41bcffff, 0xf2ffb9ff, 0xff39dfff, 0x006c79ff, 0xf8a019ff, 0xf8ffffff, 0x84c09eff, 0xde0031ff, 0x00c7ffff, 0xffffc0ff, 0xff63ffff, 0x006e68ff, 0xff9356ff, 0x09ffffff, 0x99bd8fff, 0xd40063ff, 0x00d0ffff, 0xffffceff, 0xcb8dffff, 0x00704eff, 0xff9082ff, 0x00ffffff, 0xacb97fff, 0xb6008dff, 0x00d7ecff, 0xffffdeff, 0x00adffff, 0x007027ff, 0xff94a5ff, 0x00ffffff, 0xc3b46dff, 0x8a3ba6ff, 0x00d9b6ff, 0xffffebff, 0x00c1ffff, 0x136e00ff, 0xeb9cbdff, 0x00ffffff, 0xdfaa5dff, 0x5853adff, 0x00d774ff, 0xfffbf8ff, 0x00caffff, 0x4d6900ff, 0xd7a3cdff, 0x00ffffff, 0xff9a59ff, 0x2760a6ff, 0x00d323ff, 0xfff4ffff, 0x00ceffff, 0x7b5c00ff, 0xc3a9daff, 0x00fff8ff, 0xff8269ff, 0x006799ff, 0x72c900ff, 0xffedffff, 0x00cddfff, 0xa74300ff, 0xabafe7ff, 0x65ffc2ff, 0xff648eff, 0x006b8bff, 0xb9ba00ff, 0xffedffff, 0x00c9bbff, 0xcd0000ff, 0x85b5f5ff, 0xcaffa4ff, 0xff4dc3ff, 0x006e7fff, 0xeca500ff, 0xfff5ffff, 0x6bc5a3ff, 0xe7001aff, 0x2abeffff, 0xffff9fff, 0xff59ffff, 0x007274ff, 0xff9122ff, 0xb7ffffff, 0x8fc195ff, 0xee0054ff, 0x00c7ffff, 0xffffabff, 0xf47effff, 0x007664ff, 0xff8460ff, 0x00ffffff, 0xa7bd8bff, 0xdd008bff, 0x00ceefff, 0xfffabfff, 0x82a3ffff, 0x007a4cff, 0xff838eff, 0x00ffffff, 0xbbb981ff, 0xb624b7ff, 0x00d2c8ff, 0xfff3d2ff, 0x00beffff, 0x2e7b29ff, 0xff8cb1ff, 0x00ffffff, 0xd2b377ff, 0x7f55cfff, 0x00d290ff, 0xffece1ff, 0x00ceffff, 0x537b00ff, 0xe397c7ff, 0x00ffffff, 0xedaa71ff, 0x3b6cd2ff, 0x00ce4bff, 0xffe6efff, 0x00d7ffff, 0x7c7500ff, 0xc89fd2ff, 0x00fffeff, 0xff9c76ff, 0x0079c5ff, 0x3dc600ff, 0xffdfffff, 0x00d9ffff, 0xa96600ff, 0xafa5d8ff, 0x00ffb9ff, 0xff898cff, 0x0081b3ff, 0x93b900ff, 0xffdbffff, 0x00d9d7ff, 0xd64700ff, 0x94a9ddff, 0x75ff85ff, 0xff75b5ff, 0x0086a1ff, 0xcda500ff, 0xffdbffff, 0x27d6b4ff, 0xfd000fff, 0x6daee2ff, 0xc7ff6bff, 0xff6feeff, 0x218a92ff, 0xf98b00ff, 0xd5e2ffff, 0x80d29fff, 0xff0052ff, 0x00b3e4ff, 0xf7ee6fff, 0xff82ffff, 0x348e85ff, 0xff7229ff, 0x40efffff, 0xa8ce97ff, 0xff0090ff, 0x00b8dcff, 0xffde84ff, 0xcfa4ffff, 0x429276ff, 0xff6363ff, 0x00fcffff, 0xc3cb95ff, 0xf403cbff, 0x00bbc3ff, 0xffd39eff, 0x00c4ffff, 0x539560ff, 0xff6790ff, 0x00ffffff, 0xd9c894ff, 0xbe5bf6ff, 0x00bc99ff, 0xffccb6ff, 0x00dcffff, 0x6d9741ff, 0xea75b0ff, 0x00ffffff, 0xf0c493ff, 0x727effff, 0x00b95fff, 0xffc8c8ff, 0x00ecffff, 0x8e9414ff, 0xc682c1ff, 0x00fffcff, 0xffbd95ff, 0x0093ffff, 0x00b200ff, 0xffc4d7ff, 0x00f4ffff, 0xb68c00ff, 0xa58bc6ff, 0x00ffb3ff, 0xffb3a2ff, 0x009fefff, 0x5da700ff, 0xffc0e8ff, 0x00f7ffff, 0xe37900ff, 0x8890c4ff, 0x00f570ff, 0xffa6beff, 0x00a5d5ff, 0x9a9600ff, 0xf2befeff, 0x00f7dcff, 0xff5819ff, 0x6c92c1ff, 0x7ae83bff, 0xff9decff, 0x11a9bcff, 0xcb7d00ff, 0xd2c1ffff, 0x5ff4baff, 0xff1354ff, 0x4694bdff, 0xc0d726ff, 0xffa1ffff, 0x4aaba9ff, 0xee5b00ff, 0x8ec9ffff, 0xa9f0aaff, 0xff0092ff, 0x0097b8ff, 0xeac53eff, 0xffb7ffff, 0x62ad9aff, 0xff3627ff, 0x00d5ffff, 0xd3eba8ff, 0xff00d4ff, 0x0099a9ff, 0xffb661ff, 0xacd5ffff, 0x73af8aff, 0xfd245eff, 0x00dfffff, 0xefe8aeff, 0xfe5bffff, 0x00998dff, 0xffad84ff, 0x00f2ffff, 0x85b075ff, 0xe63a88ff, 0x00e6ffff, 0xffe6b5ff, 0xb288ffff, 0x009762ff, 0xffa9a0ff, 0x00ffffff, 0x9eae5bff, 0xc254a4ff, 0x00e8fcff, 0xffe3bcff, 0x31a4ffff, 0x009326ff, 0xf7a9b6ff, 0x00ffffff, 0xbea93eff, 0x9965b0ff, 0x00e6b9ff, 0xffdec6ff, 0x00b5ffff, 0x1b8c00ff, 0xeaaac7ff, 0x00ffffff, 0xe59c2aff, 0x736fafff, 0x00e172ff, 0xffd7d8ff, 0x00bdffff, 0x637f00ff, 0xddabd7ff, 0x00ffffff, 0xff8533ff, 0x5574a7ff, 0x00d829ff, 0xffcff8ff, 0x00c1e9ff, 0x956b00ff, 0xcaadeaff, 0x00ffe3ff, 0xff6356ff, 0x3a769eff, 0x89cc00ff, 0xffccffff, 0x1cc1caff, 0xbe4b00ff, 0xa6b3ffff, 0x98ffc3ff, 0xff3589ff, 0x0b7795ff, 0xc6bb00ff, 0xffd5ffff, 0x5fbfb2ff, 0xdb0200ff, 0x58bcffff, 0xdaffb7ff, 0xff1fc8ff, 0x00788bff, 0xeeaa1dff, 0xffeaffff, 0x7dbea1ff, 0xe7002aff, 0x00c8ffff, 0xffffbbff, 0xff50ffff, 0x00797aff, 0xff9c54ff, 0x74ffffff, 0x90bc91ff, 0xdf005cff, 0x00d2ffff, 0xffffc7ff, 0xe780ffff, 0x00795fff, 0xff967dff, 0x00ffffff, 0xa3ba7fff, 0xc30086ff, 0x00d8ffff, 0xffffd5ff, 0x76a4ffff, 0x007836ff, 0xfd989fff, 0x00ffffff, 0xbab569ff, 0x9a3c9fff, 0x00dacbff, 0xfffde1ff, 0x00baffff, 0x007500ff, 0xec9db7ff, 0x00ffffff, 0xd8ac55ff, 0x6c53a8ff, 0x00d989ff, 0xfff8eeff, 0x00c6ffff, 0x3b6e00ff, 0xdba3c8ff, 0x00ffffff, 0xfb9d4cff, 0x425ea2ff, 0x00d540ff, 0xfff2ffff, 0x00cbffff, 0x6c6200ff, 0xcaa8d6ff, 0x00ffffff, 0xff8559ff, 0x1f6496ff, 0x54cd00ff, 0xffecffff, 0x00cbe6ff, 0x984b00ff, 0xb4ade6ff, 0x48ffd8ff, 0xff647cff, 0x006789ff, 0xa6bf00ff, 0xffebffff, 0x00c8c3ff, 0xbe1600ff, 0x8fb4f8ff, 0xc1ffb9ff, 0xff44b0ff, 0x00697eff, 0xddad00ff, 0xfff4ffff, 0x66c4aaff, 0xd80000ff, 0x39bdffff, 0xfcffb1ff, 0xff48efff, 0x006b73ff, 0xff9a1bff, 0xe1ffffff, 0x88c099ff, 0xe1003cff, 0x00c8ffff, 0xffffbaff, 0xff6effff, 0x006e62ff, 0xff8d5aff, 0x00ffffff, 0x9fbc8cff, 0xd40070ff, 0x00d0ffff, 0xffffccff, 0xae96ffff, 0x007049ff, 0xff8b88ff, 0x00ffffff, 0xb2b87fff, 0xb2009aff, 0x00d6ddff, 0xffffddff, 0x00b3ffff, 0x007023ff, 0xff92abff, 0x00ffffff, 0xc8b270ff, 0x8241b3ff, 0x00d8a6ff, 0xfffeecff, 0x00c5ffff, 0x316f00ff, 0xea9bc3ff, 0x00ffffff, 0xe3a865ff, 0x4959b8ff, 0x00d664ff, 0xfff8faff, 0x00ceffff, 0x5d6900ff, 0xd3a3d2ff, 0x00ffffff, 0xff9864ff, 0x0066afff, 0x00d000ff, 0xfff1ffff, 0x00d0ffff, 0x895c00ff, 0xbdaadcff, 0x00ffe2ff, 0xff8277ff, 0x006d9fff, 0x84c500ff, 0xffecffff, 0x00cfdaff, 0xb53f00ff, 0xa4afe5ff, 0x73ffadff, 0xff689dff, 0x00718fff, 0xc5b400ff, 0xffebffff, 0x00ccb5ff, 0xdb0000ff, 0x7eb5f0ff, 0xceff90ff, 0xff59d3ff, 0x007582ff, 0xf69e00ff, 0xfff3ffff, 0x6fc79eff, 0xf4002dff, 0x27bdf7ff, 0xffff8eff, 0xff68ffff, 0x017876ff, 0xff8727ff, 0x94ffffff, 0x96c292ff, 0xf80068ff, 0x00c5f4ff, 0xfffa9fff, 0xe18bffff, 0x137c66ff, 0xff7a64ff, 0x00ffffff, 0xafbf8bff, 0xe200a1ff, 0x00cae0ff, 0xffefb6ff, 0x4badffff, 0x2a804fff, 0xff7b93ff, 0x00ffffff, 0xc3bb85ff, 0xb537ccff, 0x00cdb7ff, 0xffe8cbff, 0x00c7ffff, 0x47822eff, 0xfc86b5ff, 0x00ffffff, 0xdab67fff, 0x7661e3ff, 0x00cc7eff, 0xffe2dcff, 0x00d6ffff, 0x688100ff, 0xdd92c9ff, 0x00ffffff, 0xf4ad7cff, 0x1478e3ff, 0x00c736ff, 0xffddeaff, 0x00deffff, 0x907a00ff, 0xbe9bd2ff, 0xccccccff, 0x999999ff, 0x666666ff, 0x333333ff
//////                0xb62b00ff, 0xa3beffff, 0xb6ffc5ff, 0xff2696ff, 0x006982ff, 0xdebd00ff, 0xffedffff, 0x5bb9a5ff, 0xd20000ff, 0x50c5ffff, 0xf3ffbaff, 0xff27d7ff, 0x006c78ff, 0xffa824ff, 0xf9ffffff, 0x7eb997ff, 0xde0030ff, 0x00cdffff, 0xffffc0ff, 0xff5cffff, 0x006e68ff, 0xff985aff, 0x0cffffff, 0x95b98bff, 0xd40063ff, 0x00d4ffff, 0xffffcfff, 0xc88bffff, 0x00704eff, 0xff9184ff, 0x00ffffff, 0xacb97eff, 0xb6008dff, 0x00d6ecff, 0xffffdeff, 0x00aeffff, 0x007027ff, 0xfd93a3ff, 0x00ffffff, 0xc5b66fff, 0x8a3ba6ff, 0x00d5b3ff, 0xffffebff, 0x00c5ffff, 0x136e00ff, 0xe697b9ff, 0x00ffffff, 0xe5af62ff, 0x5853adff, 0x00d16eff, 0xfffcf9ff, 0x00d2ffff, 0x4c6800ff, 0xce9bc5ff, 0x00ffffff, 0xffa261ff, 0x255fa6ff, 0x00c813ff, 0xfff5ffff, 0x00d8ffff, 0x7a5b00ff, 0xb89eceff, 0x00fff9ff, 0xff8e74ff, 0x006597ff, 0x64bc00ff, 0xffefffff, 0x00dbedff, 0xa44100ff, 0x9da1d8ff, 0x69ffc4ff, 0xff759cff, 0x006988ff, 0xa8aa00ff, 0xfff0ffff, 0x2fdacbff, 0xc90000ff, 0x74a5e3ff, 0xceffa7ff, 0xff65d5ff, 0x006b7cff, 0xd79400ff, 0xfff9ffff, 0x7ed8b6ff, 0xe20017ff, 0x00aaebff, 0xffffa3ff, 0xff72ffff, 0x006d6fff, 0xf47d00ff, 0xbeffffff, 0xa5d7aaff, 0xe7004fff, 0x00b0e9ff, 0xffffb1ff, 0xff96ffff, 0x00705eff, 0xff6c4bff, 0x00ffffff, 0xbfd5a2ff, 0xd50084ff, 0x00b5d5ff, 0xffffc7ff, 0xa2bbffff, 0x007245ff, 0xf66976ff, 0x00ffffff, 0xd6d39bff, 0xac15aeff, 0x00b6aeff, 0xfffddcff, 0x00d9ffff, 0x227220ff, 0xe07196ff, 0x00ffffff, 0xf0d092ff, 0x744bc3ff, 0x00b375ff, 0xfff8edff, 0x00ecffff, 0x477000ff, 0xc47aa9ff, 0x00ffffff, 0xffc88dff, 0x2861c4ff, 0x00ae2dff, 0xfff4fdff, 0x00f7ffff, 0x6e6800ff, 0xa981b3ff, 0x00ffffff, 0xffbc94ff, 0x006cb7ff, 0x00a600ff, 0xfff0ffff, 0x00fbffff, 0x985800ff, 0x8f86b7ff, 0x00ffc9ff, 0xffaaacff, 0x0071a2ff, 0x6f9900ff, 0xffedffff, 0x00fcfaff, 0xc23500ff, 0x7489bbff, 0x8aff97ff, 0xff9ad7ff, 0x00748fff, 0xa78500ff, 0xffefffff, 0x5bfad6ff, 0xe40000ff, 0x498dbfff, 0xddff80ff, 0xff96ffff, 0x00767fff, 0xd06b00ff, 0xedf8ffff, 0xa2f6c1ff, 0xf80040ff, 0x0091c1ff, 0xffff85ff, 0xffa7ffff, 0x177870ff, 0xe94e00ff, 0x6effffff, 0xcbf2b9ff, 0xf5007aff, 0x0096b9ff, 0xfff79cff, 0xf7c7ffff, 0x287a60ff, 0xee3c45ff, 0x00ffffff, 0xe6eeb7ff, 0xd700b1ff, 0x0098a1ff, 0xffeeb8ff, 0x43e7ffff, 0x3a7c48ff, 0xe04270ff, 0x00ffffff, 0xfdebb5ff, 0xa13fd9ff, 0x009878ff, 0xffe9d2ff, 0x00ffffff, 0x527c27ff, 0xc5548fff, 0x00ffffff, 0xffe7b4ff, 0x4f64e9ff, 0x009640ff, 0xffe6e6ff, 0x00ffffff, 0x707900ff, 0xa463a0ff, 0x00ffffff, 0xffdfb6ff, 0x0077e3ff, 0x009100ff, 0xffe3f7ff, 0x00ffffff, 0x956f00ff, 0x856da5ff, 0x00ffd2ff, 0xffd4c2ff, 0x0081ceff, 0x398800ff, 0xffe1ffff, 0x00ffffff, 0xbe5b00ff, 0x6a73a4ff, 0x00ff8fff, 0xffc6deff, 0x0086b4ff, 0x7a7900ff, 0xffe0ffff, 0x00fffcff, 0xe63100ff, 0x4e76a2ff, 0x9fff5fff, 0xffbdffff, 0x00889bff, 0xaa6200ff, 0xf6e4ffff, 0x81ffd8ff, 0xff0038ff, 0x2079a1ff, 0xe5fa4fff, 0xffc0ffff, 0x1f8a89ff, 0xcd3f00ff, 0xb5ecffff, 0xc5ffc6ff, 0xff0072ff, 0x007d9dff, 0xffe861ff, 0xffd3ffff, 0x408b7aff, 0xe0000fff, 0x00f8ffff, 0xeeffc2ff, 0xff00b0ff, 0x008091ff, 0xffd983ff, 0xcbefffff, 0x528d6aff, 0xe00049ff, 0x00ffffff, 0xffffc6ff, 0xd72fe8ff, 0x008277ff, 0xffd0a5ff, 0x00ffffff, 0x658e56ff, 0xcd1873ff, 0x00ffffff, 0xfffdcbff, 0x8b67ffff, 0x00824fff, 0xffccc3ff, 0x00ffffff, 0x7c8d3bff, 0xac3f90ff, 0x00ffffff, 0xfff8d0ff, 0x0084ffff, 0x008011ff, 0xffccd8ff, 0x00ffffff, 0x9b881bff, 0x86549eff, 0x00ffdbff, 0xfff1d8ff, 0x0095ffff, 0x007b00ff, 0xffcce8ff, 0x00ffffff, 0xc07d00ff, 0x64609fff, 0x00ff91ff, 0xffe7e8ff, 0x009eeaff, 0x547100ff, 0xfecbf8ff, 0x00ffffff, 0xe9670fff, 0x476699ff, 0x3cfa4dff, 0xffdeffff, 0x00a2c9ff, 0x875f00ff, 0xe9ccffff, 0x00fff1ff, 0xff3f3cff, 0x2c6a91ff, 0xa9ea01ff, 0xffd9ffff, 0x00a3acff, 0xb04000ff, 0xc4d0ffff, 0xa4ffceff, 0xff006fff, 0x006d8bff, 0xe5d700ff, 0xffe0ffff, 0x42a498ff, 0xcf0000ff, 0x7ad8ffff, 0xe4ffc0ff, 0xff00aeff, 0x007082ff, 0xffc33dff, 0xfff3ffff, 0x64a489ff, 0xdd0024ff, 0x00e1ffff, 0xffffc3ff, 0xff29efff, 0x007274ff, 0xffb36aff, 0x80ffffff, 0x7aa57bff, 0xd70057ff, 0x00e9ffff, 0xffffceff, 0xce6affff, 0x00745aff, 0xffab92ff, 0x00ffffff, 0x8fa56cff, 0xbd0081ff, 0x00eeffff, 0xffffd9ff, 0x5a91ffff, 0x007432ff, 0xffabb1ff, 0x00ffffff, 0xa8a458ff, 0x96389cff, 0x00edddff, 0xffffe4ff, 0x00aaffff, 0x007200ff, 0xfeadc7ff, 0x00ffffff, 0xc79e47ff, 0x6950a5ff, 0x00e997ff, 0xfffbf0ff, 0x00b8ffff, 0x396c00ff, 0xe9b0d5ff, 0x00ffffff, 0xed9141ff, 0x405da1ff, 0x00e24cff, 0xfff3ffff, 0x00c0ffff, 0x6b6100ff, 0xd4b2e1ff, 0x00ffffff, 0xff7b51ff, 0x1d6395ff, 0x5fd600ff, 0xffecffff, 0x00c3deff, 0x984a00ff, 0xbbb4eeff, 0x49ffd8ff, 0xff5c76ff, 0x006689ff, 0xacc500ff, 0xffebffff, 0x00c3beff, 0xbd1600ff, 0x93b8fdff, 0xc1ffb9ff, 0xff3fadff, 0x00697eff, 0xe0af00ff, 0xfff4ffff, 0x65c2a8ff, 0xd80000ff, 0x3abeffff, 0xfcffb1ff, 0xff48efff, 0x006b73ff, 0xff991aff, 0xe1ffffff, 0x8ac19bff, 0xe1003cff, 0x00c5ffff, 0xffffbaff, 0xff72ffff, 0x006d62ff, 0xff8956ff, 0x00ffffff, 0xa3c191ff, 0xd40070ff, 0x00cafaff, 0xffffccff, 0xb69cffff, 0x006f49ff, 0xff8481ff, 0x00ffffff, 0xbac087ff, 0xb1009aff, 0x00ccd4ff, 0xffffdeff, 0x00bdffff, 0x006f22ff, 0xf687a1ff, 0x00ffffff, 0xd4bd7bff, 0x8040b2ff, 0x00cb9bff, 0xffffeeff, 0x00d2ffff, 0x2f6d00ff, 0xdc8eb6ff, 0x00ffffff, 0xf3b672ff, 0x4757b6ff, 0x00c656ff, 0xfffbfdff, 0x00deffff, 0x5a6600ff, 0xc294c1ff, 0x00ffffff, 0xffa974ff, 0x0063acff, 0x00bd00ff, 0xfff5ffff, 0x00e4ffff, 0x855800ff, 0xaa97c8ff, 0x00ffe7ff, 0xff9689ff, 0x00689aff, 0x6eb000ff, 0xfff1ffff, 0x00e5efff, 0xaf3a00ff, 0x8f9acfff, 0x7affb3ff, 0xff81b2ff, 0x006b89ff, 0xac9e00ff, 0xfff2ffff, 0x3ae4cdff, 0xd20000ff, 0x659ed7ff, 0xd6ff98ff, 0xff77ecff, 0x006d7bff, 0xd98600ff, 0xfffcffff, 0x89e1b7ff, 0xe90027ff, 0x00a3dcff, 0xffff97ff, 0xff87ffff, 0x006f6dff, 0xf46d00ff, 0xa2ffffff, 0xb1dfadff, 0xeb005fff, 0x00a8d7ff, 0xffffaaff, 0xffa8ffff, 0x00725cff, 0xfc5c4aff, 0x00ffffff, 0xccdca7ff, 0xd40095ff, 0x00acc1ff, 0xfffdc3ff, 0x80cbffff, 0x1a7344ff, 0xf25c76ff, 0x00ffffff, 0xe4daa3ff, 0xa625beff, 0x00ac99ff, 0xfff7daff, 0x00e6ffff, 0x387420ff, 0xd96796ff, 0x00ffffff, 0xfcd69eff, 0x6453d1ff, 0x00aa61ff, 0xfff4edff, 0x00f8ffff, 0x577100ff, 0xba72a8ff, 0x00ffffff, 0xffcf9cff, 0x0067d0ff, 0x00a50cff, 0xfff0feff, 0x00ffffff, 0x7c6800ff, 0x9d7bb0ff, 0xccccccff, 0x999999ff, 0x666666ff, 0x333333ff
//////                0x9e0c00ff, 0xa0bbffff, 0xc2ffd0ff, 0xff0c8eff, 0x00556eff, 0xdab900ff, 0xfff9ffff, 0x53b29eff, 0xb80000ff, 0x4ac1ffff, 0xffffc5ff, 0xff0fcfff, 0x005864ff, 0xfca41fff, 0xffffffff, 0x77b291ff, 0xc30020ff, 0x00c9ffff, 0xffffcbff, 0xff54ffff, 0x005a55ff, 0xff9356ff, 0x46ffffff, 0x8fb386ff, 0xba0050ff, 0x00cfffff, 0xffffdaff, 0xc285ffff, 0x005c3cff, 0xff8c7fff, 0x00ffffff, 0xa6b379ff, 0x9e0078ff, 0x00d1e6ff, 0xffffe9ff, 0x00a9ffff, 0x005c14ff, 0xf68d9eff, 0x00ffffff, 0xc0b16aff, 0x742591ff, 0x00cfadff, 0xfffff6ff, 0x00c0ffff, 0x005a00ff, 0xdf91b2ff, 0x00ffffff, 0xe0ab5eff, 0x424197ff, 0x00c967ff, 0xffffffff, 0x00cdffff, 0x3a5500ff, 0xc795beff, 0x00ffffff, 0xff9e5eff, 0x004d90ff, 0x00c102ff, 0xffffffff, 0x00d5ffff, 0x654900ff, 0xb097c7ff, 0x00ffffff, 0xff8b71ff, 0x005282ff, 0x5bb400ff, 0xfffbffff, 0x00d7eaff, 0x8d2d00ff, 0x9599d0ff, 0x77ffcfff, 0xff729aff, 0x005574ff, 0x9ea200ff, 0xfffbffff, 0x2ad8c9ff, 0xb00000ff, 0x6b9cdaff, 0xd9ffb2ff, 0xff62d3ff, 0x005868ff, 0xcd8b00ff, 0xffffffff, 0x7cd7b4ff, 0xc70006ff, 0x00a1e2ff, 0xffffaeff, 0xff70ffff, 0x005a5cff, 0xe97300ff, 0xcbffffff, 0xa3d5a8ff, 0xcd003eff, 0x00a7dfff, 0xffffbcff, 0xff95ffff, 0x005c4cff, 0xf36242ff, 0x00ffffff, 0xbed4a1ff, 0xbd0071ff, 0x00aacbff, 0xffffd1ff, 0xa2bbffff, 0x005e34ff, 0xea5f6cff, 0x00ffffff, 0xd6d39aff, 0x960099ff, 0x00aaa3ff, 0xffffe6ff, 0x00d9ffff, 0x025f0bff, 0xd4668bff, 0x00ffffff, 0xf0d093ff, 0x5e38aeff, 0x00a76bff, 0xfffff7ff, 0x00edffff, 0x345d00ff, 0xb86f9eff, 0x00ffffff, 0xffc98eff, 0x004fb0ff, 0x00a220ff, 0xfffeffff, 0x00f8ffff, 0x5b5600ff, 0x9d76a7ff, 0x00ffffff, 0xffbd95ff, 0x005aa2ff, 0x009900ff, 0xfff9ffff, 0x00fdffff, 0x834600ff, 0x837aaaff, 0x00ffd3ff, 0xffadaeff, 0x005f8fff, 0x628c00ff, 0xfff6ffff, 0x00fefcff, 0xab1d00ff, 0x677daeff, 0x95ffa0ff, 0xff9ddaff, 0x00627cff, 0x997800ff, 0xfff8ffff, 0x5efdd9ff, 0xcc0000ff, 0x3980b1ff, 0xe6ff88ff, 0xff99ffff, 0x00646dff, 0xc05e00ff, 0xf6ffffff, 0xa6f9c5ff, 0xe00031ff, 0x0084b2ff, 0xffff8dff, 0xffabffff, 0x00665fff, 0xd73f00ff, 0x7cffffff, 0xcff6bdff, 0xde0069ff, 0x0087aaff, 0xffffa4ff, 0xfbcbffff, 0x0f694fff, 0xdc2938ff, 0x00ffffff, 0xebf3bbff, 0xc3009fff, 0x008992ff, 0xfff6c0ff, 0x4febffff, 0x286b39ff, 0xcf3062ff, 0x00ffffff, 0xfff0baff, 0x8d2bc6ff, 0x00886aff, 0xfff0d9ff, 0x00ffffff, 0x416b16ff, 0xb44480ff, 0x00ffffff, 0xffecb9ff, 0x3654d6ff, 0x008632ff, 0xffededff, 0x00ffffff, 0x5e6900ff, 0x945490ff, 0x00ffffff, 0xffe5bcff, 0x0067d1ff, 0x008000ff, 0xffeafeff, 0x00ffffff, 0x826000ff, 0x755e95ff, 0x00ffd9ff, 0xffdac8ff, 0x0071bdff, 0x267800ff, 0xffe7ffff, 0x00ffffff, 0xab4c00ff, 0x5a6394ff, 0x00ff95ff, 0xffcde5ff, 0x0076a3ff, 0x6a6900ff, 0xffe6ffff, 0x00ffffff, 0xd21800ff, 0x3d6691ff, 0xa5ff65ff, 0xffc4ffff, 0x00798cff, 0x975200ff, 0xfce9ffff, 0x89ffdfff, 0xf1002bff, 0x006990ff, 0xeaff55ff, 0xffc7ffff, 0x007b7aff, 0xb82b00ff, 0xbbf1ffff, 0xcdffcdff, 0xfe0064ff, 0x006c8bff, 0xffed66ff, 0xffdbffff, 0x307c6bff, 0xc90000ff, 0x00fdffff, 0xf6ffcaff, 0xf100a1ff, 0x006e7fff, 0xffdd87ff, 0xd5f7ffff, 0x447f5dff, 0xc90039ff, 0x00ffffff, 0xffffceff, 0xc712d8ff, 0x007066ff, 0xffd4a9ff, 0x00ffffff, 0x578049ff, 0xb70062ff, 0x00ffffff, 0xffffd4ff, 0x7b5afcff, 0x006f3fff, 0xffcfc6ff, 0x00ffffff, 0x6f802fff, 0x982b7eff, 0x00ffffff, 0xffffd9ff, 0x0078ffff, 0x006c00ff, 0xffcedbff, 0x00ffffff, 0x8d7c08ff, 0x73438bff, 0x00ffddff, 0xfffbe1ff, 0x0088f9ff, 0x006800ff, 0xffceebff, 0x00ffffff, 0xb27100ff, 0x514f8bff, 0x00ff93ff, 0xfff1f2ff, 0x0091ddff, 0x435e00ff, 0xffcdfaff, 0x00ffffff, 0xda5b00ff, 0x335585ff, 0x3ffb4eff, 0xffe8ffff, 0x0096bdff, 0x734d00ff, 0xebcdffff, 0x00fffbff, 0xff2e31ff, 0x0f587eff, 0xaaeb04ff, 0xffe3ffff, 0x0098a1ff, 0x9a2c00ff, 0xc5d1ffff, 0xaeffd8ff, 0xff0065ff, 0x005a77ff, 0xe5d800ff, 0xffeaffff, 0x35998dff, 0xb60000ff, 0x7bd8ffff, 0xeeffcbff, 0xff00a3ff, 0x005d6fff, 0xffc33cff, 0xfffdffff, 0x5a9a7fff, 0xc30015ff, 0x00e1ffff, 0xffffcdff, 0xff0ae4ff, 0x005f60ff, 0xffb369ff, 0x91ffffff, 0x719b72ff, 0xbe0045ff, 0x00e8ffff, 0xffffd8ff, 0xc460ffff, 0x006047ff, 0xffaa90ff, 0x00ffffff, 0x869c63ff, 0xa6006dff, 0x00ecffff, 0xffffe4ff, 0x4a88ffff, 0x006020ff, 0xffa9afff, 0x00ffffff, 0x9f9b50ff, 0x802187ff, 0x00ebdaff, 0xffffefff, 0x00a2ffff, 0x005e00ff, 0xfbabc4ff, 0x00ffffff, 0xbf953fff, 0x553e90ff, 0x00e694ff, 0xfffffcff, 0x00b0ffff, 0x275900ff, 0xe6add2ff, 0x00ffffff, 0xe48939ff, 0x284a8cff, 0x00de49ff, 0xffffffff, 0x00b8fdff, 0x584e00ff, 0xd1aeddff, 0x00ffffff, 0xff744aff, 0x005080ff, 0x5ad200ff, 0xfff8ffff, 0x00bcd7ff, 0x813700ff, 0xb7afe9ff, 0x5bffe4ff, 0xff5470ff, 0x005374ff, 0xa8c000ff, 0xfff7ffff, 0x00bdb8ff, 0xa40000ff, 0x8eb3f8ff, 0xccffc4ff, 0xff34a7ff, 0x00556aff, 0xdaaa00ff, 0xffffffff, 0x5fbda3ff, 0xbd0000ff, 0x30b9ffff, 0xffffbcff, 0xff3fe9ff, 0x00575fff, 0xfa9410ff, 0xeeffffff, 0x85bc96ff, 0xc6002cff, 0x00bfffff, 0xffffc6ff, 0xff6dffff, 0x00594fff, 0xff8350ff, 0x00ffffff, 0x9fbc8dff, 0xba005cff, 0x00c4f3ff, 0xffffd7ff, 0xb198ffff, 0x005b36ff, 0xff7d7bff, 0x00ffffff, 0xb6bc83ff, 0x9a0085ff, 0x00c5cdff, 0xffffeaff, 0x00b9ffff, 0x005b0eff, 0xee809aff, 0x00ffffff, 0xd0ba77ff, 0x6a2b9cff, 0x00c394ff, 0xfffff9ff, 0x00cfffff, 0x185900ff, 0xd486aeff, 0x00ffffff, 0xf0b36fff, 0x2d45a0ff, 0x00bd4eff, 0xffffffff, 0x00dbffff, 0x475300ff, 0xba8bb9ff, 0x00ffffff, 0xffa772ff, 0x005096ff, 0x00b400ff, 0xffffffff, 0x00e1ffff, 0x704500ff, 0xa18fbfff, 0x00fff2ff, 0xff9488ff, 0x005586ff, 0x63a700ff, 0xfffcffff, 0x00e4eeff, 0x972400ff, 0x8591c5ff, 0x87ffbdff, 0xff80b1ff, 0x005875ff, 0xa19400ff, 0xfffdffff, 0x38e3ccff, 0xb90000ff, 0x5b94ccff, 0xe1ffa2ff, 0xff76ecff, 0x005a67ff, 0xcd7c00ff, 0xffffffff, 0x89e1b7ff, 0xcf0018ff, 0x0099d1ff, 0xffffa2ff, 0xff86ffff, 0x005c5aff, 0xe76200ff, 0xafffffff, 0xb1dfadff, 0xd2004dff, 0x009dcbff, 0xffffb4ff, 0xffa9ffff, 0x005f4aff, 0xef5040ff, 0x00ffffff, 0xcddda8ff, 0xbd0081ff, 0x00a0b5ff, 0xffffccff, 0x81ccffff, 0x006133ff, 0xe44f6bff, 0x00ffffff, 0xe5dba4ff, 0x9100a9ff, 0x009f8dff, 0xffffe4ff, 0x00e8ffff, 0x24610cff, 0xcb5a8aff, 0x00ffffff, 0xfed8a0ff, 0x4e41bdff, 0x009c55ff, 0xfffdf7ff, 0x00faffff, 0x445f00ff, 0xad669bff, 0x00ffffff, 0xffd19fff, 0x0056bbff, 0x009700ff, 0xfffaffff, 0x00ffffff, 0x695700ff, 0x8f6ea2ff, 0xccccccff, 0x999999ff, 0x666666ff, 0x333333ff
////                0x600000ff, 0x93aef8ff, 0xdcffe9ff, 0xff0077ff, 0x002238ff, 0xcaac00ff, 0xffffffff, 0x399a87ff, 0x720000ff, 0x31b3ffff, 0xffffdeff, 0xff00b8ff, 0x00242fff, 0xea9502ff, 0xffffffff, 0x629c7bff, 0x790000ff, 0x00b9ffff, 0xffffe5ff, 0xf437f9ff, 0x002622ff, 0xf78347ff, 0x80ffffff, 0x7b9e72ff, 0x72001fff, 0x00bcf8ff, 0xfffff4ff, 0xab71ffff, 0x002809ff, 0xf27a6eff, 0x00ffffff, 0x93a067ff, 0x5c0040ff, 0x00bcd2ff, 0xffffffff, 0x0097ffff, 0x002800ff, 0xe1798bff, 0x00ffffff, 0xaea05aff, 0x370056ff, 0x00b898ff, 0xffffffff, 0x00afffff, 0x002600ff, 0xc97c9dff, 0x00ffffff, 0xcf9b4fff, 0x00105cff, 0x00b253ff, 0xffffffff, 0x00beffff, 0x222000ff, 0xb07fa7ff, 0x00ffffff, 0xf59050ff, 0x001c56ff, 0x00a800ff, 0xffffffff, 0x00c6ffff, 0x3c1400ff, 0x9880aeff, 0x00ffffff, 0xff7d65ff, 0x00204aff, 0x3c9b00ff, 0xffffffff, 0x00caddff, 0x540000ff, 0x7c80b5ff, 0x95ffe9ff, 0xff648eff, 0x00223dff, 0x828900ff, 0xffffffff, 0x07ccbeff, 0x6d0000ff, 0x4e82beff, 0xf3ffcbff, 0xff54c9ff, 0x002433ff, 0xad7100ff, 0xffffffff, 0x73cdaaff, 0x7e0000ff, 0x0086c4ff, 0xffffc6ff, 0xff66ffff, 0x002629ff, 0xc65700ff, 0xe7ffffff, 0x9bcda0ff, 0x830010ff, 0x0089bfff, 0xffffd4ff, 0xff8dffff, 0x00281bff, 0xce4227ff, 0x00ffffff, 0xb8ce9bff, 0x78003bff, 0x008aabff, 0xffffe9ff, 0x9ab5ffff, 0x002a00ff, 0xc53d50ff, 0x00ffffff, 0xd1ce95ff, 0x580060ff, 0x008883ff, 0xfffffdff, 0x00d5ffff, 0x002b00ff, 0xb0446cff, 0x00ffffff, 0xeccc8fff, 0x1b0174ff, 0x00834cff, 0xffffffff, 0x00eaffff, 0x172900ff, 0x944e7cff, 0x00ffffff, 0xffc78cff, 0x002176ff, 0x007d00ff, 0xffffffff, 0x00f7ffff, 0x332400ff, 0x795483ff, 0x00ffffff, 0xffbc94ff, 0x002a6aff, 0x007400ff, 0xffffffff, 0x00fdffff, 0x501400ff, 0x5f5785ff, 0x00ffe7ff, 0xffadaeff, 0x002e59ff, 0x3e6700ff, 0xffffffff, 0x00fffeff, 0x6e0000ff, 0x415987ff, 0xabffb4ff, 0xff9fdcff, 0x003149ff, 0x715400ff, 0xffffffff, 0x62ffdcff, 0x890000ff, 0x005b89ff, 0xfbff9bff, 0xff9dffff, 0x00333cff, 0x923700ff, 0xffffffff, 0xaafdc9ff, 0x9b0004ff, 0x005d89ff, 0xffff9fff, 0xffb0ffff, 0x003630ff, 0xa50000ff, 0x96ffffff, 0xd4fbc2ff, 0x9d003aff, 0x005e80ff, 0xffffb5ff, 0xffd1ffff, 0x003923ff, 0xa80016ff, 0x00ffffff, 0xf2fac2ff, 0x87006bff, 0x005d68ff, 0xffffd0ff, 0x60f3ffff, 0x003b0dff, 0x9c003bff, 0x00ffffff, 0xfff8c2ff, 0x560090ff, 0x005b42ff, 0xffffe9ff, 0x00ffffff, 0x123d00ff, 0x830d55ff, 0x00ffffff, 0xfff6c2ff, 0x002aa1ff, 0x005707ff, 0xfffcfcff, 0x00ffffff, 0x333b00ff, 0x662963ff, 0x00ffffff, 0xfff0c6ff, 0x003d9dff, 0x005100ff, 0xfff8ffff, 0x00ffffff, 0x533400ff, 0x483366ff, 0x00ffe6ff, 0xffe6d4ff, 0x00458bff, 0x014900ff, 0xfff4ffff, 0x00ffffff, 0x761e00ff, 0x2c3864ff, 0x00ffa1ff, 0xffdbf2ff, 0x004a74ff, 0x423d00ff, 0xfff1ffff, 0x00ffffff, 0x990000ff, 0x003a61ff, 0xb0ff70ff, 0xffd3ffff, 0x004d5fff, 0x652500ff, 0xfff3ffff, 0x99ffeeff, 0xb60006ff, 0x003c5fff, 0xf4ff5eff, 0xffd7ffff, 0x005050ff, 0x7e0000ff, 0xc4faffff, 0xddffddff, 0xc6003dff, 0x003d5aff, 0xfff56eff, 0xffebffff, 0x005344ff, 0x8b0000ff, 0x00ffffff, 0xffffdaff, 0xbd0076ff, 0x003d4eff, 0xffe48dff, 0xe9ffffff, 0x1a5637ff, 0x890011ff, 0x00ffffff, 0xffffe0ff, 0x9700abff, 0x003c37ff, 0xffd9aeff, 0x00ffffff, 0x315925ff, 0x7a0033ff, 0x00ffffff, 0xffffe6ff, 0x4933cfff, 0x003b12ff, 0xffd4caff, 0x00ffffff, 0x485a05ff, 0x5f004bff, 0x00ffffff, 0xffffedff, 0x0054daff, 0x003800ff, 0xffd1deff, 0x00ffffff, 0x645800ff, 0x3e1156ff, 0x00ffdfff, 0xfffff5ff, 0x0065cfff, 0x003300ff, 0xffcfecff, 0x00ffffff, 0x874e00ff, 0x1a2056ff, 0x00ff94ff, 0xffffffff, 0x006db5ff, 0x2a2b00ff, 0xffcdfaff, 0x00ffffff, 0xae3600ff, 0x002550ff, 0x3dfa4dff, 0xfffeffff, 0x007298ff, 0x461c00ff, 0xe9ccffff, 0x3cffffff, 0xd30013ff, 0x002749ff, 0xa8e900ff, 0xfffaffff, 0x00767fff, 0x600000ff, 0xc2ceffff, 0xc6ffefff, 0xee0047ff, 0x002842ff, 0xe1d400ff, 0xffffffff, 0x00786dff, 0x730000ff, 0x75d3ffff, 0xffffe2ff, 0xf40083ff, 0x00293aff, 0xffbe37ff, 0xffffffff, 0x3b7b61ff, 0x7c0000ff, 0x00dbffff, 0xffffe5ff, 0xdd00c2ff, 0x002a2dff, 0xffac63ff, 0xb3ffffff, 0x547e56ff, 0x770017ff, 0x00e1ffff, 0xfffff0ff, 0xa240f8ff, 0x002c17ff, 0xffa289ff, 0x00ffffff, 0x6a8048ff, 0x640037ff, 0x00e2ffff, 0xfffffdff, 0x006effff, 0x002c00ff, 0xff9fa6ff, 0x00ffffff, 0x838037ff, 0x44004eff, 0x00e0d0ff, 0xffffffff, 0x0088ffff, 0x002900ff, 0xefa0b9ff, 0x00ffffff, 0xa27c26ff, 0x170c56ff, 0x00d989ff, 0xffffffff, 0x0097ffff, 0x172400ff, 0xd8a0c5ff, 0x00ffffff, 0xc77121ff, 0x001a52ff, 0x00d03bff, 0xffffffff, 0x00a0e4ff, 0x361900ff, 0xc2a0ceff, 0x00ffffff, 0xef5c35ff, 0x001e48ff, 0x47c200ff, 0xffffffff, 0x00a4c0ff, 0x4d0000ff, 0xa7a0d9ff, 0x7ffffeff, 0xff375cff, 0x00203dff, 0x96b000ff, 0xffffffff, 0x00a7a3ff, 0x640000ff, 0x7ca2e5ff, 0xe7ffddff, 0xff0093ff, 0x002234ff, 0xc69900ff, 0xffffffff, 0x4aa98fff, 0x750000ff, 0x00a6efff, 0xffffd5ff, 0xff14d5ff, 0x00242aff, 0xe48100ff, 0xffffffff, 0x73aa84ff, 0x7b0000ff, 0x00abefff, 0xffffdfff, 0xf559ffff, 0x00261dff, 0xee6e3dff, 0x38ffffff, 0x8fac7dff, 0x730028ff, 0x00adddff, 0xfffff1ff, 0x9e88ffff, 0x002701ff, 0xe76766ff, 0x00ffffff, 0xa7ad74ff, 0x59004bff, 0x00acb5ff, 0xffffffff, 0x00abffff, 0x002700ff, 0xd36983ff, 0x00ffffff, 0xc2ac6bff, 0x2c0060ff, 0x00a87cff, 0xffffffff, 0x00c2ffff, 0x002500ff, 0xb96e95ff, 0x00ffffff, 0xe2a764ff, 0x001665ff, 0x00a136ff, 0xffffffff, 0x00cfffff, 0x2a1f00ff, 0x9e729eff, 0x00ffffff, 0xff9c68ff, 0x00205cff, 0x009800ff, 0xffffffff, 0x00d7ffff, 0x421200ff, 0x8574a2ff, 0x00ffffff, 0xff8b7fff, 0x00234eff, 0x438a00ff, 0xffffffff, 0x00dbe5ff, 0x5c0000ff, 0x6875a7ff, 0xa2ffd5ff, 0xff77aaff, 0x00253fff, 0x817700ff, 0xffffffff, 0x2cdcc5ff, 0x750000ff, 0x3776acff, 0xfaffb9ff, 0xff6fe5ff, 0x002733ff, 0xa85e00ff, 0xffffffff, 0x83dcb1ff, 0x860000ff, 0x0079afff, 0xffffb8ff, 0xff81ffff, 0x002928ff, 0xbf4100ff, 0xcbffffff, 0xaddba9ff, 0x8a001fff, 0x007ca8ff, 0xffffcaff, 0xfea5ffff, 0x002b1bff, 0xc52723ff, 0x00ffffff, 0xcbdba6ff, 0x7b004bff, 0x007c92ff, 0xffffe2ff, 0x7fcaffff, 0x002d00ff, 0xba254bff, 0x00ffffff, 0xe4daa3ff, 0x540070ff, 0x00796aff, 0xfffff9ff, 0x00e8ffff, 0x002e00ff, 0xa33466ff, 0x00ffffff, 0xffd8a0ff, 0x001183ff, 0x007534ff, 0xffffffff, 0x00fcffff, 0x202d00ff, 0x864276ff, 0x00ffffff, 0xffd3a0ff, 0x002982ff, 0x006f00ff, 0xffffffff, 0x00ffffff, 0x3c2700ff, 0x684a7bff, 0xccccccff, 0x999999ff, 0x666666ff, 0x333333ff
////        };
////        double[] color = new double[3];
//        
//
//
////        System.arraycopy(Coloring.CORPUT_64, 0, PALETTE, 0, 10);
////        for (int i = 10, r = 0; r < 9; r++) {
////            PALETTE[i++] = Coloring.CORPUT_64[17+r*8];
////            PALETTE[i++] = VoxelColor.mixThird  (Coloring.CORPUT_64[16+r*8], Coloring.CORPUT_64[15+r*8]);
////            PALETTE[i++] = VoxelColor.mixThird  (Coloring.CORPUT_64[14+r*8], Coloring.CORPUT_64[15+r*8]);
////            PALETTE[i++] = VoxelColor.mixLightly(Coloring.CORPUT_64[13+r*8], Coloring.CORPUT_64[14+r*8]);
////            PALETTE[i++] = VoxelColor.mixEvenly (Coloring.CORPUT_64[12+r*8], Coloring.CORPUT_64[13+r*8]);
////            PALETTE[i++] = VoxelColor.mixThird  (Coloring.CORPUT_64[11+r*8], Coloring.CORPUT_64[12+r*8]);
////        }
//        
//        double luma, warm, mild, hue;
//        double[] lumas = new double[PALETTE.length], warms = new double[PALETTE.length], milds = new double[PALETTE.length];
//        int ctr = 1;
//        int r, g, b;
//        int pal;
//        for (int i = 1; i < PALETTE.length; i++) {
//            //if ((i & 7) == 7)
////            {
////                int ch = i << 2 | i >>> 3;
////                PALETTE[i] = ch << 24 | ch << 16 | ch << 8 | 0xFF;
////                milds[i] = warms[i] = 0.0;
////                lumas[i] = ch / 255.0;
////                ctr++;
////                i++;
//            //} else {
//                //do 
//                    {
////                hue = i * (Math.PI * 1.6180339887498949);
////                    hue = (ctr) * (Math.PI * 2.0 / 53.0);
////                    milds[i] = mild = (NumberTools.sin(hue) * (NumberTools.zigzag(ctr * 1.543) * 0.5 + 0.8));
////                    warms[i] = warm = (NumberTools.cos(hue) * (NumberTools.zigzag(0.4 + ctr * 1.611) * 0.5 + 0.8));
////                    lumas[i] = luma = curvedDouble();
//                    //ctr++;
//                    pal = PALETTE[i];//Coloring.FLESURRECT_ALT[i];
//                    r = pal >>> 24;
//                    g = pal >>> 16 & 0xFF;
//                    b = pal >>> 8 & 0xFF;
//                    mild = (g - b) / 255.0; 
//                    warm = (r - b) / 255.0;                     
//                    luma = (0.375 * r + 0.5 * g + 0.125 * b) / 255.0;
////                    lumas[i] = luma = MathUtils.clamp(((0.375 * r + 0.5 * g + 0.125 * b) / 255.0) 
////                            * (1.0 + (nextDouble() + nextDouble() - nextDouble() - nextDouble()) * 0.2), 0.05, 0.95);
////                    lumas[i] = luma = (curvedDouble() + curvedDouble() + curvedDouble() + curvedDouble()) * 0.25;
//                    
////                color[0] = i * (360.0 * 1.6180339887498949);
////                color[1] = Math.sqrt(1.0 - nextDouble() * nextDouble()) * 100.0;
////                color[2] = curvedDouble() * 100.0;
////                color[2] = i * (94.0 / 255.0) + 3.0;
////                System.out.println(StringKit.join(", ", color) + "  -> " + StringKit.join(", ", HSLUVColorConverter.hsluvToRgb(color)));                 
//
//////normally this next section is used
//                  r = MathUtils.clamp((int) ((luma + warm * 0.625 - mild * 0.5) * 255.5), 0, 255);
//                  g = MathUtils.clamp((int) ((luma + mild * 0.5 - warm * 0.375) * 255.5), 0, 255);
//                  b = MathUtils.clamp((int) ((luma - warm * 0.375 - mild * 0.5) * 255.5), 0, 255);
//                  ////PALETTE[i] = r << 24 | g << 16 | b << 8 | 0xFF;
//                  milds[i] = (g - b) / 255.0;
//                  warms[i] = (r - b) / 255.0;
//                  lumas[i] = (0.375 * r + 0.5 * g + 0.125 * b) / 255.0;
////                }//while (r < 0 || r > 255 || g < 0 || g > 255 || b < 0 || b > 255);
////                PALETTE[i++] = r << 24 |
////                        g << 16 |
////                        b << 8 | 0xFF;
//
////                PALETTE[i++] = (int) (MathUtils.clamp(color[0], 0.0, 1.0) * 255.5) << 24 |
////                        (int) (MathUtils.clamp(color[1], 0.0, 1.0) * 255.5) << 16 |
////                        (int) (MathUtils.clamp(color[2], 0.0, 1.0) * 255.5) << 8 | 0xFF;
//            }
//        }
//        final double THRESHOLD = 0.011; // threshold controls the "stark-ness" of color changes; must not be negative.
//        byte[] paletteMapping = new byte[1 << 16];
//        int[] reverse = new int[PALETTE.length];
//        byte[][] ramps = new byte[PALETTE.length][4];
//        final int yLim = 63, cwLim = 31, cmLim = 31, shift1 = 6, shift2 = 11;
//        for (int i = 1; i < PALETTE.length; i++) {
//            reverse[i] =
//                    (int) ((lumas[i]) * yLim)
//                            | (int) ((warms[i] * 0.5 + 0.5) * cwLim) << shift1
//                            | (int) ((milds[i] * 0.5 + 0.5) * cmLim) << shift2;
//            if(paletteMapping[reverse[i]] != 0)
//                System.out.println("color at index " + i + " overlaps an existing color that has index " + reverse[i] + "!");
//            paletteMapping[reverse[i]] = (byte) i;
//        }
//        double wf, mf, yf;
//        for (int cr = 0; cr <= cmLim; cr++) {
//            wf = (double) cr / cmLim - 0.5;
//            for (int cb = 0; cb <= cwLim; cb++) {
//                mf = (double) cb / cwLim - 0.5;
//                for (int y = 0; y <= yLim; y++) {
//                    final int c2 = cr << shift2 | cb << shift1 | y;
//                    if (paletteMapping[c2] == 0) {
//                        yf = (double) y / yLim;
//                        double dist = Double.POSITIVE_INFINITY;
//                        for (int i = 1; i < PALETTE.length; i++) {
//                            if (Math.abs(lumas[i] - yf) < 0.2f && dist > (dist = Math.min(dist, difference(lumas[i], warms[i], milds[i], yf, wf, mf))))
//                                paletteMapping[c2] = (byte) i;
//                        }
//                    }
//                }
//            }
//        }
//
//        double adj;
//        int idx2;
//        for (int i = 1; i < PALETTE.length; i++) {
//            int rev = reverse[i], y = rev & yLim, match = i;
//            yf = lumas[i];
//            warm = warms[i];
//            mild = milds[i];
//            ramps[i][1] = (byte)i;//Color.rgba8888(DAWNBRINGER_AURORA[i]);
//            ramps[i][0] = 9;//15;  //0xFFFFFFFF, white
//            ramps[i][2] = 1;//0x010101FF, black
//            ramps[i][3] = 1;//0x010101FF, black
//            for (int yy = y + 2, rr = rev + 2; yy <= yLim; yy++, rr++) {
//                if ((idx2 = paletteMapping[rr] & 255) != i && difference(lumas[idx2], warms[idx2], milds[idx2], yf, warm, mild) > THRESHOLD) {
//                    ramps[i][0] = paletteMapping[rr];
//                    break;
//                }
//                adj = 1.0 + ((yLim + 1 >>> 1) - yy) * 0x1p-10;
//                mild = MathUtils.clamp(mild * adj, -0.5, 0.5);
//                warm = MathUtils.clamp(warm * adj + 0x1.8p-10, -0.5, 0.5);
//
////                cof = (cof + 0.5f) * 0.984375f - 0.5f;
////                cgf = (cgf - 0.5f) * 0.96875f + 0.5f;
//                rr = yy
//                        | (int) ((warm + 0.5) * cwLim) << shift1
//                        | (int) ((mild + 0.5) * cmLim) << shift2;
//            }
//            warm = warms[i];
//            mild = milds[i];
//            for (int yy = y - 2, rr = rev - 2; yy > 0; rr--) {
//                if ((idx2 = paletteMapping[rr] & 255) != i && difference(lumas[idx2], warms[idx2], milds[idx2], yf, warm, mild) > THRESHOLD) {
//                    ramps[i][2] = paletteMapping[rr];
//                    rev = rr;
//                    y = yy;
//                    match = paletteMapping[rr] & 255;
//                    break;
//                }
//                adj = 1.0 + (yy - (yLim + 1 >>> 1)) * 0x1p-10;
//                mild = MathUtils.clamp(mild * adj, -0.5, 0.5);
//                warm = MathUtils.clamp(warm * adj - 0x1.8p-10, -0.5, 0.5);
//                rr = yy
//                        | (int) ((warm + 0.5) * cwLim) << shift1
//                        | (int) ((mild + 0.5) * cmLim) << shift2;
//
////                cof = MathUtils.clamp(cof * 0.9375f, -0.5f, 0.5f);
////                cgf = MathUtils.clamp(cgf * 0.9375f, -0.5f, 0.5f);
////                rr = yy
////                        | (int) ((cof + 0.5f) * 63) << 7
////                        | (int) ((cgf + 0.5f) * 63) << 13;
//                if (--yy == 0) {
//                    match = -1;
//                }
//            }
//            if (match >= 0) {
//                for (int yy = y - 3, rr = rev - 3; yy > 0; yy--, rr--) {
//                    if ((idx2 = paletteMapping[rr] & 255) != match && difference(lumas[idx2], warms[idx2], milds[idx2], yf, warm, mild) > THRESHOLD) {
//                        ramps[i][3] = paletteMapping[rr];
//                        break;
//                    }
//                    adj = 1.0 + (yy - (yLim + 1 >>> 1)) * 0x1p-10;
//                    mild = MathUtils.clamp(mild * adj, -0.5, 0.5);
//                    warm = MathUtils.clamp(warm * adj - 0x1.8p-10, -0.5, 0.5);
//                    rr = yy
//                            | (int) ((warm + 0.5) * cwLim) << shift1
//                            | (int) ((mild + 0.5) * cmLim) << shift2;
//                }
//            }
//        }
//
//        System.out.println("public static final byte[][] LABRADOR256_RAMPS = new byte[][]{");
//        for (int i = 0; i < PALETTE.length; i++) {
//            System.out.println(
//                    "{ " + ramps[i][3]
//                            + ", " + ramps[i][2]
//                            + ", " + ramps[i][1]
//                            + ", " + ramps[i][0] + " },"
//            );
//        }
//        System.out.println("};");
//
//        System.out.println("public static final int[][] LABRADOR256_RAMP_VALUES = new int[][]{");
//        for (int i = 0; i < PALETTE.length; i++) {
//            System.out.println("{ 0x" + StringKit.hex(PALETTE[ramps[i][3] & 255])
//                    + ", 0x" + StringKit.hex(PALETTE[ramps[i][2] & 255])
//                    + ", 0x" + StringKit.hex(PALETTE[ramps[i][1] & 255])
//                    + ", 0x" + StringKit.hex(PALETTE[ramps[i][0] & 255]) + " },"
//            );
//        }
//        System.out.println("};");


        CIELABConverter cielab = new CIELABConverter();
        CIELABConverter.Lab lab1 = new CIELABConverter.Lab(), lab2 = new CIELABConverter.Lab();
        IntVLA base = new IntVLA(29 * 29 * 29);
//        IntVLA base = new IntVLA(52 * 52 * 52);
//        base.addAll(PALETTE, 1, PALETTE.length - 1);
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
//        for (int r = 0, rr = 0; r < 29; r++, rr += 0x05000000) {
//            for (int g = 0, gg = 0; g < 29; g++, gg += 0x050000) {
//                for (int b = 0, bb = 0; b < 29; b++, bb += 0x0500) {
        for (int r = 0, rr = 0; r < 9; r++) {
            rr = r * 32 - (r >>> 3) << 24;
            for (int g = 0, gg = 0; g < 9; g++) {
                gg = g * 32 - (g >>> 3) << 16;
                for (int b = 0, bb = 0; b < 9; b++) {
                    bb = b * 32 - (b >>> 3) << 8;
                    base.add(rr | gg | bb | 0xFF);
                }
            }
        }
        while (base.size > 256) {
            System.out.println(base.size);
            int ca = 0, cb = 1, cc, idx, color1, color2;
//            int t, d = 0xFFFFFFF;
            double t, d = 0x1p500;
            OUTER:
            for (int i = 0; i < base.size; i++) {
                color1 = base.get(i);
                lab1.fromRGBA(base.get(i));
                for (int j = i + 1; j < base.size; j++) {
                    color2 = base.get(j);
                    lab2.fromRGBA(base.get(j));
//                    if ((t = difference(color1, color2)) < d) {
                    if ((t = cielab.CIEDE2000(lab1, lab2)) < d) {
                        d = t;
                        ca = i;
                        cb = j;
                        if(d <= 0)
                            break OUTER;
                    }
                }
            }
            idx = cb;
            cc = base.get(ca);
            cb = base.get(cb);
            int ra = (cc >>> 24), ga = (cc >>> 16 & 0xFF), ba = (cc >>> 8 & 0xFF),
                    rb = (cb >>> 24), gb = (cb >>> 16 & 0xFF), bb = (cb >>> 8 & 0xFF);
//                    maxa = Math.max(ra, Math.max(ga, ba)), mina = Math.min(ra, Math.min(ga, ba)),
//                    maxb = Math.max(rb, Math.max(gb, bb)), minb = Math.min(rb, Math.min(gb, bb));
//            if (maxa - mina > 100)
//                base.set(cb, ca);
//            else if (maxb - minb > 100)
//                base.set(cb, t);
//            else
                base.set(ca,
                        (ra + rb + 1 << 23 & 0xFF000000)
                                | (ga + gb + 1 << 15 & 0xFF0000)
                                | (ba + bb + 1 << 7 & 0xFF00)
                                | 0xFF);
            base.removeIndex(idx);
        }
//        base.insert(0, 0);
////        System.arraycopy(grayscale, 0, base.items, 1, grayscale.length);
        int[] PALETTE = base.toArray();
//        
//        //// used for Uniform216 and SemiUniform256
//        // used for NonUniform256
//        PALETTE = new int[256];
//        PALETTE[1] = 0x3F3F3FFF;
//        PALETTE[2] = 0x7F7F7FFF;
//        PALETTE[3] = 0xBFBFBFFF;
//        int idx = 4;
//        for (int rr = 0; rr < 7; rr++) {
//            for (int gg = 0; gg < 9; gg++) {
//                for (int bb = 0; bb < 4; bb++) {
//                    PALETTE[idx++] = rr * 42 + (rr >> 1) << 24 | gg * 32 - (gg >> 3) << 16 | bb * 85 << 8 | 0xFF;
//                }
//            }
//        }
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
        for (int i = 0; i < (PALETTE.length + 7 >>> 3); i++) {
            for (int j = 0; j < 8 && (i << 3 | j) < PALETTE.length; j++) {
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
            png8.writePrecisely(Gdx.files.local("Labrador256.png"), pix, false);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Pixmap p2 = new Pixmap(1024, 32, Pixmap.Format.RGBA8888);
        for (int red = 0; red < 32; red++) {
            for (int blu = 0; blu < 32; blu++) {
                for (int gre = 0; gre < 32; gre++) {
                    p2.drawPixel(red << 5 | blu, gre, PALETTE[png8.palette.paletteMapping[
                            ((red << 10) & 0x7C00)
                                    | ((gre << 5) & 0x3E0)
                                    | blu] & 0xFF]);
                }
            }
        }

        try {
            png8.writePrecisely(Gdx.files.local("Labrador256_GLSL.png"), p2, false);
        } catch (IOException e) {
            e.printStackTrace();
        }

//        int[][] LABRADOR256_BONUS_RAMP_VALUES = new int[256][4];
//        for (int i = 1; i < PALETTE.length; i++) {
//            int color = LABRADOR256_BONUS_RAMP_VALUES[i | 128][2] = LABRADOR256_BONUS_RAMP_VALUES[i][2] =
//                    PALETTE[i];             
////            r = (color >>> 24);
////            g = (color >>> 16 & 0xFF);
////            b = (color >>> 8 & 0xFF);
//            luma = lumas[i];
//            warm = warms[i];
//            mild = milds[i];
//            LABRADOR256_BONUS_RAMP_VALUES[i | 64][1] = LABRADOR256_BONUS_RAMP_VALUES[i | 64][2] =
//                    LABRADOR256_BONUS_RAMP_VALUES[i | 64][3] = color;
//            LABRADOR256_BONUS_RAMP_VALUES[i | 192][0] = LABRADOR256_BONUS_RAMP_VALUES[i | 192][2] = color;
////            int co = r - b, t = b + (co >> 1), cg = g - t, y = t + (cg >> 1),
////                    yBright = y * 21 >> 4, yDim = y * 11 >> 4, yDark = y * 6 >> 4, chromO, chromG;
////            chromO = (co * 3) >> 2;
////            chromG = (cg * 3) >> 2;
////            t = yDim - (chromG >> 1);
////            g = chromG + t;
////            b = t - (chromO >> 1);
////            r = b + chromO;
//            r = MathUtils.clamp((int) ((luma * 0.83 + warm * 0.6) * 255.5), 0, 255);
//            g = MathUtils.clamp((int) ((luma * 0.83 + mild * 0.6) * 255.5), 0, 255);
//            b = MathUtils.clamp((int) ((luma * 0.83 - (warm + mild) * 0.3) * 255.5), 0, 255);
//            LABRADOR256_BONUS_RAMP_VALUES[i | 192][1] = LABRADOR256_BONUS_RAMP_VALUES[i | 128][1] =
//                    LABRADOR256_BONUS_RAMP_VALUES[i | 64][0] = LABRADOR256_BONUS_RAMP_VALUES[i][1] =
//                            MathUtils.clamp(r, 0, 255) << 24 |
//                                    MathUtils.clamp(g, 0, 255) << 16 |
//                                    MathUtils.clamp(b, 0, 255) << 8 | 0xFF;
//            r = MathUtils.clamp((int) ((luma * 1.2 + warm * 0.44) * 255.5), 0, 255);
//            g = MathUtils.clamp((int) ((luma * 1.2 + mild * 0.44) * 255.5), 0, 255);
//            b = MathUtils.clamp((int) ((luma * 1.2 - (warm + mild) * 0.22) * 255.5), 0, 255);
//            LABRADOR256_BONUS_RAMP_VALUES[i | 192][3] = LABRADOR256_BONUS_RAMP_VALUES[i | 128][3] =
//                    LABRADOR256_BONUS_RAMP_VALUES[i][3] =
//                            MathUtils.clamp(r, 0, 255) << 24 |
//                                    MathUtils.clamp(g, 0, 255) << 16 |
//                                    MathUtils.clamp(b, 0, 255) << 8 | 0xFF;
//            r = MathUtils.clamp((int) ((luma * 0.65 + warm * 0.5) * 255.5), 0, 255);
//            g = MathUtils.clamp((int) ((luma * 0.65 + mild * 0.5) * 255.5), 0, 255);
//            b = MathUtils.clamp((int) ((luma * 0.65 - (warm + mild) * 0.25) * 255.5), 0, 255);
//            LABRADOR256_BONUS_RAMP_VALUES[i | 128][0] = LABRADOR256_BONUS_RAMP_VALUES[i][0] =
//                    MathUtils.clamp(r, 0, 255) << 24 |
//                            MathUtils.clamp(g, 0, 255) << 16 |
//                            MathUtils.clamp(b, 0, 255) << 8 | 0xFF;
//        }
//        sb.setLength(0);
//        sb.ensureCapacity(2800);
//        sb.append("private static final int[][] LABRADOR256_BONUS_RAMP_VALUES = new int[][] {\n");
//        for (int i = 0; i < 256; i++) {
//            sb.append("{ 0x");
//            StringKit.appendHex(sb, LABRADOR256_BONUS_RAMP_VALUES[i][0]);
//            StringKit.appendHex(sb.append(", 0x"), LABRADOR256_BONUS_RAMP_VALUES[i][1]);
//            StringKit.appendHex(sb.append(", 0x"), LABRADOR256_BONUS_RAMP_VALUES[i][2]);
//            StringKit.appendHex(sb.append(", 0x"), LABRADOR256_BONUS_RAMP_VALUES[i][3]);
//            sb.append(" },\n");
//
//        }
//        System.out.println(sb.append("};"));
//        PALETTE = new int[256];
//        for (int i = 0; i < 64; i++) {
//            System.arraycopy(LABRADOR256_BONUS_RAMP_VALUES[i], 0, PALETTE, i << 2, 4);
//        }
//        sb.setLength(0);
//        sb.ensureCapacity((1 + 12 * 8) * (PALETTE.length >>> 3));
//        for (int i = 0; i < (PALETTE.length >>> 3); i++) {
//            for (int j = 0; j < 8; j++) {
//                sb.append("0x").append(StringKit.hex(PALETTE[i << 3 | j]).toUpperCase()).append(", ");
//            }
//            sb.append('\n');
//        }
//        System.out.println(sb.toString());
//        sb.setLength(0);
//
//        pix = new Pixmap(256, 1, Pixmap.Format.RGBA8888);
//        for (int i = 0; i < PALETTE.length - 1; i++) {
//            pix.drawPixel(i, 0, PALETTE[i + 1]);
//        }
//        //pix.drawPixel(255, 0, 0);
//        png8.palette = new PaletteReducer(PALETTE);
//        try {
//            png8.writePrecisely(Gdx.files.local("Labrador256Bonus.png"), pix, false);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        p2 = new Pixmap(1024, 32, Pixmap.Format.RGBA8888);
//        for (int red = 0; red < 32; red++) {
//            for (int blu = 0; blu < 32; blu++) {
//                for (int gre = 0; gre < 32; gre++) {
//                    p2.drawPixel(red << 5 | blu, gre, PALETTE[png8.palette.paletteMapping[
//                            ((red << 10) & 0x7C00)
//                                    | ((gre << 5) & 0x3E0)
//                                    | blu] & 0xFF]);
//                }
//            }
//        }
//        try {
//            png8.writePrecisely(Gdx.files.local("Labrador256Bonus_GLSL.png"), p2, false);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        pix = new Pixmap(256, 1, Pixmap.Format.RGBA8888);
//        for (int i = 1; i < 64; i++) {
////            pix.drawPixel(i-1, 0, PALETTE[i]);
//            pix.drawPixel(i-1, 0, PALETTE[i << 2 | 2]);
//            pix.drawPixel(i+63, 0, PALETTE[i << 2]);
//            pix.drawPixel(i+127, 0, PALETTE[i << 2 | 1]);
//            pix.drawPixel(i+191, 0, PALETTE[i << 2 | 3]);
//        }
//        png8.palette = new PaletteReducer(PALETTE);
//        try {
//            png8.writePrecisely(Gdx.files.local("Labrador256BonusMagicaVoxel.png"), pix, false);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        
        
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
