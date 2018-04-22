import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import squidpony.FakeLanguageGen;
import squidpony.StringKit;
import squidpony.squidmath.StatefulRNG;
import squidpony.squidmath.LightRNG;
import squidpony.squidmath.UnorderedSet;
import warpwriter.Coloring;
import warpwriter.ModelMaker;
import warpwriter.ModelRenderer;

/**
 * Created by Tommy Ettinger on 1/19/2018.
 */
public class TestOutput extends ApplicationAdapter {
    private static final int LIMIT = 100;
    private Pixmap pix;
    private static long initialSeed = LightRNG.determine(System.nanoTime());
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
    @Override
    public void create() {
        int[][] rendered = mr.renderIso(mm.shipLargeRandom(), 0);
        width = rendered.length;
        height = rendered[0].length;
        for (int i = 0; i < frames; i++) {
            pixes[i] = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        }
        pix = pixes[0];
        pathName = "target/out/" + StringKit.hex(seed);
        Gdx.files.local(pathName).mkdirs();
        FakeLanguageGen language = FakeLanguageGen.randomLanguage(initialSeed).removeAccents();
        UnorderedSet<String> names = new UnorderedSet<>(LIMIT);
        for (int i = 0; i < LIMIT; i++) {
            dir = 0;
            modelName = language.word(srng, true);
            while (names.contains(modelName))
                modelName = language.word(srng, true);
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
            mm.light.state = LightRNG.determine(newModel);
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
            mm.light.state = LightRNG.determine(newModel);
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
        PixmapIO.writePNG(Gdx.files.local(pathName + "/" + modelName + "/" + modelName + "_dir" + dir + "_0.png"), pix);
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
