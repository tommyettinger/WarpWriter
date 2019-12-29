import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.GdxRuntimeException;
import squidpony.FakeLanguageGen;
import squidpony.StringKit;
import squidpony.Thesaurus;
import squidpony.squidmath.OrderedSet;
import squidpony.squidmath.StatefulRNG;
import squidpony.squidmath.UnorderedSet;
import warpwriter.ModelMaker;
import warpwriter.PNG8;
import warpwriter.PaletteReducer;
import warpwriter.model.VoxelSeq;
import warpwriter.model.color.Colorizer;
import warpwriter.view.WarpDraw;
import warpwriter.view.color.VoxelColor;
import warpwriter.view.render.VoxelPixmapRenderer;

import java.io.IOException;

import static squidpony.squidmath.LinnormRNG.determine;

/**
 * Created by Tommy Ettinger on 1/19/2018.
 */
public class TestOutput extends ApplicationAdapter {
    private static final int LIMIT = 40;
    private Pixmap pix;
    private static long initialSeed = determine(System.nanoTime());
    private long seed = initialSeed;
    private ModelMaker mm;
    
    private VoxelSeq seq;
    
//    private ModelRenderer mr = new ModelRenderer();
    private VoxelPixmapRenderer vpr;
    private int dir = 1;
    private int width, height, frames = 1;
    private Pixmap[] pixes = new Pixmap[frames];
    private String pathName, modelName;
    private StatefulRNG srng = new StatefulRNG(initialSeed);
    private PNG8 png;
    private String makeName(Thesaurus thesaurus)
    {
        return StringKit.capitalize(thesaurus.makePlantName(FakeLanguageGen.MALAY).replaceAll("'s", "")).replaceAll("\\W", "");
    }
    private String makeName(OrderedSet<String> alpha, OrderedSet<String> beta)
    {
        String a = alpha.randomItem(srng);
        while (a.contains("'"))
            a = alpha.randomItem(srng);
        String b = beta.randomItem(srng);
        while (b.contains("'"))
            b = beta.randomItem(srng);
        final int al = a.length(), bl = b.length();
        final char[] ch = new char[al + bl];
        a.getChars(1, al, ch, 1);
        b.getChars(1, bl, ch, al+1);
        ch[0] = Character.toUpperCase(a.charAt(0));
        ch[al] = Character.toUpperCase(b.charAt(0));
        return String.valueOf(ch);
    }
    @Override
    public void create() {
        mm = new ModelMaker(seed, Colorizer.RollBonusColorizer);
        seq = new VoxelSeq(30000);
        seq.putSurface(mm.shipLargeSmoothColorized());
        width = WarpDraw.xLimit(seq);
        height = WarpDraw.yLimit(seq);
        for (int i = 0; i < frames; i++) {
            pixes[i] = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        }
        pix = pixes[0];
        vpr = new VoxelPixmapRenderer().set(pix).set(new VoxelColor().set(mm.getColorizer()));
        png = new PNG8(width * height);
        png.setFlipY(false);
        png.setCompression(6);
        png.palette = new PaletteReducer(Colorizer.ReallyRelaxedRollBonusPalette); 
                //new PaletteReducer(Coloring.GRAY);
//                new int[]{
//                        0x00000000,
//                        0x1B092CFF, 0x1C162BFF, 0x1F1833FF, 0x2C1D3BFF,
//                        0x1F1C32FF, 0x252837FF, 0x2B2E42FF, 0x3A394CFF,
//                        0x2E1F35FF, 0x352E3BFF, 0x3E3546FF, 0x4D4253FF,
//                        0x2C2F40FF, 0x393E4BFF, 0x414859FF, 0x565A68FF,
//                        0x464D55FF, 0x5B6269FF, 0x68717AFF, 0x868D94FF,
//                        0x606E74FF, 0x808C92FF, 0x90A1A8FF, 0xBCC6CEFF,
//                        0x7B8B90FF, 0xA1B0B4FF, 0xB6CBCFFF, 0xEEF7FFFF,
//                        0x8F9DA5FF, 0xBAC7CEFF, 0xD3E5EDFF, 0xFFFFFFFF,
//                        0xAFAFAFFF, 0xDFDFDFFF, 0xFFFFFFFF, 0xFFFFFFFF,
//                        0x472132FF, 0x4D3439FF, 0x5C3A41FF, 0x6A4B56FF,
//                        0x5F3E5FFF, 0x6E586EFF, 0x826481FF, 0x988498FF,
//                        0x6E454CFF, 0x7F5F60FF, 0x966C6CFF, 0xAA8C8BFF,
//                        0x796652FF, 0x92816EFF, 0xAB947AFF, 0xC9B7A5FF,
//                        0xB85159FF, 0xCC7475FF, 0xF68181FF, 0xFCBBA5FF,
//                        0xCD0A30FF, 0xC43233FF, 0xF53333FF, 0xEA5559FF,
//                        0xC92F36FF, 0xCF5348FF, 0xFF5A4AFF, 0xF88971FF,
//                        0x8C232DFF, 0x8E3F37FF, 0xAE4539FF, 0xB3605CFF,
//                        0x69312CFF, 0x73473AFF, 0x8A503EFF, 0x96685DFF,
//                        0x9D4128FF, 0xA85C3CFF, 0xCD683DFF, 0xD38A67FF,
//                        0xB77033FF, 0xD08F56FF, 0xFBA458FF, 0xFFCE93FF,
//                        0xC1420DFF, 0xCB5F25FF, 0xFB6B1DFF, 0xF68F50FF,
//                        0x705C3FFF, 0x87735AFF, 0x9F8562FF, 0xB8A38BFF,
//                        0xB48258FF, 0xD5A780FF, 0xFCBF8AFF, 0xFFF1C8FF,
//                        0xBA6E00FF, 0xD18823FF, 0xFF9E17FF, 0xFFBD5FFF,
//                        0xAC810AFF, 0xC89C32FF, 0xF0B628FF, 0xFFD078FF,
//                        0xA08A62FF, 0xC2AE89FF, 0xE3C896FF, 0xFFF2D6FF,
//                        0xB5A308FF, 0xD3C334FF, 0xFBE626FF, 0xFFEF95FF,
//                        0xA69C00FF, 0xC6B415FF, 0xEDD500FF, 0xFFDC69FF,
//                        0xB5AE56FF, 0xD8DB81FF, 0xFBFF86FF, 0xFFFFF0FF,
//                        0x7A9C20FF, 0x9CB549FF, 0xB4D645FF, 0xF2DF9FFF,
//                        0x467123FF, 0x657E44FF, 0x729446FF, 0x96A775FF,
//                        0x5D9F3DFF, 0x82B964FF, 0x91DB69FF, 0xDBE2BDFF,
//                        0x0A7800FF, 0x316D16FF, 0x358510FF, 0x49932EFF,
//                        0x239D15FF, 0x4EA441FF, 0x51C43FFF, 0x8EC781FF,
//                        0x208120FF, 0x468746FF, 0x4BA14AFF, 0x76AE76FF,
//                        0x00963FFF, 0x269D66FF, 0x1EBC73FF, 0x63C2A3FF,
//                        0x0CA47CFF, 0x39BDA0FF, 0x30E1B9FF, 0x93E2FAFF,
//                        0x509D87FF, 0x77C0AAFF, 0x7FE0C2FF, 0xD4F3FFFF,
//                        0x7BACB5FF, 0xA7DBDDFF, 0xB8FDFFFF, 0xFFFFFFFF,
//                        0x008343FF, 0x108568FF, 0x039F78FF, 0x3BAB93FF,
//                        0x3A898DFF, 0x60A7ADFF, 0x63C2C9FF, 0xACDCF9FF,
//                        0x31598CFF, 0x4C73A0FF, 0x4F83BFFF, 0x7EA5D2FF,
//                        0x0A4E58FF, 0x245A6CFF, 0x216981FF, 0x477E8FFF,
//                        0x4FA0ADFF, 0x79C8D0FF, 0x7FE8F2FF, 0xDAFFFFFF,
//                        0x292E7BFF, 0x384783FF, 0x3B509FFF, 0x5C69A7FF,
//                        0x2B6AA8FF, 0x4D87C0FF, 0x4D9BE6FF, 0x87C0FAFF,
//                        0x1F1759FF, 0x252B5BFF, 0x28306FFF, 0x403E76FF,
//                        0x2D469BFF, 0x4563ABFF, 0x4870CFFF, 0x7194D7FF,
//                        0x3B29A9FF, 0x474AADFF, 0x4D50D4FF, 0x6F76D5FF,
//                        0x2600BBFF, 0x1913A3FF, 0x180FCFFF, 0x401ECAFF,
//                        0x4D0070FF, 0x462066FF, 0x53207DFF, 0x6A318AFF,
//                        0x682CA1FF, 0x7551AAFF, 0x8657CCFF, 0x9E89D3FF,
//                        0x7652B3FF, 0x9378CCFF, 0xA884F3FF, 0xC0C8F9FF,
//                        0x640068FF, 0x510C54FF, 0x630867FF, 0x761079FF,
//                        0x851394FF, 0x863C94FF, 0xA03EB2FF, 0xAC6CBAFF,
//                        0x6F08A0FF, 0x6E339BFF, 0x8032BCFF, 0x945AC1FF,
//                        0x9D71AFFF, 0xC496D5FF, 0xE4A8FAFF, 0xF6FCFFFF,
//                        0x95136FFF, 0x953B72FF, 0xB53D86FF, 0xBC6699FF,
//                        0xBB21B3FF, 0xCA4EC3FF, 0xF34FE9FF, 0xE5AADEFF,
//                        0x66113BFF, 0x642C3DFF, 0x7A3045FF, 0x84415DFF,
//                        0xBF225EFF, 0xC34A69FF, 0xF04F78FF, 0xEB8591FF,
//                        0x92455EFF, 0xA26572FF, 0xC27182FF, 0xD09FA0FF,
//                        0xA90A34FF, 0xA12F35FF, 0xC93038FF, 0xC74C5BFF,
//        }
        png.palette.setDitherStrength(1f);
        pathName = "target/out/" + StringKit.hex(seed);
        Gdx.files.local(pathName).mkdirs();
//        OrderedSet<String> adjective = new OrderedSet<>(256), noun = new OrderedSet<>(256);
//        for (int i = 0; i < Thesaurus.adjective.size(); i++) {
//            adjective.addAll(Thesaurus.adjective.getAt(i));
//        }
//        for (int i = 0; i < Thesaurus.noun.size(); i++) {
//            noun.addAll(Thesaurus.noun.getAt(i));
//        }
        Thesaurus thesaurus = new Thesaurus(srng);
        thesaurus.addKnownCategories();
        UnorderedSet<String> names = new UnorderedSet<>(LIMIT);
        for (int i = 0; i < LIMIT; i++) {
            dir = 0;
            modelName = makeName(thesaurus);
            while (names.contains(modelName))
                modelName = makeName(thesaurus);
            names.add(modelName);
            Gdx.files.local(pathName + "/" + modelName).mkdirs();
            remakeShip(++seed);
            for (dir = 1; dir < 8; dir++) {
                remakeShip(0);
            }
        }
        Gdx.app.exit();
    }

//    public void remakeModel(long newModel) {
//        if (newModel != 0){
//            mm.rng.setState(determine(newModel));
//            voxels = mm.shipRandom();
//            animatedVoxels = mm.animateShip(voxels, frames);
//        }
//        for (int f = 0; f < frames; f++) {
//            pix = pixes[f];
//            pix.setColor(0);
//            pix.fill();
//            int[][] indices = mr.renderIso24x32(animatedVoxels[f], dir);
//                    //dir >= 4 ? mr.renderIso24x32(animatedVoxels[f], dir) : mr.renderOrtho(animatedVoxels[f], dir);
//            for (int x = 0; x < width; x++) {
//                for (int y = 0; y < height; y++) {
//                    pix.drawPixel(x, y, palette[indices[x][y]]);
//                }
//            }
//            PixmapIO.writePNG(Gdx.files.local(pathName + "/" + modelName + "/" + modelName + "_dir" + dir + "_" + f + ".png"), pix);
//        }
//    }

    public void remakeShip(long newModel) {
        if (newModel != 0) {
            mm.rng.setState(determine(newModel));
            seq.clear();
            seq.putSurface(mm.shipLargeSmoothColorized());
            //animatedVoxels = mm.animateShip(voxels, frames);
        }

//        pix = pixes[0];
//        pix.setColor(0);
//        pix.fill();
//        int[][] indices = dir >= 4 ? mr.renderIso(voxels, dir) : mr.renderOrtho(voxels, dir);
//        for (int x = 0; x < width; x++) {
//            for (int y = 0; y < height; y++) {
//                pix.drawPixel(x, y, palette[indices[x][y]]);
//            }
//        }
        if((dir & 1) == 1)
        {
            pix = WarpDraw.drawIso(seq, vpr);
            seq.clockZ();
        }
        else
        {
            pix = WarpDraw.drawAbove(seq, vpr);
        }
        try {
            png.write(Gdx.files.local(pathName + "/" + modelName + "/" + modelName + "_dir" + dir + "_0.png"), pix, false, true);
        } catch (IOException ex) {
            throw new GdxRuntimeException("Error writing PNG: " + modelName + "_dir" + dir + "_0.png", ex);
        }
        //PixmapIO.writePNG(Gdx.files.local(pathName + "/" + modelName + "/" + modelName + "_dir" + dir + "_0.png"), pix);
    }

    @Override
    public void render() {
    }

    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Output! Seed is 0x" + StringKit.hex(initialSeed));
        config.setWindowedMode(500, 500);
        config.setResizable(false);
        new Lwjgl3Application(new TestOutput(), config);
    }
}
