 package org.darkquest.ls.model.collections;
 
 import java.util.LinkedHashMap;
 
 import java.util.Collection;
import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.ArrayList;
 import java.util.Map.Entry;
 
 import org.darkquest.ls.model.PlayerSave;
 
 /**
  * Least Recently Used Algorithm
  * @author openfrog
  *
  * @param <K>
  * @param <V>
  */
 
 public class LRUCache<K,V> {
 
 	private static final float hashTableLoadFactor = 0.75f;
 	private LinkedHashMap<K,V> map;
 	private LinkedList<V> list;
 	private int cacheSize;
 
 	public LRUCache(int cacheSize) {
 		this.cacheSize = cacheSize;
 		int hashTableCapacity = (int) Math.ceil(cacheSize / hashTableLoadFactor) + 1;
 		this.list = new LinkedList<V>();
 		this.map = new LinkedHashMap<K,V>(hashTableCapacity, hashTableLoadFactor, true) { // (an anonymous inner class)
 		private static final long serialVersionUID = 1;
 		@Override 
 		protected boolean removeEldestEntry(Map.Entry<K,V> eldest) { // basically hackish, we need to predict who is first
 			boolean shouldRemoveEldest = size() > LRUCache.this.cacheSize;
 			if(shouldRemoveEldest) {
 				if(!list.isEmpty() && list.getFirst() instanceof PlayerSave) {
 					PlayerSave save = (PlayerSave) list.getFirst(); // obtain head
 					System.out.println("Oldest entry is... " + save.getUsername());
 					save.save();
 					list.removeFirst(); // remove head
 				}
 			}
 			return shouldRemoveEldest; 
 		}}; 
 	}
 	
 	public LRUCache() {
 		this.cacheSize = 25; // 25
 		int hashTableCapacity = (int) Math.ceil(cacheSize / hashTableLoadFactor) + 1;
 		this.list = new LinkedList<V>();
 		this.map = new LinkedHashMap<K,V>(hashTableCapacity, hashTableLoadFactor, true) { // (an anonymous inner class)
 		private static final long serialVersionUID = 1;
 		@Override 
 		protected boolean removeEldestEntry(Map.Entry<K,V> eldest) { // basically hackish, we need to predict who is first
 			boolean shouldRemoveEldest = size() > LRUCache.this.cacheSize;
 			if(shouldRemoveEldest) {
 				if(!list.isEmpty() && list.getFirst() instanceof PlayerSave) {
 					PlayerSave save = (PlayerSave) list.getFirst(); // obtain head
 					System.out.println("Oldest entry is... " + save.getUsername());
 					save.save();
 					list.removeFirst(); // remove head
 				}
 			}
 			return shouldRemoveEldest; 
 		}}; 
 	}
 	
 	/**
	 * Iterator
 	 * @return iterator
 	 */
 	
	public Iterator<Entry<K, V>> iterator() {
		return map.entrySet().iterator();
 	}
 	
 	/**
 	 * Sets cache size (not in use yet)
 	 * @param cacheSize
 	 */
 	
 	public synchronized void setCacheSize(int cacheSize) {
 		this.cacheSize = cacheSize;
 	}
 
 	/**
 	 * Retrieves an entry from the cache.<br>
 	 * The retrieved entry becomes the MRU (most recently used) entry.
 	 * @param key the key whose associated value is to be returned.
 	 * @return    the value associated to this key, or null if no value with this key exists in the cache.
 	 */
 	public synchronized V get(K key) {
 		return map.get(key); 
 	}
 
 	/**
 	 * Adds an entry to this cache.
 	 * The new entry becomes the MRU (most recently used) entry.
 	 * If an entry with the specified key already exists in the cache, it is replaced by the new entry.
 	 * If the cache is full, the LRU (least recently used) entry is removed from the cache.
 	 * @param key    the key with which the specified value is to be associated.
 	 * @param value  a value to be associated with the specified key.
 	 */
 	public synchronized void put(K key, V value) {
 		map.put(key, value); 
 		list.add(value); // add to tail
 	}
 	
 	/**
 	 * Removes the given key from cache
 	 * @param key
 	 */
 	public synchronized void remove(K key) {
 		map.remove(key);
 		list.remove(key);
 	}
 
 	/**
 	 * Checks if the given map contains a key
 	 * @param key
 	 * @return
 	 */
 	public synchronized boolean contains(K key) {
 		return map.containsKey(key);
 	}
 
 	/**
 	 * Clears the cache.
 	 */
 	public synchronized void clear() {
 		map.clear();
 		list.clear();
 	}
 
 	/**
 	 * Returns the number of used entries in the cache.
 	 * @return the number of entries currently in the cache.
 	 */
 	public synchronized int usedEntries() {
 		return map.size(); 
 	}
 
 	/**
 	 * Returns a <code>Collection</code> that contains a copy of all cache entries.
 	 * @return a <code>Collection</code> with a copy of the cache content.
 	 */
 	public synchronized Collection<Map.Entry<K,V>> getAll() {
 		return new ArrayList<Map.Entry<K,V>>(map.entrySet()); 
 	}
 } 
 
