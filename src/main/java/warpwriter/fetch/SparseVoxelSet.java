
/*******************************************************************************
 * Copyright 2011 See LibGDX AUTHORS file.
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

package warpwriter.fetch;

import squidpony.squidmath.*;

import java.io.Serializable;
import java.util.NoSuchElementException;

/** An unordered set that uses int keys to represent voxels, considering 24 bits of each key a position and using this
 * for lookups, and the last 8 bits a color that is not considered in comparisons.
 * This implementation uses cuckoo hashing using 3 hashes, random walking, and a
 * small stash for problematic keys. No allocation is done except when growing the table size.
 * <br>
 * This set performs very fast contains and remove (typically O(1), worst case O(log(n))). Add may be a bit slower,
 * depending on hash collisions. Load factors greater than 0.91 greatly increase the chances the set will have to rehash
 * to the next higher POT size.
 * @author Nathan Sweet
 * Ported from libGDX by Tommy Ettinger on 10/19/2015.
 * Ported again from SquidLib by Tommy Ettinger on 10/11/2018
 */
public class SparseVoxelSet implements Serializable, IFetch {
    private static final long serialVersionUID = 2L;

    private static final int PRIME2 = 0xB7659;//0xb4b82e39;
    private static final int PRIME3 = 0xCFEB1;//0xced1c241;
    private static final int EMPTY = 0;

    public int size;

    int[] keyTable;
    int capacity, stashSize;
    boolean hasZeroValue;

    private float loadFactor;
    private int hashShift, threshold;
    private int stashCapacity;
    private int pushIterations;
    private int mask;
    private static long rngState;

    private SparseVoxelSetIterator iterator1, iterator2;

    /** Creates a new sets with an initial capacity of 32 and a load factor of 0.8. This set will hold 25 items before growing the
     * backing table. */
    public SparseVoxelSet() {
        this(32, 0.8f);
    }

    /** Creates a new set with a load factor of 0.8. This set will hold initialCapacity * 0.8 items before growing the backing
     * table. */
    public SparseVoxelSet(int initialCapacity) {
        this(initialCapacity, 0.8f);
    }

    /** Creates a new set with the specified initial capacity and load factor. This set will hold initialCapacity * loadFactor items
     * before growing the backing table. */
    public SparseVoxelSet(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0) throw new IllegalArgumentException("initialCapacity must be >= 0: " + initialCapacity);
        if (initialCapacity > 1 << 30) throw new IllegalArgumentException("initialCapacity is too large: " + initialCapacity);
        capacity = nextPowerOfTwo(initialCapacity);

        rngState = (long) ((Math.random() - 0.5) * 0x10000000000000L)
                ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L);

        if (loadFactor <= 0) throw new IllegalArgumentException("loadFactor must be > 0: " + loadFactor);
        this.loadFactor = loadFactor;

        threshold = (int)(capacity * loadFactor);
        mask = capacity - 1;
        hashShift = 31 - Integer.numberOfTrailingZeros(capacity);
        stashCapacity = Math.max(3, (int)Math.ceil(Math.log(capacity)) * 2);
        pushIterations = Math.max(Math.min(capacity, 8), (int)Math.sqrt(capacity) / 8);

        keyTable = new int[capacity + stashCapacity];
    }

    /** Creates a new map identical to the specified map. */
    public SparseVoxelSet(SparseVoxelSet map) {
        this(map.capacity, map.loadFactor);
        stashSize = map.stashSize;
        System.arraycopy(map.keyTable, 0, keyTable, 0, map.keyTable.length);
        size = map.size;
        hasZeroValue = map.hasZeroValue;
    }

    public boolean add (int x, int y, int z, byte color) {
        if (color == 0) {
            return false;
        }
        return add((x & 0xFF) | (y & 0xFF) << 8 | (z & 0xFF) << 16 | color << 24);
    }
    public boolean add (int full) {
        
        int[] keyTable = this.keyTable;
        int key = full & 0xFFFFFF;
        // Check for existing keys.
        int index1 = key & mask;
        int key1 = keyTable[index1];
        if ((key1 & 0xFFFFFF) == key) 
        {
            keyTable[index1] = full;
            return key1 == full;
        }

        int index2 = hash2(key);
        int key2 = keyTable[index2];
        if ((key2 & 0xFFFFFF) == key)
        {
            keyTable[index2] = full;
            return key2 == full;
        }

        int index3 = hash3(key);
        int key3 = keyTable[index3];
        if ((key3 & 0xFFFFFF) == key)
        {
            keyTable[index3] = full;
            return key3 == full;
        }

        // Find key in the stash.
        for (int i = capacity, n = i + stashSize; i < n; i++)
            if (keyTable[i] == full) return false;

        // Check for empty buckets.
        if (key1 == EMPTY) {
            keyTable[index1] = full;
            if (size++ >= threshold) resize(capacity << 1);
            return true;
        }

        if (key2 == EMPTY) {
            keyTable[index2] = full;
            if (size++ >= threshold) resize(capacity << 1);
            return true;
        }

        if (key3 == EMPTY) {
            keyTable[index3] = full;
            if (size++ >= threshold) resize(capacity << 1);
            return true;
        }

        push(full, index1, key1, index2, key2, index3, key3);
        return true;
    }

    public void addAll (IntVLA array) {
        addAll(array, 0, array.size);
    }

    public void addAll (IntVLA array, int offset, int length) {
        if (offset + length > array.size)
            throw new IllegalArgumentException("offset + length must be <= size: " + offset + " + " + length + " <= " + array.size);
        addAll(array.items, offset, length);
    }

    public void addAll (int... array) {
        addAll(array, 0, array.length);
    }

    public void addAll (int[] array, int offset, int length) {
        ensureCapacity(length);
        for (int i = offset, n = i + length; i < n; i++)
            add(array[i]);
    }

    public void addAll (SparseVoxelSet set) {
        ensureCapacity(set.size);
        SparseVoxelSetIterator iterator = set.iterator();
        while (iterator.hasNext)
            add(iterator.next());
    }

    /** Skips checks for existing keys. */
    private void addResize (int key) {
        if (key == 0) {
            hasZeroValue = true;
            return;
        }

        // Check for empty buckets.
        int index1 = key & mask;
        int key1 = keyTable[index1];
        if (key1 == EMPTY) {
            keyTable[index1] = key;
            if (size++ >= threshold) resize(capacity << 1);
            return;
        }

        int index2 = hash2(key);
        int key2 = keyTable[index2];
        if (key2 == EMPTY) {
            keyTable[index2] = key;
            if (size++ >= threshold) resize(capacity << 1);
            return;
        }

        int index3 = hash3(key);
        int key3 = keyTable[index3];
        if (key3 == EMPTY) {
            keyTable[index3] = key;
            if (size++ >= threshold) resize(capacity << 1);
            return;
        }

        push(key, index1, key1, index2, key2, index3, key3);
    }

    private void push (int insertKey, int index1, int key1, int index2, int key2, int index3, int key3) {
        int[] keyTable = this.keyTable;

        int mask = this.mask;

        // Push keys until an empty bucket is found.
        int evictedKey;
        int i = 0, pushIterations = this.pushIterations;
        do {
            // Replace the key and value for one of the hashes.
            switch (LinnormRNG.determineBounded(++rngState, 3)) {
                case 0:
                    evictedKey = key1;
                    keyTable[index1] = insertKey;
                    break;
                case 1:
                    evictedKey = key2;
                    keyTable[index2] = insertKey;
                    break;
                default:
                    evictedKey = key3;
                    keyTable[index3] = insertKey;
                    break;
            }

            // If the evicted key hashes to an empty bucket, put it there and stop.
            index1 = evictedKey & mask & 0xFFFFFF;
            key1 = keyTable[index1] & 0xFFFFFF;
            if (key1 == EMPTY) {
                keyTable[index1] = evictedKey;
                if (size++ >= threshold) resize(capacity << 1);
                return;
            }

            index2 = hash2(evictedKey & 0xFFFFFF);
            key2 = keyTable[index2] & 0xFFFFFF;
            if (key2 == EMPTY) {
                keyTable[index2] = evictedKey;
                if (size++ >= threshold) resize(capacity << 1);
                return;
            }

            index3 = hash3(evictedKey & 0xFFFFFF);
            key3 = keyTable[index3] & 0xFFFFFF;
            if (key3 == EMPTY) {
                keyTable[index3] = evictedKey;
                if (size++ >= threshold) resize(capacity << 1);
                return;
            }

            if (++i == pushIterations) break;

            insertKey = evictedKey;
        } while (true);

        addStash(evictedKey);
    }

    private void addStash (int key) {
        if (stashSize == stashCapacity) {
            // Too many pushes occurred and the stash is full, increase the table size.
            resize(capacity << 1);
            add(key);
            return;
        }
        // Store key in the stash.
        int index = capacity + stashSize;
        keyTable[index] = key;
        stashSize++;
        size++;
    }

    /** Returns true if the key was removed. */
    public boolean remove (int key) {
        if ((key & 0xFF000000) == 0) {
            return false;
        }

        int index = key & mask & 0xFFFFFF;
        if (keyTable[index] == key) {
            keyTable[index] = EMPTY;
            size--;
            return true;
        }

        index = hash2(key & 0xFFFFFF);
        if (keyTable[index] == key) {
            keyTable[index] = EMPTY;
            size--;
            return true;
        }

        index = hash3(key & 0xFFFFFF);
        if (keyTable[index] == key) {
            keyTable[index] = EMPTY;
            size--;
            return true;
        }

        return removeStash(key);
    }

    boolean removeStash (int key) {
        int[] keyTable = this.keyTable;
        for (int i = capacity, n = i + stashSize; i < n; i++) {
            if (keyTable[i] == key) {
                removeStashIndex(i);
                size--;
                return true;
            }
        }
        return false;
    }

    void removeStashIndex (int index) {
        // If the removed location was not last, move the last tuple to the removed location.
        stashSize--;
        int lastIndex = capacity + stashSize;
        if (index < lastIndex) keyTable[index] = keyTable[lastIndex];
    }

    /** Reduces the size of the backing arrays to be the specified capacity or less. If the capacity is already less, nothing is
     * done. If the set contains more items than the specified capacity, the next highest power of two capacity is used instead. */
    public void shrink (int maximumCapacity) {
        if (maximumCapacity < 0) throw new IllegalArgumentException("maximumCapacity must be >= 0: " + maximumCapacity);
        if (size > maximumCapacity) maximumCapacity = size;
        if (capacity <= maximumCapacity) return;
        maximumCapacity = nextPowerOfTwo(maximumCapacity);

        resize(maximumCapacity);
    }

    /** Clears the map and reduces the size of the backing arrays to be the specified capacity if they are larger. */
    public void clear (int maximumCapacity) {
        if (capacity <= maximumCapacity) {
            clear();
            return;
        }
        hasZeroValue = false;
        size = 0;
        resize(maximumCapacity);
    }

    public void clear () {
        if (size == 0) return;
        int[] keyTable = this.keyTable;
        for (int i = capacity + stashSize; i-- > 0;)
            keyTable[i] = EMPTY;
        size = 0;
        stashSize = 0;
        hasZeroValue = false;
    }

    public boolean contains (int key) {
        if (key == 0) return hasZeroValue;
        int index = key & mask;
        if (keyTable[index] != key) {
            index = hash2(key);
            if (keyTable[index] != key) {
                index = hash3(key);
                if (keyTable[index] != key) return containsKeyStash(key);
            }
        }
        return true;
    }
    public byte at(int x, int y, int z)
    {
        int key = ((x & 0xFF) | (y & 0xFF) << 8 | (z & 0xFF) << 16),
                index = key & mask;
        if ((keyTable[index] & 0xFFFFFF) != key) {
            index = hash2(key);
            if ((keyTable[index] & 0xFFFFFF) != key) {
                index = hash3(key);
                if ((keyTable[index] & 0xFFFFFF) != key) 
                {
                    return atKeyStash(key);
                }
            }
        }
        return (byte) (keyTable[index]>>>24);        
    }


    private boolean containsKeyStash (int key) {
        int[] keyTable = this.keyTable;
        for (int i = capacity, n = i + stashSize; i < n; i++)
            if (keyTable[i] == key) return true;
        return false;
    }

    private byte atKeyStash (int key) {
        int[] keyTable = this.keyTable;
        for (int i = capacity, n = i + stashSize; i < n; i++)
            if ((keyTable[i] & 0xFFFFFF) == key) return (byte)(keyTable[i]>>>24);
        return 0;
    }

    public int first () {
        if (hasZeroValue) return 0;
        int[] keyTable = this.keyTable;
        for (int i = 0, n = capacity + stashSize; i < n; i++)
            if (keyTable[i] != EMPTY) return keyTable[i];
        throw new IllegalStateException("SparseVoxelSet is empty.");
    }

    /** Increases the size of the backing array to accommodate the specified number of additional items. Useful before adding many
     * items to avoid multiple backing array resizes. */
    public void ensureCapacity (int additionalCapacity) {
        int sizeNeeded = size + additionalCapacity;
        if (sizeNeeded >= threshold) resize(nextPowerOfTwo((int)(sizeNeeded / loadFactor)));
    }

    private void resize (int newSize) {
        int oldEndIndex = capacity + stashSize;

        capacity = newSize;
        threshold = (int)(newSize * loadFactor);
        mask = newSize - 1;
        hashShift = 31 - Integer.numberOfTrailingZeros(newSize);
        stashCapacity = Math.max(3, (int)Math.ceil(Math.log(newSize)) * 2);
        pushIterations = Math.max(Math.min(newSize, 8), (int)Math.sqrt(newSize) / 8);

        int[] oldKeyTable = keyTable;

        keyTable = new int[newSize + stashCapacity];

        int oldSize = size;
        size = hasZeroValue ? 1 : 0;
        stashSize = 0;
        if (oldSize > 0) {
            for (int i = 0; i < oldEndIndex; i++) {
                int key = oldKeyTable[i];
                if (key != EMPTY) addResize(key);
            }
        }
    }

    private int hash2 (int h) {
        h *= PRIME2;
        return (h ^ h >>> hashShift) & mask;
    }

    private int hash3 (int h) {
        h *= PRIME3;
        return (h ^ h >>> hashShift) & mask;
    }

    @Override
	public int hashCode () {
        int h = 0;
        for (int i = 0, n = capacity + stashSize; i < n; i++)
            if (keyTable[i] != EMPTY) h += keyTable[i];
        return h;
    }

    @Override
	public boolean equals (Object obj) {
        if (!(obj instanceof SparseVoxelSet)) return false;
        SparseVoxelSet other = (SparseVoxelSet)obj;
        if (other.size != size) return false;
        if (other.hasZeroValue != hasZeroValue) return false;
        for (int i = 0, n = capacity + stashSize; i < n; i++)
            if (keyTable[i] != EMPTY && !other.contains(keyTable[i])) return false;
        return true;
    }

    @Override
	public String toString () {
        if (size == 0) return "[]";
        StringBuilder buffer = new StringBuilder(32);
        buffer.append('[');
        int[] keyTable = this.keyTable;
        int i = keyTable.length;
        if (hasZeroValue)
            buffer.append("0");
        else {
            while (i-- > 0) {
                int key = keyTable[i];
                if (key == EMPTY) continue;
                buffer.append(key);
                break;
            }
        }
        while (i-- > 0) {
            int key = keyTable[i];
            if (key == EMPTY) continue;
            buffer.append(", ").append(key);
        }
        return buffer.append(']').toString();
    }

    /**
     * Gets a random int from this SparseVoxelSet, using the given {@link IRNG} to generate random values.
     * If this SparseVoxelSet is empty, throws an UnsupportedOperationException. This method operates in linear time, unlike
     * the random item retrieval methods in {@link OrderedSet} and {@link OrderedMap}, which take constant time.
     * @param rng an {@link IRNG}, such as {@link RNG} or {@link GWTRNG}
     * @return a random int from this SparseVoxelSet
     */
    public int random(IRNG rng)
    {
        if (size <= 0) {
            throw new UnsupportedOperationException("SparseVoxelSet cannot be empty when getting a random element");
        }
        int n = rng.nextInt(size);
        int s = 0;
        SparseVoxelSet.SparseVoxelSetIterator ssi = iterator();
        while (n-- >= 0 && ssi.hasNext)
            s = ssi.next();
        ssi.reset();
        return s;

    }

    private static int nextPowerOfTwo(int n)
    {
        int highest = Integer.highestOneBit(n);
        return  (highest == NumberTools.lowestOneBit(n)) ? highest : highest << 1;
    }

    /** Returns an iterator for the keys in the set. Remove is supported. Note that the same iterator instance is returned each time
     * this method is called. Use the {@link SparseVoxelSetIterator} constructor for nested or multithreaded iteration. */
    public SparseVoxelSetIterator iterator () {
        if (iterator1 == null) {
            iterator1 = new SparseVoxelSetIterator(this);
            iterator2 = new SparseVoxelSetIterator(this);
        }
        if (!iterator1.valid) {
            iterator1.reset();
            iterator1.valid = true;
            iterator2.valid = false;
            return iterator1;
        }
        iterator2.reset();
        iterator2.valid = true;
        iterator1.valid = false;
        return iterator2;
    }

    public static SparseVoxelSet with (int... array) {
        SparseVoxelSet set = new SparseVoxelSet();
        set.addAll(array);
        return set;
    }

    public static class SparseVoxelSetIterator {
        static final int INDEX_ILLEGAL = -2;
        static final int INDEX_ZERO = -1;

        public boolean hasNext;

        final SparseVoxelSet set;
        int nextIndex, currentIndex;
        boolean valid = true;

        public SparseVoxelSetIterator(SparseVoxelSet set) {
            this.set = set;
            reset();
        }

        public void reset () {
            currentIndex = INDEX_ILLEGAL;
            nextIndex = INDEX_ZERO;
            if (set.hasZeroValue)
                hasNext = true;
            else
                findNextIndex();
        }

        void findNextIndex () {
            hasNext = false;
            int[] keyTable = set.keyTable;
            for (int n = set.capacity + set.stashSize; ++nextIndex < n;) {
                if (keyTable[nextIndex] != EMPTY) {
                    hasNext = true;
                    break;
                }
            }
        }

        public void remove () {
            if (currentIndex == INDEX_ZERO && set.hasZeroValue) {
                set.hasZeroValue = false;
            } else if (currentIndex < 0) {
                throw new IllegalStateException("next must be called before remove.");
            } else if (currentIndex >= set.capacity) {
                set.removeStashIndex(currentIndex);
                nextIndex = currentIndex - 1;
                findNextIndex();
            } else {
                set.keyTable[currentIndex] = EMPTY;
            }
            currentIndex = INDEX_ILLEGAL;
            set.size--;
        }

        public int next () {
            if (!hasNext) throw new NoSuchElementException();
            if (!valid) throw new RuntimeException("SparseVoxelSetIterator cannot be used nested.");
            int key = nextIndex == INDEX_ZERO ? 0 : set.keyTable[nextIndex];
            currentIndex = nextIndex;
            findNextIndex();
            return key;
        }

        /** Returns a new array containing the remaining keys. */
        public IntVLA toArray () {
            IntVLA array = new IntVLA(set.size);
            while (hasNext)
                array.add(next());
            return array;
        }
    }
}