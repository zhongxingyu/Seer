 /**
  * 
  */
 package com.booze.app.model;
 
 /**
 * @author Sushant Kumar Singh
 * 
 */
 public class Product {
 
 	private long id = 0;
 	private String name = null;
 	private long type = 0;
 	private float volume = 0;
 	private String companyName = null;
 	private String description = null;
 	private String imageLocation = null;
 	private double price = 0.0;
 	
 	public long getId() {
 		return id;
 	}
 	public void setId(long id) {
 		this.id = id;
 	}
 	public String getName() {
 		return name;
 	}
 	public void setName(String name) {
 		this.name = name;
 	}
 	public long getType() {
 		return type;
 	}
 	public void setType(long type) {
 		this.type = type;
 	}
 	public float getVolume() {
 		return volume;
 	}
 	public void setVolume(float volume) {
 		this.volume = volume;
 	}
 	public String getCompanyName() {
 		return companyName;
 	}
 	public void setCompanyName(String companyName) {
 		this.companyName = companyName;
 	}
 	public String getDescription() {
 		return description;
 	}
 	public void setDescription(String description) {
 		this.description = description;
 	}
 	public String getImageLocation() {
 		return imageLocation;
 	}
 	public void setImageLocation(String imageLocation) {
 		this.imageLocation = imageLocation;
 	}
 	public double getPrice() {
 		return price;
 	}
 	public void setPrice(double price) {
 		this.price = price;
 	}
 }
