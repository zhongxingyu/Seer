 package org.fazio.utils.map;
 
 import java.util.*;
 
 /**
  * @author Michael Fazio
  * @since 11/20/12 5:35 PM
  */
 public class CountingHashMap<K, V extends Number>
 	extends LinkedHashMap<K, Number>
 	implements CountingMap<K, V> {
 
 	public void put(final K key) {
 		this.put(key, 0);
 	}
 
 	public void putAll(final Collection<K> keys) {
 		for(K key : keys) this.put(key);
 	}
 
 	public void reset() {
 		this.setAll(0);
 	}
 
 	public void setAll(final Number amount) {
 		for(K key : this.keySet()) {
 			this.put(key, amount);
 		}
 	}
 
 	public void increaseCount(final K key) {
 		this.increaseCount(key, 1);
 	}
 
 	public void increaseCount(final K key, final Number amount) {
 		if(!this.containsKey(key)) this.put(key);
 		this.put(key, this.get(key).longValue() + amount.longValue());
 	}
 
 	public void decreaseCount(final K key) {
 		this.increaseCount(key, -1);
 	}
 
 	public void decreaseCount(final K key, final Number amount) {
 		this.increaseCount(key, -amount.longValue());
 	}
 
 	public void sortByKeys() {
 		this.sortByKeys(true);
 	}
 
 	public void sortByKeys(final boolean lowToHigh) {
 		this.sortMap(lowToHigh, new Comparator<CountingMap.Entry<K, Number>>() {
 			public int compare(final CountingMap.Entry<K, Number> kNumberEntry1, final CountingMap.Entry<K, Number> kNumberEntry2) {
 				final String k1 = kNumberEntry1.getKey().toString();
 				final String k2 = kNumberEntry2.getKey().toString();
 
 				return lowToHigh ? k1.compareToIgnoreCase(k2) : k2.compareToIgnoreCase(k1);
 			}
 		});
 	}
 
 	public void sortByCounts() {
 		this.sortByCounts(true);
 	}
 
 	public void sortByCounts(final boolean lowToHigh) {
 		this.sortMap(lowToHigh, new Comparator<CountingMap.Entry<K, Number>>() {
 			public int compare(final CountingMap.Entry<K, Number> kNumberEntry1, final CountingMap.Entry<K, Number> kNumberEntry2) {
 				final long v1 = kNumberEntry1.getValue().longValue();
 				final long v2 = kNumberEntry2.getValue().longValue();
 
 				if(v1 == v2) return 0;
 				if(v1 > v2) return lowToHigh ? 1 : -1;
 				else return lowToHigh ? -1 : 1;
 			}
 		});
 	}
 
 	//TODO: Do I need lowToHigh here?
 	public void sortMap(final boolean lowToHigh, final Comparator<CountingMap.Entry<K, Number>> comparator) {
 		final List<CountingMap.Entry<K, Number>> entryList = new ArrayList<Map.Entry<K, Number>>(this.entrySet());
 
 		Collections.sort(entryList, comparator);
 
 		this.clear();
 
 		for(CountingMap.Entry<K, Number> entry : entryList) {
 			this.put(entry.getKey(), entry.getValue());
 		}
 	}
 
 	@Override
 	public String toString() {
 		final StringBuilder sb = new StringBuilder();
 
 		for(CountingMap.Entry<K, Number> entry : this.entrySet()) {
 			sb
				.append(entry.getKey().toString())
 				.append(" = ")
				.append(entry.getValue().toString())
 				.append('\n');
 		}
 
 		return sb.toString();
 	}
 }
