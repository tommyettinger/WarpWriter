import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Pixmap;
import squidpony.FakeLanguageGen;
import squidpony.StringKit;
import squidpony.squidmath.StatefulRNG;
import squidpony.squidmath.UnorderedSet;
import warpwriter.Coloring;
import warpwriter.DetailedModelRenderer;
import warpwriter.ModelMaker;

import java.io.IOException;

import static squidpony.squidmath.LinnormRNG.determine;

/**
 * Created by Tommy Ettinger on 1/19/2018.
 */
public class TestDetailedOutput extends ApplicationAdapter {
    private static final int LIMIT = 500;
    private Pixmap pix;
    private static long initialSeed = determine(System.nanoTime());
    private long seed = initialSeed;
    private ModelMaker mm = new ModelMaker(seed);
    private DetailedModelRenderer mr = new DetailedModelRenderer();
    private byte[][][] voxels;
    private byte[][][][] animatedVoxels;
    private int dir = 1;
    private int width, height, frames = 1;
    private Pixmap[] pixes = new Pixmap[frames];
    private String pathName, modelName;
    private StatefulRNG srng = new StatefulRNG(initialSeed);
    private int[] palette = Coloring.ALT_PALETTE;
    private PNG8 png8;
    @Override
    public void create() {
        int[][] rendered = mr.renderIso(mm.shipRandom(), 0);
        width = rendered.length;
        height = rendered[0].length;
        png8 = new PNG8(width * height);
        png8.palette = new PaletteReducer(Coloring.ALT_PALETTE);
        png8.setFlipY(false);
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

    public void remakeShip(long newModel) {
        if (newModel != 0) {
            mm.rng.setState(determine(newModel));
            voxels = mm.shipRandom();
            //animatedVoxels = mm.animateShip(voxels, frames);
        }

        pix = pixes[0];
        pix.setColor(0);
        pix.fill();
        int[][] indices = dir >= 4 ? mr.renderIso(voxels, dir) : mr.renderOrtho(voxels, dir);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                pix.drawPixel(x, y, indices[x][y]);
            }
        }
        try {
            png8.write(Gdx.files.local(pathName + "/" + modelName + "/" + modelName + "_dir" + dir + "_0.png"), pix, false, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void render() {
    }

    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Output! Seed is 0x" + StringKit.hex(initialSeed));
        config.setWindowedMode(500, 500);
        new Lwjgl3Application(new TestDetailedOutput(), config);
    }
}
