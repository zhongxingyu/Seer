 package com.ccesun.framework.plugins.cache;
 
 import java.io.Serializable;
 import java.net.URL;
 
 import javax.annotation.PostConstruct;
 
 import net.sf.ehcache.Cache;
 import net.sf.ehcache.CacheManager;
 import net.sf.ehcache.Element;
 
 import org.springframework.context.annotation.Lazy;
 import org.springframework.stereotype.Component;
 
 @Component
 @Lazy
 public class CacheHelper {
 	
 	private CacheManager manager;
 	
 	@PostConstruct
 	public void init() {
 		URL url = getClass().getResource("/ehcache.xml");
 		manager = CacheManager.newInstance(url);
		manager.addCache("defaultCache");
 	}
 	
 	private void put(Cache cache, Serializable key, Serializable value) {
 		Element element = new Element(key, value);
		element.setEternal(false);
 		cache.put(element);
 	}
 	
 	public void put(String cacheName, Serializable key, Serializable value) {
 		Cache cache = manager.getCache(cacheName);
 		put(cache, key, value);
 	}
 	
 	public void put(Serializable key, Serializable value) {
 		Cache cache = manager.getCache("defaultCache");
 		put(cache, key, value);
 	}
 	
 	@SuppressWarnings("unchecked")
 	private <T> T get(Cache cache, Serializable key) {
		Element element = cache.get(key);
		return (element == null) ? null : (T) element.getObjectValue();
 	}
 	
 	public <T> T get(Serializable key) {
 		Cache cache = manager.getCache("defaultCache");
 		return get(cache, key);
 	}
 	
 	public <T> T get(String cacheName, Serializable key) {
 		Cache cache = manager.getCache(cacheName);
 		return get(cache, key);
 	}
 	
 	private boolean remove(Cache cache, Serializable key) {
 		return cache.remove(key);
 	}
 	
 	public boolean remove(Serializable key) {
 		Cache cache = manager.getCache("defaultCache");
 		return remove(cache, key);
 	}
 	
 	public boolean remove(String cacheName, Serializable key) {
 		Cache cache = manager.getCache(cacheName);
 		return remove(cache, key);
 	}
 }
