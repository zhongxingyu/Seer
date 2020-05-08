 package model;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 /**
  * @author Clayton Watts
  *
  */
 public class ItemManager {
 	private Collection<Item> items;
 	
 	public ItemManager() {
 		items = new ArrayList<Item>();
 	}
 	
	/** Adds the given item to this Manager's indexes
	 * @param item Item to manage
 	 */
 	public void manage(Item item) {
 		items.add(item);
 	}
 }
