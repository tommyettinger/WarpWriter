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
    public byte bite() {
        final int x = chainX(), y = chainY(), z = chainZ(),
                x2 = x / blockX, y2 = y / blockY, z2 = z / blockZ;
        return map.containsKey(x2, y2, z2) ?
                map.get(x2, y2, z2)
                .at(x % blockX, y % blockY, z % blockZ) : 0;
    }
}
