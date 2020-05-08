 package com.martinbrook.tesseractuhc.listeners;
 
 import org.bukkit.ChatColor;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 
 import com.martinbrook.tesseractuhc.UhcMatch;
 import com.martinbrook.tesseractuhc.UhcPlayer;
 
 public class ChatListener implements Listener {
 	private UhcMatch m;
 	public ChatListener(UhcMatch m) { this.m = m; }
 
 	@EventHandler
 	public void onPlayerChatEvent(AsyncPlayerChatEvent e) {
 		UhcPlayer pl = m.getPlayer(e.getPlayer());
 		if (m.isChatMuted() && !pl.isAdmin())
 			e.setCancelled(true);
 		
 		// Admins get gold names, spectators get gray
 		if (pl.isAdmin()) {
			e.setFormat("<" + ChatColor.GOLD + "$s" + ChatColor.RESET + "> %s");
 		} else if (pl.isSpectator()) {
			e.setFormat("<" + ChatColor.GRAY + "$s" + ChatColor.RESET + "> %s");
 		}
 		
 	}
 	
 	@EventHandler
 	public void onPlayerComandPreprocessEvent(PlayerCommandPreprocessEvent e) {
 		if (m.isChatMuted() && !m.getPlayer(e.getPlayer()).isAdmin() && e.getMessage().toLowerCase().startsWith("/me"))
 			e.setCancelled(true);
 	}
 
 }
