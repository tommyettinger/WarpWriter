import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.GdxRuntimeException;
import squidpony.FakeLanguageGen;
import squidpony.StringKit;
import warpwriter.*;

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
    private long seed = 0x1337BEEFD00DL;
    private ModelMaker mm = new ModelMaker(seed);
    private ModelRenderer mr = new ModelRenderer();
    private PaletteReducer reducer;
    private byte[][][] voxels;
    private byte[][][][] animatedVoxels;
    private int dir = 1, counter = 1;
    private static final int background = 0;
    /**
     * The height of the viewing angle, with 0 being directly below (bottom), 1 being at a 45 degree angle from below
     * (below), 2 being at the same height (side), 3 being a sorta-isometric view at a 45 degree angle from above (this
     * is the default and usually isn't mentioned in names), and 4 being directly above (top).
     */
    private int angle = 3;
    private boolean playing = false, rotating = false, tiny = false, large = true, dither = false;
    private int width = 52, height = 64, frames = 8;
    private Pixmap[] pixes = new Pixmap[frames];
    private int[][] indices;
    private int[] palette = Coloring.RINSED;

    @Override
    public void create() {
        reducer = new PaletteReducer(Coloring.RINSED);
        reducer.setDitherStrength(0.5f);
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
                    case Input.Keys.R:
                        rotating = !rotating;
                        return true;
                    case Input.Keys.L:
                        large = !large;
                        return true;
                    case Input.Keys.E: // edge, affects outline
                        mr.hardOutline = !mr.hardOutline;
                        remakeShip(0);
                        return true;
                    case Input.Keys.D: // dither, toggles between Hu or Burkes dithering 
                        dither = !dither;
                        remakeShip(0);
                        return true;
                    case Input.Keys.T:
                        tiny = !tiny;
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
                        dir = 2;
                        remakeShip(0);
                        return true;
                    case Input.Keys.NUMPAD_6:
                    case Input.Keys.NUM_6:
                    case Input.Keys.RIGHT:
                        dir = 3;
                        remakeShip(0);
                        return true;
                    case Input.Keys.NUMPAD_2:
                    case Input.Keys.NUM_2:
                    case Input.Keys.DOWN:
                        dir = 0;
                        remakeShip(0);
                        return true;
                    case Input.Keys.NUMPAD_4:
                    case Input.Keys.NUM_4:
                    case Input.Keys.LEFT:
                        dir = 1;
                        remakeShip(0);
                        return true;
                    case Input.Keys.NUMPAD_7:
                    case Input.Keys.NUM_7:
                        dir = 6;
                        remakeShip(0);
                        return true;
                    case Input.Keys.NUMPAD_9:
                    case Input.Keys.NUM_9:
                        dir = 7;
                        remakeShip(0);
                        return true;
                    case Input.Keys.NUMPAD_3:
                    case Input.Keys.NUM_3:
                        dir = 4;
                        remakeShip(0);
                        return true;
                    case Input.Keys.NUMPAD_1:
                    case Input.Keys.NUM_1:
                        dir = 5;
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
                        name = FakeLanguageGen.SIMPLISH.word(true);
                        VoxIO.writeVOX(name + ".vox", voxels, palette);
                        VoxIO.writeAnimatedVOX(name + "_Animated.vox", animatedVoxels, palette);
//                        for (int f = 0; f < frames; f++) {
//                            VoxIO.writeVOX(name + "_" + f + ".vox", animatedVoxels[f], palette);
//                        }
                        return true;
                    case Input.Keys.SLASH:
                        seed += determine(keycode);
                        return true;
                }
                return true;
            }
        };
        Gdx.input.setInputProcessor(input);
    }

    public void remakeFish(long newModel) {
        mm.rng.setState(determine(newModel));
        voxels = mm.fishRandom();
        //palette = Coloring.ALT_PALETTE;
        animatedVoxels = mm.animateFish(voxels, frames);
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
                        if (dir >= 4) indices = mr.renderIsoBelow(animatedVoxels[f], dir);
                        else indices = mr.renderOrthoBelow(animatedVoxels[f], dir);
                        break;
                    case 3:
                        if (dir >= 4) indices = mr.renderIso(animatedVoxels[f], dir);
                        else indices = mr.renderOrtho(animatedVoxels[f], dir);
                        break;
                    default:
                        if (dir >= 4) indices = mr.renderIsoSide(animatedVoxels[f], dir);
                        else indices = mr.renderOrthoSide(animatedVoxels[f], dir);
                        break;
                }
            }
            for (int x = 0; x < indices.length; x++) {
                for (int y = 0; y < indices[0].length; y++) {
                    pix.drawPixel(x, y, palette[indices[x][y]]);
                }
            }
            if (dither) reducer.reduceWithNoise(pix);
        }
    }

    public void remakeWarrior(long newModel) {
        mm.rng.setState(determine(newModel));
        voxels = mm.warriorRandom();
        if (animatedVoxels == null)
            animatedVoxels = new byte[frames][][][];
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
                        if (dir >= 4) indices = mr.renderIsoBelow(animatedVoxels[f], dir);
                        else indices = mr.renderOrthoBelow(animatedVoxels[f], dir);
                        break;
                    case 3:
                        if (dir >= 4) indices = mr.renderIso(animatedVoxels[f], dir);
                        else indices = mr.renderOrtho(animatedVoxels[f], dir);
                        break;
                    default:
                        if (dir >= 4) indices = mr.renderIsoSide(animatedVoxels[f], dir);
                        else indices = mr.renderOrthoSide(animatedVoxels[f], dir);
                        break;
                }
            }
            for (int x = 0; x < indices.length; x++) {
                for (int y = 0; y < indices[0].length; y++) {
                    pix.drawPixel(x, y, palette[indices[x][y]]);
                }
            }
            if (dither) reducer.reduceWithNoise(pix);
        }
    }

    public void remakeBlob(long newModel) {
        mm.rng.setState(determine(newModel));
        animatedVoxels = mm.animateBlobLargeRandom(frames);
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
                        if (dir >= 4) indices = mr.renderIsoBelow(animatedVoxels[f], dir);
                        else indices = mr.renderOrthoBelow(animatedVoxels[f], dir);
                        break;
                    case 3:
                        if (dir >= 4) indices = mr.renderIso(animatedVoxels[f], dir);
                        else indices = mr.renderOrtho(animatedVoxels[f], dir);
                        break;
                    default:
                        if (dir >= 4) indices = mr.renderIsoSide(animatedVoxels[f], dir);
                        else indices = mr.renderOrthoSide(animatedVoxels[f], dir);
                        break;
                }
            }
            for (int x = 0; x < indices.length; x++) {
                for (int y = 0; y < indices[0].length; y++) {
                    pix.drawPixel(x, y, palette[indices[x][y]]);
                }
            }
            if (dither) reducer.reduceWithNoise(pix);
        }
    }

    public void remakeTerrain(long newModel) {
        mm.rng.setState(determine(newModel));
        int size = 16;
        int color0 = mm.rng.nextInt(30) * 8 + 18,
                color1 = mm.rng.nextInt(30) * 8 + 18,
                color2 = mm.rng.nextInt(30) * 8 + 18,
                color3 = mm.rng.nextInt(30) * 8 + 18;
        voxels = TerrainCube.terrainCube(
                size,
                mm.rng.nextInt(size - 1) + 1,
                mm.rng.nextInt(size - 1) + 1,
                mm.rng.nextInt(size - 1) + 1,
                mm.rng.nextInt(size - 1) + 1,
                mm.rng.nextInt(size - 1) + 1,
                ByteFill.fill3D(mm.rng.nextLong(), (byte)(color0), (byte)(color0+1), (byte)(color0+1), (byte)(color0+2), (byte)(color0+2), (byte)(color0+2), (byte)(color0+3)),
                ByteFill.fill3D(mm.rng.nextLong(), (byte)(color1), (byte)(color1+1), (byte)(color1+1), (byte)(color1+2), (byte)(color1+2), (byte)(color1+2), (byte)(color1+3)),
                ByteFill.fill3D(mm.rng.nextLong(), (byte)(color2), (byte)(color2+1), (byte)(color2+1), (byte)(color2+2), (byte)(color2+2), (byte)(color2+2), (byte)(color2+3)),
                ByteFill.fill3D(mm.rng.nextLong(), (byte)(color3), (byte)(color3+1), (byte)(color3+1), (byte)(color3+2), (byte)(color3+2), (byte)(color3+2), (byte)(color3+3))
        );
        if (animatedVoxels == null)
            animatedVoxels = new byte[frames][][][];
        Arrays.fill(animatedVoxels, voxels);
        int oldWidth = width, oldHeight = height;
        for (int f = 0; f < frames; f++) {
            {
                switch (angle) {
                    case 1:
                        if (dir >= 4) indices = mr.renderIsoBelow(animatedVoxels[f], dir);
                        else indices = mr.renderOrthoBelow(animatedVoxels[f], dir);
                        break;
                    case 3:
                        if (dir >= 4) indices = mr.renderIso(animatedVoxels[f], dir);
                        else indices = mr.renderOrtho(animatedVoxels[f], dir);
                        break;
                    default:
                        if (dir >= 4) indices = mr.renderIsoSide(animatedVoxels[f], dir);
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
            if (dither) reducer.reduceWithNoise(pix);
        }
        if (oldWidth != width || oldHeight != height)
            tex = new Texture(width, height, Pixmap.Format.RGBA8888);
    }

    private VoxelText voxelText = new VoxelText();
    private BitmapFont font;

    public void remakeText(long newModel) {
        mm.rng.setState(determine(newModel));
        if (font == null) {
            font = new BitmapFont(Gdx.files.internal("PxPlus_IBM_VGA_8x16.fnt"));
            //font = new BitmapFont(Gdx.files.internal("tiny.fnt"));
        }

        /*
        // Text3D is sideways!
        voxels = voxelText.text3D(
                font,
                FakeLanguageGen.SIMPLISH.word(mm.rng.nextLong(), true),
                ByteFill.fill2D((byte)(mm.rng.between(18, 22) + mm.rng.nextInt(30) * 8)),
                2
        );
        */

        voxels = ByteFill.fill3D(voxelText.text2D(
                font,
                FakeLanguageGen.SIMPLISH.word(mm.rng.nextLong(), true),
                ByteFill.fill2D((byte) (mm.rng.between(18, 22) + mm.rng.nextInt(30) * 8))
                ),
                2
        );

        if (animatedVoxels == null)
            animatedVoxels = new byte[frames][][][];
        Arrays.fill(animatedVoxels, voxels);
        int oldWidth = width, oldHeight = height;
        for (int f = 0; f < frames; f++) {
            {
                switch (angle) {
                    case 1:
                        if (dir >= 4) indices = mr.renderIsoBelow(animatedVoxels[f], dir);
                        else indices = mr.renderOrthoBelow(animatedVoxels[f], dir);
                        break;
                    case 3:
                        if (dir >= 4) indices = mr.renderIso(animatedVoxels[f], dir);
                        else indices = mr.renderOrtho(animatedVoxels[f], dir);
                        break;
                    default:
                        if (dir >= 4) indices = mr.renderIsoSide(animatedVoxels[f], dir);
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
            if (dither) reducer.reduceWithNoise(pix);
        }
        if (oldWidth != width || oldHeight != height)
            tex = new Texture(width, height, Pixmap.Format.RGBA8888);
    }

    public void remakeShipSmall(long newModel) {
        if (newModel != 0) {
            mm.rng.setState(determine(newModel));
            voxels = mm.shipRandom();
            //palette = Coloring.ALT_PALETTE;
            animatedVoxels = mm.animateShip(voxels, frames);
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
                        if (dir >= 4) indices = mr.renderIsoBelow(animatedVoxels[f], dir);
                        else indices = mr.renderOrthoBelow(animatedVoxels[f], dir);
                        break;
                    case 3:
                        if (dir >= 4) indices = mr.renderIso(animatedVoxels[f], dir);
                        else indices = mr.renderOrtho(animatedVoxels[f], dir);
                        break;
                    default:
                        if (dir >= 4) indices = mr.renderIsoSide(animatedVoxels[f], dir);
                        else indices = mr.renderOrthoSide(animatedVoxels[f], dir);
                        break;
                }
            }
            for (int x = 0; x < indices.length; x++) {
                for (int y = 0; y < indices[0].length; y++) {
                    pix.drawPixel(x, y, palette[indices[x][y]]);
                }
            }
            if (dither) reducer.reduceWithNoise(pix);
        }
    }

    public void remakeShip(long newModel) {
        if (newModel != 0) {
            mm.rng.setState(determine(newModel));
            voxels = large ? mm.shipLargeRandom() : mm.shipRandom();
            //palette = Coloring.ALT_PALETTE;
            animatedVoxels = mm.animateShip(voxels, frames);
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
                        if (dir >= 4) indices = mr.renderIsoBelow(animatedVoxels[f], dir);
                        else indices = mr.renderOrthoBelow(animatedVoxels[f], dir);
                        break;
                    case 3:
                        if (dir >= 4) indices = mr.renderIso(animatedVoxels[f], dir);
                        else indices = mr.renderOrtho(animatedVoxels[f], dir);
                        break;
                    default:
                        if (dir >= 4) indices = mr.renderIsoSide(animatedVoxels[f], dir);
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
            if (dither) reducer.reduceWithNoise(pix);
        }
        if (oldWidth != width || oldHeight != height)
            tex = new Texture(width, height, Pixmap.Format.RGBA8888);
    }

    public void remakeFull(long newModel) {

        mm.rng.setState(determine(newModel));
        voxels = mm.fullyRandom(large);
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
                        if (dir >= 4) indices = mr.renderIsoBelow(animatedVoxels[f], dir);
                        else indices = mr.renderOrthoBelow(animatedVoxels[f], dir);
                        break;
                    case 3:
                        if (dir >= 4) indices = mr.renderIso(animatedVoxels[f], dir);
                        else indices = mr.renderOrtho(animatedVoxels[f], dir);
                        break;
                    default:
                        if (dir >= 4) indices = mr.renderIsoSide(animatedVoxels[f], dir);
                        else indices = mr.renderOrthoSide(animatedVoxels[f], dir);
                        break;
                }
            }
            for (int x = 0; x < indices.length; x++) {
                for (int y = 0; y < indices[0].length; y++) {
                    pix.drawPixel(x, y, palette[indices[x][y]]);
                }
            }
            if (dither) reducer.reduceWithNoise(pix);
        }
        if (oldWidth != width || oldHeight != height)
            tex = new Texture(width, height, Pixmap.Format.RGBA8888);
    }


    public void load(String name) {
        try {
            System.out.println(name);
            voxels = VoxIO.readVox(new LittleEndianDataInputStream(new FileInputStream(name)));
            /// this is commented out because it's pretty hard to make a working palette by hand; this uses the default
            //palette = VoxIO.lastPalette;
            //reducer.exact(VoxIO.lastPalette);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }
        Arrays.fill(animatedVoxels, voxels);
        int oldWidth = width, oldHeight = height;
        for (int f = 0; f < frames; f++) {
            if (tiny && !large) indices = mr.renderIso24x32(animatedVoxels[f], dir);
            else {
                switch (angle) {
                    case 1:
                        if (dir >= 4) indices = mr.renderIsoBelow(animatedVoxels[f], dir);
                        else indices = mr.renderOrthoBelow(animatedVoxels[f], dir);
                        break;
                    case 2:
                        if (dir >= 4) indices = mr.renderIsoSide(animatedVoxels[f], dir);
                        else indices = mr.renderOrthoSide(animatedVoxels[f], dir);
                        break;
                    default:
                        if (dir >= 4) indices = mr.renderIso(animatedVoxels[f], dir);
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
            if (dither) reducer.reduceWithNoise(pix);
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
                        if (dir >= 4) indices = mr.renderIsoBelow(animatedVoxels[f], dir);
                        else indices = mr.renderOrthoBelow(animatedVoxels[f], dir);
                        break;
                    case 3:
                        if (dir >= 4) indices = mr.renderIso(animatedVoxels[f], dir);
                        else indices = mr.renderOrtho(animatedVoxels[f], dir);
                        break;
                    default:
                        if (dir >= 4) indices = mr.renderIsoSide(animatedVoxels[f], dir);
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
            if (dither) reducer.reduceWithNoise(pix);
        }
    }

    @Override
    public void render() {
        int time = (playing ? ++counter : counter) % ((frames << 1) * 6), tempDir; //(tiny && !large ? 0 : 1)
        if (time == 0 && rotating) {
            ++dir;
            tempDir = (dir &= 7);
            dir = (dir << 2 & 4) | ((dir >> 1) + (dir & 1) & 3);
            remakeShip(0);
            dir = tempDir;
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
        batch.draw(tex, width * 19 >> 1, 250 - (width << 2), width << 3, height << 3);

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
