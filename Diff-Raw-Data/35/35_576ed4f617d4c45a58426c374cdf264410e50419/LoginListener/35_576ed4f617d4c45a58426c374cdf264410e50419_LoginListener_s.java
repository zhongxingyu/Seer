 
 package org.s4x8.bukkit.proxykicker;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
 
 public class LoginListener implements Listener {
 	private ProxyKicker plugin;
 
 	public LoginListener(ProxyKicker plugin) {
 		this.plugin = plugin;
 	}
 	
 	@EventHandler
	public void onPlayerLogin(PlayerJoinEvent event) {
 		Player player = event.getPlayer();
 		
 		if (player.hasPermission("proxykicker.bypass")) {
 			return; // This player is exempt of checking
 		};
 
 		if (plugin.getTorDatabase().isTorIp(player.getAddress().getAddress())) {
 			plugin.getLogger().info("Kicking " + player.getName() + " because his/her IP is in the Tor database");
			player.kickPlayer("Your IP is on the server Tor database");
 		};
 		
 		(new PortChecker(plugin, player)).start();
 	};
 };
