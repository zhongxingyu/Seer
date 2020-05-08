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
 			
			String playerName = (String) donation.get("custom");
 			
			if (!Bukkit.getWhitelistedPlayers().contains(playerName)) {
				double donationAmount = 0;
				donationAmount = (Double) donation.get("amount");
 				
				if (donationAmount >= 25d) {
 					String message = (String) donation.get("comment");
 					
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "whitelist add " + playerName);
					
					whitelistPlayer(playerName);
 					
					Bukkit.broadcastMessage(ChatColor.GREEN + "Thank you to " + ChatColor.YELLOW + playerName + ChatColor.GREEN + " for donating " + ChatColor.YELLOW + "$" + donationAmount + ChatColor.GREEN + "!");
 					Bukkit.broadcastMessage(ChatColor.GREEN + message);
 				}
 			}
 		}
 	}
	
	public static void whitelistPlayer(String playername) {
		System.out.println("Whitelisting " + playername);
		OfflinePlayer player = Bukkit.getOfflinePlayer(playername);
		
		if (!Bukkit.getWhitelistedPlayers().contains(player)) {
			Bukkit.getWhitelistedPlayers().add(player);
		}
	}
 }
