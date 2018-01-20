import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import squidpony.FakeLanguageGen;
import squidpony.StringKit;
import squidpony.squidmath.StatefulRNG;
import squidpony.squidmath.ThrustAltRNG;
import squidpony.squidmath.UnorderedSet;
import warpwriter.Coloring;
import warpwriter.ModelMaker;
import warpwriter.ModelRenderer;

/**
 * Created by Tommy Ettinger on 1/19/2018.
 */
public class TestOutput extends ApplicationAdapter {
    private Pixmap pix;
    private static long initialSeed = ThrustAltRNG.determine(System.nanoTime());
    private long seed = initialSeed;
    private ModelMaker mm = new ModelMaker(seed);
    private ModelRenderer mr = new ModelRenderer();
    private byte[][][] voxels;
    private byte[][][][] animatedVoxels;
    private int dir = 1;
    private final int width = 60, height = 68, frames = 8;
    private Pixmap[] pixes = new Pixmap[frames];
    private String pathName, shipName;
    StatefulRNG srng = new StatefulRNG(initialSeed);
    @Override
    public void create() {

        for (int i = 0; i < frames; i++) {
            pixes[i] = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        }
        pix = pixes[0];

        pathName = "target/out/" + StringKit.hex(seed);
        Gdx.files.local(pathName).mkdirs();
        UnorderedSet<String> names = new UnorderedSet<>(100);
        for (int i = 0; i < 100; i++) {
            dir = 0;
            shipName = FakeLanguageGen.JAPANESE_ROMANIZED.word(srng, true);
            while (names.contains(shipName))
                shipName = FakeLanguageGen.JAPANESE_ROMANIZED.word(srng, true);
            names.add(shipName);
            Gdx.files.local(pathName + "/" + shipName).mkdirs();
            remakeShip(seed++);
            for (dir = 1; dir < 8; dir++) {
                remakeShip(0);
            }
        }
        Gdx.app.exit();
    }

    public void remakeFish(long newModel) {
        if (newModel != 0){
            mm.thrust.state = ThrustAltRNG.determine(newModel);
            voxels = mm.fishRandom();
            animatedVoxels = mm.animateFish(voxels, frames);
        }
        for (int f = 0; f < frames; f++) {
            pix = pixes[f];
            pix.setColor(0);
            pix.fill();
            int[][] indices = mr.renderOrtho(animatedVoxels[f], dir);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    pix.drawPixel(x, y, Coloring.CW_PALETTE[indices[x][y]]);
                }
            }
        }
    }

    public void remakeShip(long newModel) {
        if (newModel != 0) {
            mm.thrust.state = ThrustAltRNG.determine(newModel);
            voxels = mm.shipRandom();
            //animatedVoxels = mm.animateShip(voxels, frames);
        }

        pix = pixes[0];
        pix.setColor(0);
        pix.fill();
        int[][] indices = dir >= 4 ? mr.renderIso(voxels, dir) : mr.renderOrtho(voxels, dir);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                pix.drawPixel(x, y, Coloring.CW_PALETTE[indices[x][y]]);
            }
        }
        PixmapIO.writePNG(Gdx.files.local(pathName + "/" + shipName + "/" + shipName + "_dir" + dir + "_0.png"), pix);
    }

    @Override
    public void render() {
    }

    public static void main(String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "Output! Seed is " + StringKit.hex(initialSeed);
        config.width = 500;
        config.height = 500;
        new LwjglApplication(new TestOutput(), config);
    }
}
