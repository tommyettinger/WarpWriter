package warpwriter.model.fetch;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import warpwriter.model.Fetch;
import warpwriter.model.IModel;

/**
 * @author Ben McLean
 */
public class VoxelText extends Fetch implements IModel, Disposable {
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

    protected FrameBuffer buffer;
    protected SpriteBatch batch;
    protected Pixmap pixmap;
    protected int sizeX = 1;
    protected Fetch fill;
    protected Fetch outline;
    protected ShaderProgram shader;

    public VoxelText() {
    }

    public VoxelText(BitmapFont font, String string, Fetch fill) {
        setText(font, string);
        setFill(fill);
    }

    public VoxelText(BitmapFont font, String string, Fetch fill, Fetch outline) {
        this(font, string, fill);
        setOutline(outline);
    }

    public VoxelText(BitmapFont font, String string, Fetch fill, int sizeX) {
        this(font, string, fill);
        setDepth(sizeX);
    }

    public VoxelText(BitmapFont font, String string, Fetch fill, Fetch outline, int sizeX) {
        this(font, string, fill, outline);
        setDepth(sizeX);
    }

    @Override
    public Fetch fetch() {
        final int x = chainX(), y = chainY(), z = chainZ();
        if (pixmap == null || outside(x, y, z)) return getNextFetch();
        final int pixel = pixmap.getPixel(y, z);
        return (pixel & 0xFF) > 128 ? // If opacity is greater than half
                fill
                : outline != null // If there is an outline set
                // && (pixel >>> 24) < 128 // If red is less than half
                && ((pixmap.getPixel(y + 1, z) & 0xFF) > 128
                || (pixmap.getPixel(y - 1, z) & 0xFF) > 128
                || (pixmap.getPixel(y, z + 1) & 0xFF) > 128
                || (pixmap.getPixel(y, z - 1) & 0xFF) > 128)
                ? outline
                : getNextFetch();
    }

    @Override
    public boolean bool(int x, int y, int z) {
        return pixmap != null
                && inside(x, y, z)
                && (pixmap.getPixel(y, z) & 0xFF) > 128;
    }

    /**
     * @param xSize Set to 0 for infinite depth in both directions, or to a positive depth value
     * @return this
     */
    public VoxelText setDepth(int xSize) {
        this.sizeX = xSize;
        return this;
    }

    public VoxelText setText(BitmapFont font, String string) {
        if (pixmap != null) pixmap.dispose();
        pixmap = textOutlineToPixmap(font, string);
        return this;
    }

    /**
     * @param fill What to fill the text with
     * @return this
     */
    public VoxelText setFill(Fetch fill) {
        this.fill = fill;
        return this;
    }

    /**
     * @param outline The outline around the text
     * @return this
     */
    public VoxelText setOutline(Fetch outline) {
        this.outline = outline;
        return this;
    }

    @Override
    public void dispose() {
        if (buffer != null) buffer.dispose();
        if (batch != null) batch.dispose();
        if (pixmap != null) pixmap.dispose();
        if (shader != null) shader.dispose();
    }

    public Pixmap textToPixmap(BitmapFont font, String string) {
        return textToPixmap(font, string, Color.WHITE);
    }

    public Pixmap textToPixmap(BitmapFont font, String string, Color color) {
        GlyphLayout layout = new GlyphLayout();
        layout.setText(font, string);
        final int width = (int) layout.width,
                height = (int) (layout.height - font.getDescent() + 0.5f);
        if (batch == null) batch = new SpriteBatch();
        if (buffer == null || buffer.getWidth() != width || buffer.getHeight() != height) {
            if (buffer != null) buffer.dispose();
            buffer = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false, false);
        }
        FitViewport view = new FitViewport(width, height);
        view.apply(true);
        view.update(width, height);
        buffer.begin();
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.setProjectionMatrix(view.getCamera().combined);
        batch.begin();
        Color old = font.getColor();
        if (color != null) font.setColor(color);
        font.draw(batch, string, 0, height);
        batch.end();
        Pixmap result = ScreenUtils.getFrameBufferPixmap(0, 0, width, height);
        buffer.end();
        if (color != null) font.setColor(old);
        return result;
    }

    public Pixmap textOutlineToPixmap(BitmapFont font, String string) {
        GlyphLayout layout = new GlyphLayout();
        layout.setText(font, string);
        final int width = (int) layout.width + 2,
                height = (int) (layout.height - font.getDescent() + 2.5f);
        if (batch == null) batch = new SpriteBatch();
        if (buffer == null || buffer.getWidth() != width || buffer.getHeight() != height) {
            if (buffer != null) buffer.dispose();
            buffer = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false, false);
        }
        FitViewport view = new FitViewport(width, height);
        view.apply(true);
        view.update(width, height);
        buffer.begin();
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.setProjectionMatrix(view.getCamera().combined);
        batch.begin();
        font.draw(batch, string, 1, height - 1);
        batch.end();
        Pixmap result = ScreenUtils.getFrameBufferPixmap(0, 0, width, height);
        buffer.end();
        return result;
    }

    @Override
    public int sizeX() {
        return sizeX == 0 ? 1 : sizeX;
    }

    @Override
    public int sizeY() {
        return pixmap == null ? 1 : pixmap.getWidth();
    }

    @Override
    public int sizeZ() {
        return pixmap == null ? 1 : pixmap.getHeight();
    }

    @Override
    public boolean inside(int x, int y, int z) {
        return !outside(x, y, z);
    }

    /**
     * @return Ignores x if sizeX == 0
     */
    @Override
    public boolean outside(int x, int y, int z) {
        return y < 0 || z < 0 || y >= sizeY() || z >= sizeZ() || (sizeX != 0 && (x < 0 || x >= sizeX()));
    }
}
