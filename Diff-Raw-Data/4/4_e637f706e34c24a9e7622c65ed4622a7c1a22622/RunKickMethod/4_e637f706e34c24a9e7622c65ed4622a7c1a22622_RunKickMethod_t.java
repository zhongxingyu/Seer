 package musician101.emergencywhitelist.util;
 
 import musician101.emergencywhitelist.EmergencyWhitelist;
 import musician101.emergencywhitelist.lib.Constants;
 import musician101.emergencywhitelist.runnables.KickPlayers;
 
 import org.bukkit.Bukkit;
 
 /**
  * Runs when '/ewl toggle' is executed.
  * 
  * @author Musician101
  */
 public class RunKickMethod 
 {
 	/**
 	 * @param plugin Reference to the plugin's main class.
 	 * @param enabled References the 'enabled' option in the config.
 	 */
 	public RunKickMethod(EmergencyWhitelist plugin, boolean enabled)
 	{
		plugin.getLogger().info(Constants.getWhitelistEnabled(enabled));
		plugin.getLogger().info(Constants.getToggleMessage(enabled));
 		if (enabled)
 		{
 			Bukkit.broadcastMessage(Constants.getWhitelistAnnounce(enabled));
 			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new KickPlayers(plugin), 100L);
 		}
 		else
 			Bukkit.broadcastMessage(Constants.getWhitelistAnnounce(enabled));
 	}
 }
