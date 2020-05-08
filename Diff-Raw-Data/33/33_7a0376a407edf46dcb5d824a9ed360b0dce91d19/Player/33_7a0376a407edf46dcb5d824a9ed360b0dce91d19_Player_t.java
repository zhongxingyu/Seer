 package model.object;
 import java.util.*;
 
 import model.Room;
 
 /**
  * Write a description of class Player here.
  * 
  * @author Tanzeel
  */
 public class Player{
 
 	private String name;
 	private String description;
 	private Room currentRoom;
 	private int currentWeight;
 	private int maxWeight;
 	private HashMap<String,Item> itemsInPossesion;
 	private int health;
 	private Stack<String> lastMonsterAttacked;
 	private String lookingDirection = "north";
 	private Stack<Integer> healthLoss;
 
 	/**
 	 * Constructor for objects of class Player
 	 */
 	public Player(String name, String description, int maxWeight, int health) {
 		this.name = name;
 		this.description = description;
 		this.maxWeight = maxWeight;
 		this.health = health;
 		currentWeight = 0;
 		itemsInPossesion = new HashMap<String,Item>();
 		healthLoss = new Stack<Integer>();
 		lastMonsterAttacked = new Stack<String>();
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
 	 * Sets the players name
 	 * @param name
 	 */
 	public void setPlayerName(String name) {
 		this.name = name;
 	}
 
 	/**
 	 * Returns a string with details of the players inventory
 	 * @return
 	 */
 	public String getFullPlayerDescription() {
 		return name + " \n Can pick upto : " + maxWeight
 				+ " Kg. \n total wight carried : " + currentWeight 
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
 			currentWeight += item.getItemWeight();
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
             currentWeight -= itemDropped.getItemWeight();
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
 		if ((currentWeight + item.getItemWeight()) > maxWeight) {
 			return false;
 		}
 		return true;
 	}
 
 	public void printItemsAndWeight() {
 	    Set<String> keys = itemsInPossesion.keySet();
 	    System.out.println("Items being carried: ");
 		for (String items : keys) {
 			System.out.println("- " + itemsInPossesion.get(items).getItemName() + " "
 					+  itemsInPossesion.get(items).getItemWeight());
 		}
 		System.out.println("Weight being carried: " + currentWeight + "/" + maxWeight);
 
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
             str += itemsInPossesion.get(items).getItemName() + " "
                     +  itemsInPossesion.get(items).getItemWeight() + "\n";
         }
         return str;
     }
 	
 	public int getCurrentWeight() {
 		return currentWeight;
 	}
 	
 	public int getMaxWeight() {
 		return maxWeight;
 	}
 	
 	public void attacked(String monsterName){
 		health--;
 		System.out.println(name + " attacked by " + monsterName + ". Stamina reduced to " + health);
 	}
 	public void unAttacked(){
 		health += healthLoss.pop();
 	}
 	public void heal(){
 		health += 5;
 	}
 	public int getHealth(){
 		return health;
 	}
 	public void addHealthLoss(int n){
 		healthLoss.push(n);
 	}
 	public void pushLastMonsterAttacked(String monsterName){
 		lastMonsterAttacked.push(monsterName);
 	}
 	public String getLastMonsterAttacked(){
 		return lastMonsterAttacked.pop();
 	}
 
 	public String getLookingDirection() {
 		return lookingDirection;
 	}
 
 	public void setLookingDirection(String lookingDirection) {
 		this.lookingDirection = lookingDirection;
 	}
	public ArrayList<String> getItemsInPosession() {
		return new ArrayList<String>(itemsInPossesion.keySet());
	}
 
 }
