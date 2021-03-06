import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import squidpony.FakeLanguageGen;
import squidpony.squidmath.FastNoise;
import squidpony.squidmath.MiniMover64RNG;
import warpwriter.ModelMaker;
import warpwriter.Tools3D;
import warpwriter.VoxIO;
import warpwriter.model.AnimatedVoxelSeq;
import warpwriter.model.ITemporal;
import warpwriter.model.VoxelSeq;
import warpwriter.model.color.Colorizer;
import warpwriter.view.VoxelDraw;
import warpwriter.view.color.VoxelColor;
import warpwriter.view.render.MutantBatch;
import warpwriter.view.render.VoxelImmediateRenderer;

public class VoxelDrawSeqTest extends ApplicationAdapter {
    public static final int SCREEN_WIDTH = 320;//640;
    public static final int SCREEN_HEIGHT = 360;//720;
    public static final int VIRTUAL_WIDTH = 320;
    public static final int VIRTUAL_HEIGHT = 360;
    protected MutantBatch batch;
    protected Viewport worldView;
    protected Viewport screenView;
    protected BitmapFont font;
    protected FrameBuffer buffer;
    protected Texture screenTexture;
    protected TextureRegion screenRegion;
//    protected TurnModel model, ship;
    protected ModelMaker maker;
    protected VoxelImmediateRenderer batchRenderer;
    protected VoxelColor voxelColor;
    protected int angle = 2;
    protected boolean diagonal = false;
    protected boolean animating = false;
//    protected byte[][][][] explosion;
//    protected AnimatedArrayModel boom;
    private byte[][][] voxels;
//    private byte[][][] container;
    private AnimatedVoxelSeq seq;
    private Colorizer colorizer;
    protected MiniMover64RNG rng;

//    private ChaoticFetch chaos;

    @Override
    public void create() {
        font = new BitmapFont(Gdx.files.internal("PxPlus_IBM_VGA_8x16.fnt"));
        batch = new MutantBatch();
        worldView = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        screenView = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        buffer = new FrameBuffer(Pixmap.Format.RGBA8888, VIRTUAL_WIDTH, VIRTUAL_HEIGHT, true, false);
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
//        colorizer = Colorizer.LawnBonusColorizer;
        colorizer = Colorizer.RollBonusColorizer;
        batchRenderer = new VoxelImmediateRenderer(VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        batchRenderer.color().colorizer(colorizer);
        voxelColor = batchRenderer.color();
        rng = new MiniMover64RNG(123456789);
        maker = new ModelMaker(-123456789, colorizer);
//        try {
//            box = VoxIO.readVox(new LittleEndianDataInputStream(new FileInputStream("Aurora/dumbcube.vox")));
//        } catch (Exception e) {
//            e.printStackTrace();
//            box = maker.shipNoiseColorized();
//        }
//        makeBoom(maker.fireRange());
        maker.rng.setState(rng.nextLong());
        voxels = new byte[40][40][30];
        //voxels = maker.shipLargeSmoothColorized();
        //Tools3D.fill(voxels, 0);
        makeNetwork(rng.nextLong(), colorizer.getReducer().paletteMapping[rng.next(15)]);
        
        VoxelSeq vs = new VoxelSeq(1024);
        vs.putArray(voxels);
        vs.hollow();
        seq = new AnimatedVoxelSeq(vs, 4);
//        chaos = new ChaoticFetch(maker.rng.nextLong(), (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 1);
//        ship = new TurnModel().set(
////                new ReplaceFetch(ColorFetch.color((byte) 0), (byte) 1)
////                .add(new PaintFetch(chaos, true)).model(
//                new ArrayModel(voxels));
////        model = new TurnModel().set(model());
//        model = new TurnModel().set(ship);
//        model.setDuration(16);
        Gdx.input.setInputProcessor(inputProcessor());
    }
    
    public void makeNetwork(long seed, byte color)
    {
        FastNoise cells = new FastNoise((int) seed, 0.07f, FastNoise.CELLULAR)
                , perturb = new FastNoise((int)(seed>>>32), 0.37f, FastNoise.SIMPLEX_FRACTAL, 3);
        float adj;
        cells.setCellularReturnType(FastNoise.DISTANCE);
        for (int x = 0; x < voxels.length; x++) {
            for (int y = 0; y < voxels[0].length; y++) {
                for (int z = 0; z < voxels[0][0].length; z++) {
                    adj = perturb.getConfiguredNoise(z, x, y);
                    if(cells.getConfiguredNoise(x + adj, y - adj, z) >= -0.5f)
                        voxels[x][y][z] = color;
                }
            }
        }
    }

    public void makeBoom(byte[] fireColors) {
        long state = maker.rng.getState();
        seq = new AnimatedVoxelSeq(maker.animateExplosion(18, 40, 40, 32, fireColors));
        maker.rng.setState(state);
    }

    @Override
    public void render() {
//        model.setFrame((int)(TimeUtils.millis() >>> 7) & 15);
//        boom.setFrame((int)(TimeUtils.millis() >>> 7) & 15);
        if(seq != null)
            ((ITemporal) seq).setFrame((int)(TimeUtils.millis() * 5 >>> 9));
        buffer.begin();
        
        Gdx.gl.glClearColor(0.4f, 0.75f, 0.3f, 1f);
        // for GB_GREEN palette
//        Gdx.gl.glClearColor(0xE0 / 255f, 0xF8 / 255f, 0xD0 / 255f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        worldView.apply();
        worldView.getCamera().position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);
        worldView.update(VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
//        System.out.println(seq.size());
//        batch.setProjectionMatrix(screenView.getCamera().combined);
        batchRenderer.begin();
        if(angle > 2)
        {
            if(diagonal)
                VoxelDraw.drawIso(seq, batchRenderer);
            else
                VoxelDraw.drawAbove(seq, batchRenderer);
        }
        else{
            if(diagonal)
                VoxelDraw.draw45(seq, batchRenderer);
            else
                VoxelDraw.draw(seq, batchRenderer);
        }
        batch.setPackedColor(-0x1.fffffep126f); // white as a packed float, resets any color changes that the renderer made
        batchRenderer.end();
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
//        font.draw(batch, model.sizeX() + ", " + model.sizeY() + ", " + model.sizeZ() + " (sizes)", 0, 60);
//        font.draw(batch, StringKit.join(", ", model.rotation().rotation()) + " (rotation)", 0, 40);
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
        config.setResizable(false);
        final VoxelDrawSeqTest app = new VoxelDrawSeqTest();
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
//                        diagonal = false;
                        angle = 3;
                        break;
                    case Input.Keys.U:
                            seq.clockX();
                        System.out.println("Current rotation: " + seq.rotation());
                        break;
                    case Input.Keys.J:
                            seq.counterX();
                        System.out.println("Current rotation: " + seq.rotation());
                        break;
                    case Input.Keys.I:
                        seq.clockY();
                        System.out.println("Current rotation: " + seq.rotation());
                        break;
                    case Input.Keys.K:
                        seq.counterY();
                        System.out.println("Current rotation: " + seq.rotation());
                        break;
                    case Input.Keys.O:
                        if((seq.rotation() & 28) == 0 ^ (diagonal = !diagonal)) // angle == 3 ||  
                            seq.clockZ();
                        System.out.println("Current rotation: " + seq.rotation());
                        break;
                    case Input.Keys.L:
                        if((seq.rotation() & 28) != 0 ^ (diagonal = !diagonal)) // angle == 3 ||  
                            seq.counterZ();
                        System.out.println("Current rotation: " + seq.rotation());
                        break;
//                    case Input.Keys.R:
//                        model.rotation().reset();
//                        break;
                    case Input.Keys.P:
//                        model.set(model());
//                        model.set(ship);
//                        chaos.setSeed(maker.rng.nextLong());
                        
//                        maker.rng.setState(rng.nextLong());
//                        Tools3D.deepCopyInto(maker.shipLargeSmoothColorized(), voxels);
                        
                        Tools3D.fill(voxels, 0);
                        makeNetwork(rng.nextLong(), colorizer.getReducer().paletteMapping[rng.next(15)]);
                        
                        seq.setFrame(0);
                        seq.clear();
                        seq.putSurface(voxels);
                        seq = new AnimatedVoxelSeq(seq.seqs[0], 4);
                        animating = false;
                        break;
                    case Input.Keys.B: // burn!
                        maker.rng.setState(rng.nextLong());
                        makeBoom(maker.fireRange());
                        animating = true;
                        break;
                    case Input.Keys.Z: // zap!
                        maker.rng.setState(rng.nextLong());
                        makeBoom(maker.randomFireRange());
                        animating = true;
                        break;
                    case Input.Keys.G:
                        voxelColor.direction(voxelColor.direction().counter());
                        break;
                    case Input.Keys.H:
                        voxelColor.direction(voxelColor.direction().clock());
                        break;
                    case Input.Keys.T: // try again
//                        model.rotation().reset();
                        diagonal = false;
                        angle = 2;
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
