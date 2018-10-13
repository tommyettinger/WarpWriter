package warpwriter.model;

/** Convenience wrapper for FetchFactory
 *
 * @author Ben McLean
 */
public class ModelFactory extends FetchFactory implements IModel {
    public int xSize, ySize, zSize;

    /**
     * Dimensions are needed to make this a model.
     */
    private ModelFactory() {
    }

    public ModelFactory(int xSize, int ySize, int zSize) {
        this.xSize = xSize;
        this.ySize = ySize;
        this.zSize = zSize;
    }

    @Override
    public int xSize() {
        return xSize;
    }

    @Override
    public int ySize() {
        return ySize;
    }

    @Override
    public int zSize() {
        return zSize;
    }
}
