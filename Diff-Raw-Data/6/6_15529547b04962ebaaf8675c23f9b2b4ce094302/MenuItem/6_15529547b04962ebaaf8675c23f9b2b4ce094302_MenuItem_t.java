 package org.info.menu;
 
import java.text.DecimalFormat;

 import com.thoughtworks.xstream.annotations.XStreamAlias;
 import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class MenuItem.
  */
 @XStreamAlias("MenuItem")
 public class MenuItem {
 
 	/** The Item id. */
 	@XStreamAsAttribute
 	private Integer ItemID;
 	
 	/** The item name. */
 	private String itemName;
 	
 	/** The Price. */
 	private Double Price;
 	
 	/** The Main ingredient. */
 	private String MainIngredient;
 	
 	/** The is heart healthy. */
 	private boolean isHeartHealthy;
 	
 	/** This is how long it takes to cook*. */
 	private int timeSec;
 
 	
 
 	/**
 	 * Instantiates a new menu item.
 	 *
 	 * @param itemID the item id
 	 * @param itemName the item name
 	 * @param price the price
 	 * @param mainIngredient the main ingredient
 	 * @param isHeartHealthy the is heart healthy
 	 * @param timeSec the time sec
 	 */
 	public MenuItem(Integer itemID, String itemName, Double price,
 			String mainIngredient, boolean isHeartHealthy, int timeSec) {
 		super();
 		ItemID = itemID;
 		this.itemName = itemName;
 		Price = price;
 		MainIngredient = mainIngredient;
 		this.isHeartHealthy = isHeartHealthy;
 		this.timeSec = timeSec;
 	}
 
 
 	/**
 	 * Instantiates a new menu item.
 	 *
 	 * @param itemID the item id
 	 * @param itemName the item name
 	 * @param price the price
 	 * @param mainIngredient the main ingredient
 	 */
 	public MenuItem(Integer itemID,String itemName, Double price, String mainIngredient) {
 		super();
 		ItemID = itemID;
 		this.itemName = itemName;
 		Price = price;
 		MainIngredient = mainIngredient;
 		this.isHeartHealthy=false;
 	}
 
 	
 	/**
 	 * Instantiates a new menu item.
 	 *
 	 * @param itemID the item id
 	 * @param itemName the item name
 	 * @param price the price
 	 * @param mainIngredient the main ingredient
 	 * @param isHeartHealthy the is heart healthy
 	 */
 	public MenuItem(Integer itemID,String itemName, Double price, String mainIngredient,
 			boolean isHeartHealthy) {
 		super();
 		ItemID = itemID;
 		this.itemName = itemName;
 		Price = price;
 		MainIngredient = mainIngredient;
 		this.isHeartHealthy = isHeartHealthy;
 	}
 
 
 	
 	
 	/**
 	 * Gets the item id.
 	 *
 	 * @return the item id
 	 */
 	public Integer getItemID() {
 		return ItemID;
 	}
 
 
 	/**
 	 * Gets the time sec.
 	 *
 	 * @return the time sec
 	 */
 	public int getTimeSec() {
 		return timeSec;
 	}
 
 
 	/**
 	 * Sets the time sec.
 	 *
 	 * @param timeSec the new time sec
 	 */
 	public void setTimeSec(int timeSec) {
 		this.timeSec = timeSec;
 	}
 	
 	/**
 	 * Gets the item name.
 	 *
 	 * @return the item name
 	 */
 	public String getItemName() {
 		return itemName;
 	}
 	
 	/**
 	 * Sets the item name.
 	 *
 	 * @param itemName the new item name
 	 */
 	public void setItemName(String itemName) {
 		this.itemName = itemName;
 	}
 	
 	/**
 	 * Gets the price.
 	 *
 	 * @return the price
 	 */
 	public Double getPrice() {
 		return Price;
 	}
 	
 	/**
 	 * Sets the price.
 	 *
 	 * @param price the new price
 	 */
 	public void setPrice(Double price) {
 		Price = price;
 	}
 	
 	
 	/**
 	 * Checks if is heart healthy.
 	 *
 	 * @return true, if is heart healthy
 	 */
 	public boolean isHeartHealthy() {
 		return isHeartHealthy;
 	}
 
 
 	/**
 	 * Sets the heart healthy.
 	 *
 	 * @param isHeartHealthy the new heart healthy
 	 */
 	public void setHeartHealthy(boolean isHeartHealthy) {
 		this.isHeartHealthy = isHeartHealthy;
 	}
 
 
 	/**
 	 * Gets the main ingredient.
 	 *
 	 * @return the main ingredient
 	 */
 	public String getMainIngredient() {
 		return MainIngredient;
 	}
 	
 	/**
 	 * Sets the main ingredient.
 	 *
 	 * @param mainIngredient the new main ingredient
 	 */
 	public void setMainIngredient(String mainIngredient) {
 		MainIngredient = mainIngredient;
 	}
 	
 
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((ItemID == null) ? 0 : ItemID.hashCode());
 		result = prime * result
 				+ ((MainIngredient == null) ? 0 : MainIngredient.hashCode());
 		result = prime * result + ((Price == null) ? 0 : Price.hashCode());
 		result = prime * result + (isHeartHealthy ? 1231 : 1237);
 		result = prime * result
 				+ ((itemName == null) ? 0 : itemName.hashCode());
 		result = prime * result + timeSec;
 		return result;
 	}
 
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		MenuItem other = (MenuItem) obj;
 		if (ItemID == null) {
 			if (other.ItemID != null)
 				return false;
 		} else if (!ItemID.equals(other.ItemID))
 			return false;
 		if (MainIngredient == null) {
 			if (other.MainIngredient != null)
 				return false;
 		} else if (!MainIngredient.equals(other.MainIngredient))
 			return false;
 		if (Price == null) {
 			if (other.Price != null)
 				return false;
 		} else if (!Price.equals(other.Price))
 			return false;
 		if (isHeartHealthy != other.isHeartHealthy)
 			return false;
 		if (itemName == null) {
 			if (other.itemName != null)
 				return false;
 		} else if (!itemName.equals(other.itemName))
 			return false;
 		if (timeSec != other.timeSec)
 			return false;
 		return true;
 	}
 
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
		DecimalFormat df= new DecimalFormat("#.00");
		return "" + itemName + ", " + df.format(Price)+ ", " + MainIngredient + "";
 	}
 
 }
