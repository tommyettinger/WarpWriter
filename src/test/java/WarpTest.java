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
import warpwriter.LittleEndianDataInputStream;
import warpwriter.ModelMaker;
import warpwriter.Tools3D;
import warpwriter.VoxIO;
import warpwriter.model.TurnModel;
import warpwriter.model.fetch.ArrayModel;
import warpwriter.view.*;

import java.io.FileInputStream;

public class WarpTest extends ApplicationAdapter {
    public static final int SCREEN_WIDTH = 1280;
    public static final int SCREEN_HEIGHT = 720;
    public static final int VIRTUAL_WIDTH = 640;
    public static final int VIRTUAL_HEIGHT = 360;
    protected SpriteBatch batch;
    protected Viewport worldView;
    protected Viewport screenView;
    protected BitmapFont font;
    protected FrameBuffer buffer;
    protected Texture screenTexture, pmTexture;
    protected TextureRegion screenRegion;
    protected TurnModel model, dumbCube, warrior;
    protected ModelMaker maker;
    private VoxelSpriteBatchRenderer batchRenderer;
    private VoxelPixmapRenderer pixmapRenderer;
    protected VoxelColor voxelColor;
    protected int angle = 2;
    protected boolean diagonal = false, outline = true;
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

        voxelColor = new VoxelColor().set(Twilight.AuroraTwilight);
        batchRenderer = new VoxelSpriteBatchRenderer(batch).setOffset(16, 100);
        pixmapRenderer = new VoxelPixmapRenderer(new Pixmap(512, 512, Pixmap.Format.RGBA8888), voxelColor);
        pmTexture = new Texture(pixmapRenderer.pixmap);
        maker = new ModelMaker(12345);
        try {
            box = VoxIO.readVox(new LittleEndianDataInputStream(new FileInputStream("Aurora/dumbcube.vox")));
            warrior = new TurnModel().set(new ArrayModel(maker.shipLargeRandomAurora()));
        } catch (Exception e) {
            e.printStackTrace();
            box = maker.shipLargeRandomAurora();
            warrior = new TurnModel().set(new ArrayModel(maker.shipLargeRandomAurora()));
        }
        dumbCube = new TurnModel().set(new ArrayModel(box));
        model = dumbCube;
        Gdx.input.setInputProcessor(inputProcessor());
    }

    @Override
    public void render() {
        buffer.begin();
        
        Gdx.gl.glClearColor(0.4f, 0.75f, 0.3f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        worldView.apply();
        worldView.getCamera().position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);
        worldView.update(VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        batch.setProjectionMatrix(worldView.getCamera().combined);
        batch.begin();
        if(diagonal) {
            pmTexture.draw(WarpDraw.draw45(model, pixmapRenderer), 0, 0);
            batch.draw(pmTexture, 64, 64);
//            WarpDraw.simpleDraw45(model, batchRenderer, voxelColor, outline);
        }
        else if(angle != 2)
        {
            pmTexture.draw(WarpDraw.drawAbove(model, pixmapRenderer), 0, 0);
            batch.draw(pmTexture, 64, 64);
        }
        else {
            pmTexture.draw(WarpDraw.draw(model, pixmapRenderer), 0, 0);
            batch.draw(pmTexture, 64, 64);
            //WarpDraw.simpleDraw(model, batchRenderer, voxelColor, outline);
        }
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
        //font.draw(batch, model.voxels.length + ", " + model.voxels[0].length + ", " + model.voxels[0][0].length + ", " + " (original)", 0, 80);
        font.draw(batch, model.sizeX() + ", " + model.sizeY() + ", " + model.sizeZ() + " (sizes)", 0, 60);
        font.draw(batch, StringKit.join(", ", model.turner().rotation()) + " (rotation)", 0, 40);
        font.draw(batch, Gdx.graphics.getFramesPerSecond() + " FPS", 0, 20);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        screenView.update(width, height);
    }

    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Warp Tester");
        config.setWindowedMode(SCREEN_WIDTH, SCREEN_HEIGHT);
        config.setIdleFPS(10);
        config.useVsync(false);
        final WarpTest app = new WarpTest();
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
                        model.turner().clockX();
                        break;
                    case Input.Keys.I:
                        model.turner().clockY();
                        break;
                    case Input.Keys.O:
                        if(!(diagonal = !diagonal)) 
                            model.turner().clockZ();
                        break;
                    case Input.Keys.J:
                        model.turner().counterX();
                        break;
                    case Input.Keys.K:
                        model.turner().counterY();
                        break;
                    case Input.Keys.L:
                        if(diagonal = !diagonal)
                            model.turner().counterZ();
                        break;
                    case Input.Keys.R:
                        model.turner().reset();
                        break;
                    case Input.Keys.P:
                        Tools3D.deepCopyInto(maker.shipLargeRandomAurora(), ((ArrayModel)model.getModel()).voxels);
                        model = warrior;
                        break;
                    case Input.Keys.B:
                        model = dumbCube;
                        break;
                    case Input.Keys.G:
                        batchRenderer.color().set(batchRenderer.color().direction().counter());
                        break;
                    case Input.Keys.H:
                        batchRenderer.color().set(batchRenderer.color().direction().clock());
                        break;
                    case  Input.Keys.E: // edges
                        outline = !outline;
                        break;
                    case Input.Keys.T: // try again
                        model.turner().reset();
                        diagonal = false;
                        angle = 2;
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
