 package musician101.controlcreativemode;
 
 import java.io.File;
 import java.util.List;
 
 import org.bukkit.configuration.file.FileConfiguration;
 
 public class Config
 {
 	ControlCreativeMode plugin;
 	public static boolean blockLavaBucket;
 	public static boolean blockTNTMinecart;
 	public static boolean blockWaterBucket;
 	public boolean checkForUpdate;
 	public static List<Integer> noBlockBasedInventory;
 	public static List<String> noEntityBasedInventory;
 	public static List<Integer> noDrop;
 	public static List<Integer> noPlace;
 	public static List<String> noSpawn;
 	public static List<Integer> noThrow;
 	
 	/**
 	 * Config constructor.
 	 * 
 	 * @param plugin Reference's the plugin's main class.
 	 */
 	public Config(ControlCreativeMode plugin)
 	{
 		this.plugin = plugin;
 		File config = new File(plugin.getDataFolder(), "config.yml");
 		if (!config.exists())
 		{
 			if (!config.getParentFile().mkdirs()) plugin.getLogger().warning("Could not create config.yml");
 			plugin.saveDefaultConfig();
 		}
 		reloadConfiguration();
 	}
 	
 	/**
 	 * Reloads the server's configuration file.
 	 */
 	public void reloadConfiguration()
 	{
 		plugin.reloadConfig();
 		final FileConfiguration config = plugin.getConfig();
		blockLavaBucket = config.getBoolean("blockLavaBucket", true);
		blockTNTMinecart = config.getBoolean("blockTNTMinecart", true);
		blockWaterBucket = config.getBoolean("blockWaterBucket", true);
		checkForUpdate = config.getBoolean("checkForUpdate", true);
 		noBlockBasedInventory = config.getIntegerList("noBlockBasedInventory");
 		noEntityBasedInventory = config.getStringList("noEntityBasedInventory");
 		noDrop = config.getIntegerList("noDrop");
 		noPlace = config.getIntegerList("noPlace");
 		noSpawn = config.getStringList("noSpawn");
 		noThrow = config.getIntegerList("noThrow");
 	}
 }
