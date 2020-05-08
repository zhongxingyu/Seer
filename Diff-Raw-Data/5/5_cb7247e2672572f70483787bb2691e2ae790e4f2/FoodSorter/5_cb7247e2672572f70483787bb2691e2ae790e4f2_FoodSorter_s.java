 package com.teatime.game.model;
 
 import java.util.Comparator;
 
 public class FoodSorter implements Comparator<Human> {
 
 	public int compare(Human arg0, Human arg1) {
 		
		if ( arg0.getFoodScore() < arg1.getFoodScore() ) {
 			return -1;
		} else if ( arg0.getFoodScore() > arg1.getFoodScore() ) {
 			return 1;
 		} else {
 			return 0;
 		}		
 	}
 
 }
