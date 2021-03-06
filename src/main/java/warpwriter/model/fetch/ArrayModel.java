package warpwriter.model.fetch;

import warpwriter.model.Fetch;
import warpwriter.model.IFetch;
import warpwriter.model.IModel;

/**
 * A simple IModel that wraps a 3D byte array, using its bounds to implement {@link #sizeX()} and its related methods
 * and using its contents to implement {@link #at(int, int, int)} (out-of-bounds requests are handled by returning 0).
 * <p>
 * Created by Tommy Ettinger on 10/11/2018.
 *
 * @author Tommy Ettinger
 */
public class ArrayModel extends Fetch implements IModel {
    public byte[][][] voxels;

    public ArrayModel() {
        this(new byte[12][12][8]);
    }

    public ArrayModel(byte[][][] voxels) {
        this.voxels = voxels;
    }

    public ArrayModel(IFetch fetch, int xSize, int ySize, int zSize) {
        voxels = new byte[xSize][ySize][zSize];
        for (int x = 0; x < sizeX(); x++)
            for (int y = 0; y < sizeY(); y++)
                for (int z = 0; z < sizeZ(); z++)
                    voxels[x][y][z] = fetch.at(x, y, z);
    }

    public ArrayModel(IModel model) {
        this(model, model.sizeX(), model.sizeY(), model.sizeZ());
    }

    /**
     * Gets the x size of the IModel, with requests for x limited between 0 (inclusive) to sizeX() (exclusive).
     *
     * @return the size of the x dimension of the IModel
     */
    @Override
    public int sizeX() {
        return voxels.length;
    }

    /**
     * Gets the y size of the IModel, with requests for y limited between 0 (inclusive) to sizeY() (exclusive).
     *
     * @return the size of the y dimension of the IModel
     */
    @Override
    public int sizeY() {
        return voxels[0].length;
    }

    /**
     * Gets the z size of the IModel, with requests for z limited between 0 (inclusive) to sizeZ() (exclusive).
     *
     * @return the size of the z dimension of the IModel
     */
    @Override
    public int sizeZ() {
        return voxels[0][0].length;
    }

    @Override
    public byte at(int x, int y, int z) {
        return outside(x, y, z) || voxels[x][y][z] == 0 ?
                safeNextFetch().at(x, y, z)
                : voxels[x][y][z];
    }

    @Override
    public boolean outside(int x, int y, int z) {
        return x < 0 || y < 0 || z < 0 || x >= sizeX() || y >= sizeY() || z >= sizeZ();
    }

    @Override
    public boolean inside(int x, int y, int z) {
        return !outside(x, y, z);
    }
}
