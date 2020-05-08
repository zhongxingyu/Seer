 package com.theminequest.MineQuest.Player;
 
 import java.util.LinkedHashMap;
 
 import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
 
public class PlayerManager implements Listener {
 
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
 	
	@EventHandler
	public static void onPlayerJoin(PlayerJoinEvent e){
		playerAcct(e.getPlayer());
	}
	
	@EventHandler
	public static void onPlayerQuit(PlayerQuitEvent e){
		getPlayerDetails(e.getPlayer()).save();
	}
	
	@EventHandler
	public static void onPlayerKick(PlayerKickEvent e){
		getPlayerDetails(e.getPlayer()).save();
	}
	
 }
