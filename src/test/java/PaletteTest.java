import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
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
import warpwriter.Coloring;

public class PaletteTest extends ApplicationAdapter {
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

    public static final int backgroundColor = Color.rgba8888(Color.DARK_GRAY);
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
    private Texture one;
    protected ShaderProgram shader;
    protected ShaderProgram defaultShader;

    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Palette Test");
        config.setWindowedMode(SCREEN_WIDTH, SCREEN_HEIGHT);
        config.setIdleFPS(10);
        config.useVsync(false);
        new Lwjgl3Application(new PaletteTest(), config);
    }

    @Override
    public void create() {
        //creating "one" texture.
        Pixmap pixmap1 = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap1.drawPixel(0, 0, -1);
        one = new Texture(pixmap1);
        one.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        pixmap1.dispose();

        font = new BitmapFont(Gdx.files.internal("PxPlus_IBM_VGA_8x16.fnt"));
        batch = new SpriteBatch();
        worldView = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        screenView = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        buffer = new FrameBuffer(Pixmap.Format.RGBA8888, VIRTUAL_WIDTH, VIRTUAL_HEIGHT, false, false);
        screenRegion = new TextureRegion();
        screenView.getCamera().position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);
        screenView.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.enableBlending();
        Gdx.input.setInputProcessor(inputProcessor());

        defaultShader = SpriteBatch.createDefaultShader();
        shader = new ShaderProgram(vertexShader, fragmentShader);
        if (!shader.isCompiled()) throw new GdxRuntimeException("Couldn't compile shader: " + shader.getLog());
    }

    @Override
    public void render() {
        // Drawing colors!
        buffer.begin();
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        worldView.apply();
        worldView.getCamera().position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);
        worldView.update(VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        batch.setProjectionMatrix(worldView.getCamera().combined);
        batch.begin();
        batch.setShader(defaultShader);
        for (int x = 0; x < 8; x++)
            for (int y = 0; y < 32; y++) {
                batch.setColor(color((byte) (x * 32 + y)));
                batch.draw(one, x * (VIRTUAL_WIDTH / 8), VIRTUAL_HEIGHT - y * 22, VIRTUAL_WIDTH / 8, 22);
            }
        batch.end();
        buffer.end();
        Gdx.gl.glClearColor(
                ((backgroundColor >> 24) & 0xff) / 255f,
                ((backgroundColor >> 16) & 0xff) / 255f,
                ((backgroundColor >> 8) & 0xff) / 255f,
                (backgroundColor & 0xff) / 255f
        );
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        screenView.apply();
        batch.setProjectionMatrix(screenView.getCamera().combined);
        batch.begin();
        screenTexture = buffer.getColorBufferTexture();
        screenTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        screenRegion.setRegion(screenTexture);
        screenRegion.flip(false, true);
        batch.draw(screenRegion, 0, 0);
        batch.end();

        // Drawing text overlay!
        buffer.begin();
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        worldView.apply();
        worldView.getCamera().position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);
        worldView.update(VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        batch.setProjectionMatrix(worldView.getCamera().combined);
        batch.begin();
        batch.setShader(shader);
        shader.setUniformf("outlineH", 1f / VIRTUAL_HEIGHT);
        shader.setUniformf("outlineW", 1f / VIRTUAL_WIDTH);
        batch.setColor(-0x1.fffffep126f); // white as a packed float, resets any color changes that the renderer made
        for (int x = 0; x < 8; x++)
            for (int y = 0; y < 32; y++)
                font.draw(batch, Integer.toString(x * 32 + y), x * (VIRTUAL_WIDTH / 8) + 2, VIRTUAL_HEIGHT - y * 22 - 6);
        font.draw(batch, Gdx.graphics.getFramesPerSecond() + " FPS", 0, 15);
        batch.end();
        buffer.end();
        screenView.apply();
        batch.setProjectionMatrix(screenView.getCamera().combined);
        batch.begin();
        screenTexture = buffer.getColorBufferTexture();
        screenTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        screenRegion.setRegion(screenTexture);
        screenRegion.flip(false, true);
        batch.draw(screenRegion, 0, 0);
        batch.end();
    }

    public int color(byte voxel) {
        return Coloring.AURORA[voxel & 255];
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
                    case Input.Keys.ESCAPE:
                        Gdx.app.exit();
                        break;
                }
                return true;
            }
        };
    }
}
