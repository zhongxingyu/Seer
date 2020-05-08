 package com.cse454.nel;
 
 /**
  * Simple entity data structure.
  *
  * @author andrewrogers
  */
 public class Entity {
 
 	public String wikiTitle;
 	public Integer inlinks;
 
 	public Entity(String wikiID) {
 		this.wikiTitle = wikiID;
 	}

 	public String toString() {
		return this.wikiTitle;
 	}
 }
