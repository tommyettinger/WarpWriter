package warpwriter.fetch;

/**
 * A sparse IModel that can have sizeX, sizeY, and sizeZ each up to 256, and uses memory based on how many voxels are
 * filled rather than the bounds of the model. For most models, {@link ArrayModel} will be more efficient all around.
 * <br>
 * Created by Tommy Ettinger on 10/11/2018.
 */
public class VoxModel extends SparseVoxelSet implements IModel {
    public int sizeX, sizeY, sizeZ;
    public VoxModel()
    {
        this(12, 12, 8);
    }
    public VoxModel(int xSize, int ySize, int zSize)
    {
        super(xSize * ySize * zSize >> 3);
        sizeX = xSize;
        sizeY = ySize;
        sizeZ = zSize;
    }
    public VoxModel(int xSize, int ySize, int zSize, int[] voxels)
    {
        super(voxels.length);
        sizeX = xSize;
        sizeY = ySize;
        sizeZ = zSize;
        for (int i = 0; i < voxels.length; i++) {
            add(voxels[i]);
        }
    }
    
    public VoxModel(byte[][][] voxels)
    {
        super(voxels.length * voxels[0].length * voxels[0][0].length >> 3);
        sizeX = voxels.length;
        sizeY = voxels[0].length;
        sizeZ = voxels[0][0].length;
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                for (int z = 0; z < sizeZ; z++) {
                    if(voxels[x][y][z] != 0)
                        add(x, y, z, voxels[x][y][z]);
                }
            }
        }
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
        return sizeZ;
    }
}
