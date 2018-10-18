import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import warpwriter.model.FetchModel;
import warpwriter.model.OffsetModel;

public class FetchTest extends ApplicationAdapter {

    public static final int width = 1280;
    public static final int height = 720;
    protected SpriteBatch batch;
    protected Viewport view;
    protected BitmapFont font;
    protected long seed=0;
    protected FetchModel model;
    protected OffsetModel offset;

    @Override
    public void create() {
        batch = new SpriteBatch();
        view = new FitViewport(width, height);
        font = new BitmapFont(Gdx.files.internal("PxPlus_IBM_VGA_8x16.fnt"));
        model = new FetchModel();
        offset = new OffsetModel();
        model.set(20, 20, 20).add(offset);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        view.getCamera().position.set(width / 2, height / 2, 0);
        view.update(width, height);
        batch.setProjectionMatrix(view.getCamera().combined);
        batch.begin();
        font.setColor(Color.BLUE);
        font.draw(batch, "Hello World", width / 2, height / 2);
        batch.end();
    }

    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle( "Fetch Tester");
        config.setWindowedMode(width, height);
        config.setIdleFPS(10);
        final FetchTest app = new FetchTest();
        new Lwjgl3Application(app, config);
    }
}
