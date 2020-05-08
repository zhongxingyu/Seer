 package org.peak15.newlife.types;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 /**
  * Like a Map, where both the keys and values are unique.
  * 
  * Use the Builder to incrementally create an immutable StaticBiMap.
  */
 public final class StaticBiMap<K, V> {
 	
 	private final Map<K, V> KVMap;
 	private final Map<V, K> VKMap = new HashMap<>();
 	
 	private StaticBiMap(Map<K, V> map) {
 		this.KVMap = map;
 		
 		for(Entry<K, V> e : this.KVMap.entrySet()) {
 			VKMap.put(e.getValue(), e.getKey());
 		}
 	}
 	
 	public Map<K, V> keyValueMap() {
 		return Collections.unmodifiableMap(this.KVMap);
 	}
 	
 	public Map<V, K> valueKeyMap() {
 		return Collections.unmodifiableMap(this.VKMap);
 	}
 	
 	public Set<K> keySet() {
 		return Collections.unmodifiableSet(this.keyValueMap().keySet());
 	}
 	
 	public Set<V> valueSet() {
 		return Collections.unmodifiableSet(this.valueKeyMap().keySet());
 	}
 	
 	public boolean containsKey(K key) {
 		return this.keyValueMap().containsKey(key);
 	}
 	
 	public boolean containsValue(V value) {
 		return this.valueKeyMap().containsKey(value);
 	}
 	
 	public V valueOf(K key) {
 		return this.keyValueMap().get(key);
 	}
 	
 	public K keyOf(V value) {
 		return this.valueKeyMap().get(value);
 	}
 	
 	@Override
 	public boolean equals(Object obj) {
 		if(obj == this) {
 			return true;
 		}
 		
 		if(!(obj instanceof StaticBiMap<?, ?>)) {
 			return false;
 		}
 		
 		StaticBiMap<?, ?> s = (StaticBiMap<?, ?>) obj;
 		
 		return s.keyValueMap().equals(this.keyValueMap());
 	}
 	
 	@Override
 	public int hashCode() {
 		int result = 9001;
 		result = 1327 * result + this.keyValueMap().hashCode();
 		return result;
 	}
 	
 	@Override
 	public String toString() {
 		return String.format("{BiMap: %d pairs}", this.keyValueMap().size());
 	}
 	
 	public static final class Builder<K, V> {
 		
 		private final Map<K, V> map = new HashMap<>();
 		
 		public Builder() {}
 		
 		public Builder(K firstKey, V firstValue) {
 			this.append(firstKey, firstValue);
 		}
 		
 		public Builder(Map<K, V> map) {
 			this.appendAll(map);
 		}
 		
 		/**
 		 * @param key to add
 		 * @param value to add
 		 * @return Builder with key and value added
 		 * @throws IllegalArgumentException if key or value is not unique.
 		 */
 		public Builder<K, V> append(K key, V value) {
 			if(map.containsKey(key) || map.containsValue(value)) {
 				throw new IllegalArgumentException("Key and value must be unique in the map.");
 			}
 			
 			this.map.put(key, value);
 			
 			return this;
 		}
 		
		public Builder<K, V> appendAll(Map<K, V> map) {
			for(Entry<K, V> e : map.entrySet()) {
 				this.append(e.getKey(), e.getValue());
 			}
 			
 			return this;
 		}
 		
 		public StaticBiMap<K, V> build() {
 			return new StaticBiMap<>(this.map);
 		}
 	}
 }
