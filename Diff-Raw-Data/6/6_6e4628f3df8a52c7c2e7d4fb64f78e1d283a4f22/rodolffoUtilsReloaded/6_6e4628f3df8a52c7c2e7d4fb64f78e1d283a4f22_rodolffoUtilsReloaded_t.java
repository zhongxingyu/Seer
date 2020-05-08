 package com.zolli.rodolffoutilsreloaded;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.earth2me.essentials.Essentials;
 import com.miykeal.showCaseStandalone.ShowCaseStandalone;
 import com.zolli.rodolffoutilsreloaded.listeners.blockListener;
 import com.zolli.rodolffoutilsreloaded.listeners.commandExecutor;
 import com.zolli.rodolffoutilsreloaded.listeners.entityListener;
 import com.zolli.rodolffoutilsreloaded.listeners.foundationStoneListener;
 import com.zolli.rodolffoutilsreloaded.listeners.inventoryListener;
 import com.zolli.rodolffoutilsreloaded.listeners.playerListener;
 import com.zolli.rodolffoutilsreloaded.listeners.tamedMobHelperListener;
 import com.zolli.rodolffoutilsreloaded.utils.econHandler;
 import com.zolli.rodolffoutilsreloaded.utils.permHandler;
 import com.zolli.rodolffoutilsreloaded.utils.recipes;
 
 /**
  * Main class of the plugin
  * 
  * @author Zolli
  * @version 1.2
  * @since 2012-05-09
  */
 
 public class rodolffoUtilsReloaded extends JavaPlugin {
 	
 	private PluginManager pm;
 	public econHandler econ = null;
 	public permHandler perm = null;
 	public ShowCaseStandalone scs = null;
 	public Essentials ess = null;
 	
 	private File configFile;
 	private File messagesFile;
 	private File buttonFile;
 	private File foundationStoneFile;
 	
 	public FileConfiguration config;
 	public FileConfiguration messages;
 	public FileConfiguration button;
 	public FileConfiguration foundationStone;
 	
 	public Logger log;
 	public String logPrefix;
 	public PluginDescriptionFile pdfile;
 	private CommandExecutor commandExec;
 	private recipes recipes;
 	
 	public String SelectorPlayer = null;
 	public String selectValue = null;
 	public String selectType;
 	public String selectName;
 	public Location selectLoc;
 	public List<String> freezedPlayer = new ArrayList<String>();
 	
 	public Block pistonBugBlock;
 	public int pistonBugId;
 	public byte pistonBugData;
 	
 	private AutoSaveThread autoSave;
 	private LagDetectThread lagDetect;
 	
 	/**
 	 * Fill the scs variable with one copy of the ShowCaseStandalone object
 	 * @return 
 	 */
 	
 	private ShowCaseStandalone setupScs() {	
 		ShowCaseStandalone showcase = null;
 		
 		for(Plugin p : getServer().getPluginManager().getPlugins()) {
 			String cName = p.getClass().getName();
 			
 			if(cName.equals("com.miykeal.showCaseStandalone.ShowCaseStandalone")) {
 				showcase =  (ShowCaseStandalone) p;
 			}
 		}
 		
 		if (showcase == null) {
 			return null;
 		} else {
 			return showcase;
 		}
 	}
 	
 	private Essentials setupEssentials() {
 		Essentials essentials = null;
 		
 		for(Plugin p : getServer().getPluginManager().getPlugins()) {
 			String cName = p.getClass().getName();
 			
 			if(cName.equals("com.earth2me.essentials.Essentials")) {
 				essentials = (Essentials) p;
 			}
 		}
 		
 		if(essentials == null) {
 			return null;
 		} else {
 			return essentials;
 		}
 	}
 	
 	/**
 	 * Copy a file to another location
 	 * 
 	 * @param in an inputStream object
 	 * @param file file to copy
 	 */
 	private void copy(InputStream in, File file) {
         try {
             OutputStream out = new FileOutputStream(file);
             byte[] buf = new byte[1024];
             int len;
             while((len=in.read(buf))>0){
                 out.write(buf,0,len);
             }
             out.close();
             in.close();
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 	
 	/**
 	 * If configuration files dont exist this function is copy all the resource
 	 */
 	private void firstRun() {
 		if(!configFile.exists()) {
 			configFile.getParentFile().mkdirs();
 			copy(getResource("config.yml"), configFile);
 		}
 		if(!messagesFile.exists()) {
 			messagesFile.getParentFile().mkdirs();
 			copy(getResource("messages.yml"), messagesFile);
 		}
 		if(!buttonFile.exists()) {
 			buttonFile.getParentFile().mkdirs();
 			copy(getResource("buttons.yml"), buttonFile);
 		}
 		if(!foundationStoneFile.exists()) {
 			foundationStoneFile.getParentFile().mkdirs();
 			copy(getResource("foundationStone.yml"), foundationStoneFile);
 		}
 	}
 	
 	/**
 	 * Load all configuration to object
 	 */
 	public void loadConfiguration() {
 		try {
 			config.load(configFile);
 			messages.load(messagesFile);
 			button.load(buttonFile);
 			foundationStone.load(foundationStoneFile);
 		} catch(Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Saving configuration object to disk. THis only save the buttons resource
 	 */
 	public void saveConfiguration() {
 		try {
 			config.save(configFile);
 			messages.save(messagesFile);
 			button.save(buttonFile);
 			foundationStone.save(foundationStoneFile);
 		} catch(Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void onLoad() {
 		
 		config = new YamlConfiguration();
 		messages = new YamlConfiguration();
 		button = new YamlConfiguration();
 		foundationStone = new YamlConfiguration();
 		configFile = new File(getDataFolder(), "config.yml");
 		messagesFile = new File(getDataFolder(), "messages.yml");
 		buttonFile = new File(getDataFolder(), "buttons.yml");
 		foundationStoneFile = new File(getDataFolder(), "foundationStone.yml");
 		
 		try {
             this.firstRun();
         } catch (Exception e) {
             e.printStackTrace();
         }
 		
 		this.loadConfiguration();
 		
 		log = Logger.getLogger("Minecraft");
 		pdfile = getDescription();
 		pm = getServer().getPluginManager();
 		commandExec = new commandExecutor(this);
 	}
 	
 	public void registerListeners() {
 		final playerListener playerListener = new playerListener(this);
 		final entityListener entityListener = new entityListener(this);
 		final blockListener blockListener = new blockListener(this);
 		final inventoryListener inventoryListener = new inventoryListener(this);
 		final tamedMobHelperListener tamedMobHelperListener = new tamedMobHelperListener(this);
 		final foundationStoneListener foundationStoneListener = new foundationStoneListener(this); 
 		
 		pm.registerEvents(playerListener, this);
 		pm.registerEvents(entityListener, this);
 		pm.registerEvents(blockListener, this);
 		pm.registerEvents(inventoryListener, this);
 		pm.registerEvents(tamedMobHelperListener, this);
 		pm.registerEvents(foundationStoneListener, this);
 	}
 	
 	public void registerCommands() {
 		getCommand("achat").setExecutor(commandExec);
 		getCommand("fakechat").setExecutor(commandExec);
 		getCommand("definebutton").setExecutor(commandExec);
 		getCommand("idban").setExecutor(commandExec);
 		getCommand("idunban").setExecutor(commandExec);
 		getCommand("napos").setExecutor(commandExec);
 		getCommand("nappal").setExecutor(commandExec);
 		getCommand("rur").setExecutor(commandExec);
 		getCommand("fullenchant").setExecutor(commandExec);
 		getCommand("entitylist").setExecutor(commandExec);
 		getCommand("freeze").setExecutor(commandExec);
 	}
 	
 	public void onEnable() {
 		logPrefix = "[" + pdfile.getName() + "] ";
 		perm = new permHandler(this);
 		econ = new econHandler(this);
 		scs = this.setupScs();
 		ess = this.setupEssentials();
 		this.registerListeners();
 		this.registerCommands();
 		
 		if(scs == null) {
 			this.log.warning(this.logPrefix + "ShowCaseStandalone not found! Disabling SCS support!");
 		}
 		
 		if(ess == null) {
 			this.log.warning(this.logPrefix + "Essentials not forund! Disabling essentials support!");
 		}
 		
 		if(this.config.getInt("savealldelay")>0) {
 			log.info(this.logPrefix + "Auto save enabled for " + this.config.getInt("savealldelay") + " minutes!");
 			autoSave = new AutoSaveThread(this);
 			autoSave.start();
 		} else {
 			log.info(this.logPrefix + "Auto save disabled!");
 		}
 		
 		this.lagDetect = new LagDetectThread(this);
 		this.lagDetect.start();
 		
		recipes = new recipes();
		recipes.createRecipes(this);
		
 		log.info(this.logPrefix + "Version: " + pdfile.getVersion() + " Sucessfully enabled!");
 	}
 	
 	public void onDisable() {
 		this.saveConfiguration();
 		this.autoSave.running = false;
 		this.lagDetect.running = false;
 		log.info(this.logPrefix + "Version: " + pdfile.getVersion() + " Sucessfully disabled!");
 	}
 	
 }
