import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import squidpony.StringKit;

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
        int[] initial = {
                0x00000000, 0x444444ff, 0x000000ff, 0x88ffff00, 0x212121ff, 0x00ff00ff, 0x0000ffff, 0x080808ff,
                0xff574600, 0xffb14600, 0xfffd4600, 0x4bff4600, 0x51bf6c00, 0x4697ff00, 0x9146ff00, 0xff46ae00,
                0xffffffff, 0xeeeeeeff, 0xddddddff, 0xccccccff, 0xbbbbbbff, 0xaaaaaaff, 0x999999ff, 0x888888ff,
                0x777777ff, 0x666666ff, 0x555555ff, 0x444444ff, 0x333333ff, 0x222222ff, 0x111111ff, 0x000000ff,
                0xff978cfe, 0xff5746fe, 0xeb3623fe, 0xcc1c0afe, 0xa50f00fe, 0x720a00fe,
                0xffd0ccfe, 0xebb1abfe, 0xcc8b84fe, 0xa5655ffe, 0x79423cfe, 0x46211dfe,
                0xffce8cfe, 0xffb146fe, 0xeb9723fe, 0xcc7a0afe, 0xa56000fe, 0x724200fe,
                0xffe9ccfe, 0xebd0abfe, 0xccae84fe, 0xa5885ffe, 0x795f3cfe, 0x46351dfe,
                0xfffe8cfe, 0xfffd46fe, 0xebea23fe, 0xccca0afe, 0xa5a400fe, 0x727200fe,
                0xfff0d8fe, 0xf2d19dfe, 0xdfb46ffe, 0xbf9147fe, 0x996e2afe, 0x6c4b15fe,
                0x8fff8cfe, 0x4bff46fe, 0x29eb23fe, 0x10cc0afe, 0x05a500fe, 0x037200fe,
                0xcdffccfe, 0xadebabfe, 0x86cc84fe, 0x61a55ffe, 0x3e793cfe, 0x1f461dfe,
                0x8ecc9efe, 0x51bf6cfe, 0x33ac51fe, 0x1d923afe, 0x0f7929fe, 0x06591bfe,
                0xccfffdfe, 0xabebeafe, 0x84ccc9fe, 0x5fa5a3fe, 0x3c7977fe, 0x1d4644fe,
                0x8cbefffe, 0x4697fffe, 0x237bebfe, 0x0a5eccfe, 0x0048a5fe, 0x003272fe,
                0xcce2fffe, 0xabc7ebfe, 0x84a3ccfe, 0x5f7ea5fe, 0x3c5779fe, 0x1d2f46fe,
                0xba8cfffe, 0x9146fffe, 0x7423ebfe, 0x580accfe, 0x4300a5fe, 0x2e0072fe,
                0xe0ccfffe, 0xc5abebfe, 0xa184ccfe, 0x7b5fa5fe, 0x553c79fe, 0x2e1d46fe,
                0xff8cccfe, 0xff46aefe, 0xeb2394fe, 0xcc0a77fe, 0xa5005dfe, 0x720040fe,
                0xffcce8fe, 0xebabcffe, 0xcc84acfe, 0xa55f86fe, 0x793c5efe, 0x461d34fe,
        };
        System.arraycopy(initial, 0, PALETTE, 0, 128);
        System.arraycopy(initial, 0, PALETTE, 128, 128);
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
        Gdx.files.local("GeneratedPalette.txt").writeString(sbs, false);
        sb.setLength(0);
        for (int i = 7; i < 120; i++) {
            final int p = PALETTE[i];
            int diff = Integer.MAX_VALUE;
            for (int j = 7; j < 120; j++) {
                if(i == j) continue;
                diff = Math.min(difference2(p, PALETTE[j]), diff);
            }
            sb.append("0x").append(StringKit.hex(p)).append(' ').append(diff).append('\n');
        }
        Gdx.files.local("PaletteDifferences.txt").writeString(sb.toString(), false);
        Pixmap pix = new Pixmap(256, 1, Pixmap.Format.RGBA8888);
        for (int i = 0; i < 255; i++) {
            pix.drawPixel(i, 0, PALETTE[i+1]);
        }
        pix.drawPixel(255, 1, 0);
        PixmapIO.writePNG(Gdx.files.local("GeneratedPalette.png"), pix);
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

}
