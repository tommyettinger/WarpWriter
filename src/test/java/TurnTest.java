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
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import warpwriter.LittleEndianDataInputStream;
import warpwriter.ModelRenderer;
import warpwriter.VoxIO;
import warpwriter.WorldMaker;
import warpwriter.model.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class TurnTest extends ApplicationAdapter {
    public static final int width = 1280;
    public static final int height = 720;
    protected SpriteBatch batch;
    protected Viewport view;
    protected BitmapFont font;
    protected TurnModel turnModel;
    private ModelRenderer modelRenderer = new ModelRenderer(false, true);
    private Texture tex;
    private Pixmap pix;
    private CompassDirection direction = CompassDirection.NORTH;
    private int angle = 2;
    private final WorldMaker wm = new WorldMaker(123456789L, 0.8);
    private ArrayModel world;

    @Override
    public void create() {
        batch = new SpriteBatch();
        view = new FitViewport(width, height);
        font = new BitmapFont(Gdx.files.internal("PxPlus_IBM_VGA_8x16.fnt"));
        byte[][][] arr;
        try {
            arr = VoxIO.readVox(new LittleEndianDataInputStream(new FileInputStream("Infantry.vox")));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            arr = new byte[80][80][60];
        }
        turnModel = new TurnModel(world = new ArrayModel(wm.makeWorld(80, -1, -1)), new Turner());

        reDraw();
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                boolean needRedraw = true;
                switch (keycode) {
                    case Input.Keys.R:
                        world.voxels = (wm.makeWorld(80, -1, -1));
                        break;
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
                if (needRedraw) reDraw();
                return true;
            }
        });
    }

    public TurnTest reDraw() {
        if (pix != null) pix.dispose();
        pix = modelRenderer.renderToPixmap(turnModel, angle, direction);
        if (tex != null) tex.dispose();
        tex = new Texture(pix);
        return this;
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        view.getCamera().position.set(width >> 1, height >> 1, 0);
        view.update(width, height);
        batch.setProjectionMatrix(view.getCamera().combined);
        batch.begin();
        font.draw(batch, turnModel.turner().face().toString(), 200, 20);
        font.draw(batch, turnModel.turner().roll().toString(), 200, 40);
        batch.draw(tex, 0, 0);
        batch.end();
    }

    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Fetch Tester");
        config.setWindowedMode(width, height);
        config.setIdleFPS(10);
        final TurnTest app = new TurnTest();
        new Lwjgl3Application(app, config);
    }
}
