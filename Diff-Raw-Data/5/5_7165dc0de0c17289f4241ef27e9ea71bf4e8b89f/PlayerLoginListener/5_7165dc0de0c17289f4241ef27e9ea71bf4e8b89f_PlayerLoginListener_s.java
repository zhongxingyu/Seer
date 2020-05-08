 package com.worldcretornica.ichatplayerlist;
 
 import net.TheDgtl.iChat.iChat;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerListener;
 
 public class PlayerLoginListener extends PlayerListener {
 
 	public static iChatPlayerList plugin;
 	
 	public PlayerLoginListener(iChatPlayerList instance)
 	{
 		plugin = instance;
 	}
 	
 	@Override
 	public void onPlayerJoin(PlayerJoinEvent event) {
 		
 		Player player = event.getPlayer();
 		
 		((iChat) plugin.ichatplugin).info.addPlayer(player);
 		
 		//if(plugin.ichatapi.getPrefix(player) == null)
 		if(((iChat) plugin.ichatplugin).info.getKey(player, "prefix") == null)
 		{
 			plugin.logger.severe("Unable to get prefix");
 		}else{
 			String prefix = ((iChat) plugin.ichatplugin).info.getKey(player, "prefix");
 			
 			if(prefix.lastIndexOf("&") != -1)
 			{
 				int lastcolor = prefix.lastIndexOf("&");
 				
 				String coloredname = "&" + prefix.charAt(lastcolor + 1) + player.getName();
 				
 				String fixedname = ((iChat) plugin.ichatplugin).API.addColor(coloredname);
 				
 				player.setDisplayName(fixedname);
				player.setPlayerListName(fixedname);
 			}
 		}
 		super.onPlayerJoin(event);
 	}
 }
