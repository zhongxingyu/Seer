 package edu.gatech.statusquo.spacetrader.model;
 
 import edu.gatech.statusquo.spacetrader.driver.Driver;
import edu.gatech.statusquo.spacetrader.model.Ship.ShipType;
 
 public class Player {
 
 	private Ship ship;
 	private static String name;
 	private static int currency;
 	private static int trader;
 	private static int engineer;
 	private static int pilot;
 	private static int fighter;
 	private static int playerX;
 	private static int playerY; 
 
 	public Player() {
 		ship = new Ship(ShipType.FLEA);
 		name = "Player";
 		currency = 1000;
 		trader = 0;
 		engineer = 0;
 		pilot = 0;
 		fighter = 0;
 		playerX = 0;
 		playerY = 0;
 		
 	}
 
 	/**
 	 * @return playerX
 	 */
 	public int getPlayerX() {
         return playerX;
     }
 
 	/**
 	 * @param playerX
 	 */
     public void setPlayerX(int playerX) {
         Player.playerX = playerX;
     }
 
     /**
      * @return playerY
      */
     public int getPlayerY() {
         return playerY;
     }
 
     public void setPlayerY(int playerY) {
         Player.playerY = playerY;
     }
 
     /**
 	 * Sets the ship type.
 	 * 
 	 * @param sh
 	 *            the ship type.
 	 */
 	public void setShip(Ship sh) {
 		ship = sh;
 	}
 
 	/**
 	 * Gets the ship type.
 	 * 
 	 * @return the ship type.
 	 */
 	public Ship getShip() {
 		return ship;
 	}
 
 	/**
 	 * Gets the amount of money the player has currently.
 	 * 
 	 * @return an integer that is the amount of currency.
 	 */
 	public static int getCurrency() {
 		return currency;
 	}
 
 	/**
 	 * Set the currency to be a particular value.
 	 * 
 	 * @param curr an integer that is the amount of money.
 	 */
 	public void setCurrency(int curr) {
 		currency = curr;
 	}
 
 	/**
 	 * Get the player's name.
 	 * 
 	 * @return String that contains the player's name.
 	 */
 	public static String getName() {
 		return name;
 	}
 
 	/**
 	 * Set the player's name.
 	 * 
 	 * @param nm - String - set the player's name to this.
 	 */
 	public void setName(String nm) {
 		name = nm;
 	}
 	
 	/**
 	 * Set the trader skill level to be a particular value.
 	 * 
 	 * @param i represents the trader skills
 	 */
 	public void setTraderSkills(int i) {
 		trader = i;
 	}
 	
 	/**
 	 * Set the engineer skill level to be a particular value.
 	 * 
 	 * @param i represents the engineer skills
 	 */
 	public void setEngineerSkills(int i) {
 		engineer = i;
 	}
 	
 	/**
 	 * Set the pilot skill level to be a particular value.
 	 * 
 	 * @param i represents the pilot skills
 	 */
 	public void setPilotSkills(int i) {
 		pilot = i;
 	}
 	
 	/**
 	 * Set the fighter skill level to be a particular value.
 	 * 
 	 * @param i represents the fighter skills
 	 */
 	public void setFighterSkills(int i) {
 		fighter = i;
 	}
 	
 	/**
 	 * Get the players trader skills
 	 * 
 	 * @return trader trader skills
 	 */
 	public static int getTraderSkills() {
 		return trader;
 	}
 	
 	/**
 	 * Get the players engineer skills
 	 * 
 	 * @return engineer engineer skills
 	 */
 	public static int getEngineerSkills() {
 		return engineer;
 	}
 	
 	/**
 	 * Get the players pilot skills
 	 * 
 	 * @return pilot pilot skills
 	 */
 	public static int getPilotSkills() {
 		return pilot;
 	}
 	
 	/**
 	 * Get the players fighter skills
 	 * 
 	 * @return fighter fighter skills
 	 */
 	public static int getFighterSkills() {
 		return fighter;
 	}
 	
 	public SolarSystem findSystem(int currentX, int currentY){
 	      
         SolarSystem hold = new SolarSystem();
         
         
         for (int i = 0; i<Driver.listOfSystems.size(); i++){
             SolarSystem temp = Driver.listOfSystems.get(i);
             int systemX = temp.getxLocation();
             int systemY = temp.getyLocation();
             if(currentX == systemX && currentY == systemY){
                 hold = temp;
                 break;
             }
         }
         
         return hold;
     }
 	
 	public String toString() {
 		return name + "," + Integer.toString(currency) + "," + Integer.toString(trader)
 				+ "," + Integer.toString(engineer) + "," + Integer.toString(fighter)
 				+ "," + Integer.toString(pilot) + "," + Integer.toString(playerX)
 				+ "," + Integer.toString(playerY) + ",";
 	}
 }
