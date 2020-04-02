/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package warpwriter.model;

import com.badlogic.gdx.math.MathUtils;
import squidpony.squidmath.IntVLA;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static warpwriter.model.nonvoxel.HashMap3D.fuse;

/** An unordered map where the keys are unboxed ints and values are unboxed floats. No allocation is done except when growing the
 * table size.
 * <p>
 * This class performs fast contains and remove (typically O(1), worst case O(n) but that is rare in practice). Add may be
 * slightly slower, depending on hash collisions. Hashcodes are rehashed to reduce collisions and the need to resize. Load factors
 * greater than 0.91 greatly increase the chances to resize to the next higher POT size.
 * <p>
 * Unordered sets and maps are not designed to provide especially fast iteration. Iteration is faster with OrderedSet and
 * OrderedMap.
 * <p>
 * This implementation uses linear probing with the backward shift algorithm for removal. Hashcodes are rehashed using Fibonacci
 * hashing, instead of the more common power-of-two mask, to better distribute poor hashCodes (see <a href=
 * "https://probablydance.com/2018/06/16/fibonacci-hashing-the-optimization-that-the-world-forgot-or-a-better-alternative-to-integer-modulo/">Malte
 * Skarupke's blog post</a>). Linear probing continues to work even when all hashCodes collide, just more slowly.
 * @author Nathan Sweet
 * @author Tommy Ettinger */
public class SlopeSeq implements Iterable<SlopeSeq.Entry> {
	public final IntVLA order;
	
	public int size;

	int[] keyTable;
	byte[] valueTable;
	byte[] slopeTable;

	byte zeroValue;
	byte zeroSlope;
	boolean hasZeroValue;

	private final float loadFactor;
	private int threshold;

	/** Used by {@link #place(int)} to bit shift the upper bits of a {@code long} into a usable range (&gt;= 0 and &lt;=
	 * {@link #mask}). The shift can be negative, which is convenient to match the number of bits in mask: if mask is a 7-bit
	 * number, a shift of -7 shifts the upper 7 bits into the lowest 7 positions. This class sets the shift &gt; 32 and &lt; 64,
	 * which if used with an int will still move the upper bits of an int to the lower bits due to Java's implicit modulus on
	 * shifts.
	 * <p>
	 * {@link #mask} can also be used to mask the low bits of a number, which may be faster for some hashcodes, if
	 * {@link #place(int)} is overridden. */
	protected int shift;

	/** A bitmask used to confine hashcodes to the size of the table. Must be all 1 bits in its low positions, ie a power of two
	 * minus 1. If {@link #place(int)} is overriden, this can be used instead of {@link #shift} to isolate usable bits of a
	 * hash. */
	protected int mask;

	private Entries entries1, entries2;
	private Keys keys1, keys2;
	/**
	 * Maximum size on the x-dimension for keys.
	 */
	public int sizeX = 32;

	/**
	 * Maximum size on the y-dimension for keys.
	 */
	public int sizeY = 32;

	/**
	 * Maximum size on the z-dimension for keys.
	 */
	public int sizeZ = 32;

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

	public void sizeZ(int sizeZ) {
		this.sizeZ = sizeZ;
	}

	public int rotation() {
		return rotation;
	}

	public void rotate(int rotation) {
		this.rotation = rotation;
	}

	/** Creates a new map with an initial capacity of 51 and a load factor of 0.8. */
	public SlopeSeq() {
		this(51, 0.8f);
	}

	/** Creates a new map with a load factor of 0.8.
	 *
	 * @param initialCapacity If not a power of two, it is increased to the next nearest power of two. */
	public SlopeSeq(int initialCapacity) {
		this(initialCapacity, 0.8f);
	}

	/** Creates a new map with the specified initial capacity and load factor. This map will hold initialCapacity items before
	 * growing the backing table.
	 * @param initialCapacity If not a power of two, it is increased to the next nearest power of two. */
	public SlopeSeq(int initialCapacity, float loadFactor) {
		if (loadFactor <= 0f || loadFactor >= 1f)
			throw new IllegalArgumentException("loadFactor must be > 0 and < 1: " + loadFactor);
		this.loadFactor = loadFactor;

		int tableSize = tableSize(initialCapacity, loadFactor);
		threshold = (int)(tableSize * loadFactor);
		mask = tableSize - 1;
		shift = Long.numberOfLeadingZeros(mask);

		keyTable = new int[tableSize];
		valueTable = new byte[tableSize];
		slopeTable = new byte[tableSize];
		order = new IntVLA(initialCapacity);
	}

	/** Creates a new map identical to the specified map. */
	public SlopeSeq(SlopeSeq map) {
		this.loadFactor = map.loadFactor;

		int tableSize = tableSize((int)(map.keyTable.length * map.loadFactor), loadFactor);
		threshold = (int)(tableSize * loadFactor);
		mask = tableSize - 1;
		shift = Long.numberOfLeadingZeros(mask);

		keyTable = new int[tableSize];
		valueTable = new byte[tableSize];
		slopeTable = new byte[tableSize];

		System.arraycopy(map.keyTable, 0, keyTable, 0, map.keyTable.length);
		System.arraycopy(map.valueTable, 0, valueTable, 0, map.valueTable.length);
		System.arraycopy(map.slopeTable, 0, slopeTable, 0, map.slopeTable.length);
		size = map.size;
		zeroValue = map.zeroValue;
		zeroSlope = map.zeroSlope;
		hasZeroValue = map.hasZeroValue;
		order = new IntVLA(map.order);
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
						put(x, y, z, voxels[x][y][z], -1);
				}
			}
		}
	}

	/** Returns an index >= 0 and <= {@link #mask} for the specified {@code item}.
	 * <p>
	 * The default implementation uses Fibonacci hashing on the item's {@link Object#hashCode()}: the hashcode is multiplied by a
	 * long constant (2 to the 64th, divided by the golden ratio) then the uppermost bits are shifted into the lowest positions to
	 * obtain an index in the desired range. Multiplication by a long may be slower than int (eg on GWT) but greatly improves
	 * rehashing, allowing even very poor hashcodes, such as those that only differ in their upper bits, to be used without high
	 * collision rates. Fibonacci hashing has increased collision rates when all or most hashcodes are multiples of larger
	 * Fibonacci numbers (see <a href=
	 * "https://probablydance.com/2018/06/16/fibonacci-hashing-the-optimization-that-the-world-forgot-or-a-better-alternative-to-integer-modulo/">Malte
	 * Skarupke's blog post</a>).
	 * <p>
	 * This method can be overriden to customize hashing. This may be useful eg in the unlikely event that most hashcodes are
	 * Fibonacci numbers, if keys provide poor or incorrect hashcodes, or to simplify hashing if keys provide high quality
	 * hashcodes and don't need Fibonacci hashing: {@code return item.hashCode() & mask;} */
	protected int place (int item) {
		return (int)(item * 0x9E3779B97F4A7C15L >>> shift);
	}

	/** Returns the index of the key if already present, else ~index for the next empty index. This can be overridden in this
	 * pacakge to compare for equality differently than {@link Object#equals(Object)}. */
	private int locateKey (int key) {
		int[] keyTable = this.keyTable;
		for (int i = place(key);; i = i + 1 & mask) {
			int other = keyTable[i];
			if (other == 0) return ~i; // Empty space is available.
			if (other == key) return i; // Same key was found.
		}
	}
	public void put (int x, int y, int z, int value, int slope) {
		if(x < 0 || x >= sizeX || y < 0 || y >= sizeY || z < 0 || z >= sizeZ) return;
		put(fuse(x, y, z), value, slope);
	}
	public void put (int key, int value, int slope) {
		if (key == 0) {
			zeroValue = (byte) value;
			zeroSlope = (byte) slope;
			if (!hasZeroValue) {
				hasZeroValue = true;
				size++;
			}
			return;
		}
		int i = locateKey(key);
		if (i >= 0) { // Existing key was found.
			valueTable[i] = (byte) value;
			slopeTable[i] = (byte) slope;
			return;
		}
		i = ~i; // Empty space was found.
		keyTable[i] = key;
		valueTable[i] = (byte) value;
		slopeTable[i] = (byte) slope;
		order.add(key);
		if (++size >= threshold) resize(keyTable.length << 1);
	}

	public void putAll (SlopeSeq map) {
		ensureCapacity(map.size);
		if (map.hasZeroValue) put(0, map.zeroValue, map.zeroSlope);
		int[] keyTable = map.keyTable;
		byte[] valueTable = map.valueTable;
		byte[] slopeTable = map.slopeTable;
		for (int i = 0, n = keyTable.length; i < n; i++) {
			int key = keyTable[i];
			if (key != 0) put(key, valueTable[i], slopeTable[i]);
		}
	}

	/** Skips checks for existing keys, doesn't increment size, doesn't need to handle key 0. */
	private void putResize (int key, byte value, byte slope) {
		int[] keyTable = this.keyTable;
		for (int i = place(key);; i = (i + 1) & mask) {
			if (keyTable[i] == 0) {
				keyTable[i] = key;
				valueTable[i] = value;
				slopeTable[i] = slope;
				return;
			}
		}
	}

	public byte get(int x, int y, int z) {
		if(x < 0 || x >= sizeX || y < 0 || y >= sizeY || z < 0 || z >= sizeZ) return 0;
		return get(fuse(x, y, z));
	}
	public byte get(int key) {
		if (key == 0) return hasZeroValue ? zeroValue : 0;
		int i = locateKey(key);
		return i >= 0 ? valueTable[i] : 0;
	}

	public byte getSlope(int x, int y, int z) {
		if(x < 0 || x >= sizeX || y < 0 || y >= sizeY || z < 0 || z >= sizeZ) return 0;
		return getSlope(fuse(x, y, z));
	}
	public byte getSlope(int key) {
		if (key == 0) return hasZeroValue ? zeroSlope :  0;
		int i = locateKey(key);
		return i >= 0 ? slopeTable[i] : 0;
	}
	
	public byte remove(int x, int y, int z) {
		if(x < 0 || x >= sizeX || y < 0 || y >= sizeY || z < 0 || z >= sizeZ) return 0;
		return remove(fuse(x, y, z));
	}
	public byte remove(int key) {
		if (key == 0) {
			if (!hasZeroValue) return 0;
			order.removeValue(key);
			hasZeroValue = false;
			size--;
			return zeroValue;
		}

		int i = locateKey(key);
		if (i < 0) return 0;
		order.removeValue(key);
		int[] keyTable = this.keyTable;
		byte[] valueTable = this.valueTable;
		byte[] slopeTable = this.slopeTable;
		byte oldValue = valueTable[i];
		int mask = this.mask, next = i + 1 & mask;
		while ((key = keyTable[next]) != 0) {
			int placement = place(key);
			if ((next - placement & mask) > (i - placement & mask)) {
				keyTable[i] = key;
				valueTable[i] = valueTable[next];
				slopeTable[i] = slopeTable[next];
				i = next;
			}
			next = next + 1 & mask;
		}
		keyTable[i] = 0;
		size--;
		return oldValue;
	}

	/** Returns true if the map has one or more items. */
	public boolean notEmpty () {
		return size > 0;
	}

	/** Returns true if the map is empty. */
	public boolean isEmpty () {
		return size == 0;
	}

	/** Reduces the size of the backing arrays to be the specified capacity / loadFactor, or less. If the capacity is already less,
	 * nothing is done. If the map contains more items than the specified capacity, the next highest power of two capacity is used
	 * instead. */
	public void shrink (int maximumCapacity) {
		if (maximumCapacity < 0) throw new IllegalArgumentException("maximumCapacity must be >= 0: " + maximumCapacity);
		int tableSize = tableSize(maximumCapacity, loadFactor);
		if (keyTable.length > tableSize) resize(tableSize);
	}

	/** Clears the map and reduces the size of the backing arrays to be the specified capacity / loadFactor, if they are larger. */
	public void clear (int maximumCapacity) {
		int tableSize = tableSize(maximumCapacity, loadFactor);
		if (keyTable.length <= tableSize) {
			clear();
			return;
		}
		order.clear();
		size = 0;
		hasZeroValue = false;
		resize(tableSize);
	}

	public void clear () {
		if (size == 0) return;
		Arrays.fill(keyTable, 0);
		size = 0;
		hasZeroValue = false;
		order.clear();
	}

	public int keyAt(int index){
		return order.get(index);
	}
	public byte getAt(int index){
		return get(order.get(index));
	}
	public byte getSlopeAt(int index){
		return getSlope(order.get(index));
	}
	public byte removeAt(int index) {
		return remove(order.removeIndex(index));
	}

	/** Changes the key {@code before} to {@code after} without changing its position in the order or its value. Returns true if
	 * {@code after} has been added to the OrderedMap and {@code before} has been removed; returns false if {@code after} is
	 * already present or {@code before} is not present. If you are iterating over an OrderedMap and have an index, you should
	 * prefer {@link #alterAt(int, int)}, which doesn't need to search for an index like this does and so can be faster.
	 * @param before a key that must be present for this to succeed
	 * @param after a key that must not be in this map for this to succeed
	 * @return true if {@code before} was removed and {@code after} was added, false otherwise */
	public boolean alter (int before, int after) {
		if (containsKey(after)) return false;
		int index = order.indexOf(before);
		if (index == -1) return false;
		byte slope = getSlope(before);
		put(after, remove(before), slope);
		order.set(index, after);
		return true;
	}

	/** Changes the key at the given {@code index} in the order to {@code after}, without changing the ordering of other entries or
	 * any values. If {@code after} is already present, this returns false; it will also return false if {@code index} is invalid
	 * for the size of this map. Otherwise, it returns true. Unlike {@link #alter(int, int)}, this operates in constant time.
	 * @param index the index in the order of the key to change; must be non-negative and less than {@link #size}
	 * @param after the key that will replace the contents at {@code index}; this key must not be present for this to succeed
	 * @return true if {@code after} successfully replaced the key at {@code index}, false otherwise */
	public boolean alterAt (int index, int after) {
		if (index < 0 || index >= size || containsKey(after)) return false;
		int before = order.get(index);
		byte slope = getSlope(before);
		put(after, remove(before), slope);
		order.set(index, after);
		return true;
	}
	public boolean containsKey(int x, int y, int z) {
		if(x < 0 || x >= sizeX || y < 0 || y >= sizeY || z < 0 || z >= sizeZ) return false;
		return containsKey(fuse(x, y, z));
	}

	public boolean containsKey (int key) {
		if (key == 0) return hasZeroValue;
		return locateKey(key) >= 0;
	}

	/** Increases the size of the backing array to accommodate the specified number of additional items / loadFactor. Useful before
	 * adding many items to avoid multiple backing array resizes. */
	public void ensureCapacity (int additionalCapacity) {
		int tableSize = tableSize(size + additionalCapacity, loadFactor);
		if (keyTable.length < tableSize) resize(tableSize);
	}

	private void resize (int newSize) {
		int oldCapacity = keyTable.length;
		threshold = (int)(newSize * loadFactor);
		mask = newSize - 1;
		shift = Long.numberOfLeadingZeros(mask);

		int[] oldKeyTable = keyTable;
		byte[] oldValueTable = valueTable;
		byte[] oldSlopeTable = slopeTable;

		keyTable = new int[newSize];
		valueTable = new byte[newSize];

		if (size > 0) {
			for (int i = 0; i < oldCapacity; i++) {
				int key = oldKeyTable[i];
				if (key != 0) putResize(key, oldValueTable[i], oldSlopeTable[i]);
			}
		}
	}

	static int tableSize (int capacity, float loadFactor) {
		if (capacity < 0) throw new IllegalArgumentException("capacity must be >= 0: " + capacity);
		int tableSize = MathUtils.nextPowerOfTwo(Math.max(2, (int)Math.ceil(capacity / loadFactor)));
		if (tableSize > 1 << 30) throw new IllegalArgumentException("The required capacity is too large: " + capacity);
		return tableSize;
	}

	public int hashCode () {
		int h = size;
		if (hasZeroValue) h += ((zeroValue + zeroSlope * 421) * 421);
		int[] keyTable = this.keyTable;
		byte[] valueTable = this.valueTable;
		byte[] slopeTable = this.slopeTable;
		for (int i = 0, n = keyTable.length; i < n; i++) {
			int key = keyTable[i];
			if (key != 0) h += key + ((valueTable[i] + slopeTable[i] * 421) * 421);
		}
		return h;
	}

	public boolean equals (Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof SlopeSeq)) return false;
		SlopeSeq other = (SlopeSeq)obj;
		if (other.size != size) return false;
		if (other.hasZeroValue != hasZeroValue) return false;
		if (hasZeroValue) {
			if (other.zeroValue != zeroValue) return false;
			if (other.zeroSlope != zeroSlope) return false;
		}
		int[] keyTable = this.keyTable;
		byte[] valueTable = this.valueTable;
		byte[] slopeTable = this.slopeTable;
		for (int i = 0, n = keyTable.length; i < n; i++) {
			int key = keyTable[i];
			if (key != 0) {
				byte otherValue = other.get(key);
				byte otherSlope = other.getSlope(key);
				if (otherValue == 0) return false;
				if (otherValue != valueTable[i]) return false;
				if (otherSlope != slopeTable[i]) return false;
			}
		}
		return true;
	}

	public String toString () {
		if (size == 0) return "[]";
		java.lang.StringBuilder buffer = new java.lang.StringBuilder(32);
		buffer.append('[');
		int[] keyTable = this.keyTable;
		byte[] valueTable = this.valueTable;
		byte[] slopeTable = this.slopeTable;
		int i = keyTable.length;
		if (hasZeroValue) {
			buffer.append("0=(").append(zeroValue).append(',').append(zeroSlope).append(')');
		} else {
			while (i-- > 0) {
				int key = keyTable[i];
				if (key == 0) continue;
				buffer.append(key).append("=(").append(valueTable[i]).append(',').append(slopeTable[i]).append(')');
				break;
			}
		}
		while (i-- > 0) {
			int key = keyTable[i];
			if (key != 0) 
				buffer.append(", ").append(key).append("=(").append(valueTable[i]).append(',').append(slopeTable[i]).append(')');
		}
		buffer.append(']');
		return buffer.toString();
	}

	public Iterator<Entry> iterator () {
		return entries();
	}

	/** Returns an iterator for the entries in the map. Remove is supported.
	 * <p>
	 * Use the {@link Entries} constructor for nested or multithreaded iteration. */
	public Entries entries () {
		if (entries1 == null) {
			entries1 = new Entries(this);
			entries2 = new Entries(this);
		}
		if (!entries1.valid) {
			entries1.reset();
			entries1.valid = true;
			entries2.valid = false;
			return entries1;
		}
		entries2.reset();
		entries2.valid = true;
		entries1.valid = false;
		return entries2;
	}

	/** Returns an iterator for the keys in the map. Remove is supported.
	 * <p>
	 * Use the {@link Entries} constructor for nested or multithreaded iteration. */
	public Keys keys () { 
		if (keys1 == null) {
			keys1 = new Keys(this);
			keys2 = new Keys(this);
		}
		if (!keys1.valid) {
			keys1.reset();
			keys1.valid = true;
			keys2.valid = false;
			return keys1;
		}
		keys2.reset();
		keys2.valid = true;
		keys1.valid = false;
		return keys2;
	}

	static public class Entry {
		public int key;
		public byte value;
		public byte slope;

		public String toString () {
			return key + "=(" + value + "," + slope + ")";
		}
	}
	
	static public class Entries implements Iterable<Entry>, Iterator<Entry> {
		private final Entry entry = new Entry();
		final SlopeSeq map;
		final IntVLA keys;
		public boolean hasNext;

		int nextIndex, currentIndex;
		boolean valid = true;

		public Entries(SlopeSeq map) {
			this.map = map;
			this.keys = map.order;
			reset();
		}

		public void reset() {
			currentIndex = -1;
			nextIndex = 0;
			hasNext = map.size > 0;
		}

		public Entry next() {
			if (!hasNext) throw new NoSuchElementException();
			if (!valid) throw new RuntimeException("#iterator() cannot be used nested.");
			currentIndex = nextIndex;
			entry.key = keys.get(nextIndex);
			entry.value = map.get(entry.key);
			entry.slope = map.getSlope(entry.key);
			nextIndex++;
			hasNext = nextIndex < map.size;
			return entry;
		}

		public void remove() {
			if (currentIndex < 0) throw new IllegalStateException("next must be called before remove.");
			map.remove(entry.key);
			nextIndex--;
			currentIndex = -1;
		}

		public boolean hasNext() {
			if (!valid) throw new RuntimeException("#iterator() cannot be used nested.");
			return hasNext;
		}

		public Iterator<Entry> iterator() {
			return this;
		}
	}

	static public class Keys {
		final SlopeSeq map;
		final IntVLA keys;
		public boolean hasNext;

		int nextIndex, currentIndex;
		boolean valid = true;

		public Keys (SlopeSeq map) {
			this.map = map;
			this.keys = map.order;
			reset();
		}
		
		public void reset () {
			currentIndex = -1;
			nextIndex = 0;
			hasNext = map.size > 0;
		}

		public int next () {
			if (!hasNext) throw new NoSuchElementException();
			if (!valid) throw new RuntimeException("#iterator() cannot be used nested.");
			int key = keys.get(nextIndex);
			currentIndex = nextIndex;
			nextIndex++;
			hasNext = nextIndex < map.size;
			return key;
		}

		public void remove () {
			if (currentIndex < 0) throw new IllegalStateException("next must be called before remove.");
			map.removeAt(currentIndex);
			nextIndex = currentIndex;
			currentIndex = -1;
		}

		public IntVLA toArray (IntVLA array) {
			array.addAll(keys, nextIndex, keys.size - nextIndex);
			nextIndex = keys.size;
			hasNext = false;
			return array;
		}

		public IntVLA toArray () {
			return toArray(new IntVLA(keys.size - nextIndex));
		}	
	}
}
