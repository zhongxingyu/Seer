 /**
  * Humanoid Class.
  * This is a super class of the player and monster classes
  * This class has the health variable, and a MAX_HEALTH static variable.
  * The health represents the remaining health of the player or monster.
  * The MAX_HEALTH is a default health, if there is no other input.
  * The inventory is a list of the items that the player/monster have.
  * Items can be added or removed from the inventory.
  *
  */
 
 import java.util.ArrayList;
 
 public class Humanoid {
 
 	private int health;
 	private String name;
 	private static int MAX_HEALTH = 100;
 	private static String NO_NAME_GIVEN = "NO_NAME";
 	private ArrayList<Item> inventory;
 
 	public Humanoid(int maxHealth, String name){
 		health = maxHealth;
 		inventory = new ArrayList<Item>();
 		this.name = name;
 	}
 
 	public Humanoid(){
 		this(MAX_HEALTH, NO_NAME_GIVEN);
 	}
 
 	public int getHealth(){
 		return health;
 	}
 
 	public void addItem(Item i){
 		inventory.add(i);
 	}
 
 	public ArrayList<Item> getInventory(){
 		return inventory;
 	}
 
 	public boolean removeItem(Item i){
 		return inventory.remove(i);
 	}
 
 	public Item getBestItem(){
 		Item tempItem;
 		if(inventory.isEmpty()){
 			return new Item("NO_ITEM");
 		}
 		tempItem = inventory.get(0);
 
 		for(Item i: inventory){
 			if(i.getValue()>tempItem.getValue()){
 				tempItem = i;
 			}
 		}
 		return tempItem;
 	}
 
 	public void updateHealth(int h){
 		health = health - h;
 	}
 
 	public boolean equals(Object o){
 		if(o instanceof Humanoid){
 			return (this.health == ((Humanoid) o).health) && (this.inventory.equals(((Humanoid) o).inventory));
 		}
 		return false;
 	}
 
 	public String toString(){
 		return name;
 	}
 	
 	public String getInventoryString(){
 		String s;
 		if(inventory.isEmpty()){
			s = "The " + this.getClass().getName() + " has no items";
 		} else {
			s = "The " + this.getClass().getName() + " has the following items:\n";
 			for(Item i: inventory){
 				s+= i.getDescription();
 				s+= "\n";
 			}
 		}
 		return s;
 	}
 	
 }
