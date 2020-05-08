 package com.theminequest.MineQuest.Player;
 
 import java.util.LinkedHashMap;
 
 import org.bukkit.entity.Player;
 
public class PlayerManager {
 
 	private static LinkedHashMap<Player,PlayerDetails> players = new LinkedHashMap<Player,PlayerDetails>();
 	
 	private static void playerAcct(Player p){
 		if (!players.containsKey(p)){
 			players.put(p,new PlayerDetails(p));
 		}
 	}
 	
 	public static PlayerDetails getPlayerDetails(Player p){
 		playerAcct(p);
 		return players.get(p);
 	}
 	
 }
