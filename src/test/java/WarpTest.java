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
import warpwriter.VoxIO;
import warpwriter.view.SpriteBatchVoxelRenderer;
import warpwriter.view.VoxelColor;
import warpwriter.view.WarpDraw;
import warpwriter.warp.VoxelModel;

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
    protected Texture screenTexture;
    protected TextureRegion screenRegion;
    protected VoxelModel model, dumbCube, warrior;
    protected ModelMaker maker;
    private SpriteBatchVoxelRenderer batchRenderer;
    protected VoxelColor voxelColor;
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

        voxelColor = new VoxelColor();
        batchRenderer = new SpriteBatchVoxelRenderer(batch).setOffset(16, 100);
        maker = new ModelMaker(12345);
        try {
            box = VoxIO.readVox(new LittleEndianDataInputStream(WarpTest.class.getResourceAsStream("/dumbcube.vox")));
        } catch (Exception e) {
            e.printStackTrace();
            box = maker.warriorRandom();
        }
        dumbCube = new VoxelModel(box);
        warrior = new VoxelModel(maker.warriorRandom());
        model = dumbCube;
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

        font.draw(batch, StringKit.join(", ", model.sizes()) + " (original)", 0, 80);
        font.draw(batch, model.sizeX() + ", " + model.sizeY() + ", " + model.sizeZ() + " (modified)", 0, 60);
        font.draw(batch, StringKit.join(", ", model.rotation()) + " (rotation)", 0, 40);
        font.draw(batch, model.startX() + ", " + model.startY() + ", " + model.startZ() + " (starts)", 0, 20);
        WarpDraw.draw(model, batchRenderer.setScale(4f), voxelColor);

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
        final WarpTest app = new WarpTest();
        new Lwjgl3Application(app, config);
    }

    public InputProcessor inputProcessor() {
        return new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                switch (keycode) {
//                    case Input.Keys.UP:
//                        turnModel.turner().set(Turner.Roll.TWELVE);
//                        break;
//                    case Input.Keys.DOWN:
//                        turnModel.turner().set(Turner.Roll.SIX);
//                        break;
//                    case Input.Keys.RIGHT:
//                        turnModel.turner().set(Turner.Roll.THREE);
//                        break;
//                    case Input.Keys.LEFT:
//                        turnModel.turner().set(Turner.Roll.NINE);
//                        break;
//                    case Input.Keys.NUMPAD_8:
//                    case Input.Keys.NUM_8:
//                        direction = CompassDirection.NORTH;
//                        turnModel.turner().set(Turner.Face.X_PLUS);
//                        break;
//                    case Input.Keys.NUMPAD_6:
//                    case Input.Keys.NUM_6:
//                        direction = CompassDirection.EAST;
//                        turnModel.turner().set(Turner.Face.Y_PLUS);
//                        break;
//                    case Input.Keys.NUMPAD_2:
//                    case Input.Keys.NUM_2:
//                        direction = CompassDirection.SOUTH;
//                        turnModel.turner().set(Turner.Face.X_MINUS);
//                        break;
//                    case Input.Keys.NUMPAD_4:
//                    case Input.Keys.NUM_4:
//                        direction = CompassDirection.WEST;
//                        turnModel.turner().set(Turner.Face.Y_MINUS);
//                        break;
//                    case Input.Keys.SLASH:
//                        direction = CompassDirection.NORTH;
//                        turnModel.turner().set(Turner.Face.Z_PLUS);
//                        break;
//                    case Input.Keys.STAR:
//                        direction = CompassDirection.NORTH;
//                        turnModel.turner().set(Turner.Face.Z_MINUS);
//                        break;
//                    case Input.Keys.NUMPAD_7:
//                    case Input.Keys.NUM_7:
//                        direction = CompassDirection.NORTH_WEST;
//                        turnModel.turner().set(Turner.Face.X_PLUS);
//                        break;
//                    case Input.Keys.NUMPAD_9:
//                    case Input.Keys.NUM_9:
//                        direction = CompassDirection.NORTH_EAST;
//                        turnModel.turner().set(Turner.Face.Y_PLUS);
//                        break;
//                    case Input.Keys.NUMPAD_3:
//                    case Input.Keys.NUM_3:
//                        direction = CompassDirection.SOUTH_EAST;
//                        turnModel.turner().set(Turner.Face.X_MINUS);
//                        break;
//                    case Input.Keys.NUMPAD_1:
//                    case Input.Keys.NUM_1:
//                        direction = CompassDirection.SOUTH_WEST;
//                        turnModel.turner().set(Turner.Face.Y_MINUS);
//                        break;
//                    case Input.Keys.NUM_0:
//                        angle = 1;
//                        break;
//                    case Input.Keys.MINUS:
//                        angle = 2;
//                        break;
//                    case Input.Keys.EQUALS:
//                        angle = 3;
//                        break;
//                    case Input.Keys.U:
//                        turnModel.turner().clockX();
//                        break;
//                    case Input.Keys.I:
//                        turnModel.turner().clockY();
//                        break;
//                    case Input.Keys.O:
//                        turnModel.turner().clockZ();
//                        break;
                    case Input.Keys.J:
                    {
                        final int y = model.rotation()[2], z = ~model.rotation()[1];
                        model.rotation()[1] = y;
                        model.rotation()[2] = z;
                    }
                        break;
                    case Input.Keys.K:
                    {
                        final int x = ~model.rotation()[2], z = model.rotation()[0];
                        model.rotation()[0] = x;
                        model.rotation()[2] = z;
                    }
                        break;
                    case Input.Keys.L:
                    {
                        final int x = model.rotation()[1], y = ~model.rotation()[0];
                        model.rotation()[0] = x;
                        model.rotation()[1] = y;
                    }
                        break;
                    case Input.Keys.B:
                        model = dumbCube;
                        break;
                    case Input.Keys.W:
                        model = warrior;
                        break;
                    case Input.Keys.A:
                        voxelColor.set(voxelColor.direction().counter());
                        break;
                    case Input.Keys.S:
                        voxelColor.set(voxelColor.direction().clock());
                        break;
                    case Input.Keys.D:
                        voxelColor.set(!voxelColor.darkSide());
                        break;
                    case Input.Keys.R: // reset
                        model.rotation()[0] = -1;
                        model.rotation()[1] = 1;
                        model.rotation()[2] = 2;
                        break;
                    case Input.Keys.ESCAPE:
                        Gdx.app.exit();
                        break;
                    default:
                        break;
                }
                return true;
            }
        };
    }
}