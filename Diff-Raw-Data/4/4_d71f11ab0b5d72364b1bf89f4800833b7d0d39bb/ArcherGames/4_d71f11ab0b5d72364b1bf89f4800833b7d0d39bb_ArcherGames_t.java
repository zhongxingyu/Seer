 package com.araeosia.ArcherGames;
 
 import com.araeosia.ArcherGames.utils.Archer;
 import com.araeosia.ArcherGames.utils.Config;
 import com.araeosia.ArcherGames.listeners.PlayerEventListener;
 import com.araeosia.ArcherGames.listeners.EntityEventListener;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 
 import java.util.List;
 import java.util.logging.Logger;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.logging.Level;
 import net.milkbowl.vault.economy.Economy;
 
 import org.bukkit.Location;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.RegisteredServiceProvider;
 
 public class ArcherGames extends JavaPlugin {
 
 	public boolean debug = false;
 	public Config config;
 	public ScheduledTasks scheduler;
 	public static Logger log;
 	public List<String> voteSites;
	public Location startPosition;
 	public HashMap<String, ArrayList<ItemStack>> kits = new HashMap<String, ArrayList<ItemStack>>();
 	public static ArrayList<Archer> players = new ArrayList<Archer>();
 	public HashMap<String, String> strings = new HashMap<String, String>();
 	public ServerWide serverwide;
 	public HashMap<String, Boolean> configToggles = new HashMap<String, Boolean>();
 	public static Economy econ = null;
 	public Connection conn;
 
 	/**
 	 *
 	 */
 	@Override
 	public void onEnable() {
		startPosition = getServer().getWorlds().get(0).getSpawnLocation();
 		log = this.getLogger();
 		scheduler = new ScheduledTasks(this);
 		serverwide = new ServerWide(this);
 		config = new Config(this);
 		config.loadConfiguration();
 		// Events
 		this.getServer().getPluginManager().registerEvents(new EntityEventListener(this), this);
 		this.getServer().getPluginManager().registerEvents(new PlayerEventListener(this), this);
 		// Commands
 		this.getServer().getPluginManager().registerEvents(new CommandHandler(this), this);
 		this.getCommand("kit").setExecutor(new CommandHandler(this));
 		this.getCommand("listkits").setExecutor(new CommandHandler(this));
 		this.getCommand("vote").setExecutor(new CommandHandler(this));
 		this.getCommand("money").setExecutor(new CommandHandler(this));
 		this.getCommand("stats").setExecutor(new CommandHandler(this));
 		this.getCommand("archergames").setExecutor(new CommandHandler(this));
 
 		log.info("ArcherGames is enabled!");
 		if (debug) {
 			log.info("Debug mode is enabled!");
 		}
 		if (!setupEconomy()) {
 			log.info(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
 			getServer().getPluginManager().disablePlugin(this);
 			return;
 		}
 		log.info("Starting automated loop of games...");
 		scheduler.everySecondCheck();
 		dbConnect();
 		
 	}
 
 	/**
 	 *
 	 */
 	@Override
 	public void onDisable() {
 		this.getServer().getScheduler().cancelTask(scheduler.schedulerTaskID); // Kill the loop.
 		log.info("ArcherGames is disabled.");
 
 	}
 
 	private boolean setupEconomy() {
 		if (getServer().getPluginManager().getPlugin("Vault") == null) {
 			return false;
 		}
 		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
 		if (rsp == null) {
 			return false;
 		}
 		econ = rsp.getProvider();
 		return econ != null;
 	}
 
 	public void dbConnect() {
 		try {
 			if (conn.isValid(1) || conn == null || conn.isClosed()) {
 				java.util.Properties conProperties = new java.util.Properties();
 				conProperties.put("user", this.getConfig().getString("ArcherGames.mysql.username"));
 				conProperties.put("password", this.getConfig().getString("ArcherGames.mysql.password"));
 				conProperties.put("autoReconnect", "true");
 				conProperties.put("maxReconnects", "3");
 				String uri = "jdbc:mysql://" + this.getConfig().getString("Archerames.mysql.hostname") + ":" + this.getConfig().getString("ArcherGames.mysql.port") + "/" + this.getConfig().getString("ArcherGames.mysql.database");
 				conn = DriverManager.getConnection(uri, conProperties);
 			}
 		} catch (SQLException ex) {
 			log.log(Level.SEVERE, "Unable to connect to database!");
 		}
 	}
 }
