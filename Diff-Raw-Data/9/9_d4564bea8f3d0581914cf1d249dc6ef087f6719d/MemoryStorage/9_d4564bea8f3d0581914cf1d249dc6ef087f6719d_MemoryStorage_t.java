 package com.commsen.guicelet.util;
 
 import javax.servlet.http.HttpServletRequest;
 
 public class MemoryStorage {
 
	@SuppressWarnings("unchecked")
 	public static <T> T getOrCreate (HttpServletRequest req, Class<T> clazz) {
 		String key = clazz.getName();
 		Object o = req.getAttribute(key); 
 		T t;
 		if (o == null) {
 			try {
 				t = clazz.newInstance();
 			} catch (InstantiationException e) {
 				throw new IllegalArgumentException("Can not create new instance of class " + clazz, e);
 			} catch (IllegalAccessException e) {
 				throw new IllegalArgumentException("Can not create new instance of class " + clazz, e);
 			}
 			req.setAttribute(key, t);
 		} else {
 			if (!(o.getClass().equals(clazz))) {
 				throw new IllegalStateException("Key " + key + " contains value which is not of type " + clazz);
 			}
 			t = (T)o;
 		}
 		return t;
 	}
 
 	
	@SuppressWarnings("unchecked")
 	public static <T> T get (HttpServletRequest req, Class<T> clazz) {
 		String key = clazz.getName();
 		Object o = req.getAttribute(key); 
 		if (o == null || !(o.getClass().equals(clazz))) {
 			return null;
 		}
 		return (T)o;
 	}
 
 }
