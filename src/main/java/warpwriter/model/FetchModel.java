package warpwriter.model;

import warpwriter.model.fetch.FetchFetch;
import warpwriter.model.nonvoxel.CompassDirection;

/**
 * Converts a Fetch to a Model with sizes stored in ints
 *
 * @author Ben McLean
 */
public class FetchModel extends Fetch implements IModel {
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
        set(sizeX, sizeY, sizeZ);
        if (fetch != null) add(fetch);
    }

    /**
     * Converts an IModel which doesn't extend Fetch into one that does.
     * @param model Warning: FetchModel size is based on initial model. Does not automatically update FetchModel size if model size changes!
     */
    public FetchModel (IModel model) {
        set(model.sizeX(), model.sizeY(), model.sizeZ());
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
     * Copies size
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

    @Override
    public boolean outside(int x, int y, int z) {
        return x < 0 || y < 0 || z < 0 || x >= sizeX() || y >= sizeY() || z >= sizeZ();
    }

    @Override
    public boolean inside(int x, int y, int z) {
        return !outside(x, y, z);
    }

    public FetchModel add(int x, int y, int z) {
        return addX(x).addY(y).addZ(z);
    }

    public FetchModel addX(int x) {
        sizeX += x;
        return this;
    }

    public FetchModel addY(int y) {
        sizeY += y;
        return this;
    }

    public FetchModel addZ(int z) {
        sizeZ += z;
        return this;
    }

    public FetchModel add(CompassDirection direction) {
        return addX(direction.deltaX).addY(direction.deltaY);
    }

    public FetchModel set(int x, int y, int z) {
        return setX(x).setY(y).setZ(z);
    }

    public FetchModel setX(int x) {
        sizeX = x;
        return this;
    }

    public FetchModel setY(int y) {
        sizeY = y;
        return this;
    }

    public FetchModel setZ(int z) {
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
