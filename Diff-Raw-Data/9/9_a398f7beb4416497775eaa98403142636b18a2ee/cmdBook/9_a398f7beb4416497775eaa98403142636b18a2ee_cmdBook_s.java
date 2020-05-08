 package VdW.Maxim.cmdBook;
 
 /* Name:		cmdBook
  * Version: 	1.1.1
  * Made by: 	Maxim Van de Wynckel
  * Build date:	2/09/2012						
  */
 
 import java.io.IOException;
 import java.util.logging.Logger;
 
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import VdW.Maxim.cmdBook.Metrics;
 
 public class cmdBook extends JavaPlugin {
 	// Create global variables
 	public final Logger logger = Logger.getLogger("Minecraft"); // Console
 	public cmdBook plugin = this; // Plugin will now refer to cmdBook
 	private CommandClass CommandListener; // Wait for commands in a different
 											// class
 	public final PlayerListener pl = new PlayerListener(this);
 
 	@Override
 	public void onEnable() {
 		// PUT THIS INTO EVERY METHOD
 		PluginDescriptionFile pdfFile = this.getDescription();
 		String cmdFormat = "[" + pdfFile.getName() + "] ";
 		// --------------------------
 
 		// This function will be started when the plugin is Enabled
 		// Load everything here
 
 		// Show plugin information in console
 		this.logger.info(cmdFormat + "Made by: Maxim Van de Wynckel");
 
 		// Start Player listener
 		this.logger.info(cmdFormat + "Starting player listener...");
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvents(pl, this);
 		this.logger.info(cmdFormat + "Player listener loaded!");
 
 		// Now start Command Listener - This will wait for commands
 		this.logger.info(cmdFormat + "Starting command listener...");
 		CommandListener = new CommandClass(this);
 		// List all commands that have to be heard by the Command Listener
		getCommand("cmdbook").setExecutor(CommandListener);
		getCommand("cb").setExecutor(CommandListener);
 		this.logger.info(cmdFormat + "Command listener loaded!");
 
 		// Load Metrics
 		try {
 			Metrics metrics = new Metrics(this);
 			metrics.start();
 			this.logger.info(cmdFormat + "Metrics Stats loaded!");
 		} catch (IOException e) {
 			// Failed to submit the stats :-(
 			this.logger.warning(cmdFormat + "Unable to load Metrics!");
 		}
 
 		// Check for updates
 		this.getServer().getScheduler()
 				.scheduleAsyncDelayedTask(this, new Runnable() {
 					public void run() {
 						updater check = new updater(plugin);
 						check.checkUpdates();
 					}
 				}, 0L);
 	}
 
 	@Override
 	public void onDisable() {
 
 	}
 }
