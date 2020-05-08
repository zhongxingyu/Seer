 package net.loadingchunks.plugins.PushEnder;
 
 import java.util.HashMap;
 
 import org.bukkit.configuration.ConfigurationSection;
 
 public class PushUser {
 	public String userToken = "";
 	public String username = "";
 	public HashMap<String, Boolean> eventConfig = new HashMap<String, Boolean>();
 	
 	public PushUser(String name, ConfigurationSection cs) {
		if(cs.contains("token"))
			userToken = cs.getString("token");
 		
 		if(!cs.contains("events"))
 			return;
 				
 		for(String key : cs.getConfigurationSection("events").getKeys(false)) {
 			eventConfig.put(key, cs.getBoolean("events." + key));
 		}
 	}
 }
