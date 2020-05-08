 package edu.berkeley.cs160.theccertservice.splist;
 
 import java.util.ArrayList;
 
 public class Item {
 	String _name;
 	Boolean _shared;
 	int _numPeopleSharing;
 	Double _price;
 	ArrayList<String> _peopleSharing;
 	ShoppingList _list;
 	
 	public Item(String name) {
 		_name = name;
 		_shared = false;
 		_price = 0.0;
 		_peopleSharing = new ArrayList<String>();
 	}
 	
 	public Item(String name, Boolean shared, Double price) {
 		_name = name;
 		_shared = shared;
 		_price = price;
 		ArrayList<Person> _peopleSharing = new ArrayList<Person>();
 	}
 	
 	public String getName() {
 		return _name;
 	}
 
 	public void setName(String _name) {
 		this._name = _name;
 	}
 
 	public Boolean getShared() {
 		return _shared;
 	}
 
 	public void setShared(Boolean _shared) {
 		this._shared = _shared;
 	}
 	
 	public double getNumPeopleSharing() {
 		return (double) _peopleSharing.size() + 1;
 	}
 	
 	public void setPrice(double price) {
 		this._price = price;
 	}
 	
 	public double getPrice() {
 		return _price;
 	}
 	
 	public ArrayList<String> getPeopleSharing() {
 		return _peopleSharing;
 	}
 	
 	@Override
 	public String toString(){
 		return getName();
 	}
 }
