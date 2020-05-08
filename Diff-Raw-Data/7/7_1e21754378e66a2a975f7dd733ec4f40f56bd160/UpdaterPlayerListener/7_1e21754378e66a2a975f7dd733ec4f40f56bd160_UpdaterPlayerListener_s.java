 package com.moosemanstudios.Notebook.Bukkit;
 
 
 import net.gravitydevelopment.updater.Updater;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 
 public class UpdaterPlayerListener implements Listener {
 	
 	private Notebook plugin;
 	
 	UpdaterPlayerListener(Notebook plugin) {
 		this.plugin = plugin;
 	}
 	
 	@EventHandler
 	public void onPlayerJoin(PlayerJoinEvent event) {
 		Player player=  event.getPlayer();
 		
		if (player.hasPermission("glownightlight.admin")) {
 			Updater updater = new Updater(plugin, 35179, plugin.getFileFolder(), Updater.UpdateType.NO_DOWNLOAD, false);
 			
 			if (updater.getResult() == Updater.UpdateResult.UPDATE_AVAILABLE && plugin.updaterNotify && plugin.updaterEnabled) {
				player.sendMessage(ChatColor.AQUA + "An update is avaiable for GlowNightLight: " + updater.getLatestName());
				player.sendMessage(ChatColor.AQUA + "Type " + ChatColor.WHITE + "/gnl update" + ChatColor.AQUA + " to update");
 			}
 		}
 	}
 }
