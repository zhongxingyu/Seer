 package com.meleemistress.core;
 
 /**
  * A single Particle object
  * @author hparry
  *
  */
 public class Particle {
 
 	private static final int LUCK_LIMIT = 10;
 	private String name;
 	private int pennies;
 	private int luck;
 	
 	public Particle(String name) {
 		this.name = name;
 		this.luck = 1;
 		this.pennies = 0;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public int getPennies() {
 		return pennies;
 	}
 
 	public void setPennies(int pennies) {
 		this.pennies = pennies;
 	}
 
 	public int getLuck() {
 		return luck;
 	}
 
 	public void setLuck(int luck) {
 		this.luck = luck;
 	}
 	
 	public boolean lookForAPenny() {
 		double m = Math.random() * 10;
 		if (m < luck) {
 			pennies++;
			if (luck < LUCK_LIMIT) {
				luck++;
			}
 			return true;
 		}
 		
 		return false;
 	}
 	
 	
 }
