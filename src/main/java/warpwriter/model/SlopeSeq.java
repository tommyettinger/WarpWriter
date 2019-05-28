/*
 * Copyright (C) 2002-2015 Sebastiano Vigna
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package warpwriter.model;

import squidpony.annotation.GwtIncompatible;
import squidpony.squidmath.*;
import warpwriter.model.nonvoxel.IntComparator;
import warpwriter.model.nonvoxel.IntSort;

import java.io.Serializable;
import java.util.*;

import static warpwriter.model.nonvoxel.HashMap3D.*;

/**
 * A variant on {@link VoxelSeq} that adds a byte value for slope to every voxel pair that already has a position and
 * color. The 8 bits of slope correspond to the 8 corners of a cube voxel that may be truncated; slope 0 is a normal
 * cube and slope 255 (or -1) is an empty cell. These corners are ordered so that the index of a bit (where an index can
 * range from 0 to 7) is itself constructed of a bit for x (if this is 0, it refers to the low-x side, or if this is 1
 * it refers to the high-x side), a bit for y (like for x, but 0 or 2), and a bit for z (like the others, but 0 or 4).
 * <br>
 * Thank you, Sebastiano Vigna, for making FastUtil available to the public with such high quality.
 * <br>
 * See https://github.com/vigna/fastutil for the original library.
 * @author Sebastiano Vigna (responsible for all the hard parts)
 * @author Tommy Ettinger (mostly responsible for squashing several layers of parent classes into one monster class)
 */
public class SlopeSeq implements IVoxelSeq, Serializable, Cloneable {
    private static final long serialVersionUID = 0L;
    /**
     * The array of position keys.
     */
    protected int[] key;
    /**
     * The array of color index values.
     */
    protected byte[] value;
    /**
     * The array of slope index values.
     */
    protected byte[] slope;
    /**
     * The mask for wrapping a position counter.
     */
    protected int mask;
    /**
     * Whether this set contains the key zero.
     */
    protected boolean containsNullKey;
    /**
     * An IntVLA (variable-length int sequence) that stores the positions in the key array of specific keys, with the
     * positions in insertion order. The order can be changed with {@link #reorder(int...)} and other methods. This
     * IntVLA tracks only voxels that touch a boundary or empty space; use {@link #full} for the order of all voxels.
     */
    protected IntVLA order;
    /**
     * An IntVLA (variable-length int sequence) that stores the positions in the key array of all keys, with the
     * positions in insertion order. The order can be changed with {@link #reorder(int...)} and other methods. This
     * IntVLA tracks all voxels; many operations instead operate only on voxels that are potentially visible, which are
     * in the IntVLA {@link #order}.
     */
    protected IntVLA full;
    /**
     * The current table size.
     */
    protected int n;
    /**
     * Threshold after which we rehash. It must be the table size times {@link #f}.
     */
    protected int maxFill;
    /**
     * Number of entries in the set (including the key zero, if present).
     */
    protected int size;

    /**
     * Maximum size on the x-dimension for keys.
     */
    public int sizeX = 40;

    /**
     * Maximum size on the y-dimension for keys.
     */
    public int sizeY = 40;

    /**
     * Maximum size on the z-dimension for keys.
     */
    public int sizeZ = 40;

    /**
     * Index into a rotation array, which should almost always have 24 items, so this should be between 0-23 inclusive.
     */
    public int rotation = 0;

    public int sizeX() {
        return sizeX;
    }

    public void sizeX(int sizeX) {
        this.sizeX = sizeX;
    }

    public int sizeY() {
        return sizeY;
    }

    public void sizeY(int sizeY) {
        this.sizeY = sizeY;
    }

    public int sizeZ() {
        return sizeZ;
    }

    public void setSizeZ(int sizeZ) {
        this.sizeZ = sizeZ;
    }

    public int rotation() {
        return rotation;
    }

    public void rotate(int rotation) {
        this.rotation = rotation;
    }

    /**
     * A fast alternative to HashCommon's mix() implementation; still GWT-safe. Since this a purely  bitwise-math
     * method, it may perform a bit better on GWT (the bitwise-heavy xoroshiro generator tends to significantly outpace
     * multiplication-dependent random number generators on GWT, so xoroshiro is used by GWTRNG).
     * @param n the number to mix, usually for a hash
     * @return the number with bits mixed; this is a bijection but I don't know how to reverse it (just that it can be done)
     */
    private static int mix(final int n){
        return n ^ n >>> 12 ^ n >>> 22 ^ n >>> 16;
    }

    /**
     * The acceptable load factor.
     */
    public final float f;
    /**
     * Cached set of entries.
     */
    protected volatile MapEntrySet entries;
    /**
     * Cached set of keys.
     */
    protected volatile KeySet keys;
    /**
     * Cached collection of values.
     */
    protected volatile ValueCollection values;
    /**
     * Default return value.
     */
    protected byte defRetValue = 0;

    /**
     * The initial default size of a hash table.
     */
    public static final int DEFAULT_INITIAL_SIZE = 16;
    /**
     * The default load factor of a hash table.
     */
    public static final float DEFAULT_LOAD_FACTOR = .75f; // .1875f; // .75f;
    /**
     * The load factor for a (usually small) table that is meant to be particularly fast.
     */
    public static final float FAST_LOAD_FACTOR = .5f;
    /**
     * The load factor for a (usually very small) table that is meant to be extremely fast.
     */
    public static final float VERY_FAST_LOAD_FACTOR = .25f;

    public void defaultReturnValue(final byte rv) {
        defRetValue = rv;
    }

    public byte defaultReturnValue() {
        return defRetValue;
    }

    /**
     * Creates a new VoxelSeq.
     * <p>
     * <p>The actual table size will be the least power of two greater than <code>expected</code>/<code>f</code>.
     *
     * @param expected the expected number of voxels in the whole VoxelSeq, including non-visible ones
     * @param f        the load factor.
     */
    @SuppressWarnings("unchecked")
    public SlopeSeq(final int expected, final float f) {
        if (f <= 0 || f > 1)
            throw new IllegalArgumentException("Load factor must be greater than 0 and smaller than or equal to 1");
        if (expected < 0) throw new IllegalArgumentException("The expected number of elements must be nonnegative");
        this.f = f;
        n = arraySize(expected, f);
        mask = n - 1;
        maxFill = maxFill(n, f);
        key = new int[n + 1];
        value = new byte[n + 1];
        slope = new byte[n + 1];
        order = new IntVLA(expected * 3 >>> 2);
        full = new IntVLA(expected);
    }

    /**
     * Creates a new VoxelSeq with 0.75f as load factor.
     *
     * @param expected the expected number of elements in the VoxelSeq.
     */
    public SlopeSeq(final int expected) {
        this(expected, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Creates a new VoxelSeq with initial expected 16 entries and 0.75f as load factor.
     */
    public SlopeSeq() {
        this(DEFAULT_INITIAL_SIZE, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Creates a new VoxelSeq copying a given one.
     *
     * @param m a {@link Map} to be copied into the new VoxelSeq.
     * @param f the load factor.
     */
    public SlopeSeq(final SlopeSeq m, final float f) {
        this(m.size, f);
        putAll(m);
    }

    /**
     * Creates a new VoxelSeq with 0.75f as load factor copying a given one.
     *
     * @param m a {@link Map} to be copied into the new VoxelSeq.
     */
    public SlopeSeq(final SlopeSeq m) {
        this(m, m.f);
    }

    /**
     * Creates a new VoxelSeq using the elements of two parallel arrays.
     *
     * @param keyArray the array of keys of the new VoxelSeq.
     * @param valueArray the array of corresponding values in the new VoxelSeq.
     * @param f the load factor.
     * @throws IllegalArgumentException if <code>k</code> and <code>v</code> have different lengths.
     */
    public SlopeSeq(final int[] keyArray, final byte[] valueArray, final float f) {
        this(keyArray.length, f);
        if (keyArray.length != valueArray.length)
            throw new IllegalArgumentException("The key array and the value array have different lengths (" + keyArray.length + " and " + valueArray.length + ")");
        for (int i = 0; i < keyArray.length; i++)
            put(keyArray[i], valueArray[i]);
    }

    /**
     * Creates a new VoxelSeq with 0.75f as load factor using the elements of two parallel arrays.
     *
     * @param keyArray the array of keys of the new VoxelSeq.
     * @param valueArray the array of corresponding values in the new VoxelSeq.
     * @throws IllegalArgumentException if <code>k</code> and <code>v</code> have different lengths.
     */
    public SlopeSeq(final int[] keyArray, final byte[] valueArray) {
        this(keyArray, valueArray, DEFAULT_LOAD_FACTOR);
    }


    /**
     * Creates a new VoxelSeq using the elements of two parallel arrays.
     *
     * @param keyArray the array of keys of the new VoxelSeq.
     * @param valueArray the array of corresponding values in the new VoxelSeq.
     * @param f the load factor.
     * @throws IllegalArgumentException if <code>k</code> and <code>v</code> have different lengths.
     */
    public SlopeSeq(final int[] keyArray, final byte[] valueArray, final byte[] slopeArray, final float f) {
        this(keyArray.length, f);
        if (keyArray.length != valueArray.length || keyArray.length != slopeArray.length)
            throw new IllegalArgumentException("The key array and the value array have different lengths (keys:" + keyArray.length + ", values:" + valueArray.length + ", slopes:" + slopeArray.length + ")");
        for (int i = 0; i < keyArray.length; i++)
            put(keyArray[i], valueArray[i], slopeArray[i]);
    }

    /**
     * Creates a new VoxelSeq with 0.75f as load factor using the elements of two parallel arrays.
     *
     * @param keyArray the array of keys of the new VoxelSeq.
     * @param valueArray the array of corresponding values in the new VoxelSeq.
     * @throws IllegalArgumentException if <code>k</code> and <code>v</code> have different lengths.
     */
    public SlopeSeq(final int[] keyArray, final byte[] valueArray, final byte[] slopeArray) {
        this(keyArray, valueArray, slopeArray, DEFAULT_LOAD_FACTOR);
    }

    public void putArray(byte[][][] voxels)
    {
        final int sizeX = (voxels.length);
        final int sizeY = (voxels[0].length);
        final int sizeZ = (voxels[0][0].length);
        this.sizeX = this.sizeY = this.sizeZ = Math.max(sizeX, Math.max(sizeY, sizeZ));
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                for (int z = 0; z < sizeZ; z++) {
                    if(voxels[x][y][z] != 0)
                        put(x, y, z, voxels[x][y][z]);
                }
            }
        }
    }

    public void putSurface(byte[][][] voxels)
    {
        final int sizeX = (voxels.length);
        final int sizeY = (voxels[0].length);
        final int sizeZ = (voxels[0][0].length);
        this.sizeX = this.sizeY = this.sizeZ = Math.max(sizeX, Math.max(sizeY, sizeZ));
        for (int y = 0; y < sizeY; y++) {
            for (int z = 0; z < sizeZ; z++) {
                if(voxels[0][y][z] != 0)
                    put(0, y, z, voxels[0][y][z]);
                if(voxels[sizeX - 1][y][z] != 0)
                    put(sizeX - 1, y, z, voxels[sizeX - 1][y][z]);
            }
        }
        for (int x = 1; x < sizeX - 1; x++) {
            for (int z = 0; z < sizeZ; z++) {
                if(voxels[x][0][z] != 0)
                    put(x, 0, z, voxels[x][0][z]);
                if(voxels[x][sizeY - 1][z] != 0)
                    put(x, sizeY - 1, z, voxels[x][sizeY - 1][z]);
            }
            for (int y = 1; y < sizeY - 1; y++) {
                if(voxels[x][y][0] != 0)
                    put(x, y, 0, voxels[x][y][0]);
                if(voxels[x][y][sizeZ - 1] != 0)
                    put(x, y, sizeZ - 1, voxels[x][y][sizeZ - 1]);
                for (int z = 1; z < sizeZ - 1; z++) {
                    if(voxels[x][y][z] != 0 && (voxels[x - 1][y][z] == 0 || voxels[x + 1][y][z] == 0 ||
                            voxels[x][y - 1][z] == 0 || voxels[x][y + 1][z] == 0 ||
                            voxels[x][y][z - 1] == 0 || voxels[x][y][z + 1] == 0))
                        put(x, y, z, voxels[x][y][z]);
                }
            }
        }
        order.clear();
        order.addAll(full);
    }

    /**
     * Resets the order of potentially-visible voxels as used by the rotation-related methods; since this order is not
     * changed by normal {@link #put(int, byte)} and {@link #remove(int)}, this method must be used to complete any
     * changes to the structure of the VoxelSeq.
     */
    public void hollow()
    {
        order.clear();
        final int sz = full.size;
        int k, x, y, z, item;
        for (int i = 0; i < sz; i++) {
            k = key[item = full.items[i]];
            x = extractX(k);
            y = extractY(k);
            z = extractZ(k);
            if(x <= 0 || x >= sizeX - 1 || y <= 0 || y >= sizeY -1 || z <= 0 || z >= sizeZ - 1 ||
                    !containsKey(x - 1, y, z) || !containsKey(x + 1, y, z) ||
                    !containsKey(x, y - 1, z) || !containsKey(x, y + 1, z) ||
                    !containsKey(x, y, z - 1) || !containsKey(x, y, z + 1))
                order.add(item);
        }
    }
    private int realSize() {
        return containsNullKey ? size - 1 : size;
    }
    private void ensureCapacity(final int capacity) {
        final int needed = arraySize(capacity, f);
        if (needed > n)
            rehash(needed);
    }
    private void tryCapacity(final long capacity) {
        final int needed = (int) Math.min(
                1 << 30,
                Math.max(2, HashCommon.nextPowerOfTwo((long) Math.ceil(capacity
                        / f))));
        if (needed > n)
            rehash(needed);
    }
    private byte removeEntry(final int pos) {
        final byte oldValue = value[pos];
        size--;
        fixOrder(pos);
        shiftKeys(pos);
        if (size < (maxFill >> 2) && n > DEFAULT_INITIAL_SIZE)
            rehash(n >> 1);
        return oldValue;
    }
    private byte removeNullEntry() {
        containsNullKey = false;
        final byte oldValue = value[n];
        size--;
        fixOrder(n);
        if (size < (maxFill >> 2) && n > DEFAULT_INITIAL_SIZE)
            rehash(n >> 1);
        return oldValue;
    }

    /**
     * Puts the first key in keyArray with the first value in valueArray, then the second in each and so on.
     * The entries are all appended to the end of the iteration order, unless a key was already present. Then,
     * its value is changed at the existing position in the iteration order.
     * If the lengths of the two arrays are not equal, this puts a number of entries equal to the lesser length.
     * If either array is null, this returns without performing any changes.
     * @param keyArray an array of int keys that should usually have the same length as valueArray
     * @param valueArray an array of int values that should usually have the same length as keyArray
     */
    public void putAll(final int[] keyArray, final byte[] valueArray)
    {
        if(keyArray == null || valueArray == null)
            return;
        for (int i = 0; i < keyArray.length && i < valueArray.length; i++)
            put(keyArray[i], valueArray[i]);

    }

    /**
     * Puts all key-value pairs in the Map m into this VoxelSeq.
     * The entries are all appended to the end of the iteration order, unless a key was already present. Then,
     * its value is changed at the existing position in the iteration order. This can take any kind of Map,
     * including unordered HashMap objects; if the Map does not have stable ordering, the order in which entries
     * will be appended is not stable either. For this reason, VoxelSeq, LinkedHashMap, and TreeMap (or other
     * SortedMap implementations) will work best when order matters.
     * @param m an IntIntOrderedMap to add into this
     */
    public void putAll(SlopeSeq m) {
        if (f <= .5)
            ensureCapacity(m.size); // The resulting map will be sized for m.size() elements
        else
            tryCapacity(size + m.size); // The resulting map will be size() +  m.size() elements
        int n = m.size;
        for (int i = 0; i < n; i++) {             
            put(m.keyAt(i), m.getAt(i));
        }
    }
    private int insert(final int k, final byte v, final byte s) {
        int pos;
        if (k == 0) {
            if (containsNullKey)
                return n;
            containsNullKey = true;
            pos = n;
        } else {
            int curr;
            final int[] key = this.key;
            // The starting point.
            if ((curr = key[pos = (mix(k)) & mask]) != 0) {
                if (curr == k)
                    return pos;
                while ((curr = key[pos = (pos + 1) & mask]) != 0)
                    if (curr == k)
                        return pos;
            }
        }
        key[pos] = k;
        value[pos] = v;
        slope[pos] = s;
        full.add(pos);
        if (size++ >= maxFill)
            rehash(arraySize(size + 1, f));
        return -1;
    }
    private int insertAt(final int k, final byte v, final byte s, final int idx) {
        int pos;
        if (k == 0) {
            if (containsNullKey)
            {
                fixOrder(n);
                full.insert(idx, n);
                return n;
            }
            containsNullKey = true;
            pos = n;
        } else {
            int curr;
            final int[] key = this.key;
            // The starting point.
            if ((curr = key[pos = (mix(k)) & mask]) != 0) {
                if (curr == k)
                {
                    fixOrder(pos);
                    full.insert(idx, pos);
                    return pos;
                }
                while ((curr = key[pos = (pos + 1) & mask]) != 0)
                    if (curr == k)
                    {
                        fixOrder(pos);
                        full.insert(idx, pos);
                        return pos;
                    }
            }
        }
        key[pos] = k;
        value[pos] = v;
        slope[pos] = s;
        full.insert(idx, pos);
        if (size++ >= maxFill)
            rehash(arraySize(size + 1, f));
        return -1;
    }
    public byte put(final int k, final byte v) {
        final int pos = insert(k, v, (byte) 0);
        if (pos < 0)
            return defRetValue;
        final byte oldValue = value[pos];
        value[pos] = v;
        return oldValue;
    }
    public byte putAt(final int k, final byte v, final int idx) {
        final int pos = insertAt(k, v, (byte) 0, idx);
        if (pos < 0)
            return defRetValue;
        final byte oldValue = value[pos];
        value[pos] = v;
        return oldValue;
    }
    public byte put(final int k, final byte v, final byte s) {
        final int pos = insert(k, v, s);
        if (pos < 0)
            return defRetValue;
        final byte oldValue = value[pos];
        value[pos] = v;
        slope[pos] = s;
        return oldValue;
    }
    public byte putAt(final int k, final byte v, final byte s, final int idx) {
        final int pos = insertAt(k, v, s, idx);
        if (pos < 0)
            return defRetValue;
        final byte oldValue = value[pos];
        value[pos] = v;
        slope[pos] = s;
        return oldValue;
    }
    /**
     * Shifts left entries with the specified hash code, starting at the
     * specified position, and empties the resulting free entry.
     *
     * @param pos
     *            a starting position.
     */
    protected final void shiftKeys(int pos) {
        // Shift entries with the same hash.
        int last, slot;
        int curr;
        final int[] key = this.key;
        for (;;) {
            pos = ((last = pos) + 1) & mask;
            for (;;) {
                if ((curr = key[pos]) == 0) {
                    key[last] = 0;
                    return;
                }
                slot = (mix(curr))
                        & mask;
                if (last <= pos ? last >= slot || slot > pos : last >= slot
                        && slot > pos)
                    break;
                pos = (pos + 1) & mask;
            }
            key[last] = curr;
            value[last] = value[pos];
            slope[last] = slope[pos];
            fixOrder(pos, last);
        }
    }
    @SuppressWarnings("unchecked")
    public byte remove(final int k) {
        if (k == 0) {
            if (containsNullKey)
                return removeNullEntry();
            return defRetValue;
        }
        int curr;
        final int[] key = this.key;
        int pos;
        // The starting point.
        if ((curr = key[pos = (mix(k)) & mask]) == 0)
            return defRetValue;
        if (k == curr)
            return removeEntry(pos);
        while (true) {
            if ((curr = key[pos = (pos + 1) & mask]) == 0)
                return defRetValue;
            if (k == curr)
                return removeEntry(pos);
        }
    }
    public byte get(final int k) {
        if (k == 0)
            return containsNullKey ? value[n] : defRetValue;
        int curr;
        final int[] key = this.key;
        int pos;
        // The starting point.
        if ((curr = key[pos = (mix(k)) & mask]) == 0)
            return defRetValue;
        if (k == curr)
            return value[pos];
        // There's always an unused entry.
        while (true) {
            if ((curr = key[pos = (pos + 1) & mask]) == 0)
                return defRetValue;
            if (k == curr)
                return value[pos];
        }
    }

    public byte slope(final int k) {
        if (k == 0)
            return containsNullKey ? slope[n] : -1;
        int curr;
        final int[] key = this.key;
        int pos;
        // The starting point.
        if ((curr = key[pos = (mix(k)) & mask]) == 0)
            return -1;
        if (k == curr)
            return slope[pos];
        // There's always an unused entry.
        while (true) {
            if ((curr = key[pos = (pos + 1) & mask]) == 0)
                return -1;
            if (k == curr)
                return slope[pos];
        }
    }


    public byte getOrDefault(final int k, final byte defaultValue) {
        if (k == 0)
            return containsNullKey ? value[n] : defaultValue;
        int curr;
        final int[] key = this.key;
        int pos;
        // The starting point.
        if ((curr = key[pos = (mix(k)) & mask]) == 0)
            return defaultValue;
        if (k == curr)
            return value[pos];
        // There's always an unused entry.
        while (true) {
            if ((curr = key[pos = (pos + 1) & mask]) == 0)
                return defaultValue;
            if (k == curr)
                return value[pos];
        }
    }

    protected int positionOf(final int k) {
        if (k == 0)
        {
            if(containsNullKey)
                return n;
            else
                return -1;
        }
        int curr;
        final int[] key = this.key;
        int pos;
        // The starting point.
        if ((curr = key[pos = (mix(k)) & mask]) == 0)
            return -1;
        if (k == curr)
            return pos;
        // There's always an unused entry.
        while (true) {
            if ((curr = key[pos = pos + 1 & mask]) == 0)
                return -1;
            if (k == curr)
                return pos;
        }
    }

    /**
     * Gets the position in the ordering of the given key, though not as efficiently as some data structures can do it
     * (e.g. {@link Arrangement} can access ordering position very quickly but doesn't store other values on its own).
     * Returns a value that is at least 0 if it found k, or -1 if k was not present.
     * @param k a key or possible key that this should find the index of
     * @return the index of k, if present, or -1 if it is not present in this VoxelSeq
     */
    public int indexOf(final int k)
    {
        int pos = positionOf(k);
        return (pos < 0) ? -1 : full.indexOf(pos);
    }

    /**
     * Swaps the positions in the ordering for the given items, if they are both present. Returns true if the ordering
     * changed as a result of this call, or false if it stayed the same (which can be because left or right was not
     * present, or because left and right are the same reference (so swapping would do nothing)).
     * @param left an item that should be present in this VoxelSeq
     * @param right an item that should be present in this VoxelSeq
     * @return true if this VoxelSeq changed in ordering as a result of this call, or false otherwise
     */
    public boolean swap(final int left, final int right)
    {
        if(left == right) return false;
        int l = indexOf(left);
        if(l < 0) return false;
        int r = indexOf(right);
        if(r < 0) return false;
        full.swap(l, r);
        return true;
    }
    /**
     * Swaps the given indices in the ordering, if they are both valid int indices. Returns true if the ordering
     * changed as a result of this call, or false if it stayed the same (which can be because left or right referred to
     * an out-of-bounds index, or because left and right are equal (so swapping would do nothing)).
     * @param left an index of an item in this VoxelSeq, at least 0 and less than {@link #size()}
     * @param right an index of an item in this VoxelSeq, at least 0 and less than {@link #size()}
     * @return true if this VoxelSeq changed in ordering as a result of this call, or false otherwise
     */
    public boolean swapIndices(final int left, final int right)
    {
        if(left < 0 || right < 0 || left >= full.size || right >= full.size || left == right) return false;
        full.swap(left, right);
        return true;
    }


    public boolean containsKey(final int k) {
        if (k == 0)
            return containsNullKey;
        int curr;
        final int[] key = this.key;
        int pos;
        // The starting point.
        if ((curr = key[pos = (mix(k)) & mask]) == 0)
            return false;
        if (k == curr)
            return true;
        // There's always an unused entry.
        while (true) {
            if ((curr = key[pos = (pos + 1) & mask]) == 0)
                return false;
            if (k == curr)
                return true;
        }
    }
    public boolean containsValue(final byte v) {
        final byte[] value = this.value;
        final int[] key = this.key;
        if (containsNullKey && value[n] == v)
            return true;
        for (int i = n; i-- != 0;)
            if (key[i] != 0 && value[i] == v)
                return true;
        return false;
    }
    /*
     * Removes all elements from this map.
     *
     * <P>To increase object reuse, this method does not change the table size.
     * If you want to reduce the table size, you must use {@link #trim()}.
     */
    public void clear() {
        if (size == 0)
            return;
        size = 0;
        containsNullKey = false;
        Arrays.fill(key, 0);
        full.clear();
        order.clear();
    }

    public int fullSize() {
        return size;
    }
    public int size() {
        return order.size;
    }

    public boolean isEmpty() {
        return size == 0;
    }
    
    /**
     * The entry class for a VoxelSeq does not record key and value, but rather the position in the hash table of the corresponding entry. This is necessary so that calls to
     * {@link MapEntry#setValue(byte)} are reflected in the map
     */
    public final class MapEntry {
        // The table index this entry refers to, or -1 if this entry has been
        // deleted.
        int index;
        MapEntry(final int index) {
            this.index = index;
        }
        MapEntry() {
        }
        public int getKey() {
            return key[index];
        }
        public byte getValue() {
            return value[index];
        }
        public byte getSlope() {
            return slope[index];
        }
        public byte setValue(final byte v) {
            final byte oldValue = value[index];
            value[index] = v;
            return oldValue;
        }
        @SuppressWarnings("unchecked")
        public boolean equals(final Object o) {
            if (!(o instanceof MapEntry))
                return false;
            MapEntry e = (MapEntry) o;
            return key[index] == e.getKey() && value[index] == e.getValue() && slope[index] == e.getSlope();
        }
        public int hashCode() {
            return mix(key[index]) ^ mix(value[index]) ^ mix(slope[index]);
        }
        @Override
        public String toString() {
            return key[index] + "=>{" + value[index] + "," + slope[index] + "}";
        }
    }

    /**
     * Modifies the ordering so that the given entry is removed.
     *
     * @param i the index of an entry.
     * @return the iteration-order index of the removed entry
     */
    protected int fixOrder(final int i) {
        if (size == 0) {
            full.clear();
            order.clear();
            return -1;
        }
        order.removeValue(i);
        return full.removeValue(i);
    }

    /**
     * Modifies the ordering for a shift from s to d.
     *
     * @param s the source position.
     * @param d the destination position.
     */
    protected void fixOrder(int s, int d) {
        if(size == 0)
            return;
        if (size == 1)
        {
            order.set(0, d);
            full.set(0, d);
        }
        if(full.items[0] == s) {
            full.set(0, d);
        }
        else if (full.items[full.size-1] == s) {
            full.set(full.size - 1, d);
        }
        else
        {
            full.set(full.indexOf(s), d);
        }
        if(order.items[0] == s) {
            order.set(0, d);
        }
        else if (order.items[order.size-1] == s) {
            order.set(order.size - 1, d);
        }
        else
        {
            order.set(order.indexOf(s), d);
        }
    }

    /**
     * Returns the first key of this map in iteration order.
     *
     * @return the first key in iteration order.
     */
    public int firstKey() {
        if (size == 0)
            throw new NoSuchElementException();
        return key[full.items[0]];
    }
    /**
     * Returns the last key of this map in iteration order.
     *
     * @return the last key in iteration order.
     */
    public int lastKey() {
        if (size == 0)
            throw new NoSuchElementException();
        return key[full.items[full.size-1]];
    }
    /**
     * A list iterator over a VoxelSeq.
     *
     * <P>
     * This class provides a list iterator over a VoxelSeq. The
     * constructor runs in constant time.
     */
    private class MapIterator {
        /**
         * The entry that will be returned by the next call to
         * {@link ListIterator#previous()} (or <code>null</code> if no
         * previous entry exists).
         */
        int prev = -1;
        /**
         * The entry that will be returned by the next call to
         * {@link ListIterator#next()} (or <code>null</code> if no
         * next entry exists).
         */
        int next = -1;
        /**
         * The last entry that was returned (or -1 if we did not iterate or used
         * {@link Iterator#remove()}).
         */
        int curr = -1;
        /**
         * The current index (in the sense of a {@link ListIterator}).
         * Note that this value is not meaningful when this iterator has been
         * created using the nonempty constructor.
         */
        int index = 0;
        private MapIterator() {
            next = size == 0 ? -1 : order.items[0];
            index = 0;
        }
        /*
        private MapIterator(final K from) {
            if (((from) == null)) {
                if (containsNullKey) {
                    next = (int) link[n];
                    prev = n;
                    return;
                } else
                    throw new NoSuchElementException("The key null"
                            + " does not belong to this map.");
            }
            if (((key[last]) != null && (key[last]).equals(from))) {
                prev = last;
                index = size;
                return;
            }
            // The starting point.
            int pos = (((from).hashCode()))
                    & mask;
            // There's always an unused entry.
            while (!((key[pos]) == null)) {
                if (((key[pos]).equals(from))) {
                    // Note: no valid index known.
                    next = (int) link[pos];
                    prev = pos;
                    return;
                }
                pos = (pos + 1) & mask;
            }
            throw new NoSuchElementException("The key " + from
                    + " does not belong to this map.");
        }*/
        public boolean hasNext() {
            return next != -1;
        }
        public boolean hasPrevious() {
            return prev != -1;
        }
        private void ensureIndexKnown() {
            if (index >= 0)
                return;
            if (prev == -1) {
                index = 0;
                return;
            }
            if (next == -1) {
                index = size;
                return;
            }
            index = 0;
            /*while (pos != prev) {
                pos = (int) link[pos];
                index++;
            }*/
        }
        public int nextIndex() {
            ensureIndexKnown();
            return index + 1;
        }
        public int previousIndex() {
            ensureIndexKnown();
            return index - 1;
        }
        public int nextEntry() {
            if (!hasNext())
                throw new NoSuchElementException();
            curr = next;
            if(++index >= order.size)
                next = -1;
            else
                next = order.get(index);//(int) link[curr];
            prev = curr;
            return curr;
        }
        public int previousEntry() {
            if (!hasPrevious())
                throw new NoSuchElementException();
            curr = prev;
            if(--index < 1)
                prev = -1;
            else
                prev = order.get(index - 1);
            //prev = (int) (link[curr] >>> 32);
            next = curr;
            return curr;
        }
        public void remove() {
            ensureIndexKnown();
            if (curr == -1)
                throw new IllegalStateException();
            if (curr == prev) {
                /*
                 * If the last operation was a next(), we are removing an entry
                 * that precedes the current index, and thus we must decrement
                 * it.
                 */
                if(--index >= 1)
                    prev = order.get(index - 1); //(int) (link[curr] >>> 32);
                else
                    prev = -1;
            } else {
                if(index < order.size - 1)
                    next = order.get(index + 1);
                else
                    next = -1;
            }
            order.removeIndex(index);
            size--;
            int last, slot, pos = curr;
            curr = -1;
            if (pos == n) {
                containsNullKey = false;
            } else {
                int curr;
                final int[] key = SlopeSeq.this.key;
                // We have to horribly duplicate the shiftKeys() code because we
                // need to update next/prev.
                for (;;) {
                    pos = ((last = pos) + 1) & mask;
                    for (;;) {
                        if ((curr = key[pos]) == 0) {
                            key[last] = 0;
                            return;
                        }
                        slot = (mix(curr)) & mask;
                        if (last <= pos
                                ? last >= slot || slot > pos
                                : last >= slot && slot > pos)
                            break;
                        pos = (pos + 1) & mask;
                    }
                    key[last] = curr;
                    value[last] = value[pos];
                    if (next == pos)
                        next = last;
                    if (prev == pos)
                        prev = last;
                    fixOrder(pos, last);
                }
            }
        }
        public int skip(final int n) {
            int i = n;
            while (i-- != 0 && hasNext())
                nextEntry();
            return n - i - 1;
        }
        public int back(final int n) {
            int i = n;
            while (i-- != 0 && hasPrevious())
                previousEntry();
            return n - i - 1;
        }
    }
    private class EntryIterator extends MapIterator
            implements
            Iterator<MapEntry>, Serializable {
        private static final long serialVersionUID = 0L;

        private MapEntry entry;
        public EntryIterator() {
        }
        public MapEntry next() {
            return entry = new MapEntry(nextEntry());
        }
        public MapEntry previous() {
            return entry = new MapEntry(previousEntry());
        }
        @Override
        public void remove() {
            super.remove();
            entry.index = -1; // You cannot use a deleted entry.
        }
    }

    public class FastEntryIterator extends MapIterator implements ListIterator<MapEntry>, Serializable {
        private static final long serialVersionUID = 0L;

        final MapEntry entry = new MapEntry();

        public FastEntryIterator() {
        }
        public MapEntry next() {
            entry.index = nextEntry();
            return entry;
        }
        public MapEntry previous() {
            entry.index = previousEntry();
            return entry;
        }
        public void set(MapEntry ok) {
            throw new UnsupportedOperationException();
        }
        public void add(MapEntry ok) {
            throw new UnsupportedOperationException();
        }
    }
    public final class MapEntrySet
            implements Cloneable, SortedSet<MapEntry>, Set<MapEntry>, Serializable {
        private static final long serialVersionUID = 0L;
        public EntryIterator iterator() {
            return new EntryIterator();
        }
        public Comparator<? super MapEntry> comparator() {
            return null;
        }
        public SortedSet<MapEntry> subSet(
                MapEntry fromElement,
                MapEntry toElement) {
            throw new UnsupportedOperationException();
        }
        public SortedSet<MapEntry> headSet(
                MapEntry toElement) {
            throw new UnsupportedOperationException();
        }
        public SortedSet<MapEntry> tailSet(
                MapEntry fromElement) {
            throw new UnsupportedOperationException();
        }
        public MapEntry first() {
            if (size == 0)
                throw new NoSuchElementException();
            return new MapEntry(order.items[0]);
        }
        public MapEntry last() {
            if (size == 0)
                throw new NoSuchElementException();
            return new MapEntry(order.items[order.size-1]);
        }
        @SuppressWarnings("unchecked")
        public boolean contains(final Object o) {
            if (!(o instanceof MapEntry))
                return false;
            final MapEntry e = (MapEntry) o;
            final int k = e.getKey();
            final byte v = e.getValue();
            if (k == 0)
                return containsNullKey && (value[n] == v);
            int curr;
            final int[] key = SlopeSeq.this.key;
            int pos;
            // The starting point.
            if ((curr = key[pos = (mix(k)) & mask]) == 0)
                return false;
            if (k == curr)
                return value[pos] == v;
            // There's always an unused entry.
            while (true) {
                if ((curr = key[pos = (pos + 1) & mask]) == 0)
                    return false;
                if (k == curr)
                    return value[pos] == v;
            }
        }
        @SuppressWarnings("unchecked")
        public boolean remove(final Object o) {
            if (!(o instanceof MapEntry))
                return false;
            final MapEntry e = (MapEntry) o;
            final int k = e.getKey();
            final byte v = e.getValue();
            if (k == 0) {
                if (containsNullKey && value[n] == v) {
                    removeNullEntry();
                    return true;
                }
                return false;
            }
            int curr;
            final int[] key = SlopeSeq.this.key;
            int pos;
            // The starting point.
            if ((curr = key[pos = (mix(k)) & mask]) == 0)
                return false;
            if (curr == k) {
                if (value[pos] == v) {
                    removeEntry(pos);
                    return true;
                }
                return false;
            }
            while (true) {
                if ((curr = key[pos = (pos + 1) & mask]) == 0)
                    return false;
                if (curr == k) {
                    if (value[pos] == v) {
                        removeEntry(pos);
                        return true;
                    }
                }
            }
        }
        public int size() {
            return size;
        }
        public void clear() {
            SlopeSeq.this.clear();
        }

        public FastEntryIterator fastIterator() {
            return new FastEntryIterator();
        }

        @Override
        public boolean equals(final Object o) {
            if (o == this)
                return true;
            if (!(o instanceof Set))
                return false;
            Set<?> s = (Set<?>) o;
            return s.size() == size() && containsAll(s);
        }

        public Object[] toArray() {
            final Object[] a = new Object[size()];
            objectUnwrap(iterator(), a);
            return a;
        }

        @SuppressWarnings("unchecked")
        public <T> T[] toArray(T[] a) {
            if (a == null || a.length < size()) a = (T[]) new Object[size()];
            objectUnwrap(iterator(), a);
            return a;
        }

        /**
         * Unsupported.
         *
         * @param c ignored
         * @return nothing, throws UnsupportedOperationException
         * @throws UnsupportedOperationException always
         */
        public boolean addAll(Collection<? extends MapEntry> c) {
            throw new UnsupportedOperationException("addAll not supported");
        }

        /**
         * Unsupported.
         *
         * @param k ignored
         * @return nothing, throws UnsupportedOperationException
         * @throws UnsupportedOperationException always
         */
        public boolean add(MapEntry k) {
            throw new UnsupportedOperationException("add not supported");
        }

        /**
         * Checks whether this collection contains all elements from the given
         * collection.
         *
         * @param c a collection.
         * @return <code>true</code> if this collection contains all elements of the
         * argument.
         */
        public boolean containsAll(Collection<?> c) {
            int n = c.size();
            final Iterator<?> i = c.iterator();
            while (n-- != 0)
                if (!contains(i.next()))
                    return false;
            return true;
        }

        /**
         * Retains in this collection only elements from the given collection.
         *
         * @param c a collection.
         * @return <code>true</code> if this collection changed as a result of the
         * call.
         */
        public boolean retainAll(Collection<?> c) {
            boolean retVal = false;
            int n = size();
            final Iterator<?> i = iterator();
            while (n-- != 0) {
                if (!c.contains(i.next())) {
                    i.remove();
                    retVal = true;
                }
            }
            return retVal;
        }

        /**
         * Remove from this collection all elements in the given collection. If the
         * collection is an instance of this class, it uses faster iterators.
         *
         * @param c a collection.
         * @return <code>true</code> if this collection changed as a result of the
         * call.
         */
        public boolean removeAll(Collection<?> c) {
            boolean retVal = false;
            int n = c.size();
            final Iterator<?> i = c.iterator();
            while (n-- != 0)
                if (remove(i.next()))
                    retVal = true;
            return retVal;
        }

        public boolean isEmpty() {
            return size() == 0;
        }

        @Override
        public String toString() {
            final StringBuilder s = new StringBuilder();
            final EntryIterator i = iterator();
            int n = size();
            MapEntry k;
            boolean first = true;
            s.append("{");
            while (n-- != 0) {
                if (first)
                    first = false;
                else
                    s.append(", ");
                k = i.next();                 
                s.append(key[k.index]).append("=>").append(value[k.index]);
            }
            s.append("}");
            return s.toString();
        }

    }

    public SortedSet<MapEntry> entrySet() {
        if (entries == null) entries = new MapEntrySet();
        return entries;
    }

    /**
     * An iterator on keys.
     * <p>
     * <P>We simply override the {@link ListIterator#next()}/{@link ListIterator#previous()} methods (and possibly their type-specific counterparts) so that they return keys
     * instead of entries.
     */
    public final class KeyIterator extends MapIterator implements Iterator<Integer>, Serializable {
        private static final long serialVersionUID = 0L;
        public Integer previous() {
            return key[previousEntry()];
        }        
        public int previousInt() {
            return key[previousEntry()];
        }
        public void set(Integer k) {
            throw new UnsupportedOperationException();
        }
        public void add(Integer k) {
            throw new UnsupportedOperationException();
        }
        public KeyIterator() {}
        public Integer next() {
            return key[nextEntry()];
        }
        public int nextInt() {
            return key[nextEntry()];
        }
        public void remove() { super.remove(); }
    }

    public final class KeySet implements SortedSet<Integer>, Serializable {
        private static final long serialVersionUID = 0L;

        public KeyIterator iterator() {
            return new KeyIterator();
        }

        public int size() {
            return size;
        }

        public void clear() {
            SlopeSeq.this.clear();
        }

        public Integer first() {
            if (size == 0) throw new NoSuchElementException();
            return key[order.items[0]];
        }

        public Integer last() {
            if (size == 0) throw new NoSuchElementException();
            return key[order.items[order.size-1]];
        }

        public Comparator<Integer> comparator() {
            return null;
        }

        public final SortedSet<Integer> tailSet(Integer from) {
            throw new UnsupportedOperationException();
        }

        public final SortedSet<Integer> headSet(Integer to) {
            throw new UnsupportedOperationException();
        }

        public final SortedSet<Integer> subSet(Integer from, Integer to) {
            throw new UnsupportedOperationException();
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T[] toArray(T[] a) {
            if (a == null || a.length < size()) a = (T[]) new Object[size()];
            unwrap(iterator(), a);
            return a;
        }

        /**
         * Always throws an UnsupportedOperationException
         */
        public boolean remove(Object ok) {
            throw new UnsupportedOperationException("Cannot remove from the key set directly");
        }

        /**
         * Always throws an UnsupportedOperationException
         */
        public boolean add(final Integer o) {
            throw new UnsupportedOperationException("Cannot add to the key set directly");
        }

        /**
         * Delegates to the corresponding type-specific method.
         */
        public boolean contains(final Object o) {
            return o instanceof Integer && containsKey((Integer) o);
        }

        /**
         * Checks whether this collection contains all elements from the given type-specific collection.
         *
         * @param c a type-specific collection.
         * @return <code>true</code> if this collection contains all elements of the argument.
         */
        public boolean containsAll(Collection<?> c) {
            final Iterator<?> i = c.iterator();
            int n = c.size();
            while (n-- != 0)
                if (!contains(i.next())) return false;
            return true;
        }

        /**
         * Retains in this collection only elements from the given type-specific collection.
         *
         * @param c a type-specific collection.
         * @return <code>true</code> if this collection changed as a result of the call.
         */
        public boolean retainAll(Collection<?> c) {
            boolean retVal = false;
            int n = size();
            final Iterator<?> i = iterator();
            while (n-- != 0) {
                if (!c.contains(i.next())) {
                    i.remove();
                    retVal = true;
                }
            }
            return retVal;
        }

        /**
         * Remove from this collection all elements in the given type-specific collection.
         *
         * @param c a type-specific collection.
         * @return <code>true</code> if this collection changed as a result of the call.
         */
        public boolean removeAll(Collection<?> c) {
            boolean retVal = false;
            int n = c.size();
            final Iterator<?> i = c.iterator();
            while (n-- != 0)
                if (remove(i.next())) retVal = true;
            return retVal;
        }

        public Object[] toArray() {
            final Object[] a = new Object[size()];
            objectUnwrap(iterator(), a);
            return a;
        }

        /**
         * Adds all elements of the given collection to this collection.
         *
         * @param c a collection.
         * @return <code>true</code> if this collection changed as a result of the call.
         */
        public boolean addAll(Collection<? extends Integer> c) {
            boolean retVal = false;
            final Iterator<? extends Integer> i = c.iterator();
            int n = c.size();
            while (n-- != 0)
                if (add(i.next())) retVal = true;
            return retVal;
        }

        @Override
        public boolean equals(final Object o) {
            if (o == this)
                return true;
            if (!(o instanceof Set))
                return false;
            Set<?> s = (Set<?>) o;
            if (s.size() != size())
                return false;
            return containsAll(s);
        }
        /**
         * Unwraps an iterator into an array starting at a given offset for a given number of elements.
         * <p>
         * <P>This method iterates over the given type-specific iterator and stores the elements returned, up to a maximum of <code>length</code>, in the given array starting at <code>offset</code>. The
         * number of actually unwrapped elements is returned (it may be less than <code>max</code> if the iterator emits less than <code>max</code> elements).
         *
         * @param i      a type-specific iterator.
         * @param array  an array to contain the output of the iterator.
         * @param offset the first element of the array to be returned.
         * @param max    the maximum number of elements to unwrap.
         * @return the number of elements unwrapped.
         */
        public int unwrap(final KeyIterator i, final int[] array, int offset, final int max) {
            if (max < 0) throw new IllegalArgumentException("The maximum number of elements (" + max + ") is negative");
            if (offset < 0 || offset + max > array.length) throw new IllegalArgumentException();
            int j = max;
            while (j-- != 0 && i.hasNext())
                array[offset++] = i.nextInt();
            return max - j - 1;
        }
        /**
         * Unwraps an iterator into an array.
         * <p>
         * <P>This method iterates over the given type-specific iterator and stores the elements returned in the given array. The iteration will stop when the iterator has no more elements or when the end
         * of the array has been reached.
         *
         * @param i     a type-specific iterator.
         * @param array an array to contain the output of the iterator.
         * @return the number of elements unwrapped.
         */
        public int unwrap(final KeyIterator i, final int[] array) {
            return unwrap(i, array, 0, array.length);
        }
        public int[] toIntArray() {
            int[] a = new int[size()];
            unwrap(iterator(), a);
            return a;
        }

        public int[] toIntArray(int[] a) {
            if (a == null || a.length < size()) a = new int[size()];
            unwrap(iterator(), a);
            return a;
        }

        /**
         * Unwraps an iterator into an array starting at a given offset for a given number of elements.
         * <p>
         * <P>This method iterates over the given type-specific iterator and stores the elements returned, up to a maximum of <code>length</code>, in the given array starting at <code>offset</code>. The
         * number of actually unwrapped elements is returned (it may be less than <code>max</code> if the iterator emits less than <code>max</code> elements).
         *
         * @param i      a type-specific iterator.
         * @param array  an array to contain the output of the iterator.
         * @param offset the first element of the array to be returned.
         * @param max    the maximum number of elements to unwrap.
         * @return the number of elements unwrapped.
         */
        public int unwrap(final KeyIterator i, final Object[] array, int offset, final int max) {
            if (max < 0) throw new IllegalArgumentException("The maximum number of elements (" + max + ") is negative");
            if (offset < 0 || offset + max > array.length) throw new IllegalArgumentException();
            int j = max;
            while (j-- != 0 && i.hasNext())
                array[offset++] = i.next();
            return max - j - 1;
        }

        /**
         * Unwraps an iterator into an array.
         * <p>
         * <P>This method iterates over the given type-specific iterator and stores the elements returned in the given array. The iteration will stop when the iterator has no more elements or when the end
         * of the array has been reached.
         *
         * @param i     a type-specific iterator.
         * @param array an array to contain the output of the iterator.
         * @return the number of elements unwrapped.
         */
        public int unwrap(final KeyIterator i, final Object[] array) {
            return unwrap(i, array, 0, array.length);
        }

        public boolean isEmpty() {
            return size() == 0;
        }

        @Override
        public String toString() {
            final StringBuilder s = new StringBuilder();
            final KeyIterator i = iterator();
            int n = size();
            boolean first = true;
            s.append("{");
            while (n-- != 0) {
                if (first) first = false;
                else s.append(", ");
                s.append(i.next());
            }
            s.append("}");
            return s.toString();
        }
    }

    public KeySet keySet() {
        if (keys == null) keys = new KeySet();
        return keys;
    }

    public OrderedSet<Integer> keysAsOrderedSet()
    {
        OrderedSet<Integer> os = new OrderedSet<>(size, f);
        for (int i = 0; i < size; i++) {
            os.add(keyAt(i));
        }
        return os;
    }
    public int[] keysAsArray() {
        return keySet().toIntArray();
    }

    /**
     * An iterator on values.
     * <p>
     * <P>We simply override the {@link ListIterator#next()}/{@link ListIterator#previous()} methods (and possibly their type-specific counterparts) so that they return values
     * instead of entries.
     */
    public final class ValueIterator extends MapIterator implements ListIterator<Byte>, Serializable {
        private static final long serialVersionUID = 0L;

        public Byte previous() {
            return value[previousEntry()];
        }
        public void set(Byte v) {
            throw new UnsupportedOperationException();
        }
        public void add(Byte v) {
            throw new UnsupportedOperationException();
        }
        public ValueIterator() {}
        public Byte next() {
            return value[nextEntry()];
        }
        public void remove() { super.remove(); }
    }
    public final class ValueCollection extends AbstractCollection<Byte> implements Serializable
    {
        private static final long serialVersionUID = 0L;
        public ValueIterator iterator() {
            return new ValueIterator();
        }
        public int size() {
            return size;
        }
        public boolean contains(Object v) {
            return v instanceof Byte && containsValue((Byte)v);
        }
        public void clear() {
            SlopeSeq.this.clear();
        }
    }
    public Collection<Byte> values() {
        if (values == null) values = new ValueCollection();
        return values;
    }

    public byte[] valuesAsArray()
    {
        byte[] ls = new byte[size];
        for (int i = 0; i < size; i++) {
            ls[i] = getAt(i);
        }
        return ls;
    }

    /**
     * Rehashes the map, making the table as small as possible.
     * <p>
     * <P>This method rehashes the table to the smallest size satisfying the load factor. It can be used when the set will not be changed anymore, so to optimize access speed and size.
     * <p>
     * <P>If the table size is already the minimum possible, this method does nothing.
     *
     * @return true if there was enough memory to trim the map.
     * @see #trim(int)
     */
    public boolean trim() {
        final int l = arraySize(size, f);
        if (l >= n || size > maxFill(l, f)) return true;
        try {
            rehash(l);
        } catch (Exception cantDoIt) {
            return false;
        }
        return true;
    }

    /**
     * Rehashes this map if the table is too large.
     * <p>
     * <P>Let <var>N</var> be the smallest table size that can hold <code>max(n,{@link #size()})</code> entries, still satisfying the load factor. If the current table size is smaller than or equal to
     * <var>N</var>, this method does nothing. Otherwise, it rehashes this map in a table of size <var>N</var>.
     * <p>
     * <P>This method is useful when reusing maps. {@linkplain #clear() Clearing a map} leaves the table size untouched. If you are reusing a map many times, you can call this method with a typical
     * size to avoid keeping around a very large table just because of a few large transient maps.
     *
     * @param n the threshold for the trimming.
     * @return true if there was enough memory to trim the map.
     * @see #trim()
     */
    public boolean trim(final int n) {
        final int l = HashCommon.nextPowerOfTwo((int) Math.ceil(n / f));
        if (l >= n || size > maxFill(l, f)) return true;
        try {
            rehash(l);
        } catch (Exception cantDoIt) {
            return false;
        }
        return true;
    }

    /**
     * Rehashes the map.
     *
     * <P>
     * This method implements the basic rehashing strategy, and may be overriden
     * by subclasses implementing different rehashing strategies (e.g.,
     * disk-based rehashing). However, you should not override this method
     * unless you understand the internal workings of this class.
     *
     * @param newN
     *            the new size
     */

    @SuppressWarnings("unchecked")
    protected void rehash(final int newN) {
        final int[] key = this.key;
        final byte[] value = this.value;
        final int mask = newN - 1; // Note that this is used by the hashing macro
        final int[] newKey = new int[newN + 1];
        final byte[] newValue = new byte[newN + 1];
        final int sz = full.size;
        int k;
        int i, pos;
        final int[] oi = full.items;
        for (int q = 0; q < sz; q++) {
            i = oi[q];
            if ((k = key[i]) == 0)
                pos = newN;
            else {
                pos = (mix(k)) & mask;
                while (newKey[pos] != 0)
                    pos = (pos + 1) & mask;
            }
            newKey[pos] = k;
            newValue[pos] = value[i];
            oi[q] = pos;
        }
        n = newN;
        this.mask = mask;
        maxFill = maxFill(n, f);
        this.key = newKey;
        this.value = newValue;
    }
    /**
     * Returns a deep copy of this map.
     *
     * <P>
     * This method performs a deep copy of this VoxelSeq; the data stored in the
     * map, however, is not cloned. Note that this makes a difference only for
     * object keys.
     *
     * @return a deep copy of this map.
     */
    @SuppressWarnings("unchecked")
    @GwtIncompatible
    public SlopeSeq clone() {
        SlopeSeq c;
        try {
            c = (SlopeSeq) super.clone();
            c.key = new int[n + 1];
            System.arraycopy(key, 0, c.key, 0, n + 1);
            c.value = new byte[n + 1];
            System.arraycopy(value, 0, c.value, 0, n + 1);
            c.slope = new byte[n + 1];
            System.arraycopy(slope, 0, c.slope, 0, n + 1);
            c.order = order.copy();
            c.full = full.copy();
            return c;
        } catch (Exception cantHappen) {
            throw new UnsupportedOperationException(cantHappen + (cantHappen.getMessage() != null ?
                    "; " + cantHappen.getMessage() : ""));
        }
    }
    /**
     * Returns a hash code for this map.
     *
     * @return a hash code for this map.
     */
    public int hashCode() {
        int h = 0;
        for (int j = realSize(), i = 0, t = 0; j-- != 0;) {
            while (key[i] == 0)
                i++;             
            t = mix(key[i]) ^ mix(
                    value[i] ^ HashCommon.INV_INT_PHI ^ mix(slope[i] * 0xC13F));
            h += t;
            i++;
        }
        // Zero / null keys have hash zero.
        if (containsNullKey)
            h += mix(value[n] ^ HashCommon.INV_INT_PHI ^ mix(slope[n] * 0xC13F));
        return h;
    }

    public long hash64()
    {
        return 31L * (31L * CrossHash.hash64(key) + (31L * CrossHash.hash64(value) + CrossHash.hash64(slope))) + size;
    }
    /**
     * Returns the maximum number of entries that can be filled before rehashing.
     *
     * @param n the size of the backing array.
     * @param f the load factor.
     * @return the maximum number of entries before rehashing.
     */
    public static int maxFill(final int n, final float f) {
        /* We must guarantee that there is always at least
         * one free entry (even with pathological load factors). */
        return Math.min((int)(n * f + 0.99999994f), n - 1);
    }

    /**
     * Returns the least power of two smaller than or equal to 2<sup>30</sup> and larger than or equal to <code>Math.ceil( expected / f )</code>.
     *
     * @param expected the expected number of elements in a hash table.
     * @param f        the load factor.
     * @return the minimum possible size for a backing array.
     * @throws IllegalArgumentException if the necessary size is larger than 2<sup>30</sup>.
     */
    public static int arraySize(final int expected, final float f) {
        final long s = Math.max(2, HashCommon.nextPowerOfTwo((long) Math.ceil(expected / f)));
        if (s > (1 << 30))
            throw new IllegalArgumentException("Too large (" + expected + " expected elements with load factor " + f + ")");
        return (int) s;
    }

    /**
     * Unwraps an iterator into an array starting at a given offset for a given number of elements.
     * <p>
     * <P>This method iterates over the given type-specific iterator and stores the elements returned, up to a maximum of <code>length</code>, in the given array starting at <code>offset</code>. The
     * number of actually unwrapped elements is returned (it may be less than <code>max</code> if the iterator emits less than <code>max</code> elements).
     *
     * @param i      a type-specific iterator.
     * @param array  an array to contain the output of the iterator.
     * @param offset the first element of the array to be returned.
     * @param max    the maximum number of elements to unwrap.
     * @return the number of elements unwrapped.
     */
    private int unwrap(final ValueIterator i, final Object[] array, int offset, final int max) {
        if (max < 0) throw new IllegalArgumentException("The maximum number of elements (" + max + ") is negative");
        if (offset < 0 || offset + max > array.length) throw new IllegalArgumentException();
        int j = max;
        while (j-- != 0 && i.hasNext())
            array[offset++] = i.next();
        return max - j - 1;
    }

    /**
     * Unwraps an iterator into an array.
     * <p>
     * <P>This method iterates over the given type-specific iterator and stores the elements returned in the given array. The iteration will stop when the iterator has no more elements or when the end
     * of the array has been reached.
     *
     * @param i     a type-specific iterator.
     * @param array an array to contain the output of the iterator.
     * @return the number of elements unwrapped.
     */
    private int unwrap(final ValueIterator i, final Object[] array) {
        return unwrap(i, array, 0, array.length);
    }


    /** Unwraps an iterator into an array starting at a given offset for a given number of elements.
     *
     * <P>This method iterates over the given type-specific iterator and stores the elements returned, up to a maximum of <code>length</code>, in the given array starting at <code>offset</code>. The
     * number of actually unwrapped elements is returned (it may be less than <code>max</code> if the iterator emits less than <code>max</code> elements).
     *
     * @param i a type-specific iterator.
     * @param array an array to contain the output of the iterator.
     * @param offset the first element of the array to be returned.
     * @param max the maximum number of elements to unwrap.
     * @return the number of elements unwrapped. */
    private static <K> int objectUnwrap(final Iterator<? extends K> i, final K[] array, int offset, final int max ) {
        if ( max < 0 ) throw new IllegalArgumentException( "The maximum number of elements (" + max + ") is negative" );
        if ( offset < 0 || offset + max > array.length ) throw new IllegalArgumentException();
        int j = max;
        while ( j-- != 0 && i.hasNext() )
            array[ offset++ ] = i.next();
        return max - j - 1;
    }

    /** Unwraps an iterator into an array.
     *
     * <P>This method iterates over the given type-specific iterator and stores the elements returned in the given array. The iteration will stop when the iterator has no more elements or when the end
     * of the array has been reached.
     *
     * @param i a type-specific iterator.
     * @param array an array to contain the output of the iterator.
     * @return the number of elements unwrapped. */
    private static <K> int objectUnwrap(final Iterator<? extends K> i, final K[] array) {
        return objectUnwrap(i, array, 0, array.length );
    }

    @Override
    public String toString() {
        final StringBuilder s = new StringBuilder();
        int n = size, i = 0;
        boolean first = true;
        s.append("VoxelSeq{");
        while (i < n) {
            if (first) first = false;
            else s.append(", ");
            s.append(entryAt(i++));
        }
        s.append("}");
        return s.toString();
    }
    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof SlopeSeq))
            return false;
        SlopeSeq m = (SlopeSeq) o;
        if (m.size != size)
            return false;
        return entrySet().containsAll(m.entrySet());
    }
    
    /**
     * Gets the value at the given index in the iteration order in constant time (random-access).
     * @param idx the index in the iteration order of the value to fetch
     * @return the value at the index, if the index is valid, otherwise the default return value
     */
    public byte getAt(final int idx) {
        int pos;
        if (idx < 0 || idx >= full.size)
            return defRetValue;
        // The starting point.
        if (key[pos = full.get(idx)] == 0)
            return containsNullKey ? value[n] : defRetValue;
        return value[pos];
    }

    /**
     * Gets the value at the given index in the iteration order in constant time (random-access).
     * @param idx the index in the iteration order of the value to fetch
     * @return the value at the index, if the index is valid, otherwise the default return value
     */
    public byte slopeAt(final int idx) {
        int pos;
        if (idx < 0 || idx >= full.size)
            return defRetValue;
        // The starting point.
        if (key[pos = full.get(idx)] == 0)
            return containsNullKey ? slope[n] : defRetValue;
        return slope[pos];
    }
    /**
     * Gets the key at the given index in the iteration order in constant time (random-access).
     * @param idx the index in the iteration order of the key to fetch
     * @return the key at the index, if the index is valid, otherwise 0
     */
    public int keyAt(final int idx) {
        if (idx < 0 || idx >= full.size)
            return 0;
        // The starting point.
        return key[full.get(idx)];
    }

    /**
     * Gets the key-value Map.Entry at the given index in the iteration order in constant time (random-access).
     * @param idx the index in the iteration order of the entry to fetch
     * @return the key-value entry at the index, if the index is valid, otherwise null
     */
    public MapEntry entryAt(final int idx)
    {
        if (idx < 0 || idx >= full.size)
            return null;
        return new MapEntry(full.get(idx));
    }

    /**
     * Removes the key and value at the given index in the iteration order in not-exactly constant time (though it still
     * should be efficient).
     * @param idx the index in the iteration order of the key and value to remove
     * @return the value removed, if there was anything removed, or the default return value otherwise (often null)
     */
    public byte removeAt(final int idx) {

        if (idx < 0 || idx >= full.size)
            return defRetValue;
        int pos = full.get(idx);
        if (key[pos] == 0) {
            if (containsNullKey)
                return removeNullEntry();
            return defRetValue;
        }
        return removeEntry(pos);
    }
    /**
     * Gets the value at the given index in the iteration order in constant time (random-access).
     * @param idx the index in the iteration order of the value to fetch
     * @return the value at the index, if the index is valid, otherwise the default return value
     */
    public byte getAtHollow(final int idx) {
        int pos;
        if (idx < 0 || idx >= order.size)
            return defRetValue;
        // The starting point.
        if (key[pos = order.get(idx)] == 0)
            return containsNullKey ? value[n] : defRetValue;
        return value[pos];
    }
    /**
     * Gets the value at the given index in the iteration order in constant time (random-access).
     * @param idx the index in the iteration order of the value to fetch
     * @return the value at the index, if the index is valid, otherwise the default return value
     */
    public byte slopeAtHollow(final int idx) {
        int pos;
        if (idx < 0 || idx >= order.size)
            return defRetValue;
        // The starting point.
        if (key[pos = order.get(idx)] == 0)
            return containsNullKey ? slope[n] : defRetValue;
        return slope[pos];
    }
    /**
     * Gets the key at the given index in the iteration order in constant time (random-access).
     * @param idx the index in the iteration order of the key to fetch
     * @return the key at the index, if the index is valid, otherwise 0
     */
    public int keyAtHollow(final int idx) {
        if (idx < 0 || idx >= order.size)
            return 0;
        // The starting point.
        return key[order.get(idx)];
    }

    /**
     * Gets the key-value Map.Entry at the given index in the iteration order in constant time (random-access).
     * @param idx the index in the iteration order of the entry to fetch
     * @return the key-value entry at the index, if the index is valid, otherwise null
     */
    public MapEntry entryAtHollow(final int idx)
    {
        if (idx < 0 || idx >= order.size)
            return null;
        return new MapEntry(order.get(idx));
    }

    /**
     * Removes the key and value at the given index in the iteration order in not-exactly constant time (though it still
     * should be efficient).
     * @param idx the index in the iteration order of the key and value to remove
     * @return the value removed, if there was anything removed, or the default return value otherwise (often null)
     */
    public byte removeAtHollow(final int idx) {

        if (idx < 0 || idx >= full.size)
            return defRetValue;
        int pos = order.get(idx);
        if (key[pos] == 0) {
            if (containsNullKey)
                return removeNullEntry();
            return defRetValue;
        }
        return removeEntry(pos);
    }
    /**
     * Gets a random value from this VoxelSeq in constant time, using the given IRNG to generate a random number.
     * @param rng used to generate a random index for a value
     * @return a random value from this VoxelSeq
     */
    public byte randomValue(IRNG rng)
    {
        return value[full.getRandomElement(rng)];
//        return getAt(rng.nextInt(order.size));
    }

    /**
     * Gets a random key from this VoxelSeq in constant time, using the given IRNG to generate a random number.
     * @param rng used to generate a random index for a key
     * @return a random key from this VoxelSeq
     */
    public int randomKey(IRNG rng)
    {
        return key[full.getRandomElement(rng)];
//        return keyAt(rng.nextInt(order.size));
    }

    /**
     * Gets a random entry from this VoxelSeq in constant time, using the given IRNG to generate a random number.
     * @param rng used to generate a random index for a entry
     * @return a random key-value entry from this VoxelSeq
     */
    public MapEntry randomEntry(IRNG rng)
    {
        return new MapEntry(order.getRandomElement(rng));
    }

    /**
     * Randomly alters the iteration order for this VoxelSeq using the given IRNG to shuffle.
     * @param rng used to generate a random ordering
     * @return this for chaining
     */
    public SlopeSeq shuffle(IRNG rng)
    {
        if(size < 2)
            return this;
        order.shuffle(rng);
        return this;
    }

    /**
     * Given an array or varargs of replacement indices for this VoxelSeq's iteration order, reorders this so the
     * first item in the returned version is the same as {@code getAt(ordering[0])} (with some care taken for negative
     * or too-large indices), the second item in the returned version is the same as {@code getAt(ordering[1])}, etc.
     * <br>
     * Negative indices are considered reversed distances from the end of ordering, so -1 refers to the same index as
     * {@code ordering[ordering.length - 1]}. If ordering is smaller than {@code size()}, only the indices up to the
     * length of ordering will be modified. If ordering is larger than {@code size()}, only as many indices will be
     * affected as {@code size()}, and reversed distances are measured from the end of this Map's entries instead of
     * the end of ordering. Duplicate values in ordering will produce duplicate values in the returned Map.
     * <br>
     * This method modifies this VoxelSeq in-place and also returns it for chaining.
     * @param ordering an array or varargs of int indices, where the nth item in ordering changes the nth item in this
     *                 Map to have the value currently in this Map at the index specified by the value in ordering
     * @return this for chaining, after modifying it in-place
     */
    public SlopeSeq reorder(int... ordering)
    {
        full.reorder(ordering);
        return this;
    }
    private int alterEntry(final int pos) {
        int idx = fixOrder(pos);
        size--;
        shiftKeys(pos);
        return idx;
    }
    private int alterNullEntry() {
        int idx = fixOrder(n);
        containsNullKey = false;
        size--;
        return idx;
    }
    /**
     * Swaps a key, original, for another key, replacement, while keeping replacement at the same point in the iteration
     * order as original and keeping it associated with the same value (which also keeps its iteration index). Unlike
     * the similar method {@link #alter(int, int)}, this will not change this VoxelSeq if replacement is already
     * present. To contrast, alter() can reduce the size of the VoxelSeq if both original and replacement are already
     * in the Map. If replacement is found, this returns the default return value, otherwise it switches out original
     * for replacement and returns whatever was associated with original.
     * @param original the key to find and swap out
     * @param replacement the key to replace original with
     * @return the value associated with original before, and replacement now
     */
    public byte alterCarefully(final int original, final int replacement) {
        if(!containsKey(replacement))
            return alter(original, replacement);
        else
            return defRetValue;
    }
    /**
     * Swaps a key, original, for another key, replacement, while keeping replacement at the same point in the iteration
     * order as original and keeping it associated with the same value (which also keeps its iteration index).
     * Be aware that if both original and replacement are present in the VoxelSeq, this will still replace original
     * with replacement but will also remove the other occurrence of replacement to avoid duplicate keys. This can throw
     * off the expected order because the duplicate could be at any point in the ordering when it is removed. You may
     * want to prefer {@link #alterCarefully(int, int)} if you don't feel like checking by hand for whether
     * replacement is already present, but using this method is perfectly reasonable if you know overlaps won't happen.
     * @param original the key to find and swap out
     * @param replacement the key to replace original with
     * @return the value associated with original before, and replacement now
     */
    public byte alter(final int original, final int replacement) {
        byte v;
        int idx;
        if (original == 0) {
            if (containsNullKey) {
                v = value[n];
                idx = alterNullEntry();
                putAt(replacement, v, idx);
                return v;
            }
            else
                v = defRetValue;
            return v;
        }
        int curr;
        final int[] key = this.key;
        int pos;
        // The starting point.
        if ((curr = key[pos = (mix(original)) & mask]) == 0)
            return defRetValue;
        if (original == curr)
        {
            v = value[pos];
            idx = alterEntry(pos);
            putAt(replacement, v, idx);
            return v;
        }
        while (true) {
            if ((curr = key[pos = (pos + 1) & mask]) == 0)
                return defRetValue;
            if (original == curr)
            {
                v = value[pos];
                idx = alterEntry(pos);
                putAt(replacement, v, idx);
                return v;
            }
        }
    }

    public byte[] getMany(int... keys)
    {
        if(keys == null || keys.length == 0)
            return new byte[0];
        final int len = keys.length;
        byte[] vals = new byte[len];
        for (int i = 0; i < len; i++) {
            vals[i] = get(keys[i]);
        }
        return vals;
    }
    /**
     * Changes the int at the given index to replacement while keeping replacement at the same point in the ordering.
     * Be aware that if replacement is present in the VoxelSeq, this will still replace the given index
     * with replacement but will also remove the other occurrence of replacement to avoid duplicate keys. This can throw
     * off the expected order because the duplicate could be at any point in the ordering when it is removed. You may
     * want to prefer {@link #alterAtCarefully(int, int)} if you don't feel like checking by hand for whether
     * replacement is already present, but using this method is perfectly reasonable if you know overlaps won't happen.
     * @param index       an index to replace the int key at
     * @param replacement another int key that will replace the original at the remembered index
     * @return the value associated with the possibly-altered key
     */
    public byte alterAt(int index, int replacement)
    {
        return alter(keyAt(index), replacement);
    }
    /**
     * Changes the int at the given index to replacement while keeping replacement at the same point in the ordering.
     * Unlike the similar method {@link #alterAt(int, int)}, this will not change this VoxelSeq if replacement is
     * already present. To contrast, alterAt() can reduce the size of the VoxelSeq if replacement is already
     * in the Map. If replacement is found, this returns the default return value, otherwise it switches out the index
     * for replacement and returns whatever value was at the index before.
     * @param index       an index to replace the int key at
     * @param replacement another int key that will replace the original at the remembered index
     * @return the value associated with the key at the altered index before, and replacement now
     */
    public byte alterAtCarefully(int index, int replacement)
    {
        return alterCarefully(keyAt(index), replacement);
    }

    /**
     * If the specified key is not already associated with a value, associates it with the given value
     * and returns that value, else returns the current value without changing anything.
     *
     * @param key   key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with the specified key, or
     * {@code value} if there was no mapping for the key.
     */
    public byte putIfAbsent(int key, byte value) {
        if(containsKey(key))
            return get(key);         
        put(key, value);
        return value;
    }

    /**
     * Removes the entry for the specified key only if it is currently
     * mapped to the specified value.
     *
     * @param key   key with which the specified value is associated
     * @param value value expected to be associated with the specified key
     * @return {@code true} if the value was removed
     */
    public boolean remove(int key, byte value) {
        if (containsKey(key) && get(key) == value) {
            remove(key);
            return true;
        } else
            return false;
    }

    /**
     * Replaces the entry for the specified key only if currently
     * mapped to the specified value. The position in the iteration
     * order is retained.
     *
     * @param key      key with which the specified value is associated
     * @param oldValue value expected to be associated with the specified key
     * @param newValue value to be associated with the specified key
     * @return {@code true} if the value was replaced
     */
    public boolean replace(int key, byte oldValue, byte newValue) {
        if (containsKey(key) && get(key) == oldValue) {
            put(key, newValue);
            return true;
        } else
            return false;
    }

    /**
     * Replaces the entry for the specified key only if it is
     * currently mapped to some value. Preserves the existing key's
     * position in the iteration order.
     *
     * @param key   key with which the specified value is associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with the specified key, or
     * {@code null} if there was no mapping for the key.
     * (A {@code null} return can also indicate that the map
     * previously associated {@code null} with the key.)
     */
    public byte replace(int key, byte value) {
        if (containsKey(key)) {
            return put(key, value);
        } else
            return defRetValue;
    }
    public byte get(final int x, final int y, final int z)
    {
        return get(fuse(x, y, z));
    }
    public byte slope(final int x, final int y, final int z)
    {
        return slope(fuse(x, y, z));
    }
    public boolean containsKey(final int x, final int y, final int z)
    {
        return containsKey(fuse(x, y, z));
    }
    public byte remove(final int x, final int y, final int z)
    {
        return remove(fuse(x, y, z));
    }
    public byte put(final int x, final int y, final int z, final byte val)
    {
        return put(fuse(x, y, z), val);
    }
    public byte put(final int x, final int y, final int z, final byte val, final byte slope)
    {
        return put(fuse(x, y, z), val, slope);
    }
    /**
     * Sorts this whole VoxelSeq on its keys using the supplied Comparator.
     * @param comparator a Comparator that can be used on the same type this uses for its keys (may need wildcards)
     */
    public void sort(IntComparator comparator)
    {
        sort(comparator, 0, order.size);
    }

    /**
     * Sorts a sub-range of this VoxelSeq on its keys from what is currently the index {@code start} up to (but not
     * including) the index {@code end}, using the supplied Comparator. Only sorts potentially-visible voxels.
     * @param comparator a Comparator that can be used on the same type this uses for its keys (may need wildcards)
     * @param start the first index of a key to sort (the index can change after this)
     * @param end the exclusive bound on the indices to sort; often this is just {@link #size()}
     */
    public void sort(IntComparator comparator, int start, int end)
    {
        IntSort.sort(key, order, start, end, comparator);
    }
    /**
     * Gets the key at the given index in the iteration order in constant time, rotating the x, y, and z components of
     * the key to match {@link #rotation}.
     * @param idx the index in the potentially-visible-voxel order of the key to fetch
     * @return the key at the index, if the index is valid, otherwise 0
     */
    public int keyAtRotated(final int idx) {
        return keyAtRotated(idx, rotation);
    }
    /**
     * Gets the key at the given index in the iteration order in constant time, rotating the x, y, and z components of
     * the key to match {@code rotation} (a parameter, not the {@link #rotation} field of this class).
     * @param idx the index in the potentially-visible-voxel order of the key to fetch
     * @param rotation the rotation to use to edit the key; should be between 0 and 23 inclusive
     * @return the key at the index, if the index is valid, otherwise 0
     */
    public int keyAtRotated(final int idx, final int rotation) {
        if (idx < 0 || idx >= order.size)
            return 0;
        // The starting point.
        return rotate(key[order.get(idx)], rotation);
    }

    public byte getRotated(final int key)
    {
        return get(rotate(key, ((-rotation) & 3) | rotation & -4));
    }
    public byte getRotated(final int key, final int rotation)
    {
        return get(rotate(key, ((-rotation) & 3) | rotation & -4));
    }
    public byte getRotated(final int x, final int y, final int z)
    {
        return get(rotate(fuse(x, y, z), ((-rotation) & 3) | rotation & -4));
    }
    public byte getRotated(final int x, final int y, final int z, final int rotation)
    {
        return get(rotate(fuse(x, y, z), ((-rotation) & 3) | rotation & -4));
    }
    
    public int rotate(final int k, final int rotation)
    {
        switch (rotation)
        {
            // 0-3 have z pointing towards z+ and the voxels rotating on that axis
            case 0: return k;
            case 1: return (k & 0x3FF00000) | sizeX - (k & 0x3FF) << 10 | (k >>> 10 & 0x3FF);
            case 2: return (k & 0x3FF00000) | (sizeY << 10) - (k & 0xFFC00) | sizeX - (k & 0x3FF);
            case 3: return (k & 0x3FF00000) | (k & 0x3FF) << 10 | (sizeY - (k >>> 10 & 0x3FF));
            // 4-7 have z pointing towards y+ and the voxels rotating on that axis
            case 4: return (k >>> 10 & 0x000FFC00) | (sizeY << 10) - (k & 0x000FFC00) << 10 | (k & 0x3FF);
            case 5: return (k >>> 10 & 0x000FFC00) | (k & 0x3FF) << 20 | (k >>> 10 & 0x3FF);
            case 6: return (k >>> 10 & 0x000FFC00) | (k & 0x000FFC00) << 10 | sizeX - (k & 0x3FF);
            case 7: return (k >>> 10 & 0x000FFC00) | (sizeX - (k & 0x3FF) << 20) | sizeY - (k >>> 10 & 0x3FF);
            // 8-11 have z pointing towards z-
            case 8: return (sizeZ << 20) - (k & 0x3FF00000) | (k & 0xFFC00) | (k & 0x3FF);
            case 9: return (sizeZ << 20) - (k & 0x3FF00000) |(sizeY) - (k >>> 10 & 0x3FF) | (k & 0x3FF) << 10;
            case 10: return (sizeZ << 20) - (k & 0x3FF00000) | (sizeY << 10) - (k & 0xFFC00) | sizeX - (k & 0x3FF);
            case 11: return (sizeZ << 20) - (k & 0x3FF00000) | (k >>> 10 & 0x3FF) | sizeX - (k & 0x3FF) << 10;
            // 12-15 have z pointing towards y-
            case 12: return (sizeZ << 10) - (k >>> 10 & 0x000FFC00) | (k & 0x000FFC00) << 10 | (k & 0x3FF);
            case 13: return (sizeZ << 10) - (k >>> 10 & 0x000FFC00) | sizeX - (k & 0x3FF) << 20 | (k >>> 10 & 0x3FF);
            case 14: return (sizeZ << 10) - (k >>> 10 & 0x000FFC00) | (sizeY << 20) - (k << 10 & 0x3FF00000) | sizeX - (k & 0x3FF);
            case 15: return (sizeZ << 10) - (k >>> 10 & 0x000FFC00) | (k & 0x3FF) << 20 | sizeY - (k >>> 10 & 0x3FF);
            // 16-19 have z pointing towards x+ and the voxels rotating on that axis
            case 16: return (k >>> 20 & 0x3FF) | (k & 0x000FFC00) | (k << 20 & 0x3FF00000);
            case 17: return (k >>> 20 & 0x3FF) | (k << 10 & 0x3FF00000) | (sizeX - (k & 0x3FF) << 10);
            case 18: return (k >>> 20 & 0x3FF) | (sizeY << 10) - (k & 0x000FFC00) | (sizeX - (k & 0x3FF)) << 20;
            case 19: return (k >>> 20 & 0x3FF) | (sizeY << 20) - (k << 10 & 0x3FF00000) | (k << 10 & 0x000FFC00);
            // 20-23 have z pointing towards x- and the voxels rotating on that axis
            case 20: return sizeZ - (k >>> 20 & 0x3FF) | (k & 0x000FFC00) | (k << 20 & 0x3FF00000);
            case 21: return sizeZ - (k >>> 20 & 0x3FF) | (k << 10 & 0x3FF00000) | (sizeX - (k & 0x3FF) << 10);
            case 22: return sizeZ - (k >>> 20 & 0x3FF) | (sizeY << 10) - (k & 0x000FFC00) | (sizeX - (k & 0x3FF)) << 20;
            case 23: return sizeZ - (k >>> 20 & 0x3FF) | (sizeY << 20) - (k << 10 & 0x3FF00000) | (k << 10 & 0x000FFC00);
            default:
//                System.out.println("this shouldn't be happening! " + k);
                return 0;
        }

    }

    @Override
    public SlopeSeq counterX() {
        final int r = rotation();
        switch (r & 28) { // 16, 8, 4
            case 0:
            case 8:
                rotate(r ^ 4);
                break;
            case 12:
            case 4:
                rotate(r ^ 12);
                break;
            case 16:
                rotate((r + 1 & 3) | 16);
                break;
            case 20:
                rotate((r - 1 & 3) | 20);
                break;
        }
        return this;
    }
    
    @Override
    public SlopeSeq counterY() {
        final int r = rotation();
        switch (r & 28) // 16, 8, and 4 can each be set.
        {
            case 0:
                rotate((r & 3) | 20);
                break;
            case 4:
                rotate((r - 1 & 3) | (r & 12));
                break;
            case 8:
                rotate((2-r & 3) | 16);
                break;
            case 12:
                rotate((r + 1 & 3) | (r & 12));
                break;
            case 16:
                rotate(-r & 3);
                break;
            case 20:
                rotate((2+r & 3) | 8);
                break;
        }
        return this;
    }

    @Override
    public SlopeSeq counterZ() {
        rotate((rotation() - 1 & 3) | (rotation() & 28));
        return this;
    }
    @Override
    public SlopeSeq clockX() {
        final int r = rotation();
        switch (r & 28) {
            case 4:
            case 12:
                rotate(r ^ 4);
                break;
            case 0:
            case 8:
                rotate(r ^ 12);
                break;
            case 16:
                rotate((r - 1 & 3) | 16);
                break;
            case 20:
                rotate((r + 1 & 3) | 20);
                break;
        }
        return this;
    }

    @Override
    public SlopeSeq clockY() {
        final int r = rotation();
        switch (r & 28) // 16, 8, and 4 can each be set.
        {
            case 0:
                rotate((-r & 3) | 16);
                break;
            case 4:
                rotate((r + 1 & 3) | (r & 12));
                break;
            case 8:
                rotate((2+r & 3) | 20);
                break;
            case 12:
                rotate((r - 1 & 3) | (r & 12));
                break;
            case 16:
                rotate((2-r & 3) | 8);
                break;
            case 20:
                rotate(r & 3);
                break;
        }
        return this;
    }

    @Override
    public SlopeSeq clockZ() {
        rotate((rotation() + 1 & 3) | (rotation() & 28));
        return this;
    }

    @Override
    public SlopeSeq reset() {
        rotate(0);
        return this;
    }

//    @Override
    public float angleX() {
        return 90f;
    }

//    @Override
    public float angleY() {
        return 90f;
    }

//    @Override
    public float angleZ() {
        return 90f;
    }

    protected void pass0()
    {
        final int sz = order.size;
        final int[] arr = order.items;
        int current, x, y, z;
        byte color;
        byte[] neighbors = new byte[27];
        for (int i = 0; i < sz; i++) {
            current = arr[i];
            color = value[current];
            current = key[current];
            
            x = extractX(current);
            y = extractY(current);
            z = extractZ(current);
            for (int xx = x-1, xi = 0; xi < 27; xi+=9, xx++) {
                for (int yy = y-1, yi = 0; yi < 9; yi+=3, yy++) {
                    for (int zz = z - 1, zi = 0; zi < 3; zi++, zz++) {
                        neighbors[xi + yi + zi] = get(xx, yy, zz);
                    }
                }
            }
            if(neighbors[4] == 0 && x > 0)
            {
                if(neighbors[1] == color && neighbors[7] == 0)
                {
                    if(neighbors[3] == 0 && neighbors[5] == 0)
                    {
                        put(x-1,y,z, color, (byte) (1<<2|1<<6));
                    }
                }
            }
            // this is going to take a while to write... all of the slopes will need their own checks.
        }
    }
    
    public SlopeSeq addSlopes()
    {
        pass0();
        return this;
    }
}
