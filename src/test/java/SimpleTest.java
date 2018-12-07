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
import warpwriter.model.*;
import warpwriter.view.SpriteBatchVoxelRenderer;
import warpwriter.view.Turnable;

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
    protected Turnable turnable;
    private SpriteBatchVoxelRenderer batchRenderer;
    protected boolean z45 = false;
    protected int angle = 2;
    protected byte[][][] box;

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

        batchRenderer = new SpriteBatchVoxelRenderer(batch);

        maker = new ModelMaker(12345);
//        try {
//            box = VoxIO.readVox(new LittleEndianDataInputStream(SimpleTest.class.getResourceAsStream("/dumbcube.vox")));
//        } catch (Exception e) {
//            e.printStackTrace();
//            box = maker.warriorRandom();
//        }
        // uses a 13x12x8 model to test SimpleDraw's support for odd-number sizes
//        turnModel = new TurnModel(knightModel
//                .boxModel(13, 12, 8, ColorFetch.color(Coloring.rinsed("Red 4")))
//                .model(13, 12, 8)
//        );
//        turnModel = new TurnModel(knightModel);
//        turnModel = new TurnModel(new BoxModel(knightModel,
//                ColorFetch.color(Coloring.rinsed("Red 4"))
//        ));

        //reDraw();
        turnable = new Turnable()
                .set(batchRenderer)
                .setOffset(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2)
                .set(new TurnModel().set(new ArrayModel(maker.warriorRandom())));
        Gdx.input.setInputProcessor(inputProcessor());
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

        font.draw(batch, StringKit.join(", ", turnable.getModel().sizeX(), turnable.getModel().sizeY(), turnable.getModel().sizeZ()) + " (original)", 0, 80);
        font.draw(batch, turnable.turnModel().sizeX() + ", " + turnable.turnModel().sizeY() + ", " + turnable.turnModel().sizeZ() + " (modified)", 0, 60);
        font.draw(batch, StringKit.join(", ", turnable.turnModel().turner().rotation()) + " (rotation)", 0, 40);
        font.draw(batch, Gdx.graphics.getFramesPerSecond() + " FPS", 0, 20);

        turnable.render();

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

    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Simple Tester");
        config.setWindowedMode(SCREEN_WIDTH, SCREEN_HEIGHT);
        config.setIdleFPS(10);
        config.useVsync(false);
        final SimpleTest app = new SimpleTest();
        new Lwjgl3Application(app, config);
    }

    public InputProcessor inputProcessor() {
        return new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                switch (keycode) {
                    case Input.Keys.NUM_0:
                        angle = 1;
                        break;
                    case Input.Keys.MINUS:
                        angle = 2;
                        break;
                    case Input.Keys.EQUALS:
                        angle = 3;
                        break;
                    case Input.Keys.U:
                        turnable.clockX();
                        break;
                    case Input.Keys.I:
                        turnable.clockY();
                        break;
                    case Input.Keys.O:
                        turnable.clockZ();
                        break;
                    case Input.Keys.J:
                        turnable.counterX();
                        break;
                    case Input.Keys.K:
                        turnable.counterY();
                        break;
                    case Input.Keys.L:
                        turnable.counterZ();
                        break;
                    case Input.Keys.R:
                        turnable.reset();
                        break;
                    case Input.Keys.P:
                        turnable.set(
                                new ArrayModel(maker.warriorRandom())
                                        .boxModel(13, 12, 8,
                                                ColorFetch.color(Coloring.rinsed("Red 4"))
                                        )
                                        .model(13, 12, 8)
                        );
                        break;
                    case Input.Keys.S:
                        turnable.set(new FetchModel(20, 20, 20,
                                new NoiseFetch((byte) 194, (byte) 194))); //(byte) 0, (byte) 0, (byte) 0,   , (byte) 0, (byte) 0, (byte) 0   , (byte) 98, (byte) 130, (byte) 162 
                        break;
                    case Input.Keys.G:
                        batchRenderer.color().set(batchRenderer.color().direction().counter());
                        break;
                    case Input.Keys.H:
                        batchRenderer.color().set(batchRenderer.color().direction().clock());
                        break;
                    case Input.Keys.T: // try again
                        turnable.reset();
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
