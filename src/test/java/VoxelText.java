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
        buffer.dispose();
        batch.dispose();
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
        font.setColor(color);
        font.draw(batch, string, 0, height);
        batch.end();
        Pixmap result = ScreenUtils.getFrameBufferPixmap(0, 0, width, height);
        buffer.end();
        return result;
    }

    public byte[][] text2D(BitmapFont font, String string, ByteFill.Fill2D fill) {
        Pixmap pixmap = textToPixmap(font, string);
        return ByteFill.fill2D(
                ByteFill.transparent2D(
                        pixmap,
                        ByteFill.Fill2D((byte) 0),
                        fill
                ),
                pixmap.getWidth(),
                pixmap.getHeight()
        );
    }

    public byte[][][] textToVoxels(BitmapFont font, String string, ByteFill.Fill2D fill) {
        return textToVoxels(font, string, fill, 1);
    }

    public byte[][][] textToVoxels(BitmapFont font, String string, ByteFill.Fill2D fill, int depth) {
        return ByteFill.voxels2D(text2D(font, string, fill), depth);
    }
}
