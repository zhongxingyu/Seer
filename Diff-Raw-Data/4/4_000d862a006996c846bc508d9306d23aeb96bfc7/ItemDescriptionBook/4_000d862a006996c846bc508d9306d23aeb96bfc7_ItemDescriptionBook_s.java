package com.core;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 
 import com.database.ItemDescriptionBookDB;
 
 public class ItemDescriptionBook {
 	private ItemDescriptionBookDB db;
 	private List<ItemDescription> itemDescription;
 	private Context con;
 	public ItemDescriptionBook(Context context) {
 		con = context;
 		itemDescription = new ArrayList<ItemDescription>();
 	}
 
 	public ItemDescription get(int id) {
 		db = new ItemDescriptionBookDB(con);
 		ItemDescription x = db.findBy(id);
 		db.close();
 		return x;
 	}
 
 	public void add(ItemDescription item) {
 		db = new ItemDescriptionBookDB(con);
 		db.insert(item);
 		db.close();
 		itemDescription.add(item);
 	}
 
 	public int getAmount() {
 		db = new ItemDescriptionBookDB(con);
 		int x = db.findAll().length;
 		db.close();
 		return x;
 	}
 
 	public boolean remove(int id) {
 		db = new ItemDescriptionBookDB(con);
 		db.delete(id);
 		db.close();
 		for (ItemDescription i : itemDescription) {
 			if (i.getId() == id)
 				return itemDescription.remove(i);
 		}
 		return false;
 	}
 
 }
