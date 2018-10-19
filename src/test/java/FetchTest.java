import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
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
import squidpony.squidgrid.mapping.PacMazeGenerator;
import warpwriter.ModelMaker;
import warpwriter.ModelRenderer;
import warpwriter.model.*;

public class FetchTest extends ApplicationAdapter {
    public static final int width = 1280;
    public static final int height = 720;
    protected SpriteBatch batch;
    protected Viewport view;
    protected BitmapFont font;
    protected long seed = 0;
    protected FetchModel viewArea;
    protected OffsetModel offset;
    private ModelMaker modelMaker = new ModelMaker(seed);
    private ModelRenderer modelRenderer = new ModelRenderer(false, true);
    private Texture tex;
    private Pixmap pix;
    //private int[] palette = Coloring.RINSED; // do we need this?
    private CompassDirection direction = CompassDirection.NORTH;
    private int angle=1;

    @Override
    public void create() {
        batch = new SpriteBatch();
        view = new FitViewport(width, height);
        font = new BitmapFont(Gdx.files.internal("PxPlus_IBM_VGA_8x16.fnt"));
        viewArea = new FetchModel(200, 200, 20);
        offset = new OffsetModel();
        PacMazeGenerator maze = new PacMazeGenerator();
        boolean[][] dungeon = maze.create();
        viewArea.add(offset)
                .add(new DungeonFetch(dungeon, 5,
                        ColorFetch.color(modelMaker.randomMainColor()
                )))
                .add(new BoxModel(viewArea.xSize(), viewArea.ySize(), viewArea.zSize(),
                        ColorFetch.color(modelMaker.randomMainColor()
                        )));

        reDraw();
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                boolean needRedraw = true;
                switch (keycode) {
                    case Input.Keys.SPACE:
                        offset.addZ(1); // Up
                        break;
                    case Input.Keys.NUMPAD_5:
                    case Input.Keys.NUM_5:
                        offset.addZ(-1); // Down
                        break;
                    case Input.Keys.UP:
                        offset.add(direction.opposite());
                        break;
                    case Input.Keys.DOWN:
                        offset.add(direction);
                        break;
                    case Input.Keys.RIGHT:
                        offset.add(direction.right());
                        break;
                    case Input.Keys.LEFT:
                        offset.add(direction.left());
                        break;
                    case Input.Keys.NUMPAD_8:
                    case Input.Keys.NUM_8:
                        direction = CompassDirection.SOUTH;
                        break;
                    case Input.Keys.NUMPAD_6:
                    case Input.Keys.NUM_6:
                        direction = CompassDirection.WEST;
                        break;
                    case Input.Keys.NUMPAD_2:
                    case Input.Keys.NUM_2:
                        direction = CompassDirection.NORTH;
                        break;
                    case Input.Keys.NUMPAD_4:
                    case Input.Keys.NUM_4:
                        direction = CompassDirection.EAST;
                        break;
                    case Input.Keys.NUMPAD_7:
                    case Input.Keys.NUM_7:
                        direction = CompassDirection.SOUTH_EAST;
                        break;
                    case Input.Keys.NUMPAD_9:
                    case Input.Keys.NUM_9:
                        direction = CompassDirection.SOUTH_WEST;
                        break;
                    case Input.Keys.NUMPAD_3:
                    case Input.Keys.NUM_3:
                        direction = CompassDirection.NORTH_WEST;
                        break;
                    case Input.Keys.NUMPAD_1:
                    case Input.Keys.NUM_1:
                        direction = CompassDirection.NORTH_EAST;
                        break;
                    case Input.Keys.ENTER:
                        offset.set(0, 0, 0);
                        break;
                    case Input.Keys.NUM_0:
                        angle=1;
                        break;
                    case Input.Keys.MINUS:
                        angle=2;
                        break;
                    case Input.Keys.EQUALS:
                        angle=3;
                        break;
                    default:
                        needRedraw=false;
                        break;
                }
                if (needRedraw) reDraw();
                return true;
            }
        });
    }

    public void reDraw() {
        if (pix != null) pix.dispose();
        pix = modelRenderer.renderToPixmap(viewArea, angle, direction);
        if (tex != null) tex.dispose();
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
