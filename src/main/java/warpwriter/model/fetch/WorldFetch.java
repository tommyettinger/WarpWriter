package warpwriter.model.fetch;

import warpwriter.model.Fetch;
import warpwriter.model.nonvoxel.HashMap3D;
import warpwriter.model.IFetch;

public class WorldFetch extends Fetch {
    protected HashMap3D<IFetch> map;

    public WorldFetch set(HashMap3D<IFetch> map) {
        this.map = map;
        return this;
    }

    public HashMap3D<IFetch> map() {
        return map;
    }

    protected int blockX = 16, blockY = 16, blockZ = 16;

    public int blockX() {
        return blockX;
    }

    public int blockY() {
        return blockY;
    }

    public int blockZ() {
        return blockZ;
    }

    public WorldFetch setX(int x) {
        this.blockX = x;
        return this;
    }

    public WorldFetch setY(int y) {
        this.blockY = y;
        return this;
    }

    public WorldFetch setZ(int z) {
        this.blockZ = z;
        return this;
    }

    public WorldFetch set(int x, int y, int z) {
        return setX(x).setY(y).setZ(z);
    }

    @Override
    public byte at(int x, int y, int z) {
        if((x | y | z) < 0) // if any of x, y or z are negative
        {
//            System.out.println("WorldFetch.bite() requested bad coordinates: " + x + "," + y + "," + z);
            return 0;
        }
        // the check for negative x, y, or z is needed because `x / blockX` produces 0 for
        // all x values from `-blockX + 1` to `blockX - 1`, making small negative x, y, and
        // z values produce the voxel at 0 for that dimension.
        final int x2 = x / blockX, y2 = y / blockY, z2 = z / blockZ;
        return map.containsKey(x2, y2, z2) ?
                map.get(x2, y2, z2)
                .at(x % blockX, y % blockY, z % blockZ) : 0;
    }
}
