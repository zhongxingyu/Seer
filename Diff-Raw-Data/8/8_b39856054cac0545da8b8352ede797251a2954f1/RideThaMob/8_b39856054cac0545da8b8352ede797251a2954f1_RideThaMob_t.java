 package de.MiniDigger.RideThaMob;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.logging.Filter;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.EntityType;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import de.MiniDigger.RideThaMob.Entity.RideAbleEntityType;
 
 public class RideThaMob extends JavaPlugin {
 	public static String prefix;
 	public static String cprefix;
 	public static double defaultspeed;
 	public static double maxspeed;
 	public static int nyan_change_speed;
 	public static ArrayList<String> speed;
 	public static ArrayList<String> sneak;
 	public static ArrayList<String> control;
 	public static ArrayList<String> player;
 	public static ArrayList<String> fly;
 	public static ArrayList<EntityType> entity_blacklist;
 	public static RideThaMob pl;
 	public static Updater updater;
 	public static boolean update;
 	public static File file;
 	public static boolean check_update;
 	private String version = "(MC: 1.7.2)";
 
 	public void onEnable() {
 		pl = this;
 		loadConfig();
 
 		registerEvents();
 
 		registerCommands();
 
 		RideThaMob.prefix = ("[" + getDescription().getName() + "] ");
 		RideThaMob.cprefix = (ChatColor.AQUA + "[" + ChatColor.RED
 				+ getDescription().getName() + ChatColor.AQUA + "] " + ChatColor.RESET);
 
 		setupMetrics();
 
 		setupEntityBlacklist();
 
 		if (RideThaMob.check_update) {
 			setupUpdater();
 		}
 
 		RideThaMob.speed = new ArrayList<String>();
 		RideThaMob.sneak = new ArrayList<String>();
 		RideThaMob.control = new ArrayList<String>();
 		RideThaMob.player = new ArrayList<String>();
 		RideThaMob.fly = new ArrayList<String>();
 
 		if (Bukkit.getVersion().contains(version)) {
 			RideAbleEntityType.registerEntities();
 		} else {
 			Bukkit.getConsoleSender()
 					.sendMessage(
 							ChatColor.YELLOW
 									+ "[RideThaMob] WARINING:"
 									+ ChatColor.RED
 									+ "Failed to register the custom Entitys! You cant control the Entitys with the wasd keys now! Please make sure your are running the right Bukkit version("
 									+ version
 									+ ") if you want to use the wasd mode!");
 			Bukkit.getConsoleSender().sendMessage(
 					ChatColor.YELLOW + "[RideThaMob] WARINING:" + ChatColor.RED
 							+ "You are running " + Bukkit.getVersion());
 		}
 
 		Filter filter = new ConsoleFilter();
 		Bukkit.getLogger().setFilter(filter);
 		Logger.getLogger("Minecraft").setFilter(filter);
 	}
 
 	public void onDisable() {
 		RideAbleEntityType.unregisterEntities();
 	}
 
 	public void loadConfig() {
 		saveDefaultConfig();
 
 		RideThaMob.defaultspeed = getConfig().getDouble("defaultspeed");
 		RideThaMob.maxspeed = getConfig().getDouble("maxspeed");
 		nyan_change_speed = getConfig().getInt("nyan_change_speed");
 		try {
 			RideThaMob.check_update = getConfig().getBoolean(
 					"check_for_updates");
 		} catch (Exception e) {
 			RideThaMob.check_update = false;
 			getConfig().set("check_for_updates", false);
 		}
		new Lang();
 	}
 
 	private void setupUpdater() {
 		file = this.getFile();
 		updater = new Updater(pl, 53240, file, Updater.UpdateType.NO_DOWNLOAD,
 				false);
 
 		Updater.UpdateResult result = updater.getResult();
 		switch (result) {
 		case FAIL_DBO:
 			getLogger().info("Could not reach dev.bukkit.org. It is offline?");
 			update = false;
 			break;
 		case FAIL_NOVERSION:
 			getLogger().info("Failed to look for Updates: FAIL_NOVERSION");
 			update = false;
 			break;
 		case FAIL_APIKEY:
 			getLogger().info("Failed to look for Updates: FAIL_APIKEY");
 			update = false;
 			break;
 		case FAIL_BADID:
 			getLogger().info("Failed to look for Updates: FAIL_BADID");
 			update = false;
 			break;
 		case UPDATE_AVAILABLE:
 			Bukkit.getConsoleSender()
 					.sendMessage(
 							ChatColor.YELLOW
 									+ "[RideThaMob] There is an Update avalible. Type '/ridethamob update' to download it.");
 			update = true;
 			break;
 		default:
 			update = false;
 			break;
 		}
 	}
 
 	private void registerCommands() {
 		getCommand("RideThaMob").setExecutor(new Commands());
 	}
 
 	private void registerEvents() {
 		getServer().getPluginManager().registerEvents(new RideThaMobListener(),
 				pl);
 	}
 
 	private void setupEntityBlacklist() {
 		entity_blacklist = new ArrayList<EntityType>();
 		entity_blacklist.add(EntityType.DROPPED_ITEM);
 		entity_blacklist.add(EntityType.ENDER_CRYSTAL);
 		entity_blacklist.add(EntityType.ENDER_SIGNAL);
 		entity_blacklist.add(EntityType.EXPERIENCE_ORB);
 		entity_blacklist.add(EntityType.FIREBALL);
 		entity_blacklist.add(EntityType.FISHING_HOOK);
 		entity_blacklist.add(EntityType.FIREWORK);
 		entity_blacklist.add(EntityType.ITEM_FRAME);
 		entity_blacklist.add(EntityType.LIGHTNING);
 		entity_blacklist.add(EntityType.PAINTING);
 		entity_blacklist.add(EntityType.PRIMED_TNT);
 		entity_blacklist.add(EntityType.SMALL_FIREBALL);
 		entity_blacklist.add(EntityType.SPLASH_POTION);
 		entity_blacklist.add(EntityType.THROWN_EXP_BOTTLE);
 		entity_blacklist.add(EntityType.WEATHER);
 		entity_blacklist.add(EntityType.WITHER_SKULL);
 	}
 
 	private void setupMetrics() {
 		try {
 			Metrics metrics = new Metrics(pl);
 			metrics.start();
 		} catch (IOException e) {
 			getLogger().warning("Failed to load the Metrics :(");
 		}
 	}
 }
