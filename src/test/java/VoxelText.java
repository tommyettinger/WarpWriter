import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class VoxelText {
    public interface FillRule {
        boolean fill(int color);
    }

    public static FillRule FillRuleColor(final int testColor) {
        return new FillRule() {
            @Override
            public boolean fill(int color) {
                return color == testColor;
            }
        };
    }

    public static FillRule FillRuleTransparent() {
        return FillRuleTransparent(0.5f);
    }

    public static FillRule FillRuleTransparent(final float threshold) {
        return new FillRule() {
            @Override
            public boolean fill(int color) {
                return (color & 0xFF) / 255f > threshold;
            }
        };
    }

    public static FillRule FillRuleTransparent(final int threshold) {
        return new FillRule() {
            @Override
            public boolean fill(int color) {
                return (color & 0xFF) > threshold;
            }
        };
    }

    public static byte[][] PixmapToBytes(Pixmap pixmap) {
        return PixmapToBytes(pixmap, (byte) 255);
    }

    public static byte[][] PixmapToBytes(Pixmap pixmap, byte fill) {
        return PixmapToBytes(pixmap, fill, FillRuleTransparent());
    }

    public static byte[][] PixmapToBytes(Pixmap pixmap, byte fill, FillRule fillRule) {
        byte[][] result = new byte[pixmap.getWidth()][pixmap.getHeight()];
        for (int x = 0; x < pixmap.getWidth(); x++)
            for (int y = 0; y < pixmap.getHeight(); y++)
                if (fillRule.fill(pixmap.getPixel(x, y)))
                    result[x][y] = fill;
        return result;
    }

    protected FrameBuffer buffer;
    protected SpriteBatch batch;
    protected Viewport view;

    public Pixmap textPixmap(String string, BitmapFont font) {
        return textPixmap(string, font, Color.WHITE);
    }

    public Pixmap textPixmap(String string, BitmapFont font, Color color) {
        return textPixmap(string, font, color,
                (int) (font.getSpaceWidth() * Gdx.graphics.getDensity()) * string.length()
        );
    }

    public Pixmap textPixmap(String string, BitmapFont font, Color color, int width) {
        int height = (int) (font.getLineHeight() * Gdx.graphics.getDensity());
        if (batch == null) batch = new SpriteBatch();
        if (buffer == null || buffer.getWidth() != width || buffer.getHeight() != height) {
            if (buffer != null) buffer.dispose();
            buffer = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false, false);
        }
        if (view == null || view.getScreenWidth() != width || view.getScreenHeight() != height)
            view = new FitViewport(width, height);
        view.getCamera().position.set(width / 2, height / 2, 0);
        view.update(width, height);
        buffer.begin();
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.setProjectionMatrix(view.getCamera().combined);
        batch.begin();
        font.setColor(color);
        font.draw(batch, string, 0, 0);
        batch.end();
        Pixmap result = ScreenUtils.getFrameBufferPixmap(0, 0, width, height);
        buffer.end();
        return result;
    }
}
