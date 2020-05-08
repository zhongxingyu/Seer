 package com.eseteam9.shoppyapp.valuesets;
 
 import java.util.Date;
 import com.eseteam9.shoppyapp.handlers.ItemHandler;
 import android.database.Cursor;
 import android.content.ContentValues;
 
 public class ItemValueSet {
 	public final int id;
 	public final String onlineKey;
 	public final int listId;
 	public final String name;
 	public final String quantity;
 	public final boolean bought;
 	public final Date timestamp;
 
 	public ItemValueSet(int id,  String onlineKey, int listId, String name, String quantity, boolean bought, Date timestamp) {
 		this.id = id;
 		this.onlineKey = onlineKey;
 		this.listId = listId;
 		this.name = name;
 		this.quantity = quantity;
 		this.bought = bought;
 		this.timestamp = timestamp;
 	}
 
 	public ItemValueSet(int listId, String name, String quantity) {
 		this.id = 0;
 		this.onlineKey = "0";
 		this.listId = listId;
 		this.name = name;
 		this.quantity = quantity;
 		this.bought = false;
 		this.timestamp = new Date();
 	}
 
 	public ItemValueSet(Cursor c) {
 		this.id = c.getInt(0);
 		this.onlineKey = c.getString(6);
 		this.listId = c.getInt(1);
 		this.name = c.getString(2);
 		this.quantity = c.getString(3);
 		this.bought = c.getInt(4) == 1;
 		this.timestamp = new Date(c.getLong(5));
 	}
 
 	public ContentValues getContentValues(boolean withId) {
 		ContentValues returnValues = new ContentValues();
 		if(withId) returnValues.put(ItemHandler.KEY_ID, id);
 		returnValues.put(ItemHandler.KEY_ONLINEKEY, onlineKey);
 		returnValues.put(ItemHandler.KEY_NAME, name);
 		returnValues.put(ItemHandler.KEY_LIST_ID, listId);
 		returnValues.put(ItemHandler.KEY_QUANTITY, quantity);
 		returnValues.put(ItemHandler.KEY_BOUGHT, (bought ? 1 : 0));
		returnValues.put(ItemHandler.KEY_TIMESTAMP, timestamp.getTime());
 		return returnValues;
 	}
 
 	public boolean equals(Object other) {
 		if (other == null) return false;
 		if (other == this) return true;
 		if (!(other instanceof ItemValueSet)) return false;
 	 	ItemValueSet that = (ItemValueSet)other;
 
 		return this.listId == that.listId
 			&& this.name.equals(that.name)
 			&& this.quantity.equals(that.quantity)
 			&& this.onlineKey.equals(that.onlineKey)
 			&& this.bought == that.bought;
 	}
 }
