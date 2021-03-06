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
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import squidpony.Mnemonic;
import squidpony.StringKit;
import warpwriter.ModelMaker;
import warpwriter.VoxIO;
import warpwriter.model.AnimatedVoxelSeq;
import warpwriter.model.FetchModel;
import warpwriter.model.VoxelSeq;
import warpwriter.model.color.Colorizer;
import warpwriter.model.decide.DecideFetch;
import warpwriter.model.decide.OctantDecide;
import warpwriter.model.fetch.ArrayModel;
import warpwriter.model.fetch.ColorFetch;
import warpwriter.model.fetch.CursorModel;
import warpwriter.view.VoxelSprite;
import warpwriter.view.render.MutantBatch;
import warpwriter.view.render.VoxelSpriteBatchRenderer;

public class ConnectionPointUtility extends ApplicationAdapter {
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
    public static final String fragmentShader = "#ifdef GL_ES\n" +
            "#define LOWP lowp\n" +
            "precision mediump int;\n" +
            "#else\n" +
            "#define LOWP\n" +
            "#endif\n" +
            "varying vec2 v_texCoords;\n" +
            "varying vec4 v_color;\n" +
            "uniform float outlineH;\n" +
            "uniform float outlineW;\n" +
            "uniform sampler2D u_texture;\n" +
            "void main()\n" +
            "{\n" +
            "   vec2 offsetx = vec2(outlineW, 0.0);\n" +
            "   vec2 offsety = vec2(0.0, outlineH);\n" +
            "   float alpha = texture2D( u_texture, v_texCoords ).a;\n" +
            "   alpha = max(alpha, texture2D( u_texture, v_texCoords + offsetx).a);\n" +
            "   alpha = max(alpha, texture2D( u_texture, v_texCoords - offsetx).a);\n" +
            "   alpha = max(alpha, texture2D( u_texture, v_texCoords + offsety).a);\n" +
            "   alpha = max(alpha, texture2D( u_texture, v_texCoords - offsety).a);\n" +
            "   gl_FragColor = v_color * texture2D( u_texture, v_texCoords );\n" +
            "   gl_FragColor.a = alpha;\n" +
            "}";

    //public static final int backgroundColor = Color.rgba8888(Color.DARK_GRAY);
    public static final int SCREEN_WIDTH = 1280;
    public static final int SCREEN_HEIGHT = 720;
    public static final int VIRTUAL_WIDTH = 1280;
    public static final int VIRTUAL_HEIGHT = 720;
    protected MutantBatch batch;
    protected Viewport worldView;
    protected Viewport screenView;
    protected BitmapFont font;
    protected FrameBuffer buffer;
    protected Texture screenTexture;
    protected TextureRegion screenRegion;
    protected ModelMaker maker;
    protected VoxelSprite voxelSprite;
    protected boolean box = false;
    protected VoxelSpriteBatchRenderer renderer;
    protected ShaderProgram shader;
    protected ShaderProgram defaultShader;
    protected Colorizer colorizer = Colorizer.AuroraColorizer;
    protected CursorModel cursorModel = new CursorModel();
    protected OctantDecide octantDecide = new OctantDecide();
    protected FetchModel frontModel;
    protected VoxelSeq[] seqs;
    protected String name = "Glorious Axe and Dark Frost";
    protected Mnemonic naming;

    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("WarpWriter Connection Point Utility");
        config.setWindowedMode(SCREEN_WIDTH, SCREEN_HEIGHT);
        config.setIdleFPS(10);
        config.useVsync(false);
        config.setResizable(false);
        final ConnectionPointUtility app = new ConnectionPointUtility();
        new Lwjgl3Application(app, config);
    }

    @Override
    public void create() {
        font = new BitmapFont(Gdx.files.internal("PxPlus_IBM_VGA_8x16.fnt"));
        batch = new MutantBatch();
        worldView = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        screenView = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        buffer = new FrameBuffer(Pixmap.Format.RGBA8888, VIRTUAL_WIDTH, VIRTUAL_HEIGHT, false, false);
        screenRegion = new TextureRegion();
        screenView.getCamera().position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);
        screenView.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.enableBlending();
        colorizer = Colorizer.AuroraColorizer;
        naming = new Mnemonic(54321);
        maker = new ModelMaker(987654321123456789L, colorizer);
        renderer = new VoxelSpriteBatchRenderer(batch);
        renderer.color().colorizer(colorizer);
        voxelSprite = new VoxelSprite()
                .set(renderer)
                .setOffset(VIRTUAL_WIDTH / 2, 100)
                .setZ45(true)
                .setAngle(3);
        // 0 is the normal model, 1 is priority non-connection voxels, each later seq is one connection point
        seqs = new VoxelSeq[]{new VoxelSeq(256), new VoxelSeq(64), new VoxelSeq(16), new VoxelSeq(16)};
        makeModel();
        Gdx.input.setInputProcessor(inputProcessor());

        defaultShader = SpriteBatch.createDefaultShader();
        shader = new ShaderProgram(vertexShader, fragmentShader);
        if (!shader.isCompiled()) throw new GdxRuntimeException("Couldn't compile shader: " + shader.getLog());
    }

    public void makeModel() {
        FetchModel model = model();
        seqs[0].clear();
        seqs[0].putModel(model);
        seqs[1].clear();
        seqs[2].clear();
        cursorModel.setColor((byte) 40)
                .setSize(model.sizeX(), model.sizeY(), model.sizeZ())
                .add(new DecideFetch().setDecide(octantDecide).setFetch(ColorFetch.transparent).add(model));

        cursorModel.set(model.sizeX() / 2, model.sizeY() / 2, model.sizeZ() / 2);

        frontModel = new FetchModel(new DecideFetch().setDecide(octantDecide).setFetch(model), model);

        voxelSprite.set(frontModel);
        setOctantDecide();
    }

    public ConnectionPointUtility setOctantDecide() {
        octantDecide.set(voxelSprite.turnModel().rotation())
                .set(
                        cursorModel.x() - voxelSprite.turnModel().rotation().octantStepX(),
                        cursorModel.y() - voxelSprite.turnModel().rotation().octantStepY(),
                        cursorModel.z() - voxelSprite.turnModel().rotation().octantStepZ()
                );
        return this;
    }

    public FetchModel model() {
        //return new FetchModel(ColorFetch.color((byte) 9), 48, 48, 16);
        name = "The " + naming.toWordMnemonic(maker.rng.stateA, false) + " and the " + naming.toWordMnemonic(maker.rng.stateB, false) + ".vox";

        return new FetchModel(new ArrayModel(maker.shipLargeSmoothColorized()));
//        HashMap3D<IFetch> map = new HashMap3D<>();
//        for (int x=0; x<3; x++) {
//            for (int y = 0; y < 3; y++)
////                for (int z=0; z<3; z++)
////                    map.put(x, y, 0, ColorFetch.color(maker.randomMainColor()));
//            {
//                byte midColor = colorizer.getReducer().randomColorIndex(maker.rng);
//                map.put(x, y, 0, new DecideFetch()
//                        .setDecide(TileFetch.Diagonal16x16x16)
//                        .setFetch(new NoiseFetch(colorizer.darken(midColor), midColor, midColor, colorizer.brighten(midColor))
//                ));
//            }
//        }
//        return //new ArrayModel(
//                new WorldFetch()
//                .set(map)
////                , 48, 48, 16);
//                .model(48, 48, 16);
////        return new DecideFetch(
////                TileFetch.Diagonal16x16x16,
////                new NoiseFetch(Colorizer.AuroraBonusColorizer.darken(midColor), midColor, midColor, Colorizer.AuroraBonusColorizer.brighten(midColor))
////        ).model(16, 16, 16);
    }

    @Override
    public void render() {
        cursorModel.addDelta(Gdx.graphics.getDeltaTime());

        buffer.begin();
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        worldView.apply();
        worldView.getCamera().position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);
        worldView.update(VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        batch.setProjectionMatrix(worldView.getCamera().combined);
        batch.begin();

        font.draw(batch, StringKit.join(", ", cursorModel.x(), cursorModel.y(), cursorModel.z()) + " (roving voxel)", 0, 100);
        font.draw(batch, StringKit.join(", ", octantDecide.x(), octantDecide.y(), octantDecide.z()) + " (octantDecide)", 0, 80);
        font.draw(batch, "Delta: " + cursorModel.delta() + ", Frame: " + cursorModel.frame(), 0, 60);
//        font.draw(batch, StringKit.join(", ", voxelSprite.turnModel().rotation().octantStepX(), voxelSprite.turnModel().rotation().octantStepY(), voxelSprite.turnModel().rotation().octantStepZ()) + " (octant steps)", 0, 60);
        font.draw(batch, "Rotation: " + voxelSprite.turnModel().rotation() + ", z45: " + voxelSprite.getZ45(), 0, 40);
        font.draw(batch, Gdx.graphics.getFramesPerSecond() + " FPS", 0, 20);

        voxelSprite.set(cursorModel);
        renderer.setTransparency(Byte.MAX_VALUE);
        voxelSprite.render();
        voxelSprite.set(frontModel);
        renderer.setTransparency((byte) 64);
        voxelSprite.render();

        batch.setPackedColor(-0x1.fffffep126f); // white as a packed float, resets any color changes that the renderer made
        batch.end();
        buffer.end();
        float bright = 0.25f; // NumberTools.swayTight((TimeUtils.millis() & 0xFFFFFF) * 3E-4f) * 0.7f + 0.2f;
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
                        setOctantDecide();
                        break;
                    case Input.Keys.I:
                        voxelSprite.clockY();
                        setOctantDecide();
                        break;
                    case Input.Keys.O:
                        voxelSprite.clockZ();
                        setOctantDecide();
                        break;
                    case Input.Keys.J:
                        voxelSprite.counterX();
                        setOctantDecide();
                        break;
                    case Input.Keys.K:
                        voxelSprite.counterY();
                        setOctantDecide();
                        break;
                    case Input.Keys.L:
                        voxelSprite.counterZ();
                        setOctantDecide();
                        break;
                    case Input.Keys.R:
                        voxelSprite.reset();
                        setOctantDecide();
                        break;
                    case Input.Keys.P:
                        makeModel();
                        break;
                    case Input.Keys.B:
                        box = !box;
                        makeModel();
                        break;
                    case Input.Keys.G:
                        renderer.color().direction(renderer.color().direction().counter());
                        break;
                    case Input.Keys.H:
                        renderer.color().direction(renderer.color().direction().clock());
                        break;
                    case Input.Keys.W:
                        cursorModel.addX(1);
                        setOctantDecide();
                        break;
                    case Input.Keys.S:
                        cursorModel.addX(-1);
                        setOctantDecide();
                        break;
                    case Input.Keys.A:
                        cursorModel.addY(-1);
                        setOctantDecide();
                        break;
                    case Input.Keys.D:
                        cursorModel.addY(1);
                        setOctantDecide();
                        break;
                    case Input.Keys.SPACE:
                        cursorModel.addZ(1);
                        setOctantDecide();
                        break;
                    case Input.Keys.C:
                        cursorModel.addZ(-1);
                        setOctantDecide();
                        break;
                    case Input.Keys.ENTER:
                        seqs[1].put(cursorModel.x(), cursorModel.y(), cursorModel.z(), seqs[0].get(cursorModel.x(), cursorModel.y(), cursorModel.z()));
                        break;
                    case Input.Keys.SLASH:
                        VoxIO.writeAnimatedVOX(name, new AnimatedVoxelSeq(seqs), colorizer.getReducer().paletteArray);
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
