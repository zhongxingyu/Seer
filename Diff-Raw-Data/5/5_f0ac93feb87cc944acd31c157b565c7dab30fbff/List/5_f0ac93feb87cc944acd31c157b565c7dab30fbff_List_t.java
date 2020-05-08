 package fr.frozentux.craftguard2.list;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 /**
  * Data structure reprensenting a craftGuard List, with it's attributes, it's type lists and other stuff 
  * @author FrozenTux
  */
 public class List {
 	
 	private String name, permission, parentName;
 	private List parent;
 	private HashMap<Integer, Id> ids;
 	private HashMap<String, HashMap<Integer, Id>> typesLists;
 	private ListManager manager;
 	
 	
 	public List(String name, String permission, String parent, ListManager manager){
 		this.name = name;
		this.permission = (permission == null) ? name : permission;
 		this.parentName = parent;
 		this.manager = manager;
 		this.ids = new HashMap<Integer, Id>();
 		this.typesLists = new HashMap<String, HashMap<Integer, Id>>();
 	}
 	
 	public List(String name, String permission, String parent, java.util.List<String> list, ListManager manager){
 		this.name = name;
		this.permission = (permission == null) ? name : permission;
 		this.parentName = parent;
 		this.manager = manager;
 		this.ids = new HashMap<Integer, Id>();
 		this.typesLists = new HashMap<String, HashMap<Integer, Id>>();
 		
 		Iterator<String> it = list.iterator();
 		while(it.hasNext()){
 			Id id = new Id(it.next());
 			ids.put(id.getId(), id);
 		}
 	}
 	
 	public void addId(Id id){
 		if(ids.containsKey(id.getId()))id = id.merge(ids.get(id.getId()));
 		ids.put(id.getId(), id);
 	}
 	
 	/**
 	 * Checks if the list has a {@link TypeList} for the given type
 	 * @param type	The type
 	 * @return	true if the {@link TypeList} exists
 	 */
 	public boolean typeListAvailable(String type){
 		return ((parent != null && parent.typeListAvailable(type))|| typesLists.containsKey(type));
 	}
 	
 	/**
 	 * Checks if the commond ids list for this list contains a given {@link Id}
 	 * @param id	The id to check
 	 * @return		<code>true</code> if the id is contained
 	 */
 	public boolean containsId(int id){
 		return ((parent != null && parent.containsId(id))|| ids.containsKey(id));
 	}
 	
 	/**
 	 * Returns the {@link TypeList} for a give type
 	 * @param type	The type
 	 * @return		The {@link TypeList} if it exists; otherwise <code>null</code>
 	 */
 	public HashMap<Integer, Id> getTypeList(String type, boolean containsParent){
 		if(parent == null || !parent.typeListAvailable(type) || !containsParent)return typesLists.get(type);
 		else if(!typesLists.containsKey(type))return parent.getTypeList(type, true);
 		HashMap<Integer, Id> merged = typesLists.get(type);
 		HashMap<Integer, Id> parentList = parent.getTypeList(type, true);
 		Iterator<Integer> it = parentList.keySet().iterator();
 		while(it.hasNext()){
 			Id id = parentList.get(it.next());
 			if(merged.containsKey(id.getId()))merged.put(id.getId(), merged.get(id.getId()).merge(id));
 			else merged.put(id.getId(), id);
 		}
 		return merged;
 	}
 	
 	/**
 	 * Returns an {@link Id} from the common ids list
 	 * @param id	The id of the {@link Id}
 	 * @return		The {@link Id} if exists; otherwise null
 	 */
 	public Id getId(int id, boolean containsParent){
 		if(parent == null || !parent.containsId(id) || !containsParent)return ids.get(id);
 		else if(!ids.containsKey(id))return parent.getId(id, true);
 		return ids.get(id).merge(parent.getId(id, true));
 	}
 	
 	/**
 	 * @return A {@link HashMap} containing all the common ids with the id as int as key and the {@link Id} object as value
 	 */
 	public HashMap<Integer, Id> getIds(boolean containParent){
 		if(!containParent || parent == null)return ids;
 		HashMap<Integer, Id> mergedMap = this.ids;
 		Iterator<Integer> parentIt = parent.getIds(true).keySet().iterator();
 		while(parentIt.hasNext()){
 			Id id = parent.getId(parentIt.next(), true);
 			if(!mergedMap.containsKey(id.getId()))mergedMap.put(id.getId(), id);
 			else mergedMap.put(id.getId(), mergedMap.get(id).merge(id));
 		}
 		
 		return mergedMap;
 	}
 	
 	/**
 	 * @return The name of this list
 	 */
 	public String getName(){
 		return name;
 	}
 	
 	/**
 	 * @return	The permission for this list
 	 */
 	public String getPermission(){
 		return permission;
 	}
 	
 	/**
 	 * @return The parent list of this list if exists; otherwise null;
 	 */
 	public List getParent(){
 		return parent;
 	}
 	
 	/**
 	 * Called to retrieve the parent list from the listManager with the given parentName in constructor
 	 * Must be called after all lists are loaded
 	 */
 	public void registerParent(){
 		parent = manager.getList(parentName);
 	}
 	
 	/**
 	 * @return The listManager storing this list
 	 */
 	public ListManager getListManager(){
 		return manager;
 	}
 	
 	public void addTypeList(String type, HashMap<Integer, Id> list){
 		typesLists.put(type, list);
 	}
 	
 	public void addTypeList(String type, java.util.List<String> list){
 		HashMap<Integer, Id> map = new HashMap<Integer, Id>();
 		Iterator<String> it = list.iterator();
 		while(it.hasNext()){
 			Id id = new Id(it.next());
 			map.put(id.getId(), id);
 		}
 		typesLists.put(type, map);
 	}
 	
 	public Set<String> idsToStringSet(){
 		HashSet<String> result = new HashSet<String>();
 		Iterator<Integer> it = this.getIds(false).keySet().iterator();
 		while(it.hasNext()){
 			result.add(this.getId(it.next(), false).toString());
 		}
 		return result;
 	}
 	
 	public String toString(){
 		String result = "";
 		Iterator<Integer> it = this.getIds(true).keySet().iterator();
 		while(it.hasNext()){
 			result = result + "\n* " + ids.get(it.next()).toString();
 		}
 		return result;
 	}
 	
 	public boolean hasTypeLists(){
 		return !typesLists.isEmpty();
 	}
 	
 	public Set<String> typeListsNames(){
 		return typesLists.keySet();
 	}
 
 	public void removeId(int id) {
 		ids.remove(id);
 	}
 }
