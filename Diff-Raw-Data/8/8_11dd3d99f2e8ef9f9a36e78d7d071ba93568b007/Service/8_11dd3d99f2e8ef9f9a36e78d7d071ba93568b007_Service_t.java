 package com.fanitoz.hairdresser;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.Table;
 
 @Entity
 @Table(name="services") 
 public class Service {
 
 	@Id @GeneratedValue
 	private int id;
 	private String type;
 	private String category;
 	
 	@Column(nullable=false)
 	private String name;	
 	private Double price;
 	
 	public Service() {
 		this.id = 0;
 		this.type = "type";
 		this.category = "category";
 		this.name = "name";
		this.price = (double) 0;
 	}
 	
 	public Service(String type, String category, String name, Double price) {
 		this.type = type;
 		this.category = category;
 		this.name = name;
 		this.price = price;
 	}
 	
 	public int getId() {
 		return id;
 	}
 
 	public void setId(int id) {
 		this.id = id;
 	}	
 	
 	public String getType() {
 		return type;
 	}
 
 	public void setType(String type) {
 		this.type = type;
 	}
 	
 	public String getCategory() {
 		return category;
 	}
 
 	public void setCategory(String category) {
 		this.category = category;
 	}
 	
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public Double getPrice() {
 		return price;
 	}
 
 	public void setPrice(Double price) {
 		this.price = price;
 	}	
 }
