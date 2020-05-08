 package org.kryomq;
 
 import java.util.Collections;
import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 
import org.kryomq.kryonet.Connection;

 public class TopicRegistry<T> {
 	protected Map<String, Set<T>> registry = new ConcurrentHashMap<String, Set<T>>();
 	
 	public void subscribe(String topic, T subscriber) {
 		synchronized(registry) {
 			if(!registry.containsKey(topic))
 				registry.put(topic, Collections.synchronizedSet(new HashSet<T>()));
 		}
 		Set<T> subs = registry.get(topic);
 		subs.add(subscriber);
 	}
 	
 	public Set<T> unsubscribe(String topic, T subscriber) {
 		Set<T> subs = registry.get(topic);
 		if(subs == null)
 			return Collections.emptySet();
 		subs.remove(subscriber);
 		return subs;
 	}
 	
 	public Set<T> get(String topic) {
 		Set<T> subs = registry.get(topic);
 		if(subs == null)
 			return Collections.emptySet();
 		return Collections.unmodifiableSet(subs);
 	}
 	
 	public void deregister(T subscriber) {
 		synchronized(registry) {
 			for(Set<T> subs : registry.values()) {
 				subs.remove(subscriber);
 			}
 		}
 	}
 }
