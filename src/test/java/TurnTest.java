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
import warpwriter.*;
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
            arr = VoxIO.readVox(new LittleEndianDataInputStream(new FileInputStream("SpaceMarine.vox")));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            arr = new byte[80][80][60];
        }
        world = new ArrayModel(arr);

        turnModel = new TurnModel().set(
                new DecideFetch(
//                        new PlaneDecide(1, 1, 1, 100).set(PlaneDecide.Condition.ON),
                        new BalloonDecide(0, 30, 30, 30, 0, 0), // gives it a weird angle for testing
                        ColorFetch.color(Coloring.rinsed("Coastal Water 2"))
                )
                        .boxModel(world,
                                ColorFetch.color(Coloring.rinsed("Red 4"))
                        )
                        .model(world.sizeX(), world.sizeY(), world.sizeZ())
        );

        reDraw();
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                boolean needRedraw = true;
                switch (keycode) {
                    case Input.Keys.R:
                        world.voxels = (wm.makeWorld(60, -1, -1));
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
        Gdx.gl.glClearColor(0.3f, 0.5f, 0.8f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        view.getCamera().position.set(width >> 1, height >> 1, 0);
        view.update(width, height);
        batch.setProjectionMatrix(view.getCamera().combined);
        batch.begin();
//        font.draw(batch, turnModel.turner().face().toString(), 200, 20);
//        font.draw(batch, turnModel.turner().roll().toString(), 200, 40);
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
