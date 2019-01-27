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
import warpwriter.ModelMaker;
import warpwriter.ModelRenderer;
import warpwriter.Tools3D;
import warpwriter.model.FetchModel;
import warpwriter.model.fetch.ArrayModel;
import warpwriter.model.fetch.BurstFetch;
import warpwriter.model.fetch.OffsetModel;
import warpwriter.model.nonvoxel.CompassDirection;

public class FetchTest extends ApplicationAdapter {
    public static final int width = 1280;
    public static final int height = 720;
    protected SpriteBatch batch;
    protected Viewport view;
    protected BitmapFont font;
    protected long seed = 1;
    protected FetchModel viewArea;
    protected OffsetModel offset;
    protected BurstFetch burst;
    private ModelMaker modelMaker = new ModelMaker(seed);
    private ModelRenderer modelRenderer = new ModelRenderer(false, true);
    private Texture tex;
    private Pixmap pix;
    private CompassDirection direction = CompassDirection.NORTH;
    private int angle = 3;

    @Override
    public void create() {
        batch = new SpriteBatch();
        view = new FitViewport(width, height);
        font = new BitmapFont(Gdx.files.internal("PxPlus_IBM_VGA_8x16.fnt"));
//        viewArea = new FetchModel(230, 100, 32);
        viewArea = new FetchModel(120, 120, 80);
        offset = new OffsetModel();
        byte[][][] bigger = new byte[120][120][80];
        Tools3D.translateCopyInto(modelMaker.shipLargeRandom(), bigger, 40, 40, 0);
        burst = new BurstFetch(new ArrayModel(bigger), 60, 60, 4, 16, 3);
//        PacMazeGenerator maze = new PacMazeGenerator(1000, 1000, modelMaker.rng);
//        boolean[][] dungeon = maze.create();
        viewArea.add(offset).add(burst);
//        viewArea.add(offset)
//                .add(new BoxModel(viewArea.sizeX(), viewArea.sizeY(), viewArea.sizeZ(),
//                        ColorFetch.color(modelMaker.randomMainColor()
//                        )))
//                // new NoiseHeightMap(new Noise.Layered2D(FastNoise.instance, 2, 0.125), 0)
//                .decideFetch(new HeightDecide(new TerracedHeightMap(modelMaker.rng.nextInt(), 16), 16), new NoiseFetch(new Noise.Layered3D(FastNoise.instance, 2, 0.25), modelMaker.randomMainColor()))
//                .swapper(Swapper.Swap.zxy)
//                .offsetModel(0, 5, 5)
////                .add(new DecideFetch(
////                        new DungeonDecide(dungeon, 5),
////                        new NoiseFetch(modelMaker.randomMainColor())
////                ))
//        ;
        reDraw();
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                boolean needRedraw = true;
                switch (keycode) {
                    case Input.Keys.SPACE:
                        offset.addZ(angle > 1 ? 1 : -1); // Up
                        break;
                    case Input.Keys.NUMPAD_5:
                    case Input.Keys.NUM_5:
                        offset.addZ(angle > 1 ? -1 : 1); // Down
                        break;
                    case Input.Keys.UP:
                        offset.add(angle > 1 ? direction.opposite() : direction);
                        break;
                    case Input.Keys.DOWN:
                        offset.add(angle > 1 ? direction : direction.opposite());
                        break;
                    case Input.Keys.RIGHT:
                        offset.add(angle > 1 ? direction.left() : direction.right());
                        break;
                    case Input.Keys.LEFT:
                        offset.add(angle > 1 ? direction.right() : direction.left());
                        break;
                    case Input.Keys.NUMPAD_8:
                    case Input.Keys.NUM_8:
                        direction = CompassDirection.NORTH;
                        break;
                    case Input.Keys.NUMPAD_6:
                    case Input.Keys.NUM_6:
                        direction = CompassDirection.EAST;
                        break;
                    case Input.Keys.NUMPAD_2:
                    case Input.Keys.NUM_2:
                        direction = CompassDirection.NORTH;
                        break;
                    case Input.Keys.NUMPAD_4:
                    case Input.Keys.NUM_4:
                        direction = CompassDirection.SOUTH;
                        break;
                    case Input.Keys.NUMPAD_7:
                    case Input.Keys.NUM_7:
                        direction = CompassDirection.NORTH_WEST;
                        break;
                    case Input.Keys.NUMPAD_9:
                    case Input.Keys.NUM_9:
                        direction = CompassDirection.NORTH_EAST;
                        break;
                    case Input.Keys.NUMPAD_3:
                    case Input.Keys.NUM_3:
                        direction = CompassDirection.SOUTH_EAST;
                        break;
                    case Input.Keys.NUMPAD_1:
                    case Input.Keys.NUM_1:
                        direction = CompassDirection.SOUTH_WEST;
                        break;
                    case Input.Keys.ENTER:
                        offset.set(0, 0, 0);
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
        // this SHOULD be changing the burst effect, but no change happens
        burst.setFrame((int) (System.currentTimeMillis() >> 8) & 15);
        if (pix != null) pix.dispose();
        pix = modelRenderer.renderToPixmap(viewArea, angle, direction);
        if (tex != null) tex.draw(pix, 0, 0);
        else tex = new Texture(pix);
    }

    @Override
    public void render() {
        reDraw();
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        view.getCamera().position.set(width / 2, height / 2, 0);
        view.update(width, height);
        batch.setProjectionMatrix(view.getCamera().combined);
        batch.begin();
        batch.draw(tex, 0, 0);
        batch.end();
    }

    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Fetch Tester");
        config.setWindowedMode(width, height);
        config.setIdleFPS(10);
        config.useVsync(true);
        final FetchTest app = new FetchTest();
        new Lwjgl3Application(app, config);
    }
}
