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
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import warpwriter.ModelMaker;
import warpwriter.Tools3D;
import warpwriter.model.FetchModel;
import warpwriter.model.TurnModel;
import warpwriter.model.color.Colorizer;
import warpwriter.model.fetch.AnimatedArrayModel;
import warpwriter.model.fetch.ArrayModel;
import warpwriter.model.fetch.BurstFetch;
import warpwriter.model.fetch.OffsetModel;
import warpwriter.model.nonvoxel.CompassDirection;
import warpwriter.view.WarpDraw;
import warpwriter.view.color.VoxelColor;
import warpwriter.view.render.VoxelPixmapRenderer;

//import warpwriter.ModelRenderer;

public class FetchTest extends ApplicationAdapter {
//    public static final int width = 1280;
//    public static final int height = 720;
    public static final int SCREEN_WIDTH = 640;//640;
    public static final int SCREEN_HEIGHT = 720;//720;
    public static final int VIRTUAL_WIDTH = 640;
    public static final int VIRTUAL_HEIGHT = 720;
    protected Viewport worldView;
    protected Viewport screenView;
    protected FrameBuffer buffer;
    protected Texture screenTexture;
    protected TextureRegion screenRegion;
    protected VoxelPixmapRenderer pixmapRenderer;
    protected VoxelColor voxelColor;

    protected SpriteBatch batch;
    protected Viewport view;
    protected BitmapFont font;
    protected long seed = 1;
    protected TurnModel viewArea;
    protected OffsetModel offset;
    protected byte[][][] container;
    protected BurstFetch burst;
    protected AnimatedArrayModel fire;
    protected FetchModel fm;
    private ModelMaker modelMaker = new ModelMaker(seed, Colorizer.FlesurrectBonusColorizer);
//    private ModelRenderer modelRenderer = new ModelRenderer(false, true);
    private Texture[] tex;
    private Pixmap pix;
    private CompassDirection direction = CompassDirection.NORTH;
    private int angle = 3;

    @Override
    public void create() {
        batch = new SpriteBatch();
        worldView = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        screenView = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        buffer = new FrameBuffer(Pixmap.Format.RGBA8888, VIRTUAL_WIDTH, VIRTUAL_HEIGHT, false, false);
        screenRegion = new TextureRegion();
        screenView.getCamera().position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);
        screenView.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        voxelColor = new VoxelColor().set(Colorizer.FlesurrectBonusColorizer);
        tex = new Texture[16];
        for (int f = 0; f < tex.length; f++) {
            tex[f] = new Texture(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, Pixmap.Format.RGBA8888);
        }
        pixmapRenderer = new VoxelPixmapRenderer(new Pixmap(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, Pixmap.Format.RGBA8888), voxelColor);

//        view = new FitViewport(width, height);
        font = new BitmapFont(Gdx.files.internal("PxPlus_IBM_VGA_8x16.fnt"));
//        viewArea = new FetchModel(230, 100, 32);
        fm = new FetchModel(100, 100, 60);
        viewArea = new TurnModel().set(fm);
        offset = new OffsetModel();
        container = new byte[100][100][50];
        Tools3D.translateCopyInto(modelMaker.shipLargeNoiseColorized(), container, 30, 30, 10);
        fire = new AnimatedArrayModel(modelMaker.animateExplosion(17, 70, 70, 60));
        burst = new BurstFetch(new ArrayModel(container), 50, 50, 4, 16, 3);
//        PacMazeGenerator maze = new PacMazeGenerator(1000, 1000, modelMaker.rng);
//        boolean[][] dungeon = maze.create();
        fm.add(offset).add(burst).add(new OffsetModel(-15, -15, -14).add(fire));
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
        redraw();
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
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
                        viewArea.turner().reset();
                        break;
                    case Input.Keys.NUMPAD_6:
                    case Input.Keys.NUM_6:
                        direction = CompassDirection.EAST;
                        viewArea.turner().reset().counterZ();
                        break;
                    case Input.Keys.NUMPAD_2:
                    case Input.Keys.NUM_2:
                        direction = CompassDirection.SOUTH;
                        viewArea.turner().reset().clockZ().clockZ();
                        break;
                    case Input.Keys.NUMPAD_4:
                    case Input.Keys.NUM_4:
                        direction = CompassDirection.WEST;
                        viewArea.turner().reset().clockZ();
                        break;
                    case Input.Keys.NUMPAD_7:
                    case Input.Keys.NUM_7:
                        direction = CompassDirection.NORTH_WEST;
                        viewArea.turner().reset();
                        break;
                    case Input.Keys.NUMPAD_9:
                    case Input.Keys.NUM_9:
                        direction = CompassDirection.NORTH_EAST;
                        viewArea.turner().reset().counterZ();
                        break;
                    case Input.Keys.NUMPAD_3:
                    case Input.Keys.NUM_3:
                        direction = CompassDirection.SOUTH_EAST;
                        viewArea.turner().reset().clockZ().clockZ();
                        break;
                    case Input.Keys.NUMPAD_1:
                    case Input.Keys.NUM_1:
                        direction = CompassDirection.SOUTH_WEST;
                        viewArea.turner().reset().clockZ();
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
                    case Input.Keys.P:
                        Tools3D.fill(container, 0);
                        Tools3D.translateCopyInto(modelMaker.shipLargeNoiseColorized(), container, 30, 30, 10);
                        break;
                    default:
                        return true;
                }
                redraw();
                return true;
            }
        });
    }

    public void redraw() {
        for (int f = 0; f < 16; f++) {
            long start = TimeUtils.nanoTime();
            burst.setFrame(f);
            fire.setFrame(burst.frame() + 1);
//            buffer.begin();
//            Gdx.gl.glClearColor(0.55f, 0.3f, 0.14f, 1f);
//            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

//            worldView.apply();
//            worldView.getCamera().position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);
//            worldView.update(VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
//            batch.setProjectionMatrix(worldView.getCamera().combined);
//            batch.begin();
            if (direction.isDiagonal()) {
                if (angle != 2) {
                    tex[f].draw(WarpDraw.drawIso(viewArea, pixmapRenderer), 0, 0);
                } else {
                    tex[f].draw(WarpDraw.draw45(viewArea, pixmapRenderer), 0, 0);
                }
//            WarpDraw.simpleDraw45(model, batchRenderer, voxelColor, outline);
            } else if (angle != 2) {
                tex[f].draw(WarpDraw.drawAbove(viewArea, pixmapRenderer), 0, 0);
            } else {
                tex[f].draw(WarpDraw.draw(viewArea, pixmapRenderer), 0, 0);
                //WarpDraw.simpleDraw(model, batchRenderer, voxelColor, outline);
            }
            System.out.println(TimeUtils.timeSinceNanos(start) + " nanoseconds to render frame " + f);
//            batch.draw(tex[f], 0, 0);
//            //batch.setColor(-0x1.fffffep126f); // white as a packed float, resets any color changes that the renderer made
//            batch.end();
//            buffer.end();
        }
    }
    @Override
    public void render() {
        Gdx.gl.glClearColor(0.55f, 0.3f, 0.14f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        screenView.apply();
        batch.setProjectionMatrix(screenView.getCamera().combined);
        batch.begin();
        screenTexture = tex[(int) (System.currentTimeMillis() / 90) & 15];
        screenTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        screenRegion.setRegion(screenTexture);
        //screenRegion.flip(false, true);
        batch.draw(screenRegion, 0, 0);
        batch.end();

//        Gdx.gl.glClearColor(0, 0, 0, 0);
//        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
//        view.getCamera().position.set(width / 2, height / 2, 0);
//        view.update(width, height);
//        batch.setProjectionMatrix(view.getCamera().combined);
//        batch.begin();
//        batch.draw(tex, 0, 0);
//        batch.end();
    }

    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Fetch Tester");
        config.setWindowedMode(SCREEN_WIDTH, SCREEN_HEIGHT);
        config.setIdleFPS(10);
        config.useVsync(true);
        final FetchTest app = new FetchTest();
        new Lwjgl3Application(app, config);
    }
}
