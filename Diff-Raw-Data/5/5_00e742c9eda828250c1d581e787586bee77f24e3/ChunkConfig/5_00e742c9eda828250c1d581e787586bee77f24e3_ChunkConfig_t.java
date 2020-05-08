 package me.bluejelly.main.configs;
 
 import java.io.File;
 
 import org.bukkit.configuration.file.YamlConfiguration;
 
import me.bluejelly.main.GuildZ;

 public class ChunkConfig {
 
 	public static YamlConfiguration config;
 	private static File configFile;
 
 	public ChunkConfig(File configFile) {
 		ChunkConfig.config = new YamlConfiguration();
 		ChunkConfig.configFile = configFile;
 	}
 
 	public boolean load() {
 		try {
 			if (!configFile.exists()) {
 				configFile.createNewFile();
 				getdefaults();
 			}
 			config.load(configFile);
 			save();
 			return true;
 		} catch (Exception e) {
 			GuildZ.console.sendMessage("Config Failed to load, returned error:\n"
 					+ e.getMessage());
 			return false;
 		}
 	}
 
 	private void getdefaults() {
 		config.options().copyDefaults(true);
 		GuildZ.console.sendMessage("Config Defaults loaded for the first time.");
 	}
 
 	public static boolean save() {
 		try {
 			config.save(configFile);
 		} catch (Exception e) {
 			GuildZ.console.sendMessage("Config Failed to save, returned error: "
 					+ e.getMessage());
 		}
 		return true;
 	}
 
 	public YamlConfiguration getConfig() {
 		return config;
 	}
 	
 }
 
