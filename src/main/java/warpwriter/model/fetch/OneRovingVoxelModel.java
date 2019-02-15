package warpwriter.model.fetch;

import warpwriter.model.Fetch;
import warpwriter.model.FetchModel;

/**
 * Adds one voxel on top of a FetchModel.
 * The added voxel's position is restricted to being inside the bounds of the model somewhere.
 * This is useful for debugging purposes.
 *
 * @author Ben McLean
 */
public class OneRovingVoxelModel extends FetchModel {
    protected byte voxel = 0;

    public byte voxel() {
        return voxel;
    }

    public OneRovingVoxelModel setVoxel(final byte voxel) {
        this.voxel = voxel;
        return this;
    }

    protected int x = 0, y = 0, z = 0;

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public int z() {
        return z;
    }

    public OneRovingVoxelModel setX(final int x) {
        if (x >= 0 && x < sizeX()) this.x = x;
        return this;
    }

    public OneRovingVoxelModel setY(final int y) {
        if (y >= 0 && y < sizeY()) this.y = y;
        return this;
    }

    public OneRovingVoxelModel setZ(final int z) {
        if (z >= 0 && z < sizeZ()) this.z = z;
        return this;
    }

    public OneRovingVoxelModel set(final int x, final int y, final int z) {
        return setX(x).setY(y).setZ(z);
    }

    public OneRovingVoxelModel addX(final int x) {
        return setX(this.x + x);
    }

    public OneRovingVoxelModel addY(final int y) {
        return setY(this.y + y);
    }

    public OneRovingVoxelModel addZ(final int z) {
        return setZ(this.z + z);
    }

    public OneRovingVoxelModel add(final int x, final int y, final int z) {
        return addX(x).addY(y).addZ(z);
    }

    @Override
    public Fetch fetch() {
        return ColorFetch.color(bite());
    }

    @Override
    public byte bite() {
        return x() == chainX() && y() == chainY() && z() == chainZ() ? voxel : deferByte();
    }
}
