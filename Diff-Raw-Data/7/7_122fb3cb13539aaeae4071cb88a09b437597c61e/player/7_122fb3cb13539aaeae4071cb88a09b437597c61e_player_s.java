 package bteam.capstone.risk;
 
 import java.util.ArrayList;
 import java.util.Scanner;
 
 /**
  * 
  * @author Nick The player class will have a constructor that will create a
  *         player given a name and faction. The class will hold data that
  *         includes missles, redstars, coins, and resources.
  * 
  */
 public class player {
 
 	private int missles, redstar, coin, resource, troops;
 	private String name;
 	private Faction faction;
 	private ArrayList<Integer> countrys = new ArrayList<Integer>();
 	private int conquered;
 	private ArrayList<Integer> conqueredList = new ArrayList<Integer>();
 	/**
 	 * @author Ian Paterson
 	 * Will require more functonality to track missles
 	 * Will add this later
 	 */
	private int Wins;
 	// private Faction faction;
 
 	/**
 	 * @return the wins
 	 */
 	public int getWins() {
 		return Wins;
 	}

 	/**
 	 * @param wins the wins to set
 	 */
 	public void setWins(int wins) {
 		Wins = wins;
 	}
 
 	/**
 	 * Constructor for creating a new player given a string of data.
 	 * 
 	 * @Param Data is string formatted to hold all the necessary information
 	 */
 
 	public player(){
 		
 	}
 	
 	public player(String data) {
 		Scanner scan = new Scanner(data);
 		this.name = scan.next();
 		this.missles = scan.nextInt();
 		this.redstar = scan.nextInt();
 		this.coin = scan.nextInt();
 		this.resource = scan.nextInt();
 		this.Wins = scan.nextInt();
 		scan.close();
 	}
 
 	public ArrayList<Integer> getCountrys() {
 		return countrys;
 	}
 
 	/**
 	 * Returns the country from the array given a country
 	 * 
 	 * @Param country, the given country needed to be returned
 	 * 
 	 * @Return  returns the country given its int value.
 	 */
 	public int getCountryAt(int country) {
 		int index = getCountrys().indexOf(country);
 		return getCountrys().get(index);
 	}
 
 	/**
 	 * Removes the country at the index of the provided entry
 	 * 
 	 * @Param country, the given country needed to be removed from the array
 	 */
 	public void removeCountrys(int country) {
 		int index = getCountrys().indexOf(country);
 		countrys.remove(index);
 	}
 
 	/**
 	 * Removes all countrys from the array used only for testing methods
 	 * curently
 	 */
 	public void removeAllCountrys() {
 		countrys.clear();
 	}
 
 	/**
 	 * Overrided toString method used for saving all the player information in
 	 * the same format to which it is entered.
 	 * 
 	 * @Return out, returns the string representing a player.
 	 */
 	@Override
 	public String toString() {
 		String out = "";
 
 		//out += this.name + " ";
 		out += this.missles + " ";
 		out += this.redstar + " ";
 		out += this.coin + " ";
 		out += this.resource;
 		out += this.Wins+ " ";
 		return out;
 	}
 
 	/**
 	 * adds a country to the end of the list
 	 */
 	public void addCountry(Integer country) {
 		countrys.add(country);
 	}
 
 	/*
 	 * returns the player name
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/*
 	 * set the number of missles for the player
 	 */
 	public void setMissles(int missles) {
 		this.missles = missles;
 	}
 
 	/*
 	 * get the number of missles for a player
 	 */
 	public int getMissles() {
 		return missles;
 	}
 
 	/*
 	 * set the number of redstars a player has
 	 */
 	public void setRedstar(int redstar) {
 		this.redstar = redstar;
 	}
 
 	/*
 	 * returns the number of redstars can be used to determine if the player has
 	 * one the game (needs 2)
 	 */
 	public int getRedstar() {
 		return redstar;
 	}
 
 	/*
 	 * Set the number of coins the player currently owns
 	 */
 	public void setCoin(int coin) {
 		this.coin = coin;
 	}
 
 	/*
 	 * returns the number of coins the player owns/
 	 */
 	public int getCoin() {
 		return coin;
 	}
 
 	/*
 	 * sets the number of resources that the payer currently owns
 	 */
 	public void setResource(int resource) {
 		this.resource = resource;
 	}
 
 	/*
 	 * Gets the resources belonging to a player
 	 */
 	public int getResource() {
 		return resource;
 	}
 
 	/*
 	 * Sets the players faction not implemented.
 	 */
 	public void setFaction(StandardFaction faction) {
 		this.faction = faction;
 	}
 
 	/*
 	 * returns a faction
 	 */
 	public Faction getFaction() {
 		return faction;
 	}
 
 	/**
 	 * @param troops the troops to set
 	 */
 	public void setTroops(int troops) {
 		this.troops = troops;
 	}
 
 	/**
 	 * @return the troops
 	 */
 	public int getTroops() {
 		return troops;
 	}
 	public void addConquered(int country) {
 		
 		conqueredList.add(country);
 	}
 	public ArrayList<Integer> getConquered() {
 		// TODO Auto-generated method stub
 		return conqueredList;
 	}
 	
 	
 
 }
