import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import squidpony.FakeLanguageGen;
import squidpony.squidmath.ThrustAltRNG;
import warpwriter.Coloring;
import warpwriter.ModelMaker;
import warpwriter.ModelRenderer;
import warpwriter.VoxIO;

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
    private long seed = 0x1337BEEFL;
    private ModelMaker mm = new ModelMaker(seed);
    private ModelRenderer mr = new ModelRenderer();
    private byte[][][] voxels;
    private byte[][][][] animatedVoxels;
    private int dir = 1;
    private final int width = 60, height = 68, frames = 8;
    private Pixmap[] pixes = new Pixmap[frames];
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
                    case Input.Keys.SPACE:
                        remakeShip(++seed);
                        return true;
                    case Input.Keys.UP:
                    case Input.Keys.NUMPAD_8:
                        dir = 2;
                        remakeShip(0);
                        return true;
                    case Input.Keys.NUMPAD_6:
                    case Input.Keys.RIGHT:
                        dir = 3;
                        remakeShip(0);
                        return true;
                    case Input.Keys.NUMPAD_2:
                    case Input.Keys.DOWN:
                        dir = 0;
                        remakeShip(0);
                        return true;
                    case Input.Keys.NUMPAD_4:
                    case Input.Keys.LEFT:
                        dir = 1;
                        remakeShip(0);
                        return true;

                    case Input.Keys.NUMPAD_7:
                        dir = 6;
                        remakeShip(0);
                        return true;
                    case Input.Keys.NUMPAD_9:
                        dir = 7;
                        remakeShip(0);
                        return true;
                    case Input.Keys.NUMPAD_3:
                        dir = 4;
                        remakeShip(0);
                        return true;
                    case Input.Keys.NUMPAD_1:
                        dir = 5;
                        remakeShip(0);
                        return true;

                    case Input.Keys.F:
                        remakeFish(++seed);
                        return true;
                    case Input.Keys.W:
                        dir = 2;
                        remakeFish(0);
                        return true;
                    case Input.Keys.A:
                        dir = 1;
                        remakeShip(0);
                        return true;
                    case Input.Keys.S:
                        dir = 0;
                        remakeFish(0);
                        return true;
                    case Input.Keys.D:
                        dir = 3;
                        remakeFish(0);
                        return true;
                    case Input.Keys.O: // output
                        name = FakeLanguageGen.SIMPLISH.word(true);
                        VoxIO.writeVOX(name + ".vox", voxels, Coloring.CW_PALETTE);
                        VoxIO.writeAnimatedVOX(name + "_Animated.vox", animatedVoxels, Coloring.CW_PALETTE);
//                        for (int f = 0; f < frames; f++) {
//                            VoxIO.writeVOX(name + "_" + f + ".vox", animatedVoxels[f], Coloring.CW_PALETTE);
//                        }
                        return true;
                    default:
                        seed += ThrustAltRNG.determine(keycode);
                        return true;
                }
            }
        };
        Gdx.input.setInputProcessor(input);
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
        if (newModel != 0){
            mm.thrust.state = ThrustAltRNG.determine(newModel);
            voxels = mm.shipRandom();
            animatedVoxels = mm.animateShip(voxels, frames);
        }
        for (int f = 0; f < frames; f++) {
            pix = pixes[f];
            pix.setColor(0);
            pix.fill();
            int[][] indices = dir >= 4 ? mr.renderIso(animatedVoxels[f], dir) : mr.renderOrtho(animatedVoxels[f], dir);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    pix.drawPixel(x, y, Coloring.CW_PALETTE[indices[x][y]]);
                }
            }
        }
    }

    @Override
    public void render() {
        tex.draw(pixes[(int) ((System.currentTimeMillis() >> 7) % frames)], 0, 0);

        // standard clear the background routine for libGDX
        Gdx.gl.glClearColor(0.7f, 0.99f, 0.6f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.draw(tex, width >> 1, 250 - (width >> 1), width, height);
        batch.draw(tex, width * 3 >> 1, 250 - (width), width << 1, height << 1);
        batch.draw(tex, width * 3, 250 - (width << 1), width << 2,  height << 2);
        batch.draw(tex, width * 6, 250 - (width << 2), width << 3, height << 3);

//        batch.draw(tex, 64 - 8, 240 - 8, 16, 16);
//        batch.draw(tex, 192 - 16, 240 - 16, 32, 32);
//        batch.draw(tex, 320 - 32, 240 - 32, 64, 64);
//        batch.draw(tex, 512 - 64, 240 - 64, 128, 128);
        batch.end();
    }

    public static void main(String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "Display Test";
        config.width = 800;
        config.height = 500;
        new LwjglApplication(new TestDisplay(), config);
    }
}
