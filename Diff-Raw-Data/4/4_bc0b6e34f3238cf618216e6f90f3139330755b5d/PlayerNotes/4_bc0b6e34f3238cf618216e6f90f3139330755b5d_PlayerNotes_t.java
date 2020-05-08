 package com.hamaluik.PlayerNotes;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.logging.Logger;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.config.Configuration;
 
 import com.hamaluik.PlayerNotes.commands.*;
 
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 public class PlayerNotes extends JavaPlugin {
 	// the basics
 	Logger log = Logger.getLogger("Minecraft");
 	public PermissionHandler permissionHandler;
 	
 	// the database manager..
 	public DBManager dbm = new DBManager(this);
 	
 	// data..
 	public HashMap<String, Stat> playerStats = new HashMap<String, Stat>();
 	
 	// the commands..
 	public HashMap<String, Command> commands = new HashMap<String, Command>();
 	
 	// the listeners
 	PlayerNotesPlayerListener playerListener = new PlayerNotesPlayerListener(this);
 	PlayerNotesEntityListener entityListener = new PlayerNotesEntityListener(this);
 	PlayerNotesBlockListener blockListener = new PlayerNotesBlockListener(this);
 	PlayerNotesCommandExecutor commandExecutor = new PlayerNotesCommandExecutor(this);
 	
 	// the scheduled task
 	SaveToDB statsDump = new SaveToDB(this);
 	
 	// options
 	boolean useMYSQL = true;
 	String databaseName;
 	String mysqlUser;
 	String mysqlPass;
 	private long statsDumpInterval;
 	
 	// startup routine..
 	public void onEnable() {		
 		// set up the plugin..
 		this.setupPermissions();
 		this.loadConfiguration();
 		
 		// ensure the database table exists..
 		dbm.ensureTablesExist();
 		
 		// import the plugin manager
 		PluginManager pm = this.getServer().getPluginManager();
 		
 		// register the events
 		pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Event.Priority.Monitor, this);
 		pm.registerEvent(Event.Type.PLAYER_KICK, playerListener, Event.Priority.Monitor, this);
 		pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Event.Priority.Monitor, this);
 		pm.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, Event.Priority.Monitor, this);
 		pm.registerEvent(Event.Type.ENTITY_DEATH, entityListener, Event.Priority.Monitor, this);
 		pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Event.Priority.Monitor, this);
 		pm.registerEvent(Event.Type.BLOCK_PLACE, blockListener, Event.Priority.Monitor, this);
 		
 		// register commands
 		registerCommand(new CommandNote(this));
 		registerCommand(new CommandNoteDelete(this));
 		registerCommand(new CommandNotes(this));
 		registerCommand(new CommandStats(this));
 		
 		// load the "join times" for any players currently on the server
 		// (in case of reload)
 		Player[] players = this.getServer().getOnlinePlayers();
 		for(int i = 0; i < players.length; i++) {
 			getPlayerStats(players[i].getName(), true).joinTime = System.currentTimeMillis() / 1000;
 			getPlayerStats(players[i].getName(), true).changed = true;
 		}
 		
 		// schedule the database saving
 		this.getServer().getScheduler().scheduleAsyncRepeatingTask(this, statsDump, statsDumpInterval * 20, statsDumpInterval * 20);
 		log.info("[PlayerNotes] dumping to database every " + statsDumpInterval + " seconds");
 		
 		log.info("[PlayerNotes] plugin enabled");
 	}
 
 	// shutdown routine
 	public void onDisable() {
 		// save the player stats
 		log.info("[PlayerNotes] saving stats to database...");
 		for(String player: playerStats.keySet()) {
 			setPlayerDBStat(player, playerStats.get(player));
 		}
 		playerStats.clear();
 		log.info("[PlayerNotes] plugin disabled");
 	}
 	
 	// register a command
 	private void registerCommand(Command command) {
 		// add the command to the commands list and register it with Bukkit
 		this.commands.put(command.getCommand(), command);
 		this.getCommand(command.getCommand()).setExecutor(this.commandExecutor);
 	}
 	
 	// keep internal track of player stats..
 	public Stat getPlayerStats(String player, boolean create) {
 		// if player is already being tracked internally, grab the internal one
 		if(playerStats.containsKey(player)) return playerStats.get(player);
 
 		// if they're not already being kept track of, query their data
 		Stat stat = dbm.getStat(player);
 		if(create) playerStats.put(player, stat);
 		return stat;
 	}
 	
 	public void setPlayerStat(String player, Stat newStat) {
 		// update the internal record
 		playerStats.put(player, newStat);
 	}
 	
 	public void setPlayerDBStat(String player, Stat newStat) {
 		// update the database
 		dbm.setStat(player, newStat);
 	}
 	
 	public void unloadPlayerStat(String player) {
 		// make sure the player IS loaded
 		if(!playerStats.containsKey(player)) return;
 		// update the database..
 		dbm.setStat(player, playerStats.get(player));
 		// and remove the player from internal tracking!
 		playerStats.remove(player);
 	}
 	
 	// load the permissions plugin..
 	private void setupPermissions() {
 		Plugin permissionsPlugin = this.getServer().getPluginManager().getPlugin("Permissions");
 		
 		if(this.permissionHandler == null) {
 			if(permissionsPlugin != null) {
 				this.permissionHandler = ((Permissions)permissionsPlugin).getHandler();
 				log.info("[PlayerNotes] permissions successfully loaded");
 			} else {
 				log.info("[PlayerNotes] permission system not detected, defaulting to OP");
 			}
 		}
 	}
 	
 	// just an interface function for checking permissions
 	// if permissions are down, default to OP status.
 	public boolean hasPermission(Player player, String permission) {
 		if(permissionHandler == null) {
 			return player.isOp();
 		}
 		else {
 			return (permissionHandler.has(player, permission));
 		}
 	}
 	
 	private void checkConfiguration() {
 		// first, check to see if the file exists
 		File configFile = new File(getDataFolder() + "/config.yml");
 		if(!configFile.exists()) {
 			// file doesn't exist yet :/
 			log.info("[PlayerNotes] config file not found, will attempt to create a default!");
 			new File(getDataFolder().toString()).mkdir();
 			try {
 				// create the file
 				configFile.createNewFile();
 				// and attempt to write the defaults to it
 				FileWriter out = new FileWriter(getDataFolder() + "/config.yml");
 				out.write("---\n");
 				out.write("# database can be either:\n");
 				out.write("# 'mysql' or 'sqlite'\n");
 				out.write("database: sqlite\n\n");
 				out.write("# if using sqlite, this should be: plugins/PlayerNotes/PlayerNotes.db\n");
 				out.write("# if using mysql, this is the mysql database you wish to use\n");
 				out.write("database-name: plugins/PlayerNotes/PlayerNotes.db\n\n");
 				out.write("# only needed if using mysql\n");
 				out.write("mysql-user: ''\n");
 				out.write("mysql-pass: ''\n\n");
				out.write("# how often (in minutes) to force-save stats to the DB\n");
				out.write("stats-dump-interval: 30\n");
 				out.close();
 			} catch(IOException ex) {
 				// something went wrong :/
 				log.info("[PlayerNotes] error: config file does not exist and could not be created");
 			}
 		}
 	}
 
 	public void loadConfiguration() {
 		// make sure the config exists
 		// and if it doesn't, make it!
 		this.checkConfiguration();
 		
 		// get the configuration..
 		Configuration config = getConfiguration();
 		String database = config.getString("database");
 		if(database.equalsIgnoreCase("mysql")) useMYSQL = true;
 		else useMYSQL = false;
 		databaseName = config.getString("database-name");
 		mysqlUser = config.getString("mysql-user");
 		mysqlPass = config.getString("mysql-pass");
 		statsDumpInterval = config.getInt("stats-dump-interval", 5) * 60;
 	}
 	
 	// allow for colour tags to be used in strings..
 	public String processColours(String str) {
 		return str.replaceAll("(&([a-f0-9]))", "\u00A7$2");
 	}
 	
 	// strip colour tags from strings..
 	public String stripColours(String str) {
 		return str.replaceAll("(&([a-f0-9]))", "");
 	}
 }
