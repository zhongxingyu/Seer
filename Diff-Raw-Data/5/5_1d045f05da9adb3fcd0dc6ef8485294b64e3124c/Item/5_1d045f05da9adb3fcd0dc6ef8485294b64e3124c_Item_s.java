  package edu.gatech.cs2340_sp13.teamrocket.findmythings;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import android.content.Context;
 
 public class Item {
 	
 	public enum Type {
 		Lost(0), Found(1), Donation(2), Request(3);
 		
 		private int idx;
 		
 		private Type(int i)
 	    {
 	        idx = i;
 	    }
 		
 		public String getLocalizedValue(Context context) {
 			return context.getResources().getStringArray(R.array.item_classes)[idx];
 		}
 		
 		public String getLocalizedDescription(Context context) {
 			return context.getResources().getStringArray(R.array.item_class_descriptions)[idx];
 		}
 		
 		public String getListActivityTitle(Context context) {
 			return context.getResources().getStringArray(R.array.item_list_titles)[idx];
 		}
 		
 		public static Type forInt(int value) {
 			return Type.values()[value];
 		}
 		
 		public static final String ID = "item_class";
 	}
 	
 	private String name, loc;
 	
 	private String description = "";
 	
 	private boolean open;
 	
 	/**
 	 * Using enum for type and category, can always change to 
 	 * int later if you guys prefer.
 	 */ 
 	public enum Category {HEIR, KEEPSAKE, MISC};
 	
 	private Type mType;
 
 	private Category cat;
 	
 	private Date date;
 	
 	private int reward;
 	
 	public Item (String name, int reward) {
 		open = true;
 		mType = Type.Found;
 		cat = Category.MISC;
 				
 		loc = "The streets";
 		
 		//Gets todays date - maybe
 		
 		date = new Date();
 		
 		
 		this.name = name;
 		this.reward = reward;
 	}
 	
 	
 	// Setters
 	public void setDescription(String s) {
 		if(s!=null)
 			description = s;
 	}
 	
 	public void setLoc(String s) {
 		if(s!=null)
 			loc=s;
 	}
 	
 	public void setType(Item.Type c) {
 		mType = c;
 	}
 	
 	public void setCat(int i) {
 		switch(i) {
 		case 0:
 			cat = Category.HEIR;
 			break;
 		case 1: 
 			cat = Category.KEEPSAKE;
 			break;
 		case 2:
 			cat = Category.MISC;
 			break;
 			
 		}
 			
 	}
 	
 	public void setDate(Date d) {
 		date = d;
 	}
 	
 	public void setReward(int r) {
 		reward = r;
 	}
 	
 	public String getName() {
 		return name;
 	}
 	
 	public String getDescription() {
 		return description;
 	}
 	
 	public boolean isOpen() {
 		return open;
 	}
 	
 	public Item.Type getType() {
 		return mType;
 	}
 	
 	public Item.Category getCat() {
 		return cat;
 	}
 	
 	public String getCatString() {
 		if(cat== Category.HEIR)
 			return "Heirloom";
 		if(cat==Category.KEEPSAKE)
 			return "Keepsake";
 		return "Miscellaneous"; 
 		
 	}
 	
 	public Date getDate() {
 		return date;
 	}
 	
 	public String getDateString() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
 		return dateFormat.format(date);
 	}
 	
 	public int getReward() {
 		return reward;
 	}
 	
 	public String getLoc() {
 		return loc;
 	}
 	
 	@Override
 	public String toString() {
 		String desc = description.length()>10? description.substring(0,10):description.substring(0,description.length());
 		return name + " - " + loc + "\n" + desc;
 	}
 
 }
