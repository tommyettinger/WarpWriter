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
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import squidpony.StringKit;
import squidpony.squidmath.NumberTools;
import warpwriter.ModelMaker;
import warpwriter.Tools3D;
import warpwriter.model.FetchModel;
import warpwriter.model.IModel;
import warpwriter.model.color.Colorizer;
import warpwriter.model.fetch.AnimatedArrayModel;
import warpwriter.model.fetch.ArrayModel;
import warpwriter.model.fetch.BurstFetch;
import warpwriter.model.fetch.OffsetModel;
import warpwriter.view.VoxelSprite;
import warpwriter.view.render.VoxelSpriteBatchRenderer;

public class BurstTest extends ApplicationAdapter {
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
     * This fragment shader draws a black outline around things.
     */
    public static final String fragmentShader = "#version 150\n" +
            "varying vec2 v_texCoords;\n" +
            "varying vec4 v_color;\n" +
            "uniform float outlineH;\n" +
            "uniform float outlineW;\n" +
            "uniform sampler2D u_texture;\n" +
            "void main()\n" +
            "{\n" +
            "   vec2 offsetx;\n" +
            "   offsetx.x = outlineW;\n" +
            "   vec2 offsety;\n" +
            "   offsety.y = outlineH;\n" +
            "   float alpha = texture2D( u_texture, v_texCoords ).a;\n" +
            "   alpha = max(alpha, texture2D( u_texture, v_texCoords + offsetx).a);\n" +
            "   alpha = max(alpha, texture2D( u_texture, v_texCoords - offsetx).a);\n" +
            "   alpha = max(alpha, texture2D( u_texture, v_texCoords + offsety).a);\n" +
            "   alpha = max(alpha, texture2D( u_texture, v_texCoords - offsety).a);\n" +
            "   gl_FragColor = v_color * texture2D( u_texture, v_texCoords );\n" +
            "   gl_FragColor.a = alpha;\n" +
            "}";
    /**
     * This fragment shader draws a black outline around things.
     */
    public static final String fragmentShaderLighterOutline = "#version 150\n" +
            "varying vec2 v_texCoords;\n" +
            "varying vec4 v_color;\n" +
            "uniform float outlineH;\n" +
            "uniform float outlineW;\n" +
            "uniform sampler2D u_texture;\n" +
            "void main()\n" +
            "{\n" +
            "   vec2 offsetx;\n" +
            "   offsetx.x = outlineW;\n" +
            "   vec2 offsety;\n" +
            "   offsety.y = outlineH;\n" +
            "   vec4 self = texture2D( u_texture, v_texCoords );\n" +
            "   if(self.a > 0.0)\n" +
            "   {\n" +
            "     gl_FragColor = v_color * self;\n" +
            "   }\n" +
            "   else\n" +
            "   {\n" +
            "     vec4 e = texture2D( u_texture, v_texCoords + offsetx);\n" +
            "     vec4 w = texture2D( u_texture, v_texCoords - offsetx);\n" +
            "     vec4 n = texture2D( u_texture, v_texCoords + offsety);\n" +
            "     vec4 s = texture2D( u_texture, v_texCoords - offsety);\n" +
            "     gl_FragColor.rgb = e.rgb * e.a + w.rgb * w.a + n.rgb * n.a + s.rgb * s.a;\n" +
            "     gl_FragColor.a = 0;\n" +
            "     if(length(gl_FragColor.rgb) > 0.0)\n" +
            "     {\n" +
            "       gl_FragColor.rgb /= (e.a + w.a + n.a + s.a) * 1.75;\n" +
            "       gl_FragColor.a = 1.0;\n" +
            "     }\n" +
            "   }\n" +
            "}";

    //public static final int backgroundColor = Color.rgba8888(Color.DARK_GRAY);
    public static final int SCREEN_WIDTH = 1280;
    public static final int SCREEN_HEIGHT = 720;
    public static final int VIRTUAL_WIDTH = 1280;
    public static final int VIRTUAL_HEIGHT = 720;
    protected SpriteBatch batch;
    protected Viewport worldView;
    protected Viewport screenView;
    protected BitmapFont font;
    protected FrameBuffer buffer;
    protected Texture screenTexture;
    protected TextureRegion screenRegion;
    protected ModelMaker maker;
    protected VoxelSprite voxelSprite;
    protected VoxelSpriteBatchRenderer batchRenderer;
    protected ShaderProgram shader;
    protected ShaderProgram defaultShader;

    protected byte[][][] container;
    protected AnimatedArrayModel am;
    protected long startTime;

    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Simple Tester");
        config.setWindowedMode(SCREEN_WIDTH, SCREEN_HEIGHT);
        config.setIdleFPS(10);
        config.useVsync(false);
        config.setResizable(false);
        final BurstTest app = new BurstTest();
//        config.setWindowListener(new Lwjgl3WindowAdapter() {
//            @Override
//            public void filesDropped(String[] files) {
//                if (files != null && files.length > 0) {
//                    if (files[0].endsWith(".vox"))
//                        app.load(files[0]);
//                }
//            }
//        });

        new Lwjgl3Application(app, config);
    }

//    public void load(String name) {
//        try {
//            //// loads a file by its full path, which we get via drag+drop
//            final byte[][][] arr = VoxIO.readVox(new LittleEndianDataInputStream(new FileInputStream(name)));
//            //// set the palette to the one from the vox model, using arbitraryDimmer()
//            batchRenderer.set(batchRenderer.color().set(Dimmer.arbitraryDimmer(VoxIO.lastPalette)));
//            voxelSprite.set(new ArrayModel(
//                    arr
//                    //// Aurora folder has vox models with a different palette, which involves a different IDimmer.
//                    //VoxIO.readVox(new LittleEndianDataInputStream(new FileInputStream("Aurora/Warrior_Male_W.vox")))
//                    // If using Rinsed, use the line below instead of the one above.
//                    //maker.warriorRandom()
//            ));
//        } catch (FileNotFoundException e) {
//            voxelSprite.set(new ArrayModel(maker.warriorRandom()));
//            batchRenderer.set(batchRenderer.color().set(Dimmer.RinsedDimmer));
//        }
//    }

    @Override
    public void create() {
        startTime = TimeUtils.millis();
        font = new BitmapFont(Gdx.files.internal("PxPlus_IBM_VGA_8x16.fnt"));
        batch = new SpriteBatch();
        worldView = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        screenView = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        buffer = new FrameBuffer(Pixmap.Format.RGBA8888, VIRTUAL_WIDTH, VIRTUAL_HEIGHT, false, false);
        screenRegion = new TextureRegion();
        screenView.getCamera().position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);
        screenView.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.enableBlending();

        maker = new ModelMaker(-123456789, Colorizer.FlesurrectBonusColorizer);
        batchRenderer = new VoxelSpriteBatchRenderer(batch);
        batchRenderer.color().set(Colorizer.FlesurrectBonusColorizer);
        voxelSprite = new VoxelSprite()
                .set(batchRenderer)
                .setOffset(VIRTUAL_WIDTH / 2, 100);
        makeModel();
        Gdx.input.setInputProcessor(inputProcessor());

        defaultShader = SpriteBatch.createDefaultShader();
        shader = new ShaderProgram(vertexShader, fragmentShaderLighterOutline);
        if (!shader.isCompiled()) throw new GdxRuntimeException("Couldn't compile shader: " + shader.getLog());
    }

    public void makeModel() {
        voxelSprite.set(model());
    }

    public IModel model() {
        FetchModel fm = new FetchModel(100, 100, 60);
        container = new byte[100][100][50];
        Tools3D.translateCopyInto(maker.shipLargeNoiseColorized(), container, 30, 30, 10);
        AnimatedArrayModel fire = new AnimatedArrayModel(maker.animateExplosion(17, 70, 70, 60));
        BurstFetch burst = new BurstFetch(new ArrayModel(container), 50, 50, 4, 16, 3);
        fm.add(burst).add(new OffsetModel(-15, -15, -14).add(fire));
        byte[][][][] voxelFrames = new byte[16][][][];
        am = new AnimatedArrayModel(voxelFrames);
        for (int i = 0; i < 16; i++) {
            burst.setFrame(i);
            fire.setFrame(i+1);
            voxelFrames[i] = new ArrayModel(fm).voxels;
        }
        return am;
    }

    @Override
    public void render() {
        am.setFrame((int)(TimeUtils.timeSinceMillis(startTime) >>> 7 & 15));

        buffer.begin();
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        worldView.apply();
        worldView.getCamera().position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);
        worldView.update(VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        batch.setProjectionMatrix(worldView.getCamera().combined);
        batch.begin();

        font.draw(batch, StringKit.join(", ", voxelSprite.getModel().sizeX(), voxelSprite.getModel().sizeY(), voxelSprite.getModel().sizeZ()) + " (original)", 0, 80);
        font.draw(batch, voxelSprite.turnModel().sizeX() + ", " + voxelSprite.turnModel().sizeY() + ", " + voxelSprite.turnModel().sizeZ() + " (modified)", 0, 60);
        font.draw(batch, StringKit.join(", ", voxelSprite.turnModel().turner().rotation()) + " (rotation)", 0, 40);
        font.draw(batch, Gdx.graphics.getFramesPerSecond() + " FPS", 0, 20);

        voxelSprite.render();

        batch.setColor(-0x1.fffffep126f); // white as a packed float, resets any color changes that the renderer made
        batch.end();
        buffer.end();
        float bright = NumberTools.swayTight((TimeUtils.millis() & 0xFFFFFF) * 3E-4f) * 0.7f + 0.2f;
        Gdx.gl.glClearColor(bright, bright, bright, 1f);
//        ((backgroundColor >> 24) & 0xff) / 255f,
//                ((backgroundColor >> 16) & 0xff) / 255f,
//                ((backgroundColor >> 8) & 0xff) / 255f,
//                (backgroundColor & 0xff) / 255f
//        );
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        screenView.apply();
        batch.setProjectionMatrix(screenView.getCamera().combined);
        batch.begin();
        screenTexture = buffer.getColorBufferTexture();
        screenTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        screenRegion.setRegion(screenTexture);
        screenRegion.flip(false, true);

        batch.setShader(shader);
        shader.setUniformf("outlineH", 1f / VIRTUAL_HEIGHT);
        shader.setUniformf("outlineW", 1f / VIRTUAL_WIDTH);

        batch.draw(screenRegion, 0, 0);
        batch.setShader(defaultShader);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        screenView.update(width, height);
    }

    public InputProcessor inputProcessor() {
        return new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                switch (keycode) {
                    case Input.Keys.NUM_0:
                        voxelSprite.setAngle(1);
                        break;
                    case Input.Keys.MINUS:
                        voxelSprite.setAngle(2);
                        break;
                    case Input.Keys.EQUALS:
                        voxelSprite.setAngle(3);
                        break;
                    case Input.Keys.U:
                        voxelSprite.clockX();
                        break;
                    case Input.Keys.I:
                        voxelSprite.clockY();
                        break;
                    case Input.Keys.O:
                        voxelSprite.clockZ();
                        break;
                    case Input.Keys.J:
                        voxelSprite.counterX();
                        break;
                    case Input.Keys.K:
                        voxelSprite.counterY();
                        break;
                    case Input.Keys.L:
                        voxelSprite.counterZ();
                        break;
                    case Input.Keys.R:
                        voxelSprite.reset();
                        break;
                    case Input.Keys.P:
                        makeModel();
                        break;
                    case Input.Keys.G:
                        batchRenderer.color().set(batchRenderer.color().direction().counter());
                        break;
                    case Input.Keys.H:
                        batchRenderer.color().set(batchRenderer.color().direction().clock());
                        break;
                    case Input.Keys.T: // try again
                        voxelSprite.reset();
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
