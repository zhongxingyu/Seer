 package org.desmin88.simplesave;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.util.Properties;
 import java.util.logging.Filter;
 import java.util.logging.LogRecord;
 import java.util.logging.Logger;
 
 import net.minecraft.server.WorldServer;
 
 import org.bukkit.ChatColor;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class SimpleSave extends JavaPlugin {
 	Logger log;
 	long saveinterval;
 	long backupinterval;
 	String[] ConfigArray = new String[21];
 	FileOutputStream out;
 	FileUtils fu = new FileUtils();
 	Backup bu = new Backup(this);
 
 	@Override
 	public void onDisable() {
 		getServer().getScheduler().cancelTasks(this);
 		log.info("SimpleSave: Powered down");
 	}
 
 	@Override
 	public void onEnable() {
 		SSplayerListener playerListener = new SSplayerListener(this,
 				getServer().getOnlinePlayers().length);
 		log = Logger.getLogger("Minecraft");
 		PluginDescriptionFile pdfFile = getDescription();
 		String version = pdfFile.getVersion();
 		Logger.getLogger("Minecraft").setFilter(new CCSFilter());
 		try {
 			MakeConfig();
 			ConfigArray = ReadConfig();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		for (Object o : ConfigArray) {
 			if (o == null) {
 				log.severe("SimpleSave: Config file is malformed, regenerating with defaults.");
 				ConfigArray = FixConfig();
 			}
 		}
 		if (!ConfigArray[12].equals(version)) {
 			log.warning("SimpleSave: Running version below " + version
 					+ ", the config file is outdated. Regenerating");
 			ConfigArray = FixConfig();
 
 		}
 		log.info("SimpleSave: 3.07 Initialized");
 		log.warning("SimpleSave: 3.07 is a beta version.");
 		if (ConfigArray[17].equals("true")) {
 			setY(true);
 		}
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener,
 				Priority.Monitor, this);
 		pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener,
 				Priority.Monitor, this);
 		pm.registerEvent(Event.Type.PLAYER_KICK, playerListener,
 				Priority.Monitor, this);
 
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command,
 			String commandLabel, String[] args) {
 		String[] split = args;
 		String commandName = command.getName().toLowerCase();
 		if (sender instanceof Player) {
 			Player player = (Player) sender;
 			if (commandName.equalsIgnoreCase("ssBackup")) {
 				if (split.length == 0) {
 					if (player.isOp()) {
 						getServer().getScheduler().scheduleAsyncDelayedTask(
 								this, new BackupMethod());
 					}
 				}
 			}
 		}
 		return true;
 	}
 
 	public class SaveMethod implements Runnable {
 		@Override
 		public void run() {
 			if (ConfigArray[4].equals("true")) {
 				log.info(ConfigArray[2]);
 				getServer().broadcastMessage(
 						ChatColor.valueOf(ConfigArray[5]) + ConfigArray[2]);
 			}
 			saveWorlds();
 			if (ConfigArray[4].equals("true")) {
 				log.info(ConfigArray[3]);
 				getServer().broadcastMessage(
 						ChatColor.valueOf(ConfigArray[5]) + ConfigArray[3]);
 			}
 		}
 	}
 
 	public class BackupMethod implements Runnable {
 		@Override
 		public void run() {
 			if (ConfigArray[10].equals("true")) {
 				log.info(ConfigArray[8]);
 				getServer().broadcastMessage(
 						ChatColor.valueOf(ConfigArray[11]) + ConfigArray[8]);
 			}
 			bu.backup();
 			if (ConfigArray[10].equals("true")) {
 				log.info(ConfigArray[9]);
 				getServer().broadcastMessage(
 						ChatColor.valueOf(ConfigArray[11]) + ConfigArray[9]);
 			}
 
 		}
 
 	}
 
 	public int ScheduleSave(long interval) {
 		int taskID = getServer().getScheduler().scheduleSyncRepeatingTask(this,
 				new SaveMethod(), interval, interval);
 		return taskID;
 	}
 
 	public int ScheduleBackup(long interval) {
 		int taskID = getServer().getScheduler().scheduleAsyncRepeatingTask(
 				this, new BackupMethod(), interval, interval);
 		return taskID;
 	}
 
 	// Fixes the new setting for worldserver boolean
 	public void setY(Boolean b) {
 		for (Field f : WorldServer.class.getFields()) {
 			if (f.getType().getName().equals("boolean")
 					&& !f.getName().equals("weirdIsOpCache")) {
 				try {
 					f.setBoolean(WorldServer.class, b);
 				} catch (IllegalArgumentException e) {
 					e.printStackTrace();
 				} catch (IllegalAccessException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 
 	}
 
 	public void saveWorlds() {
 		for (World world : getServer().getWorlds()) {
 			world.save();
 		}
 		getServer().savePlayers();
 	}
 
 	public class CCSFilter implements Filter {
 		String LS1 = "Disabling level saving..";
 		String LS2 = "ConsoleCommandSender: Disabling level saving..";
 		String LS3 = "Enabling level saving..";
 		String LS4 = "ConsoleCommandSender: Enabling level saving..";
 
 		@Override
 		public boolean isLoggable(LogRecord record) {
 			return !record.getMessage().equals(LS1)
 					& !record.getMessage().equals(LS2)
 					& !record.getMessage().equals(LS3)
 					& !record.getMessage().equals(LS4);
 
 		}
 
 	}
 
 	public void MakeConfig() throws IOException {
 		File Dir = new File("plugins/SimpleSave");
 		File configfile = new File("plugins/SimpleSave/config.properties");
 		if (Dir.exists() && configfile.exists()) {
 		} else {
 			Dir.mkdir();
 			configfile.createNewFile();
 			Properties simplesaveproperties = new Properties();
 			simplesaveproperties.load(new FileInputStream(
 					"plugins/SimpleSave/config.properties"));
 			simplesaveproperties.setProperty("save.use", "true");
 			simplesaveproperties.setProperty("save.interval", "10");
 			simplesaveproperties.setProperty("save.message.starting",
 					"[SimpleSave] Beginning world save");
 			simplesaveproperties.setProperty("save.message.ending",
 					"[SimpleSave] Ending world save");
 			simplesaveproperties.setProperty("save.message.send", "true");
 			simplesaveproperties.setProperty("save.message.color", "RED");
 			simplesaveproperties.setProperty("backup.use", "true");
 			simplesaveproperties.setProperty("backup.interval", "60");
 			simplesaveproperties.setProperty("backup.message.starting",
 					"[SimpleSave] Beginning world backup");
 			simplesaveproperties.setProperty("backup.message.ending",
 					"[SimpleSave] Ending world backup");
 			simplesaveproperties.setProperty("backup.message.send", "true");
 			simplesaveproperties.setProperty("backup.message.color", "RED");
 			simplesaveproperties.setProperty("backup.history.length", "5");
 			simplesaveproperties.setProperty("backup.directory", "backup");
 			simplesaveproperties.setProperty("plugin.send.saveoff-on", "true");
 			simplesaveproperties.setProperty("plugin.version", "3.03");
 			simplesaveproperties.setProperty("backup.date.format",
 					"yyyy-MM-dd hh-mm-ss");
 			simplesaveproperties.setProperty("backup.world.filter", "");
 			simplesaveproperties
 					.setProperty("plugin.ignore.noplayers", "false");
 			simplesaveproperties.store(out = new FileOutputStream(
 					"plugins/SimpleSave/config.properties"),
 					"SimpleSave Config File ");
 			out.close();
 		}
 	}
 
 	public String[] ReadConfig() throws IOException {
 		Properties simplesaveproperties = new Properties();
 		FileInputStream in = new FileInputStream(
 				"plugins/SimpleSave/config.properties");
 		simplesaveproperties.load(in);
 		ConfigArray[0] = simplesaveproperties.getProperty("save.use");
 		ConfigArray[1] = simplesaveproperties.getProperty("save.interval");
 		ConfigArray[2] = simplesaveproperties
 				.getProperty("save.message.starting");
 		ConfigArray[3] = simplesaveproperties
 				.getProperty("save.message.ending");
 		ConfigArray[4] = simplesaveproperties.getProperty("save.message.send");
 		ConfigArray[5] = simplesaveproperties.getProperty("save.message.color");
 		ConfigArray[6] = simplesaveproperties.getProperty("backup.use");
 		ConfigArray[7] = simplesaveproperties.getProperty("backup.interval");
 		ConfigArray[8] = simplesaveproperties
 				.getProperty("backup.message.starting");
 		ConfigArray[9] = simplesaveproperties
 				.getProperty("backup.message.ending");
 		ConfigArray[10] = simplesaveproperties
 				.getProperty("backup.message.send");
 		ConfigArray[11] = simplesaveproperties
 				.getProperty("backup.message.color");
 		ConfigArray[12] = simplesaveproperties.getProperty("plugin.version");
 		ConfigArray[13] = simplesaveproperties
 				.getProperty("backup.history.length");
 		ConfigArray[14] = "";
 		ConfigArray[15] = "";
 		ConfigArray[16] = simplesaveproperties.getProperty("backup.directory");
 		ConfigArray[17] = simplesaveproperties
 				.getProperty("plugin.send.saveoff-on");
 		ConfigArray[18] = simplesaveproperties
 				.getProperty("backup.date.format");
 		ConfigArray[19] = simplesaveproperties
 				.getProperty("backup.world.filter");
 		ConfigArray[20] = simplesaveproperties
 				.getProperty("plugin.ignore.noplayers");
 		in.close();
 		return ConfigArray;
 	}
 
 	public String[] FixConfig() {
 
 		File configfile = new File("plugins/SimpleSave/config.properties");
 		boolean b = configfile.delete();
 		if (b == false) {
 			log.severe("SimpleSave: Error deleting config file. Open in another program?");
 		}
 		try {
 			MakeConfig();
 			ConfigArray = ReadConfig();
 		} catch (IOException e) {
 			log.severe("SimpleSave: Can't make config file!");
 		}
 		return ConfigArray;
 	}
 }
