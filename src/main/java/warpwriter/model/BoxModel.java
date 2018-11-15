package warpwriter.model;

/**
 * Draws a wireframe box!
 *
 * @author Ben McLean
 */
public class BoxModel extends FetchModel {
    public Fetch fetch;

    public BoxModel(int sizeX, int sizeY, int sizeZ, Fetch fetch) {
        super(sizeX, sizeY, sizeZ);
        this.fetch = fetch;
    }

    public BoxModel(Fetch fetch, int sizeX, int sizeY, int sizeZ) {
        this(sizeX, sizeY, sizeZ, fetch);
    }

    /**
     * @param model A model which gets added inside the box. Warning: Box size is based on initial model. Does not automatically update box size if model size changes!
     * @param fetch Color of the box
     */
    public BoxModel (IModel model, Fetch fetch) {
        this(fetch, model.sizeX(), model.sizeY(), model.sizeZ());
        add(new FetchModel(model));
    }

    /**
     * @see BoxModel(IModel, Fetch)
     */
    public BoxModel (Fetch fetch, IModel model) {
        this(model, fetch);
    }

    public BoxModel(byte[][][] convenience, Fetch fetch) {
        this(convenience.length, convenience[0].length, convenience[0][0].length, fetch);
    }

    public BoxModel(Fetch fetch, byte[][][] convenience) {
        this(convenience, fetch);
    }

    @Override
    public Fetch fetch() {
        int x = chainX(), y = chainY(), z = chainZ();
        return bool(x, y, z) ? fetch : getNextFetch();
    }

    /**
     * @return True if coordinates are part of the wireframe edges of the box.
     */
    @Override
    public boolean bool(int x, int y, int z) {
        return inside(x, y, z) && (((x == 0 || x == sizeX() - 1) && (y == 0 || y == sizeY() - 1)) || ((x == 0 || x == sizeX() - 1) && (z == 0 || z == sizeZ() - 1)) || ((y == 0 || y == sizeY() - 1) && (z == 0 || z == sizeZ() - 1)));
    }
}
