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
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import squidpony.FakeLanguageGen;
import squidpony.StringKit;
import warpwriter.ModelMaker;
import warpwriter.Tools3D;
import warpwriter.VoxIO;
import warpwriter.model.FetchModel;
import warpwriter.model.IFetch;
import warpwriter.model.IModel;
import warpwriter.model.TurnModel;
import warpwriter.model.color.Colorizer;
import warpwriter.model.decide.DecideFetch;
import warpwriter.model.decide.SphereDecide;
import warpwriter.model.fetch.*;
import warpwriter.model.nonvoxel.HashMap3D;
import warpwriter.view.WarpDraw;
import warpwriter.view.color.VoxelColor;
import warpwriter.view.render.VoxelPixmapRenderer;
import warpwriter.view.render.VoxelSpriteBatchRenderer;

public class WarpTest extends ApplicationAdapter {
    public static final int SCREEN_WIDTH = 320;//640;
    public static final int SCREEN_HEIGHT = 360;//720;
    public static final int VIRTUAL_WIDTH = 320;
    public static final int VIRTUAL_HEIGHT = 360;
    protected SpriteBatch batch;
    protected Viewport worldView;
    protected Viewport screenView;
    protected BitmapFont font;
    protected FrameBuffer buffer;
    protected Texture screenTexture, pmTexture;
    protected TextureRegion screenRegion;
    protected TurnModel model, ship;
    protected ModelMaker maker;
    private VoxelSpriteBatchRenderer batchRenderer;
    private VoxelPixmapRenderer pixmapRenderer;
    protected VoxelColor voxelColor;
    protected int angle = 3;
    protected int frame = 0;
    protected boolean diagonal = true;
    protected boolean animating = false;
    protected byte[][][][] explosion;
    protected AnimatedArrayModel boom;
    private byte[][][] voxels, container;
    private Colorizer colorizer;
//    private ChaoticFetch chaos;

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
//        colorizer = Colorizer.arbitraryBonusColorizer(Coloring.AURORA);
//        colorizer = Colorizer.arbitraryColorizer(Coloring.GB_GREEN);
//        colorizer = Colorizer.arbitraryColorizer(Coloring.DB32);
//        colorizer = Colorizer.arbitraryColorizer(Coloring.BLK36);
//        colorizer = Colorizer.arbitraryBonusColorizer(Coloring.UNSEVEN);
//        colorizer = Colorizer.arbitraryBonusColorizer(Coloring.CW_PALETTE);
//        colorizer = Colorizer.arbitraryBonusColorizer(Coloring.VGA256);
//        colorizer = Colorizer.arbitraryBonusColorizer(Coloring.FLESURRECT);
        colorizer = Colorizer.FlesurrectBonusColorizer;
        voxelColor = new VoxelColor().set(colorizer);
        batchRenderer = new VoxelSpriteBatchRenderer(batch).setOffset(16, 100);
        pixmapRenderer = new VoxelPixmapRenderer(new Pixmap(512, 512, Pixmap.Format.RGBA8888), voxelColor);
        pmTexture = new Texture(pixmapRenderer.pixmap);
        maker = new ModelMaker(12345, colorizer);
//        try {
//            box = VoxIO.readVox(new LittleEndianDataInputStream(new FileInputStream("Aurora/dumbcube.vox")));
//        } catch (Exception e) {
//            e.printStackTrace();
//            box = maker.shipNoiseColorized();
//        }
        makeBoom(maker.fireRange());
        voxels = maker.shipLargeNoiseColorized();
//        chaos = new ChaoticFetch(maker.rng.nextLong(), (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 1);
        ship = new TurnModel().set(
//                new ReplaceFetch(ColorFetch.color((byte) 0), (byte) 1)
//                .add(new PaintFetch(chaos, true)).model(
                new ArrayModel(voxels));
//        model = new TurnModel().set(model());
        model = new TurnModel().set(ship);
        model.setDuration(16);
        Gdx.input.setInputProcessor(inputProcessor());
    }
    
    public IModel model()
    {
        HashMap3D<IFetch> map = new HashMap3D<>();
        for (int z = 0; z < 1; z++) {
            for (int x = 0; x < 4; x++) {
                for (int y = 0; y < 4; y++) {
                    byte midColor = colorizer.getReducer().randomColorIndex(maker.rng);
                    if ((y & 1) == 1)
                        midColor |= colorizer.getWaveBit();
                    if ((y & 2) == 2)
                        midColor |= colorizer.getShadeBit();
                    map.put(x, y, z, new DecideFetch(
                            new SphereDecide(8, 8, 8, 7), ColorFetch.color(midColor)
                    ));
                }
            }
        }
        return new WorldFetch()
                .set(map)
                .model(64, 64, 16);

    }
    
    public void makeBoom(byte[] fireColors) {
        FetchModel fm = new FetchModel(48, 48, 32);
        container = new byte[48][48][32];
        long state = maker.rng.getState();
        Tools3D.translateCopyInto(maker.shipNoiseColorized(), container, 18, 18, 0);
        AnimatedArrayModel fire = new AnimatedArrayModel(maker.animateExplosion(18, 40, 40, 32, fireColors));
        BurstFetch burst = new BurstFetch(new ArrayModel(container), 24, 24, 3, 16, 2);
        fm.add(burst).add(new OffsetModel(-4, -4, -2).add(fire));
        byte[][][][] voxelFrames = new byte[16][][][];
        boom = new AnimatedArrayModel(voxelFrames);
        for (int i = 0; i < 16; i++) {
            burst.setFrame(i);
            fire.setFrame(i+2);
            voxelFrames[i] = new ArrayModel(fm).voxels;
        }
        maker.rng.setState(state);
    }

    @Override
    public void render() {
        model.setFrame((int)(TimeUtils.millis() >>> 7) & 15);
        boom.setFrame((int)(TimeUtils.millis() >>> 7) & 15);
        buffer.begin();
        
        Gdx.gl.glClearColor(0.4f, 0.75f, 0.3f, 1f);
        // for GB_GREEN palette
//        Gdx.gl.glClearColor(0xE0 / 255f, 0xF8 / 255f, 0xD0 / 255f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        worldView.apply();
        worldView.getCamera().position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);
        worldView.update(VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        batch.setProjectionMatrix(worldView.getCamera().combined);
        batch.begin();
        if(diagonal) {
            if(angle != 2){
                pmTexture.draw(WarpDraw.drawIso(model, pixmapRenderer), 0, 0);
            }
            else 
            {
                pmTexture.draw(WarpDraw.draw45(model, pixmapRenderer), 0, 0);
            }
//            WarpDraw.simpleDraw45(model, batchRenderer, voxelColor, outline);
        }
        else if(angle != 2)
        {
            pmTexture.draw(WarpDraw.drawAbove(model, pixmapRenderer), 0, 0);
        }
        else {
            pmTexture.draw(WarpDraw.draw(model, pixmapRenderer), 0, 0);
            //WarpDraw.simpleDraw(model, batchRenderer, voxelColor, outline);
        }
        batch.draw(pmTexture, 64, 64);
        //batch.setColor(-0x1.fffffep126f); // white as a packed float, resets any color changes that the renderer made
        batch.end();
        buffer.end();
        Gdx.gl.glClearColor(0, 0, 0, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        screenView.apply();
        batch.setProjectionMatrix(screenView.getCamera().combined);
        batch.begin();
        screenTexture = buffer.getColorBufferTexture();
        screenTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        screenRegion.setRegion(screenTexture);
        screenRegion.flip(false, true);
        batch.draw(screenRegion, 0, 0);
        //// for GB_GREEN
        //font.setColor(0x34 / 255f, 0x68 / 255f, 0x56 / 255f, 1f);
        font.setColor(0f, 0f, 0f, 1f);
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
//                        model.set(model());
                        model.set(ship);
//                        chaos.setSeed(maker.rng.nextLong());
                        Tools3D.deepCopyInto(maker.shipLargeNoiseColorized(), voxels);
                        animating = false;
                        break;
                    case Input.Keys.B: // burn!
                        makeBoom(maker.fireRange());
                        model.set(boom);
                        animating = true;
                        break;
                    case Input.Keys.Z: // zap!
                        makeBoom(maker.randomFireRange());
                        model.set(boom);
                        animating = true;
                        break;
                    case Input.Keys.G:
                        batchRenderer.color().set(batchRenderer.color().direction().counter());
                        break;
                    case Input.Keys.H:
                        batchRenderer.color().set(batchRenderer.color().direction().clock());
                        break;
                    case Input.Keys.E: // easing
                        pixmapRenderer.easing = !pixmapRenderer.easing;
                        break;
                    case Input.Keys.F: // fringe, affects outline/edge
                        pixmapRenderer.outline = !pixmapRenderer.outline;
                        break;
                    case Input.Keys.T: // try again
                        model.turner().reset();
                        diagonal = false;
                        angle = 2;
                        break;
                    case Input.Keys.A:
                        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT))
                        {
                            pixmapRenderer.color().set(Colorizer.AuroraBonusColorizer);
                            maker.setColorizer(Colorizer.AuroraBonusColorizer);
                        }
                        else
                        {
                            pixmapRenderer.color.set(Colorizer.AuroraColorizer);
                            maker.setColorizer(Colorizer.AuroraColorizer);
                        }
                        break;
                    case Input.Keys.S: // smaller palette, 64 colors
                        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT))
                        {
                            pixmapRenderer.color().set(colorizer);
                            maker.setColorizer(colorizer);
                        }
                        else 
                        {
                            pixmapRenderer.color().set(Colorizer.FlesurrectColorizer);
                            maker.setColorizer(Colorizer.FlesurrectColorizer);
                        }
                        break;
                    case Input.Keys.W: // write
                        VoxIO.writeVOX(FakeLanguageGen.SIMPLISH.word(Tools3D.hash64(voxels), true) + ".vox", voxels, maker.getColorizer().getReducer().paletteArray);
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
