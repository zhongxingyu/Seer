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
 
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 
 public class War extends JavaPlugin {
 	private Logger log							=	Logger.getLogger("Minecraft");
 	private Commands cmds						=	new Commands(this);
 	private Map<Player,Set<Player>> warMongers	=	Collections.synchronizedMap(new HashMap<Player,Set<Player>>());
 
 	@Override
 	public void onEnable() {
 		getCommand("war").setExecutor(cmds);
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
 	public boolean addWar(Player monger,Player target){
 		if(!warMongers.containsKey(monger)){
 			warMongers.put(monger, Collections.synchronizedSet(new HashSet<Player>()));
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
 	public boolean removeWar(Player monger, Player target){
 		if(warMongers.containsKey(monger)){
 			boolean result = warMongers.get(monger).remove(target);
 			saveMongersFile();
 			return result;
 		}
 		return false;
 	}
 	
 	private void saveMongersFile(){
 		File configFile				=	new File(this.getDataFolder(),"WarMongers");
 		FileConfiguration config	=	YamlConfiguration.loadConfiguration(configFile);
 		Iterator<Player> pitr		=	warMongers.keySet().iterator();
 		while(pitr.hasNext()){
 			Player monger					=	pitr.next();
			config.set(monger.getName(), warMongers.get(monger));			
 		}
 		try {
 			config.save(configFile);
 		} catch (IOException e) {
 			log.log(Level.WARNING,"Unable to save the configuration to disk: "+configFile.toString(), e);
 		}
 		
 	}
 }
