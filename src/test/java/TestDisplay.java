import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import warpwriter.Coloring;

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
        int idx = 8;
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                pix.drawPixel(x, y, Coloring.CW_PALETTE[idx++]);
                if(idx >= 184) idx = 8;
            }
        }
        tex = new Texture(16, 16, Pixmap.Format.RGBA8888);
        tex.draw(pix, 0, 0);
    }

    @Override
    public void render() {
        // standard clear the background routine for libGDX
        Gdx.gl.glClearColor(0f, 0f, 0f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.draw(tex, 288, 208, 64, 64);
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
