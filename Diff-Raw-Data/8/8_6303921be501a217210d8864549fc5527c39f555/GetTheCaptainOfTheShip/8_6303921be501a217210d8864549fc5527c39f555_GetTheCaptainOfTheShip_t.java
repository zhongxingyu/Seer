 package com.valtech.devoxx.tddgame.wecodeinpeace.captainselection;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.valtech.devoxx.tddgame.wecodeinpeace.model.Javaltechian;
 import com.valtech.devoxx.tddgame.wecodeinpeace.service.IJavaltechianService;
 import com.valtech.devoxx.tddgame.wecodeinpeace.service.JavaltechianService;
 
 public class GetTheCaptainOfTheShip {
 	IJavaltechianService service= new JavaltechianService();;
 	
 	Javaltechian dowe = new Javaltechian("Dowe",  "John", 34,service.getPowerByName("Dowe"));
 	Javaltechian Brardow = new Javaltechian("Brardow",  "Bernie", 32,service.getPowerByName("Dowe"));
 	Javaltechian Lothalirin1 = new Javaltechian("Lothalirin1",  "Bruce", 44,service.getPowerByName("Dowe"));
 	Javaltechian Lothalirin2 = new Javaltechian("Lothalirin2",  "Milkysnow", 36,service.getPowerByName("Dowe"));
 	Javaltechian Dreisean = new Javaltechian("Dreisean",  "Poppins", 23,service.getPowerByName("Dowe"));
 	
 
 	private String populationOfTheShip;
 	
 
 	public String getPopulationOfTheShip() {
 		return populationOfTheShip;
 	}
 
 	public void setPopulationOfTheShip(String populationOfTheShip) {
 		this.populationOfTheShip = populationOfTheShip;
 	}
 	
 	public String whoIsTheCaptain(){
 		List<Javaltechian> javaltechians = new ArrayList<Javaltechian>();
		javaltechians.add(dowe);
 		
		return null;
 		
 	}
 }
