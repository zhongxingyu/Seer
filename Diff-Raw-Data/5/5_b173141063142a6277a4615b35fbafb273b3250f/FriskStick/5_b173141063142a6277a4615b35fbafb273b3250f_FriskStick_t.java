 package friskstick.cops.plugin;
 
 import java.io.IOException;
 import java.util.logging.Logger;
 
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import friskstick.cops.commands.FriskCommand;
 import friskstick.cops.commands.ReportCommand;
 import friskstick.cops.data.MetricsLite;
//import friskstick.cops.drugs.Drugs;
 //import friskstick.cops.data.PluginUpdateCheck;
 import friskstick.cops.stick.Stick;
 
 public class FriskStick extends JavaPlugin {
 
 	public final Logger logger = Logger.getLogger("Minecraft");
 
 	public void onEnable() {
 
 		try {
 
 			MetricsLite metrics = new MetricsLite(this);
 			metrics.start();
 
 		} catch (IOException e) {
 
 		}
 
 		PluginDescriptionFile pdffile = this.getDescription();
 		logger.info(pdffile.getName() + " v" + pdffile.getVersion() + " has been enabled!");
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvents(new Stick(this), this); //Stick(Right Click Event) Register
		//pm.registerEvents(new Drugs(this), this); // Drug Register
 		//pm.registerEvents(new PluginUpdateCheck(this), this);
 		getCommand("frisk").setExecutor(new FriskCommand(this));
 		getCommand("report").setExecutor(new ReportCommand(this));
 		getConfig().options().copyDefaults(true);
 		saveConfig();
 
 	}
 
 	public void onDisable() {
 
 		PluginDescriptionFile pdffile = this.getDescription();
 		logger.info(pdffile.getName() + " has been disabled.");
 
 	}
 
 }
