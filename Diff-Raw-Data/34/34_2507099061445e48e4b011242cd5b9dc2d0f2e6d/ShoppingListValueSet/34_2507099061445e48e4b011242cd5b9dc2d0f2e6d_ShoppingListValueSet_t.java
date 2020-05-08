 package com.eseteam9.shoppyapp.valuesets;
 
import java.util.Calendar;
 import java.util.Date;
 
 import com.eseteam9.shoppyapp.handlers.ShoppingListHandler;
 import android.content.ContentValues;
 import android.database.Cursor;
 
 public class ShoppingListValueSet {
 	public final int id;
 	public final String onlineKey;
 	public final String title;
 	public final boolean archived;
 	public final Date timestamp;
 
 	public ShoppingListValueSet(int id, String onlineKey, String title, boolean archived, Date timestamp) {
 		this.id = id;
 		this.onlineKey = onlineKey;
 		this.title = title;
 		this.archived = archived;
 		this.timestamp = timestamp;
 	}
 
 	public ShoppingListValueSet(String title) {
 		this.id = 0;
 		this.onlineKey = "0";
 		this.title = title;
 		this.archived = false;
		Calendar c = Calendar.getInstance();
		this.timestamp = c.getTime();
 	}
 
 	public ShoppingListValueSet(Cursor c) {
 		this.id = c.getInt(0);
 		this.onlineKey = c.getString(2);
 		this.title = c.getString(1);
 		this.archived = c.getInt(3) == 1;
 		this.timestamp = new Date(c.getLong(4));
 	}
 
 	public ContentValues getContentValues(boolean withId) {
 		ContentValues returnValues = new ContentValues();
 		if(withId) returnValues.put(ShoppingListHandler.KEY_ID, id);
 		returnValues.put(ShoppingListHandler.KEY_ONLINE_KEY, onlineKey);
 		returnValues.put(ShoppingListHandler.KEY_TITLE, title);
 		returnValues.put(ShoppingListHandler.KEY_ARCHIVED, (archived ? 1 : 0));
 		returnValues.put(ShoppingListHandler.KEY_TIMESTAMP, timestamp.getTime());
 		return returnValues;
 	}
 
 	public boolean equals(Object other){
 		if (other == null) return false;
 		if (other == this) return true;
 		if (!(other instanceof ShoppingListValueSet))return false;
 		ShoppingListValueSet that = (ShoppingListValueSet)other;
 
 		return this.title.equals(that.title)
 			&& this.onlineKey.equals(that.onlineKey)
 			&& this.archived == that.archived;
 	}
 }
