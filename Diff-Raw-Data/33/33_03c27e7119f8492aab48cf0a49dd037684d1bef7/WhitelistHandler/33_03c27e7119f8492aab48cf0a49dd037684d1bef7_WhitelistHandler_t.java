 package com.mcprohosting.plugins.imraising;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.OfflinePlayer;
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 
 
 public class WhitelistHandler {
 
 	public static void whitelistFromJSON(JSONObject jsonObject) {
 		JSONArray donationJSON = (JSONArray) jsonObject.get("donation");
 		
 		for (Object o : donationJSON) {
 			JSONObject donation = (JSONObject) o;
 			
			String ign = (String) donation.get("custom");

			OfflinePlayer player = Bukkit.getOfflinePlayer(ign);
 			
			if (!player.isWhitelisted()) {
				Number donationAmount = 0;
				donationAmount = (Number) donation.get("amount");
 				
				if (donationAmount.floatValue() >= 25f) {
					String screenName = (String) donation.get("screen");
 					String message = (String) donation.get("comment");
 					
					player.setWhitelisted(true);
 					
					Bukkit.broadcastMessage(ChatColor.GREEN + "Thank you to " + ChatColor.YELLOW + screenName + ChatColor.GREEN + " for donating " + ChatColor.YELLOW + "$" + donationAmount + ChatColor.GREEN + "!");
 					Bukkit.broadcastMessage(ChatColor.GREEN + message);
 				}
 			}
 		}
 	}
 }
