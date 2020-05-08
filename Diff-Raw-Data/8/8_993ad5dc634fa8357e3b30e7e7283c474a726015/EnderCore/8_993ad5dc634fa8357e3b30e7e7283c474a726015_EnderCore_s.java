 package com.Zolli.EnderCore;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.World;
 import org.bukkit.World.Environment;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.Zolli.EnderCore.Commands.commandHandler;
 import com.Zolli.EnderCore.Commands.command.infoCommand;
 import com.Zolli.EnderCore.Commands.command.updateCommand;
 import com.Zolli.EnderCore.Configuration.Configuration;
 import com.Zolli.EnderCore.Economy.economyHandler;
 import com.Zolli.EnderCore.Listeners.blockListener;
 import com.Zolli.EnderCore.Listeners.entityListener;
 import com.Zolli.EnderCore.Listeners.inventoryListener;
 import com.Zolli.EnderCore.Listeners.playerListener;
 import com.Zolli.EnderCore.Listeners.serverListeners;
 import com.Zolli.EnderCore.Listeners.worldListener;
 import com.Zolli.EnderCore.Localization.localizationManager;
 import com.Zolli.EnderCore.Logger.simpleErrorReporter;
 import com.Zolli.EnderCore.Logger.simpleLogger;
 import com.Zolli.EnderCore.Logger.simpleLogger.Level;
 import com.Zolli.EnderCore.Metrics.Metrics;
 import com.Zolli.EnderCore.Permission.permissionHandler;
 import com.Zolli.EnderCore.Storage.Storage;
 import com.Zolli.EnderCore.Storage.storageActions;
 import com.Zolli.EnderCore.Updater.updateChecker;
 import com.Zolli.EnderCore.Utils.stringUtils;
 import com.Zolli.EnderCore.Utils.WebPaste.pasteService;
 import com.Zolli.EnderCore.Utils.WebPaste.pasteServiceProvider;
 import com.Zolli.EnderCore.Utils.WebPaste.pasteServiceType.PasteServiceType;
 
 public class EnderCore extends JavaPlugin {
 	
 	/**
 	 * Bukkit PluginManager instance
 	 */
 	public PluginManager pluginManager;
 	
 	/**
 	 * SimpleLogger object
 	 */
 	public simpleLogger logger;
 	
 	/**
 	 * Configuration object for main config
 	 */
 	private Configuration mainConfig;
 	
 	/**
 	 * Configuration object for flatfile storage engine
 	 */
 	private Configuration storageConfig;
 	
 	/**
 	 * PluginDescriptionFile object
 	 */
 	public PluginDescriptionFile pluginDescription;
 	
 	/**
 	 * Location of plugin data folder
 	 */
 	private File dataFolder;
 	
 	/**
 	 * Main configuration object
 	 */
 	public YamlConfiguration config;
 	
 	/**
 	 * FaltFileStorage object
 	 */
 	public YamlConfiguration ffStorage;
 	
 	/**
 	 * Storage engine class
 	 */
 	public Storage storage;
 	
 	/**
 	 * StorageActions class
 	 */
 	public storageActions dbAction;
 	
 	/**
 	 * LocalizationManager class
 	 */
 	public localizationManager local;
 	
 	/**
 	 * commandHandler class
 	 */
 	public commandHandler command;
 	
 	/**
 	 * Permission handler object
 	 */
 	public permissionHandler permission;
 	
 	/**
 	 * Economy handler object
 	 */
 	public economyHandler economy;
 	
 	/**
 	 * updateChecker object
 	 */
 	public updateChecker updater;
 	
 	/**
 	 * pluginMetrics object
 	 */
 	public Metrics metrics;
 	
 	/**
 	 * Paste service builder
 	 */
 	public pasteService paste;
 	
 	/**
 	 * simpleErrorReporter object
 	 */
 	public simpleErrorReporter reporter;
 	
 	/**
 	 * Runs when plugin initialization started
 	 */
 	public void onLoad() {
 		
 		/* Fill some variables with objects */
 		this.pluginDescription = getDescription();
 		this.pluginManager = this.getServer().getPluginManager();
 		this.dataFolder = this.getDataFolder();
 		this.logger = new simpleLogger(this.pluginDescription, this.dataFolder, "EnderCore.Log");
 		this.mainConfig = new Configuration(this, "config.yml");
 		config = mainConfig.config;
 		this.local = new localizationManager(this);
 		
 		/* Initialize paste services */
 		new pasteServiceProvider();
 		this.paste = pasteServiceProvider.getService(PasteServiceType.PASTIE, false);
 		
 		/* Initialize error reporter */
 		this.reporter = new simpleErrorReporter(this);
 		
 	}
 	
 	/**
 	 * Runs when initialization end (loaded plugin.yml file) 
 	 */
 	public void onEnable() {
 		/* Log the loaded localization */
 		String[] find = {"#LOCALNAME#", "#LOCALEAUTHOR#"};
 		String[] repl = {this.local.getLanguageRealName(), this.local.getLanguageAuthor()};
 		try {
 			this.logger.log(Level.CONFIG, stringUtils.replaceByArray(this.local.getLocalizedString("localization.load"), find, repl));
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		/* Fill configuration and get some values for future class initialization */
 		String driver = config.getString("database.type");
 		String debugMode = config.getString("debug.debugStatus");
 		
 		/* Setting debug mode, based on config value */
 		this.logger.setDebug(debugMode);
 		
 		/* Initializing some class depended on previous class initialization */
 		this.detectWords();
 		this.updater = new updateChecker(this);
 		this.permission = new permissionHandler(this);
 		this.economy = new economyHandler(this);
 		this.storage = new Storage(this, driver, this.dataFolder, this.config, this.logger);
 		this.dbAction = new storageActions(this.storage);
 		this.command = new commandHandler(this);
 		this.ffStorage = this.storage.getFlatFileStorage();
 		
 		/* Registering listeners and commands */
 		this.registerListeners();
 		this.registerCommands();
 		
 		/* Initializing Metrics */
 		this.initializeMetrics();
 		
 		/* Log the successfully initialization */
 		this.logger.log(Level.INFO, this.local.getLocalizedString("initialization.sucess"));
 	}
 	
 	/**
 	 * Runs when disabling signal sent
 	 */
 	public void onDisable() {
 		this.mainConfig.saveConfig();
 		this.logger.log(Level.INFO, this.local.getLocalizedString("initialization.disabled!"));
 	}
 	
 	/**
 	 * Initialize Metrics
 	 */
 	public void initializeMetrics() {
 		try {
 			this.metrics = new Metrics(this);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Register all listeners into EventHandler
 	 */
 	private void registerListeners() {
 		this.pluginManager.registerEvents(new blockListener(this), this);
 		this.pluginManager.registerEvents(new entityListener(this), this);
 		this.pluginManager.registerEvents(new inventoryListener(this), this);
 		this.pluginManager.registerEvents(new playerListener(this), this);
 		this.pluginManager.registerEvents(new serverListeners(this), this);
 		this.pluginManager.registerEvents(new worldListener(this), this);
 	}
 	
 	/**
 	 * Register all the commands
 	 */
 	private void registerCommands() {
 		this.getCommand("ec").setExecutor(this.command);
 		this.command.registerCommand("info", new infoCommand(this));
 		this.command.registerCommand("update", new updateCommand(this));
 	}
 	
 	/**
 	 * Initialize flatfile storage engine, if needed. Called in storageEngine
 	 * @return 
 	 */
 	public Configuration initializeFlatfile() {
 		this.storageConfig = new Configuration(this, "storage.yml");
 		return this.storageConfig;
 	}
 	
 	/**
 	 * Return the relative position of the plugin file, and the filename
 	 * @return String relative location
 	 */
 	public String getRelativePath() {
 		return this.getFile().toString();
 	}
 	
 	/**
 	 * Get all worlds and get the main world name and nether name
 	 * Save the config when main world name contains the default value
 	 */
 	public void detectWords() {
 		List<World> worlds = Bukkit.getServer().getWorlds();
 		String mainWorld = worlds.get(0).getName();
 		String endWorld = null;
 		
 		for(World w : worlds) {
 			if(w.getEnvironment().equals(Environment.THE_END)) {
 				endWorld = w.getName();
 			}
 		}
 		
 		if(this.config.getString("worlds.mainWorld").equalsIgnoreCase("defaultWorld")) {
 			this.logger.log(Level.WARNING, this.local.getLocalizedString("worldDetection.notSet"));
 			this.logger.log(Level.CONFIG, this.local.getLocalizedString("worldDetection.detectNormal").replace("#MAINWORLD#", mainWorld));
 			this.logger.log(Level.CONFIG, this.local.getLocalizedString("worldDetection.detectEnder").replace("#ENDWORLD#", endWorld));
 			
 			this.config.set("worlds.mainWorld", mainWorld);
 			this.config.set("worlds.endWorld", endWorld);
 		}
 	}
 	
 }
