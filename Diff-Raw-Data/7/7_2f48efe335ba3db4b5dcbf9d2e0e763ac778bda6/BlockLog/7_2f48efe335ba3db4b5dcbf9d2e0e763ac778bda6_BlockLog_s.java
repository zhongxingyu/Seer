 package me.arno.blocklog;
 
 import java.io.File;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.logging.Logger;
 
 import me.arno.blocklog.Metrics.Graph;
 import me.arno.blocklog.commands.*;
 import me.arno.blocklog.listeners.*;
 import me.arno.blocklog.logs.LogType;
 import me.arno.blocklog.managers.*;
 import me.arno.blocklog.pail.PailInterface;
 import me.arno.blocklog.schedules.Save;
 import me.arno.blocklog.schedules.Updates;
 import me.arno.blocklog.util.Query;
 import me.arno.blocklog.util.Text;
 import me.escapeNT.pail.Pail;
 
 import org.bukkit.ChatColor;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class BlockLog extends JavaPlugin {
 	public static BlockLog plugin;
 	public Logger log;
 	public Connection conn;
 	
 	private SettingsManager settingsManager;
 	private DatabaseManager databaseManager;
 	private QueueManager queueManager;
 	private DependencyManager dependencyManager;
 	
 	public ArrayList<String> users = new ArrayList<String>();
 	public HashMap<String, ItemStack> playerItemStack = new HashMap<String, ItemStack>();
 	public HashMap<String, Integer> playerItemSlot = new HashMap<String, Integer>();
 	
 	private HashMap<Integer, Integer> schedules = new HashMap<Integer, Integer>();
 	
 	public String newVersion;
 	public String currentVersion;
 	public double doubleNewVersion;
 	public double doubleCurrentVersion;
 	
 	public int autoSave = 0;
 	public boolean saving = false;
 	
 	public HashMap<Integer, Integer> getSchedules() {
 		return schedules;
 	}
 	
 	public SettingsManager getSettingsManager() {
 		return settingsManager;
 	}
 	
 	public DatabaseManager getDatabaseManager() {
 		return databaseManager;
 	}
 	
 	public QueueManager getQueueManager() {
 		return queueManager;
 	}
 	
 	public DependencyManager getDependencyManager() {
 		return dependencyManager;
 	}
 	
 	private void loadConfiguration() {
 		Config worldConfig;
 		for(World world : getServer().getWorlds()) {
 			worldConfig = new Config("worlds" + File.separator + world.getName() + ".yml");
 			
 			for(LogType type : LogType.values()) {
 				if(type != LogType.CREEPER && type != LogType.FIREBALL && type != LogType.TNT) {
 					worldConfig.getConfig().addDefault(type.name(), true);
 				}
 			}
 			worldConfig.getConfig().options().copyDefaults(true);
 			worldConfig.saveConfig();
 		}
 		
 		getConfig().addDefault("mysql.host", "localhost");
 	    getConfig().addDefault("mysql.username", "root");
 	    getConfig().addDefault("mysql.password", "");
 	    getConfig().addDefault("mysql.database", "bukkit");
 	    getConfig().addDefault("mysql.port", 3306);
 	   	getConfig().addDefault("blocklog.wand", 19);
 	   	getConfig().addDefault("blocklog.results", 5);
 	   	getConfig().addDefault("blocklog.save-delay", 1);
 	    getConfig().addDefault("blocklog.reports", true);
 	    getConfig().addDefault("blocklog.updates", true);
 	    getConfig().addDefault("blocklog.metrics", true);
 	    getConfig().addDefault("blocklog.dateformat", "%d-%m %H:%i");
 	    getConfig().addDefault("warning.blocks", 500);
 	    getConfig().addDefault("warning.repeat", 100);
 	    getConfig().addDefault("warning.delay", 30);
 	    getConfig().addDefault("auto-save.enabled", true);
 	    getConfig().addDefault("auto-save.blocks", 1000);
 	    getConfig().addDefault("auto-save.world-save", false);
 	    getConfig().addDefault("purge.log", true);
 	    getConfig().addDefault("purge.blocks.enabled", false);
 	    getConfig().addDefault("purge.blocks.days", 14);
 	    getConfig().addDefault("purge.interactions.enabled", false);
 	    getConfig().addDefault("purge.interactions.days", 14);
 	    getConfig().addDefault("purge.chat.enabled", false);
 	    getConfig().addDefault("purge.chat.days", 14);
 	    getConfig().addDefault("purge.deaths.enabled", false);
 	    getConfig().addDefault("purge.deaths.days", 14);
 	    getConfig().addDefault("purge.kills.enabled", false);
 	    getConfig().addDefault("purge.kills.days", 14);
 	    getConfig().options().copyDefaults(true);
 	    saveConfig();
 		
 		if(getConfig().getBoolean("auto-save.enabled")) {
 			autoSave = getConfig().getInt("auto-save.blocks");
 		}
 	}
 	
 	private void purgeDatabase() {
 		try {
 			getDatabaseManager().purge(DatabaseManager.purgeableTables);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private void loadDatabase() {
 		try {
 	    	conn = getDatabaseManager().getConnection();
 	    	Statement stmt = conn.createStatement();
 	    	
 	    	for(String table : DatabaseManager.databaseTables) {
 	    		stmt.executeUpdate(Text.getResourceContent("database/" + DatabaseManager.databasePrefix + table + ".sql"));
 	    	}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private void updateDatabase() {
 		try {
 			Config versions = new Config("VERSIONS");
 			versions.getConfig().addDefault("database", 3);
 			versions.getConfig().options().copyDefaults(true);
 			versions.saveConfig();
 
 			Statement stmt = conn.createStatement();
 			if(versions.getConfig().getInt("database") == 10) {
 				stmt.executeUpdate("ALTER TABLE `blocklog_chat` CHANGE `message` `message` TEXT NOT NULL");
 				stmt.executeUpdate("ALTER TABLE `blocklog_blocks` CHANGE `trigered` `triggered` varchar(75) NOT NULL");
 				versions.getConfig().set("database", 1);
			} else if(versions.getConfig().getInt("database") == 1) {
 				stmt.executeUpdate("UPDATE `blocklog_blocks` SET `entity`='player' WHERE `triggered`!='environment' AND `entity`!='creeper'");
 				stmt.executeUpdate("UPDATE `blocklog_blocks` SET `entity`='unkown' WHERE `triggered`='environment' AND `entity`!='creeper' AND `entity`!='fireball' AND `entity`!='primed_tnt'");
 				versions.getConfig().set("database", 2);
			} else if(versions.getConfig().getInt("database") == 2) {
 				stmt.executeUpdate("ALTER TABLE `blocklog_commands` CHANGE `command` `command` varchar(255) NOT NULL");
 				versions.getConfig().set("database", 3);
 			}
 			versions.saveConfig();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void loadMetrics() {
 		try {
 		    Metrics metrics = new Metrics(this);
 		    
 		    // Storage Graph
 		    Graph graph = metrics.createGraph("Storage");
 		    graph.addPlotter(new Metrics.Plotter("Block Edits") {
 		    	@Override
 		        public int getValue() {
 		        	try {
 						return new Query().from(DatabaseManager.databasePrefix + "blocks").getRowCount();
 					} catch (SQLException e) { e.printStackTrace(); }
 		        	return 0;
 		        }
 
 		    });
 		    graph.addPlotter(new Metrics.Plotter("Block Interactions") {
 			    @Override
 			    public int getValue() {
 			    	try {
 						return new Query().from(DatabaseManager.databasePrefix + "interactions").getRowCount();
 			    	} catch (SQLException e) { e.printStackTrace(); }
 			    	return 0;
 			    }
 		    });
 		    graph.addPlotter(new Metrics.Plotter("Chat Messages") {
 			    @Override
 			    public int getValue() {
 			    	try {
 						return new Query().from(DatabaseManager.databasePrefix + "chat").getRowCount();
 			    	} catch (SQLException e) { e.printStackTrace(); }
 			    	return 0;
 			    }
 		    });
 		    graph.addPlotter(new Metrics.Plotter("Executed Commands") {
 			    @Override
 			    public int getValue() {
 			    	try {
 						return new Query().from(DatabaseManager.databasePrefix + "commands").getRowCount();
 			    	} catch (SQLException e) { e.printStackTrace(); }
 			    	return 0;
 			    }
 		    });
 		    graph.addPlotter(new Metrics.Plotter("Player Kills") {
 			    @Override
 			    public int getValue() {
 			    	try {
 						return new Query().from(DatabaseManager.databasePrefix + "kills").getRowCount();
 			    	} catch (SQLException e) { e.printStackTrace(); }
 			    	return 0;
 			    }
 		    });
 		    graph.addPlotter(new Metrics.Plotter("Entity Deaths") {
 			    @Override
 			    public int getValue() {
 			    	try {
 						return new Query().from(DatabaseManager.databasePrefix + "deaths").getRowCount();
 			    	} catch (SQLException e) { e.printStackTrace(); }
 			    	return 0;
 			    }
 		    });
 		    
 		    metrics.start();
 		} catch (IOException e) {
 			log.warning("Unable to submit the statistics");
 		}
 	}
 	
 	public boolean reloadPlugin() {
 		if(saving)
 			return false;
 		
 		getServer().getScheduler().cancelTasks(this);
 		
 		log.info("Reloading the configurations");
 		loadConfiguration();
 		    
 		log.info("Reloading the database");
 		loadDatabase();
 		return true;
 	}
 	
 	private void loadPlugin() {
 		BlockLog.plugin = this;
 		currentVersion = getDescription().getVersion();
 		log = getLogger();
 		
 		log.info("Loading the configurations");
 	    loadConfiguration();
 		
 		log.info("Loading the managers");
 		settingsManager = new SettingsManager();
 		databaseManager = new DatabaseManager();
 		queueManager = new QueueManager();
 		dependencyManager = new DependencyManager();
 	    
 	    log.info("Loading the database");
 	    loadDatabase();
 	    updateDatabase();
 	    
 	    if(getDependencyManager().isDependencyEnabled("Pail")) {
 	    	log.info("Hooking into pail");
 	    	Pail pail = (Pail) getDependencyManager().getDependency("Pail");
 			pail.loadInterfaceComponent("BlockLog", new PailInterface());
 	    }
 	    
 	    log.info("Purging the database");
 	    purgeDatabase();
 		
 		if(getConfig().getBoolean("blocklog.metrics")) {
 			log.info("Loading metrics");
 			loadMetrics();
 		}
 	    
 		log.info("Starting BlockLog");
 		getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Save(1, null, false), 100L, getSettingsManager().getBlockSaveDelay() * 20L);
     	
     	getServer().getPluginManager().registerEvents(new WandListener(), this);
     	getServer().getPluginManager().registerEvents(new BlockListener(), this);
     	getServer().getPluginManager().registerEvents(new InteractionListener(), this);
     	getServer().getPluginManager().registerEvents(new PlayerListener(), this);
     	getServer().getPluginManager().registerEvents(new EntityListener(), this);
     	getServer().getPluginManager().registerEvents(new WorldListener(), this);
     	
     	if(getDependencyManager().isDependencyEnabled("mcMMO"))
     		getServer().getPluginManager().registerEvents(new McMMOListener(), this);
     	
     	if(getConfig().getBoolean("blocklog.updates")) {
 	    	getServer().getScheduler().scheduleSyncRepeatingTask(this, new Updates(), 1L, 1 * 60 * 60 * 20L); // Check every hour for a new version
 	    	getServer().getPluginManager().registerEvents(new NoticeListener(), this);
 	    }
     }
 	
 	public void saveLogs(final int count) {
 		saveLogs(count, getServer().getConsoleSender());
 	}
 	
 	public void saveLogs(final int count, final CommandSender sender) {
 		getServer().getScheduler().scheduleAsyncDelayedTask(this, new Save(count, sender));
 	}
 	
 	private void stopPlugin() {
 		try {
 			getServer().getScheduler().cancelTasks(this);
 			
 			log.info("Saving all the queued logs!");
 			while(!getQueueManager().getInteractionQueue().isEmpty()) {
 				getQueueManager().saveQueuedInteraction();
 	    	}
 			while(!getQueueManager().getEditQueue().isEmpty()) {
 				getQueueManager().saveQueuedEdit();
 			}
 			log.info("Successfully saved all the queued logs!");
 			
 			if(conn != null)
 				conn.close();
 		} catch(SQLException e) {
 			e.printStackTrace();
 		}
 		BlockLog.plugin = null;
 	}
 	
 	@Override
 	public void onEnable() {
 		loadPlugin();
 		PluginDescriptionFile PluginDesc = getDescription();
 		log.info("v" + PluginDesc.getVersion() + " is enabled!");
 	}
 	
 	@Override
 	public void onDisable() {
 		stopPlugin();
 		PluginDescriptionFile PluginDesc = getDescription();
 		log.info("v" + PluginDesc.getVersion() + " is disabled!");
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		Player player = null;
 		
 		if (sender instanceof Player)
 			player = (Player) sender;
 		
 		if(!cmd.getName().equalsIgnoreCase("blocklog"))
 			return false;
 		
 		if(args.length < 1) {
 			player.sendMessage(ChatColor.GOLD + "Say " + ChatColor.BLUE + "/bl help " + ChatColor.GOLD + "for a list of available commands");
 			player.sendMessage(ChatColor.GOLD + "This server is using BlockLog v" + getDescription().getVersion() + " by Anerach");
 			return true;
 		}
 		
 		ArrayList<String> argList = new ArrayList<String>();
 		
 		if(args.length > 1) {
 			for(int i=1;i<args.length;i++) {
 				argList.add(args[i]);
 			}
 		}
 		
 		String[] newArgs = argList.toArray(new String[]{});
 		
 		BlockLogCommand command = new BlockLogCommand();
 		
 		if(args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("h"))
 			command = new CommandHelp();
 		else if(args[0].equalsIgnoreCase("autosave"))
 			command = new CommandAutoSave();
 		else if(args[0].equalsIgnoreCase("cancel"))
 			command = new CommandCancel();
 		else if(args[0].equalsIgnoreCase("config") || args[0].equalsIgnoreCase("cfg"))
 			command = new CommandConfig();
 		else if(args[0].equalsIgnoreCase("lookup"))
 			command = new CommandLookup();
 		else if(args[0].equalsIgnoreCase("purge"))
 			command = new CommandPurge();
 		else if(args[0].equalsIgnoreCase("queue"))
 			command = new CommandQueue();
 		else if(args[0].equalsIgnoreCase("read"))
 			command = new CommandRead();
 		else if(args[0].equalsIgnoreCase("reload"))
 			command = new CommandReload();
 		else if(args[0].equalsIgnoreCase("report"))
 			command = new CommandReport();
 		else if(args[0].equalsIgnoreCase("rollback") || args[0].equalsIgnoreCase("rb"))
 			command = new CommandRollback();
 		else if(args[0].equalsIgnoreCase("rollbacklist") || args[0].equalsIgnoreCase("rblist") || args[0].equalsIgnoreCase("rbl"))
 			command = new CommandRollbackList();
 		else if(args[0].equalsIgnoreCase("save"))
 			command = new CommandSave();
 		else if(args[0].equalsIgnoreCase("search"))
 			command = new CommandSearch();
 		else if(args[0].equalsIgnoreCase("simrollback") || args[0].equalsIgnoreCase("simrb"))
 			command = new CommandSimulateRollback();
 		else if(args[0].equalsIgnoreCase("simundo"))
 			command = new CommandSimulateUndo();
 		else if(args[0].equalsIgnoreCase("storage"))
 			command = new CommandStorage();
 		else if(args[0].equalsIgnoreCase("undo"))
 			command = new CommandUndo();
 		else if(args[0].equalsIgnoreCase("wand"))
 			command = new CommandWand();
 		
 		cmd.setUsage(command.getCommandUsage());
 		return command.execute(player, cmd, newArgs);
 	}
 }
