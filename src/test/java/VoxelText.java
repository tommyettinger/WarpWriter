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
import java.util.Arrays;

public class VoxelText implements Disposable {
    protected FrameBuffer buffer;
    protected SpriteBatch batch;

    @Override
    public void dispose() {
        buffer.dispose();
        batch.dispose();
    }

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

    public static byte[][] pixmapToBytes(Pixmap pixmap) {
        return pixmapToBytes(pixmap, (byte) 255);
    }

    public static byte[][] pixmapToBytes(Pixmap pixmap, byte fill) {
        return pixmapToBytes(pixmap, fill, FillRuleTransparent());
    }

    public static byte[][] pixmapToBytes(Pixmap pixmap, byte fill, FillRule fillRule) {
        byte[][] result = new byte[pixmap.getWidth()][pixmap.getHeight()];
        for (int x = 0; x < pixmap.getWidth(); x++)
            for (int y = 0; y < pixmap.getHeight(); y++)
                if (fillRule.fill(pixmap.getPixel(x, y)))
                    result[x][y] = fill;
        return result;
    }

    public static byte[][][] voxels2D(byte[][] bytes) {
        return voxels2D(bytes, 1);
    }

    public static byte[][][] voxels2D(byte[][] bytes, int depth) {
        byte[][][] voxels = new byte[depth][bytes.length][bytes[0].length];
        Arrays.fill(voxels, bytes);
        return voxels;
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

    public byte[][][] textToVoxels(BitmapFont font, String string, byte color) {
        return textToVoxels(font, string, color, 1);
    }

    public byte[][][] textToVoxels(BitmapFont font, String string, byte color, int depth) {
        return voxels2D(
                pixmapToBytes(
                        textToPixmap(font, string),
                        color
                ),
                depth
        );
    }
}
