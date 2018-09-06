import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.utils.GdxRuntimeException;
import squidpony.FakeLanguageGen;
import squidpony.StringKit;
import squidpony.Thesaurus;
import squidpony.squidmath.OrderedSet;
import squidpony.squidmath.StatefulRNG;
import squidpony.squidmath.UnorderedSet;
import warpwriter.Coloring;
import warpwriter.ModelMaker;
import warpwriter.ModelRenderer;

import java.io.IOException;

import static squidpony.squidmath.LinnormRNG.determine;

/**
 * Created by Tommy Ettinger on 1/19/2018.
 */
public class TestOutput extends ApplicationAdapter {
    private static final int LIMIT = 160;
    private Pixmap pix;
    private static long initialSeed = determine(System.nanoTime());
    private long seed = initialSeed;
    private ModelMaker mm = new ModelMaker(seed);
    private ModelRenderer mr = new ModelRenderer();
    private byte[][][] voxels;
    private byte[][][][] animatedVoxels;
    private int dir = 1;
    private int width, height, frames = 1;
    private Pixmap[] pixes = new Pixmap[frames];
    private String pathName, modelName;
    StatefulRNG srng = new StatefulRNG(initialSeed);
    int[] palette = Coloring.ALT_PALETTE;
    private PNG8 png;
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
        int[][] rendered = mr.renderIso(mm.shipLargeRandom(), 0);
        width = rendered.length;
        height = rendered[0].length;
        for (int i = 0; i < frames; i++) {
            pixes[i] = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        }
        pix = pixes[0];
        png = new PNG8(width * height * 3 >> 1);
        png.setFlipY(false);
        png.setCompression(6);
        png.palette = new PaletteReducer(Coloring.GB_GREEN);
        png.palette.setDitherStrength(0.5f);
        pathName = "target/out/" + StringKit.hex(seed);
        Gdx.files.local(pathName).mkdirs();
        FakeLanguageGen language = FakeLanguageGen.randomLanguage(initialSeed).removeAccents();
        OrderedSet<String> adjective = new OrderedSet<>(256), noun = new OrderedSet<>(256);
        for (int i = 0; i < Thesaurus.adjective.size(); i++) {
            adjective.addAll(Thesaurus.adjective.getAt(i));
        }
        for (int i = 0; i < Thesaurus.noun.size(); i++) {
            noun.addAll(Thesaurus.noun.getAt(i));
        }
        UnorderedSet<String> names = new UnorderedSet<>(LIMIT);
        for (int i = 0; i < LIMIT; i++) {
            dir = 0;
            modelName = makeName(adjective, noun);
            while (names.contains(modelName))
                modelName = makeName(adjective, noun);
            names.add(modelName);
            Gdx.files.local(pathName + "/" + modelName).mkdirs();
            remakeShip(++seed);
            for (dir = 1; dir < 8; dir++) {
                remakeShip(0);
            }
        }
        Gdx.app.exit();
    }

    public void remakeModel(long newModel) {
        if (newModel != 0){
            mm.rng.setState(determine(newModel));
            voxels = mm.shipRandom();
            animatedVoxels = mm.animateShip(voxels, frames);
        }
        for (int f = 0; f < frames; f++) {
            pix = pixes[f];
            pix.setColor(0);
            pix.fill();
            int[][] indices = mr.renderIso24x32(animatedVoxels[f], dir);
                    //dir >= 4 ? mr.renderIso24x32(animatedVoxels[f], dir) : mr.renderOrtho(animatedVoxels[f], dir);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    pix.drawPixel(x, y, palette[indices[x][y]]);
                }
            }
            PixmapIO.writePNG(Gdx.files.local(pathName + "/" + modelName + "/" + modelName + "_dir" + dir + "_" + f + ".png"), pix);
        }
    }

    public void remakeShip(long newModel) {
        if (newModel != 0) {
            mm.rng.setState(determine(newModel));
            voxels = mm.shipLargeRandom();
            //animatedVoxels = mm.animateShip(voxels, frames);
        }

        pix = pixes[0];
        pix.setColor(0);
        pix.fill();
        int[][] indices = dir >= 4 ? mr.renderIso(voxels, dir) : mr.renderOrtho(voxels, dir);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                pix.drawPixel(x, y, palette[indices[x][y]]);
            }
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
        new Lwjgl3Application(new TestOutput(), config);
    }
}
