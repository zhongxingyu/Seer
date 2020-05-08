 package com.aj3.kiss.models;
 
import android.app.Activity;

 import com.aj3.kiss.helpers.DatabaseHelper;
 
 public class ListItem {
 	private Item item;
 	private double quantity;
 	
 	public ListItem(Item item, double quantity) {
 		this.item = item;
 		this.quantity = quantity;
 	}
 	
 	public ListItem() {}
 
 	public Item getItem() {
 		return item;
 	}
 
 	public void setItem(Item item) {
 		this.item = item;
 	}
 
 	public double getQuantity() {
 		return quantity;
 	}
 
 	public void setQuantity(double quantity) {
 		this.quantity = quantity;
 	}
 	
 	public double updateQuantity(double quantity) {
 		double newQuantity = this.quantity += quantity;
 		if (newQuantity < 0) {
 			// Throw exception here
 		}
 		this.setQuantity(newQuantity);
 		return newQuantity;
 	}
 	
 	/**
 	 * Checks if the item should be added to Grocery List
 	 */
 	public void checkQuantity() {
 		if (quantity < Item.THRESHOLD_QUANTITY) {
			DatabaseHelper db = new DatabaseHelper(new Activity());
 			ListItem li = new ListItem(this.getItem(), Item.INITIAL_QUANTITY);
 			db.addGroceryItem(li);
			db.close();
 		}
 	}
 	
 	public String toString() {
 		return item.toString();
 	}
 	
 	public boolean equals(ListItem li) {
 		return this.getItem().equals(li.getItem());
 	}
 }
