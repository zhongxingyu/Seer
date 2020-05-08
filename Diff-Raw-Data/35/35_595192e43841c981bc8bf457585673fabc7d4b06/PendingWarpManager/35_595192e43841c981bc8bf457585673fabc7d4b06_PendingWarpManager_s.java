 package com.mrz.dyndns.server.warpsuite.managers;
 
import java.util.HashSet;
import java.util.Set;
 
 import org.bukkit.Bukkit;
 
 public class PendingWarpManager
 {
 	public PendingWarpManager()
 	{
		playersWaitingToTeleport = new HashSet<String>();
 	}
 	
	private Set<String> playersWaitingToTeleport;
 	
	public void addPlayer(String player)
 	{
		playersWaitingToTeleport.add(player);
 	}
 	
 	public void removePlayer(String player)
 	{
 		playersWaitingToTeleport.remove(player);
 	}
 	
 	public boolean isWaitingToTeleport(String player)
 	{
 		if(Bukkit.getPlayer(player) == null)
 		{
 			removePlayer(player);
 			return false;
 		}
 		
		return playersWaitingToTeleport.contains(player);
 	}
 }
