 package com.braisgabin.dhtbalanced.utils;
 
 import java.util.HashMap;
 import java.util.Observer;
 
 public class MyHashMap<K, V> extends HashMap<K, V> {
	private static final long serialVersionUID = 1L;
 
 	private Observer ob;

 	public void setObserver(Observer ob) {
 		this.ob = ob;
 	}
 
 	public V put(K key, V value) {
 		V v = super.put(key, value);
 
 		if (ob != null) {
 			ob.update(null, null);
 		}
 
 		return v;
 	}
 }
