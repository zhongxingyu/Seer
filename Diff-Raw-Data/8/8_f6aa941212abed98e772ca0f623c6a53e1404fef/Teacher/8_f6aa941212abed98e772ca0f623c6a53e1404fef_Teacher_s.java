 /**
  * 
  */
 package model;
 
 /**
  * 
  * @author Emin
  * @version 9/10/2013
  *
  */
 public enum Teacher {
 	JACOBS ("Jacobs"),
 	LEEUWEN ("Leeuwen"),
 	BAKKER ("Bakker"),
 	MULDER ("Mulder"),
 	SMIT ("Smit");
 	
 	private final String name;
 	
 	Teacher(final String name) {
 	      this.name = name;
 	}
 
 	@Override
 	public String toString() {
 	return name;
 }
