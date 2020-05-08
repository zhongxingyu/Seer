 package team.ApiPlus.Manager;
 
 import java.util.ArrayList;
 import java.util.List;
 
import team.ApiPlus.ApiPlus;
 import team.ApiPlus.Item;
 
 /**
  * API+ Custom Item Manager.
  * @author SirTyler (Tyler Martin)
  * @version 1.0
  */
 public class ItemManager {
 	
 	private static ItemManager instance;
 	private List<Item> list = new ArrayList<Item>();
 	
 	protected ItemManager() {
		if(instance != null) ApiPlus.info("Cannot have multiple Instances of the Item Manager.");
 	}
 	
 	/**
 	 * Method used for Adding Items to Manager.
 	 * @param i Item to be added.
 	 * @return boolean True if action was completed, False if not.
 	 */
 	public boolean addItem(Item i) {
 		if(list.contains(i)) return false;
 		else {
 			list.add(i);
 			return true;
 		}
 	}
 	
 	/**
 	 * Method used for Removing Item from Manager.
 	 * @param i Item to be removed.
 	 * @return boolean True if action was completed, False if not.
 	 */
 	public boolean removeItem(Item i) {
 		if(list.contains(i)) {
 			list.remove(i);
 			return true;
 		} else return false;
 	}
 	
 	/**
 	 * Method used for Removing Item from Manager based on Name.
 	 * @param name Name of Item to be removed.
 	 * @return boolean True if action was completed, False if not.
 	 */
 	public boolean removeItem(String name) {
 		Item i = getItem(name);
 		if(i == null) return false;
 		return removeItem(i);
 	}
 	
 	/**
 	 * Method used for getting Item from Manager based on Name.
 	 * @param name Name of Item to look for.
 	 * @return Item Item found, returns null if nothing.
 	 */
 	public Item getItem(String name) {
 		for(Item i : list) {
 			if(i.getName().equalsIgnoreCase(name)) return i;
 		}
 		return null;
 	}
 	
 	/**
 	 * Method used for getting an Instance of the API's ItemManager, Only one instance is allowed.
 	 * @return ItemManager Instance of the API's ItemManager.
 	 */
 	public static ItemManager getInstance() {
 		if(instance == null) instance = new ItemManager();
 		return instance;
 	}
 }
