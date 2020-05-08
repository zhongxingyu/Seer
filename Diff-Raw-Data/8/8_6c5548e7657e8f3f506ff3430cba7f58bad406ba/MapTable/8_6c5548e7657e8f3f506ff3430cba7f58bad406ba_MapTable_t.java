 package niagara.utils.nitree;
 
 
 /** MapTable class to keep track of associations between NIElements
  * and dom elements - maybe should be more general than that - yes
  * just provides a mapping between two objects such that the mapped-to
  * object is referred to by a weak reference so it is deleted
  * appropriately - also need a queue or something to get rid
  * of the weakly referenced elements (java.lang.ref)
  */
 
 import java.lang.ref.*;
 import java.util.*;
 
 class WeakRefWithKey extends WeakReference {
 
     /* key which this map table value is associated with */
     private Object key; 
 
     /**
      * Constructor - create a new WeakRefWithKey
      *
      * @param referent - object reference should point to
      * @param q - queue that this weak reference should be registered with
      * @param key - key associated with this map table value
      *
      * @return Returns nothing
      */
     WeakRefWithKey(Object referent, ReferenceQueue q, Object _key) {
 	super(referent, q);
 	key = _key;
 	return;
     }
 
     /**
      * returns the key with which this map table value is associated
      * used for removing the key, value pair when the value becomes
      * weakly reachable
      */
     Object getKey() { return key; }
 
 }
 
 public class MapTable {
 
     /* MEMBERS */
    HashMap hashtable;
     ReferenceQueue refQueue;
 
     /* METHODS */
     
     /**
      * Constructor - allocates a new HashTable and ReferenceQueue
      */
     public MapTable() {
	hashtable = new HashMap(50000);
 	refQueue = new ReferenceQueue();
     }
 
     /**
      * looks up a key in the hash table and returns the associated
      * object - note this isn't synch because hashtable is synch
      *
      * @param key   key to be used to probe the hash table
      *
      * @return the object associated with the key, null if no 
      *         associated object found
      */            
     Object lookup(Object key) {
 	/* use key to probe a hash table - returns a weak
 	 * reference to object  - get the value of that
 	 * reference and return it
 	 */
 
 	/* first do cleanup if necessary */
 	checkWeakRefQueue();
 
 	WeakRefWithKey weakRef = (WeakRefWithKey)hashtable.get(key);
 
 	if(weakRef == null) {
 	    return null;
 	}
 
 	/* if get returns null, this means that this weak ref was cleared 
 	 * and enqueued in the time between the above call to checkWeakRefQueue 
 	 * and now.  Call checkWeakRefQueue() to get the obsolete key-value
 	 * pair out of the hash table and return null  
 	 */
 	Object retVal = weakRef.get();
 	if(retVal == null) {
 	    checkWeakRefQueue();
 	}
 	return retVal;
     }
 
     /**
      * inserts a key, value pair into the hash table
      *
      * @param key  the key of the object being inserted
      * @param value  the value of the object being inserted
      *
      * @return Returns any old value associated with key 
      */
     Object insert(Object key, Object value) {
 
 	/* first do cleanup if necessary */
 	checkWeakRefQueue();
 
 	/* insert key and a weak reference to value in a hash table
 	 * we use WeakRefWithKey class which extends WeakReference 
 	 */
 
 	/* create the map table value - don't synchronize
 	 * on refQueue here because I think this just creates
 	 * a ref to refQueue and doesn't do any functions on it
 	 */
        WeakRefWithKey weakRef = new WeakRefWithKey(value, refQueue, key);
 
        /* put it into the hash table - put returns the previous value
         * associated with the key, if any - should be no previous value
         */
        return hashtable.put(key, weakRef);
     }
     
     /**
      * delete the object referenced by key from the hash table 
      *
      * @param key - the key of the object to be removed
      *
      * @return the value to which key was mapped in the hash table - null if 
      *         there was no mapping
      */
     private Object remove(Object key) {
 	/* first do cleanup if necessary */
	/* nope gets us in a loop checkWeakRefQueue(); */
 
 	return hashtable.remove(key);
     }
 
     /**
      * checks the weak reference queue to see if there are any
      * weak references in the hash table (recall all "values" inserted
      * into the hash table are weak references) that have become finalizable
      * and weakly referenced
      * and cleared - if any are found - remove the references from
      * the hash table
      * This needs to be synchronized because refQueue is not synchronized
      */
     private synchronized void checkWeakRefQueue() {
 	
 	/* just poll the ref queue - will return immediately
 	 * if nothing in the queue - TODO do I want to catch
 	 * ClassCastException??
 	 */
 	WeakRefWithKey toRemove = (WeakRefWithKey)(refQueue.poll());
 
 	while(toRemove != null) {
 	    /* if we got a reference that has been recently
 	     * cleared (i.e. it was the only reference to 
 	     * the object it pointed to) - remove the
 	     * entry for this reference from the hash table
 	     */
 
 	    /* first check to see if the WeakReference extension
 	     * really worked... remove this once I know it's working
 	     */
 	    if(toRemove.getKey() == null) {
 		System.err.println("ERROR - WeakReference extension failed");
 	    }
 
 	    remove(toRemove.getKey());
 	    
 	    /* get next element from queue */
 	    toRemove = (WeakRefWithKey)(refQueue.poll());
 	}
 
 	return;
     }
 }
 
 
