 package com.braintrust.shootmanager.model;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class Photographer extends Model {
 	public Photographer(int id) {
 		super(id);
 		
 		this.shoots = new ArrayList<Shoot>();
 	}
 
 	private String name;
	private Boolean retired;
 	private List<Shoot> shoots;
 	private List<Equipment> equipment;
 	
 	public String getName(){
 		return this.name;
 	}
 	
 	public void setName(String name) {
 		this.name = name;
 	}
 	
	public Boolean getRetired(){
 		return this.retired;
 	}
 	
	public void setRetired(Boolean retired){
 		this.retired = retired;
 	}
 	
 	public List<Shoot> getShoots(){
 		return this.shoots;
 	}
 	
 	public void addShoot(Shoot shoot){
 		this.shoots.add(shoot);
 	}
 	
 	public void addShoot(List<Shoot> shoots){
 		this.shoots.addAll(shoots);
 	}
 	
 	public List<Equipment> getEquipment(){
 		return this.equipment;
 	}
 	
 	public void addEquipment(Equipment equipment){
 		this.equipment.add(equipment);
 	}
 	
 	public void addEquipment(List<Equipment> equipment){
 		this.equipment.addAll(equipment);
 	}
 	
 }
