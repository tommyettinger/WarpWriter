import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import warpwriter.Coloring;
import warpwriter.ModelMaker;
import warpwriter.ModelRenderer;
import warpwriter.model.BoxModel;
import warpwriter.model.ColorFetch;
import warpwriter.model.FetchModel;
import warpwriter.model.OffsetModel;

public class FetchTest extends ApplicationAdapter {

    public static final int width = 1280;
    public static final int height = 720;
    protected SpriteBatch batch;
    protected Viewport view;
    protected BitmapFont font;
    protected long seed = 0;
    protected FetchModel model;
    protected OffsetModel offset;
    private ModelMaker modelMaker = new ModelMaker(seed);
    private ModelRenderer modelRenderer = new ModelRenderer(false, true);
    private Texture tex;
    private Pixmap pix;
    private int[] palette = Coloring.RINSED;

    @Override
    public void create() {
        batch = new SpriteBatch();
        view = new FitViewport(width, height);
        font = new BitmapFont(Gdx.files.internal("PxPlus_IBM_VGA_8x16.fnt"));
        model = new FetchModel(20, 20, 20);
        offset = new OffsetModel();
        model.add(offset)
                .add(new BoxModel(model.xSize(), model.ySize(), model.zSize(),
                        ColorFetch.color(modelMaker.randomMainColor()
                        )));
        pix = modelRenderer.renderToPixmap(model, 1, 1);
        tex = new Texture(pix);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        view.getCamera().position.set(width / 2, height / 2, 0);
        view.update(width, height);
        batch.setProjectionMatrix(view.getCamera().combined);
        batch.begin();
        batch.draw(tex, 0, 0);
        font.setColor(Color.BLUE);
        font.draw(batch, "Hello World", width / 2, height / 2);
        batch.end();
    }

    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Fetch Tester");
        config.setWindowedMode(width, height);
        config.setIdleFPS(10);
        final FetchTest app = new FetchTest();
        new Lwjgl3Application(app, config);
    }
}
