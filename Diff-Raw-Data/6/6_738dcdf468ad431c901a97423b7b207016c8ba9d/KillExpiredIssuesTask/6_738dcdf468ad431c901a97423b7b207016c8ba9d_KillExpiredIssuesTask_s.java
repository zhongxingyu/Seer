 package me.makskay.bukkit.tidy.tasks;
 
 import java.util.Set;
 
 import me.makskay.bukkit.tidy.TidyPlugin;
 
 public class KillExpiredIssuesTask implements Runnable { // TODO this needs to be tested, it might hang if there's a bunch of issues
 	private TidyPlugin plugin;
 	
 	public KillExpiredIssuesTask(TidyPlugin plugin) {
 		this.plugin = plugin;
 	}
 
 	public void run() {
 		boolean isOpen = true;
 		String key     = "";
 		Set<String> keys = plugin.issuesYml.getConfig().getConfigurationSection("issues").getKeys(false);
		while (isOpen) {
 			if (!keys.iterator().hasNext()) {
 				return;
 			}
 			
 			key = keys.iterator().next();
 			isOpen = plugin.issuesYml.getConfig().getBoolean("issues." + key + ".open");
 		}
 		
 		long currentTime = System.currentTimeMillis();
 		long deltaTime = currentTime - plugin.issuesYml.getConfig().getLong("issues." + key + ".timestamp");
 		if (deltaTime > 259200000) { // 259200000 == 3 days (TODO make the number of days configurable)
 			plugin.issuesYml.getConfig().set("issues." + key, null);
 		}
 	}
 
 }
