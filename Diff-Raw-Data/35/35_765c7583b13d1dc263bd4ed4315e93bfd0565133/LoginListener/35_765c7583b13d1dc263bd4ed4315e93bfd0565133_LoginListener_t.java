 package com.github.thebiologist13.listeners;
 
 import org.bukkit.ChatColor;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 
 import com.github.thebiologist13.NeverBreak;
 
 public class LoginListener implements Listener {
 	
 	private NeverBreak plugin;
 	
 	private FileConfiguration config;
 	
 	public LoginListener(NeverBreak neverBreak) {
 		this.plugin = neverBreak;
 		this.config = neverBreak.getCustomConfig();
 	}
 	
 	/*
 	 * All this class does is set the player's NeverBreak mode 
 	 * to the automatic one in the config.yml on join.
 	 */
 	
 	@EventHandler
 	public void onJoin(PlayerJoinEvent ev) {
 		Player p = ev.getPlayer();
		plugin.setMode(p, config.getBoolean("autoNeverBreak", false));
 		if(config.getBoolean("messageOnLogin", true)) {
 			plugin.sendMessage(p, ChatColor.GREEN + "Your NeverBreak mode has been set to " + ChatColor.GOLD + config.getBoolean("autoNeverBreak") + ChatColor.GREEN + " !");
 		}
 	}
 }
