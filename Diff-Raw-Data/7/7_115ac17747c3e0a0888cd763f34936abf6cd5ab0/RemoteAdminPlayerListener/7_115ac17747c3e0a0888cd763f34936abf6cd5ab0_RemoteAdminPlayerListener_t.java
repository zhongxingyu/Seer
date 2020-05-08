 package me.coolblinger.remoteadmin.listeners;
 
 import me.coolblinger.remoteadmin.RemoteAdmin;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.*;
 
 public class RemoteAdminPlayerListener extends PlayerListener {
 	private final RemoteAdmin plugin;
 
 	public RemoteAdminPlayerListener(RemoteAdmin instance) {
 		plugin = instance;
 	}
 
 	public void onPlayerChat(PlayerChatEvent event) {
 		String message = event.getMessage().replace("@", "%40");
 		plugin.send("CHAT@" + event.getPlayer().getName() + "@" + message);
 	}
 
 	public void onPlayerJoin(PlayerJoinEvent event) {
 		plugin.send("SYS@PLAYER_JOIN@" + event.getPlayer().getName());
 	}
 
 	public void onPlayerQuit(PlayerQuitEvent event) {
 		plugin.send("SYS@PLAYER_LEAVE@" + event.getPlayer().getName());
 	}
 
 	public void onPlayerInteract(PlayerInteractEvent event) {
 		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
 			org.bukkit.util.Vector vector = event.getClickedBlock().getLocation().toVector();
 			String name = event.getPlayer().getName();
 			String clickedBlock = event.getClickedBlock().getType().name();
 			String holding = event.getPlayer().getItemInHand().getType().name();
 			plugin.send("PLAYER_INTERACT@" + vector.toString() + "@RIGHT@" + name + "@" + clickedBlock + "@" + holding);
 		}
 	}
 
 	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
 		String command = event.getMessage();
 		if (command.startsWith("/login ")) {
 			command = "/login <CENSORED>";
 		} else if (command.startsWith("/register ")) {
 			command = "/register <CENSORED>";
 		}
 		command.replace("@", "%44");
		plugin.send("PLAYER_COMMAND@" + event.getPlayer().getName() + "@" + command);
 	}
 }
