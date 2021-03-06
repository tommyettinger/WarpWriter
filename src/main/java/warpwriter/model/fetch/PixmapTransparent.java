package warpwriter.model.fetch;

import com.badlogic.gdx.graphics.Pixmap;
import warpwriter.model.Fetch;
import warpwriter.model.IModel;

/**
 * This fetch discriminates based on the transparency level of a Pixmap.
 * <p>
 * WARNING: Pixmaps are Disposable, but this class does NOT implement Disposable! The code calling this class is still responsible for disposal of the Pixmap.
 *
 * @author Ben McLean
 */
public class PixmapTransparent extends Fetch implements IModel {
    protected Pixmap pixmap;
    protected float threshold;
    protected Fetch fetch;

    /**
     * The pixmap doesn't have to be set right away.
     */
    public PixmapTransparent() {
        this(null);
    }

    public PixmapTransparent(float threshold) {
        this(null, threshold);
    }

    public PixmapTransparent(Pixmap pixmap) {
        this(pixmap, 0.5f);
    }

    public PixmapTransparent(Pixmap pixmap, float threshold) {
        this(pixmap, threshold, ColorFetch.transparent);
    }

    public PixmapTransparent(Pixmap pixmap, Fetch fetch) {
        this(pixmap, 0.5f, fetch);
    }

    public PixmapTransparent(Pixmap pixmap, float threshold, Fetch fetch) {
        set(pixmap).set(threshold).set(fetch);
    }

    public PixmapTransparent set(Pixmap pixmap) {
        this.pixmap = pixmap;
        return this;
    }

    public PixmapTransparent set(float threshold) {
        this.threshold = threshold;
        return this;
    }

    public PixmapTransparent set(Fetch fetch) {
        this.fetch = fetch;
        return this;
    }

    public Pixmap getPixmap() {
        return pixmap;
    }

    public float getThreshold() {
        return threshold;
    }

    public Fetch getFetch() {
        return fetch;
    }

    public PixmapTransparent setPixmap(Pixmap pixmap) {
        this.pixmap = pixmap;
        return this;
    }

    @Override
    public int sizeX() {
        return 1;
    }

    @Override
    public int sizeY() {
        return pixmap == null ? 1 : pixmap.getWidth();
    }

    @Override
    public int sizeZ() {
        return pixmap == null ? 1 : pixmap.getHeight();
    }

    /**
     * @return Ignores the third dimension! (x, which is depth, is not checked.)
     */
    @Override
    public boolean inside(int x, int y, int z) {
        return !outside(x, y, z);
    }

    /**
     * @return Ignores the third dimension!
     */
    @Override
    public boolean outside(int x, int y, int z) {
        return y < 0 || z < 0 || y >= sizeY() || z >= sizeZ();
    }

    @Override
    public byte at(int x, int y, int z) {
        return bool(x, y, z) ? fetch.at(x, y, z) : safeNextFetch().at(x, y, z);
    }

    @Override
    public boolean bool (int x, int y, int z) {
        return pixmap != null
                && inside(x, y, z)
                && (pixmap.getPixel(y, z) & 0xFF) < threshold * 255f;
    }
}
