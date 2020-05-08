 package com.brightcove.johnny.coll;
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableMultiset;
 import com.google.common.collect.Multimap;
 import com.google.common.collect.Multiset;
 
 import clojure.lang.APersistentVector;
 import clojure.lang.IEditableCollection;
 import clojure.lang.IMapEntry;
 import clojure.lang.IPersistentMap;
 import clojure.lang.IPersistentVector;
 import clojure.lang.ISeq;
 import clojure.lang.ITransientCollection;
 import clojure.lang.ITransientMap;
 import clojure.lang.PersistentHashMap;
 import clojure.lang.PersistentHashSet;
 import clojure.lang.PersistentList;
 import clojure.lang.PersistentVector;
 import clojure.lang.SeqIterator;
 
 /**
  * Immutable multimap implementation that takes advantage of structure-sharing
  * where possible. Modifications are generally O(n log n), and value searches
  * are linear in the size of the search space. Keys must be hashable.
  * <p>
  * Originally implemented using Clojure's {@link PersistentHashMap} and
  * {@link PersistentVector}, although this detail is subject to change
  * at any time.
  *
  * @param <K> Type of keys
  * @param <V> Type of values
  */
 public class PersistentMultimap<K, V> implements Multimap<K, V>{
 
     /** Map of K to PersistentVector of V. */
     private final IPersistentMap store;
     private final int count;
 
     /**
      * Create an empty multimap.
      */
     public PersistentMultimap() {
         store = PersistentHashMap.EMPTY;
         count = 0;
     }
 
     private PersistentMultimap(IPersistentMap store, int count) {
         this.store = store;
         this.count = count;
     }
 
     /**
      * Get a non-empty sublist for the key (may be missing from map.)
      */
     private APersistentVector getList(K key) {
         IMapEntry e = store.entryAt(key);
         if (e == null || e.getValue() == null) {
             return PersistentVector.EMPTY;
         } else {
             return (APersistentVector) e.getValue();
         }
     }
 
     private static APersistentVector into(APersistentVector base, Iterator<?> more) {
         ITransientCollection list = ((IEditableCollection) base).asTransient();
         for (; more.hasNext();) {
             list = list.conj(more.next());
         }
         return (APersistentVector) list.persistent();
     }
 
     /**
      * Produce a non-null seq on the store.
      */
     @SuppressWarnings("unchecked")
     private Collection<Map.Entry<K, IPersistentVector>> nnSeq() {
         ISeq s = store.seq();
         return (Collection<Map.Entry<K, IPersistentVector>>) (s == null ? PersistentList.EMPTY : s);
     }
 
     /*== Interface impl ==*/
 
     /**
      * Returns number of key-value pairs in constant time.
      */
     public int size() {
         return count;
     }
 
     /**
      * Returns number of keys in constant time.
      */
     public int length() {
         return store.count();
     }
 
     public boolean isEmpty() {
         return store.count() == 0;
     }
 
     public boolean containsKey(Object key) {
         return store.containsKey(key);
     }
 
     /**
      * Searches multimap for the specified value under any key.
      * Linear in {@link #size()}.
      */
     public boolean containsValue(Object value) {
         for (Map.Entry<K, IPersistentVector> list : nnSeq()) {
             if (((APersistentVector) list.getValue()).contains(value)) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Searches multimap for the specified value under one key.
      * Linear in size of {@link #get(Object)}.
      */
     public boolean containsEntry(Object key, Object value) {
         APersistentVector list = (APersistentVector) store.entryAt(key);
         return list != null && list.contains(value);
     }
 
     /** @see #cons(Object, Object) */
     @Deprecated
     public boolean put(K key, V value) {
         throw new UnsupportedOperationException("Mutation not supported on PersistentMultimap.");
     }
 
     /** Add this key-value pair to the multimap. */
     public PersistentMultimap<K, V> cons(K key, V value) {
         return new PersistentMultimap<K, V>(store.assoc(key, getList(key).cons(value)), count + 1);
     }
 
     /** @see #consAll(Object, Iterable) */
     @Deprecated
     public boolean putAll(K key, Iterable<? extends V> values) {
         throw new UnsupportedOperationException("Mutation not supported on PersistentMultimap.");
     }
 
     /** Add pairs of the key to each value. */
     public PersistentMultimap<K, V> consAll(K key, Iterable<? extends V> values) {
         Iterator<? extends V> iter = values.iterator();
         if (!iter.hasNext()) {
             return this; // avoid creating empty key
         }
         APersistentVector list = getList(key);
         int oldCount = list.size();
         list = into(list, iter);
         int added = list.size() - oldCount;
         return new PersistentMultimap<K, V>(store.assoc(key, list), count + added);
     }
 
     /** @see #putAll(Multimap) */
     @Deprecated
     public boolean putAll(Multimap<? extends K, ? extends V> multimap) {
         throw new UnsupportedOperationException("Mutation not supported on PersistentMultimap.");
     }
 
     /** Add pairs of the key to each value. */
     public PersistentMultimap<K, V> consAll(Multimap<K, V> multimap) { // TODO should be <? extends K, ? extends V> to match
         ITransientMap build = (ITransientMap) ((IEditableCollection) store).asTransient();
         for (K key : multimap.keySet()) {
             APersistentVector replacement = into(getList(key), multimap.get(key).iterator());
             if (!replacement.isEmpty()) { // not sure if the multimap can have keys -> empty value lists
                 build = build.assoc(key, replacement);
             }
         }
         return new PersistentMultimap<K, V>(build.persistent(), count + multimap.size());
     }
 
     /** Add pairs of keys to values. */
     public PersistentMultimap<K, V> consAll(Iterable<Map.Entry<K, V>> pairs) {
         ITransientMap build = (ITransientMap) ((IEditableCollection) store).asTransient();
         int added = 0;
         for (Map.Entry<K, V> e : pairs) {
            build = build.assoc(e.getKey(), getList(e.getKey()).cons(e.getValue()));
             added++;
         }
         return new PersistentMultimap<K, V>(build.persistent(), count + added);
     }
 
     /** @see #assoc(Object, Iterable) */
     @Deprecated
     public Collection<V> replaceValues(K key, Iterable<? extends V> values) {
         throw new UnsupportedOperationException("Mutation not supported on PersistentMultimap.");
     }
 
     /** Associate these (and only these) values for the key. */
     public PersistentMultimap<K, V> assoc(K key, Iterable<? extends V> values) {
         APersistentVector replacement = into(PersistentVector.EMPTY, values.iterator());
         if (replacement.isEmpty()) {
             return dissoc(key);
         } else {
             return new PersistentMultimap<K, V>(store.assoc(key, replacement),
                                                 count + replacement.count() - getList(key).count());
         }
     }
 
     /** @see #dissoc(Object) */
     @Deprecated
     public Collection<V> removeAll(Object key) {
         throw new UnsupportedOperationException("Mutation not supported on PersistentMultimap.");
     }
 
     /** Remove all values for a key. (Dissociate the key.) */
     public PersistentMultimap<K, V> dissoc(K key) {
         return new PersistentMultimap<K, V>(store.without(key), count - getList(key).count());
     }
 
     /** @see #dropAll(Object, Object) */
     @Deprecated
     public boolean remove(Object key, Object value) {
         throw new UnsupportedOperationException("Mutation not supported on PersistentMultimap.");
     }
 
     private Integer findLast(APersistentVector vals, V val) {
         for (int i = vals.length() - 1; i >= 0; i--) {
             Object cur = vals.get(i);
             if (val == null ? cur == null : val.equals(cur)) {
                 return i;
             }
         }
         return null;
     }
 
     /** Remove last matching value for a key. */
     @SuppressWarnings("unchecked")
     public PersistentMultimap<K, V> dropLast(K key, V val) {
         APersistentVector vals = getList(key);
         Integer foundAt = findLast(vals, val);
         if (foundAt == null) {
             return this;
         } else if (vals.length() == 1) {
             return dissoc(key); // removing only val
         } else {
             return assoc(key, new Concat<V>(vals.subList(0, foundAt),
                                             vals.subList(foundAt + 1, vals.size())));
         }
     }
 
     /** Remove all matching values for a key. */
     public PersistentMultimap<K, V> dropAll(K key, V val) {
         ITransientCollection smallerT = PersistentVector.EMPTY.asTransient();
         int removed = 0;
         for (Object o : getList(key)) {
             if (val == null ? o == null : val.equals(o)) {
                 removed++;
             } else {
                 smallerT = smallerT.conj(o);
             }
         }
         if (removed == getList(key).size()) {
             return dissoc(key);
         } else {
             return new PersistentMultimap<K, V>(store.assoc(key, smallerT.persistent()), count - removed);
         }
     }
 
     /** @see #empty() */
     @Deprecated
     public void clear() {
         throw new UnsupportedOperationException("Mutation not supported on PersistentMultimap.");
     }
 
     /** Remove all key-value pairs. */
     public PersistentMultimap<K, V> empty() {
         return new PersistentMultimap<K, V>();
     }
 
     /**
      * Returns immutable ordered collection of values for a key.
      * @return Non-null, but possibly empty list of values.
      */
     @SuppressWarnings("unchecked")
     public List<V> get(K key) {
         return (List<V>) getList(key);
     }
 
     /** Returns immutable set of keys in map. */
     @SuppressWarnings("unchecked")
     public Set<K> keySet() {
         Set<Object> s = ((PersistentHashMap) store).keySet();
         return (Set<K>) (s == null ? PersistentHashSet.EMPTY : s);
     }
 
     public Multiset<K> keys() {
         ImmutableMultiset.Builder<K> builder = ImmutableMultiset.builder();
         for (Map.Entry<K, IPersistentVector> e : (Collection<Map.Entry<K, IPersistentVector>>) nnSeq()) {
             builder.addCopies(e.getKey(), e.getValue().count());
         }
         return builder.build();
     }
 
     @SuppressWarnings("unchecked")
     public Collection<V> values() {
         ImmutableList.Builder<V> builder = ImmutableList.builder();
         for (Map.Entry<K, IPersistentVector> e : (Collection<Map.Entry<K, IPersistentVector>>) nnSeq()) {
             builder.addAll(new SeqIterator(e.getValue().seq()));
         }
         return builder.build();
     }
 
     /** Return an immutable collection of all key-value pairs in arbitrary order. */
     @SuppressWarnings("unchecked")
     public Collection<Map.Entry<K, V>> entries() {
         ImmutableList.Builder<Map.Entry<K, V>> builder = ImmutableList.builder();
         for (Map.Entry<K, IPersistentVector> e : nnSeq()) {
             for (V v : (Collection<V>) e.getValue().seq()) {
                 builder.add(new MapEntry<K, V>(e.getKey(), v));
             }
         }
         return builder.build();
     }
 
     /**
      * Returns immutable view of multimap of map of keys to collections of values.
      */
     @SuppressWarnings("unchecked")
     public Map<K, Collection<V>> asMap() {
         return (Map<K, Collection<V>>) store;
     }
 }
