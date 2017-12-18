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
import squidpony.squidmath.ThrustRNG;
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
    private long seed = 0x31337BEEFC0FFEEL;
    private ModelMaker mm = new ModelMaker(seed);
    private ModelRenderer mr = new ModelRenderer();
    private byte[][][] voxels;
    private byte[][][][] animatedVoxels;
    private int dir = 1;
    private final int width = 32, height = 46, frames = 8;
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
        remake(seed);
        InputAdapter input = new InputAdapter() {
            String name = "Wriggler";
            @Override
            public boolean keyDown(int keycode) {
                switch (keycode) {
                    case Input.Keys.Q:
                        Gdx.app.exit();
                        return true;
                    case Input.Keys.SPACE:
                        remake(++seed);
                        return true;
                    case Input.Keys.UP:
                        dir = 2;
                        remake(0);
                        return true;
                    case Input.Keys.RIGHT:
                        dir = 3;
                        remake(0);
                        return true;
                    case Input.Keys.DOWN:
                        dir = 0;
                        remake(0);
                        return true;
                    case Input.Keys.LEFT:
                        dir = 1;
                        remake(0);
                        return true;
                    case Input.Keys.W:
                        name = FakeLanguageGen.SIMPLISH.word(true);
                        VoxIO.writeVOX(name + ".vox", voxels, Coloring.CW_PALETTE);
                        VoxIO.writeAnimatedVOX(name + "_Animated.vox", animatedVoxels, Coloring.CW_PALETTE);
//                        for (int f = 0; f < frames; f++) {
//                            VoxIO.writeVOX(name + "_" + f + ".vox", animatedVoxels[f], Coloring.CW_PALETTE);
//                        }
                        return true;
                    default:
                        seed += ThrustRNG.determine(keycode);
                        return true;
                }
            }
        };
        Gdx.input.setInputProcessor(input);
    }

    public void remake(long newModel) {
        if (newModel != 0){
            mm.thrust.state = ThrustRNG.determine(newModel);
            voxels = mm.fishRandom();
            animatedVoxels = mm.animateFish(voxels, frames);
        }
        for (int f = 0; f < frames; f++) {
            pix = pixes[f];
            pix.setColor(0);
            pix.fill();
            int[][] indices = mr.renderIso(animatedVoxels[f], dir);
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
        batch.draw(tex, width * 2, 200 - (width >> 1), width, height);
        batch.draw(tex, width * 4, 200 - (width), width << 1, height << 1);
        batch.draw(tex, width * 7, 200 - (width << 1), width << 2,  height << 2);
        batch.draw(tex, width * 11, 200 - (width << 2), width << 3, height << 3);

//        batch.draw(tex, 64 - 8, 240 - 8, 16, 16);
//        batch.draw(tex, 192 - 16, 240 - 16, 32, 32);
//        batch.draw(tex, 320 - 32, 240 - 32, 64, 64);
//        batch.draw(tex, 512 - 64, 240 - 64, 128, 128);
        batch.end();
    }

    public static void main(String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "Display Test";
        config.width = 600;
        config.height = 400;
        new LwjglApplication(new TestDisplay(), config);
    }
}
