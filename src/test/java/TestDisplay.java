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
import warpwriter.Coloring;
import warpwriter.ModelMaker;
import warpwriter.ModelRenderer;

/**
 * Created by Tommy Ettinger on 11/4/2017.
 */
public class TestDisplay extends ApplicationAdapter {
    SpriteBatch batch;
    Texture tex;
    Pixmap pix;
    ModelMaker mm = new ModelMaker();
    ModelRenderer mr = new ModelRenderer();
    InputAdapter input;
    byte[][][] voxels;
    int dir = 0;

    @Override
    public void create() {
        batch = new SpriteBatch();
        pix = new Pixmap(16, 16, Pixmap.Format.RGBA8888);
        tex = new Texture(16, 16, Pixmap.Format.RGBA8888);
        remake(true);
        input = new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                switch (keycode) {
                    case Input.Keys.Q:
                        Gdx.app.exit();
                        return true;
                    case Input.Keys.SPACE:
                        remake(true);
                        return true;
                    case Input.Keys.UP:
                        dir = 0;
                        remake(false);
                        return true;
                    case Input.Keys.RIGHT:
                        dir = 1;
                        remake(false);
                        return true;
                    case Input.Keys.DOWN:
                        dir = 2;
                        remake(false);
                        return true;
                    case Input.Keys.LEFT:
                        dir = 3;
                        remake(false);
                        return true;
                    default:
                        return true;
                }
            }
        };
        Gdx.input.setInputProcessor(input);
    }

    public void remake(boolean newModel) {
        if (newModel) voxels = mm.shipRandom();
        pix.setColor(0);
        pix.fill();
        int[][] indices = mr.render16x16(voxels, dir);
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                pix.drawPixel(x, y, Coloring.CW_PALETTE[indices[x][y]]);
            }
        }
        tex.draw(pix, 0, 0);
    }

    @Override
    public void render() {
        // standard clear the background routine for libGDX
        Gdx.gl.glClearColor(0.7f, 0.99f, 0.6f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.draw(tex, 64 - 8, 240 - 8, 16, 16);
        batch.draw(tex, 192 - 16, 240 - 16, 32, 32);
        batch.draw(tex, 320 - 32, 240 - 32, 64, 64);
        batch.draw(tex, 512 - 64, 240 - 64, 128, 128);
        batch.end();
    }

    public static void main(String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "Display Test";
        config.width = 640;
        config.height = 480;
        new LwjglApplication(new TestDisplay(), config);
    }
}
