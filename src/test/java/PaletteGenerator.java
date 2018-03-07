import com.badlogic.gdx.graphics.Color;
import warpwriter.Coloring;

/**
 * Created by Tommy Ettinger on 1/21/2018.
 */
public class PaletteGenerator {
    public static void main(String[] args) {
        int[] PALETTE = new int[256];
        System.arraycopy(Coloring.CW_PALETTE, 0, PALETTE, 0, 7);
        Color temp = Color.WHITE.cpy();
        float[] hsv = new float[3];
        for (int i = 0; i < 9; i++) {
            PALETTE[7 + i] = Color.rgba8888(temp.fromHsv(0, 0, 0.06f + 0.11f * i));
        }
//        float[] hues = {0.0f, 0.07179487f, 0.07749468f, 0.098445594f, 0.09782606f, 0.14184391f, 0.16522992f,
//                0.20281118f, 0.20285714f, 0.21867621f, 0.25163394f, 0.3141666f, 0.3715499f, 0.37061405f, 0.44054055f,
//                0.49561405f, 0.53289473f, 0.53312635f, 0.5931374f, 0.6494253f, 0.7192119f, 0.7562056f, 0.7564103f,
//                0.8037036f, 0.8703703f, 0.9282946f, 0.92884994f};

//        float[] sats = {0.863354f, 0.2742616f, 0.8051282f, 0.7751004f, 0.519774f, 0.7768595f, 0.46031743f, 0.36086953f,
//                        0.9067358f, 0.94630873f, 0.20731705f, 0.91324204f, 0.6946903f, 0.4691358f, 0.74596775f,
//                        0.47698745f, 0.38f, 0.8846154f, 0.86624205f, 0.25777775f, 0.9575472f, 0.81385285f, 0.6453901f,
//                        0.746888f, 0.46153846f, 0.48863637f, 0.9395605f};
        float hue, sat;
        for (int i = 0; i < 21; i++) {
            Color.rgba8888ToColor(temp, Coloring.CW_PALETTE[21 + i * 8]);
            temp.toHsv(hsv);
            hue = hsv[0];
            sat = hsv[1];
//            sat = 0.5f * (sats[i] + 0.625f);
//            hue = hues[i] * 360;
//            sat = sats[i];
            for (int j = 0; j < 8; j++) {
                PALETTE[16 + j + i * 8] = Color.rgba8888(temp.fromHsv(hue, sat + 0.0175f * (45f - (j+1) * (j+1)), 0.125f + 0.125f * j));
            }
        }
        for (int i = 0; i < 16; i++) {
            PALETTE[184 + i] = i * 0x10101000 | 0xFF;
        }
        System.arraycopy(PALETTE, 16 + 4 * 8, PALETTE, 208, 8);
        System.arraycopy(PALETTE, 16 + 8, PALETTE, 216, 8);
        System.arraycopy(PALETTE, 16 + 2 * 8, PALETTE, 224, 8);
        System.arraycopy(PALETTE, 16, PALETTE, 232, 8);
        for (int i = 0; i < 32; i++) {
            for (int j = 0; j < 8; j++) {
                System.out.printf("0x%08X, ", PALETTE[i << 3 | j]);
            }
            System.out.println();
        }
    }
}
