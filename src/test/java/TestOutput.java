import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.GdxRuntimeException;
import squidpony.FakeLanguageGen;
import squidpony.StringKit;
import squidpony.Thesaurus;
import squidpony.squidmath.StatefulRNG;
import squidpony.squidmath.UnorderedSet;
import warpwriter.ModelMaker;
import warpwriter.PNG8;
import warpwriter.Tools3D;
import warpwriter.VoxIO;
import warpwriter.model.IVoxelSeq;
import warpwriter.model.VoxelSeq;
import warpwriter.model.color.Colorizer;
import warpwriter.model.nonvoxel.LittleEndianDataInputStream;
import warpwriter.view.WarpDraw;
import warpwriter.view.color.VoxelColor;
import warpwriter.view.render.VoxelPixmapRenderer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
    
    private IVoxelSeq seq;
    
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
    @Override
    public void create() {
//        mm = new ModelMaker(seed, Colorizer.AuroraColorizer);
//        seq = new VoxelSeq(30000);
//        seq.putSurface(mm.shipLargeSmoothColorized());
        try {
            seq = VoxIO.readVoxelSeq(new LittleEndianDataInputStream(new FileInputStream("ColorSolids/AuroraColorSolid.vox")));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            byte[][][] bytes = new byte[32][32][32];
            Tools3D.fill(bytes, 40);
            seq = new VoxelSeq(32 * 32 * 6);
            seq.putSurface(bytes);
        }

        width = WarpDraw.xLimit(seq);
        height = WarpDraw.yLimit(seq);
        for (int i = 0; i < frames; i++) {
            pixes[i] = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        }
        pix = pixes[0];
        vpr = new VoxelPixmapRenderer().set(pix).set(new VoxelColor().colorizer(Colorizer.AuroraColorizer));
        png = new PNG8(width * height);
        png.setFlipY(false);
        png.setCompression(6);
        png.palette = Colorizer.AuroraColorizer.getReducer();//new PaletteReducer(Colorizer.ReallyRelaxedRollBonusPalette); 
                //new PaletteReducer(Coloring.GRAY);
        png.palette.setDitherStrength(1f);
        pathName = "target/out/" + StringKit.hex(seed);
        Gdx.files.local(pathName).mkdirs();
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
        
//        try {
//            seq = VoxIO.readVoxelSeq(new LittleEndianDataInputStream(new FileInputStream("ColorSolids/AuroraColorSolid.vox")));
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//
//        for (dir = 0; dir < 8; dir++) {
//            if ((dir & 1) == 1) {
//                pix = WarpDraw.drawIso(seq, vpr);
//                seq.clockZ();
//            } else {
//                pix = WarpDraw.drawAbove(seq, vpr);
//            }
//            try {
//                png.writePrecisely(Gdx.files.local("target/out/AuroraColorSolid2/Cube_dir" + dir + "_0.png"), pix, true, 1);
//            } catch (IOException ex) {
//                throw new GdxRuntimeException("Error writing PNG: Cube_dir" + dir + "_0.png", ex);
//            }
//        }
        
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
