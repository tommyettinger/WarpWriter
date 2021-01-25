import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import warpwriter.Coloring;
import warpwriter.PaletteReducer;
import warpwriter.Tools3D;
import warpwriter.VoxIO;
import warpwriter.model.color.Colorizer;

public class ColorSolidGenerator extends ApplicationAdapter {
    public static void main(String[] arg) {
        final ColorSolidGenerator app = new ColorSolidGenerator();
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.disableAudio(true);
        new Lwjgl3Application(app, config);
    }

    private PaletteReducer reducer;
    @Override
    public void create() {
        byte[][][] sparse = new byte[96][96][96], dense = new byte[32][32][32];
        Gdx.files.local("ColorSolids/").mkdirs();
        reducer = new PaletteReducer();
        for (int x = 0; x < 32; x++) {
            for (int y = 0; y < 32; y++) {
                for (int z = 0; z < 32; z++) {
                    dense[x][y][z] = reducer.paletteMapping[x << 10 | y << 5 | z];
                }
            }
        }
        VoxIO.writeVOX("ColorSolids/AuroraColorSolid.vox", dense, reducer.paletteArray);
        for (int i = 0; i < 256; i++) {
            int rgba =  reducer.paletteArray[i];
            if((rgba & 0xFF) == 0)
                continue;
            sparse[(rgba >>> 27)*3+1][(rgba >>> 19 & 31)*3+1][(rgba >>> 11 & 31)*3+1] = (byte) i;
        }
        VoxIO.writeVOX("ColorSolids/AuroraColorSpots.vox", sparse, reducer.paletteArray);
//
//        {
//            Tools3D.fill(sparse, 0);
//            Random random = new Random(1);
//            Color color = new Color(0f, 0f, 0f, 1f);
//            for (int i = 0; i < 4096; i++) {
//                int x = random.nextInt(96);
//                int y = random.nextInt(96);
//                int z = random.nextInt(96);
//                color.set(x / 95f,
//                        y / 95f,
//                        z / 95f, 1f);
//                sparse[x][y][z] = reducer.reduceIndex(Color.rgba8888(color));
//            }
//            VoxIO.writeVOX("JavaUtilRandom.vox", sparse, reducer.paletteArray);
//        }
//
//        {
//            Tools3D.fill(sparse, 0);
//            LightRNG random = new LightRNG(1);
//            Color color = new Color(0f, 0f, 0f, 1f);
//            for (int i = 0; i < 4096; i++) {
//                int x = random.nextInt(96);
//                int y = random.nextInt(96);
//                int z = random.nextInt(96);
//                color.set(x / 95f,
//                        y / 95f,
//                        z / 95f, 1f);
//                sparse[x][y][z] = reducer.reduceIndex(Color.rgba8888(color));
//            }
//            VoxIO.writeVOX("JavaUtilSplittableRandom.vox", sparse, reducer.paletteArray);
//        }
//
        {
            reducer.exact(Coloring.WEBSAFE);
            Tools3D.fill(sparse, 0);
            Color color = new Color(0f, 0f, 0f, 1f);
            double l, m, s, i, p, t;
            int limit = 500;
            float mul = 1f / ((limit - 1f) * (limit - 1f));
            for (int x = 0; x < limit; x++) {
                for (int y = 0; y < limit; y++) {
                    for (int z = 0; z < limit; z++) {
                        color.set(x * x * mul, y * y * mul, z * z * mul, 1f);
                        l = 0.313921 * color.r + 0.639468 * color.g + 0.0465970 * color.b;
                        m = 0.151693 * color.r + 0.748209 * color.g + 0.1000044 * color.b;
                        s = 0.017700 * color.r + 0.109400 * color.g + 0.8729000 * color.b;

                        //0.4000, 4.4550, 0.8056, 0.4000, 4.8510, 0.3572, 0.2000, 0.3960, 1.1628
                        // original, 0 - 1, -1 - 1, -1 - 1 
                        i = +0.4000 * l +0.4000 * m +0.2000 * s;
                        p = +4.4550 * l -4.8510 * m +0.3960 * s;
                        t = +0.8056 * l +0.3572 * m -1.1628 * s;
                        // half-range
                        //i = 0.4000 * l +0.4000 * m +0.2000 * s;
                        //p = 2.2275 * l -2.4255 * m +0.1980 * s;
                        //t = 0.4028 * l +0.1786 * m -0.5814 * s;

//                        i = 0.4000 * l + 0.4000 * m + 0.2000 * s;
//                        p = 6.6825 * l - 7.2765 * m + 0.5940 * s;
//                        t = 1.0741 * l + 0.4763 * m - 1.5504 * s;

                        sparse[(int)(p * 47.5 + 48)][(int)(t * 47.5 + 48)][(int)(95.5 * i)] = reducer.reduceIndex(Color.rgba8888(color));

                    }
                }
            }
            VoxIO.writeVOX("ColorSolids/IPT_No_Pow.vox", sparse, reducer.paletteArray);
//            VoxIO.writeVOX("ColorSolids/IPT_Original.vox", sparse, reducer.paletteArray);
//            Gdx.app.exit();
//            System.exit(0);
        }
        {
            reducer.exact(Coloring.WEBSAFE);
            Tools3D.fill(sparse, 0);
            Color color = new Color(0f, 0f, 0f, 1f);
            double r, g, b, x, y, z, L, A, B;
            int limit = 600;
            float mul = 1f / ((limit - 1f) * (limit - 1f));
            for (int xx = 0; xx < limit; xx++) {
                for (int yy = 0; yy < limit; yy++) {
                    for (int zz = 0; zz < limit; zz++) {
                        color.set(xx * xx * mul, yy * yy * mul, zz * zz * mul, 1f);
                        r = color.r;
                        g = color.g;
                        b = color.b;
                        
                        r = ((r > 0.04045) ? Math.pow((r + 0.055) / 1.055, 2.4) : r / 12.92);
                        g = ((g > 0.04045) ? Math.pow((g + 0.055) / 1.055, 2.4) : g / 12.92);
                        b = ((b > 0.04045) ? Math.pow((b + 0.055) / 1.055, 2.4) : b / 12.92);

                        x = (r * 0.4124 + g * 0.3576 + b * 0.1805) / 0.950489;
                        y = (r * 0.2126 + g * 0.7152 + b * 0.0722) / 1.000000;
                        z = (r * 0.0193 + g * 0.1192 + b * 0.9505) / 1.088840;

                        x = (x > 0.008856) ? Math.cbrt(x) : (7.787037037037037 * x) + 0.13793103448275862;
                        y = (y > 0.008856) ? Math.cbrt(y) : (7.787037037037037 * y) + 0.13793103448275862;
                        z = (z > 0.008856) ? Math.cbrt(z) : (7.787037037037037 * z) + 0.13793103448275862;

                        L = (116.0 * y) - 16.0;
                        A = 600.0 * (x - y);
                        B = 210.0 * (y - z);

                        sparse[(int)(A * 0.39 + 48)][(int)(B * 0.39 + 48)][(int)(L * 0.959)] = reducer.reduceIndex(Color.rgba8888(color));
                    }
                }
            }
            VoxIO.writeVOX("ColorSolids/LAB.vox", sparse, reducer.paletteArray);
        }
        {
            reducer.exact(Coloring.WEBSAFE);
            Tools3D.fill(sparse, 0);
            Color color = new Color(0f, 0f, 0f, 1f);
            double r, g, b, x, y, z, L, A, B;
            int limit = 600;
            float mul = 1f / ((limit - 1f) * (limit - 1f));
            for (int xx = 0; xx < limit; xx++) {
                for (int yy = 0; yy < limit; yy++) {
                    for (int zz = 0; zz < limit; zz++) {
                        color.set(xx * xx * mul, yy * yy * mul, zz * zz * mul, 1f);
                        r = color.r;
                        g = color.g;
                        b = color.b;

                        r *= r;
                        g *= g;
                        b *= b;

                        final double l = Math.cbrt(0.4121656120f * r + 0.5362752080f * g + 0.0514575653f * b);
                        final double m = Math.cbrt(0.2118591070f * r + 0.6807189584f * g + 0.1074065790f * b);
                        final double s = Math.cbrt(0.0883097947f * r + 0.2818474174f * g + 0.6302613616f * b);

                        //+0.2104542553 +0.7936177850 -0.0040720468
                        //+1.9779984951 -2.4285922050 +0.4505937099
                        //+0.0259040371 +0.7827717662 -0.8086757660
                        L = l * +0.2104542553 + m * +0.7936177850 + s * -0.0040720468;
                        A = l * +1.9779984951 + m * -2.4285922050 + s * +0.4505937099;
                        B = l * +0.0259040371 + m * +0.7827717662 + s * -0.8086757660;

                        sparse[(int)(A * 95 + 48)][(int)(B * 95 + 48)][(int)(L * 95)] = reducer.reduceIndex(Color.rgba8888(color));
                    }
                }
            }
            VoxIO.writeVOX("ColorSolids/Oklab.vox", sparse, reducer.paletteArray);
        }
        generate("Flesurrect", Coloring.FLESURRECT);
        generate("FlesurrectBonus", Colorizer.FlesurrectBonusPalette);
        generate("Rinsed", Coloring.RINSED);
        generate("VGA256", Coloring.VGA256);
        generate("Twirl64", Coloring.TWIRL64);
        generate("Twirl256", Coloring.TWIRL256);
        generate("Tincture64", new int[] {
                0x00000000, 0x29071aff, 0x0c220dff, 0x332832ff, 0x2f3d26ff, 0x595332ff, 0x46677fff, 0x8e8580ff,
                0x69b0e9ff, 0xd19a8fff, 0xe9d5e1ff, 0xd4f2b1ff, 0x39784dff, 0x56b8b1ff, 0x61f1f0ff, 0x536eeeff,
                0x0000ffff, 0x3245e1ff, 0x300b53ff, 0x700e79ff, 0xd42decff, 0xf866fcff, 0xfe7e89ff, 0xef2309ff,
                0xd34b45ff, 0x6d1e1fff, 0x7e3f10ff, 0xb1744cff, 0xff7f00ff, 0xf0c15aff, 0xe3f727ff, 0xc2b43aff,
                0x87975aff, 0x1f8e29ff, 0x2bd318ff, 0x00ff00ff, 0xc76d8bff, 0x713f5dff, 0x9e5322ff, 0x7a6831ff,
                0x75d4baff, 0x47c182ff, 0x1f532dff, 0x6ca944ff, 0x1f498cff, 0x936198ff, 0xeba2dcff, 0xc185dcff,
                0xaf1a2bff, 0xfc5b4dff, 0xe59c33ff, 0xda6e0aff, 0xac9400ff, 0xefd935ff, 0x56ee73ff, 0xa9db3fff,
                0x29988eff, 0x222e79ff, 0x2c1ab5ff, 0x9a29c3ff, 0x7f00ffff, 0x963074ff, 0xda1571ff, 0xf557a3ff,
        });
        generate("Tincture256", new int[] {
                0x00000000, 0x0c0510ff, 0x0f1b12ff, 0x242418ff, 0x334041ff, 0x534d41ff, 0x6a6777ff, 0x856b68ff,
                0x9f8283ff, 0xa49a95ff, 0xb2abb4ff, 0xbbb7deff, 0xc7c8c8ff, 0xdbddbeff, 0xf1f6edff, 0x0a817eff,
                0x56c0b8ff, 0x00ffffff, 0xc5f8f9ff, 0x8181ffff, 0x0000ffff, 0x3942cdff, 0x180a71ff, 0x1c0a4bff,
                0x7f007fff, 0xcd47caff, 0xf500f5ff, 0xfb7ef1ff, 0xffc0cbff, 0xfe7e89ff, 0xff1e05ff, 0xbf3f3fff,
                0x850511ff, 0x4c1915ff, 0x7e3f10ff, 0xbf7d31ff, 0xff7f00ff, 0xfdc18cff, 0xf8fdb0ff, 0xffff00ff,
                0xbfbf3fff, 0x838009ff, 0x007f00ff, 0x31ba4aff, 0x00ff00ff, 0xb5fda8ff, 0xc8a883ff, 0x7d8e90ff,
                0x6e816fff, 0xad6b5cff, 0xce6f75ff, 0xcd8c85ff, 0xe4a999ff, 0xe4bfacff, 0xf4cfccff, 0xf4e1e6ff,
                0x4e373cff, 0x7b434bff, 0x96584fff, 0x8c7c63ff, 0x6a5441ff, 0x3d2919ff, 0x463e21ff, 0x747140ff,
                0x948841ff, 0xa1a166ff, 0xa8b376ff, 0xcac188ff, 0xc7eea5ff, 0x9ec48eff, 0x7fbd54ff, 0x739555ff,
                0x547d4fff, 0x275241ff, 0x3b6469ff, 0x276f4aff, 0x70aa5fff, 0x6fc485ff, 0xa8d69cff, 0x9ee1cfff,
                0x8ab290ff, 0x48787aff, 0x56afaaff, 0x8bc9beff, 0xa4e9f4ff, 0xcdd4ecff, 0xa7c9f0ff, 0x84acc5ff,
                0x3998d4ff, 0x3a5f8aff, 0x0e1429ff, 0x242846ff, 0x314672ff, 0x655a95ff, 0x846a9dff, 0x5b78cdff,
                0x9d8cb3ff, 0x914ebbff, 0x573b73ff, 0x49243dff, 0x753869ff, 0x915294ff, 0xa35cb4ff, 0xab73abff,
                0xdfa7f7ff, 0xe7c5f5ff, 0xcb9ea7ff, 0xce90bbff, 0xc67aaaff, 0xc65d95ff, 0x2b1b25ff, 0x612110ff,
                0xa9210aff, 0xdf280dff, 0xde4c4aff, 0xf95438ff, 0xff6262ff, 0xf6bd31ff, 0xfca548ff, 0xd79b0fff,
                0xda6e0aff, 0xb45a00ff, 0xa04b05ff, 0x64341aff, 0x54561bff, 0x696606ff, 0xac9400ff, 0xb1b10aff,
                0xdfdc5aff, 0xffd510ff, 0xfdf052ff, 0xcffa32ff, 0x93ed3eff, 0x9cd928ff, 0x72cd1cff, 0x7fa618ff,
                0x3c6e14ff, 0x223e1bff, 0x0b4c1cff, 0x225a16ff, 0x149605ff, 0x0ad70aff, 0x14e60aff, 0x88fd6bff,
                0x4df254ff, 0x00c514ff, 0x34904fff, 0x1b9d84ff, 0x17c096ff, 0x12de6eff, 0x2deba8ff, 0x45feb3ff,
                0x7efdc0ff, 0x5ee9ffff, 0x91d3dfff, 0x3be1d2ff, 0x0f377dff, 0x0d4ca5ff, 0x0052f6ff, 0x186abdff,
                0x669db2ff, 0x5ea8f8ff, 0x8fb6ffff, 0x2fc0feff, 0x007fffff, 0x786ef0ff, 0x6e5af0ff, 0x553cf3ff,
                0x101cdaff, 0x1811b9ff, 0x1b0d9eff, 0x5b10b3ff, 0x571addff, 0x8d28c7ff, 0xa341f3ff, 0x7f00ffff,
                0xbd62ffff, 0xaf91f6ff, 0xe673ffff, 0xff52ffff, 0xe227ddff, 0xbd29ffff, 0xa812d5ff, 0x581783ff,
                0x682062ff, 0xa01982ff, 0xc90778ff, 0xff50bfff, 0xf668a8ff, 0xf7a4baff, 0xf83d92ff, 0xe61e78ff,
                0xbd1039ff, 0x983357ff, 0x9c1633ff, 0x3a2265ff, 0x353153ff, 0x93312cff, 0x8b4e30ff, 0x7e39b8ff,
                0xb05079ff, 0x55a335ff, 0x819e7eff, 0x9f9cc2ff, 0xd1b219ff, 0x24021bff, 0x380518ff, 0x2f343dff,
                0x2932b2ff, 0x921566ff, 0x615261ff, 0xa93b6fff, 0x886432ff, 0xb12fb4ff, 0x417b27ff, 0x4e7aa9ff,
                0xca4f32ff, 0xc17ad8ff, 0xd889d5ff, 0xbead55ff, 0x63313dff, 0x478f82ff, 0x48d987ff, 0xd1c915ff,
                0xb7e55dff, 0xf3e291ff, 0x2d2685ff, 0x7c3b95ff, 0xe5648fff, 0xa1baacff, 0xe1ca6bff, 0xdaf9d0ff,
                0x450c48ff, 0xd3105aff, 0x6d852fff, 0x76b41bff, 0xf593c6ff, 0x70f1e5ff, 0x761f29ff, 0xc8eb03ff,
                0x3268e9ff, 0xca4a5cff, 0x8f99e4ff, 0x7ff28dff, 0x98fbd8ff, 0x7c12b1ff, 0x2494adff, 0xea9563ff,
                0xe6fd79ff, 0xaa4597ff, 0xa87a47ff, 0xb5d44cff, 0xa5638fff, 0x59d1c2ff, 0x082908ff, 0x5b683aff,
        });
        Gdx.app.exit();
    }
    
    public void generate(String name, int[] pal)
    {
        byte[][][] sparse = new byte[96][96][96], dense = new byte[32][32][32];
        reducer.exact(pal);
        for (int x = 0; x < 32; x++) {
            for (int y = 0; y < 32; y++) {
                for (int z = 0; z < 32; z++) {
                    dense[x][y][z] = reducer.paletteMapping[x << 10 | y << 5 | z];
                }
            }
        }
        for (int i = 0; i < 256; i++) {
            int rgba =  reducer.paletteArray[i];
            if((rgba & 0xFF) == 0)
                continue;
            sparse[(rgba >>> 27)*3+1][(rgba >>> 19 & 31)*3+1][(rgba >>> 11 & 31)*3+1] = (byte) i;
        }
        VoxIO.writeVOX("ColorSolids/" + name + "ColorSolid.vox", dense, reducer.paletteArray);
        VoxIO.writeVOX("ColorSolids/" + name + "ColorSpots.vox", sparse, reducer.paletteArray);
    }
}
