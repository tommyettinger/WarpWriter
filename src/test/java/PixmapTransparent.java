import com.badlogic.gdx.graphics.Pixmap;
import warpwriter.model.ColorFetch;
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
    public Pixmap pixmap;
    public float threshold;
    public Fetch fill;

    public PixmapTransparent(Pixmap pixmap) {
        this(pixmap, 0.5f);
    }

    public PixmapTransparent(Pixmap pixmap, float threshold) {
        this(pixmap, threshold, ColorFetch.transparent);
    }

    public PixmapTransparent(Pixmap pixmap, Fetch fill) {
        this(pixmap, 0.5f, fill);
    }

    public PixmapTransparent(Pixmap pixmap, float threshold, Fetch fill) {
        this.pixmap = pixmap;
        this.threshold = threshold;
        this.fill = fill;
    }

    @Override
    public int xSize() {
        return 1;
    }

    @Override
    public int ySize() {
        return pixmap.getWidth();
    }

    @Override
    public int zSize() {
        return pixmap.getHeight();
    }

    /**
     * @return Ignores the third dimension!
     */
    @Override
    public boolean inside(int x, int y, int z) {
        return !outside(0, y, z);
    }

    /**
     * @return Ignores the third dimension!
     */
    @Override
    public boolean outside(int x, int y, int z) {
        return y < 0 || z < 0 || y >= ySize() || z >= zSize();
    }

    @Override
    public Fetch fetch(int x, int y, int z) {
        return outside(x, y, z) || (pixmap.getPixel(y, z) & 0xFF) / 255f < threshold ? getNextFetch() : fill;
    }
}
