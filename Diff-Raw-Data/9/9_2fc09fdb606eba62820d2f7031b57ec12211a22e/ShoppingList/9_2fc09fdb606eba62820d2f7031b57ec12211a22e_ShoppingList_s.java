 package edu.berkeley.cs160.theccertservice.splist;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 public class ShoppingList {
 	
 	String _name;
 	String _owner;
 	ArrayList<Item> _items;
 	static HashMap<String, ShoppingList> hm = new HashMap<String, ShoppingList>();
 	
 	public ShoppingList(String name, String owner) {
 		_name = name;
 		_owner = owner;
 		_items = new ArrayList<Item>();
 		hm.put(name, this);
 		
 	}
 	
 	public void setOwner(String _owner) {
 		this._owner = _owner;
 	}
 	
 	public String getOwner() {
 		return _owner;
 	}
 	
 	public void addItem(Item item) {
 		_items.add(item);
 	}
 	
 	public Item deleteItem(int pos) {		
 		return _items.remove(pos);
 	}
 
	public Item deleteItem(Item item){
 		return _items.remove(item);
 	}
 	
 	public String getName() {
 		return _name;
 	}
 
 	public void setName(String _name) {
 		this._name = _name;
 	}
 	
 	public Integer getSize() {
 		return _items.size();
 	}
 	
 	public ArrayList<Item> getItems() {
 		return _items;
 	}
 	
 	public static ShoppingList getShoppingList(String name) {
 		return hm.get(name);
 	}
 	
 	public static ArrayList<String> allListNames(){
 		ArrayList<String> list = new ArrayList(hm.keySet());
 		return list;
 	}
 }
