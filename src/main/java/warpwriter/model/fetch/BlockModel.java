package warpwriter.model.fetch;

import warpwriter.model.Fetch;
import warpwriter.model.IFetch;
import warpwriter.model.IModel;

/**
 * An IModel of a 3D array of equally-sized blocks of IFetches
 *
 * @author Ben McLean
 */
public class BlockModel extends Fetch implements IModel {
    @Override
    public byte bite() {
        final int x = chainX(), y = chainY(), z = chainZ();
        if (outside(x, y, z)) return deferByte();
        final IFetch iFetch = blocks[x / blockX][y / blockY][z / blockZ];
        final byte result = iFetch == null ? 0
                : iFetch.at(x % blockX, y % blockY, z % blockZ);
        return showThru ? deferByte(result) : result;
    }

    protected IFetch blocks[][][];

    public IFetch[][][] blocks() {
        return blocks;
    }

    public BlockModel setBlocks(IFetch[][][] blocks) {
        this.blocks = blocks;
        return this;
    }

    protected int blockX, blockY, blockZ;

    public BlockModel set(int blockX, int blockY, int blockZ) {
        return setBlockX(blockX).setBlockY(blockY).setBlockZ(blockZ);
    }

    public BlockModel setBlockX(int blockX) {
        this.blockX = blockX;
        return this;
    }

    public BlockModel setBlockY(int blockY) {
        this.blockY = blockY;
        return this;
    }

    public BlockModel setBlockZ(int blockZ) {
        this.blockZ = blockZ;
        return this;
    }

    protected boolean showThru = false;

    public boolean showThru() {
        return showThru;
    }

    public BlockModel set(boolean showThru) {
        this.showThru = showThru;
        return this;
    }

    @Override
    public int sizeX() {
        return blocks.length * blockX;
    }

    @Override
    public int sizeY() {
        return blocks[0].length * blockY;
    }

    @Override
    public int sizeZ() {
        return blocks[0][0].length * blockZ;
    }

    @Override
    public boolean inside(int x, int y, int z) {
        return !outside(x, y, z);
    }

    @Override
    public boolean outside(int x, int y, int z) {
        return x < 0 || y < 0 || z < 0 || x >= sizeX() || y >= sizeY() || z >= sizeZ();
    }
}
