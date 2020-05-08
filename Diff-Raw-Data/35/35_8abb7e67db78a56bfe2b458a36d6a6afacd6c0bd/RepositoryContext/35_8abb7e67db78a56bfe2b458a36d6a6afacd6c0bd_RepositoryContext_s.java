 package com.technosophos.rhizome.repository;
 
 import java.util.HashMap;
 
 /**
  * Configuration Storage for Repository.
  * This class is for creating a container for parameters
  * that need to be passed on to the other components of 
  * a repository.
  * 
  * The RepositoryContext takes String keys and String values.
  * 
  * @author mbutcher
  *
  */
 public class RepositoryContext {
 	
 	protected HashMap<String,String> ctx;
 	
 	/**
 	 * Create a new context.
 	 *
 	 */
 	public RepositoryContext() {
 		this(new HashMap<String,String>());
 		
 	}
 	
 	/**
 	 * Create a new context with a given HashMap of keys and values.
 	 * 
 	 * The hash map must have only strings for keys and values.
 	 * @param map
 	 */
 	public RepositoryContext(HashMap<String, String> map) {
 		this.ctx = map;
 	}
 
 	/**
 	 * Given a key, fetch a value.
 	 * 
 	 * This will return the value for the key given. If the key does
 	 * not exist, this will return a NULL value (rather than throw an exception).
 	 * @param key
 	 * @return the value corresponding to that key, or null.
 	 */
 	public String getParam(String key) {	
 		if(this.ctx.containsKey(key)) return this.ctx.get(key);
 		return null;
 	}
 
 	/**
 	 * Check to see if this key is present.
 	 * 
 	 * Returns true if this key exists. The key value may still be NULL.
 	 * @param key
 	 * @return true if the key exists
 	 */
 	public boolean hasKey(String key) {
 		return this.ctx.containsKey(key);
 	}
 	
 	/**
 	 * Set a particular key/value.
 	 * 
 	 * Insert or overwrite a key in the context.
 	 * @param key
 	 * @param value
 	 */
	public void setParam(String key, String value) {
 		this.ctx.put(key, value);
 	}
 	
 	/**
 	 * This replaces the existing keys/vals with the ones in the new map.
 	 * 
 	 * This will overwrite the old keys and values with a new one.
 	 * @param map
 	 */
 	public void replaceParams(HashMap<String, String> map) {
 		this.ctx = map;
 	}
 }
