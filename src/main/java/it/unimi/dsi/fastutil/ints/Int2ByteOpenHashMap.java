/* Generic definitions */
/* Assertions (useful to generate conditional code) */
/* Current type and class (and size, if applicable) */
/* Value methods */
/* Interfaces (keys) */
/* Interfaces (values) */
/* Abstract implementations (keys) */
/* Abstract implementations (values) */
/* Static containers (keys) */
/* Static containers (values) */
/* Implementations */
/* Synchronized wrappers */
/* Unmodifiable wrappers */
/* Other wrappers */
/* Methods (keys) */
/* Methods (values) */
/* Methods (keys/values) */
/* Methods that have special names depending on keys (but the special names depend on values) */
/* Equality */
/* Object/Reference-only definitions (keys) */
/* Primitive-type-only definitions (keys) */
/* Object/Reference-only definitions (values) */
/* Primitive-type-only definitions (values) */
/*		 
 * Copyright (C) 2002-2016 Sebastiano Vigna
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
package it.unimi.dsi.fastutil.ints;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.objects.AbstractObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectSet;

import java.util.*;

import static it.unimi.dsi.fastutil.HashCommon.arraySize;
import static it.unimi.dsi.fastutil.HashCommon.maxFill;

/** A type-specific hash map with a fast, small-footprint implementation.
 *
 * <P>Instances of this class use a hash table to represent a map. The table is
 * filled up to a specified <em>load factor</em>, and then doubled in size to
 * accommodate new entries. If the table is emptied below <em>one fourth</em>
 * of the load factor, it is halved in size. However, halving is
 * not performed when deleting entries from an iterator, as it would interfere
 * with the iteration process.
 *
 * <p>Note that {@link #clear()} does not modify the hash table size. 
 * Rather, a family of {@linkplain #trim() trimming
 * methods} lets you control the size of the table; this is particularly useful
 * if you reuse instances of this class.
 *
 * @see Hash
 * @see HashCommon
 */
public class Int2ByteOpenHashMap implements Map<Integer, Byte>, java.io.Serializable, Cloneable, Hash {

 /** An entry set providing fast iteration. 
  *
  * <p>In some cases (e.g., hash-based classes) iteration over an entry set requires the creation
  * of a large number of {@link Map.Entry} objects. Some <code>fastutil</code>
  * maps might return entry set objects of type <code>FastEntrySet</code>: in this case, {@link #fastIterator() fastIterator()}
  * will return an iterator that is guaranteed not to create a large number of objects, <em>possibly
  * by returning always the same entry</em> (of course, mutated).
  */
 public interface FastEntrySet extends ObjectSet<Entry > {
  /** Returns a fast iterator over this entry set; the iterator might return always the same entry object, suitably mutated.
   *
   * @return a fast iterator over this entry set; the iterator might return always the same {@link Map.Entry} object, suitably mutated.
   */
  public ObjectIterator<Entry > fastIterator();
 }
 /** A type-specific {@link Map.Entry}; provides some additional methods
  *  that use polymorphism to avoid (un)boxing.
  *
  * @see Map.Entry
  */
 interface Entry extends Map.Entry <Integer,Byte> {
  /** {@inheritDoc}
   * @deprecated Please use the corresponding type-specific method instead. */
  @Deprecated
  @Override
  Integer getKey();
  /**
   * @see Map.Entry#getKey()
   */
  int getIntKey();
  /** {@inheritDoc}
   * @deprecated Please use the corresponding type-specific method instead. */
  @Deprecated
  @Override
  Byte getValue();
  /**
   * @see Map.Entry#setValue(Object)
   */
  byte setValue(byte value);
  /**
   * @see Map.Entry#getValue()
   */
  byte getByteValue();
 }
 protected byte defRetValue;
 public void defaultReturnValue( final byte rv ) {
  defRetValue = rv;
 }
 public byte defaultReturnValue() {
  return defRetValue;
 }



 public boolean containsKey( final Object ok ) {
  if ( ok == null ) return false;
  return containsKey( ((((Integer)(ok)).intValue())) );
 }
 /** Delegates to the corresponding type-specific method, taking care of returning <code>null</code> on a missing key.
  *
  * <P>This method must check whether the provided key is in the map using <code>containsKey()</code>. Thus,
  * it probes the map <em>twice</em>. Implementors of subclasses should override it with a more efficient method.
  *
  * @deprecated Please use the corresponding type-specific method instead. */
 @Deprecated
 public Byte get( final Object ok ) {
  if ( ok == null ) return null;
  final int k = (((Integer) (ok)));
  return containsKey( k ) ? (get(k)) : null;
 }



 private static final long serialVersionUID = 0L;
 private static final boolean ASSERTS = false;
 /** The array of keys. */
 protected transient int[] key;
 /** The array of values. */
 protected transient byte[] value;
 /** The mask for wrapping a position counter. */
 protected transient int mask;
 /** Whether this set contains the key zero. */
 protected transient boolean containsNullKey;
 /** The current table size. */
 protected transient int n;
 /** Threshold after which we rehash. It must be the table size times {@link #f}. */
 protected transient int maxFill;
 /** Number of entries in the set (including the key zero, if present). */
 protected int size;
 /** The acceptable load factor. */
 protected final float f;
 /** Cached set of entries. */
 protected transient FastEntrySet entries;
 /** Cached set of keys. */
 protected transient IntSet keys;
 /** Creates a new hash map.
	 *
	 * <p>The actual table size will be the least power of two greater than <code>expected</code>/<code>f</code>.
	 *
	 * @param expected the expected number of elements in the hash set. 
	 * @param f the load factor.
	 */

 public Int2ByteOpenHashMap(final int expected, final float f ) {
  if ( f <= 0 || f > 1 ) throw new IllegalArgumentException( "Load factor must be greater than 0 and smaller than or equal to 1" );
  if ( expected < 0 ) throw new IllegalArgumentException( "The expected number of elements must be nonnegative" );
  this.f = f;
  n = arraySize( expected, f );
  mask = n - 1;
  maxFill = maxFill( n, f );
  key = new int[ n + 1 ];
  value = new byte[ n + 1 ];
 }
 /** Creates a new hash map with {@link Hash#DEFAULT_LOAD_FACTOR} as load factor.
	 *
	 * @param expected the expected number of elements in the hash map.
	 */
 public Int2ByteOpenHashMap(final int expected ) {
  this( expected, DEFAULT_LOAD_FACTOR );
 }
 /** Creates a new hash map with initial expected {@link Hash#DEFAULT_INITIAL_SIZE} entries
	 * and {@link Hash#DEFAULT_LOAD_FACTOR} as load factor.
	 */
 public Int2ByteOpenHashMap() {
  this( DEFAULT_INITIAL_SIZE, DEFAULT_LOAD_FACTOR );
 }
 /** Creates a new hash map copying a given one.
	 *
	 * @param m a {@link Map} to be copied into the new hash map. 
	 * @param f the load factor.
	 */
 public Int2ByteOpenHashMap(final Map<? extends Integer, ? extends Byte> m, final float f ) {
  this( m.size(), f );
  putAll( m );
 }
 /** Creates a new hash map with {@link Hash#DEFAULT_LOAD_FACTOR} as load factor copying a given one.
	 *
	 * @param m a {@link Map} to be copied into the new hash map. 
	 */
 public Int2ByteOpenHashMap(final Map<? extends Integer, ? extends Byte> m ) {
  this( m, DEFAULT_LOAD_FACTOR );
 }
 /** Creates a new hash map copying a given type-specific one.
	 *
	 * @param m a type-specific map to be copied into the new hash map. 
	 * @param f the load factor.
	 */
 public Int2ByteOpenHashMap(final Int2ByteOpenHashMap m, final float f ) {
  this( m.size(), f );
  putAll( m );
 }
 /** Creates a new hash map with {@link Hash#DEFAULT_LOAD_FACTOR} as load factor copying a given type-specific one.
	 *
	 * @param m a type-specific map to be copied into the new hash map. 
	 */
 public Int2ByteOpenHashMap(final Int2ByteOpenHashMap m ) {
  this( m, DEFAULT_LOAD_FACTOR );
 }
 /** Creates a new hash map using the elements of two parallel arrays.
	 *
	 * @param k the array of keys of the new hash map.
	 * @param v the array of corresponding values in the new hash map.
	 * @param f the load factor.
	 * @throws IllegalArgumentException if <code>k</code> and <code>v</code> have different lengths.
	 */
 public Int2ByteOpenHashMap(final int[] k, final byte[] v, final float f ) {
  this( k.length, f );
  if ( k.length != v.length ) throw new IllegalArgumentException( "The key array and the value array have different lengths (" + k.length + " and " + v.length + ")" );
  for( int i = 0; i < k.length; i++ ) this.put( k[ i ], v[ i ] );
 }
 /** Creates a new hash map with {@link Hash#DEFAULT_LOAD_FACTOR} as load factor using the elements of two parallel arrays.
	 *
	 * @param k the array of keys of the new hash map.
	 * @param v the array of corresponding values in the new hash map.
	 * @throws IllegalArgumentException if <code>k</code> and <code>v</code> have different lengths.
	 */
 public Int2ByteOpenHashMap(final int[] k, final byte[] v ) {
  this( k, v, DEFAULT_LOAD_FACTOR );
 }
 private int realSize() {
  return containsNullKey ? size - 1 : size;
 }
 private void ensureCapacity( final int capacity ) {
  final int needed = arraySize( capacity, f );
  if ( needed > n ) rehash( needed );
 }
 private void tryCapacity( final long capacity ) {
  final int needed = (int)Math.min( 1 << 30, Math.max( 2, HashCommon.nextPowerOfTwo( (long)Math.ceil( capacity / f ) ) ) );
  if ( needed > n ) rehash( needed );
 }
 private byte removeEntry( final int pos ) {
  final byte oldValue = value[ pos ];
  size--;
  shiftKeys( pos );
  if ( size < maxFill / 4 && n > DEFAULT_INITIAL_SIZE ) rehash( n / 2 );
  return oldValue;
 }
 private byte removeNullEntry() {
  containsNullKey = false;
  final byte oldValue = value[ n ];
  size--;
  if ( size < maxFill / 4 && n > DEFAULT_INITIAL_SIZE ) rehash( n / 2 );
  return oldValue;
 }
 public boolean containsValue( Object ov ) {
  if ( ov == null ) return false;
  return containsValue( ((((Byte)(ov)).byteValue())) );
 }
 /** Puts all pairs in the given map.
  * If the map implements the interface of this map,
  * it uses the faster iterators.
  *
  * @param m a map.
  */
 @SuppressWarnings("deprecation")
 public void putAll(Map<? extends Integer,? extends Byte> m) {
  if ( f <= .5 ) ensureCapacity( m.size() ); // The resulting map will be sized for m.size() elements
  else tryCapacity( size() + m.size() ); // The resulting map will be tentatively sized for size() + m.size() elements
  int n = m.size();
  final Iterator<? extends Map.Entry<? extends Integer,? extends Byte>> i = m.entrySet().iterator();
  if (m instanceof Int2ByteOpenHashMap) {
   Entry e;
   while(n-- != 0) {
    e = (Entry )i.next();
    put(e.getIntKey(), e.getByteValue());
   }
  }
  else {
   Map.Entry<? extends Integer,? extends Byte> e;
   while(n-- != 0) {
    e = i.next();
    put(e.getKey(), e.getValue());
   }
  }
 }
 
 @SuppressWarnings({ "unchecked", "rawtypes" })
 public ObjectSet<Map.Entry<Integer, Byte>> entrySet() {
  return (ObjectSet)int2ByteEntrySet();
 }
 public boolean equals(Object o) {
  if ( o == this ) return true;
  if ( ! ( o instanceof Map ) ) return false;
  Map<?,?> m = (Map<?,?>)o;
  if ( m.size() != size() ) return false;
  return entrySet().containsAll( m.entrySet() );
 }
 public String toString() {
  final StringBuilder s = new StringBuilder();
  final ObjectIterator<? extends Map.Entry<Integer,Byte>> i = entrySet().iterator();
  int n = size();
  Entry e;
  boolean first = true;
  s.append("{");
  while(n-- != 0) {
   if (first) first = false;
   else s.append(", ");
   e = (Entry )i.next();
   s.append(String.valueOf(e.getIntKey()));
   s.append("=>");
   s.append(String.valueOf(e.getByteValue()));
  }
  s.append("}");
  return s.toString();
 }

 private int insert(final int k, final byte v) {
  int pos;
  if ( ( (k) == (0) ) ) {
   if ( containsNullKey ) return n;
   containsNullKey = true;
   pos = n;
  }
  else {
   int curr;
   final int[] key = this.key;
   // The starting point.
   if ( ! ( (curr = key[ pos = ( HashCommon.mix( (k) ) ) & mask ]) == (0) ) ) {
    if ( ( (curr) == (k) ) ) return pos;
    while( ! ( (curr = key[ pos = ( pos + 1 ) & mask ]) == (0) ) )
     if ( ( (curr) == (k) ) ) return pos;
   }
  }
  key[ pos ] = k;
  value[ pos ] = v;
  if ( size++ >= maxFill ) rehash( arraySize( size + 1, f ) );
  if ( ASSERTS ) checkTable();
  return -1;
 }
 public byte put(final int k, final byte v) {
  final int pos = insert( k, v );
  if ( pos < 0 ) return defRetValue;
  final byte oldValue = value[ pos ];
  value[ pos ] = v;
  return oldValue;
 }
 /** {@inheritDoc}
	 * @deprecated Please use the corresponding type-specific method instead. */
 @Deprecated
 @Override
 public Byte put( final Integer ok, final Byte ov ) {
  final byte v = ((ov).byteValue());
  final int pos = insert( ((ok).intValue()), v );
  if ( pos < 0 ) return (null);
  final byte oldValue = value[ pos ];
  value[ pos ] = v;
  return (Byte.valueOf(oldValue));
 }
 private byte addToValue( final int pos, final byte incr ) {
  final byte oldValue = value[ pos ];
  value[ pos ] = (byte)(oldValue + incr);
  return oldValue;
 }
 /** Adds an increment to value currently associated with a key.
	 *
	 * <P>Note that this method respects the {@linkplain #defaultReturnValue() default return value} semantics: when
	 * called with a key that does not currently appears in the map, the key
	 * will be associated with the default return value plus
	 * the given increment.
	 *
	 * @param k the key.
	 * @param incr the increment.
	 * @return the old value, or the {@linkplain #defaultReturnValue() default return value} if no value was present for the given key.
	 */
 public byte addTo(final int k, final byte incr) {
  int pos;
  if ( ( (k) == (0) ) ) {
   if ( containsNullKey ) return addToValue( n, incr );
   pos = n;
   containsNullKey = true;
  }
  else {
   int curr;
   final int[] key = this.key;
   // The starting point.
   if ( ! ( (curr = key[ pos = ( HashCommon.mix( (k) ) ) & mask ]) == (0) ) ) {
    if ( ( (curr) == (k) ) ) return addToValue( pos, incr );
    while( ! ( (curr = key[ pos = ( pos + 1 ) & mask ]) == (0) ) )
     if ( ( (curr) == (k) ) ) return addToValue( pos, incr );
   }
  }
  key[ pos ] = k;
  value[ pos ] = (byte)(defRetValue + incr);
  if ( size++ >= maxFill ) rehash( arraySize( size + 1, f ) );
  if ( ASSERTS ) checkTable();
  return defRetValue;
 }
 /** Shifts left entries with the specified hash code, starting at the specified position,
	 * and empties the resulting free entry.
	 *
	 * @param pos a starting position.
	 */
 protected final void shiftKeys( int pos ) {
  // Shift entries with the same hash.
  int last, slot;
  int curr;
  final int[] key = this.key;
  for(;;) {
   pos = ( ( last = pos ) + 1 ) & mask;
   for(;;) {
    if ( ( (curr = key[ pos ]) == (0) ) ) {
     key[ last ] = (0);
     return;
    }
    slot = ( HashCommon.mix( (curr) ) ) & mask;
    if ( last <= pos ? last >= slot || slot > pos : last >= slot && slot > pos ) break;
    pos = ( pos + 1 ) & mask;
   }
   key[ last ] = curr;
   value[ last ] = value[ pos ];
  }
 }

 public byte remove( final int k ) {
  if ( ( (k) == (0) ) ) {
   if ( containsNullKey ) return removeNullEntry();
   return defRetValue;
  }
  int curr;
  final int[] key = this.key;
  int pos;
  // The starting point.
  if ( ( (curr = key[ pos = ( HashCommon.mix( (k) ) ) & mask ]) == (0) ) ) return defRetValue;
  if ( ( (k) == (curr) ) ) return removeEntry( pos );
  while( true ) {
   if ( ( (curr = key[ pos = ( pos + 1 ) & mask ]) == (0) ) ) return defRetValue;
   if ( ( (k) == (curr) ) ) return removeEntry( pos );
  }
 }
 /** {@inheritDoc}
	 * @deprecated Please use the corresponding type-specific method instead. */
 @Deprecated
 @Override

 public Byte remove( final Object ok ) {
  final int k = ((((Integer)(ok)).intValue()));
  if ( ( (k) == (0) ) ) {
   if ( containsNullKey ) return (Byte.valueOf(removeNullEntry()));
   return (null);
  }
  int curr;
  final int[] key = this.key;
  int pos;
  // The starting point.
  if ( ( (curr = key[ pos = ( HashCommon.mix( (k) ) ) & mask ]) == (0) ) ) return (null);
  if ( ( (curr) == (k) ) ) return (Byte.valueOf(removeEntry( pos )));
  while( true ) {
   if ( ( (curr = key[ pos = ( pos + 1 ) & mask ]) == (0) ) ) return (null);
   if ( ( (curr) == (k) ) ) return (Byte.valueOf(removeEntry( pos )));
  }
 }
 /** @deprecated Please use the corresponding type-specific method instead. */
 @Deprecated
 public Byte get( final Integer ok ) {
  if ( ok == null ) return null;
  final int k = ((ok).intValue());
  if ( ( (k) == (0) ) ) return containsNullKey ? (Byte.valueOf(value[ n ])) : (null);
  int curr;
  final int[] key = this.key;
  int pos;
  // The starting point.
  if ( ( (curr = key[ pos = ( HashCommon.mix( (k) ) ) & mask ]) == (0) ) ) return (null);
  if ( ( (k) == (curr) ) ) return (Byte.valueOf(value[ pos ]));
  // There's always an unused entry.
  while( true ) {
   if ( ( (curr = key[ pos = ( pos + 1 ) & mask ]) == (0) ) ) return (null);
   if ( ( (k) == (curr) ) ) return (Byte.valueOf(value[ pos ]));
  }
 }

 public byte get( final int k ) {
  if ( ( (k) == (0) ) ) return containsNullKey ? value[ n ] : defRetValue;
  int curr;
  final int[] key = this.key;
  int pos;
  // The starting point.
  if ( ( (curr = key[ pos = ( HashCommon.mix( (k) ) ) & mask ]) == (0) ) ) return defRetValue;
  if ( ( (k) == (curr) ) ) return value[ pos ];
  // There's always an unused entry.
  while( true ) {
   if ( ( (curr = key[ pos = ( pos + 1 ) & mask ]) == (0) ) ) return defRetValue;
   if ( ( (k) == (curr) ) ) return value[ pos ];
  }
 }

 public boolean containsKey( final int k ) {
  if ( ( (k) == (0) ) ) return containsNullKey;
  int curr;
  final int[] key = this.key;
  int pos;
  // The starting point.
  if ( ( (curr = key[ pos = ( HashCommon.mix( (k) ) ) & mask ]) == (0) ) ) return false;
  if ( ( (k) == (curr) ) ) return true;
  // There's always an unused entry.
  while( true ) {
   if ( ( (curr = key[ pos = ( pos + 1 ) & mask ]) == (0) ) ) return false;
   if ( ( (k) == (curr) ) ) return true;
  }
 }
 public boolean containsValue( final byte v ) {
  final byte value[] = this.value;
  final int key[] = this.key;
  if ( containsNullKey && ( (value[ n ]) == (v) ) ) return true;
  for( int i = n; i-- != 0; ) if ( ! ( (key[ i ]) == (0) ) && ( (value[ i ]) == (v) ) ) return true;
  return false;
 }
 /* Removes all elements from this map.
	 *
	 * <P>To increase object reuse, this method does not change the table size.
	 * If you want to reduce the table size, you must use {@link #trim()}.
	 *
	 */
 public void clear() {
  if ( size == 0 ) return;
  size = 0;
  containsNullKey = false;
  Arrays.fill( key, (0) );
 }
 public int size() {
  return size;
 }
 public boolean isEmpty() {
  return size == 0;
 }
 /** A no-op for backward compatibility.
	 * 
	 * @param growthFactor unused.
	 * @deprecated Since <code>fastutil</code> 6.1.0, hash tables are doubled when they are too full.
	 */
 @Deprecated
 public void growthFactor( int growthFactor ) {}
 /** Gets the growth factor (2).
	 *
	 * @return the growth factor of this set, which is fixed (2).
	 * @see #growthFactor(int)
	 * @deprecated Since <code>fastutil</code> 6.1.0, hash tables are doubled when they are too full.
	 */
 @Deprecated
 public int growthFactor() {
  return 16;
 }
 /** The entry class for a hash map does not record key and value, but
	 * rather the position in the hash table of the corresponding entry. This
	 * is necessary so that calls to {@link Map.Entry#setValue(Object)} are reflected in
	 * the map */
 final class MapEntry implements Entry , Map.Entry<Integer, Byte> {
  // The table index this entry refers to, or -1 if this entry has been deleted.
  int index;
  MapEntry( final int index ) {
   this.index = index;
  }
  MapEntry() {}
  /** {@inheritDoc}
		 * @deprecated Please use the corresponding type-specific method instead. */
  @Deprecated
  public Integer getKey() {
   return (Integer.valueOf(key[ index ]));
  }
  public int getIntKey() {
      return key[ index ];
  }
  /** {@inheritDoc}
		 * @deprecated Please use the corresponding type-specific method instead. */
  @Deprecated
  public Byte getValue() {
   return (Byte.valueOf(value[ index ]));
  }
  public byte getByteValue() {
   return value[ index ];
  }
  public byte setValue( final byte v ) {
   final byte oldValue = value[ index ];
   value[ index ] = v;
   return oldValue;
  }
  public Byte setValue( final Byte v ) {
   return (Byte.valueOf(setValue( ((v).byteValue()) )));
  }
  @SuppressWarnings("unchecked")
  public boolean equals( final Object o ) {
   if (!(o instanceof Map.Entry)) return false;
   Map.Entry<Integer, Byte> e = (Map.Entry<Integer, Byte>)o;
   return ( (key[ index ]) == (((e.getKey()).intValue())) ) && ( (value[ index ]) == (((e.getValue()).byteValue())) );
  }
  public int hashCode() {
   return (key[ index ]) ^ (value[ index ]);
  }
  public String toString() {
   return key[ index ] + "=>" + value[ index ];
  }
 }
 /** An iterator over a hash map. */
 private class MapIterator {
  /** The index of the last entry returned, if positive or zero; initially, {@link #n}. If negative, the last
			entry returned was that of the key of index {@code - pos - 1} from the {@link #wrapped} list. */
  int pos = n;
  /** The index of the last entry that has been returned (more precisely, the value of {@link #pos} if {@link #pos} is positive,
			or {@link Integer#MIN_VALUE} if {@link #pos} is negative). It is -1 if either
			we did not return an entry yet, or the last returned entry has been removed. */
  int last = -1;
  /** A downward counter measuring how many entries must still be returned. */
  int c = size;
  /** A boolean telling us whether we should return the entry with the null key. */
  boolean mustReturnNullKey = Int2ByteOpenHashMap.this.containsNullKey;
  /** A lazily allocated list containing keys of entries that have wrapped around the table because of removals. */
  IntArrayList wrapped;
  public boolean hasNext() {
   return c != 0;
  }
  public int nextEntry() {
   if ( ! hasNext() ) throw new NoSuchElementException();
   c--;
   if ( mustReturnNullKey ) {
    mustReturnNullKey = false;
    return last = n;
   }
   final int key[] = Int2ByteOpenHashMap.this.key;
   for(;;) {
    if ( --pos < 0 ) {
     // We are just enumerating elements from the wrapped list.
     last = Integer.MIN_VALUE;
     final int k = wrapped.getInt( - pos - 1 );
     int p = ( HashCommon.mix( (k) ) ) & mask;
     while ( ! ( (k) == (key[ p ]) ) ) p = ( p + 1 ) & mask;
     return p;
    }
    if ( ! ( (key[ pos ]) == (0) ) ) return last = pos;
   }
  }
  /** Shifts left entries with the specified hash code, starting at the specified position,
		 * and empties the resulting free entry.
		 *
		 * @param pos a starting position.
		 */
  private final void shiftKeys( int pos ) {
   // Shift entries with the same hash.
   int last, slot;
   int curr;
   final int[] key = Int2ByteOpenHashMap.this.key;
   for(;;) {
    pos = ( ( last = pos ) + 1 ) & mask;
    for(;;) {
     if ( ( (curr = key[ pos ]) == (0) ) ) {
      key[ last ] = (0);
      return;
     }
     slot = ( HashCommon.mix( (curr) ) ) & mask;
     if ( last <= pos ? last >= slot || slot > pos : last >= slot && slot > pos ) break;
     pos = ( pos + 1 ) & mask;
    }
    if ( pos < last ) { // Wrapped entry.
     if ( wrapped == null ) wrapped = new IntArrayList( 2 );
     wrapped.add( key[ pos ] );
    }
    key[ last ] = curr;
    value[ last ] = value[ pos ];
   }
  }
  public void remove() {
   if ( last == -1 ) throw new IllegalStateException();
   if ( last == n ) {
    containsNullKey = false;
   }
   else if ( pos >= 0 ) shiftKeys( last );
   else {
    // We're removing wrapped entries.
    Int2ByteOpenHashMap.this.remove( wrapped.getInt( - pos - 1 ) );
    last = -1; // Note that we must not decrement size
    return;
   }
   size--;
   last = -1; // You can no longer remove this entry.
   if ( ASSERTS ) checkTable();
  }
  public int skip( final int n ) {
   int i = n;
   while( i-- != 0 && hasNext() ) nextEntry();
   return n - i - 1;
  }
 }
 private class EntryIterator extends MapIterator implements ObjectIterator<Entry > {
  private MapEntry entry;
  public Entry next() {
   return entry = new MapEntry( nextEntry() );
  }
  @Override
  public void remove() {
   super.remove();
   entry.index = -1; // You cannot use a deleted entry.
  }
 }
 private class FastEntryIterator extends MapIterator implements ObjectIterator<Entry > {
  private final MapEntry entry = new MapEntry();
  public MapEntry next() {
   entry.index = nextEntry();
   return entry;
  }
 }
 private final class MapEntrySet extends AbstractObjectSet<Entry > implements FastEntrySet {
  public ObjectIterator<Entry > iterator() {
   return new EntryIterator();
  }
  public ObjectIterator<Entry > fastIterator() {
   return new FastEntryIterator();
  }
 
  public boolean contains( final Object o ) {
   if ( !( o instanceof Map.Entry ) ) return false;
   final Map.Entry<?,?> e = (Map.Entry<?,?>)o;
   if (e.getKey() == null || ! (e.getKey() instanceof Integer)) return false;
   if (e.getValue() == null || ! (e.getValue() instanceof Byte)) return false;
   final int k = ((((Integer)( e.getKey())).intValue()));
   final byte v = ((((Byte)( e.getValue())).byteValue()));
   if ( ( (k) == (0) ) ) return Int2ByteOpenHashMap.this.containsNullKey && ( (value[ n ]) == (v) );
   int curr;
   final int[] key = Int2ByteOpenHashMap.this.key;
   int pos;
   // The starting point.
   if ( ( (curr = key[ pos = ( HashCommon.mix( (k) ) ) & mask ]) == (0) ) ) return false;
   if ( ( (k) == (curr) ) ) return ( (value[ pos ]) == (v) );
   // There's always an unused entry.
   while( true ) {
    if ( ( (curr = key[ pos = ( pos + 1 ) & mask ]) == (0) ) ) return false;
    if ( ( (k) == (curr) ) ) return ( (value[ pos ]) == (v) );
   }
  }
 
  public boolean rem( final Object o ) {
   if ( !( o instanceof Map.Entry ) ) return false;
   final Map.Entry<?,?> e = (Map.Entry<?,?>)o;
   if (e.getKey() == null || ! (e.getKey() instanceof Integer)) return false;
   if (e.getValue() == null || ! (e.getValue() instanceof Byte)) return false;
   final int k = ((((Integer)( e.getKey())).intValue()));
   final byte v = ((((Byte)( e.getValue())).byteValue()));
   if ( ( (k) == (0) ) ) {
    if ( containsNullKey && ( (value[ n ]) == (v) ) ) {
     removeNullEntry();
     return true;
    }
    return false;
   }
   int curr;
   final int[] key = Int2ByteOpenHashMap.this.key;
   int pos;
   // The starting point.
   if ( ( (curr = key[ pos = ( HashCommon.mix( (k) ) ) & mask ]) == (0) ) ) return false;
   if ( ( (curr) == (k) ) ) {
    if ( ( (value[ pos ]) == (v) ) ) {
     removeEntry( pos );
     return true;
    }
    return false;
   }
   while( true ) {
    if ( ( (curr = key[ pos = ( pos + 1 ) & mask ]) == (0) ) ) return false;
    if ( ( (curr) == (k) ) ) {
     if ( ( (value[ pos ]) == (v) ) ) {
      removeEntry( pos );
      return true;
     }
    }
   }
  }
  public int size() {
   return size;
  }
  public void clear() {
   Int2ByteOpenHashMap.this.clear();
  }
 }
 public FastEntrySet int2ByteEntrySet() {
  if ( entries == null ) entries = new MapEntrySet();
  return entries;
 }
 /** An iterator on keys.
	 *
	 * <P>We simply override the {@link ListIterator#next()}/{@link ListIterator#previous()} methods
	 * (and possibly their type-specific counterparts) so that they return keys
	 * instead of entries.
	 */
 private final class KeyIterator extends MapIterator implements IntIterator {
  public KeyIterator() { super(); }
  public int nextInt() { return key[ nextEntry() ]; }
  public Integer next() { return (Integer.valueOf(key[ nextEntry() ])); }
 }
 private final class KeySet extends AbstractIntSet {
  public IntIterator iterator() {
   return new KeyIterator();
  }
  public int size() {
   return size;
  }
  public boolean contains( int k ) {
   return containsKey( k );
  }
  public boolean rem( int k ) {
   final int oldSize = size;
   Int2ByteOpenHashMap.this.remove( k );
   return size != oldSize;
  }
  public void clear() {
   Int2ByteOpenHashMap.this.clear();
  }
 }
 public IntSet keySet() {
  if ( keys == null ) keys = new KeySet();
  return keys;
 }
 /** A no-op for backward compatibility. The kind of tables implemented by
	 * this class never need rehashing.
	 *
	 * <P>If you need to reduce the table size to fit exactly
	 * this set, use {@link #trim()}.
	 *
	 * @return true.
	 * @see #trim()
	 * @deprecated A no-op.
	 */
 @Deprecated
 public boolean rehash() {
  return true;
 }
 /** Rehashes the map, making the table as small as possible.
	 * 
	 * <P>This method rehashes the table to the smallest size satisfying the
	 * load factor. It can be used when the set will not be changed anymore, so
	 * to optimize access speed and size.
	 *
	 * <P>If the table size is already the minimum possible, this method
	 * does nothing. 
	 *
	 * @return true if there was enough memory to trim the map.
	 * @see #trim(int)
	 */
 public boolean trim() {
  final int l = arraySize( size, f );
  if ( l >= n || size > maxFill( l, f ) ) return true;
  try {
   rehash( l );
  }
  catch(OutOfMemoryError cantDoIt) { return false; }
  return true;
 }
 /** Rehashes this map if the table is too large.
	 * 
	 * <P>Let <var>N</var> be the smallest table size that can hold
	 * <code>max(n,{@link #size()})</code> entries, still satisfying the load factor. If the current
	 * table size is smaller than or equal to <var>N</var>, this method does
	 * nothing. Otherwise, it rehashes this map in a table of size
	 * <var>N</var>.
	 *
	 * <P>This method is useful when reusing maps.  {@linkplain #clear() Clearing a
	 * map} leaves the table size untouched. If you are reusing a map
	 * many times, you can call this method with a typical
	 * size to avoid keeping around a very large table just
	 * because of a few large transient maps.
	 *
	 * @param n the threshold for the trimming.
	 * @return true if there was enough memory to trim the map.
	 * @see #trim()
	 */
 public boolean trim( final int n ) {
  final int l = HashCommon.nextPowerOfTwo( (int)Math.ceil( n / f ) );
  if ( l >= n || size > maxFill( l, f ) ) return true;
  try {
   rehash( l );
  }
  catch( OutOfMemoryError cantDoIt ) { return false; }
  return true;
 }
 /** Rehashes the map.
	 *
	 * <P>This method implements the basic rehashing strategy, and may be
	 * overriden by subclasses implementing different rehashing strategies (e.g.,
	 * disk-based rehashing). However, you should not override this method
	 * unless you understand the internal workings of this class.
	 *
	 * @param newN the new size
	 */

 protected void rehash( final int newN ) {
  final int key[] = this.key;
  final byte value[] = this.value;
  final int mask = newN - 1; // Note that this is used by the hashing macro
  final int newKey[] = new int[ newN + 1 ];
  final byte newValue[] = new byte[ newN + 1 ];
  int i = n, pos;
  for( int j = realSize(); j-- != 0; ) {
   while( ( (key[ --i ]) == (0) ) );
   if ( ! ( (newKey[ pos = ( HashCommon.mix( (key[ i ]) ) ) & mask ]) == (0) ) )
    while ( ! ( (newKey[ pos = ( pos + 1 ) & mask ]) == (0) ) );
   newKey[ pos ] = key[ i ];
   newValue[ pos ] = value[ i ];
  }
  newValue[ newN ] = value[ n ];
  n = newN;
  this.mask = mask;
  maxFill = maxFill( n, f );
  this.key = newKey;
  this.value = newValue;
 }
 /** Returns a deep copy of this map. 
	 *
	 * <P>This method performs a deep copy of this hash map; the data stored in the
	 * map, however, is not cloned. Note that this makes a difference only for object keys.
	 *
	 *  @return a deep copy of this map.
	 */

 public Int2ByteOpenHashMap clone() {
  Int2ByteOpenHashMap c;
  try {
   c = (Int2ByteOpenHashMap)super.clone();
  }
  catch(CloneNotSupportedException cantHappen) {
   throw new InternalError();
  }
  c.keys = null;
  c.entries = null;
  c.containsNullKey = containsNullKey;
  c.key = key.clone();
  c.value = value.clone();
  return c;
 }
 /** Returns a hash code for this map.
	 *
	 * This method overrides the generic method provided by the superclass. 
	 * Since <code>equals()</code> is not overriden, it is important
	 * that the value returned by this method is the same value as
	 * the one returned by the overriden method.
	 *
	 * @return a hash code for this map.
	 */
 public int hashCode() {
  int h = 0;
  for( int j = realSize(), i = 0, t = 0; j-- != 0; ) {
   while( ( (key[ i ]) == (0) ) ) i++;
    t = (key[ i ]);
    t ^= (value[ i ]);
   h += t;
   i++;
  }
  // Zero / null keys have hash zero.		
  if ( containsNullKey ) h += (value[ n ]);
  return h;
 }
 private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
  final int key[] = this.key;
  final byte value[] = this.value;
  final MapIterator i = new MapIterator();
  s.defaultWriteObject();
  for( int j = size, e; j-- != 0; ) {
   e = i.nextEntry();
   s.writeInt( key[ e ] );
   s.writeByte( value[ e ] );
  }
 }

 private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
  s.defaultReadObject();
  n = arraySize( size, f );
  maxFill = maxFill( n, f );
  mask = n - 1;
  final int key[] = this.key = new int[ n + 1 ];
  final byte value[] = this.value = new byte[ n + 1 ];
  int k;
  byte v;
  for( int i = size, pos; i-- != 0; ) {
   k = s.readInt();
   v = s.readByte();
   if ( ( (k) == (0) ) ) {
    pos = n;
    containsNullKey = true;
   }
   else {
    pos = ( HashCommon.mix( (k) ) ) & mask;
    while ( ! ( (key[ pos ]) == (0) ) ) pos = ( pos + 1 ) & mask;
   }
   key[ pos ] = k;
   value[ pos ] = v;
  }
  if ( ASSERTS ) checkTable();
 }
 private void checkTable() {}

 /**
  * You don't need the values on their own; this throws an UnsupportedOperationException.
  */
 @Override
 public Collection<Byte> values() {
  throw new UnsupportedOperationException("values() is not supported");
 }

 /**
  * Returns the value to which the specified key is mapped, or
  * {@code defaultValue} if this map contains no mapping for the key.
  *
  * @param key          the key whose associated value is to be returned; must not be null
  * @param defaultValue the default mapping of the key; must not be null
  * @return the value to which the specified key is mapped, or
  * {@code defaultValue} if this map contains no mapping for the key
  * @throws ClassCastException   if the key is of an inappropriate type for
  *                              this map
  *                              (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
  * @throws NullPointerException if the specified key is null and this map
  *                              does not permit null keys
  *                              (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
  */
 public Byte getOrDefault(Object key, Byte defaultValue) {
  return getOrDefault(((Integer)key).intValue(), defaultValue.byteValue());
 }
 public byte getOrDefault(int k, byte defaultValue) {
  if ( ( (k) == (0) ) ) return containsNullKey ? value[ n ] : defaultValue;
  int curr;
  final int[] key = this.key;
  int pos;
  // The starting point.
  if ( ( (curr = key[ pos = ( HashCommon.mix( (k) ) ) & mask ]) == (0) ) ) return defaultValue;
  if ( ( (k) == (curr) ) ) return value[ pos ];
  // There's always an unused entry.
  while( true ) {
   if ( ( (curr = key[ pos = ( pos + 1 ) & mask ]) == (0) ) ) return defaultValue;
   if ( ( (k) == (curr) ) ) return value[ pos ];
  }
 }
}
