 package com.gsi.telecom.data;
 
 /**
  * This class implements the BasicData interface
  * 
  * @author willyaranda
  * 
  */
 public class Product implements BasicData {
 	/**
 	 * The value to search in the DB
 	 */
 	private Integer id;
 	private String name;
 	private Float price;
 	private Boolean valid;
 	private String description;
 
 	/**
 	 * Empty constructor
 	 */
 	public Product() {
 		// Dummy
 	}
 
 	/**
 	 * Constructor with parameters
 	 * 
 	 * @param id
 	 * @param name
 	 * @param price
 	 * @param valid
 	 * @param description
 	 */
 	public Product(Integer id, String name, Float price, Boolean valid,
 			String description) {
 		this.id = id;
 		this.name = name;
 		this.price = price;
 		this.valid = valid;
 		this.description = description;
 	}
 
 	/**
 	 * Copy constructor
 	 * 
	 * @param cust1
 	 */
 	public Product(Product prod) {
 		this.id = prod.getId();
 		this.name = prod.getName();
 		this.price = prod.getPrice();
 		this.valid = prod.getValid();
 		this.description = prod.getDescription();
 	}
 
 	/**
 	 * Returns the description
 	 * 
 	 * @return the description
 	 */
 	public String getDescription() {
 		return description;
 	}
 
 	@Override
 	public Integer getId() {
 		return id;
 	}
 
 	/**
 	 * Returns the name
 	 * 
 	 * @return the name
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * Returns the price
 	 * 
 	 * @return the price
 	 */
 	public Float getPrice() {
 		return price;
 	}
 
 	/**
 	 * Returns if it's valid
 	 * 
 	 * @return the validity
 	 */
 	public Boolean getValid() {
 		return valid;
 	}
 
 	/**
 	 * Sets the description
 	 * 
 	 * @param description
 	 */
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	@Override
 	public void setId(Integer id) {
 		this.id = id;
 	}
 
 	/**
 	 * Sets the name
 	 * 
 	 * @param name
 	 */
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	/**
 	 * Sets the price
 	 * 
 	 * @param price
 	 */
 	public void setPrice(float price) {
 		this.price = price;
 	}
 
 	/**
 	 * Sets the validity
 	 * 
 	 * @param valid
 	 */
 	public void setValid(Boolean valid) {
 		this.valid = valid;
 	}
 
 	/**
 	 * Prettyprint any Customer object
 	 * 
 	 * @return the string prettyprinted
 	 */
 	@Override
 	public String toString() {
 		return new String("ID=" + this.getId() + " -- " + "Name="
 				+ this.getName() + " -- " + "Price=" + this.getPrice() + " -- "
 				+ "Valid=" + this.getValid() + " -- " + "Desc="
 				+ this.getDescription());
 	}
 }
