package warpwriter.model.fetch;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import warpwriter.model.Fetch;
import warpwriter.model.IFetch;

public class WorldFetch extends Fetch {
    Long2ObjectMap<IFetch> chunks;

    public Long2ObjectMap<IFetch> chunks() {
        return chunks;
    }

    public WorldFetch set(Long2ObjectMap<IFetch> chunks) {
        this.chunks = chunks;
        return this;
    }

    /**
     * @param x Any int from -1048576 to 1048575 inclusive
     * @param y Any int from -1048576 to 1048575 inclusive
     * @param z Any int from -1048576 to 1048575 inclusive
     * @return 3D coordinate packed into a long
     */
    public static long encode(int x, int y, int z) {
        return (x + 0x100000L & 0x1FFFFFL) | (y + 0x100000L & 0x1FFFFFL) << 21 | (z + 0x100000L & 0x1FFFFFL) << 42;
    }

    public static int decodeX(long encoded) {
        return (int) (encoded & 0x1FFFFFL) - 0x100000;
    }

    public static int decodeY(long encoded) {
        return (int) (encoded >>> 21 & 0x1FFFFFL) - 0x100000;
    }

    public static int decodeZ(long encoded) {
        return (int) (encoded >>> 42 & 0x1FFFFFL) - 0x100000;
    }

    /**
     * @param x Any int from 0 to 2097151 inclusive
     * @param y Any int from 0 to 2097151 inclusive
     * @param z Any int from 0 to 2097151 inclusive
     * @return 3D coordinate packed into a long
     */
    public static long encodeUnsigned(int x, int y, int z) {
        return (x & 0x1FFFFFL) | (y & 0x1FFFFFL) << 21 | (z & 0x1FFFFFL) << 42;
    }

    public static int decodeUnsignedX(long encoded) {
        return (int) (encoded & 0x1FFFFFL);
    }

    public static int decodeUnsignedY(long encoded) {
        return (int) (encoded >>> 21 & 0x1FFFFFL);
    }

    public static int decodeUnsignedZ(long encoded) {
        return (int) (encoded >>> 42 & 0x1FFFFFL);
    }
}
