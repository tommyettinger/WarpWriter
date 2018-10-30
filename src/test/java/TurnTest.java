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
    private Rotator.Face face = Rotator.Face.NORTH;
    private Rotator.Roll roll = Rotator.Roll.UP;
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
        turnModel = new TurnModel(world = new ArrayModel(wm.makeWorld(80, -1, -1)), face, roll);

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
                        roll = Rotator.Roll.UP;
                        break;
                    case Input.Keys.DOWN:
                        roll = Rotator.Roll.DOWN;
                        break;
                    case Input.Keys.RIGHT:
                        roll = Rotator.Roll.RIGHT;
                        break;
                    case Input.Keys.LEFT:
                        roll = Rotator.Roll.LEFT;
                        break;
                    case Input.Keys.NUMPAD_8:
                    case Input.Keys.NUM_8:
                        direction = CompassDirection.NORTH;
                        face = Rotator.Face.NORTH;
                        break;
                    case Input.Keys.NUMPAD_6:
                    case Input.Keys.NUM_6:
                        direction = CompassDirection.NORTH;
                        face = Rotator.Face.EAST;
                        break;
                    case Input.Keys.NUMPAD_2:
                    case Input.Keys.NUM_2:
                        direction = CompassDirection.NORTH;
                        face = Rotator.Face.SOUTH;
                        break;
                    case Input.Keys.NUMPAD_4:
                    case Input.Keys.NUM_4:
                        direction = CompassDirection.NORTH;
                        face = Rotator.Face.WEST;
                        break;
                    case Input.Keys.SLASH:
                        direction = CompassDirection.NORTH;
                        face = Rotator.Face.TOP;
                        break;
                    case Input.Keys.STAR:
                        direction = CompassDirection.NORTH;
                        face = Rotator.Face.BOTTOM;
                        break;
                    case Input.Keys.NUMPAD_7:
                    case Input.Keys.NUM_7:
                        direction = CompassDirection.NORTH_WEST;
                        face = Rotator.Face.NORTH;
                        break;
                    case Input.Keys.NUMPAD_9:
                    case Input.Keys.NUM_9:
                        direction = CompassDirection.NORTH_WEST;
                        face = Rotator.Face.EAST;
                        break;
                    case Input.Keys.NUMPAD_3:
                    case Input.Keys.NUM_3:
                        direction = CompassDirection.NORTH_WEST;
                        face = Rotator.Face.SOUTH;
                        break;
                    case Input.Keys.NUMPAD_1:
                    case Input.Keys.NUM_1:
                        direction = CompassDirection.NORTH_WEST;
                        face = Rotator.Face.WEST;
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
                    case Input.Keys.Q:
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

    public void reDraw() {
        if (pix != null) pix.dispose();
        turnModel.set(face).set(roll);
        pix = modelRenderer.renderToPixmap(turnModel, angle, direction);
        if (tex != null) tex.dispose();
        tex = new Texture(pix);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        view.getCamera().position.set(width >> 1, height >> 1, 0);
        view.update(width, height);
        batch.setProjectionMatrix(view.getCamera().combined);
        batch.begin();
        font.draw(batch, face.toString(), 200, 20);
        font.draw(batch, roll.toString(), 200, 40);
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
