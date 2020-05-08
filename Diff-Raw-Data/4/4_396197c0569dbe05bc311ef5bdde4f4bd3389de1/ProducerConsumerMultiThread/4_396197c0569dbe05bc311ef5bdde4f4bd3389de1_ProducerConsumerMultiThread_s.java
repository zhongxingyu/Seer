 package com.practice.java;
 
 import java.util.List;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 public class ProducerConsumerMultiThread implements Event {
 
 	// TODO Write Test Case
 
 	private ConcurrentMap<String, List<Listener>> items = new ConcurrentHashMap<String, List<Listener>>();
 
 	@Override
 	public void register(String source, Listener listener) {
 		List<Listener> listeners = new CopyOnWriteArrayList<Listener>();
 		List<Listener> putIfAbsent = items.putIfAbsent(source, listeners);
 		if (putIfAbsent != null) {
 			putIfAbsent.add(listener);
 		} else {
 			listeners.add(listener);
 		}
 	}
 
 	@Override
 	public void deregister(String source, Listener listener) {
 		List<Listener> list = items.get(source);
 		if (list != null) {
 			list.remove(listener);
			items.remove(source, list);
 		}
 	}
 
 	@Override
 	public void notifyAllItems(String source) {
 		List<Listener> list = items.get(source);
 		for (Listener listener : list) {
 			listener.notifyMe();
 		}
 	}
 
 }
 
 interface Event {
 	void register(String source, Listener listener);
 
 	void deregister(String source, Listener listener);
 
 	void notifyAllItems(String source);
 }
 
 interface Listener {
 	void notifyMe();
 }
 
 class ListenerImpl implements Listener {
 	String str;
 
 	public ListenerImpl(String str) {
 		this.str = str;
 	}
 
 	@Override
 	public void notifyMe() {
 		System.out.println(str);
 	}
 }
