package warpwriter.model;

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
public class VoxelText extends Fetch implements IModel, Disposable {
    protected FrameBuffer buffer;
    protected SpriteBatch batch;
    protected Pixmap pixmap;
    protected int xSize = 1;
    protected Fetch fetch;

    public VoxelText() {
    }

    public VoxelText(BitmapFont font, String string, Fetch fetch) {
        setText(font, string);
        setFetch(fetch);
    }

    public VoxelText(BitmapFont font, String string, Fetch fetch, int xSize) {
        this(font, string, fetch);
        setDepth(xSize);
    }

    @Override
    public Fetch fetch() {
        return bool(xChain(), yChain(), zChain()) ? fetch : getNextFetch();
    }

    @Override
    public boolean bool (int x, int y, int z) {
        return pixmap != null
                && inside(x, y, z)
                && (pixmap.getPixel(y, z) & 0xFF) > 128;
    }

    /**
     * @param xSize Set to 0 for infinite depth in both directions, or to a positive depth value
     * @return this
     */
    public VoxelText setDepth(int xSize) {
        this.xSize = xSize;
        return this;
    }

    public VoxelText setText(BitmapFont font, String string) {
        if (pixmap != null) pixmap.dispose();
        pixmap = textToPixmap(font, string);
        return this;
    }

    /**
     * @param fetch What to fetch the text with
     * @return this
     */
    public VoxelText setFetch(Fetch fetch) {
        this.fetch = fetch;
        return this;
    }

    @Override
    public void dispose() {
        if (buffer != null) buffer.dispose();
        if (batch != null) batch.dispose();
        if (pixmap != null) pixmap.dispose();
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
        if (color != null) font.setColor(color);
        font.draw(batch, string, 0, height);
        batch.end();
        Pixmap result = ScreenUtils.getFrameBufferPixmap(0, 0, width, height);
        buffer.end();
        if (color != null) font.setColor(old);
        return result;
    }

    @Override
    public int xSize() {
        return xSize == 0 ? 1 : xSize;
    }

    @Override
    public int ySize() {
        return pixmap == null ? 1 : pixmap.getWidth();
    }

    @Override
    public int zSize() {
        return pixmap == null ? 1 : pixmap.getHeight();
    }

    @Override
    public boolean inside(int x, int y, int z) {
        return !outside(x, y, z);
    }

    /**
     * @return Ignores x if xSize == 0
     */
    @Override
    public boolean outside(int x, int y, int z) {
        return y < 0 || z < 0 || y >= ySize() || z >= zSize() || (xSize != 0 && (x < 0 || x >= xSize()));
    }
}
