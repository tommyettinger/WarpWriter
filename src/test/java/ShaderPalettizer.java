import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowAdapter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import static warpwriter.view.render.ShaderUtils.*;

public class ShaderPalettizer extends ApplicationAdapter {
    //public static final int backgroundColor = Color.rgba8888(Color.DARK_GRAY);
    public static final int SCREEN_WIDTH = 1280;
    public static final int SCREEN_HEIGHT = 720;
    protected SpriteBatch batch;
    protected Viewport screenView;
    protected Texture screenTexture;
    protected BitmapFont font;
    
    protected long startTime;
    private ShaderProgram defaultShader;
    private ShaderProgram shader;
    private ShaderProgram shaderNoDither;
    private Texture palette;

    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Palette Reducer");
        config.setWindowedMode(SCREEN_WIDTH, SCREEN_HEIGHT);
        config.setIdleFPS(10);
        config.useVsync(true);
        config.setResizable(false);
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
        FileHandle file = Gdx.files.absolute(name);
        if(!file.exists())
            return;
        screenTexture = new Texture(file);
        screenTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    }

    @Override
    public void create() {
        startTime = TimeUtils.millis();
        palette = new Texture(Gdx.files.local("palettes/DB_Aurora_GLSL.png"), Pixmap.Format.RGBA8888, false);
        palette.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        font = new BitmapFont(Gdx.files.internal("PxPlus_IBM_VGA_8x16.fnt"));
        defaultShader = SpriteBatch.createDefaultShader();
        shader = new ShaderProgram(vertexShader, fragmentShaderWarmMildLimited);
        if (!shader.isCompiled()) throw new GdxRuntimeException("Couldn't compile shader: " + shader.getLog());
        shaderNoDither = new ShaderProgram(vertexShader, fragmentShaderWarmMildSoft);
        if (!shaderNoDither.isCompiled()) throw new GdxRuntimeException("Couldn't compile shader: " + shaderNoDither.getLog());
        batch = new SpriteBatch(1000, defaultShader);
        screenView = new ScreenViewport();
        screenView.getCamera().position.set(SCREEN_WIDTH / 2, SCREEN_HEIGHT / 2, 0);
        screenView.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.enableBlending();
        Gdx.input.setInputProcessor(inputProcessor());

        // if you don't have these files on this absolute path, that's fine, and they will be ignored
//        load("D:/Painting_by_Henri_Biva.jpg");
        load("D:/Among_the_Sierra_Nevada_by_Albert Bierstadt.jpg");
//        load("D:/Mona_Lisa.jpg");
    }


    @Override
    public void render() {
        Gdx.gl.glClearColor(0.4f, 0.4f, 0.4f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(screenView.getCamera().combined);
        if(screenTexture != null) {
            if(!batch.getShader().equals(defaultShader)) {
                batch.setPackedColor(-0x1.fffffep126f);
                Gdx.gl.glActiveTexture(GL20.GL_TEXTURE1);
                palette.bind();
                batch.begin();
                batch.getShader().setUniformi("u_palette", 1);
                //if(!batch.getShader().equals(defaultShader)) 
                //{
//                    shader.setUniformf("u_mul", 0.9f, 0.7f, 0.75f);
//                    shader.setUniformf("u_add", 0.05f, 0.14f, 0.16f);
                    shader.setUniformf("u_mul", 1f, 1f, 1f);
                    shader.setUniformf("u_add", 0f, 0f, 0f);
//                    shader.setUniformf("u_mul", 1f, 0.8f, 0.85f);
//                    shader.setUniformf("u_add", 0.1f, 0.95f, NumberTools.swayRandomized(12345, TimeUtils.timeSinceMillis(startTime) * 0x1p-9f) * 0.4f + 0.2f);
                //}
//                else batch.getShader().setUniformi("u_palette", 1);

                Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
            }
            else
                batch.begin();
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
                    case Input.Keys.NUM_1:
                    case Input.Keys.NUMPAD_1:
                        palette = new Texture(Gdx.files.local("palettes/Quorum64_GLSL.png"), Pixmap.Format.RGBA8888, false);
                        break;
                    case Input.Keys.NUM_2:
                    case Input.Keys.NUMPAD_2:
                        palette = new Texture(Gdx.files.local("palettes/Quorum128_GLSL.png"), Pixmap.Format.RGBA8888, false);
                        break;
                    case Input.Keys.NUM_3:
                    case Input.Keys.NUMPAD_3:
                        palette = new Texture(Gdx.files.local("palettes/Quorum256_GLSL.png"), Pixmap.Format.RGBA8888, false);
                        break;
                    case Input.Keys.NUM_4:
                    case Input.Keys.NUMPAD_4:
                        palette = new Texture(Gdx.files.local("palettes/DB_Aurora_GLSL.png"), Pixmap.Format.RGBA8888, false);
                        break;
                    case Input.Keys.NUM_5:
                    case Input.Keys.NUMPAD_5:
                        palette = new Texture(Gdx.files.local("palettes/Flesurrect_GLSL.png"), Pixmap.Format.RGBA8888, false);
                        break;
                    case Input.Keys.NUM_6:
                    case Input.Keys.NUMPAD_6:
                        palette = new Texture(Gdx.files.local("palettes/FlesurrectBonus_GLSL.png"), Pixmap.Format.RGBA8888, false);
                        break;
                    case Input.Keys.NUM_7:
                    case Input.Keys.NUMPAD_7:
                        palette = new Texture(Gdx.files.local("palettes/JudgeBonus_GLSL.png"), Pixmap.Format.RGBA8888, false);
                        break;
                    case Input.Keys.NUM_8:
                    case Input.Keys.NUMPAD_8:
                        palette = new Texture(Gdx.files.local("palettes/Mash256_GLSL.png"), Pixmap.Format.RGBA8888, false);
                        break;
                    case Input.Keys.NUM_9:
                    case Input.Keys.NUMPAD_9:
                        palette = new Texture(Gdx.files.local("palettes/Uniform216_GLSL.png"), Pixmap.Format.RGBA8888, false);
                        break;
                    case Input.Keys.NUM_0:
                    case Input.Keys.NUMPAD_0:
                        palette = new Texture(Gdx.files.local("palettes/RippleBonus_GLSL.png"), Pixmap.Format.RGBA8888, false);
                        break;
                    case Input.Keys.M: // Mona Lisa
                        load("D:/Mona_Lisa.jpg");
                        break;
                    case Input.Keys.S: //Sierra Nevada
                        load("D:/Among_the_Sierra_Nevada_by_Albert Bierstadt.jpg");
                        break;
                    case Input.Keys.B: // Biva
                        load("D:/Painting_by_Henri_Biva.jpg");
                        break;
                    case Input.Keys.C: // Color Guard
                        load("D:/Color_Guard.png");
                        break;
                    case Input.Keys.P: // lower-color palette
                        load("D:/Quorum64_GLSL.png");
                        break;
                    case Input.Keys.O: // higher-color palette
                        load("D:/Quorum128_GLSL.png");
                        break;
                    case Input.Keys.D: // dither/disable
                        if(!batch.getShader().equals(shaderNoDither))
                        {
                            batch.setShader(shaderNoDither);
                            Gdx.graphics.setTitle("Softness ON");
                        }
                        else
                        {
                            batch.setShader(shader);
                            Gdx.graphics.setTitle("Softness OFF");
                        }
                        break;
                    default:
                        if(!batch.getShader().equals(shader))
                        {
                            batch.setShader(shader);
                            Gdx.graphics.setTitle("Softness OFF");
                        }
                        else
                        {
                            batch.setShader(defaultShader);
                            Gdx.graphics.setTitle("Default Shader");
                        }
                        break;
                }
                palette.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                return true;
            }
        };
    }
}
