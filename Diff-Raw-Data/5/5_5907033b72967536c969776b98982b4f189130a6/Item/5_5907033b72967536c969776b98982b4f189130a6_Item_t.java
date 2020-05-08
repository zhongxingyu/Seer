 package main.java.knapsack;
 
 public class Item {
 
     int value;
     int weight;
     int lbl;
 
     public Item(int value, int weight, int name) {
 	if (weight <= 0)
	    throw new IllegalArgumentException("weight of item should be positive");
 	if (value <= 0)
	    throw new IllegalArgumentException("value of item should be positive");
 	this.value = value;
 	this.weight = weight;
 	this.lbl = name;
     }
 }
