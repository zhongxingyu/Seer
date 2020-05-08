 /* HashTableChained.java */
 
 package dict;
 import list.*;
 
 /**
  *  HashTableChained implements a Dictionary as a hash table with chaining.
  *  All objects used as keys must have a valid hashCode() method, which is
  *  used to determine which bucket of the hash table an entry is stored in.
  *  Each object's hashCode() is presumed to return an int between
  *  Integer.MIN_VALUE and Integer.MAX_VALUE.  The HashTableChained class
  *  implements only the compression function, which maps the hash code to
  *  a bucket in the table's range.
  *
  *  DO NOT CHANGE ANY PROTOTYPES IN THIS FILE.
  **/
 
 public class HashTableChained implements Dictionary {
 
 	/**
 	 *  Place any data fields here.
 	 **/
 	DList[] htable;
 	protected int size; //STRICTLY the number of entries, elements
 
 	/** 
 	 *  Construct a new empty hash table intended to hold roughly sizeEstimate
 	 *  entries.  (The precise number of buckets is up to you, but we recommend
 	 *  you use a prime number, and shoot for a load factor between 0.5 and 1.)
 	 **/
 
 	public HashTableChained(int sizeEstimate) {
 		int N = 10*sizeEstimate / 7;
 		while (isPrime(N) != true) {
 			N++;
 		}
 		htable = new DList[N];
 		size = 0;
 	}
 
 
 	public static boolean isPrime(int n) {
 		for (int divisor = 2; divisor < n; divisor++) { 
 			if (n % divisor == 0) {                       
 				return false;                                 
 			}                                                
 		}
 		return true;
 	}
 
 	/** 
 	 *  Construct a new empty hash table with a default size.  Say, a prime in
 	 *  the neighborhood of 100.
 	 **/
 
 	public HashTableChained() {
 		htable = new DList[1];
 		htable[0] = new DList();
 		size = 0;
 	}
 
 	/**
 	 *  Converts a hash code in the range Integer.MIN_VALUE...Integer.MAX_VALUE
 	 *  to a value in the range 0...(size of hash table) - 1.
 	 *
 	 *  This function should have package protection (so we can test it), and
 	 *  should be used by insert, find, and remove.
 	 **/
 
 	int compFunction(int code) {
 		//	  System.out.println("comp fxn: " + (Math.abs(9413*code + 439)%12981433) % htable.length);
 		return (Math.abs(9413*code + 439)%12981433) % htable.length;
 	}
 
 
 
 	/** 
 	 *  Returns the number of entries stored in the dictionary.  Entries with
 	 *  the same key (or even the same key and value) each still count as
 	 *  a separate entry.
 	 *  @return number of entries in the dictionary.
 	 **/
 
 	public int size() {
 		return size;
 	}
 
 	public int hlength() {
 		return htable.length;
 	}
 
 	/** 
 	 *  Tests if the dictionary is empty.
 	 *
 	 *  @return true if the dictionary has no entries; false otherwise.
 	 **/
 
 	public boolean isEmpty() {
//		int i = 0;
//		while(i < htable.length){
//			if(htable[i] != null){
//				return false;
//			}
//			i++;
//		}
//		return true;
 		return size == 0;
 	}
 
 	/**
 	 *  Create a new Entry object referencing the input key and associated value,
 	 *  and insert the entry into the dictionary.  Return a reference to the new
 	 *  entry.  Multiple entries with the same key (or even the same key and
 	 *  value) can coexist in the dictionary.
 	 *
 	 *  This method should run in O(1) time if the number of collisions is small.
 	 *
 	 *  @param key the key by which the entry can be retrieved.
 	 *  @param value an arbitrary object.
 	 *  @return an entry containing the key and value.
 	 **/
 
 	//  public Entry insert(Object key, Object value) {
 	//	  if ((double)(numEntries/numBuckets) >= 0.75) {
 	//		  updateTable(numBuckets*2);
 	//	  }
 	//	  Entry newEntry = new Entry(key, value);
 	//	  htable[compFunction(key.hashCode())].back().insertAfter(newEntry);
 	//	  numEntries++;
 	//	  return newEntry;
 	//  }
 
 
 	public HashEntry insert(Object key, int value) {
 		if (find(key) == null || !find(key).key().equals(key)){
 			if ((double)(size/htable.length) >= 0.75) {
 				updateTable(htable.length*2);
 			}
 			HashEntry newEntry = new HashEntry(key, value);
			if(htable[compFunction(key.hashCode())] == null){
				DList news = new DList();
				htable[compFunction(key.hashCode())] = news;
				htable[compFunction(key.hashCode())].insertBack(newEntry);
			} else{
				htable[compFunction(key.hashCode())].insertBack(newEntry);
			}
 			size++;
 			return newEntry;
 		}
 		else {
 			return null;
 		}
 	}
 
 	/** 
 	 *  Search for an entry with the specified key.  If such an entry is found,
 	 *  return it; otherwise return null.  If several entries have the specified
 	 *  key, choose one arbitrarily and return it.
 	 *
 	 *  This method should run in O(1) time if the number of collisions is small.
 	 *
 	 *  @param key the search key.
 	 *  @return an entry containing the key and an associated value, or null if
 	 *          no entry contains the specified key.
 	 **/
 
 	public HashEntry find(Object key) {
 		int h = key.hashCode();
 		int hz = compFunction(h);
 		DListNode pointer = (DListNode) htable[hz].front();
 		for(int j = 0; j < htable[hz].length(); j++) {
 			if(((HashEntry) pointer.item()).key().equals(key)){
 				return ((HashEntry) pointer.item());    			
 			}
 			pointer = (DListNode) pointer.next();
 		}
 		return null;
 	}
 
 	/** 
 	 *  Remove an entry with the specified key.  If such an entry is found,
 	 *  remove it from the table and return it; otherwise return null.
 	 *  If several entries have the specified key, choose one arbitrarily, then
 	 *  remove and return it.
 	 *
 	 *  This method should run in O(1) time if the number of collisions is small.
 	 *
 	 *  @param key the search key.
 	 *  @return an entry containing the key and an associated value, or null if
 	 *          no entry contains the specified key.
 	 */
 
 	public HashEntry remove(Object key) {
 		if ((double)(size/htable.length) <= 0.25) {
 			updateTable(htable.length/2);
 		}
 		int numTable = compFunction(key.hashCode());
 		HashEntry removed = (HashEntry) htable[numTable].front().remove(); //Works because never more than one entry in bucket
 		if (removed != null) {
 			size--;
 		}
 		return removed;
 	}
 
 	//  public Entry remove(Object key) {
 	//	    int h = key.hashCode();
 	//	    int hz = compFunction(h);
 	//	    ListNode pointer = htable[hz].front();
 	//	    	for(int j = 0; j < htable[hz].length(); j++) {
 	//	    		if(((Entry) pointer.item()).key().equals(key)){
 	//	    	    	Entry removed = (Entry) pointer.item();
 	//	    	    	pointer.remove();
 	//	    	    	size--;
 	//	    	    	return removed;	 			
 	//	    		}
 	//	    	}   	
 	//	    return null;
 	//  }
 
 	/**
 	 *  Remove all entries from the dictionary.
 	 */
 	public void makeEmpty() {
 		htable = new DList[htable.length];
 		//    for(int i = 0; i < this.size(); i++){
 		//    	htable[i] = new SList();
 		//    }
 		size = 0;
 	}
 
 	//  public int collisions() {
 	//	  int count = 0;
 	//	  for(int i = 0; i < htable.length; i++){
 	//		  if(htable[i] != null) {
 	//			  count = count + htable[i].length()-1;
 	//		  }
 	//	  }
 	//	  return count;
 	//  }
 
 	private void updateTable(int newSize) {
 		HashTableChained newTable = new HashTableChained(newSize);
 		int counter = 0;
 		for (DList bucket : htable) {
 			DListNode curr = (DListNode) bucket.front();
 			while (counter < size) {
 				newTable.insert(((HashEntry)curr.item()).key(), ((HashEntry)curr.item()).value());
 				curr = (DListNode) curr.next();
 				counter++;
 				System.out.println("adsf");
 			}
 		}
 		htable = newTable.htable;
 		size = newTable.size;
 	}
 }
