 package com.derpicons.gshelf;
 
 import java.sql.Date;
 import java.util.ArrayList;
 
 import android.graphics.drawable.Drawable;
 
 public class Deal {
 
 	private Date ExpirationDate;
 	private String Source;
 	private String Description;
 	private int Key;
 	private ArrayList<Integer> GameKeys;
 
 	public Deal() {
 		
 	}
 
 	public Date getExpirationDate() {
 		return ExpirationDate;
 	}
 
 	public void setExpirationDate(Date d){
 		ExpirationDate = d;	
 	}
 
 	public boolean isValid() {
 		Date CurrentDate = new Date(System.currentTimeMillis());
 		if (CurrentDate.before(ExpirationDate))
 			return true;
 		return false;
 
 	}
 
 	public int getKey() {
 		return Key;
 	}
 
	public String setDescription() {
		return Description;
 	}
 	
 	public String getDescription() {
 		return Description;
 	}
 
 	public String getSource() {
 		return Source;
 	}
 
 	public void setSource(String source) {
 		Source = source;
 	}
 
 	public ArrayList<Integer> getGameKeys() {
 		return GameKeys;
 	}
 
 	public void setGameKeys(ArrayList<Integer> gameKeys) {
 		GameKeys = gameKeys;
 	}
 }
