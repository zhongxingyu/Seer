 package edu.gatech.cs2340.data;
 
 import java.io.Serializable;
 import java.util.Random;
 import java.util.logging.Logger;
 
 /**
  * 
  * @author Stephen Conway Function group: Model: Data holder Created for: M6
  *         10/7/13 Assigned to: Maddy Modifications:
  * 
  * 
  * 
  *         Purpose: Parent class for all Tile objects that make up a grid on the
  *         game's map.
  */
 public abstract class Tile implements Serializable {
 	//Static
 	public static Logger logger;
 	
 	//Instance
 	private Player owner;
 	private String id;
 	private boolean isActive;
 	private String name;
 	private int price;
 	private Mule mule;
 	
 	protected boolean dirty;
 
 	/**
 	 * Create a tile of type name
 	 * @param name
 	 */
 	public Tile(String name) {
 		this.name = name;
 		dirty = true;
 		// TODO throw error if not of correct type?
 	}
 
 	/**
	 * #FUTURE Method to get the production of a Tile. Based upon Tile type,
 	 * MULE present, and random events.
 	 * 
 	 * @return
 	 */
 	public ResourceAmount calculateProduction() {
		Random random = new Random();
		int crystiteIncrease = getCrystiteIncrease();
		return new ResourceAmount(getOreIncrease(), getFoodIncrease(), getEnergyIncrease(), 
				crystiteIncrease*random.nextInt(crystiteIncrease+1));
 	};
 
 	/**
 	 * Get increase in ore for a particular kind of tile's production phase.
 	 * @return Increase in resource
 	 */
 	protected abstract int getOreIncrease();
 	
 	/**
 	 * Get increase in food for a particular kind of tile's production phase.
 	 * @return Increase in resource
 	 */
 	protected abstract int getFoodIncrease();
 	
 	/**
 	 * Get increase in energy for a particular kind of tile's production phase.
 	 * @return Increase in resource
 	 */
 	protected abstract int getEnergyIncrease();
 	
 	/**
 	 * Get increase in crystite for a particular kind of tile's production phase.
 	 * @return Increase in resource
 	 */
 	protected abstract int getCrystiteIncrease();
 
 	/**
 	 * #M6 Method to set owner of tile. Should handle "null" as returning Tile
 	 * to neutral (through sale) Should call something to update its
 	 * TileRenderer to reflect the change
 	 * 
 	 * @param newOwner
 	 */
 	public void setOwner(Player newOwner) {
 		this.owner = newOwner;
 		dirty = true;
 	}
 
 	/**
 	 * #M6 Method to get owner of tile. Should return null if tile is unowned
 	 * 
 	 * @return
 	 */
 	public Player getOwner() {
 		return this.owner;
 	}
 
 	public String getId() {
 		return id;
 	}
 
 	public void setId(String id) {
 		this.id = id;
 	}
 	
 	public boolean isDirty() {
 		return dirty;
 	}
 	
 	public boolean isOwned() {
 		return owner != null;
 	}
 	
 	public void cleaned() {
 		dirty = false;
 	}
 
 	// returns true if equal, false otherwise
 	public boolean equals(Object t) {
 		boolean result = false;
 		if (t instanceof Tile) {
 			result = (id == ((Tile)t).getId());
 		}
 		return result;
 	}
 
 	//get whether or not is active
 	public boolean isActive() {
 		return isActive;
 	}
 
 	public void setActive(boolean isActive) {
 		this.isActive = isActive;
 		dirty = true;
 	}
 
 	public String getName() {
 		return name;
 	}
 	
 	public Mule getMule() {
 		return mule;
 	}
 	
 	public void setMule(Mule mule) {
 		if (this.mule != null) {
 			throw new RuntimeException("Cannot place mule where there already is one.");
 		}
 		this.mule = mule;
 		dirty = true;
 	}
 	
 	public boolean hasMule() {
 		return (mule != null);
 	}
 
 
 	/**
 	 * Set the price of the tile and draw a string with that price
 	 * @param price Price of the tile
 	 * @return Whether the setting was successful (because the tile is unowned)
 	 */
 	public void setPrice(int price) {
 		this.price = price; 
 		dirty = true;
 	}
 	
 	/**
 	 * Get the price of the tile
 	 * @return Price of the tile
 	 */
 	public int getPrice(){
 		return price;
 	}
 
 }
