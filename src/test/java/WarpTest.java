import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import squidpony.FakeLanguageGen;
import squidpony.squidmath.MiniMover64RNG;
import warpwriter.ModelMaker;
import warpwriter.Tools3D;
import warpwriter.VoxIO;
import warpwriter.model.IFetch;
import warpwriter.model.IModel;
import warpwriter.model.TurnModel;
import warpwriter.model.color.Colorizer;
import warpwriter.model.decide.DecideFetch;
import warpwriter.model.decide.SphereDecide;
import warpwriter.model.fetch.AnimatedArrayModel;
import warpwriter.model.fetch.ArrayModel;
import warpwriter.model.fetch.ColorFetch;
import warpwriter.model.fetch.WorldFetch;
import warpwriter.model.nonvoxel.HashMap3D;
import warpwriter.view.WarpDraw;
import warpwriter.view.color.VoxelColor;
import warpwriter.view.render.VoxelPixmapRenderer;

public class WarpTest extends ApplicationAdapter {
    public static final int SCREEN_WIDTH = 320;//640;
    public static final int SCREEN_HEIGHT = 360;//720;
    public static final int VIRTUAL_WIDTH = 320;
    public static final int VIRTUAL_HEIGHT = 360;

    /**
     * This is the default vertex shader from libGDX.
     */
    public static final String vertexShader = "attribute vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
            + "attribute vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
            + "attribute vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
            + "uniform mat4 u_projTrans;\n" //
            + "varying vec4 v_color;\n" //
            + "varying vec2 v_texCoords;\n" //
            + "\n" //
            + "void main()\n" //
            + "{\n" //
            + "   v_color = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
            + "   v_color.a = v_color.a * (255.0/254.0);\n" //
            + "   v_texCoords = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
            + "   gl_Position =  u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
            + "}\n";

    /**
     * This fragment shader substitutes colors with ones from a palette, dithering as needed.
     */
//    public static final String fragmentShader = "#version 150\n" +
//            "varying vec2 v_texCoords;\n" +
//            "varying vec4 v_color;\n" +
//            "uniform sampler2D u_texture;\n" +
//            "uniform sampler2D u_palette;\n" +
//            "void main()\n" +
//            "{\n" +
//            "   vec4 tgt = texture2D( u_texture, v_texCoords );\n" +
//            "   vec4 used = texture2D(u_palette, vec2((tgt.b + floor(tgt.r * 31.99999)) * 0.03125, 1.0 - tgt.g));\n" +
//            "   float adj = sin(gl_FragCoord.x * 4.743036261279236 + gl_FragCoord.y * 3.580412143837574) * 0.75 + 0.5;\n" +
//            "   tgt.rgb = clamp(tgt.rgb + (used.rgb - tgt.rgb) * adj, 0.0, 1.0);\n" +
//            "   gl_FragColor = v_color * texture2D(u_palette, vec2((tgt.b + floor(tgt.r * 31.99999)) * 0.03125, 1.0 - tgt.g));\n" + //(tgt.b + floor(tgt.r * 32.0)) * 0.03125, tgt.g
//            "   gl_FragColor.a = v_color.a * tgt.a;\n" +
//            "}";
    public static final String fragmentShader = "#version 150\n" +
            "varying vec2 v_texCoords;\n" +
            "varying vec4 v_color;\n" +
            "uniform sampler2D u_texture;\n" +
            "uniform sampler2D u_palette;\n" +
            "const float b_adj = 31.0 / 32.0;\n" +
            "const float rb_adj = 32.0 / 1023.0;\n" +
            "void main()\n" +
            "{\n" +
            "   vec4 tgt = texture2D( u_texture, v_texCoords );\n" +
//            "   gl_FragColor = texture2D(u_palette, vec2((tgt.b * b_adj + floor(tgt.r * 31.999)) * rb_adj, 1.0 - tgt.g));\n" + //solid shading
            "   vec4 used = texture2D(u_palette, vec2((tgt.b * b_adj + floor(tgt.r * 31.999)) * rb_adj, 1.0 - tgt.g));\n" +
            "   float len = length(tgt.rgb) * 0.75;\n" +
            "   float adj = sin(dot(gl_FragCoord.xy, vec2(4.743036261279236, 3.580412143837574)) + len) * (len * len + 0.175);\n" +
            "   tgt.rgb = clamp(tgt.rgb + (tgt.rgb - used.rgb) * adj, 0.0, 1.0);\n" +
            "   gl_FragColor.rgb = v_color.rgb * texture2D(u_palette, vec2((tgt.b * b_adj + floor(tgt.r * 31.999)) * rb_adj, 1.0 - tgt.g)).rgb;\n" +
            "   gl_FragColor.a = v_color.a * tgt.a;\n" +
            "}";
//            "   float adj = fract(dot(gl_FragCoord.xy, vec2(0.7548776662466927, 0.5698402909980532))) * 2.15 - 1.1;\n" +
//            "   float adj = sin(dot(gl_FragCoord.xy, vec2(2.371518130639618, 1.7902060719189539)));\n" +


    protected SpriteBatch batch;
    protected Viewport worldView;
    protected Viewport screenView;
//    protected BitmapFont font;
    protected FrameBuffer buffer;
    protected Texture screenTexture, pmTexture;
    protected TextureRegion screenRegion;
    protected TurnModel model, ship;
    protected ModelMaker maker;
    private VoxelPixmapRenderer pixmapRenderer;
    protected VoxelColor voxelColor;
    protected int angle = 3;
    protected boolean diagonal = true;
    protected boolean animating = false;
    protected AnimatedArrayModel boom;
    private byte[][][] voxels;
    private Colorizer colorizer;
    private ShaderProgram shader, defaultShader;
    private Texture palette;
    protected MiniMover64RNG rng;

//    private ChaoticFetch chaos;

    @Override
    public void create() {
//        font = new BitmapFont(Gdx.files.internal("PxPlus_IBM_VGA_8x16.fnt"));
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
//        colorizer = Colorizer.arbitraryBonusColorizer(Coloring.AURORA); // 1020 possible colors, will be reduced to 63
        colorizer = Colorizer.FlesurrectBonusColorizer;
        voxelColor = new VoxelColor().set(colorizer);
        pixmapRenderer = new VoxelPixmapRenderer().set(new Pixmap(512, 512, Pixmap.Format.RGBA8888)).set(voxelColor);
        pmTexture = new Texture(pixmapRenderer.pixmap());
        rng = new MiniMover64RNG(-123456789);
        maker = new ModelMaker(-123456789, colorizer);
//        try {
//            box = VoxIO.readVox(new LittleEndianDataInputStream(new FileInputStream("Aurora/dumbcube.vox")));
//        } catch (Exception e) {
//            e.printStackTrace();
//            box = maker.shipNoiseColorized();
//        }
        makeBoom(maker.fireRange());
        maker.rng.setState(rng.nextLong());
        voxels = maker.shipLargeNoiseColorized();
//        chaos = new ChaoticFetch(maker.rng.nextLong(), (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 1);
        ship = new TurnModel().set(
//                new ReplaceFetch(ColorFetch.color((byte) 0), (byte) 1)
//                .add(new PaintFetch(chaos, true)).model(
                new ArrayModel(voxels));
        model = new TurnModel().set(ship);
        // TODO: restore time here
        //model.setDuration(16);
        Gdx.input.setInputProcessor(inputProcessor());

        ////choose palette here
        palette = new Texture(Gdx.files.local("palettes/FlesurrectBonus_GLSL.png"), Pixmap.Format.RGBA8888, false);
//        palette = new Texture(Gdx.files.local("palettes/DB8_GLSL.png"), Pixmap.Format.RGBA8888, false);
//        palette = new Texture(Gdx.files.local("palettes/Unseven_GLSL.png"), Pixmap.Format.RGBA8888, false);
        
        defaultShader = SpriteBatch.createDefaultShader();
        shader = new ShaderProgram(vertexShader, fragmentShader);
        if (!shader.isCompiled()) throw new GdxRuntimeException("Couldn't compile shader: " + shader.getLog());
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
//        FetchModel fm = new FetchModel(48, 48, 32);
//        container = new byte[48][48][32];
        long state = maker.rng.getState();
//        Tools3D.translateCopyInto(maker.shipNoiseColorized(), container, 18, 18, 0);
//        AnimatedArrayModel fire = new AnimatedArrayModel(maker.animateExplosion(18, 40, 40, 32, fireColors));
//        BurstFetch burst = new BurstFetch(new ArrayModel(container), 24, 24, 3, 16, 2);
//        fm.add(burst).add(new OffsetModel(-4, -4, -2).add(fire));
//        byte[][][][] voxelFrames = new byte[16][][][];
//        boom = new AnimatedArrayModel(voxelFrames);
//        for (int i = 0; i < 16; i++) {
//            burst.setFrame(i);
//            fire.setFrame(i+2);
//            voxelFrames[i] = new ArrayModel(fm).voxels;
//        }
        boom = new AnimatedArrayModel(maker.animateExplosion(18, 40, 40, 32, fireColors));
        maker.rng.setState(state);
    }

    @Override
    public void render() {
        // TODO: Restore time here
        //model.setFrame((int)(TimeUtils.millis() >>> 7) & 15);
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
        batch.setShader(shader);
        batch.setColor(-0x1.fffffep126f);
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE1);
        palette.bind();
        batch.begin();
        shader.setUniformi("u_palette", 1);
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
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
        //batch.setColor(-0x1.fffffep126f); // white as a packed float, resets any color changes that the renderer made
        batch.draw(pmTexture, 64, 64);
        batch.end();
        buffer.end();
        Gdx.gl.glClearColor(0, 0, 0, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        screenView.apply();
        batch.setProjectionMatrix(screenView.getCamera().combined);
//        batch.setColor(-0x1.fffffep126f); // white as a packed float, resets any color changes that the renderer made
//        batch.setColor(Color.RED);
        batch.setShader(defaultShader);
        batch.begin();
        screenTexture = buffer.getColorBufferTexture();
        screenTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        screenTexture.bind();
        screenRegion.setRegion(screenTexture);
        screenRegion.flip(false, true);
        batch.draw(screenRegion, 0, 0);
        //// for GB_GREEN
        //font.setColor(0x34 / 255f, 0x68 / 255f, 0x56 / 255f, 1f);
        batch.setColor(-0x1.fffffep126f);
//        font.setColor(0f, 0f, 0f, 1f);
        //font.draw(batch, model.voxels.length + ", " + model.voxels[0].length + ", " + model.voxels[0][0].length + ", " + " (original)", 0, 80);
        
//        font.draw(batch, model.sizeX() + ", " + model.sizeY() + ", " + model.sizeZ() + " (sizes)", 0, 60);
//        font.draw(batch, StringKit.join(", ", model.turner().rotation()) + " (rotation)", 0, 40);
//        font.draw(batch, Gdx.graphics.getFramesPerSecond() + " FPS", 0, 20);
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
                        maker.rng.setState(rng.nextLong());
                        model.set(ship);
//                        chaos.setSeed(maker.rng.nextLong());
                        Tools3D.deepCopyInto(maker.shipLargeNoiseColorized(), voxels);
                        animating = false;
                        break;
                    case Input.Keys.B: // burn!
                        //maker.rng.setState(rng.nextLong());
                        makeBoom(maker.fireRange());
                        model.set(boom);
                        animating = true;
                        break;
                    case Input.Keys.Z: // zap!
                        //maker.rng.setState(rng.nextLong());
                        makeBoom(maker.randomFireRange());
                        model.set(boom);
                        animating = true;
                        break;
                    case Input.Keys.G:
                        pixmapRenderer.color().set(pixmapRenderer.color().direction().counter());
                        break;
                    case Input.Keys.H:
                        pixmapRenderer.color().set(pixmapRenderer.color().direction().clock());
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
                            pixmapRenderer.voxelColor().set(Colorizer.AuroraColorizer);
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
