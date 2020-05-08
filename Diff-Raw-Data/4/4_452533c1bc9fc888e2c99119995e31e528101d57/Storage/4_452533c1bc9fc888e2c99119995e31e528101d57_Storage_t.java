 package newbieprotect;
 
 import java.io.File;
 import java.io.IOException;
import java.util.HashSet;
 import java.util.List;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.bukkit.Bukkit;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 
 import com.sk89q.worldguard.bukkit.BukkitUtil;
 import com.sk89q.worldguard.bukkit.WGBukkit;
 
 public class Storage {
 
 	private Nprotect plugin;
 	private Config config;
 	private File configfile;
 	public Storage(Nprotect plugin, Config config)
 	{
 		this.plugin = plugin;
 		this.config = config;
 		configfile = new File(plugin.getDataFolder(),"playerdata.yml");
 	}
 	
 	private ConcurrentHashMap<String,Long> playerprotecttime = new ConcurrentHashMap<String,Long>();
 	protected void protectPlayer(String playername, long starttimestamp)
 	{
 		playerprotecttime.put(playername, starttimestamp);
 	}
 	protected void unprotectPlayer(String playername)
 	{
 		playerprotecttime.remove(playername);
 	}
 	protected boolean isPlayerProtected(String playername)
 	{
 		if (playerprotecttime.containsKey(playername) && System.currentTimeMillis() - playerprotecttime.get(playername) > config.protecttime)
 		{
 			unprotectPlayer(playername);
 		}
 		Player player = Bukkit.getPlayerExact(playername);
 		if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null)
 		{
 			List<String> aregions = WGBukkit.getRegionManager(player.getWorld()).getApplicableRegionsIDs(BukkitUtil.toVector(player.getLocation()));
 			for (String region : aregions)
 			{
 				if (config.disabledWGregions.contains(region))
 				{
 					return false;
 				}
 			}
 		}
 		return playerprotecttime.containsKey(playername);
 	}
 	
 	protected void loadTimeConfig()
 	{
 		FileConfiguration config = YamlConfiguration.loadConfiguration(configfile);
 		ConfigurationSection cs = config.getConfigurationSection("");
 		if (cs != null)
 		{
 			for (String playername : cs.getKeys(false))
 			{
 				playerprotecttime.put(playername, config.getLong(playername));
 			}
 		}
 	}
 	protected void saveTimeConfig()
 	{
 		FileConfiguration config = new YamlConfiguration();
 		for (String playername : playerprotecttime.keySet())
 		{
 			config.set(playername, playerprotecttime.get(playername));
 		}
 		try {
 			config.save(configfile);
 		} catch (IOException e) {}
 	}
 	
 	private int taskid;
 	protected void startCheck()
 	{
 		Bukkit.getScheduler().scheduleAsyncRepeatingTask(plugin, new Runnable(){
 			public void run()
 			{
				for (String playername : new HashSet<String>(playerprotecttime.keySet()))
 				{
 					if (System.currentTimeMillis() - playerprotecttime.get(playername) > config.protecttime)
 					{
 						unprotectPlayer(playername);
 					}
 				}
 			}
 		}, 0, 20*60);	
 	}
 	protected void stopCheck()
 	{
 		Bukkit.getScheduler().cancelTask(taskid);
 	}
 	
 
 	
 	
 }
