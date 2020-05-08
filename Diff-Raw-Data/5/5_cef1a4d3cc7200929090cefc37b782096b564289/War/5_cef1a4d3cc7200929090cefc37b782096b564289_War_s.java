 package me.hunterboerner.war;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.bukkit.OfflinePlayer;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 public class War extends JavaPlugin {
 	private Logger log											=	Logger.getLogger("Minecraft");
 	private Commands cmds										=	new Commands(this);
 	private WarListener warListener								=	new WarListener(this);
 	private Map<OfflinePlayer,Set<OfflinePlayer>> warMongers	=	Collections.synchronizedMap(new HashMap<OfflinePlayer,Set<OfflinePlayer>>());
 
 	@Override
 	public void onEnable() {
 		getCommand("war").setExecutor(cmds);
 		getServer().getPluginManager().registerEvents(warListener, this);
 		getMongersFile();
 		logMessage("Enabled");
 		// plugin is being enabled and outputting to the console
 	}
 
 	@Override
 	public void onDisable() {
 		logMessage("Disabled");
 		// Same as enable but turns it off
 
 	}
 	
 	public void logMessage(String msg){
 		PluginDescriptionFile pdFile	=	this.getDescription();
 		log.info("["+pdFile.getName()+" "+pdFile.getVersion()+"]: "+msg);
 	}
 	
 	
 	/**
 	 * Adds a war to the player's set of wars
 	 * 
 	 * @param monger The person initiating the war
 	 * @param target The person upon which the war is being declared
 	 * @return boolean True if the war was added, false if it was already there
 	 */
 	public boolean addWar(OfflinePlayer monger,OfflinePlayer target){
 		if(!warMongers.containsKey(monger)){
 			warMongers.put(monger, Collections.synchronizedSet(new HashSet<OfflinePlayer>()));
 		}
 		boolean result	=	 warMongers.get(monger).add(target);
 		saveMongersFile();
 		return result;
 	}
 	
 	/**
 	 * Removes a war from the player's set of wars
 	 * 
 	 * @param monger The person who initiated the war
 	 * @param target The person upon which the war was declared
 	 * @return boolean True if the war was removed, false if it was never there
 	 */
 	public boolean removeWar(OfflinePlayer monger, OfflinePlayer target){
 		if(warMongers.containsKey(monger)){
 			boolean result = warMongers.get(monger).remove(target);
 			saveMongersFile();
 			return result;
 		}
 		return false;
 	}
 	
 	/**
 	 * Loads the WarMongers file from the config directory into the memory map
 	 * My hope is this will not cause a problem because of reading the list from disk
 	 * and then writing to the same config through addWar
 	 */
 	private void getMongersFile(){
 		File configFile				=	new File(this.getDataFolder(),"WarMongers");
 		if(configFile.exists()){
 			FileConfiguration config	=	YamlConfiguration.loadConfiguration(configFile);
 			Iterator<String> kitr		=	config.getKeys(false).iterator();
 			while(kitr.hasNext()){
 				String mongerName		=	kitr.next();
 				OfflinePlayer monger	=	getServer().getOfflinePlayer(mongerName);
 				if(config.isList(mongerName)){
 					Iterator<String> titr	=	config.getStringList(mongerName).iterator();
 					while(titr.hasNext()){
 						String targetName		=	titr.next();
 						OfflinePlayer target	=	getServer().getOfflinePlayer(targetName);
 						if(monger!=null && target!=null){
 							addWar(monger, target);
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Saves the current warMongers and their targets to a config file in the directory
 	 */
 	private void saveMongersFile(){
 		File configFile				=	new File(this.getDataFolder(),"WarMongers");
 		FileConfiguration config	=	YamlConfiguration.loadConfiguration(configFile);
 		Iterator<OfflinePlayer> pitr		=	warMongers.keySet().iterator();
 		while(pitr.hasNext()){
 			OfflinePlayer monger			=	pitr.next();
 			String[] names					=	new String[warMongers.get(monger).size()];
 			Iterator<OfflinePlayer> titr	=	warMongers.get(monger).iterator();
 			while(titr.hasNext()){
				names[names.length]	=	titr.next().getName();
 			}
 			
 			config.set(monger.getName(), names);			
 		}
 		try {
 			config.save(configFile);
 		} catch (IOException e) {
 			log.log(Level.WARNING,"Unable to save the configuration to disk: "+configFile.toString(), e);
 		}
 		
 	}
 	
 	/**
 	 * Checks to see if two players are at war
 	 * 
 	 * @param monger The first player to check
 	 * @param target The second player to check
 	 * @return boolean True if a player is at war with another, false otherwise
 	 */
 	public boolean areAtWar(OfflinePlayer monger,OfflinePlayer target){
 		boolean hasTarget	=	false;
 		if(warMongers.containsKey(monger)){
 			hasTarget	=	warMongers.get(monger).contains(target);
 		}
 		if(!hasTarget && warMongers.containsKey(target)){
 			hasTarget	=	warMongers.get(target).contains(monger);
 		}
 		
 		return hasTarget;
 	}
 	
 	/**
 	 * Checks to see if a player has declared war on another player
 	 * This differs from areAtWar in that it will not return true if the target
 	 * had declared war on the monger
 	 * 
 	 * @param monger The player who may have declared war
 	 * @param target The player whom may have had war declared upon them
 	 * @return True if monger has declared war on target, false otherwise
 	 */
 	public boolean isAtWarWith(OfflinePlayer monger,OfflinePlayer target){
 		boolean hasTarget	=	false;
 		if(warMongers.containsKey(monger)){
 			hasTarget	=	warMongers.get(monger).contains(target);
 		}
 		return hasTarget;
 	}
 }
