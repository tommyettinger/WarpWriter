package warpwriter.model;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.Map;

/**
 * A hash-map data structure, with keys made from (x,y,z) triples fused into ints, and Fetch values meant to be used as
 * repeating tiles placed sparsely throughout a large space.
 * <p>
 * Created by Tommy Ettinger on 1/9/2019.
 */
public class HashMap3D<T> extends Int2ObjectOpenHashMap<T> {
    /**
     * Combines 3 int components x, y, and z, each between 0 and 1023 inclusive, into one int that can be used as a key
     * in this HashMap3D. 30 of the 32 bits in the returned int have the potential to be used, allowing about a
     * billion possible keys that never produce garbage or need garbage collection (at least for themselves).
     *
     * @param x the x component, between 0 and 1023; this can be extracted with {@link #extractX(int)}
     * @param y the y component, between 0 and 1023; this can be extracted with {@link #extractY(int)}
     * @param z the z component, between 0 and 1023; this can be extracted with {@link #extractZ(int)}
     * @return a fused XYZ index that can be used as one key; will be unique for any (x,y,z) triple within range
     */
    public static int fuse(int x, int y, int z) {
        return (z << 20 & 0x3FF00000) | (y << 10 & 0x000FFC00) | (x & 0x000003FF);
    }

    /**
     * Given a fused XYZ index as produced by {@link #fuse(int, int, int)}, this gets the x component back out of it.
     *
     * @param fused a fused XYZ index as produced by {@link #fuse(int, int, int)}
     * @return the x component stored in fused
     */
    public static int extractX(int fused) {
        return fused & 0x000003FF;
    }

    /**
     * Given a fused XYZ index as produced by {@link #fuse(int, int, int)}, this gets the y component back out of it.
     *
     * @param fused a fused XYZ index as produced by {@link #fuse(int, int, int)}
     * @return the y component stored in fused
     */
    public static int extractY(int fused) {
        return fused >>> 10 & 0x000003FF;
    }

    /**
     * Given a fused XYZ index as produced by {@link #fuse(int, int, int)}, this gets the z component back out of it.
     *
     * @param fused a fused XYZ index as produced by {@link #fuse(int, int, int)}
     * @return the z component stored in fused
     */
    public static int extractZ(int fused) {
        return fused >>> 20 & 0x000003FF;
    }

    public HashMap3D(int expected, float f) {
        super(expected, f);
    }

    public HashMap3D(int expected) {
        super(expected);
    }

    public HashMap3D() {
        super();
    }

    public HashMap3D(Map<? extends Integer, ? extends T> m, float f) {
        super(m, f);
    }

    public HashMap3D(Map<? extends Integer, ? extends T> m) {
        super(m);
    }

    public HashMap3D(Int2ObjectMap<T> m, float f) {
        super(m, f);
    }

    public HashMap3D(Int2ObjectMap<T> m) {
        super(m);
    }

    public HashMap3D(int[] k, T[] v, float f) {
        super(k, v, f);
    }

    public HashMap3D(int[] k, T[] v) {
        super(k, v);
    }

    public T put(final int x, final int y, final int z, T t) {
        return super.put(fuse(x, y, z), t);
    }

    public T remove(final int x, final int y, final int z) {
        return super.remove(fuse(x, y, z));
    }

    public T get(final int x, final int y, final int z) {
        return super.get(fuse(x, y, z));
    }

    public boolean containsKey(final int x, final int y, final int z) {
        return super.containsKey(fuse(x, y, z));
    }
}
