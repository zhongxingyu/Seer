 package uk.codingbadgers.bcreative;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.logging.Level;
 
 import org.bukkit.GameMode;
 import org.bukkit.Material;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerDropItemEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 
 import uk.codingbadgers.bFundamentals.bFundamentals;
 import uk.codingbadgers.bFundamentals.module.Module;
 import uk.codingbadgers.bFundamentals.player.FundamentalPlayer;
 
 public class bCreative extends Module implements Listener {
 
 	private List<String> activeWorlds;
 	private List<Material> interactBlacklist;
 
 	@Override
 	public void onEnable() {
 		register(this);
 		
 		FileConfiguration config = getConfig();
 		config.addDefault("worlds", new ArrayList<String>());
 		config.addDefault("interact-blacklist", Arrays.asList("ENDER_CHEST", "CHEST"));
 		config.options().copyDefaults(true);
 		saveConfig();
 		
 		activeWorlds = config.getStringList("worlds");
 		interactBlacklist = new ArrayList<Material>();
 		for (String mat : config.getStringList("interact-blacklist")) {
 			try {
 				int id = Integer.parseInt(mat);
 				interactBlacklist.add(Material.getMaterial(id));
 			} catch (Exception ex) {
 				interactBlacklist.add(Material.getMaterial(mat));
 			}
 		}
 		log(Level.INFO, activeWorlds.toString());
 	}
 
 	@Override
 	public void onDisable() {
 		activeWorlds.clear();
 		interactBlacklist.clear();
 	}
 	
 	@EventHandler
 	public void onPlayerInteract(PlayerInteractEvent event) {
 		if (hasPermission(event.getPlayer(), "bcreative.chest.open")) {
 			return;
 		}
 		
 		if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
 			return;
 		}
 
 		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
 			return;
 		}
 		
 		if (!activeWorlds.contains(event.getPlayer().getWorld().getName())) { 
 			return;
 		}
 		
 		if (!interactBlacklist.contains(event.getClickedBlock().getType())) {
 			return;
 		}
 		
 		event.setCancelled(true);
 		sendMessage(getName(), event.getPlayer(), "You cannot open chest's whilst in creative");
 	}
 
 	@EventHandler
 	public void onPlayerItemDrop(PlayerDropItemEvent event) {
 		if (hasPermission(event.getPlayer(), "bcreative.item.drop")) {
 			return;
 		}
 		
 		if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
 			return;
 		}
 		
 		if (!activeWorlds.contains(event.getPlayer().getWorld().getName())) { 
 			return;
 		}
 		
 		event.setCancelled(true);
 		sendMessage(getName(), event.getPlayer(), "You cannot drop items whilst in creative");
 	}
 	
 	@EventHandler
 	public void onPlayerItemPickup(PlayerPickupItemEvent event) {
 		if (hasPermission(event.getPlayer(), "bcreative.item.pickup")) {
 			return;
 		}
 		
 		if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
 			return;
 		}
 		
 		if (!activeWorlds.contains(event.getPlayer().getWorld().getName())) { 
 			return;
 		}
 		
 		event.setCancelled(true);
 		// TODO work out better way to send message to player, this is spammy
 		//sendMessage(getName(), event.getPlayer(), "You cannot pickup items whilst in creative");
 	}
 
 	@EventHandler
 	public void onPlayerTeleport(PlayerTeleportEvent event) {
 		if (hasPermission(event.getPlayer(), "bcreative.teleport")) {
 			return;
 		}
 		
 		if (event.getTo().getWorld() == event.getFrom().getWorld()) {
 			return;
 		}
 		
 		FundamentalPlayer player = bFundamentals.Players.getPlayer(event.getPlayer());
 
 		// Teleporting to creative world
 		if (activeWorlds.contains(event.getTo().getWorld().getName())) {
 			// Order is everything
			player.backupInventory(event.getFrom().getWorld(), true);
 			player.restoreInventory(event.getTo().getWorld());
 			sendMessage(getName(), event.getPlayer(), "You're inventory has been backed up whilst you are in the creative world");
 		}
 
 		// Teleporting from creative world
 		if (activeWorlds.contains(event.getFrom().getWorld().getName())) {	
 			// Order is everything		
 			player.backupInventory(event.getFrom().getWorld(), true);			
			player.restoreInventory(event.getTo().getWorld());
 			sendMessage(getName(), event.getPlayer(), "You're inventory has been restored now you have left the creative world");
 		}
 	}
 }
