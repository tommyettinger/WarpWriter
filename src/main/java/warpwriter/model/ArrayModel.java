package warpwriter.model;

/**
 * A simple IModel that wraps a 3D byte array, using its bounds to implement {@link #xSize()} and its related methods
 * and using its contents to implement {@link #bite(int, int, int)} (out-of-bounds requests are handled by returning 0).
 * <br>
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
        for (int x = 0; x < xSize(); x++)
            for (int y = 0; y < ySize(); y++)
                for (int z = 0; z < zSize(); z++)
                    voxels[x][y][z] = fetch.at(x, y, z);
    }

    public ArrayModel(IModel model) {
        this(model, model.xSize(), model.ySize(), model.zSize());
    }

    /**
     * Gets the x size of the IModel, with requests for x limited between 0 (inclusive) to xSize() (exclusive).
     *
     * @return the size of the x dimension of the IModel
     */
    @Override
    public int xSize() {
        return voxels.length;
    }

    /**
     * Gets the y size of the IModel, with requests for y limited between 0 (inclusive) to ySize() (exclusive).
     *
     * @return the size of the y dimension of the IModel
     */
    @Override
    public int ySize() {
        return voxels[0].length;
    }

    /**
     * Gets the z size of the IModel, with requests for z limited between 0 (inclusive) to zSize() (exclusive).
     *
     * @return the size of the z dimension of the IModel
     */
    @Override
    public int zSize() {
        return voxels[0][0].length;
    }

    /**
     * Looks up a color index (a byte) from a 3D position as x,y,z int parameters. Index 0 is used to mean an
     * empty position with no color.
     *
     * @param x x position to look up; depending on angle, can be forward/back or left/right
     * @param y y position to look up; depending on angle, can be left/right or forward/back
     * @param z z position to look up; almost always up/down
     * @return a color index as a byte; 0 is empty, and this should usually be masked with {@code & 255} to get an index
     */
    @Override
    public Fetch fetch() {
        int x = xChain(), y = yChain(), z = zChain();
        return inside(x, y, z) ? ColorFetch.color(voxels[x][y][z]) : getNextFetch();
    }

    @Override
    public byte bite() {
        int x = xChain(), y = yChain(), z = zChain();
        return deferByte(inside(x, y, z) ? voxels[x][y][z] : (byte) 0, x, y, z);
    }

    @Override
    public boolean outside(int x, int y, int z) {
        return x < 0 || y < 0 || z < 0 || x >= xSize() || y >= ySize() || z >= zSize();
    }

    @Override
    public boolean inside(int x, int y, int z) {
        return !outside(x, y, z);
    }
}
