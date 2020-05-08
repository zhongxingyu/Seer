 package org.nchelp.meteor.util;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Hashtable;
 import java.util.List;
 
 /**
 * This is really just a Hashmap that has some additional logic
 * to expire the elements.
 * 
 * @version   $Revision$ $Date$
 * @since     Meteor1.0
 * 
 */
 public class Cache {
 
 	// Default to clear the cache every 12 hours
 	private int timeoutSeconds = 43200;
 	private Hashtable data = new Hashtable();
 	private Hashtable times = new Hashtable();
 	
 	/**
 	 * Return the cached object if available, otherwise returns null
 	 * @param key
 	 * @return List
 	 */
     public Object cached(Object key)
     {
    	if(key == null) {
    		return new ArrayList();
    	}
    	
     	// See if it is in the list
     	if(data.containsKey(key)){
     		// Check to make sure it hasn't timed out
     		Date currentDate = new Date();
 	   		Date storedDate = (Date)times.get(key);
 
 			// Calculate difference in dates
 			long numericalDifference = storedDate.getTime() - currentDate.getTime();
 			
 			// Divide by 1000 to find number of seconds difference
 			numericalDifference = numericalDifference / 1000;
 			
 			if(numericalDifference >= this.timeoutSeconds){
 				this.remove(key);
 				return new ArrayList();
 			}
 			
 			return data.get(key);
 
 		}
 		
  		return new ArrayList();
     }
     
 	/**
 	 * Add this object to the Cache and store the lookup key.
 	 * Also, save the timestamp that the oject was saved.
 	 * @param key
 	 * @param list
 	 */
     public void add(Object key, Object value)
     {
     	data.put(key, value);
     	times.put(key, new Date());
     	
     }
     
 	/**
 	 * Force the removal of the object with the given key.
 	 * @param key
 	 */
     public void remove(Object key){
     	if(data.containsKey(key)){
     		data.remove(key);
     	}
     	if(times.containsKey(key)){
     		times.remove(key);
     	}
     }
     
 	/**
 	 * Clear all elements from this Cache.
 	 */
     public void clear(){
     	data.clear();
     	times.clear();
     }
 }
 
