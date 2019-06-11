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
import squidpony.squidmath.MiniMover64RNG;
import warpwriter.ModelMaker;
import warpwriter.Tools3D;
import warpwriter.VoxIO;
import warpwriter.model.VoxelSeq;
import warpwriter.model.color.Colorizer;
import warpwriter.model.nonvoxel.Transform;
import warpwriter.model.nonvoxel.TurnQuaternion;
import warpwriter.view.color.VoxelColor;
import warpwriter.view.render.MutantBatch;
import warpwriter.view.render.VoxelSpotRenderer;

public class VoxelSpotTest extends ApplicationAdapter {
    public static final int SCREEN_WIDTH = 320;//640;
    public static final int SCREEN_HEIGHT = 360;//720;
    public static final int VIRTUAL_WIDTH = 80;
    public static final int VIRTUAL_HEIGHT = 90;
    protected MutantBatch batch;
    protected Viewport worldView;
    protected Viewport screenView;
    protected BitmapFont font;
    protected FrameBuffer buffer;
    protected Texture screenTexture, pmTexture;
    protected TextureRegion screenRegion;
//    protected TurnModel model, ship;
    protected ModelMaker maker;
    protected VoxelSpotRenderer batchRenderer;
    protected VoxelColor voxelColor;
    protected int angle = 3;
    protected boolean diagonal = true;
    protected boolean animating = false;
//    protected byte[][][][] explosion;
//    protected AnimatedArrayModel boom;
    private byte[][][] voxels;
//    private byte[][][] container;
    private VoxelSeq seq;
    private VoxelSeq middleSeq;
    private VoxelSeq axes;
    private Colorizer colorizer;
    protected MiniMover64RNG rng;
    private TurnQuaternion turnX90, turnY90, turnZ90;
    private Transform[] transforms;
    private Transform transformMid;
    private float alpha;
    private long startTime;
//    private GifRecorder gifRecorder;
//    private ChaoticFetch chaos;

    @Override
    public void create() {
        font = new BitmapFont(Gdx.files.internal("PxPlus_IBM_VGA_8x16.fnt"));
        worldView = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        screenView = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        buffer = new FrameBuffer(Pixmap.Format.RGBA8888, VIRTUAL_WIDTH, VIRTUAL_HEIGHT, false, false);
        screenRegion = new TextureRegion();
        screenView.getCamera().position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);
        screenView.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
//        colorizer = Colorizer.arbitraryBonusColorizer(Coloring.AURORA);
//        colorizer = Colorizer.arbitraryColorizer(Coloring.GB_GREEN);
//        colorizer = Colorizer.arbitraryColorizer(Coloring.DB32);
//        colorizer = Colorizer.arbitraryColorizer(Coloring.BLK36);
//        colorizer = Colorizer.arbitraryBonusColorizer(Coloring.UNSEVEN);
//        colorizer = Colorizer.arbitraryBonusColorizer(Coloring.CW_PALETTE);
//        colorizer = Colorizer.arbitraryBonusColorizer(Coloring.VGA256);
//        colorizer = Colorizer.arbitraryBonusColorizer(Coloring.FLESURRECT);
        colorizer = Colorizer.JudgeBonusColorizer;
        voxelColor = new VoxelColor().set(colorizer);
        batchRenderer = new VoxelSpotRenderer(VIRTUAL_WIDTH, VIRTUAL_HEIGHT).setOffset(VIRTUAL_WIDTH, 0).flipX();
        batch = new MutantBatch();
        batchRenderer.color().set(colorizer);
        rng = new MiniMover64RNG(-123456789);
        maker = new ModelMaker(-123456789, colorizer);
//        transformStart = new Transform();
//        transformEnd = new Transform(32, 64, 128, 0, 0, 0);
        turnX90 = new TurnQuaternion().setEulerAnglesBrad(64, 0, 0);
        turnY90 = new TurnQuaternion().setEulerAnglesBrad(0, 64, 0);
        turnZ90 = new TurnQuaternion().setEulerAnglesBrad(0, 0, 64);
        transforms = new Transform[12];
//        transforms[0] = new Transform();
//        transforms[1] = new Transform(transforms[0].rotation.cpy().mul(turnZ90).nor(), 0, 0, 0, 1, 1, 1);
//        for (int i = 0; i < transforms.length; i+=3) {
//            if(i == 0)
//                transforms[i] = new Transform(new TurnQuaternion().mul(turnX90).nor(), 0, 0, 0, 1, 1, 1);
//            else
//                transforms[i] = new Transform(transforms[i-1].rotation.cpy().mul(turnX90).nor(), 0, 0, 0, 1, 1, 1);
//            transforms[i+1] = new Transform(transforms[i].rotation.cpy().mul(turnY90).nor(), 0, 0, 0, 1, 1, 1);
//            transforms[i+2] = new Transform(transforms[i+1].rotation.cpy().mul(turnZ90).nor(), 0, 0, 0, 1, 1, 1);
//        }
        for (int i = 0; i < transforms.length; i+=2) {
            if(i == 0)
                transforms[i] = new Transform(new TurnQuaternion().mul(turnX90).nor(), 0, 0, 0, 1, 1, 1);
            else
                transforms[i] = new Transform(transforms[i-1].rotation.cpy().mul(turnX90).nor(), 0, 0, 0, 1, 1, 1);
            transforms[i+1] = new Transform(transforms[i].rotation.cpy().mul(turnZ90).nor(), 0, 0, 0, 1, 1, 1);
        }
        transformMid = new Transform();
//        try {
//            box = VoxIO.readVox(new LittleEndianDataInputStream(new FileInputStream("Aurora/dumbcube.vox")));
//        } catch (Exception e) {
//            e.printStackTrace();
//            box = maker.shipNoiseColorized();
//        }
//        makeBoom(maker.fireRange());
        byte red = colorizer.reduce(0xFF0000FF), green = colorizer.reduce(0x00FF00FF),
                blue = colorizer.reduce(0x0000FFFF), white = colorizer.reduce(0xFFFFFFFF);
        maker.rng.setState(rng.nextLong());
        voxels = maker.shipLargeSmoothColorized();
//        voxels = new byte[60][60][60];
//        seq = new VoxelSeq(1024);
//        seq.putSurface(voxels);
        
//        for (int i = 30; i < 60; i++) {
//            voxels[i ][30][30] =(red);
//            voxels[30][i ][30] =(green);
//            voxels[30][30][i ] =(blue);
//        }
//        voxels[50][30][30]=(white);
//        voxels[49][31][30]=(white);
//        voxels[49][29][30]=(white);
//        voxels[49][30][31]=(white);
//        voxels[49][30][29]=(white);
//        voxels[48][31][30]=(white);
//        voxels[48][29][30]=(white);
//        voxels[48][30][31]=(white);
//        voxels[48][30][29]=(white);
//        voxels[48][31][31]=(white);
//        voxels[48][29][29]=(white);
//        voxels[48][29][31]=(white);
//        voxels[48][31][29]=(white);
        seq = new VoxelSeq(1024);
        seq.putSurface(voxels);
//        seq.hollow();
        middleSeq = new VoxelSeq(seq.fullSize());
        middleSeq.sizeX(60);
        middleSeq.sizeY(60);
        middleSeq.sizeZ(60);
//        axes = new VoxelSeq(180);
//        axes.sizeX(60);
//        axes.sizeY(60);
//        axes.sizeZ(60);
        
//        chaos = new ChaoticFetch(maker.rng.nextLong(), (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 1);
//        ship = new TurnModel().set(
////                new ReplaceFetch(ColorFetch.color((byte) 0), (byte) 1)
////                .add(new PaintFetch(chaos, true)).model(
//                new ArrayModel(voxels));
////        model = new TurnModel().set(model());
//        model = new TurnModel().set(ship);
//        model.setDuration(16);
        Gdx.input.setInputProcessor(inputProcessor());
//        gifRecorder = new GifRecorder(batch);
//        gifRecorder.setGUIDisabled(true);
//        gifRecorder.open();
//        gifRecorder.setBounds(SCREEN_WIDTH * -0.5f, SCREEN_HEIGHT * -0.5f, SCREEN_WIDTH, SCREEN_HEIGHT);
//        gifRecorder.setFPS(16);
//        gifRecorder.startRecording();

        startTime = TimeUtils.millis();
    }

//    public void makeBoom(byte[] fireColors) {
//        long state = maker.rng.getState();
//        seq = new AnimatedVoxelSeq(maker.animateExplosion(18, 40, 40, 32, fireColors));
//        maker.rng.setState(state);
//    }

    @Override
    public void render() {
//        model.setFrame((int)(TimeUtils.millis() >>> 7) & 15);
//        boom.setFrame((int)(TimeUtils.millis() >>> 7) & 15);
        if(seq != null)
        {
            long time = TimeUtils.timeSinceMillis(startTime);
//            ((ITemporal) seq).setFrame((int)(TimeUtils.millis() * 5 >>> 9));
            alpha = (time & 0x3FFL) * 0x1p-10f;
            transforms[(int) (time >>> 10) % transforms.length].interpolateInto(transforms[((int) (time >>> 10) + 1) % transforms.length], alpha, transformMid);
//            transforms[0].interpolateInto(transforms[1], alpha, transformMid);
//            if((time & 0x800L) == 0L) 
//                transformStart.interpolateInto(transformEnd, alpha, transformMid);
//            else
//                transformStart.interpolateInto(transformEnd, 1f - alpha, transformMid);
            middleSeq.clear();
            transformMid.transformInto(seq, middleSeq, 29.5f, 29.5f, 29.5f);
        }
        buffer.begin();
        
        Gdx.gl.glClearColor(0.4f, 0.75f, 0.3f, 1f);
        // for GB_GREEN palette
//        Gdx.gl.glClearColor(0xE0 / 255f, 0xF8 / 255f, 0xD0 / 255f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        worldView.apply();
        worldView.getCamera().position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);
        worldView.update(VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        batchRenderer.begin();
        batchRenderer.drawAbove45(middleSeq);
//        if(angle > 2)
//        {
//            if(diagonal)
//                VoxelDraw.drawIso(middleSeq, batchRenderer);
//            else
//                VoxelDraw.drawAbove(middleSeq, batchRenderer);
//        }
//        else{
//            if(diagonal)
//                VoxelDraw.draw45(middleSeq, batchRenderer);
//            else
//                VoxelDraw.draw(middleSeq, batchRenderer);
//        }
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
        //font.draw(batch, model.voxels.length + ", " + model.voxels[0].length + ", " + model.voxels[0][0].length + ", " + " (original)", 0, 80);
//        font.draw(batch, model.sizeX() + ", " + model.sizeY() + ", " + model.sizeZ() + " (sizes)", 0, 60);
//        font.draw(batch, StringKit.join(", ", model.rotation().rotation()) + " (rotation)", 0, 40);

//        font.setColor(0f, 0f, 0f, 1f);
//        font.draw(batch, Gdx.graphics.getFramesPerSecond() + " FPS", 0, 20);
        batch.end();
//        gifRecorder.update();
//        if(gifRecorder.isRecording() && TimeUtils.timeSinceMillis(startTime) > 0x800L){
//            gifRecorder.finishRecording();
//            gifRecorder.writeGIF();
//        }

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
        final VoxelSpotTest app = new VoxelSpotTest();
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
                            middleSeq.clockX();
                        System.out.println("Current rotation: " + middleSeq.rotation());
                        break;
                    case Input.Keys.J:
                            middleSeq.counterX();
                        System.out.println("Current rotation: " + middleSeq.rotation());
                        break;
                    case Input.Keys.I:
                        middleSeq.clockY();
                        System.out.println("Current rotation: " + middleSeq.rotation());
                        break;
                    case Input.Keys.K:
                        middleSeq.counterY();
                        System.out.println("Current rotation: " + middleSeq.rotation());
                        break;
                    case Input.Keys.O:
                        if((middleSeq.rotation() & 28) == 0 ^ (diagonal = !diagonal)) // angle == 3 ||  
                            middleSeq.clockZ();
                        System.out.println("Current rotation: " + middleSeq.rotation());
                        break;
                    case Input.Keys.L:
                        if((middleSeq.rotation() & 28) != 0 ^ (diagonal = !diagonal)) // angle == 3 ||  
                            middleSeq.counterZ();
                        System.out.println("Current rotation: " + middleSeq.rotation());
                        break;
//                    case Input.Keys.R:
//                        model.rotation().reset();
//                        break;
                    case Input.Keys.P:
//                        model.set(model());
//                        model.set(ship);
//                        chaos.setSeed(maker.rng.nextLong());
                        maker.rng.setState(rng.nextLong());
                        Tools3D.deepCopyInto(maker.shipLargeSmoothColorized(), voxels);
//                        seq.setFrame(0);
                        seq.clear();
                        seq.putSurface(voxels);
//                        seq = new AnimatedVoxelSeq(seq.seqs[0], 4);
                        animating = false;
                        break;
//                    case Input.Keys.B: // burn!
//                        maker.rng.setState(rng.nextLong());
//                        makeBoom(maker.fireRange());
//                        animating = true;
//                        break;
//                    case Input.Keys.Z: // zap!
//                        maker.rng.setState(rng.nextLong());
//                        makeBoom(maker.randomFireRange());
//                        animating = true;
//                        break;
                    case Input.Keys.G:
                        voxelColor.set(voxelColor.direction().counter());
                        break;
                    case Input.Keys.H:
                        voxelColor.set(voxelColor.direction().clock());
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
