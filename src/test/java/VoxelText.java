import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

/**
 * @author Ben McLean
 */
public class VoxelText implements Disposable {
    protected FrameBuffer buffer;
    protected SpriteBatch batch;

    @Override
    public void dispose() {
        if (buffer != null) buffer.dispose();
        if (batch != null) batch.dispose();
    }

    public Pixmap textToPixmap(BitmapFont font, String string) {
        return textToPixmap(font, string, Color.WHITE);
    }

    public Pixmap textToPixmap(BitmapFont font, String string, Color color) {
        GlyphLayout layout = new GlyphLayout();
        layout.setText(font, string);
        int width = (int) layout.width;
        int height = (int) (layout.height - font.getDescent() + 0.5f);
        if (batch == null) batch = new SpriteBatch();
        if (buffer == null || buffer.getWidth() != width || buffer.getHeight() != height) {
            if (buffer != null) buffer.dispose();
            buffer = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false, false);
        }
        FitViewport view = new FitViewport(width, height);
        view.apply(true);
//        view.getCamera().position.set(width / 2, height / 2, 0);
        view.update(width, height);
        buffer.begin();
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.setProjectionMatrix(view.getCamera().combined);
        batch.begin();
        Color old = font.getColor();
        font.setColor(color);
        font.draw(batch, string, 0, height);
        batch.end();
        Pixmap result = ScreenUtils.getFrameBufferPixmap(0, 0, width, height);
        buffer.end();
        font.setColor(old);
        return result;
    }

    public byte[][] text2D(BitmapFont font, String string, ByteFill.Fill2D fill) {
        return text2D(font, string, fill, ByteFill.fill2D((byte) 0));
    }

    public byte[][] text2D(BitmapFont font, String string, ByteFill.Fill2D yes, ByteFill.Fill2D no) {
        Pixmap pixmap = textToPixmap(font, string);
        byte[][] result = ByteFill.fill(
                ByteFill.transparent(pixmap, no, yes),
                pixmap.getWidth(),
                pixmap.getHeight()
        );
        pixmap.dispose();
        return result;
    }

    public byte[][][] text3D(BitmapFont font, String string, ByteFill.Fill3D fill) {
        return text3D(font, string, fill, 1);
    }

    public byte[][][] text3D(BitmapFont font, String string, ByteFill.Fill3D fill, int depth) {
        return text3D(font, string, fill, ByteFill.fill3D((byte) 0), depth);
    }

    public byte[][][] text3D(BitmapFont font, String string, ByteFill.Fill3D yes, ByteFill.Fill3D no) {
        return text3D(font, string, yes, no, 1);
    }

    public byte[][][] text3D(BitmapFont font, String string, ByteFill.Fill3D yes, ByteFill.Fill3D no, int depth) {
        Pixmap pixmap = textToPixmap(font, string);
        byte[][][] result = ByteFill.fill(
                ByteFill.transparent(pixmap, no, yes),
                depth,
                pixmap.getWidth(),
                pixmap.getHeight()
        );
        pixmap.dispose();
        return result;
    }
}
