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
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import squidpony.FakeLanguageGen;
import squidpony.squidmath.MiniMover64RNG;
import warpwriter.ModelMaker;
import warpwriter.Tools3D;
import warpwriter.VoxIO;
import warpwriter.model.AnimatedVoxelSeq;
import warpwriter.model.VoxelSeq;
import warpwriter.model.color.Colorizer;
import warpwriter.model.nonvoxel.LittleEndianDataInputStream;
import warpwriter.view.VoxelDraw;
import warpwriter.view.color.VoxelColor;
import warpwriter.view.render.MutantBatch;
import warpwriter.view.render.ShaderUtils;
import warpwriter.view.render.VoxelImmediateRenderer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class VoxelDrawSeqMutantTest extends ApplicationAdapter {
    public static final int SCREEN_WIDTH = 320;//640;
    public static final int SCREEN_HEIGHT = 360;//720;
    public static final int VIRTUAL_WIDTH = 320;
    public static final int VIRTUAL_HEIGHT = 360;
    protected SpriteBatch batch;
    protected Viewport worldView;
    protected Viewport screenView;
    protected BitmapFont font;
    protected FrameBuffer buffer;
    protected Texture screenTexture;
    protected TextureRegion screenRegion;
//    protected TurnModel model, ship;
    protected ModelMaker maker;
    protected VoxelImmediateRenderer renderer;
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
    protected Texture palette;
    protected ShaderProgram shader;
    protected long startTime;

    @Override
    public void create() {
        shader = new ShaderProgram(ShaderUtils.vertexShader, ShaderUtils.fragmentShaderRoberts);
        if(!shader.isCompiled())
            System.out.println(shader.getLog());
        palette = new Texture(Gdx.files.local("palettes/ReallyRelaxedRollBonus_GLSL.png"), Pixmap.Format.RGBA8888, false);
        palette.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        batch = new SpriteBatch(1000, shader);
        font = new BitmapFont(Gdx.files.internal("PxPlus_IBM_VGA_8x16.fnt"));
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
//        colorizer = Colorizer.AuroraColorizer;
        
        colorizer = Colorizer.RollBonusColorizer;
        renderer = new VoxelImmediateRenderer(VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        renderer.color().colorizer(colorizer);
        voxelColor = renderer.color();
//        batchRenderer = new VoxelSpriteBatchRenderer(batch);
        rng = new MiniMover64RNG(-123456789);
        maker = new ModelMaker(-123456789, colorizer);
//        try {
//            box = VoxIO.readVox(new LittleEndianDataInputStream(new FileInputStream("Aurora/dumbcube.vox")));
//        } catch (Exception e) {
//            e.printStackTrace();
//            box = maker.shipNoiseColorized();
//        }
//        makeBoom(maker.fireRange());
        maker.rng.setState(rng.nextLong());
        voxels = maker.shipLargeSmoothColorized();
        VoxelSeq vs = new VoxelSeq(1024);
        vs.putArray(voxels);
        vs.hollow();
        seq = new AnimatedVoxelSeq(new VoxelSeq[]{vs, new VoxelSeq(new int[]{1}, new byte[]{100})});
//        chaos = new ChaoticFetch(maker.rng.nextLong(), (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 1);
//        ship = new TurnModel().set(
////                new ReplaceFetch(ColorFetch.color((byte) 0), (byte) 1)
////                .add(new PaintFetch(chaos, true)).model(
//                new ArrayModel(voxels));
////        model = new TurnModel().set(model());
//        model = new TurnModel().set(ship);
//        model.setDuration(16);
        Gdx.input.setInputProcessor(inputProcessor());

        System.out.println("Testing on a .vox file this program wrote (old technique):");
        try {
            System.out.println(VoxIO.readPriorities(new LittleEndianDataInputStream(new FileInputStream("hasOwnPalette/Priorities.vox"))));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        System.out.println("Testing on a .vox file this program wrote (old technique) that was then opened and saved in MagicaVoxel:");
        try {
            System.out.println(VoxIO.readPriorities(new LittleEndianDataInputStream(new FileInputStream("hasOwnPalette/PrioritiesRewritten.vox"))));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        System.out.println("Testing on a .vox file this program wrote (new technique):");
        try {
            System.out.println(VoxIO.readPriorities(new LittleEndianDataInputStream(new FileInputStream("hasOwnPalette/Priorities2.vox"))));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        System.out.println("Testing on a .vox file this program wrote (new technique) that was then opened and saved in MagicaVoxel:");
        try {
            System.out.println(VoxIO.readPriorities(new LittleEndianDataInputStream(new FileInputStream("hasOwnPalette/PrioritiesRewritten2.vox"))));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        startTime = TimeUtils.millis();

    }

    public void makeBoom(byte[] fireColors) {
        long state = maker.rng.getState();
        seq = new AnimatedVoxelSeq(maker.animateExplosion(18, 40, 40, 32, fireColors));
        maker.rng.setState(state);
    }

    @Override
    public void render() {
        int time = (int) TimeUtils.timeSinceMillis(startTime);
        voxelColor.time(time * 5 >>> 9);

//        model.setFrame((int)(TimeUtils.millis() >>> 7) & 15);
//        boom.setFrame((int)(TimeUtils.millis() >>> 7) & 15);
//        if(seq != null) ((ITemporal) seq).setFrame((int)(TimeUtils.millis() * 5 >>> 9));
        buffer.begin();
        
        Gdx.gl.glClearColor(0.4f, 0.75f, 0.3f, 1f);
        // for GB_GREEN palette
//        Gdx.gl.glClearColor(0xE0 / 255f, 0xF8 / 255f, 0xD0 / 255f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        worldView.apply();
        worldView.getCamera().position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);
        worldView.update(VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
//        batch.setProjectionMatrix(screenView.getCamera().combined);
        //System.out.println(palette.getTextureObjectHandle());
        renderer.begin();
        if(angle > 2)
        {
            if(diagonal)
                VoxelDraw.drawIso(seq, renderer);
            else
                VoxelDraw.drawAbove(seq, renderer);
        }
        else{
            if(diagonal)
                VoxelDraw.draw45(seq, renderer);
            else
                VoxelDraw.draw(seq, renderer);
        }
//        batch.setPackedColor(-0x1.fffffep126f); // white as a packed float, resets any color changes that the renderer made
        renderer.end();
        buffer.end();

        Gdx.gl.glClearColor(0, 0, 0, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        screenView.apply();
        batch.setProjectionMatrix(screenView.getCamera().combined);
//        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0 + palette.getTextureObjectHandle());
        //System.out.println(palette.getTextureObjectHandle());
        batch.begin();
        screenTexture = buffer.getColorBufferTexture();
        screenTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        screenRegion.setRegion(screenTexture);
        screenRegion.flip(false, true);

        palette.bind(1);
        screenTexture.bind(0);
        shader.setUniformi("u_palette", 1);
        batch.draw(screenRegion, 0, 0);


        //font.draw(batch, model.voxels.length + ", " + model.voxels[0].length + ", " + model.voxels[0][0].length + ", " + " (original)", 0, 80);
//        font.draw(batch, model.sizeX() + ", " + model.sizeY() + ", " + model.sizeZ() + " (sizes)", 0, 60);
//        font.draw(batch, StringKit.join(", ", model.rotation().rotation()) + " (rotation)", 0, 40);
//        batch.end();
//        batch.begin();
        //// for GB_GREEN
        font.setColor(0x34 / 255f, 0x68 / 255f, 0x56 / 255f, 1f);
//        font.setColor(0f, 0f, 0f, 1f);
        font.draw(batch, Gdx.graphics.getFramesPerSecond() + " FPS", 0, 20);
//        System.out.println(Gdx.graphics.getFramesPerSecond() + " FPS");
        batch.end();

//        batch.begin();
//        shader.setUniformi("u_palette", palette.getTextureObjectHandle());
//        screenTexture = buffer.getColorBufferTexture();
//        screenTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
//        screenRegion.setRegion(screenTexture);
//        screenRegion.flip(false, true);
//        batch.draw(screenRegion, 0, 0);
//        //// for GB_GREEN
//        //font.setColor(0x34 / 255f, 0x68 / 255f, 0x56 / 255f, 1f);
//        font.setColor(0f, 0f, 0f, 1f);
//        //font.draw(batch, model.voxels.length + ", " + model.voxels[0].length + ", " + model.voxels[0][0].length + ", " + " (original)", 0, 80);
////        font.draw(batch, model.sizeX() + ", " + model.sizeY() + ", " + model.sizeZ() + " (sizes)", 0, 60);
////        font.draw(batch, StringKit.join(", ", model.rotation().rotation()) + " (rotation)", 0, 40);
//        font.draw(batch, Gdx.graphics.getFramesPerSecond() + " FPS", 0, 20);
//        batch.end();
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
        config.useVsync(true);
        config.setResizable(false);
        final VoxelDrawSeqMutantTest app = new VoxelDrawSeqMutantTest();
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
                        maker.rng.setState(rng.nextLong());
                        Tools3D.deepCopyInto(maker.shipLargeSmoothColorized(), voxels);
                        seq.setFrame(0);
                        seq.clear();
                        seq.putSurface(voxels);
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
                        String name = FakeLanguageGen.SIMPLISH.word(Tools3D.hash64(voxels), true) + ".vox";
                        VoxIO.writeAnimatedVOX(name, seq, maker.getColorizer().getReducer().paletteArray);
                        try {
                            System.out.println(VoxIO.readPriorities(new LittleEndianDataInputStream(new FileInputStream(name))));
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
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
