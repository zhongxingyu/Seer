 package net.milkycraft.em.config;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import net.milkycraft.em.EntityManager;
 
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 public abstract class ConfigLoader {
 
 	protected FileConfiguration config;
 	protected File configFile;
 	protected File dataFolder;
 	protected String fileName;
 	protected EntityManager plugin;
 	private InputStream def;
 
 	public ConfigLoader(EntityManager plugin, String fileName) {
 		this.plugin = plugin;
 		this.def = plugin.getResource("config.yml");
 		this.fileName = fileName;
 		dataFolder = plugin.getDataFolder();
 		if (!dataFolder.exists()) {
 			dataFolder.mkdir();
 			dataFolder = plugin.getDataFolder();
 		}
 		configFile = new File(dataFolder, File.separator + fileName);
 		if (!configFile.exists()) {
 			try {
 				configFile.createNewFile();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			writeConfig(def);
 		}
 		config = YamlConfiguration.loadConfiguration(configFile);
 	}
 
 	protected void addDefaults() {
 		config.options().copyDefaults(true);
 		saveConfig();
 	}
 
 	public void load() {
 		if (!configFile.exists()) {
 			dataFolder.mkdir();
 			saveConfig();
 		}
 		addDefaults();
 		loadKeys();
 	}
 
 	protected abstract void loadKeys();
 
 	protected void rereadFromDisk() {
 		config = YamlConfiguration.loadConfiguration(configFile);
 	}
 
 	protected void saveConfig() {
 		try {
 			config.save(configFile);
 		} catch (IOException ex) {
 			//
 		}
 	}
 
 	protected void set(String key, Object value) {
 		config.set(key, value);
 		this.saveConfig();
 	}
 
 	protected void saveIfNotExist() {
 		if (!configFile.exists()) {
 			if (plugin.getResource(fileName) != null) {
 				plugin.getLogger().info("Saving " + fileName + " to disk");
 				plugin.saveResource(fileName, false);
 			}
 		}
 		rereadFromDisk();
 	}
 
 	protected void writeConfig(InputStream in) {
 		OutputStream out = null;
 		try {
 			out = new FileOutputStream(configFile);
 			int read = 0;
 			byte[] bytes = new byte[1024];
 			while ((read = in.read(bytes)) != -1) {
 				out.write(bytes, 0, read);
 			}
 			out.flush();
 		} catch (Exception ex) {
 			plugin.getLogger().severe(
					"Writing default config generating an exception: "
 							+ ex.getMessage());
 		} finally {
 			try {
 				if (in != null) {
 					in.close();
 				}
 				if (out != null) {
 					out.close();
 				}
 			} catch (Exception ex) {
 				plugin.getLogger().severe(
						"Closing streams for config writes generating an exception: "
 								+ ex.getMessage());
 			}
 		}
 	}
 }
