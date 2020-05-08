 package com.alta189.minemail.addons;
 
 import java.util.HashMap;
 
 public class ReadPaper {
	public AddonManager manageAddons;
 	private HashMap<String,Boolean> readers = new HashMap<String,Boolean>();
 	
 	public ReadPaper(AddonManager instance) {
 		this.manageAddons = instance;
 	}
 	
 	private Boolean isReader(String playerName) {
 		if (readers.containsKey(playerName)) {
 			return true;
 		}
 		return false;
 	}
 	
 	public Boolean status(String playerName) {
 		if (this.isReader(playerName)) {
 			return this.readers.get(playerName);
 		} else {
 			this.updateReader(playerName, false);
 			return false;
 		}
 	}
 	
 	public void updateReader(String playerName, Boolean value) {
 		if (this.isReader(playerName)) {
 			if (this.readers.get(playerName) == value) return;
 			this.readers.remove(playerName);
 			this.readers.put(playerName, value);
 		} else {
 			this.readers.put(playerName, value);
 		}
 		
 	}
 	
 	public Boolean toggleReader(String playerName) { //Returns true if player has paper read enabled
 		if (this.isReader(playerName)) {
 			Boolean reader = this.readers.get(playerName);
 			if (reader) {
 				this.updateReader(playerName, false);
 				return false;
 			} else {
 				this.updateReader(playerName, true);
 				return true;
 			}
 		} else {
 			this.updateReader(playerName, true);
 			return true;
 		}
 	}
 }
