package warpwriter.model;

import warpwriter.model.decide.IDecideModel;
import warpwriter.model.fetch.FetchFetch;
import warpwriter.model.nonvoxel.CompassDirection;

/**
 * Converts a Fetch to a Model with sizes stored in ints.
 *
 * @author Ben McLean
 */
public class FetchModel extends Fetch implements IModel, IDecideModel {
    protected int sizeX, sizeY, sizeZ;

    public FetchModel() {
        this(12, 12, 8);
    }

    public FetchModel(int sizeX, int sizeY, int sizeZ) {
        this(sizeX, sizeY, sizeZ, null);
    }

    public FetchModel(Fetch fetch, int sizeX, int sizeY, int sizeZ) {
        this(sizeX, sizeY, sizeZ, fetch);
    }

    public FetchModel(int sizeX, int sizeY, int sizeZ, Fetch fetch) {
        setSize(sizeX, sizeY, sizeZ);
        if (fetch != null) add(fetch);
    }

    /**
     * Converts an IModel which doesn't extend Fetch into one that does.
     * @param model Warning: FetchModel size is based on initial model. Does not automatically update FetchModel size if model size changes!
     */
    public FetchModel (IModel model) {
        setSize(model.sizeX(), model.sizeY(), model.sizeZ());
        add(new FetchFetch(model));
    }

    public FetchModel(byte[][][] convenience) {
        this(convenience, null);
    }

    public FetchModel(byte[][][] convenience, Fetch fetch) {
        this(convenience.length, convenience[0].length, convenience[0][0].length, fetch);
    }

    public FetchModel(Fetch fetch, byte[][][] convenience) {
        this(convenience, fetch);
    }

    /**
     * Copies the size of {@code convenience}, but does not copy its contents; this uses {@code fetch} for contents.
     *
     * @param convenience An IModel with a size to copy
     * @param fetch       Actual fetch to use
     */
    public FetchModel(IModel convenience, Fetch fetch) {
        this(convenience.sizeX(), convenience.sizeY(), convenience.sizeZ(), fetch);
    }

    public FetchModel(Fetch fetch, IModel convenience) {
        this(convenience, fetch);
    }

    @Override
    public int sizeX() {
        return sizeX;
    }

    @Override
    public int sizeY() {
        return sizeY;
    }

    @Override
    public int sizeZ() {
        return sizeZ;
    }

    public FetchModel addSize(CompassDirection direction) {
        return addSizeX(direction.deltaX).addSizeY(direction.deltaY);
    }

    @Override
    public boolean outside(int x, int y, int z) {
        return x < 0 || y < 0 || z < 0 || x >= sizeX() || y >= sizeY() || z >= sizeZ();
    }

    @Override
    public boolean inside(int x, int y, int z) {
        return !outside(x, y, z);
    }

    public FetchModel addSize(int x, int y, int z) {
        return addSizeX(x).addSizeY(y).addSizeZ(z);
    }

    public FetchModel addSizeX(int x) {
        sizeX += x;
        return this;
    }

    public FetchModel addSizeY(int y) {
        sizeY += y;
        return this;
    }

    public FetchModel addSizeZ(int z) {
        sizeZ += z;
        return this;
    }

    public FetchModel setSize(int x, int y, int z) {
        return setSizeX(x).setSizeY(y).setSizeZ(z);
    }

    public FetchModel setSizeX(int x) {
        sizeX = x;
        return this;
    }

    public FetchModel setSizeY(int y) {
        sizeY = y;
        return this;
    }

    public FetchModel setSizeZ(int z) {
        sizeZ = z;
        return this;
    }

    /**
     * @return inside(x, y, z)
     */
    @Override
    public boolean bool(int x, int y, int z) {
        return inside(x, y, z);
    }
}
