 package fr.frozentux.craftguard2.config;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 import fr.frozentux.craftguard2.CraftGuardPlugin;
 import fr.frozentux.craftguard2.config.compat.CraftGuard1ConfigConverter;
 
 /**
  * Loads, writes and stores configuration options for CraftGuard
  * @author FrozenTux
  *
  */
 public class CraftGuardConfig {
 	
 	private CraftGuardPlugin plugin;
 	
 	private File configFile;
 	private FileConfiguration file;
 	
 	private HashMap<String, Object> fields;
 	
	private static List<String> preModules = Arrays.asList("craft", "smelt");
 	private static ArrayList<String> modules = new ArrayList<String>();
 	
 	private String[] defaultKeys = {"preventiveallow", "denymessage", "log", "logmessage", "baseperm", "debug", "notifyplayer", "modules"};
 	private Object[] defaultValues = {true, "You can't %m %n !", true, "%p tried to %m %n(%i) but has been denied", "craftguard", false, true, modules};
 	
 	/**
 	 * Loads, writes and stores configuration options for CraftGuard
 	 * @param plugin	The plugin using this object
 	 *
 	 */
 	public CraftGuardConfig(CraftGuardPlugin plugin){
 		this.plugin = plugin;
 		fields = new HashMap<String, Object>();
 		configFile = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "config.yml");
 		file = new YamlConfiguration();
 		modules.addAll(preModules);
 	}
 	
 	/**
 	 * Loads the configuration from the config file, erasing values in memory
 	 */
 	public void load(){
 		fields = new HashMap<String, Object>();
 		
 		try {
 			file.load(configFile);
 		} catch (Exception e) {
 			
 		}
 		
 		if(file.isSet("craftguard")){	//Old configuration type, needs to be converted
 			plugin.getCraftGuardLogger().info("Old configuration format has been detected ! Your file will be converted.");
 			new CraftGuard1ConfigConverter(plugin).convert();
 			file = new YamlConfiguration();
 			try {
 				file.load(configFile);
 			} catch (Exception e) {
 				
 			}
 		}
 		
 		for(int i = 0 ; i<defaultKeys.length ; i++){
 			fields.put(defaultKeys[i], defaultValues[i]);
 		}
 		
 		if(!file.isSet("log")){
 			plugin.getCraftGuardLogger().info("Configuration file not detected ! Writing defaults...");
 			file.options().header("CraftGuard version 2.X by FrozenTux\nhttp://dev.bukkit.org/server-mods/craftguard").copyHeader();
			write();
 		}
 		
 		Iterator<String> it = file.getKeys(false).iterator();
 		
 		while(it.hasNext()){
 			String key = it.next();
 			fields.put(key, file.get(key));
 		}
 	}
 	
 	/**
 	 * Writes the fields into the plugin's {@link FileConfiguration}
 	 */
 	public void write(){
 		Iterator<String> it = fields.keySet().iterator();
 		
 		while(it.hasNext()){
 			String key = it.next();
 			file.set(key, fields.get(key));
 		}
 		
 		try {
 			file.save(configFile);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Gets the field corresponding to the given key
 	 * @param key	The key of the field
 	 * @return		<code>null</code> if the field doesn't exist;otherwise the field as an {@link Object}
 	 */
 	public Object getKey(String key){
 		Object value = fields.get(key);
 		return value;
 	}
 	
 	/**
 	 * Gets the field corresponding to the given key
 	 * @param key	The key of the field
 	 * @return		<code>null</code> if the field doesn't exist;otherwise the field as an {@link String}
 	 */
 	public String getStringKey(String key){
 		return String.valueOf(getKey(key));
 	}
 	
 	/**
 	 * Gets the field corresponding to the given key
 	 * @param key	The key of the field
 	 * @return		<code>null</code> if the field doesn't exist;otherwise the field as an {@link Boolean}
 	 */
 	public boolean getBooleanKey(String key){
 		return (Boolean)getKey(key);
 	}
 }
