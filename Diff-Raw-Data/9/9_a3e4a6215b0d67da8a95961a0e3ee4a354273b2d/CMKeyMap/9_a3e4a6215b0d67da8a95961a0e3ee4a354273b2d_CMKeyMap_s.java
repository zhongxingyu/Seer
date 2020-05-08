 /*
  * caveman - A primitive collection library
  * Copyright 2011-2014 MeBigFatGuy.com
  * Copyright 2011-2014 Dave Brosius
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and limitations
  * under the License.
  */
 package com.mebigfatguy.caveman.proto;
 
 import java.util.Collection;
 
 import com.mebigfatguy.caveman.proto.aux.CM;
 import com.mebigfatguy.caveman.proto.aux.CMKey;
 
 /**
  * An object that maps <b>CMKey</b> keys to <b>Object</b> values.  A map cannot contain duplicate keys;
  * each key can map to at most one value.
  * 
  * <p>The <tt>CMKeyMap</tt> interface provides two <i>collection views</i>, which
  * allow a map's contents to be viewed as a set of keys or collection of values
  * The <i>order</i> of a map is defined as the order in which the iterators on the map's collection 
  * views return their elements.  
  */
 public interface CMKeyMap<V> {
 	
     /**
      * Returns the number of key-value mappings in this map.
      *
      * @return the number of key-value mappings in this map
      */
 	int size();
 	
     /**
      * Returns <tt>true</tt> if this map contains no key-value mappings.
      *
      * @return <tt>true</tt> if this map contains no key-value mappings
      */
 	boolean isEmpty();
 	
     /**
      * Returns <tt>true</tt> if this map contains a mapping for the specified
      * <b>CMKey</b> key.
      *
      * @param key key whose presence in this map is to be tested
      * @return <tt>true</tt> if this map contains a mapping for the specified
      *         <b>CMKey</b>
      */
 	boolean containsKey(CM key);
 	
     /**
      * Returns <tt>true</tt> if this map maps one or more <b>CMKey</b> keys to the
      * specified <b>Object</b> value. This method is provided for completeness, 
      * but will not perform efficiently for large maps.
      *
      * @param value value whose presence in this map is to be tested
      * @return <tt>true</tt> if this map maps one or more keys to the
      *         specified value
      */
 	boolean containsValue(V value);
 	
     /**
      * Returns the value to which the specified <b>CMKey</b> key is mapped,
      * or null if this map contains no mapping for the key.
      *
      * @param key the key whose associated value is to be returned
      * @return the value to which the specified key is mapped, or
      *         {@code null} if this map contains no mapping for the key
      */
 	V get(CM key);
 	
     /**
      * Associates the specified <b>Object</b> value with the specified <b>CMKey</b> key in this map
      * (optional operation).  If the map previously contained a mapping for
      * the key, the old value is replaced by the specified value.
      *
      * @param key key with which the specified value is to be associated
      * @param value value to be associated with the specified key
      * @throws UnsupportedOperationException if the <tt>put</tt> operation
      *         is not supported by this map
      */
 	void put(CM key, V value);
 	
     /**
      * Removes the mapping for a <b>CMKey</b> key from this map if it is present
      * (optional operation).
      *
      * <p>Returns the <b>Object</b> value to which this map previously associated the <b>CMKey</b> key,
      * or <tt>null</tt> if the map contained no mapping for the key.
      *
      * <p>The map will not contain a mapping for the specified key once the
      * call returns.
      *
      * @param key key whose mapping is to be removed from the map
      * @throws UnsupportedOperationException if the <tt>remove</tt> operation
      *         is not supported by this map
      */
 	void remove(CM key);
 	
     /**
      * Copies all of the mappings from the specified map to this map
      * (optional operation).  The effect of this call is equivalent to that
     * of calling {@link #put(CMKey,Object) put(k, v)} on this map once
      * for each mapping from <b>CMKey</b> key <tt>k</tt> to <b>Object</b> value <tt>v</tt> in the
      * specified map.  The behavior of this operation is undefined if the
      * specified map is modified while the operation is in progress.
      *
      * @param m mappings to be stored in this map
      * @throws UnsupportedOperationException if the <tt>putAll</tt> operation
      *         is not supported by this map
      */
 	void putAll(CMKeyMap<V> m);
 	
     /**
      * Removes all of the mappings from this map (optional operation).
      * The map will be empty after this call returns.
      *
      * @throws UnsupportedOperationException if the <tt>clear</tt> operation
      *         is not supported by this map
      */
 	void clear();
 	
     /**
      * Returns a view of the <b>CMKey</b> keys contained in this map.
      * The set is backed by the map, so changes to the map are
      * reflected in the set, and vice-versa.  If the map is modified
      * while an iteration over the set is in progress (except through
      * the iterator's own <tt>remove</tt> operation), the results of
      * the iteration are undefined.  The set supports element removal,
      * which removes the corresponding mapping from the map, via the
      * <tt>Iterator.remove</tt>, <tt>Set.remove</tt>,
      * <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt>
      * operations.  It does not support the <tt>add</tt> or <tt>addAll</tt>
      * operations.
      *
      * @return a set view of the <b>CMKey</b> keys contained in this map
      */
 	CMSet keySet();
 	
     /**
      * Returns a view of the <b>Object</b> values contained in this map.
      * The collection is backed by the map, so changes to the map are
      * reflected in the collection, and vice-versa.  If the map is
      * modified while an iteration over the collection is in progress
      * (except through the iterator's own <tt>remove</tt> operation),
      * the results of the iteration are undefined.  The collection
      * supports element removal, which removes the corresponding
      * mapping from the map, via the <tt>Iterator.remove</tt>,
      * <tt>Collection.remove</tt>, <tt>removeAll</tt>,
      * <tt>retainAll</tt> and <tt>clear</tt> operations.  It does not
      * support the <tt>add</tt> or <tt>addAll</tt> operations.
      *
      * @return a collection view of the <b>CMValue</b> values contained in this map
      */
 	Collection<V> values();
 	
 	/**
 	 * Returns a iterator over the the <b>CMKey</b>, <b>Object</b> pairs in the map.
 	 * 
 	 * @return an iterator to navigate the map
 	 */
 	CMKeyMapIterator<V> iterator();
 }
