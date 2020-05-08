 import java.util.*;
 
 /**
  * Write a description of class Player here.
  * 
  * @author Tanzeel
  */
 public class Player{
 	/**
 	 * feilds :
 	 */
 	private String name;
 	private String description;
 	private Room currentRoom;
	@SuppressWarnings("unused")
	private int currweight;
 	private int weight;
 	private HashMap<String,Item> itemsInPossesion;
 	
 	/**
 	 * Constructor for objects of class Player
 	 */
 	public Player(String name, String description, int weight) {
 		this.name = name;
 		this.description = description;
 		this.weight = weight;
		currweight = weight;
 		itemsInPossesion = new HashMap<String,Item>();
 	}
 
 	/**
 	 * Get the room the player is currently in
 	 * @return current room
 	 */
 	public Room getCurrentPlayerRoom() {
 		return currentRoom;
 	}
 
 	/**
 	 * Get the player's description
 	 * @return description
 	 */
 	public String getPlayerDescription() {
 		return description;
 	}
 
 	/**
 	 * Get the player's name
 	 * @return name
 	 */
 	public String getPlayerName() {
 		return name;
 	}
 
 	/**
 	 * Returns a string with details of the players inventory
 	 * @return
 	 */
 	public String getFullPlayerDescription() {
 		return name + " \n Can pick upto : " + weight
 				+ " Kg. \n total wight carried : "+totalWeightCarried() 
 				+ " \n bag pack :\n"  + InventoryToString();
 	}
 
 	/**
 	 * Pick up an item
 	 * @param itemName
 	 * @param item
 	 * @return
 	 */
 	public boolean pick(String itemName,Item item) {
 		if (canPickItem(item)) {
 			itemsInPossesion.put(itemName,item);
 			currweight += item.getItemWeight();
 			return true;
 		} else {
 			System.out.println("Player can not pick " + item);
 			return false;
 		}
 	}
 
 	/** 
 	 * Drop the item called itemName
 	 * @param itemName
 	 * @return
 	 */
 	public Item drop(String itemName) {
 		if (!itemsInPossesion.containsKey(itemName)) {
 			return null;
 		} else {
 			Item itemDropped = itemsInPossesion.get(itemName);
             currweight -= itemDropped.getItemWeight();
             itemsInPossesion.remove(itemName);
 			return itemDropped;
 		}
 	}
 	
 	/**
 	 * Returns a reference to the item in the inventory with key key.
 	 * Does not remove the item from the inventory
 	 * @param key
 	 * @return
 	 */
 	public Item getItem(String key){
         return itemsInPossesion.get(key);
     }
 	
 	/**
 	 * @param item
 	 * @return true if user has room in the inventory
 	 */
 	private boolean canPickItem(Item item) {
 		if ((totalWeightCarried() + item.getItemWeight()) > weight) {
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * @return the total weight the player is carying
 	 */
 	private int totalWeightCarried() {
 		int weightCarried = 0;
 		Set<String> keys = itemsInPossesion.keySet();
 		for (String items : keys) {
 			weightCarried += itemsInPossesion.get(items).getItemWeight();
 		}
 		return weightCarried;
 	}
 
 	public void printItemsAndWeight() {
 	    Set<String> keys = itemsInPossesion.keySet();
 		for (String items : keys) {
 			System.out.println( itemsInPossesion.get(items).getItemDescription() + " "
 					+  itemsInPossesion.get(items).getItemWeight() + "\n");
 		}
 		System.out.println(totalWeightCarried());
 
 	}
 
 	/**
 	 * Set the players current room
 	 * @param currentRoom
 	 */
 	public void setCurrentRoom(Room currentRoom) {
 		this.currentRoom = currentRoom;
 	}
 	
 	/**
 	 * @return a String representation of the players inventory
 	 */
 	public String InventoryToString(){
         String str = "";
         Set<String> keys = itemsInPossesion.keySet();
         for (String items : keys){
             str += itemsInPossesion.get(items).getItemDescription() + " "
                     +  itemsInPossesion.get(items).getItemWeight() + "\n";
         }
         return str;
     }
 	  
 }
 
