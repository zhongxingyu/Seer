 package com.sis.dao;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.NoSuchElementException;
 
import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;

 import com.sis.util.Pair;
 
public class Collection implements Iterable<DataObject>, Cloneable {
	protected Resource resource 						= null;
	// CR: HashMap doesn't make sense, because hash is afaik 32 bit and we are using long in some places.
 	protected LinkedHashMap<String, DataObject> items 	= new LinkedHashMap<String, DataObject>();
    protected String tableName							= "";
    protected String idFieldName 						= "id";
    
    private final static Logger logger = Logger.getLogger(Collection.class);
 
     protected boolean loaded	= false;
 
 	@Override
 	public Iterator<DataObject> iterator() {
 		return items.values().iterator();
 	}
 
 	public void load() throws DaoException {
 		if (!loaded) {
 			loadCollection();
 			loaded = true;
 		}
 	}
 
 	protected void loadCollection() throws DaoException {
 	}
 
 	public synchronized void delete() throws DaoException {
 		// CR: I'm totally uncomfortable with this solution
 		// FIXME: we should give the resource the ability to remove a collection by its own implementation
 		// CR: Collections can be used as containment for DataObjects so it also doesn't makes a lot of 
		//     sense to let the resource decide how to delete the objects. Best practice would be probably to
 		//     set a flag whether the collection is load by a resource routine or not. So we can switch here
 		//     from object by object delete and resource backed delete implementation.
 		// TODO: better distinguish if it's load by resource or manually
 		load();
 
 		resource.beginTransaction();
 
 		try {
 			for (DataObject object : items.values()) {
 				object.delete();
 			}
 
 			clear();
 		} catch(DaoException e) {
 			resource.rollback();
 			throw e;
 		}
 
 		resource.commit();
 	}
 	
 	public synchronized void save() throws DaoException {
 		resource.beginTransaction();
 		
 		try {
 			for (DataObject object : items.values()) {
 				object.save();
 			}
 			
 			clear();
 		} catch(DaoException e) {
 			resource.rollback();
 			throw e;
 		}
 		
 		resource.commit();
 	}
 
 	public void addItem(DataObject item) {
 		items.put(item.getId(), item);
 	}
 
 	public DataObject getFirstElement() throws DaoException {
 		load();
 		try {
 			return iterator().next();
 		} catch(NoSuchElementException e) {
 			return null;
 		}
 	}
 
 	public String getTableName() {
 		return tableName;
 	}
 
 	public String getIdFieldName() {
 		return idFieldName;
 	}
 
 	protected DataObject _newItem() {
 		return null;
 	}
 
 	final public DataObject newItem() {
 		DataObject dobj = _newItem();
 		
 		if (dobj == null) {
 			throw new RuntimeException("_newItem() returns null! Please inherit _newItem() correctly!");
 		}
 		
 		return dobj;
 	}
 
 	public void removeItemById(String id) {
 		if (items.containsKey(id)) {
 			items.remove(id);
 		}
 	}
 
 	public long size() {
 		return items.size();
 	}
 
 	/**
 	 * resets filters
 	 */
 	public void reset() {
 		fieldFilters.clear();
 	}
 
 	/**
 	 * clears out collection (implies also reset())
 	 */
 	public void clear() {
 		reset();
 		items.clear();
 	}
 
     protected boolean hasResource() {
     	return resource != null;
     }
 
     protected boolean checkResourceForCapability(int cap) {
     	return hasResource() && (resource.getCapabilities() & cap) != 0;
     }
 
     /// SORT
     
     final public static int SORT_UNDEFINED				= 0;
     final public static int SORT_ASCENDING				= 1;
     final public static int SORT_DESCENDING				= 2;
 
     protected HashMap<String, Integer> fieldSortings = new HashMap<String, Integer>();
     
 	public void addFieldToSort(String string, int direction) {
 		fieldSortings.put(string, direction);
 	}
     
     /// FILTER METHODS (backed by resource then)
 
     final public static int FILTER_CUSTOM 				= 0;
     final public static int FILTER_EQUALS 				= 1;
     final public static int FILTER_LIKE 				= 2;
     final public static int FILTER_LESS					= 3;
     final public static int FILTER_LESS_OR_EQUALS		= 4;
     final public static int FILTER_GREATER				= 5;
     final public static int FILTER_GREATER_OR_EQUALS	= 6;
     final public static int FILTER_NOT_EQUALS 			= 7;
     final public static int FILTER_FIND_IN_SET			= 8;
     final public static int FILTER_NOT_IN_SET			= 9;
 
    // TODO: CR: hashmap is dump. list instead.
 	protected final HashMap<String, Pair<java.lang.Object[], Integer>> fieldFilters = new HashMap<String, Pair<java.lang.Object[], Integer>>();
 
 	public void addFieldToFilter(String field, java.lang.Object[] value, int mode) {
//		if (mode == FILTER_CUSTOM) {
//			String hash = "_cust" + Integer.toString(value.hashCode() ^ field.hashCode());
//			
//			fieldFilters.put(hash, new Pair<java.lang.Object[], Integer>(value, mode));
//		} else {
			fieldFilters.put(field, new Pair<java.lang.Object[], Integer>(value, mode));
//		}
 	}
 
 	private static final Object[] nullObjectArray = new java.lang.Object[] {null};
 	
 	public void addFieldToFilter(String field, java.lang.Object[] value) {
 		addFieldToFilter(field, value != null ? value : nullObjectArray, Collection.FILTER_EQUALS);
 	}
 
 	public void addFieldToFilter(String field, java.lang.Object value, int mode) {
 		addFieldToFilter(field, new java.lang.Object[] {value}, mode);
 	}
 
 	public void addFieldToFilter(String field, java.lang.Object value) {
 		addFieldToFilter(field, new java.lang.Object[] {value});
 	}
 
 	/// -----------------------------------------------------------------------
 	
 	protected long limitCount = 0, limitOffset = 0;
 
 	public void limit(long count) {
 		limit(count, 0);
 	}
 
 	public void limit(long count, long offset) {
 		limitCount = count;
 		limitOffset = offset;
 	}
 
 	public long getCount() {
		throw new NotImplementedException();
 	}
 }
