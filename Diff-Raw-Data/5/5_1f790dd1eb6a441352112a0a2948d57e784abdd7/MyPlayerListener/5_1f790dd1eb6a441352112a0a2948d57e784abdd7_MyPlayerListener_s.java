 package me.russjr08.plugins;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerChatEvent;
 public class MyPlayerListener implements Listener{
 	public static IFail plugin;
 	
 	
 	@EventHandler
 	public void onPlayerChat(PlayerChatEvent event){
 		Player player = event.getPlayer();
 		
		if(player.isOp()){
 			player.sendMessage(ChatColor.BLUE + "Ehh, you were close... lucky OPs!");
 		}
		else if(event.getMessage().toLowerCase().contains("fail") || event.getMessage().toLowerCase().contains("fial") || event.getMessage().toLowerCase().contains("f-a-i-l") || event.getMessage().toLowerCase().contains("f.a.i.l.") ){
 			event.setCancelled(true);
 			player.chat(ChatColor.DARK_RED + "I shouldn't say the word for doing something incorrectly!");
 			player.kickPlayer("For failing!");
 		}
 	}
 	
 
 }
