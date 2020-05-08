 /*
  *  Copyright:
  *  2013 Darius Mewes
  */
 
 package de.dariusmewes.TimoliaCore.events;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerLoginEvent.Result;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 import de.dariusmewes.TimoliaCore.TimoliaCore;
 import de.dariusmewes.TimoliaCore.commands.access;
 import de.dariusmewes.TimoliaCore.commands.deaths;
 
 public class PlayerListener implements Listener {
 
 	public static String joinMsg;
 	public static String quitMsg;
 	private TimoliaCore instance;
 
 	public PlayerListener(TimoliaCore instance) {
 		this.instance = instance;
 	}
 
 	@EventHandler
 	public void onPlayerJoin(PlayerJoinEvent event) {
 		if (!joinMsg.equalsIgnoreCase(""))
 			event.setJoinMessage(joinMsg.replaceAll("@p", event.getPlayer().getName()));
 
 		if (TimoliaCore.updateAvailable && (event.getPlayer().isOp() || event.getPlayer().hasPermission("headdrops.update"))) {
 			event.getPlayer().sendMessage(TimoliaCore.PREFIX + "A new version is available!");
 			event.getPlayer().sendMessage(TimoliaCore.PREFIX + "Get it at http://dev.bukkit.org/server-mods/timolia-core");
 		}
 
 		if (event.getPlayer().hasPermission("tcore.listname.join")) {
 			if (event.getPlayer().hasPermission("tcore.listname.red"))
 				event.getPlayer().setPlayerListName(ChatColor.RED + event.getPlayer().getName());
 
 			else if (event.getPlayer().hasPermission("tcore.listname.blue"))
 				event.getPlayer().setPlayerListName(ChatColor.BLUE + event.getPlayer().getName());
 
 			else if (event.getPlayer().hasPermission("tcore.listname.green"))
 				event.getPlayer().setPlayerListName(ChatColor.GREEN + event.getPlayer().getName());
 
 			else if (event.getPlayer().hasPermission("tcore.listname.orange"))
 				event.getPlayer().setPlayerListName(ChatColor.GOLD + event.getPlayer().getName());
 		}
 	}
 
 	@EventHandler
 	public void onPlayerQuit(PlayerQuitEvent event) {
		if (!quitMsg.equalsIgnoreCase(""))
 			event.setQuitMessage(quitMsg.replaceAll("@p", event.getPlayer().getName()).replaceAll("@c", String.valueOf(Bukkit.getOnlinePlayers().length)));
 	}
 
 	// Wartung
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onPlayerLogin(PlayerLoginEvent event) {
 		if (instance.getConfig().getBoolean("maintenance") && !(access.isAllowed(event.getPlayer()))) {
 			event.setKickMessage(ChatColor.translateAlternateColorCodes('&', instance.getConfig().getString("servername")) + ChatColor.WHITE + " " + instance.getConfig().getString("maintenancemsg"));
 			event.setResult(Result.KICK_OTHER);
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onPlayerDeath(PlayerDeathEvent event) {
 		if (deaths.hidingEnabled) {
 			String vanillaMsg = event.getDeathMessage();
 			event.setDeathMessage("");
 			for (Player p : Bukkit.getOnlinePlayers()) {
 				if (deaths.shuttedOff.contains(p.getName()))
 					continue;
 
 				if (instance.getConfig().getBoolean("darkerDeathMessages"))
 					p.sendMessage(ChatColor.DARK_GRAY + vanillaMsg);
 				else
 					p.sendMessage(vanillaMsg);
 			}
 		}
 	}
 
 }
