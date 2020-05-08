 package com.ALC.SC2BOAserver.entities;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.Lob;
 
 @Entity
 public class OnlineBuildOrder {
	public static final String[] races = {"terran","zerg","protoss"};
	
 	@Id
     @GeneratedValue(strategy = GenerationType.AUTO)
 	private String id;
 	@Column(unique=true, nullable=false)
 	private String buildName;
 	@Lob
 	private String buildOrderInstructions;
 	private String race;
 	private float rating;
 	
 	//=========================================
 	public float getRating() {
 		return rating;
 	}
 	public void setRating(float rating) {
 		this.rating = rating;
 	}
 	public String getRace() {
 		return race;
 	}
 	public void setRace(String race) {
 		this.race = race;
 	}
 	@Lob
 	public String getBuildOrderInstructions() {
 		return buildOrderInstructions;
 	}
 	public void setBuildOrderInstructions(String buildOrderInstructions) {
 		this.buildOrderInstructions = buildOrderInstructions;
 	}
 	public String getBuildName() {
 		return buildName;
 	}
 	public void setBuildName(String buildName) {
 		this.buildName = buildName;
 	}
 	@Id
     @GeneratedValue(strategy=GenerationType.AUTO)
 	public String getId() {
 		return id;
 	}
 	public void setId(String Id) {
 		this.id=Id;
 	}
 	
 	public String toString(){
 		return "Name: "+this.buildName+" race: "+this.race;
 	}
 	
 	//======================================================
 	
 	
 	public static OnlineBuildOrder merge(OnlineBuildOrder old,OnlineBuildOrder newbuild){
 		if (newbuild.getBuildName() != null)
 			 old.setBuildName(newbuild.getBuildName());
 		 if (newbuild.getBuildOrderInstructions() != null)
 			 old.setBuildOrderInstructions(newbuild.getBuildOrderInstructions());
 		 return old;
 		
 	}
 	
 	public static List<String> convertBuildsToIds(List<OnlineBuildOrder> builds){
 		ArrayList<String> list = new ArrayList<String>(builds.size());
 		for(int i =0;i<builds.size();i++)list.add(builds.get(i).getId());
 		return list;
 	}
 }
