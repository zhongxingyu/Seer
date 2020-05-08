 package com.github.thebiologist13;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.logging.Logger;
 
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class NeverBreak extends JavaPlugin {
 	
 	//YAML variable
 	private FileConfiguration config;
 	
 	//YAML file variable
 	private File configFile;
 	
 	//Toggling command executor variable
 	private ToggleCommand tc;
 	
 	//Set durability command executor variable
 	private DurabilityCommand dc;
 	
 	//Logger
 	Logger log = Logger.getLogger("Minecraft");
 	
 	public void onEnable() {
 		//Listeners
 		getServer().getPluginManager().registerEvents(new BreakListener(this), this);
 		getServer().getPluginManager().registerEvents(new LoginListener(this), this);
 		
 		//Toggle command setup
 		tc = new ToggleCommand(this);
 		getCommand("neverbreak").setExecutor(tc);
 		
 		//Durability command setup
 		dc = new DurabilityCommand(this);
		getCommand("setdurabilty").setExecutor(dc);
 		
 		config = this.getCustomConfig();
 		config.options().copyDefaults(true);
 		saveCustomConfig();
 		
 		//Enable message
 		log.info("NeverBreak by thebiologist13 has been enabled!");
 	}
 	
 	public void onDisable() {
 		//Disable message
 		log.info("NeverBreak by thebiologist13 has been disabled!");
 	}
 	
 	//Config stuff
 	public void reloadCustomConfig() {
 	    if (configFile == null) {
 	    	configFile = new File(getDataFolder(), "config.yml");
 	    }
 	    config = YamlConfiguration.loadConfiguration(configFile);
 	 
 	    // Look for defaults in the jar
 	    InputStream defConfigStream = getResource("config.yml");
 	    if (defConfigStream != null) {
 	        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
 	        config.setDefaults(defConfig);
 	    }
 	}
 	
 	public FileConfiguration getCustomConfig() {
 	    if (config == null) {
 	        reloadCustomConfig();
 	    }
 	    return config;
 	}
 	
 	public void saveCustomConfig() {
 	    if (config == null || configFile == null) {
 	    	return;
 	    }
 	    try {
 	        config.save(configFile);
 	    } catch (IOException ex) {
 	        log.severe("Could not save config!");
 	    }
 	}
 }
