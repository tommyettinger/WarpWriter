import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Pixmap;
import warpwriter.Coloring;
import warpwriter.PNG8;
import warpwriter.PaletteReducer;

import java.io.IOException;

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
//        int[] PALETTE = new int[256];
        int[] PALETTE = {
                0x00000000,
                0x010101FF,
                0x131313FF,
                0x252525FF,
                0x373737FF,
                0x494949FF,
                0x5B5B5BFF,
                0x6E6E6EFF,
                0x808080FF,
                0x929292FF,
                0xA4A4A4FF,
                0xB6B6B6FF,
                0xC9C9C9FF,
                0xDBDBDBFF,
                0xEDEDEDFF,
                0xFFFFFFFF,
                0x007F7FFF,
                0x3FBFBFFF,
                0x00FFFFFF,
                0xBFFFFFFF,
                0x8181FFFF,
                0x0000FFFF,
                0x3F3FBFFF,
                0x00007FFF,
                0x0F0F50FF,
                0x7F007FFF,
                0xBF3FBFFF,
                0xF500F5FF,
                0xFD81FFFF,
                0xFFC0CBFF,
                0xFF8181FF,
                0xFF0000FF,
                0xBF3F3FFF,
                0x7F0000FF,
                0x551414FF,
                0x7F3F00FF,
                0xBF7F3FFF,
                0xFF7F00FF,
                0xFFBF81FF,
                0xFFFFBFFF,
                0xFFFF00FF,
                0xBFBF3FFF,
                0x7F7F00FF,
                0x007F00FF,
                0x3FBF3FFF,
                0x00FF00FF,
                0xAFFFAFFF,
                0xBCAFC0FF,
                0xCBAA89FF,
                0xA6A090FF,
                0x7E9494FF,
                0x6E8287FF,
                0x7E6E60FF,
                0xA0695FFF,
                0xC07872FF,
                0xD08A74FF,
                0xE19B7DFF,
                0xEBAA8CFF,
                0xF5B99BFF,
                0xF6C8AFFF,
                0xF5E1D2FF,
                0x573B3BFF,
                0x73413CFF,
                0x8E5555FF,
                0xAB7373FF,
                0xC78F8FFF,
                0xE3ABABFF,
                0xF8D2DAFF,
                0xE3C7ABFF,
                0xC49E73FF,
                0x8F7357FF,
                0x73573BFF,
                0x3B2D1FFF,
                0x414123FF,
                0x73733BFF,
                0x8F8F57FF,
                0xA2A255FF,
                0xB5B572FF,
                0xC7C78FFF,
                0xDADAABFF,
                0xEDEDC7FF,
                0xC7E3ABFF,
                0xABC78FFF,
                0x8EBE55FF,
                0x738F57FF,
                0x587D3EFF,
                0x465032FF,
                0x191E0FFF,
                0x235037FF,
                0x3B573BFF,
                0x506450FF,
                0x3B7349FF,
                0x578F57FF,
                0x73AB73FF,
                0x64C082FF,
                0x8FC78FFF,
                0xA2D8A2FF,
                0xE1F8FAFF,
                0xB4EECAFF,
                0xABE3C5FF,
                0x87B48EFF,
                0x507D5FFF,
                0x0F6946FF,
                0x1E2D23FF,
                0x234146FF,
                0x3B7373FF,
                0x64ABABFF,
                0x8FC7C7FF,
                0xABE3E3FF,
                0xC7F1F1FF,
                0xBED2F0FF,
                0xABC7E3FF,
                0xA8B9DCFF,
                0x8FABC7FF,
                0x578FC7FF,
                0x57738FFF,
                0x3B5773FF,
                0x0F192DFF,
                0x1F1F3BFF,
                0x3B3B57FF,
                0x494973FF,
                0x57578FFF,
                0x736EAAFF,
                0x7676CAFF,
                0x8F8FC7FF,
                0xABABE3FF,
                0xD0DAF8FF,
                0xE3E3FFFF,
                0xAB8FC7FF,
                0x8F57C7FF,
                0x73578FFF,
                0x573B73FF,
                0x3C233CFF,
                0x463246FF,
                0x724072FF,
                0x8F578FFF,
                0xAB57ABFF,
                0xAB73ABFF,
                0xEBACE1FF,
                0xFFDCF5FF,
                0xE3C7E3FF,
                0xE1B9D2FF,
                0xD7A0BEFF,
                0xC78FB9FF,
                0xC87DA0FF,
                0xC35A91FF,
                0x4B2837FF,
                0x321623FF,
                0x280A1EFF,
                0x401811FF,
                0x621800FF,
                0xA5140AFF,
                0xDA2010FF,
                0xD5524AFF,
                0xFF3C0AFF,
                0xF55A32FF,
                0xFF6262FF,
                0xF6BD31FF,
                0xFFA53CFF,
                0xD79B0FFF,
                0xDA6E0AFF,
                0xB45A00FF,
                0xA04B05FF,
                0x5F3214FF,
                0x53500AFF,
                0x626200FF,
                0x8C805AFF,
                0xAC9400FF,
                0xB1B10AFF,
                0xE6D55AFF,
                0xFFD510FF,
                0xFFEA4AFF,
                0xC8FF41FF,
                0x9BF046FF,
                0x96DC19FF,
                0x73C805FF,
                0x6AA805FF,
                0x3C6E14FF,
                0x283405FF,
                0x204608FF,
                0x0C5C0CFF,
                0x149605FF,
                0x0AD70AFF,
                0x14E60AFF,
                0x7DFF73FF,
                0x4BF05AFF,
                0x00C514FF,
                0x05B450FF,
                0x1C8C4EFF,
                0x123832FF,
                0x129880FF,
                0x06C491FF,
                0x00DE6AFF,
                0x2DEBA8FF,
                0x3CFEA5FF,
                0x6AFFCDFF,
                0x91EBFFFF,
                0x55E6FFFF,
                0x7DD7F0FF,
                0x08DED5FF,
                0x109CDEFF,
                0x055A5CFF,
                0x162C52FF,
                0x0F377DFF,
                0x004A9CFF,
                0x326496FF,
                0x0052F6FF,
                0x186ABDFF,
                0x2378DCFF,
                0x699DC3FF,
                0x4AA4FFFF,
                0x90B0FFFF,
                0x5AC5FFFF,
                0xBEB9FAFF,
                0x00BFFFFF,
                0x007FFFFF,
                0x4B7DC8FF,
                0x786EF0FF,
                0x4A5AFFFF,
                0x6241F6FF,
                0x3C3CF5FF,
                0x101CDAFF,
                0x0010BDFF,
                0x231094FF,
                0x0C2148FF,
                0x5010B0FF,
                0x6010D0FF,
                0x8732D2FF,
                0x9C41FFFF,
                0x7F00FFFF,
                0xBD62FFFF,
                0xB991FFFF,
                0xD7A5FFFF,
                0xD7C3FAFF,
                0xF8C6FCFF,
                0xE673FFFF,
                0xFF52FFFF,
                0xDA20E0FF,
                0xBD29FFFF,
                0xBD10C5FF,
                0x8C14BEFF,
                0x5A187BFF,
                0x641464FF,
                0x410062FF,
                0x320A46FF,
                0x551937FF,
                0xA01982FF,
                0xC80078FF,
                0xFF50BFFF,
                0xFF6AC5FF,
                0xFAA0B9FF,
                0xFC3A8CFF,
                0xE61E78FF,
                0xBD1039FF,
                0x98344DFF,
                0x911437FF,
        };
        int[][] AURORA = {
                 {0, 0, 0, 0}
                ,{1, 17, 17, 17}
                ,{2, 34, 34, 34}
                ,{3, 51, 51, 51}
                ,{4, 68, 68, 68}
                ,{5, 85, 85, 85}
                ,{6, 102, 102, 102}
                ,{7, 119, 119, 119}
                ,{8, 136, 136, 136}
                ,{9, 153, 153, 153}
                ,{10, 170, 170, 170}
                ,{11, 187, 187, 187}
                ,{12, 204, 204, 204}
                ,{13, 221, 221, 221}
                ,{14, 238, 238, 238}
                ,{15, 255, 255, 255}
                ,{16, 0, 127, 127}
                ,{17, 63, 191, 191}
                ,{18, 0, 255, 255}
                ,{19, 191, 255, 255}
                ,{20, 129, 129, 255}
                ,{21, 0, 0, 255}
                ,{22, 63, 63, 191}
                ,{23, 0, 0, 127}
                ,{24, 15, 15, 80}
                ,{25, 127, 0, 127}
                ,{26, 191, 63, 191}
                ,{27, 245, 0, 245}
                ,{28, 253, 129, 255}
                ,{29, 255, 192, 203}
                ,{30, 255, 129, 129}
                ,{31, 255, 0, 0}
                ,{32, 191, 63, 63}
                ,{33, 127, 0, 0}
                ,{34, 85, 20, 20}
                ,{35, 127, 63, 0}
                ,{36, 191, 127, 63}
                ,{37, 255, 127, 0}
                ,{38, 255, 191, 129}
                ,{39, 255, 255, 191}
                ,{40, 255, 255, 0}
                ,{41, 191, 191, 63}
                ,{42, 127, 127, 0}
                ,{43, 0, 127, 0}
                ,{44, 63, 191, 63}
                ,{45, 0, 255, 0}
                ,{46, 175, 255, 175}
                ,{47, 0, 191, 255}
                ,{48, 0, 127, 255}
                ,{49, 75, 125, 200}
                ,{50, 188, 175, 192}
                ,{51, 203, 170, 137}
                ,{52, 166, 160, 144}
                ,{53, 126, 148, 148}
                ,{54, 110, 130, 135}
                ,{55, 126, 110, 96}
                ,{56, 160, 105, 95}
                ,{57, 192, 120, 114}
                ,{58, 208, 138, 116}
                ,{59, 225, 155, 125}
                ,{60, 235, 170, 140}
                ,{61, 245, 185, 155}
                ,{62, 246, 200, 175}
                ,{63, 245, 225, 210}
                ,{64, 127, 0, 255}
                ,{65, 87, 59, 59}
                ,{66, 115, 65, 60}
                ,{67, 142, 85, 85}
                ,{68, 171, 115, 115}
                ,{69, 199, 143, 143}
                ,{70, 227, 171, 171}
                ,{71, 248, 210, 218}
                ,{72, 227, 199, 171}
                ,{73, 196, 158, 115}
                ,{74, 143, 115, 87}
                ,{75, 115, 87, 59}
                ,{76, 59, 45, 31}
                ,{77, 65, 65, 35}
                ,{78, 115, 115, 59}
                ,{79, 143, 143, 87}
                ,{80, 162, 162, 85}
                ,{81, 181, 181, 114}
                ,{82, 199, 199, 143}
                ,{83, 218, 218, 171}
                ,{84, 237, 237, 199}
                ,{85, 199, 227, 171}
                ,{86, 171, 199, 143}
                ,{87, 142, 190, 85}
                ,{88, 115, 143, 87}
                ,{89, 88, 125, 62}
                ,{90, 70, 80, 50}
                ,{91, 25, 30, 15}
                ,{92, 35, 80, 55}
                ,{93, 59, 87, 59}
                ,{94, 80, 100, 80}
                ,{95, 59, 115, 73}
                ,{96, 87, 143, 87}
                ,{97, 115, 171, 115}
                ,{98, 100, 192, 130}
                ,{99, 143, 199, 143}
                ,{100, 162, 216, 162}
                ,{101, 225, 248, 250}
                ,{102, 180, 238, 202}
                ,{103, 171, 227, 197}
                ,{104, 135, 180, 142}
                ,{105, 80, 125, 95}
                ,{106, 15, 105, 70}
                ,{107, 30, 45, 35}
                ,{108, 35, 65, 70}
                ,{109, 59, 115, 115}
                ,{110, 100, 171, 171}
                ,{111, 143, 199, 199}
                ,{112, 171, 227, 227}
                ,{113, 199, 241, 241}
                ,{114, 190, 210, 240}
                ,{115, 171, 199, 227}
                ,{116, 168, 185, 220}
                ,{117, 143, 171, 199}
                ,{118, 87, 143, 199}
                ,{119, 87, 115, 143}
                ,{120, 59, 87, 115}
                ,{121, 15, 25, 45}
                ,{122, 31, 31, 59}
                ,{123, 59, 59, 87}
                ,{124, 73, 73, 115}
                ,{125, 87, 87, 143}
                ,{126, 115, 110, 170}
                ,{127, 118, 118, 202}
                ,{128, 143, 143, 199}
                ,{129, 171, 171, 227}
                ,{130, 208, 218, 248}
                ,{131, 227, 227, 255}
                ,{132, 171, 143, 199}
                ,{133, 143, 87, 199}
                ,{134, 115, 87, 143}
                ,{135, 87, 59, 115}
                ,{136, 60, 35, 60}
                ,{137, 70, 50, 70}
                ,{138, 114, 64, 114}
                ,{139, 143, 87, 143}
                ,{140, 171, 87, 171}
                ,{141, 171, 115, 171}
                ,{142, 235, 172, 225}
                ,{143, 255, 220, 245}
                ,{144, 227, 199, 227}
                ,{145, 225, 185, 210}
                ,{146, 215, 160, 190}
                ,{147, 199, 143, 185}
                ,{148, 200, 125, 160}
                ,{149, 195, 90, 145}
                ,{150, 75, 40, 55}
                ,{151, 50, 22, 35}
                ,{152, 40, 10, 30}
                ,{153, 64, 24, 17}
                ,{154, 98, 24, 0}
                ,{155, 165, 20, 10}
                ,{156, 218, 32, 16}
                ,{157, 213, 82, 74}
                ,{158, 255, 60, 10}
                ,{159, 245, 90, 50}
                ,{160, 255, 98, 98}
                ,{161, 246, 189, 49}
                ,{162, 255, 165, 60}
                ,{163, 215, 155, 15}
                ,{164, 218, 110, 10}
                ,{165, 180, 90, 0}
                ,{166, 160, 75, 5}
                ,{167, 95, 50, 20}
                ,{168, 83, 80, 10}
                ,{169, 98, 98, 0}
                ,{170, 140, 128, 90}
                ,{171, 172, 148, 0}
                ,{172, 177, 177, 10}
                ,{173, 230, 213, 90}
                ,{174, 255, 213, 16}
                ,{175, 255, 234, 74}
                ,{176, 200, 255, 65}
                ,{177, 155, 240, 70}
                ,{178, 150, 220, 25}
                ,{179, 115, 200, 5}
                ,{180, 106, 168, 5}
                ,{181, 60, 110, 20}
                ,{182, 40, 52, 5}
                ,{183, 32, 70, 8}
                ,{184, 12, 92, 12}
                ,{185, 20, 150, 5}
                ,{186, 10, 215, 10}
                ,{187, 20, 230, 10}
                ,{188, 125, 255, 115}
                ,{189, 75, 240, 90}
                ,{190, 0, 197, 20}
                ,{191, 5, 180, 80}
                ,{192, 28, 140, 78}
                ,{193, 18, 56, 50}
                ,{194, 18, 152, 128}
                ,{195, 6, 196, 145}
                ,{196, 0, 222, 106}
                ,{197, 45, 235, 168}
                ,{198, 60, 254, 165}
                ,{199, 106, 255, 205}
                ,{200, 145, 235, 255}
                ,{201, 85, 230, 255}
                ,{202, 125, 215, 240}
                ,{203, 8, 222, 213}
                ,{204, 16, 156, 222}
                ,{205, 5, 90, 92}
                ,{206, 22, 44, 82}
                ,{207, 15, 55, 125}
                ,{208, 0, 74, 156}
                ,{209, 50, 100, 150}
                ,{210, 0, 82, 246}
                ,{211, 24, 106, 189}
                ,{212, 35, 120, 220}
                ,{213, 105, 157, 195}
                ,{214, 74, 164, 255}
                ,{215, 144, 176, 255}
                ,{216, 90, 197, 255}
                ,{217, 190, 185, 250}
                ,{218, 120, 110, 240}
                ,{219, 74, 90, 255}
                ,{220, 98, 65, 246}
                ,{221, 60, 60, 245}
                ,{222, 16, 28, 218}
                ,{223, 0, 16, 189}
                ,{224, 35, 16, 148}
                ,{225, 12, 33, 72}
                ,{226, 80, 16, 176}
                ,{227, 96, 16, 208}
                ,{228, 135, 50, 210}
                ,{229, 156, 65, 255}
                ,{230, 189, 98, 255}
                ,{231, 185, 145, 255}
                ,{232, 215, 165, 255}
                ,{233, 215, 195, 250}
                ,{234, 248, 198, 252}
                ,{235, 230, 115, 255}
                ,{236, 255, 82, 255}
                ,{237, 218, 32, 224}
                ,{238, 189, 41, 255}
                ,{239, 189, 16, 197}
                ,{240, 140, 20, 190}
                ,{241, 90, 24, 123}
                ,{242, 100, 20, 100}
                ,{243, 65, 0, 98}
                ,{244, 50, 10, 70}
                ,{245, 85, 25, 55}
                ,{246, 160, 25, 130}
                ,{247, 200, 0, 120}
                ,{248, 255, 80, 191}
                ,{249, 255, 106, 197}
                ,{250, 250, 160, 185}
                ,{251, 252, 58, 140}
                ,{252, 230, 30, 120}
                ,{253, 189, 16, 57}
                ,{254, 152, 52, 77}
                ,{255, 145, 20, 55}
        };

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
//        System.arraycopy(Coloring.AURORA, 0, PALETTE, 0, 256);
//        IntIntOrderedMap hueToIndex = new IntIntOrderedMap(32);
//        for (int i = 18, s = 16; i < 256; i += 8, s += 8) {
//            hueToIndex.put((int)(hue(Coloring.RINSED[i]) * 1024), s);
//        }
//        TreeSet<IntIntOrderedMap.MapEntry> sorted = new TreeSet<>(new Comparator<IntIntOrderedMap.MapEntry>(){
//            /**
//             * Compares its two arguments for order.  Returns a negative integer,
//             * zero, or a positive integer as the first argument is less than, equal
//             * to, or greater than the second.<p>
//             */
//            @Override
//            public int compare(IntIntOrderedMap.MapEntry o1, IntIntOrderedMap.MapEntry o2) {
//                return o1.getKey() - o2.getKey();
//            }
//        });
//        sorted.addAll(hueToIndex.entrySet());
//        System.out.println(sorted);
//        int idx = 16;
//        for (IntIntOrderedMap.MapEntry ent : sorted) {
//            System.arraycopy(Coloring.RINSED, ent.getValue(), PALETTE, idx, 8);
//            idx += 8;
//        }
        
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

//        for (int i = 0; i < 256; i++) {
//            PALETTE[i] = (AURORA[i][1] << 24) | (AURORA[i][2] << 16) | (AURORA[i][3] << 8) | 0xFF;
//        }

//        StringBuilder sb = new StringBuilder((1 + 12 * 8) * 32);
//        for (int i = 0; i < 32; i++) {
//            for (int j = 0; j < 8; j++) {
//                sb.append(StringKit.hex(PALETTE[i << 3 | j]), 0, 6).append('\n');
//            }
//        }
//        String sbs = sb.toString();
//        System.out.println(sbs);
//        Gdx.files.local("DawnBringer_Aurora_Official.hex").writeString(sbs, false);
//        sb.setLength(0);

//        StringBuilder sb = new StringBuilder((1 + 12 * 8) * 32);
//        for (int i = 0; i < 32; i++) {
//            for (int j = 0; j < 8; j++) {
//                sb.append("0x").append(StringKit.hex(PALETTE[i << 3 | j]).toUpperCase()).append(", ");
//            }
//            sb.append('\n');
//        }
//        String sbs = sb.toString();
//        System.out.println(sbs);
//        //Gdx.files.local("GeneratedPalette.txt").writeString(sbs, false);
//        sb.setLength(0);

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
//        Pixmap pix = new Pixmap(128, 128, Pixmap.Format.RGBA8888);
//        for (int i = 0; i < 256; i++) {
//            pix.setColor(PALETTE[i]);
//            pix.fillRectangle((i & 15) << 3, (i & -16) >>> 1, 8, 8);
//        }
        PALETTE = Coloring.UNSEVEN;
        Pixmap pix = new Pixmap(256, 1, Pixmap.Format.RGBA8888);
        for (int i = 0; i < PALETTE.length - 1; i++) {
            pix.drawPixel(i, 0, PALETTE[i+1]);
        }
        //pix.drawPixel(255, 0, 0);
        PNG8 png8 = new PNG8();
        png8.palette = new PaletteReducer(PALETTE);
//        try {
//            png8.writePrecisely(Gdx.files.local("Unseven.png"), pix, false);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

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
            png8.writePrecisely(Gdx.files.local("Unseven_GLSL.png"), p2, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
//        Pixmap pm = new Pixmap(Gdx.files.internal("BlueNoiseBW64.png"));
//        final byte[] data = new byte[64 * 64];
//        int n = 0;
//        for (int x = 0; x < 64; x++) {
//            for (int y = 0; y < 64; y++) {
//                data[n++] = (byte)(pm.getPixel(x, y));
//            }
//        }
//        PaletteReducer.print(data);
        //Gdx.app.exit();
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
