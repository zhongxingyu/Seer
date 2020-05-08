 package musician101.controlcreativemode;
 
 import java.io.File;
 import java.util.logging.Logger;
 
 import musician101.controlcreativemode.commands.CCMCommand;
 import musician101.controlcreativemode.commands.CreativeCommand;
 import musician101.controlcreativemode.commands.SurvivalCommand;
 import musician101.controlcreativemode.lib.Constants;
 import musician101.controlcreativemode.listeners.BlockListener;
 import musician101.controlcreativemode.listeners.EntityListener;
 import musician101.controlcreativemode.listeners.PlayerListener;
 import musician101.controlcreativemode.util.UpdateChecker;
 
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * Main class for the plugin.
  * 
  * @author Musician101
  */
 public class ControlCreativeMode extends JavaPlugin
 {
 	protected UpdateChecker updateChecker;
 	File configFile;
 	FileConfiguration config;
 	
 	/**
 	 * Sets up the logger.
 	 * 
 	 * @return JavaPlugin.getLogger()
 	 */
 	public Logger logger()
 	{
 		return getLogger();
 	}
 	
 	/** Initializes the plugin, checks for config, and register commands and listeners. */
 	@Override
     public void onEnable()
 	{
 		/** Listener Registers */
 		getServer().getPluginManager().registerEvents(new BlockListener(this), this);
 		getServer().getPluginManager().registerEvents(new EntityListener(this), this);
 		getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
 		
 		/** Command Executors */
 		getCommand(Constants.CCM).setExecutor(new CCMCommand(this));
 		getCommand(Constants.CREATIVE).setExecutor(new CreativeCommand(this));
 		getCommand(Constants.SURVIVAL).setExecutor(new SurvivalCommand(this));
 		
 		/** Check for config, create it if it's missing, and load it. */
 		configFile = new File(getDataFolder(), "config.yml");
 		saveDefaultConfig();
 		config = new YamlConfiguration();
 		try
 		{
 			config.load(configFile);
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 		
 		/** Check for a new version if it's enabled. */
 		if (config.getBoolean("checkForUpdate"))
 		{
 			this.updateChecker = new UpdateChecker(this, "http://dev.bukkit.org/bukkit-plugins/control-creative-mode/files.rss");
 			logger().info("Update checker is enabled.");
 			if (this.updateChecker.updateNeeded())
 			{
 				logger().info("A new version is available: " + this.updateChecker.getVersion());
 				logger().info("Get it from: " + this.updateChecker.getLink());
 			}
 			else
 				logger().info("CCM is up to date.");
 		}
		else if (!config.getBoolean("checkForUpdate"))
 			logger().info("Update checker is disabled.");
     }
 	
 	/** Shuts off the plugin. */
 	@Override
 	public void onDisable()
 	{
         logger().info(Constants.PREFIX_PERMISSION + "Shutting down.");
     }
 }
