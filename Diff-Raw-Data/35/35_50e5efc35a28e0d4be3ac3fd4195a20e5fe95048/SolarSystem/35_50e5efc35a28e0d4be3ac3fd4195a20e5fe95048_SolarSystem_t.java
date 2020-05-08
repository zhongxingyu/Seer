 package edu.gatech.statusquo.spacetrader.model;
 
 import java.util.HashMap;
 
import edu.gatech.statusquo.spacetrader.model.Good.*;
 
 public class SolarSystem {
 
 	private  String systemName;
 	private  int techLevel;
 	private  int resourceLevel;
 	private  int xLocation;
 	private  int yLocation;
 	private  Planet planet;
 	private  HashMap<GoodType, Integer> marketPrice;
 	private  HashMap<GoodType, Integer> marketQuantity;
 	
 	/**
 	 * Class constructor using listed parameters
 	 * @param systemName
 	 * @param planet
 	 * @param techLevel
 	 * @param resourceLevel
 	 * @param xLocation
 	 * @param yLocation
 	 */
 	public SolarSystem() {
 	        setPlanet(new Planet("N/A"));
 	    }
 	
 	public void setxLocation(int xLocation) {
 		this.xLocation = xLocation;
 	}
 
 	public void setyLocation(int yLocation) {
 		this.yLocation = yLocation;
 	}
 
 	public void setSystemName(String systemName) {
 		this.systemName = systemName;
 	}
 
 	public SolarSystem(String systemName, Planet planet, int techLevel,
 			int resourceLevel, int xLocation, int yLocation) {
 
 		this.systemName = systemName;
 		this.setPlanet(planet);
 		this.techLevel = techLevel;
 		this.resourceLevel = resourceLevel;
 		this.xLocation = xLocation;
 		this.yLocation = yLocation;
 		marketPrice = new HashMap<Good.GoodType, Integer>();
 		marketQuantity = new HashMap<Good.GoodType, Integer>();
 		GoodType[] keys = GoodType.values();
 		for (GoodType g: keys) {
 			marketPrice.put(g, 0);
 			marketQuantity.put(g, 0);
 		}
 	}
 	
 
 
     /**
 	 * 
 	 * @return marketPrice
 	 */
 	public HashMap<GoodType, Integer> getMarketPrice() {
 		return marketPrice;
 	}
 	
 	/**
 	 * 
 	 * @param hm
 	 */
 	public void setMarketPrice(HashMap<GoodType, Integer> hm) {
 		marketPrice = hm;
 	}	
 	
 	/**
 	 * 
 	 * @return market Quantity
 	 */
 	public HashMap<GoodType, Integer> getMarketQuantity() {
 		return marketQuantity;
 	}
 	
 	/**
 	 * 
 	 * @param hm
 	 */
 	public void setMarketQuantity(HashMap<GoodType, Integer> hm) {
 		marketQuantity = hm;
 	}
 	
 	/**
 	 * 
 	 * @return techLevel
 	 */
 	public int getTechLevel() {
 		return techLevel;
 	}
 	
 	/**
 	 * 
 	 * @param techLevel
 	 */
 	public void setTechLevel(int techLevel) {
 		this.techLevel = techLevel;
 	}
 	
 	/**
 	 * 
 	 * @return resourceLevel
 	 */
 	public int getResourceLevel() {
 		return resourceLevel;
 	}
 	
 	/**
 	 * 
 	 * @param resourceLevel
 	 */
 	public void setResourceLevel(int resourceLevel) {
 		this.resourceLevel = resourceLevel;
 	}
 	
 	/**
 	 * Returns string of the resource
 	 * @return resourceName
 	 */
 	
 	public String getResourceLevelName(){
 		
 		if(resourceLevel == 0){
 			return "Lots of Water";
 		}
 		if(resourceLevel == 1){
 			return "Drought";
 		}
 		if(resourceLevel == 2){
 			return "Rich Fauna";
 		}
 		if(resourceLevel == 3){
 			return "Lifeless";
 		}
 		if(resourceLevel == 4){
 			return "Rich Soil";
 		}
 		if(resourceLevel == 5){
 			return "Poor Soil";
 		}
 		if(resourceLevel == 6){
 			return "Mineral Rich";
 		}
 		if(resourceLevel == 7){
 			return "Mineral Poor";
 		}
 		if(resourceLevel == 8){
 			return "Warlike";
 		}
 		if(resourceLevel == 9){
 			return "Lots of Herbs";
 		}
 		if(resourceLevel == 10){
 			return "Weird Mushrooms";
 		}
 		else return null;
 	}
 
 	/**
 	 * 
 	 * @return xLocation
 	 */
 	public int getxLocation() {
 		return xLocation;
 	}
 	
 	/**
 	 * 
 	 * @return yLocation
 	 */
 
 	public int getyLocation() {
 		return yLocation;
 	}
 
 	/**
 	 * 
 	 * @return systemName
 	 */
 	public String getSystemName() {
 		return systemName;
 	}
 
 	/**
 	 * @return Information of the SolarSystem in a String.
 	 */
 	public String toString() {
 		return systemName + "," + Integer.toString(techLevel) + "," + Integer.toString(resourceLevel)
 				+ "," + Integer.toString(xLocation) + "," + Integer.toString(yLocation) + ",";
 	}
 	
 	public String marketPriceToString() {
 		String ret = "";
 		GoodType[] goodTypes = Good.GoodType.values();
 		for (GoodType g: goodTypes) {
 			ret = ret + Integer.toString(marketPrice.get(g)) + ",";
 		}
 		return ret;
 	}
 	
 	public String marketQuantityToString() {
 		String ret = "";
 		GoodType[] goodTypes = Good.GoodType.values();
 		for (GoodType g: goodTypes) {
 			ret = ret + Integer.toString(marketQuantity.get(g)) + ",";
 		}
 		return ret;
 	}
 
 	public Planet getPlanet() {
 		return planet;
 	}
 
 	public void setPlanet(Planet planet) {
 		this.planet = planet;
 	}
 }
