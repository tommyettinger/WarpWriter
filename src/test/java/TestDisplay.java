import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.GdxRuntimeException;
import squidpony.FakeLanguageGen;
import squidpony.StringKit;
import squidpony.squidmath.FastNoise;
import squidpony.squidmath.Noise;
import warpwriter.*;
import warpwriter.model.Fetch;
import warpwriter.model.IModel;
import warpwriter.model.color.Colorizer;
import warpwriter.model.decide.DecideFetch;
import warpwriter.model.decide.HeightDecide;
import warpwriter.model.fetch.*;
import warpwriter.model.nonvoxel.CompassDirection;
import warpwriter.model.nonvoxel.LittleEndianDataInputStream;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;

import static squidpony.squidmath.LinnormRNG.determine;

/**
 * Displays pseudo-random spaceships, currently, with the sequence advancing when you press space, and rotating when you
 * press the arrow keys. Pressing q quits, and any other key will change the seed, producing different ships on the next
 * generation (by pressing space).
 * Created by Tommy Ettinger on 11/4/2017.
 */
public class TestDisplay extends ApplicationAdapter {
    private SpriteBatch batch;
    private Texture tex;
    private Pixmap pix;
    private long seed = 12345L;
    private int width = 52, height = 64, frames = 8;
    private ModelMaker mm;
    private ModelRenderer mr = new ModelRenderer(true, true); // change to (false, true) to disable easing
    private PaletteReducer reducer;
    //    private byte[][][] voxels;
//    private byte[][][][] animatedVoxels;
    private IModel voxels;
    private IModel[] animatedVoxels = new IModel[frames];
    private int counter = 1;
    private CompassDirection dir = CompassDirection.SOUTH;
    private static final int background = 0;
    /**
     * The height of the viewing angle, with 0 being directly below (bottom), 1 being at a 45 degree angle from below
     * (below), 2 being at the same height (side), 3 being a sorta-isometric view at a 45 degree angle from above (this
     * is the default and usually isn't mentioned in names), and 4 being directly above (top).
     */
    private int angle = 3;
    private boolean playing = false, rotating = false, errorDiffusion = true, large = true, dither = false;
    private Pixmap[] pixes = new Pixmap[frames];
    private int[][] indices;
    private int[] palette = Coloring.RINSED;
    private BitmapFont font;

    @Override
    public void create() {
//        PaletteReducer arb = Colorizer.arbitraryColorizer(Coloring.RINSED).getReducer();
//        System.out.println(Arrays.equals(Colorizer.RinsedColorizer.getReducer().paletteMapping, arb.paletteMapping));
//        System.out.println(Arrays.equals(Colorizer.RinsedColorizer.getReducer().paletteMapping, new PaletteReducer(Coloring.RINSED).paletteMapping));
        font = new BitmapFont(Gdx.files.internal("PxPlus_IBM_VGA_8x16.fnt"));
        font.setColor(Color.BLACK);
        mm = new ModelMaker(seed, Colorizer.RinsedColorizer);//arbitraryColorizer(Coloring.RINSED)
        reducer = Coloring.FLESURRECT_REDUCER;
//                new PaletteReducer(
//                        Coloring.FLESURRECT
//                //new int[]{0, 255, -1}
//        ); 
        //Coloring.FLESURRECT_REDUCER; //Colorizer.FlesurrectBonusPalette
        //PaletteReducer.generatePreloadCode(reducer.paletteMapping);
        reducer.setDitherStrength(0.25f);
        batch = new SpriteBatch();
//        pix = new Pixmap(16, 16, Pixmap.Format.RGBA8888);
//        tex = new Texture(16, 16, Pixmap.Format.RGBA8888);
        for (int i = 0; i < frames; i++) {
            pixes[i] = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        }
        pix = pixes[0];
        tex = new Texture(width, height, Pixmap.Format.RGBA8888);
        remakeWarrior(seed);
        InputAdapter input = new InputAdapter() {
            String name = "Wriggler";

            @Override
            public boolean keyDown(int keycode) {
                switch (keycode) {
                    case Input.Keys.Q:
                        Gdx.app.exit();
                        return true;
                    case Input.Keys.COMMA:
                        remakeFull(++seed);
                        return true;
                    case Input.Keys.P:
                        playing = !playing;
                        return true;
//                    case Input.Keys.R:
//                        rotating = !rotating;
//                        return true;
                    case Input.Keys.L:
                        large = !large;
                        return true;
                    case Input.Keys.E: // edge, affects outline
                        mr.hardOutline = !mr.hardOutline;
                        remakeShip(0);
                        return true;
                    case Input.Keys.D: // dither, toggles dither on or off
                        dither = !dither;
                        remakeShip(0);
                        return true;
                    case Input.Keys.M: // smoothing, affects outline
                        mr.easing = !mr.easing;
                        remakeShip(0);
                        return true;
                    case Input.Keys.R: // change dither algorithm
                        errorDiffusion = !errorDiffusion;
                        remakeShip(0);
                        return true;
                    case Input.Keys.B: // below
                        angle = 1;
                        remakeShip(0);
                        return true;
                    case Input.Keys.S: // side
                        angle = 2;
                        remakeShip(0);
                        return true;
                    case Input.Keys.A: // above
                        angle = 3;
                        remakeShip(0);
                        return true;
                    case Input.Keys.H: // height cycling
                        angle = ((angle + 1) % 4);
                        remakeShip(0);
                        return true;
                    case Input.Keys.SPACE:
                        remakeShip(++seed);
                        return true;
                    case Input.Keys.UP:
                    case Input.Keys.NUMPAD_8:
                    case Input.Keys.NUM_8:
                        dir = CompassDirection.NORTH;
                        remakeShip(0);
                        return true;
                    case Input.Keys.NUMPAD_6:
                    case Input.Keys.NUM_6:
                    case Input.Keys.RIGHT:
                        dir = CompassDirection.EAST;
                        remakeShip(0);
                        return true;
                    case Input.Keys.NUMPAD_2:
                    case Input.Keys.NUM_2:
                    case Input.Keys.DOWN:
                        dir = CompassDirection.SOUTH;
                        remakeShip(0);
                        return true;
                    case Input.Keys.NUMPAD_4:
                    case Input.Keys.NUM_4:
                    case Input.Keys.LEFT:
                        dir = CompassDirection.WEST;
                        remakeShip(0);
                        return true;
                    case Input.Keys.NUMPAD_7:
                    case Input.Keys.NUM_7:
                        dir = CompassDirection.NORTH_WEST;
                        remakeShip(0);
                        return true;
                    case Input.Keys.NUMPAD_9:
                    case Input.Keys.NUM_9:
                        dir = CompassDirection.NORTH_EAST;
                        remakeShip(0);
                        return true;
                    case Input.Keys.NUMPAD_3:
                    case Input.Keys.NUM_3:
                        dir = CompassDirection.SOUTH_EAST;
                        remakeShip(0);
                        return true;
                    case Input.Keys.NUMPAD_1:
                    case Input.Keys.NUM_1:
                        dir = CompassDirection.SOUTH_WEST;
                        remakeShip(0);
                        return true;

                    case Input.Keys.F:
                        remakeFish(++seed);
                        return true;
                    case Input.Keys.K: // knight
                        remakeWarrior(++seed);
                        return true;
                    case Input.Keys.PERIOD:
                        remakeBlob(++seed);
                        return true;
                    case Input.Keys.N:
                        remakeText(++seed);
                        return true;
                    case Input.Keys.C:
                        remakeTerrain(++seed);
                        return true;
                    case Input.Keys.O: // output
                        if (voxels instanceof ArrayModel) {
                            name = FakeLanguageGen.SIMPLISH.word(true);
                            VoxIO.writeVOX(name + ".vox", ((ArrayModel) voxels).voxels, palette);
                            //VoxIO.writeAnimatedVOX(name + "_Animated.vox", animatedVoxels, palette);
                        }
                        return true;
                    case Input.Keys.SLASH:
                        seed += determine(seed);
                        return true;
                }
                return true;
            }
        };
        Gdx.input.setInputProcessor(input);
    }

    public void remakeFish(long newModel) {
        mm.rng.setState(determine(newModel));
        voxels = new ArrayModel(mm.fishRandom());
        byte[][][][] anim = mm.animateFish(((ArrayModel) voxels).voxels, frames);
        for (int i = 0; i < frames; i++) {
            animatedVoxels[i] = new ArrayModel(anim[i]);
        }
        /*int state = Tools3D.hash(voxels);
        batch.setColor(
                Float.intBitsToFloat(0xFE000000
                        | (((state = (state ^ 0x9E3779B9) * 0x9E377) >>> 30) * 17 + 76) << 17
                        | (((state = (state ^ 0x9E3779B9) * 0x9E377) >>> 30) * 17 + 76) << 9
                        | ((          (state ^ 0x9E3779B9) * 0x9E377 >>> 30) * 17 + 76) << 1)
        );*/
        for (int f = 0; f < frames; f++) {
            pix = pixes[f];
            pix.setColor(background);
            pix.fill();
//            if(tiny && !large) indices = mr.renderIso24x32(animatedVoxels[f], dir);
//            else
            {
                switch (angle) {
                    case 1:
                        if (dir.isDiagonal()) indices = mr.renderIsoBelow(animatedVoxels[f], dir);
                        else indices = mr.renderOrthoBelow(animatedVoxels[f], dir);
                        break;
                    case 3:
                        if (dir.isDiagonal()) indices = mr.renderIso(animatedVoxels[f], dir);
                        else indices = mr.renderOrtho(animatedVoxels[f], dir);
                        break;
                    default:
                        if (dir.isDiagonal()) indices = mr.renderIsoSide(animatedVoxels[f], dir);
                        else indices = mr.renderOrthoSide(animatedVoxels[f], dir);
                        break;
                }
            }
            for (int x = 0; x < indices.length; x++) {
                for (int y = 0; y < indices[0].length; y++) {
                    pix.drawPixel(x, y, palette[indices[x][y]]);
                }
            }
            if (dither) {
                if (errorDiffusion) reducer.reduceFloydSteinberg(pix);
                else reducer.reduceShaderMimic(pix);
            }
        }
    }

    public void remakeWarrior(long newModel) {
        mm.rng.setState(determine(newModel));
        voxels = new ArrayModel(mm.warriorRandom());
        Arrays.fill(animatedVoxels, voxels);
        /*int state = Tools3D.hash(voxels);
        batch.setColor(
                Float.intBitsToFloat(0xFE000000
                        | (((state = (state ^ 0x9E3779B9) * 0x9E377) >>> 30) * 17 + 76) << 17
                        | (((state = (state ^ 0x9E3779B9) * 0x9E377) >>> 30) * 17 + 76) << 9
                        | ((          (state ^ 0x9E3779B9) * 0x9E377 >>> 30) * 17 + 76) << 1)
        );*/
        for (int f = 0; f < frames; f++) {
            pix = pixes[f];
            pix.setColor(background);
            pix.fill();
//            if(tiny && !large) indices = mr.renderIso24x32(animatedVoxels[f], dir);
//            else
            {
                switch (angle) {
                    case 1:
                        if (dir.isDiagonal()) indices = mr.renderIsoBelow(animatedVoxels[f], dir);
                        else indices = mr.renderOrthoBelow(animatedVoxels[f], dir);
                        break;
                    case 3:
                        if (dir.isDiagonal()) indices = mr.renderIso(animatedVoxels[f], dir);
                        else indices = mr.renderOrtho(animatedVoxels[f], dir);
                        break;
                    default:
                        if (dir.isDiagonal()) indices = mr.renderIsoSide(animatedVoxels[f], dir);
                        else indices = mr.renderOrthoSide(animatedVoxels[f], dir);
                        break;
                }
            }
            for (int x = 0; x < indices.length; x++) {
                for (int y = 0; y < indices[0].length; y++) {
                    pix.drawPixel(x, y, palette[indices[x][y]]);
                }
            }
            if (dither) {
                if (errorDiffusion) reducer.reduceFloydSteinberg(pix);
                else reducer.reduceShaderMimic(pix);
            }
        }
    }

    public void remakeBlob(long newModel) {
        mm.rng.setState(determine(newModel));
        byte[][][][] anim = mm.animateBlobLargeRandom(frames);
        for (int i = 0; i < frames; i++) {
            animatedVoxels[i] = new ArrayModel(anim[i]);
        }

        voxels = animatedVoxels[0];
        //palette = Coloring.ALT_PALETTE;
        /*int state = Tools3D.hash(voxels);
        batch.setColor(
                Float.intBitsToFloat(0xFE000000
                        | (((state = (state ^ 0x9E3779B9) * 0x9E377) >>> 30) * 17 + 76) << 17
                        | (((state = (state ^ 0x9E3779B9) * 0x9E377) >>> 30) * 17 + 76) << 9
                        | ((          (state ^ 0x9E3779B9) * 0x9E377 >>> 30) * 17 + 76) << 1)
        );*/
        for (int f = 0; f < frames; f++) {
            pix = pixes[f];
            pix.setColor(background);
            pix.fill();
//            if(tiny && !large) indices = mr.renderIso24x32(animatedVoxels[f], dir);
//            else
            {
                switch (angle) {
                    case 1:
                        if (dir.isDiagonal()) indices = mr.renderIsoBelow(animatedVoxels[f], dir);
                        else indices = mr.renderOrthoBelow(animatedVoxels[f], dir);
                        break;
                    case 3:
                        if (dir.isDiagonal()) indices = mr.renderIso(animatedVoxels[f], dir);
                        else indices = mr.renderOrtho(animatedVoxels[f], dir);
                        break;
                    default:
                        if (dir.isDiagonal()) indices = mr.renderIsoSide(animatedVoxels[f], dir);
                        else indices = mr.renderOrthoSide(animatedVoxels[f], dir);
                        break;
                }
            }
            for (int x = 0; x < indices.length; x++) {
                for (int y = 0; y < indices[0].length; y++) {
                    pix.drawPixel(x, y, palette[indices[x][y]]);
                }
            }
            if (dither) {
                if (errorDiffusion) reducer.reduceFloydSteinberg(pix);
                else reducer.reduceShaderMimic(pix);
            }
        }
    }

    private NoiseHeightMap heightMap;

    public void remakeTerrain(long newModel) {
        mm.rng.setState(determine(newModel));
        double size = 6;

        if (heightMap == null) {
            heightMap = new NoiseHeightMap(new Noise.Layered2D(FastNoise.instance, 2), newModel);
        } else {
            heightMap.setSeed(newModel);
        }

        voxels = (new DecideFetch()
                .setDecide(new HeightDecide(
                        heightMap, 0.05, 0.05, size
                ))
                .setFetch(new NoiseFetch(
                        mm.randomColorRange()
                ))
        ).model(50, 50, 12);

        Arrays.fill(animatedVoxels, voxels);
        int oldWidth = width, oldHeight = height;
        for (int f = 0; f < frames; f++) {
            {
                switch (angle) {
                    case 1:
                        if (dir.isDiagonal()) indices = mr.renderIsoBelow(animatedVoxels[f], dir);
                        else indices = mr.renderOrthoBelow(animatedVoxels[f], dir);
                        break;
                    case 3:
                        if (dir.isDiagonal()) indices = mr.renderIso(animatedVoxels[f], dir);
                        else indices = mr.renderOrtho(animatedVoxels[f], dir);
                        break;
                    default:
                        if (dir.isDiagonal()) indices = mr.renderIsoSide(animatedVoxels[f], dir);
                        else indices = mr.renderOrthoSide(animatedVoxels[f], dir);
                        break;
                }
            }
            width = indices.length;
            height = indices[0].length;
            if (oldWidth != width || oldHeight != height)
                pixes[f] = new Pixmap(width, height, Pixmap.Format.RGBA8888);
            pix = pixes[f];
            pix.setColor(background);
            pix.fill();
            for (int x = 0; x < indices.length; x++) {
                for (int y = 0; y < indices[0].length; y++) {
                    pix.drawPixel(x, y, palette[indices[x][y]]);
                }
            }
            if (dither) {
                if (errorDiffusion) reducer.reduceFloydSteinberg(pix);
                else reducer.reduceShaderMimic(pix);
            }
        }
        if (oldWidth != width || oldHeight != height)
            tex = new Texture(width, height, Pixmap.Format.RGBA8888);
    }

    private VoxelText voxelText = new VoxelText();

    public void remakeText(long newModel) {
        mm.rng.setState(determine(newModel));

        Fetch checkers = Stripes.checkers(
                ColorFetch.color(mm.randomMainColor()),
                ColorFetch.color(mm.randomMainColor()),
                new int[]{2, 2},
                new int[]{2, 2},
                new int[]{2, 2}
        );

        voxels = voxelText
                .setText(font, FakeLanguageGen.SIMPLISH.word(mm.rng.nextLong(), true))
                .setFill(checkers)
                .setDepth(5);

        Arrays.fill(animatedVoxels, voxels);
        int oldWidth = width, oldHeight = height;
        for (int f = 0; f < frames; f++) {
            {
                switch (angle) {
                    case 1:
                        if (dir.isDiagonal()) indices = mr.renderIsoBelow(animatedVoxels[f], dir);
                        else indices = mr.renderOrthoBelow(animatedVoxels[f], dir);
                        break;
                    case 3:
                        if (dir.isDiagonal()) indices = mr.renderIso(animatedVoxels[f], dir);
                        else indices = mr.renderOrtho(animatedVoxels[f], dir);
                        break;
                    default:
                        if (dir.isDiagonal()) indices = mr.renderIsoSide(animatedVoxels[f], dir);
                        else indices = mr.renderOrthoSide(animatedVoxels[f], dir);
                        break;
                }
            }
            width = indices.length;
            height = indices[0].length;
            if (oldWidth != width || oldHeight != height)
                pixes[f] = new Pixmap(width, height, Pixmap.Format.RGBA8888);
            pix = pixes[f];
            pix.setColor(background);
            pix.fill();
            for (int x = 0; x < indices.length; x++) {
                for (int y = 0; y < indices[0].length; y++) {
                    pix.drawPixel(x, y, palette[indices[x][y]]);
                }
            }
            if (dither) {
                if (errorDiffusion) reducer.reduceFloydSteinberg(pix);
                else reducer.reduceShaderMimic(pix);
            }
        }
        if (oldWidth != width || oldHeight != height)
            tex = new Texture(width, height, Pixmap.Format.RGBA8888);
    }

    public void remakeShipSmall(long newModel) {
        if (newModel != 0) {
            mm.rng.setState(determine(newModel));
            voxels = new ArrayModel(mm.shipSmoothColorized());
            byte[][][][] anim = mm.animateShip(((ArrayModel) voxels).voxels, frames);
            for (int i = 0; i < frames; i++) {
                animatedVoxels[i] = new ArrayModel(anim[i]);
            }
        }
        for (int f = 0; f < frames; f++) {
            pix = pixes[f];
            pix.setColor(background);
            pix.fill();
//            if(tiny && !large) indices = mr.renderIso24x32(animatedVoxels[f], dir);
//            else
            {
                switch (angle) {
                    case 1:
                        if (dir.isDiagonal()) indices = mr.renderIsoBelow(animatedVoxels[f], dir);
                        else indices = mr.renderOrthoBelow(animatedVoxels[f], dir);
                        break;
                    case 3:
                        if (dir.isDiagonal()) indices = mr.renderIso(animatedVoxels[f], dir);
                        else indices = mr.renderOrtho(animatedVoxels[f], dir);
                        break;
                    default:
                        if (dir.isDiagonal()) indices = mr.renderIsoSide(animatedVoxels[f], dir);
                        else indices = mr.renderOrthoSide(animatedVoxels[f], dir);
                        break;
                }
            }
            for (int x = 0; x < indices.length; x++) {
                for (int y = 0; y < indices[0].length; y++) {
                    pix.drawPixel(x, y, palette[indices[x][y]]);
                }
            }
            if (dither) {
                if (errorDiffusion) reducer.reduceFloydSteinberg(pix);
                else reducer.reduceShaderMimic(pix);
            }
        }
    }

    public void remakeShip(long newModel) {
        if (newModel != 0) {
            mm.rng.setState(determine(newModel));
            voxels = new ArrayModel(large ? mm.shipLargeNoiseColorized() : mm.shipNoiseColorized());
            byte[][][][] anim = mm.animateShip(((ArrayModel) voxels).voxels, frames);
            for (int i = 0; i < frames; i++) {
                animatedVoxels[i] = new ArrayModel(anim[i]);
            }
            /*int state = Tools3D.hash(voxels);
            batch.setColor(
                    Float.intBitsToFloat(0xFE000000
                            | (((state = (state ^ 0x9E3779B9) * 0x9E377) >>> 30) * 17 + 76) << 17
                            | (((state = (state ^ 0x9E3779B9) * 0x9E377) >>> 30) * 17 + 76) << 9
                            | ((          (state ^ 0x9E3779B9) * 0x9E377 >>> 30) * 17 + 76) << 1)
            );*/
        }
        int oldWidth = width, oldHeight = height;
        for (int f = 0; f < frames; f++) {
//            if(tiny && !large) indices = mr.renderIso24x32(animatedVoxels[f], dir);
//            else
            {
                switch (angle) {
                    case 1:
                        if (dir.isDiagonal()) indices = mr.renderIsoBelow(animatedVoxels[f], dir);
                        else indices = mr.renderOrthoBelow(animatedVoxels[f], dir);
                        break;
                    case 3:
                        if (dir.isDiagonal()) indices = mr.renderIso(animatedVoxels[f], dir);
                        else indices = mr.renderOrtho(animatedVoxels[f], dir);
                        break;
                    default:
                        if (dir.isDiagonal()) indices = mr.renderIsoSide(animatedVoxels[f], dir);
                        else indices = mr.renderOrthoSide(animatedVoxels[f], dir);
                        break;
                }
            }
            width = indices.length;
            height = indices[0].length;
            pix = pixes[f] = new Pixmap(width, height, Pixmap.Format.RGBA8888);
            pix.setColor(background);
            pix.fill();
            for (int x = 0; x < indices.length; x++) {
                for (int y = 0; y < indices[0].length; y++) {
                    pix.drawPixel(x, y, palette[indices[x][y]]);
                }
            }
            if (dither) {
                if (errorDiffusion) reducer.reduceFloydSteinberg(pix);
                else reducer.reduceShaderMimic(pix);
            }
        }
        if (oldWidth != width || oldHeight != height)
            tex = new Texture(width, height, Pixmap.Format.RGBA8888);
    }

    public void remakeFull(long newModel) {

        mm.rng.setState(determine(newModel));
        voxels = new ArrayModel(mm.fullyRandom(large));
        //palette = Coloring.ALT_PALETTE;
        Arrays.fill(animatedVoxels, voxels);
        /*int state = Tools3D.hash(voxels);
        batch.setColor(
                Float.intBitsToFloat(0xFE000000
                        | (((state = (state ^ 0x9E3779B9) * 0x9E377) >>> 30) * 17 + 76) << 17
                        | (((state = (state ^ 0x9E3779B9) * 0x9E377) >>> 30) * 17 + 76) << 9
                        | ((          (state ^ 0x9E3779B9) * 0x9E377 >>> 30) * 17 + 76) << 1)
        );*/
        int oldWidth = width, oldHeight = height;
        for (int f = 0; f < frames; f++) {
            pix = pixes[f];
            pix.setColor(background);
            pix.fill();
//            if(tiny && !large)
//                indices = mr.renderIso24x32(animatedVoxels[f], dir);
//            else
            {
                switch (angle) {
                    case 1:
                        if (dir.isDiagonal()) indices = mr.renderIsoBelow(animatedVoxels[f], dir);
                        else indices = mr.renderOrthoBelow(animatedVoxels[f], dir);
                        break;
                    case 3:
                        if (dir.isDiagonal()) indices = mr.renderIso(animatedVoxels[f], dir);
                        else indices = mr.renderOrtho(animatedVoxels[f], dir);
                        break;
                    default:
                        if (dir.isDiagonal()) indices = mr.renderIsoSide(animatedVoxels[f], dir);
                        else indices = mr.renderOrthoSide(animatedVoxels[f], dir);
                        break;
                }
            }
            for (int x = 0; x < indices.length; x++) {
                for (int y = 0; y < indices[0].length; y++) {
                    pix.drawPixel(x, y, palette[indices[x][y]]);
                }
            }
            if (dither) {
                if (errorDiffusion) reducer.reduceFloydSteinberg(pix);
                else reducer.reduceShaderMimic(pix);
            }
        }
        if (oldWidth != width || oldHeight != height)
            tex = new Texture(width, height, Pixmap.Format.RGBA8888);
    }


    public void load(String name) {
        try {
            System.out.println(name);
            byte[][][] arr = VoxIO.readVox(new LittleEndianDataInputStream(new FileInputStream(name)));
            if (arr != null) {
                voxels = new ArrayModel(arr);
                /// this is commented out because it's pretty hard to make a working palette by hand; this uses the default
                //palette = VoxIO.lastPalette;
                reducer.exact(VoxIO.lastPalette);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }
        Arrays.fill(animatedVoxels, voxels);
        int oldWidth = width, oldHeight = height;
        for (int f = 0; f < frames; f++) {
            {
                switch (angle) {
                    case 1:
                        if (dir.isDiagonal()) indices = mr.renderIsoBelow(animatedVoxels[f], dir);
                        else indices = mr.renderOrthoBelow(animatedVoxels[f], dir);
                        break;
                    case 2:
                        if (dir.isDiagonal()) indices = mr.renderIsoSide(animatedVoxels[f], dir);
                        else indices = mr.renderOrthoSide(animatedVoxels[f], dir);
                        break;
                    default:
                        if (dir.isDiagonal()) indices = mr.renderIso(animatedVoxels[f], dir);
                        else indices = mr.renderOrtho(animatedVoxels[f], dir);
                        break;
                }
            }
            width = indices.length;
            height = indices[0].length;
            pix = pixes[f] = new Pixmap(width, height, Pixmap.Format.RGBA8888);
            pix.setColor(background);
            pix.fill();
            for (int x = 0; x < indices.length; x++) {
                for (int y = 0; y < indices[0].length; y++) {
                    pix.drawPixel(x, y, palette[indices[x][y]]);
                }
            }
            if (dither) {
                if (errorDiffusion) reducer.reduceFloydSteinberg(pix);
                else reducer.reduceShaderMimic(pix);
            }
        }
        if (oldWidth != width || oldHeight != height)
            tex = new Texture(width, height, Pixmap.Format.RGBA8888);
    }


    public void loadPalette(String name) {
        try {
            String text = Gdx.files.absolute(name).readString();
            int start = 0, end = 6, len = text.length();
            int gap = (text.charAt(7) == '\n') ? 8 : 7;
            int[] pal = new int[1 + ((len + 2) / gap)];
            int sz = pal.length;
            for (int i = 1; i < sz; i++) {
                pal[i] = StringKit.intFromHex(text, start, end) << 8 | 0xFF;
                start += gap;
                end += gap;
            }
            reducer.exact(pal);
        } catch (GdxRuntimeException e) {
            e.printStackTrace();
            return;
        }
        int oldWidth = width, oldHeight = height;
        for (int f = 0; f < frames; f++) {
//            if(tiny && !large) indices = mr.renderIso24x32(animatedVoxels[f], dir);
//            else
            {
                switch (angle) {
                    case 1:
                        if (dir.isDiagonal()) indices = mr.renderIsoBelow(animatedVoxels[f], dir);
                        else indices = mr.renderOrthoBelow(animatedVoxels[f], dir);
                        break;
                    case 3:
                        if (dir.isDiagonal()) indices = mr.renderIso(animatedVoxels[f], dir);
                        else indices = mr.renderOrtho(animatedVoxels[f], dir);
                        break;
                    default:
                        if (dir.isDiagonal()) indices = mr.renderIsoSide(animatedVoxels[f], dir);
                        else indices = mr.renderOrthoSide(animatedVoxels[f], dir);
                        break;
                }
            }
            width = indices.length;
            height = indices[0].length;
            pix = pixes[f] = new Pixmap(width, height, Pixmap.Format.RGBA8888);
            pix.setColor(background);
            pix.fill();
            for (int x = 0; x < indices.length; x++) {
                for (int y = 0; y < indices[0].length; y++) {
                    pix.drawPixel(x, y, palette[indices[x][y]]);
                }
            }
            if (dither) {
                if (errorDiffusion) reducer.reduceFloydSteinberg(pix);
                else reducer.reduceShaderMimic(pix);
            }
        }
    }

    @Override
    public void render() {
        int time = (playing ? ++counter : counter) % ((frames << 1) * 6); //(tiny && !large ? 0 : 1)
        if (time == 0 && rotating) {
            dir = dir.clockwise();
            remakeShip(0);
        }
        tex.draw(pixes[(time / 6) % frames], 0, 0);

        // standard clear the background routine for libGDX
//        Gdx.gl.glClearColor(0x6Fp-10f, 0x25p-8f, 0x25p-9f, 1.0f);
        Gdx.gl.glClearColor(0.75f, 1f, 0.5f, 1.0f);
//        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
//        Gdx.gl.glClearColor(0.63f, 0.91f, 0.55f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
//        int state = Tools3D.hash(voxels);
//        batch.setColor(
//                Float.intBitsToFloat(0xFE000000
//                        | (determineSmallBounded((state = (state ^ 0x9E3779B9) * 0x9E377) ^ state >>> 16, 5) * 17 + 59) << 17
//                        | (determineSmallBounded((state = (state ^ 0x9E3779B9) * 0x9E377) ^ state >>> 16, 5) * 17 + 59) << 9
//                        | (determineSmallBounded((state = (state ^ 0x9E3779B9) * 0x9E377) ^ state >>> 16, 5) * 17 + 59) << 1)
//        );
        // GB
        //batch.setColor(0.75f, 1f, 0.5f, 1f);
        batch.draw(tex, width >> 1, 250 - (width >> 1), width, height);
        batch.draw(tex, width * 5 >> 1, 250 - (width), width << 1, height << 1);
        batch.draw(tex, width * 10 >> 1, 250 - (width << 1), width << 2, height << 2);
        //batch.draw(tex, width * 19 >> 1, 250 - (width << 2), width << 3, height << 3);

        font.draw(batch, "Dither " + (dither ? (errorDiffusion ? "Diffusing" : "Ordered") : "Off"), 16, 24);
//        batch.draw(tex, 64 - 8, 240 - 8, 16, 16);
//        batch.draw(tex, 192 - 16, 240 - 16, 32, 32);
//        batch.draw(tex, 320 - 32, 240 - 32, 64, 64);
//        batch.draw(tex, 512 - 64, 240 - 64, 128, 128);
        batch.end();
    }

    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Display Test");
        config.setWindowedMode(1000, 600);
        config.setIdleFPS(10);
        config.setResizable(false);
        final TestDisplay testDisplay = new TestDisplay();
        config.setWindowListener(new Lwjgl3WindowAdapter() {
            @Override
            public void filesDropped(String[] files) {
                if (files != null && files.length > 0) {
                    if (files[0].endsWith(".vox"))
                        testDisplay.load(files[0]);
                    else if (files[0].endsWith(".hex"))
                        testDisplay.loadPalette(files[0]);
                }
            }
        });

        new Lwjgl3Application(testDisplay, config);
    }
}
