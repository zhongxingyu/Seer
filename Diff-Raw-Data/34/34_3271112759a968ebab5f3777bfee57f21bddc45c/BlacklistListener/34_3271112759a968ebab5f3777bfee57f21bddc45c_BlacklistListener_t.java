 package com.archmageinc.BlacklistCheck;
 
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerPreLoginEvent;
 import org.bukkit.event.player.PlayerPreLoginEvent.Result;
 
 public class BlacklistListener implements Listener {
 	
 	private BlacklistCheck plugin;
 	
 	public BlacklistListener(BlacklistCheck plugin){
 		this.plugin	=	plugin;
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerPreLoginEvent(PlayerPreLoginEvent event){
 		if(plugin.getConfig().getBoolean("DelayCheck"))
 			return;
 		
 		if(plugin.isBlacklisted(event.getAddress())){
 			event.setKickMessage(plugin.getConfig().getString("DisconnectMessage"));
 			if(plugin.getConfig().getBoolean("LogDisconnects"))
 				plugin.logMessage(event.getAddress().toString()+" has been blocked from connecting (DNSBL address)");
 			
 			event.setResult(Result.KICK_OTHER);
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerLoginEvent(PlayerLoginEvent event){
 		if(!plugin.getConfig().getBoolean("DelayCheck"))
 			return;
 		
		plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new BlacklistTask(plugin,event.getPlayer()),30);
 	}
 
 }
