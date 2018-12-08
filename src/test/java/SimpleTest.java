import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import squidpony.StringKit;
import warpwriter.Coloring;
import warpwriter.ModelMaker;
import warpwriter.model.IModel;
import warpwriter.model.fetch.ArrayModel;
import warpwriter.model.fetch.BoxModel;
import warpwriter.model.fetch.ColorFetch;
import warpwriter.view.VoxelSprite;
import warpwriter.view.VoxelSpriteBatchRenderer;

public class SimpleTest extends ApplicationAdapter {
    public static final int SCREEN_WIDTH = 1280;
    public static final int SCREEN_HEIGHT = 720;
    public static final int VIRTUAL_WIDTH = 640;
    public static final int VIRTUAL_HEIGHT = 360;
    protected SpriteBatch batch;
    protected Viewport worldView;
    protected Viewport screenView;
    protected BitmapFont font;
    protected FrameBuffer buffer;
    protected Texture screenTexture;
    protected TextureRegion screenRegion;
    protected ModelMaker maker;
    protected VoxelSprite voxelSprite;
    protected boolean box = false;
    private VoxelSpriteBatchRenderer batchRenderer;

    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Simple Tester");
        config.setWindowedMode(SCREEN_WIDTH, SCREEN_HEIGHT);
        config.setIdleFPS(10);
        config.useVsync(false);
        final SimpleTest app = new SimpleTest();
        new Lwjgl3Application(app, config);
    }

    @Override
    public void create() {
        font = new BitmapFont(Gdx.files.internal("PxPlus_IBM_VGA_8x16.fnt"));
        batch = new SpriteBatch();
        worldView = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        screenView = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        buffer = new FrameBuffer(Pixmap.Format.RGBA8888, VIRTUAL_WIDTH, VIRTUAL_HEIGHT, false, false);
        screenRegion = new TextureRegion();
        screenView.getCamera().position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);
        screenView.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.enableBlending();

        maker = new ModelMaker(12345);
        batchRenderer = new VoxelSpriteBatchRenderer(batch);
        voxelSprite = new VoxelSprite()
                .set(batchRenderer)
                .setOffset(VIRTUAL_WIDTH / 2, 100);
        makeModel();
        Gdx.input.setInputProcessor(inputProcessor());
    }

    public void makeModel() {
        voxelSprite.set(
                box ?
                        new BoxModel(model(), ColorFetch.color(Coloring.rinsed("Powder Blue 3")))
                        : model()
        );
    }

    public IModel model() {
        return new ArrayModel(maker.warriorRandom());
    }

    @Override
    public void render() {
        buffer.begin();
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        worldView.apply();
        worldView.getCamera().position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);
        worldView.update(VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        batch.setProjectionMatrix(worldView.getCamera().combined);
        batch.begin();

        font.draw(batch, StringKit.join(", ", voxelSprite.getModel().sizeX(), voxelSprite.getModel().sizeY(), voxelSprite.getModel().sizeZ()) + " (original)", 0, 80);
        font.draw(batch, voxelSprite.turnModel().sizeX() + ", " + voxelSprite.turnModel().sizeY() + ", " + voxelSprite.turnModel().sizeZ() + " (modified)", 0, 60);
        font.draw(batch, StringKit.join(", ", voxelSprite.turnModel().turner().rotation()) + " (rotation)", 0, 40);
        font.draw(batch, Gdx.graphics.getFramesPerSecond() + " FPS", 0, 20);

        voxelSprite.render();

        batch.setColor(-0x1.fffffep126f); // white as a packed float, resets any color changes that the renderer made
        batch.end();
        buffer.end();
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        screenView.apply();
        batch.setProjectionMatrix(screenView.getCamera().combined);
        batch.begin();
        screenTexture = buffer.getColorBufferTexture();
        screenTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        screenRegion.setRegion(screenTexture);
        screenRegion.flip(false, true);
        batch.draw(screenRegion, 0, 0);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        screenView.update(width, height);
    }

    public InputProcessor inputProcessor() {
        return new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                switch (keycode) {
                    case Input.Keys.NUM_0:
                        voxelSprite.setAngle(1);
                        break;
                    case Input.Keys.MINUS:
                        voxelSprite.setAngle(2);
                        break;
                    case Input.Keys.EQUALS:
                        voxelSprite.setAngle(3);
                        break;
                    case Input.Keys.U:
                        voxelSprite.clockX();
                        break;
                    case Input.Keys.I:
                        voxelSprite.clockY();
                        break;
                    case Input.Keys.O:
                        voxelSprite.clockZ();
                        break;
                    case Input.Keys.J:
                        voxelSprite.counterX();
                        break;
                    case Input.Keys.K:
                        voxelSprite.counterY();
                        break;
                    case Input.Keys.L:
                        voxelSprite.counterZ();
                        break;
                    case Input.Keys.R:
                        voxelSprite.reset();
                        break;
                    case Input.Keys.P:
                        makeModel();
                        break;
                    case Input.Keys.B:
                        box = !box;
                        makeModel();
                        break;
                    case Input.Keys.G:
                        batchRenderer.color().set(batchRenderer.color().direction().counter());
                        break;
                    case Input.Keys.H:
                        batchRenderer.color().set(batchRenderer.color().direction().clock());
                        break;
                    case Input.Keys.T: // try again
                        voxelSprite.reset();
                        break;
                    case Input.Keys.ESCAPE:
                        Gdx.app.exit();
                        break;
                }
                return true;
            }
        };
    }
}
