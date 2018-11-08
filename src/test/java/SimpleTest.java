import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
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
import warpwriter.ModelMaker;
import warpwriter.model.ArrayModel;
import warpwriter.model.CompassDirection;
import warpwriter.model.TurnModel;
import warpwriter.model.Turner;
import warpwriter.view.SimpleDraw;
import warpwriter.view.SpriteBatchRenderer;

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
    protected TurnModel turnModel;
    private SpriteBatchRenderer batchRenderer;
    protected CompassDirection direction = CompassDirection.NORTH;
    protected int angle=2;

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

        batchRenderer = new SpriteBatchRenderer(batch).setScale(16f);
        turnModel = new TurnModel(new ArrayModel(new ModelMaker().warriorRandom()));

        //reDraw();
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                boolean needRedraw = true;
                switch (keycode) {
                    case Input.Keys.UP:
                        turnModel.turner().set(Turner.Roll.NONE);
                        break;
                    case Input.Keys.DOWN:
                        turnModel.turner().set(Turner.Roll.UTURN);
                        break;
                    case Input.Keys.RIGHT:
                        turnModel.turner().set(Turner.Roll.RIGHT);
                        break;
                    case Input.Keys.LEFT:
                        turnModel.turner().set(Turner.Roll.LEFT);
                        break;
                    case Input.Keys.NUMPAD_8:
                    case Input.Keys.NUM_8:
                        direction = CompassDirection.NORTH;
                        turnModel.turner().set(Turner.Face.NORTH);
                        break;
                    case Input.Keys.NUMPAD_6:
                    case Input.Keys.NUM_6:
                        direction = CompassDirection.NORTH;
                        turnModel.turner().set(Turner.Face.EAST);
                        break;
                    case Input.Keys.NUMPAD_2:
                    case Input.Keys.NUM_2:
                        direction = CompassDirection.NORTH;
                        turnModel.turner().set(Turner.Face.SOUTH);
                        break;
                    case Input.Keys.NUMPAD_4:
                    case Input.Keys.NUM_4:
                        direction = CompassDirection.NORTH;
                        turnModel.turner().set(Turner.Face.WEST);
                        break;
                    case Input.Keys.SLASH:
                        direction = CompassDirection.NORTH;
                        turnModel.turner().set(Turner.Face.UP);
                        break;
                    case Input.Keys.STAR:
                        direction = CompassDirection.NORTH;
                        turnModel.turner().set(Turner.Face.DOWN);
                        break;
                    case Input.Keys.NUMPAD_7:
                    case Input.Keys.NUM_7:
                        direction = CompassDirection.NORTH_EAST;
                        turnModel.turner().set(Turner.Face.WEST);
                        break;
                    case Input.Keys.NUMPAD_9:
                    case Input.Keys.NUM_9:
                        direction = CompassDirection.NORTH_EAST;
                        turnModel.turner().set(Turner.Face.NORTH);
                        break;
                    case Input.Keys.NUMPAD_3:
                    case Input.Keys.NUM_3:
                        direction = CompassDirection.NORTH_EAST;
                        turnModel.turner().set(Turner.Face.EAST);
                        break;
                    case Input.Keys.NUMPAD_1:
                    case Input.Keys.NUM_1:
                        direction = CompassDirection.NORTH_EAST;
                        turnModel.turner().set(Turner.Face.SOUTH);
                        break;
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
                        turnModel.turner().clockX();
                        break;
                    case Input.Keys.I:
                        turnModel.turner().clockY();
                        break;
                    case Input.Keys.O:
                        turnModel.turner().clockZ();
                        break;
                    case Input.Keys.J:
                        turnModel.turner().counterX();
                        break;
                    case Input.Keys.K:
                        turnModel.turner().counterY();
                        break;
                    case Input.Keys.L:
                        turnModel.turner().counterZ();
                        break;
                    case Input.Keys.ESCAPE:
                        Gdx.app.exit();
                        break;
                    default:
                        needRedraw = false;
                        break;
                }
                //if (needRedraw) reDraw();
                return true;
            }
        });
    }

    @Override
    public void render() {
        buffer.begin();
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        worldView.apply();
        worldView.getCamera().position.set(0, 0, 0);
        worldView.update(VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        batch.setProjectionMatrix(worldView.getCamera().combined);
        batch.begin();

        font.draw(batch, turnModel.turner().face().toString(), 200, 20);
        font.draw(batch, turnModel.turner().roll().toString(), 200, 40);
        //batch.draw(tex, 0, 0);
        SimpleDraw.simpleDraw(turnModel, batchRenderer);

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
        final SimpleTest app = new SimpleTest();
        new Lwjgl3Application(app, config);
    }
}
