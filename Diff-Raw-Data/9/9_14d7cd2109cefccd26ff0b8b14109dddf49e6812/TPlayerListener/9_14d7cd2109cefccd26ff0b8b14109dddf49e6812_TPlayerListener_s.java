 package com.lebelw.Tickets;
 
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerListener;
 
 public class TPlayerListener extends PlayerListener {
 	
 	private final Tickets plugin;
 
     public TPlayerListener(Tickets instance) {
         plugin = instance;
     }
     @Override
     public void onPlayerJoin(PlayerJoinEvent event){
    	if (!plugin.checkIfPlayerExists(event.getPlayer().getName(),null,1))
     		plugin.createPlayerTicketAccount(event.getPlayer().getName());
     }
 }
