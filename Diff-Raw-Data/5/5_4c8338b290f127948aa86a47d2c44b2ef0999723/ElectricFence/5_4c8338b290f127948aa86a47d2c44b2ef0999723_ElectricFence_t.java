 package org.darksoft.electricfence;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Animals;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Monster;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class ElectricFence extends JavaPlugin{
 
 	public static boolean isUsingLightning;
 	public static boolean isMessaging;
 	public static int damage;
 	public static int radiusDamage;
 	private static boolean radiusDamageEnabled;
 	public static boolean earthBlockEnabled;
 	public static int earthBlock;
 	public static YamlConfiguration config;
 	public static boolean isShockingMobs;
 	public static boolean isElectricWood;
 	public static boolean isElectricIron;
 	public static boolean isShockingPlayers;
 	private final ElectricFenceListener blockDamageListener = new ElectricFenceListener();
 
 	public void onEnable(){
 		message("ElectricFence v." + getDescription().getVersion() + "has been enabled!");
 
 		File newDir = new File("plugins/ElectricFence");
 		File configFile = new File("plugins/ElectricFence", "config.yml");
 
 		config = YamlConfiguration.loadConfiguration(new File("plugins/ElectricFence", "config.yml"));
 
 		if (!newDir.exists()) {
 			newDir.mkdirs();
 		}
 
 		if (!configFile.exists()) {
			config.set("damage", 0);
 			config.set("radiusDamageEnabled", true);
 			config.set("radiusDamage", 0);
 			config.set("earthBlockEnabled", true);
 			config.getInt("earthBlock", 0);
 			config.set("Shock.Mobs", true);
 			config.set("Shock.Players", true);
 			config.set("FenceTypes.Wood", true);
 			config.set("FenceTypes.Iron", true);
 			config.set("IsSendingMessages", true);
 			config.set("useLightningEffect", true);
 
 			try{
 				config.save(new File("plugins/ElectricFence", "config.yml"));
 			} catch (IOException e1) {
 				e1.printStackTrace();
 			}
 		}
 
 		damage = config.getInt("damage");
 		radiusDamageEnabled = config.getBoolean("radiusDamageEnabled");
 		radiusDamage = config.getInt("radiusDamage");
 		earthBlockEnabled = config.getBoolean("earthBlockEnabled");
 		earthBlock = config.getInt("earthBlock");
 		isShockingMobs = config.getBoolean("Shock.Mobs");
 		isShockingPlayers = config.getBoolean("Shock.Players");
 		isElectricWood = config.getBoolean("FenceTypes.Wood");
 		isElectricWood = config.getBoolean("FenceTypes.Iron");
 		isMessaging = config.getBoolean("isSendingMessages");
 		isUsingLightning = config.getBoolean("useLightningEffect");
 
 		message("Configuration file loaded");
 		
 		try {
 			Metrics metrics = new Metrics(this);
 			metrics.start();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		if (radiusDamageEnabled) {
 			getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){
 				public void run() {
 					for (World world : ElectricFence.this.getServer().getWorlds())
 						for (Entity entity : world.getEntities())
 							if ((((entity instanceof Player)) && (ElectricFence.isShockingPlayers)) || ((((entity instanceof Animals)) || ((entity instanceof Monster))) && (ElectricFence.isShockingMobs)))
 								if (ElectricFence.isElectricFence(entity.getLocation().getBlock().getRelative(BlockFace.NORTH_WEST))) {
 									if (!ElectricFenceListener.isBlockIndirectlyPowered(entity.getLocation().getBlock().getRelative(BlockFace.NORTH_WEST)))
 										continue;
 									ElectricFence.this.radiusStrike(entity);
 								}
 								else if (ElectricFence.isElectricFence(entity.getLocation().getBlock().getRelative(BlockFace.NORTH))) {
 									if (!ElectricFenceListener.isBlockIndirectlyPowered(entity.getLocation().getBlock().getRelative(BlockFace.NORTH)))
 										continue;
 									ElectricFence.this.radiusStrike(entity);
 								}
 								else if (ElectricFence.isElectricFence(entity.getLocation().getBlock().getRelative(BlockFace.NORTH_EAST))) {
 									if (!ElectricFenceListener.isBlockIndirectlyPowered(entity.getLocation().getBlock().getRelative(BlockFace.NORTH_EAST)))
 										continue;
 									ElectricFence.this.radiusStrike(entity);
 								}
 								else if (ElectricFence.isElectricFence(entity.getLocation().getBlock().getRelative(BlockFace.EAST))) {
 									if (!ElectricFenceListener.isBlockIndirectlyPowered(entity.getLocation().getBlock().getRelative(BlockFace.EAST)))
 										continue;
 									ElectricFence.this.radiusStrike(entity);
 								}
 								else if (ElectricFence.isElectricFence(entity.getLocation().getBlock().getRelative(BlockFace.SOUTH_EAST))) {
 									if (!ElectricFenceListener.isBlockIndirectlyPowered(entity.getLocation().getBlock().getRelative(BlockFace.SOUTH_EAST)))
 										continue;
 									ElectricFence.this.radiusStrike(entity);
 								}
 								else if (ElectricFence.isElectricFence(entity.getLocation().getBlock().getRelative(BlockFace.SOUTH))) {
 									if (!ElectricFenceListener.isBlockIndirectlyPowered(entity.getLocation().getBlock().getRelative(BlockFace.SOUTH)))
 										continue;
 									ElectricFence.this.radiusStrike(entity);
 								}
 								else if (ElectricFence.isElectricFence(entity.getLocation().getBlock().getRelative(BlockFace.SOUTH_WEST))) {
 									if (!ElectricFenceListener.isBlockIndirectlyPowered(entity.getLocation().getBlock().getRelative(BlockFace.SOUTH_WEST)))
 										continue;
 									ElectricFence.this.radiusStrike(entity);
 								}
 								else {
 									if (!ElectricFence.isElectricFence(entity.getLocation().getBlock().getRelative(BlockFace.WEST)))
 										continue;
 									if (!ElectricFenceListener.isBlockIndirectlyPowered(entity.getLocation().getBlock().getRelative(BlockFace.WEST)))
 										continue;
 									ElectricFence.this.radiusStrike(entity);
 								}
 				}
 			}
 			, 20L, 20L);
 		}
 
 		getServer().getPluginManager().registerEvents(this.blockDamageListener, this);
 	}
 
 	public void radiusStrike(Entity entity) {
 		Location location = entity.getLocation();
 
 		if (canBeStruck(entity)) {
 			if ((entity instanceof Player)) {
 				if(isMessaging)
 					((Player)entity).sendMessage(ChatColor.YELLOW + "You got too close to an electric fence!");
 			}
			if(isUsingLightning)
 				entity.getWorld().strikeLightningEffect(location);
 			((LivingEntity)entity).damage(radiusDamage);
 		}
 	}
 
 	public void onDisable() {
 		message("Plugin shutting down!");
 	}
 
 	public static void message(String msg) {
 		System.out.println("[ElectricFence]: " + msg);
 	}
 
 	public static boolean canBeStruck(Entity entity) {
 		if ((entity instanceof Player))
 			return (!((Player)entity).isOp()) || (((Player)entity).hasPermission("ElectricFence.bypass"));
 		return true;
 	}
 
 	public static boolean hasPerm(String perm, Player player) {
 		return player.hasPermission("ElectricFence." + perm);
 	}
 
 	public static boolean isElectricFence(Block b) {
 		if (config.getBoolean("FenceTypes.Wood")) {
 			if (b.getTypeId() == 85)
 				return true;
 		} else if (!config.getBoolean("FenceTypes.Iron"));
 		return b.getTypeId() == 101;
 	}
 }
