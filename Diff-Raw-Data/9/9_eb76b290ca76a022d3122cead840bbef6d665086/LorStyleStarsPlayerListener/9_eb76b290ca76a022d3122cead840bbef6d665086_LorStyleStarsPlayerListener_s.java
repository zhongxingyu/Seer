 package ru.ttyh.LorStyleStars;
 
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerListener;
 
 public class LorStyleStarsPlayerListener extends PlayerListener {
 
	 public void onPlayerLogin(PlayerJoinEvent event) {
 		 String name = event.getPlayer().getName();
 		 LorStyleStarsSystem.updScore(name);
 	 }
  
 }
