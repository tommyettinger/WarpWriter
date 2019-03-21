import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class ShaderPalettizer extends ApplicationAdapter {
    //public static final int backgroundColor = Color.rgba8888(Color.DARK_GRAY);
    public static final int SCREEN_WIDTH = 1280;
    public static final int SCREEN_HEIGHT = 720;
    protected SpriteBatch batch;
    protected Viewport screenView;
    protected Texture screenTexture;
    protected BitmapFont font;
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
    public static final String fragmentShader = "#version 150\n" +
            "varying vec2 v_texCoords;\n" +
            "varying vec4 v_color;\n" +
            "uniform sampler2D u_texture;\n" +
            "uniform sampler2D u_palette;\n" +
            "void main()\n" +
            "{\n" +
            "   vec4 tgt = texture2D( u_texture, v_texCoords );\n" +
            "   vec4 used = texture2D(u_palette, vec2((tgt.b + floor(tgt.r * 31.99999)) * 0.03125, 1.0 - tgt.g));\n" +
            "   float adj = sin(dot(gl_FragCoord.xy, vec2(4.743036261279236, 3.580412143837574))) * 1.1 + 0.05;\n" +
            "   tgt.rgb = clamp(tgt.rgb + (used.rgb - tgt.rgb) * adj, 0.0, 1.0);\n" +
            "   gl_FragColor.rgb = v_color.rgb * texture2D(u_palette, vec2((tgt.b + floor(tgt.r * 31.99999)) * 0.03125, 1.0 - tgt.g)).rgb;\n" + //(tgt.b + floor(tgt.r * 32.0)) * 0.03125, tgt.g
            "   gl_FragColor.a = v_color.a * tgt.a;\n" +
            "}";
    private ShaderProgram defaultShader;
    private ShaderProgram shader;
    private Texture palette;

    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Palette Reducer");
        config.setWindowedMode(SCREEN_WIDTH, SCREEN_HEIGHT);
        config.setIdleFPS(10);
        config.useVsync(true);
        final ShaderPalettizer app = new ShaderPalettizer();
        config.setWindowListener(new Lwjgl3WindowAdapter() {
            @Override
            public void filesDropped(String[] files) {
                if (files != null && files.length > 0) {
                    if (files[0].endsWith(".png") || files[0].endsWith(".jpg") || files[0].endsWith(".jpeg"))
                        app.load(files[0]);
                }
            }
        });

        new Lwjgl3Application(app, config);
    }

    public void load(String name) {
        //// loads a file by its full path, which we get via drag+drop
        screenTexture = new Texture(Gdx.files.absolute(name));
    }

    @Override
    public void create() {
        palette = new Texture(Gdx.files.local("palettes/Flesurrect_GLSL.png"), Pixmap.Format.RGBA8888, false);
        font = new BitmapFont(Gdx.files.internal("PxPlus_IBM_VGA_8x16.fnt"));
        defaultShader = SpriteBatch.createDefaultShader();
        shader = new ShaderProgram(vertexShader, fragmentShader);
        if (!shader.isCompiled()) throw new GdxRuntimeException("Couldn't compile shader: " + shader.getLog());

        batch = new SpriteBatch(1000, defaultShader);
        screenView = new ScreenViewport();
        screenView.getCamera().position.set(SCREEN_WIDTH / 2, SCREEN_HEIGHT / 2, 0);
        screenView.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.enableBlending();
        Gdx.input.setInputProcessor(inputProcessor());

        // if you don't have these files on this absolute path, that's fine, and they will be ignored
        load("D:/Painting_by_Henri_Biva.jpg");
        load("D:/Sierra_Nevadas.jpg");
        load("D:/Mona_Lisa.jpg");
    }


    @Override
    public void render() {
        Gdx.gl.glClearColor(0.4f, 0.4f, 0.4f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(screenView.getCamera().combined);
        if(screenTexture != null) {
            batch.setShader(shader);
            batch.setColor(-0x1.fffffep126f);
            Gdx.gl.glActiveTexture(GL20.GL_TEXTURE1);
            palette.bind();
            batch.begin();
            shader.setUniformi("u_palette", 1);
            Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);

            batch.draw(screenTexture, 0, 0);
        }
        else {
            batch.begin();
            font.draw(batch, "Drag and drop an image file onto this window;", 20, 150);
            font.draw(batch, "a palette-reduced version will be shown here.", 20, 120);
        }
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
                    case Input.Keys.Q:
                    case Input.Keys.ESCAPE:
                        Gdx.app.exit();
                        break;
                    default:
                        if(batch.getShader().equals(defaultShader))
                            batch.setShader(shader);
                        else
                            batch.setShader(defaultShader);
                        break;
                }
                return true;
            }
        };
    }
}