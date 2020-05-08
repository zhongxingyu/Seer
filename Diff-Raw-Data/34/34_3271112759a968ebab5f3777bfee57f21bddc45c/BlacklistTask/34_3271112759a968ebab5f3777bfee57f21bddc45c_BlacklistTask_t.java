 package com.archmageinc.BlacklistCheck;
 
 import java.net.InetAddress;
 import java.util.TimerTask;
 
 import org.bukkit.entity.Player;
 
 public class BlacklistTask extends TimerTask  {
 
 	private Player player;
 	private BlacklistCheck plugin;
 	private InetAddress IP;
 	
 	public BlacklistTask(BlacklistCheck instance,Player p){
 		plugin		=	instance;
 		player		=	p;
 		
		
			}
 	
 	@Override
 	public void run() {
		if(player!=null && player.getAddress()!=null)
			IP			=	player.getAddress().getAddress();
		else
			plugin.logWarning("Unable to determine if a Player's address is blacklisted because the player's address could not be found!");

 		if(player!=null && IP!=null && plugin.isBlacklisted(IP)){
 			if(plugin.getConfig().getBoolean("LogDisconnects"))
 				plugin.logMessage(IP.toString()+" has been kicked (DNSBL address)");
 			
 			player.kickPlayer(plugin.getConfig().getString("DisconnectMessage"));
 		}		
 	}
 
 }
