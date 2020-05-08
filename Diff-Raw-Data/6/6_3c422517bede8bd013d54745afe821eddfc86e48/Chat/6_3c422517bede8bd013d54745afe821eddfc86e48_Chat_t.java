 package com.lala.wordrank;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.PlayerListener;
 
 public class Chat extends PlayerListener{
 	@SuppressWarnings("unused")
 	private WordRank plugin;
 	public Chat(WordRank plugin){
 		this.plugin = plugin;
 	}
 	public void onPlayerChat(PlayerChatEvent event){
 		if (Config.exists(event.getMessage()) && WordRank.permissionHandler.has(event.getPlayer(), "WordRank.say") || Config.exists(event.getMessage()) && WordRank.permissionHandler.has(event.getPlayer(), "WordRank." + event.getMessage())){
 			Player player = event.getPlayer();
 			if (WordRank.permissionHandler.inGroup(player.getWorld().getName(), player.getName(), Config.getWordGroup(event.getMessage()))){
 				player.sendMessage(ChatColor.RED + "You can't use a magic word for a group you are already in!");
 				return;
 			}else{
 				Config.addParent(event.getPlayer(), Config.getWordGroup(event.getMessage()));
 				player.sendMessage(ChatColor.GOLD + Config.getCongratsMsg().replaceAll("%player%", event.getPlayer().getDisplayName()).replaceAll("%group%", Config.getWordGroup(event.getMessage())));
 				event.setCancelled(true);
 				return;
 			}
 		}else{
			if (Config.exists(event.getMessage())){
				event.getPlayer().sendMessage(ChatColor.RED + "You do not have permission to do that!");
				return;
			}
 		}
 	}
 }
