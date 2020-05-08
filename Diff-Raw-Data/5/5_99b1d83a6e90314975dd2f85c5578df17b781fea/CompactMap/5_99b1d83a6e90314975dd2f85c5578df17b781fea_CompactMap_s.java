 package com.psddev.dari.util;
 
 import java.util.AbstractCollection;
 import java.util.AbstractSet;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.NoSuchElementException;
 import java.util.Set;
 
 /**
  * Map implementation that's optimized for a small number of entries.
  *
  * <p>Some of its behaviors are:</p>
  *
  * <ul>
  * <li>Most operations are {@code O(n)}.</li>
  * <li>Maintains insertion order during iterator.</li>
  * <li>Switches to using {@link LinkedHashMap} internally if the number of
  * entries exceed 8.</li>
  * </ul>
  */
 @SuppressWarnings("unchecked")
 public class CompactMap<K, V> implements Map<K, V> {
 
     private static int ARRAY_SIZE = 8;
 
     private Object delegate;
     private int size;
 
     // --- Map support ---
 
     @Override
     public void clear() {
         if (size < 0) {
             ((Map<?, ?>) delegate).clear();
 
         } else {
             size = 0;
         }
     }
 
     private int indexOfKey(Object key) {
         int keyHash = ObjectUtils.hashCode(key);
 
         for (int i = 0; i < size; ++ i) {
             K k = ((K[]) delegate)[i];
 
             if (keyHash == ObjectUtils.hashCode(k) && ObjectUtils.equals(key, k)) {
                 return i;
             }
         }
 
         return -1;
     }
 
     @Override
     public boolean containsKey(Object key) {
         if (size < 0) {
             return ((Map<?, ?>) delegate).containsKey(key);
 
         } else {
             return indexOfKey(key) >= 0;
         }
     }
 
     @Override
     public boolean containsValue(Object value) {
         if (size < 0) {
             return ((Map<?, ?>) delegate).containsValue(value);
 
         } else {
             int valueHash = ObjectUtils.hashCode(value);
 
             for (int i = ARRAY_SIZE, s = i + size; i < s; ++ i) {
                 V v = ((V[]) delegate)[i];
 
                 if (valueHash == ObjectUtils.hashCode(v) && ObjectUtils.equals(value, v)) {
                     return true;
                 }
             }
 
             return false;
         }
     }
 
     @Override
     public Set<Map.Entry<K, V>> entrySet() {
         if (size < 0) {
             return ((Map<K, V>) delegate).entrySet();
 
         } else {
             return new AbstractSet<Map.Entry<K, V>>() {
 
                 @Override
                 public Iterator<Map.Entry<K, V>> iterator() {
                     return new IndexedIterator<Map.Entry<K, V>>() {
 
                         @Override
                         protected Map.Entry<K, V> doNext(final int index) {
                             return new Map.Entry<K, V>() {
 
                                 @Override
                                 public K getKey() {
                                     return ((K[]) delegate)[index];
                                 }
 
                                 @Override
                                 public V getValue() {
                                     return ((V[]) delegate)[ARRAY_SIZE + index];
                                 }
 
                                 @Override
                                 public V setValue(V value) {
                                     V oldValue = getValue();
                                     ((V[]) delegate)[ARRAY_SIZE + index] = value;
 
                                     return oldValue;
                                 }
 
                                 @Override
                                 public boolean equals(Object other) {
                                     if (this == other) {
                                         return true;
 
                                     } else if (other instanceof Map.Entry) {
                                        return ObjectUtils.equals(getKey(), ((Map.Entry<?, ?>) other).getKey());
 
                                     } else {
                                         return false;
                                     }
                                 }
 
                                 @Override
                                 public int hashCode() {
                                     return ObjectUtils.hashCode(getKey());
                                 }
 
                                 @Override
                                 public String toString() {
                                     StringBuilder string = new StringBuilder();
                                     
                                     string.append(getKey());
                                     string.append('=');
                                     string.append(getValue());
 
                                     return string.toString();
                                 }
                             };
                         }
                     };
                 }
 
                 @Override
                 public int size() {
                     return size;
                 }
             };
         }
     }
 
     @Override
     public V get(Object key) {
         if (size < 0) {
             return ((Map<?, V>) delegate).get(key);
 
         } else {
             int index = indexOfKey(key);
 
             return index >= 0 ? ((V[]) delegate)[ARRAY_SIZE + index] : null;
         }
     }
 
     @Override
     public boolean isEmpty() {
         if (size < 0) {
             return ((Map<?, ?>) delegate).isEmpty();
 
         } else {
             return size == 0;
         }
     }
 
     @Override
     public Set<K> keySet() {
         if (size < 0) {
             return ((Map<K, ?>) delegate).keySet();
 
         } else {
             return new AbstractSet<K>() {
 
                 @Override
                 public Iterator<K> iterator() {
                     return new IndexedIterator<K>() {
 
                         @Override
                         public K doNext(int index) {
                             return ((K[]) delegate)[index];
                         }
                     };
                 }
 
                 @Override
                 public int size() {
                     return size;
                 }
             };
         }
     }
 
     @Override
     public V put(K key, V value) {
         if (size < 0) {
             return ((Map<K, V>) delegate).put(key, value);
 
         } else {
             if (delegate == null) {
                 delegate = new Object[ARRAY_SIZE * 2];
 
             } else {
                 int index = indexOfKey(key);
 
                 if (index >= 0) {
                     V oldValue = ((V[]) delegate)[ARRAY_SIZE + index];
                     ((V[]) delegate)[ARRAY_SIZE + index] = value;
 
                     return oldValue;
                 }
             }
 
             if (size * 2 >= ((Object[]) delegate).length) {
                 delegate = new LinkedHashMap<K, V>(this);
                 size = -1;
 
                 return put(key, value);
 
             } else {
                 Object[] delegateArray = (Object[]) delegate;
                 delegateArray[size] = key;
                 delegateArray[ARRAY_SIZE + size] = value;
                 ++ size;
 
                 return null;
             }
         }
     }
 
     @Override
     public void putAll(Map<? extends K, ? extends V> map) {
         for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
             put(entry.getKey(), entry.getValue());
         }
     }
 
     private void removeByIndex(int index) {
         -- size;
 
         System.arraycopy(delegate, index + 1, delegate, index, size - index);
         System.arraycopy(delegate, ARRAY_SIZE + index + 1, delegate, ARRAY_SIZE + index, size - index);
     }
 
     @Override
     public V remove(Object key) {
         if (size < 0) {
             return ((Map<?, V>) delegate).remove(key);
 
         } else {
             int index = indexOfKey(key);
 
             if (index < 0) {
                 return null;
 
             } else {
                 V oldValue = ((V[]) delegate)[ARRAY_SIZE + index];
 
                 removeByIndex(index);
                 return oldValue;
             }
         }
     }
 
     @Override
     public int size() {
         if (size < 0) {
             return ((Map<?, ?>) delegate).size();
 
         } else {
             return size;
         }
     }
 
     @Override
     public Collection<V> values() {
         if (size < 0) {
             return ((Map<?, V>) delegate).values();
 
         } else {
             return new AbstractCollection<V>() {
 
                 @Override
                 public Iterator<V> iterator() {
                     return new IndexedIterator<V>() {
 
                         @Override
                         public V doNext(int index) {
                             return ((V[]) delegate)[ARRAY_SIZE + index];
                         }
                     };
                 }
 
                 @Override
                 public int size() {
                     return size;
                 }
             };
         }
     }
 
     // --- Object support ---
 
     @Override
     public boolean equals(Object other) {
         if (this == other) {
             return true;
 
         } else if (other instanceof Map) {
             return entrySet().equals(((Map<?, ?>) other).entrySet());
 
         } else {
             return false;
         }
     }
 
     @Override
     public int hashCode() {
         return entrySet().hashCode();
     }
 
     @Override
     public String toString() {
         if (size < 0) {
             return ((Map<?, ?>) delegate).toString();
 
         } else {
             StringBuilder string = new StringBuilder();
 
             string.append('{');
 
             if (size > 0) {
                 Object[] delegateArray = (Object[]) delegate;
 
                 for (int i = 0; i < size; ++ i) {
                     string.append(delegateArray[i]);
                     string.append('=');
                     string.append(delegateArray[ARRAY_SIZE + i]);
                     string.append(',');
                 }
                 string.setLength(string.length() - 1);
             }
 
             string.append('}');
             return string.toString();
         }
     }
 
     private abstract class IndexedIterator<E> implements Iterator<E> {
 
         private int index = 0;
 
         @Override
         public boolean hasNext() {
             return index < size;
         }
 
         protected abstract E doNext(int index);
 
         @Override
         public E next() {
             if (hasNext()) {
                 E nextValue = doNext(index);
                 ++ index;
 
                 return nextValue;
 
             } else {
                 throw new NoSuchElementException();
             }
         }
 
         @Override
         public void remove() {
             removeByIndex(index);
         }
     }
 }
