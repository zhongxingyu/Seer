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
 	
	/** Constructs a new Item with the specified barcode, product, and container
	 * @param barcode the Item's barcode
	 * @param product this Item's corresponding Product
	 * @param container the ProductContainer this Item is to be stored in
 	 */
 	public void manage(Item item) {
 		items.add(item);
 	}
 }
