 package de.xcraft.engelier.XcraftGate;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.PrintStream;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Chunk;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerChangedWorldEvent;
 import org.bukkit.event.player.PlayerGameModeChangeEvent;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 import org.yaml.snakeyaml.Yaml;
 
 public class ListenerPlayer implements Listener {
 	private Location location;
 	private DataGate gate = null;	
 	private XcraftGate plugin = null;
 	private Map<String, String> playerDiedInWorld = new HashMap<String, String>();
 	private Map<String, String> playerLeftInWorld = new HashMap<String, String>();
 
 	public ListenerPlayer(XcraftGate instance) {
 		plugin = instance;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public void loadPlayers() {
 		File configFile = plugin.getConfigFile("playerWorlds.yml");
 		try {
 			Yaml yaml = new Yaml();
 			if ((playerLeftInWorld = (Map<String, String>) yaml.load(new FileInputStream(configFile))) == null) {
 				playerLeftInWorld = new HashMap<String, String>();
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void savePlayers() {
 		File configFile = plugin.getConfigFile("playerWorlds.yml");
 		Yaml yaml = new Yaml();
 		String dump = yaml.dump(playerLeftInWorld);
 		try {
 			FileOutputStream fh = new FileOutputStream(configFile);
 			new PrintStream(fh).println(dump);
 			fh.flush();
 			fh.close();
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 	}
 
 	@EventHandler
 	public void onPlayerLogin(PlayerLoginEvent event) {
 		String worldName = playerLeftInWorld.get(event.getPlayer().getName());
 		DataWorld world = plugin.getWorlds().get(worldName);
 		
 		System.out.println("Player " + event.getPlayer().getName() + " trying to join in world " + worldName);
 		
 		if (world != null && !world.isLoaded())
 			world.load();
 	}
 	
 	@EventHandler
 	public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
 		DataWorld fromWorld = plugin.getWorlds().get(event.getFrom());
 		DataWorld toWorld = plugin.getWorlds().get(event.getPlayer().getWorld());
 		
 		if (plugin.getConfig().getBoolean("invsep.enabled") && !fromWorld.getInventoryGroup().equalsIgnoreCase(toWorld.getInventoryGroup())) {
 			InventoryManager.changeInventory(event.getPlayer(), fromWorld, toWorld);
 		}
 		
 		if (!event.getPlayer().hasPermission("XcraftGate.world.nogamemodechange"))
 			event.getPlayer().setGameMode(GameMode.getByValue(toWorld.getGameMode()));
 		
 		playerLeftInWorld.put(event.getPlayer().getName(), event.getPlayer().getWorld().getName());
 	}
 	
 	@EventHandler
 	public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
 		if (plugin.getConfig().getBoolean("invsep.enabled")) {
 			InventoryManager.changeInventroy(event.getPlayer(), event.getPlayer().getGameMode(), event.getNewGameMode(), plugin.getWorlds().get(event.getPlayer().getWorld()));
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerMove(PlayerMoveEvent event) {
 		location = event.getTo();
 
 		Location portTo = null;
 		Location portFrom = null;
 		
 		if (plugin.getWorlds().get(location.getWorld()) == null) return;
 		
 		int border = plugin.getWorlds().get(location.getWorld()).getBorder();
 		if (border > 0) {
 			double x = location.getX();
 			double z = location.getZ();
 		
 			if (Math.abs(x) >= border || Math.abs(z) >= border) {
 				x = Math.abs(x) >= border ? (x > 0 ? border - 1 : -border + 1) : x;
 				z = Math.abs(z) >= border ? (z > 0 ? border - 1 : -border + 1) : z;
 				
 				Location back = new Location(location.getWorld(), x, location.getY(), z, location.getYaw(), location.getPitch());
 
 				//event.setCancelled(true);
 				event.setTo(back);
 				event.getPlayer().sendMessage(ChatColor.RED	+ "You reached the border of this world.");
 				return;
 			}			
 		}
 				
 		portTo = plugin.justTeleported.get(event.getPlayer().getName());		
 		portFrom = plugin.justTeleportedFrom.get(event.getPlayer().getName());
 		
 		if (portTo != null && portFrom == null)
 			plugin.justTeleported.remove(event.getPlayer().getName());
 		
 		if (portTo == null && portFrom != null)
 			plugin.justTeleportedFrom.remove(event.getPlayer().getName());
 		
 		if (portTo != null && portFrom != null) {
 			if ((Math.floor(portTo.getX()) != Math.floor(location.getX()) || Math.floor(portTo.getZ()) != Math.floor(location.getZ()))
 				&& (Math.floor(portFrom.getX()) != Math.floor(location.getX()) || Math.floor(portFrom.getZ()) != Math.floor(location.getZ()))) {
 				plugin.justTeleported.remove(event.getPlayer().getName());
 				plugin.justTeleportedFrom.remove(event.getPlayer().getName());
 			}
 		} else if ((gate = plugin.getGates().getByLocation(location)) != null) {
 			if (plugin.getPluginManager().getPermissions() == null ?
 					event.getPlayer().hasPermission("XcraftGate.use." + gate.getName()) :
 					plugin.getPluginManager().getPermissions().has(event.getPlayer(), "XcraftGate.use." + gate.getName())) {
 				plugin.justTeleportedFrom.put(event.getPlayer().getName(), gate.getLocation());
 				if (plugin.getPluginManager().getEconomy() != null && gate.getToll() > 0) {
 					if (plugin.getPluginManager().getEconomy().has(event.getPlayer().getName(), gate.getToll())) {
 						plugin.getPluginManager().getEconomy().withdrawPlayer(event.getPlayer().getName(), gate.getToll());
 						event.getPlayer().sendMessage(ChatColor.AQUA + "Took " + plugin.getPluginManager().getEconomy().format(gate.getToll()) + " from your account for using this gate.");
 						gate.portToTarget(event);
 					} else {
 						if (!gate.getDenySilent()) {
 							event.getPlayer().sendMessage(ChatColor.RED + "You don't have enough money to use this gate (Requires: " + plugin.getPluginManager().getEconomy().format(gate.getToll()) + ")");
 						}						
 					}
 				} else {
 					gate.portToTarget(event);
 				}
 			} else {
 				if (!gate.getDenySilent()) {
 					event.getPlayer().sendMessage(ChatColor.RED + "You're not allowed to use this gate!");
 				}
 			}
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerTeleport(PlayerTeleportEvent event) {		
 		if (plugin.config.getBoolean("fixes.chunkRefreshOnTeleport")) {
 			Location targetLoc = event.getTo();
 			World targetWorld = targetLoc.getWorld();
 			Chunk targetChunk = targetWorld.getChunkAt(targetLoc);
 			targetWorld.refreshChunk(targetChunk.getX(), targetChunk.getZ());
 		}
 	}
 
 	@EventHandler
 	public void onPlayerDeath(PlayerDeathEvent event) {
 		playerDiedInWorld.put(event.getEntity().getName(), event.getEntity().getWorld().getName());
 		
 		DataWorld world = plugin.getWorlds().get(event.getEntity().getWorld());
 		
 		if (world == null) return;
 			
 		if (!world.getAnnouncePlayerDeath()) {
 			((PlayerDeathEvent)event).setDeathMessage("");
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerRespawn(PlayerRespawnEvent event) {
 		DataWorld worldDied = plugin.getWorlds().get(playerDiedInWorld.get(event.getPlayer().getName()));
 		
 		switch (worldDied.getRespawnLocation()) {
 		case WORLDSPAWN:
 			event.setRespawnLocation(worldDied.getWorld().getSpawnLocation());
 			break;
 		case BEDSPAWN:
 			if (event.getPlayer().getBedSpawnLocation() != null) {
 				event.setRespawnLocation(event.getPlayer().getBedSpawnLocation());
 			} else {
 				event.setRespawnLocation(worldDied.getWorld().getSpawnLocation());
 			}
 			break;
 		case WORLD:
 			String respawnWorldName = worldDied.getRespawnWorldName();
 			DataWorld respawnWorld;
 			
 			if (respawnWorldName != null && plugin.getWorlds().get(respawnWorldName) != null) {
 				respawnWorld = plugin.getWorlds().get(respawnWorldName);
 			} else {
 				respawnWorld = worldDied;
 			}
 
 			event.setRespawnLocation(respawnWorld.getWorld().getSpawnLocation());
 			break;
 		}
 	}
 	
 }
