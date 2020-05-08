 package com.example.howlertodolist;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.UUID;
 
 public class ItemManager {
 	
 	private static ItemManager sInstance;
 	
 	private ArrayList<Item> allItems;
 	
 	public ItemManager(){
 		allItems = new ArrayList<Item>();
 	}
 	
 	// singleton
 	public static ItemManager getInstance(){
 		
 		if(sInstance == null){
 			sInstance = new ItemManager();
 		}
 		
 		return sInstance;
 	}
 	
 	// returns all items
 	public ArrayList<Item> getAllItems(){
 		return allItems;
 	}
 	
 	// release all items
 	public void releaseAllItems(){
 		allItems.clear();
 	}
 	
 	// add Item
 	public void addItem(Item item){
 		allItems.add(item);
 	}
 	
 	// remove item and its image
 	public void removeItem(Item item){
 		
		File file = new File(item.getImageUri().getPath());
		file.delete();
 		
 		allItems.remove(item);
 	}
 	
 	// get item at index
 	public Item getItemAtIndex(int index){
 		return allItems.get(index);
 	}
 	
 	// get by id
 	public Item getById(UUID id){
 		
 		Item item = null;
 		
 		for(Item i : allItems){
 			if( i.getId().equals(id) ){
 				item = i;
 				break;
 			}
 		}
 		
 		return item;
 	}
 	
 } // end class
