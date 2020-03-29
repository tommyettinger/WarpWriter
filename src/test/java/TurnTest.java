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
import warpwriter.WorldMaker;
import warpwriter.model.VoxelSeq;
import warpwriter.model.color.Colorizer;
import warpwriter.view.WarpDraw;
import warpwriter.view.color.VoxelColor;
import warpwriter.view.render.VoxelPixmapRenderer;

public class TurnTest extends ApplicationAdapter {
    public static final int width = 1280;
    public static final int height = 720;
    protected SpriteBatch batch;
    protected Viewport view;
    protected BitmapFont font;
    private VoxelPixmapRenderer modelRenderer;
    private Texture tex;
    private int angle = 2;
    private final WorldMaker wm = new WorldMaker(123456789L, 0.8);
    private VoxelSeq world;

    @Override
    public void create() {
        modelRenderer = new VoxelPixmapRenderer().set(new Pixmap(width, height, Pixmap.Format.RGBA8888)).set(new VoxelColor().set(Colorizer.RinsedColorizer));
        batch = new SpriteBatch();
        view = new FitViewport(width, height);
        font = new BitmapFont(Gdx.files.internal("PxPlus_IBM_VGA_8x16.fnt"));
        byte[][][] arr = wm.makeWorld(120, -1, -1);
//        try {
//            arr = VoxIO.readVox(new LittleEndianDataInputStream(new FileInputStream("SpaceMarine.vox")));
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//            arr = new byte[80][80][60];
//        }
        world = new VoxelSeq(10000);
        world.putSurface(arr);

        reDraw();
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                boolean needRedraw = true;
                switch (keycode) {
                    case Input.Keys.R:
                        world.clear();
                        world.putSurface(wm.makeWorld(120, -1, -1));
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
                        world.clockX();
                        break;
                    case Input.Keys.I:
                        world.clockY();
                        break;
                    case Input.Keys.O:
                        world.clockZ();
                        break;
                    case Input.Keys.J:
                        world.counterX();
                        break;
                    case Input.Keys.K:
                        world.counterY();
                        break;
                    case Input.Keys.L:
                        world.counterZ();
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
        Pixmap pix;
        switch (angle){
            case 1:
                pix = WarpDraw.draw45(world, modelRenderer);
                break;
            default:
                pix = WarpDraw.drawIso(world, modelRenderer);
                break;
        }
//        pix = modelRenderer.renderToPixmap(turnModel, angle, direction);
        if (tex != null) tex.draw(pix, 0, 0);
        else tex = new Texture(pix);
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
//        font.draw(batch, turnModel.rotation().face().toString(), 200, 20);
//        font.draw(batch, turnModel.rotation().roll().toString(), 200, 40);
        batch.draw(tex, 0, 0);
        batch.end();
    }

    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Fetch Tester");
        config.setWindowedMode(width, height);
        config.setIdleFPS(10);
        config.setResizable(false);
        final TurnTest app = new TurnTest();
        new Lwjgl3Application(app, config);
    }
}
