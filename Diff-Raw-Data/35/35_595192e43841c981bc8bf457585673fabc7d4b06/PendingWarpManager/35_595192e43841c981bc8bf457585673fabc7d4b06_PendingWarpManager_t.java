 package com.mrz.dyndns.server.warpsuite.managers;
 
import java.util.HashMap;
import java.util.Map;
 
 import org.bukkit.Bukkit;
 
 public class PendingWarpManager
 {
 	public PendingWarpManager()
 	{
		playersWaitingToTeleport = new HashMap<String, Integer>();
 	}
 	
	private Map<String, Integer> playersWaitingToTeleport;
 	
	public void addPlayer(String player, int id)
 	{
		playersWaitingToTeleport.put(player, id);
 	}
 	
 	public void removePlayer(String player)
 	{
		Bukkit.getScheduler().cancelTask(playersWaitingToTeleport.get(player));
 		playersWaitingToTeleport.remove(player);
 	}
 	
 	public boolean isWaitingToTeleport(String player)
 	{
 		if(Bukkit.getPlayer(player) == null)
 		{
 			removePlayer(player);
 			return false;
 		}
 		
		return playersWaitingToTeleport.containsKey(player);
 	}
 }
