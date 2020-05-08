 import java.io.Serializable;
 import java.util.*;
 
 /**
  * Class representing the Player of the game.
  * 
  * @author TeamTroll
  * @version 1.0
  */
 public class Player implements Serializable {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private String name, inventoryString;
	final private int fight, trade, engin, pilot, money, cargoBay;
 	private Hashtable<String, Integer> inventory;
 	private Ship ship;
 
 	/**
 	 * Creates the create player scene.
 	 * 
 	 * @param String
 	 *            name The player's name.
 	 * @param int fight The player's fight level.
 	 * @param int trade The player's trade level.
 	 * @param int engin The player's engin level.
 	 * @param int pilot The player's pilot level.
 	 */
 	public Player(String name, int fight, int trade, int engin, int pilot) {
 		this.name = name;
 		this.setFight(fight);
 		this.setTrade(trade);
 		this.setEngin(engin);
 		this.setPilot(pilot);
 
 		ship = new Ship("Gnat");
 
 		inventory = new Hashtable<String, Integer>();
 		inventory.put("Water", 0);
 		inventory.put("Furs", 0);
 		inventory.put("Food", 0);
 		inventory.put("Ore", 0);
 		inventory.put("Games", 0);
 		inventory.put("Firearms", 0);
 		inventory.put("Medicine", 0);
 		inventory.put("Machines", 0);
 		inventory.put("Narcotics", 0);
 		inventory.put("Robots", 0);
 
 		cargoBay = 100;
 		money = 10000;
 
 		// System.out.println("player created!");
 	}
 
 	/**
 	 * Getter for the player's name.
 	 * 
 	 * @return String The player's name.
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * Getter for the player's fight level.
 	 * 
 	 * @return int The player's fight level.
 	 */
 	public int getFight() {
 		return fight;
 	}
 
 	/**
 	 * Setter for the player's fight level.
 	 * 
 	 * @param int fight The fight level of the player.
 	 */
 	public void setFight(int fight) {
 		this.fight = fight;
 	}
 
 	/**
 	 * Getter for the player's trade level.
 	 * 
 	 * @return int The player's trade level.
 	 */
 	public int getTrade() {
 		return trade;
 	}
 
 	/**
 	 * Setter for the player's trade level.
 	 * 
 	 * @param int trade The trade level of the player.
 	 */
 	public void setTrade(int trade) {
 		this.trade = trade;
 	}
 
 	/**
 	 * Getter for the player's engineering level.
 	 * 
 	 * @return int The player's engineering level
 	 */
 	public int getEngin() {
 		return engin;
 	}
 
 	/**
 	 * Setter for the player's engineering level.
 	 * 
 	 * @param int engin The engin level of the player.
 	 */
 	public void setEngin(int engin) {
 		this.engin = engin;
 	}
 
 	/**
 	 * Getter for the player's pilot level.
 	 * 
 	 * @return int The player's pilot level.
 	 */
 	public int getPilot() {
 		return pilot;
 	}
 
 	/**
 	 * Setter for the player's pilot level.
 	 * 
 	 * @param int pilot The pilot level of the player.
 	 */
 	public void setPilot(int pilot) {
 		this.pilot = pilot;
 	}
 
 	/**
 	 * Setter for the current inventory.
 	 * 
 	 * @param Hashtable
 	 *            <String, Integer> inventory The inventory to be set.
 	 */
 	public void setInventory(Hashtable<String, Integer> inventory) {
 		this.inventory = inventory;
 	}
 
 	/**
 	 * Getter for the current inventory.
 	 * 
 	 * @return Hashtable<String, Integer> The current inventory.
 	 */
 	public Hashtable<String, Integer> getInventory() {
 		return inventory;
 	}
 
 	/**
 	 * Helper method to generate a string representing the contents of the
 	 * inventory.
 	 */
 	private void generateInventoryString() {
 		String inventoryString = "| ";
 
 		for (String itemName : inventory.keySet())
 			inventoryString += itemName + ": " + inventory.get(itemName)
 					+ " | ";
 
 		this.inventoryString = inventoryString;
 	}
 
 	/**
 	 * Helper method to generate a string that displays the first half of the
 	 * items in the inventory.
 	 * 
 	 * @return String A string representing the first half of the contents of
 	 *         the inventory.
 	 */
 	public String getInventory1() {
 		generateInventoryString();
 		final int split = inventoryString.indexOf("Furs");
 		return inventoryString.substring(0, split);
 	}
 
 	/**
 	 * Helper method to generate a string that displays the second half of the
 	 * items in the inventory.
 	 * 
 	 * @return String A string representing the second half of the contents of
 	 *         the inventory.
 	 */
 	public String getInventory2() {
 		generateInventoryString();
 		final int split = inventoryString.indexOf("Furs");
 		return "| "
 				+ inventoryString.substring(split, inventoryString.length());
 	}
 
 	/**
 	 * Setter for the player's amount of money.
 	 * 
 	 * @param int money The amount of money to be set in the player's account.
 	 */
 	public void setMoney(int money) {
 		this.money = money;
 	}
 
 	/**
 	 * Getter for the player's amount of money.
 	 * 
 	 * @return int The amount of money in the player's account.
 	 */
 	public int getMoney() {
 		return money;
 	}
 
 	/**
 	 * Setter for the player's ship.
 	 * 
 	 * @param Ship
 	 *            ship The ship to be set.
 	 */
 	public void setShip(Ship ship) {
 		this.ship = ship;
 	}
 
 	/**
 	 * Getter for the player's ship.
 	 * 
 	 * @return Ship The player's ship.
 	 */
 	public Ship getShip() {
 		return ship;
 	}
 
 	/**
 	 * Helper method for getting the number of items in the player's inventory.
 	 * 
 	 * @return Integer The number of items in the player's inventory.
 	 */
 	public Integer numItems() {
 		Integer total = 0;
 
 		for (Integer i : inventory.values()) {
 			total += i;
 		}
 
 		return total;
 	}
 
 	public void printInventory() {
 		for (String good : inventory.keySet()) {
 			System.out.println(good + ": " + inventory.get(good));
 		}
 	}
 
 }
