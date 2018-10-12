package warpwriter.fetch;

/**
 * A simple IModel that wraps a 3D byte array, using its bounds to implement {@link #xSize()} and its related methods
 * and using its contents to implement {@link #at(int, int, int)} (out-of-bounds requests are handled by returning 0).
 * <br>
 * Created by Tommy Ettinger on 10/11/2018.
 */
public class ArrayModel implements IModel {
    public byte[][][] voxels;
    public int sizeX, sizeY, sizeZ;
    public ArrayModel()
    {
        this(new byte[12][12][8]);
    }
    public ArrayModel(byte[][][] voxels)
    {
        this.voxels = voxels;
        sizeX = voxels.length;
        sizeY = voxels[0].length;
        sizeZ = voxels[0][0].length;
    }

    /**
     * Gets the x size of the IModel, with requests for x limited between 0 (inclusive) to xSize() (exclusive).
     *
     * @return the size of the x dimension of the IModel
     */
    @Override
    public int xSize() {
        return sizeX;
    }

    /**
     * Gets the y size of the IModel, with requests for y limited between 0 (inclusive) to ySize() (exclusive).
     *
     * @return the size of the y dimension of the IModel
     */
    @Override
    public int ySize() {
        return sizeY;
    }

    /**
     * Gets the z size of the IModel, with requests for z limited between 0 (inclusive) to zSize() (exclusive).
     *
     * @return the size of the z dimension of the IModel
     */
    @Override
    public int zSize() {
        return sizeX;
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
    public byte at(int x, int y, int z) {
        if(x < 0 || y < 0 || z < 0 || x >= sizeX || y >= sizeY || z >= sizeZ) 
            return 0;
        return voxels[x][y][z];
    }
}
