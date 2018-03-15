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
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import squidpony.FakeLanguageGen;
import squidpony.squidmath.ThrustAltRNG;
import warpwriter.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;

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
    private byte[][][] voxels;
    private byte[][][][] animatedVoxels;
    private int dir = 1, counter = 0;
    /**
     * The height of the viewing angle, with 0 being directly below (bottom), 1 being at a 45 degree angle from below
     * (below), 2 being at the same height (side), 3 being a sorta-isometric view at a 45 degree angle from above (this
     * is the default and usually isn't mentioned in names), and 4 being directly above (top).
     */
    private int angle = 3;
    private boolean playing = true, tiny = false;
    private final int width = 52, height = 64, frames = 8;
    private Pixmap[] pixes = new Pixmap[frames];
    private int[] palette = Coloring.ALT_PALETTE;
    @Override
    public void create() {
        batch = new SpriteBatch();
//        pix = new Pixmap(16, 16, Pixmap.Format.RGBA8888);
//        tex = new Texture(16, 16, Pixmap.Format.RGBA8888);
        for (int i = 0; i < frames; i++) {
            pixes[i] = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        }
        pix = pixes[0];
        tex = new Texture(width, height, Pixmap.Format.RGBA8888);
        remakeShip(seed);
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
                    case Input.Keys.T:
                        tiny = !tiny;
                        return true;
                    case Input.Keys.B: // below
                        angle = 1;
                        return true;
                    case Input.Keys.S: // side
                        angle = 2;
                        return true;
                    case Input.Keys.A: // above
                        angle = 3;
                        return true;
                    case Input.Keys.H: // height cycling
                        angle = (angle % 3) + 1;
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
                    case Input.Keys.O: // output
                        name = FakeLanguageGen.SIMPLISH.word(true);
                        VoxIO.writeVOX(name + ".vox", voxels, palette);
                        VoxIO.writeAnimatedVOX(name + "_Animated.vox", animatedVoxels, palette);
//                        for (int f = 0; f < frames; f++) {
//                            VoxIO.writeVOX(name + "_" + f + ".vox", animatedVoxels[f], palette);
//                        }
                        return true;
                    case Input.Keys.PERIOD:
                        seed += ThrustAltRNG.determine(keycode);
                        return true;
                }
                return true;
            }
        };
        Gdx.input.setInputProcessor(input);
    }

    public void remakeFish(long newModel) {         
        mm.thrust.state = ThrustAltRNG.determine(newModel);
        voxels = mm.fishRandom();
        palette = Coloring.ALT_PALETTE;
        animatedVoxels = mm.animateFish(voxels, frames);
        
        for (int f = 0; f < frames; f++) {
            pix = pixes[f];
            pix.setColor(0);
            pix.fill();
            int[][] indices;
            if(tiny) indices = mr.renderIso24x32(animatedVoxels[f], dir);
            else
            {
                switch (angle)
                {
                    case 1: if(dir >= 4) indices = mr.renderIsoBelow(animatedVoxels[f], dir);
                    else indices = mr.renderOrthoBelow(animatedVoxels[f], dir);
                        break;
                    case 2: if(dir >= 4) indices = mr.renderIsoSide(animatedVoxels[f], dir);
                    else indices = mr.renderOrthoSide(animatedVoxels[f], dir);
                        break;
                    default: if(dir >= 4) indices = mr.renderIso(animatedVoxels[f], dir);
                    else indices = mr.renderOrtho(animatedVoxels[f], dir);
                        break;
                }
            }
            for (int x = 0; x < indices.length; x++) {
                for (int y = 0; y < indices[0].length; y++) {
                    pix.drawPixel(x, y, palette[indices[x][y]]);
                }
            }
        }
    }

    public void remakeShip(long newModel) {
        if (newModel != 0){
            mm.thrust.state = ThrustAltRNG.determine(newModel);
            voxels = mm.shipRandom();
            palette = Coloring.ALT_PALETTE;
            animatedVoxels = mm.animateShip(voxels, frames);
        }
        for (int f = 0; f < frames; f++) {
            pix = pixes[f];
            pix.setColor(0);
            pix.fill();
            int[][] indices;
            if(tiny) indices = mr.renderIso24x32(animatedVoxels[f], dir);
            else
            {
                switch (angle)
                {
                    case 1: if(dir >= 4) indices = mr.renderIsoBelow(animatedVoxels[f], dir);
                    else indices = mr.renderOrthoBelow(animatedVoxels[f], dir);
                        break;
                    case 2: if(dir >= 4) indices = mr.renderIsoSide(animatedVoxels[f], dir);
                    else indices = mr.renderOrthoSide(animatedVoxels[f], dir);
                        break;
                    default: if(dir >= 4) indices = mr.renderIso(animatedVoxels[f], dir);
                    else indices = mr.renderOrtho(animatedVoxels[f], dir);
                        break;
                }
            }
            for (int x = 0; x < indices.length; x++) {
                for (int y = 0; y < indices[0].length; y++) {
                    pix.drawPixel(x, y, palette[indices[x][y]]);
                }
            }
        }
    }

    public void remakeFull(long newModel) {

        mm.thrust.state = ThrustAltRNG.determine(newModel);
        voxels = mm.fullyRandom();
        palette = Coloring.ALT_PALETTE;
        Arrays.fill(animatedVoxels, voxels);

        for (int f = 0; f < frames; f++) {
            pix = pixes[f];
            pix.setColor(0);
            pix.fill();
            int[][] indices;
            if(tiny) indices = mr.renderIso24x32(animatedVoxels[f], dir);
            else
            {
                switch (angle)
                {
                    case 1: if(dir >= 4) indices = mr.renderIsoBelow(animatedVoxels[f], dir);
                    else indices = mr.renderOrthoBelow(animatedVoxels[f], dir);
                        break;
                    case 2: if(dir >= 4) indices = mr.renderIsoSide(animatedVoxels[f], dir);
                    else indices = mr.renderOrthoSide(animatedVoxels[f], dir);
                        break;
                    default: if(dir >= 4) indices = mr.renderIso(animatedVoxels[f], dir);
                    else indices = mr.renderOrtho(animatedVoxels[f], dir);
                        break;
                }
            }
            for (int x = 0; x < indices.length; x++) {
                for (int y = 0; y < indices[0].length; y++) {
                    pix.drawPixel(x, y, palette[indices[x][y]]);
                }
            }
        }
    }


    public void load(String name) {

        try {
            voxels = VoxIO.readVox(new LittleEndianDataInputStream(new FileInputStream(name)));
            /// this is commented out because it's pretty hard to make a working palette by hand; this uses the default
            //palette = VoxIO.lastPalette;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }
        Arrays.fill(animatedVoxels, voxels);

        for (int f = 0; f < frames; f++) {
            pix = pixes[f];
            pix.setColor(0);
            pix.fill();
            int[][] indices;
            if(tiny) indices = mr.renderIso24x32(animatedVoxels[f], dir);
            else
            {
                switch (angle)
                {
                    case 1: if(dir >= 4) indices = mr.renderIsoBelow(animatedVoxels[f], dir);
                    else indices = mr.renderOrthoBelow(animatedVoxels[f], dir);
                        break;
                    case 2: if(dir >= 4) indices = mr.renderIsoSide(animatedVoxels[f], dir);
                    else indices = mr.renderOrthoSide(animatedVoxels[f], dir);
                        break;
                    default: if(dir >= 4) indices = mr.renderIso(animatedVoxels[f], dir);
                    else indices = mr.renderOrtho(animatedVoxels[f], dir);
                        break;
                }
            }
            for (int x = 0; x < indices.length; x++) {
                for (int y = 0; y < indices[0].length; y++) {
                    pix.drawPixel(x, y, palette[indices[x][y]]);
                }
            }
        }
    }

    @Override
    public void render() {
        int time = (playing ? ++counter : counter) % ((frames << (tiny ? 0 : 1)) * 6), tempDir;
        if(time == 0)
        {
            ++dir;
            tempDir = (dir &= 7);
            dir = (dir << 2 & 4) | ((dir >> 1) + (dir & 1) & 3);
            remakeShip(0);
            dir = tempDir;
        }
        tex.draw(pixes[(time / 6) % frames], 0, 0);

        // standard clear the background routine for libGDX
        Gdx.gl.glClearColor(0.7f, 0.99f, 0.6f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        batch.draw(tex, width >> 1, 250 - (width >> 1), width, height);
        batch.draw(tex, width * 5 >> 1, 250 - (width), width << 1, height << 1);
        batch.draw(tex, width * 10 >> 1, 250 - (width << 1), width << 2,  height << 2);
        batch.draw(tex, width * 19 >> 1, 250 - (width << 2), width << 3, height << 3);

//        batch.draw(tex, 64 - 8, 240 - 8, 16, 16);
//        batch.draw(tex, 192 - 16, 240 - 16, 32, 32);
//        batch.draw(tex, 320 - 32, 240 - 32, 64, 64);
//        batch.draw(tex, 512 - 64, 240 - 64, 128, 128);
        batch.end();
    }

    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle( "Display Test");
        config.setWindowedMode(1000, 600);
        config.setIdleFPS(10);
        final TestDisplay testDisplay = new TestDisplay();
        config.setWindowListener(new Lwjgl3WindowAdapter() {
            @Override
            public void filesDropped(String[] files) {
                if (files != null && files.length > 0) { 
                    testDisplay.load(files[0]);
                }
            }
        });
			
        new Lwjgl3Application(testDisplay, config);
    }
}
