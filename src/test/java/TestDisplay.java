import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
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
    @Override
    public void create() {
        batch = new SpriteBatch();
        Pixmap pix = new Pixmap(16, 16, Pixmap.Format.RGBA8888);
        ModelMaker mm = new ModelMaker();
        byte[][][] voxels = mm.fullyRandom();
        ModelRenderer mr = new ModelRenderer();
        int[][] indices = mr.render16x16(voxels, 0);
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                pix.drawPixel(x, y, Coloring.CW_PALETTE[indices[x][y]]);
            }
        }
        tex = new Texture(16, 16, Pixmap.Format.RGBA8888);
        tex.draw(pix, 0, 0);
    }

    @Override
    public void render() {
        // standard clear the background routine for libGDX
        Gdx.gl.glClearColor(0.7f, 0.99f, 0.6f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.draw(tex, 64 -  8, 240 -  8, 16, 16);
        batch.draw(tex, 192 - 16, 240 - 16, 32, 32);
        batch.draw(tex, 320 - 32, 240 - 32, 64, 64);
        batch.draw(tex, 512 - 64, 240 - 64, 128, 128);
        batch.end();
    }

    public static void main (String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "Display Test";
        config.width = 640;
        config.height = 480;
        new LwjglApplication(new TestDisplay(), config);
    }

}
