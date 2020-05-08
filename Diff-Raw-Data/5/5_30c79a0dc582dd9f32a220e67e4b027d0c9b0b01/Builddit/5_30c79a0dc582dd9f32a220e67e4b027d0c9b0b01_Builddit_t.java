 package net.chiisana.builddit;
 
 import com.sk89q.worldedit.bukkit.WorldEditAPI;
 import com.sk89q.worldedit.bukkit.WorldEditPlugin;
 import net.chiisana.builddit.command.BuildditCommand;
 import net.chiisana.builddit.command.PlotCommand;
 import net.chiisana.builddit.controller.BuildditPlot;
 import net.chiisana.builddit.generator.PlotGenerator;
 import net.chiisana.util.MySQLUtil;
 import org.bukkit.event.Listener;
 import org.bukkit.generator.ChunkGenerator;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.logging.Level;
 
 public class Builddit extends JavaPlugin implements Listener {
 	public WorldEditPlugin wePlugin;
 	public WorldEditAPI weAPI;
 	private MySQLUtil database;
 
 	public static Builddit instance;
 
 	public void onDisable() {
 		BuildditPlot.getInstance().onDisable();
 		this.saveConfig();
 	}
 
 	public void onEnable() {
 		instance = this;
 
 		wePlugin = (WorldEditPlugin) getServer().getPluginManager().getPlugin("WorldEdit");
 		weAPI = new WorldEditAPI(wePlugin);
 
 		// Instantiate database connection
 		this.database = new MySQLUtil(
 				this.getConfig().getString("MySQL.Server", "127.0.0.1"),
 				this.getConfig().getInt("MySQL.Port", 3306),
 				this.getConfig().getString("MySQL.Database", "database"),
 				this.getConfig().getString("MySQL.User", "user"),
 				this.getConfig().getString("MySQL.Pass", "pass1234")
 		);
 
 		// save our configuration data in case if it is first run
 		this._saveConfig();
 
 		if (!this.database.openConnection()) {
 			getLogger().log(Level.SEVERE, "Database Connection Failed! Plugin Cannot Initiate!!");
 			getLogger().log(Level.SEVERE, "Builddit is disabling!");
 			this.setEnabled(false);
 			return;
 		}
 
 		getCommand("builddit").setExecutor(new BuildditCommand());
 
		// Enable BuildditPlot
 		BuildditPlot.getInstance().onEnable();
		getCommand("plot").setExecutor(new PlotCommand());
 	}
 
 	public static Builddit getInstance() {
 		if (instance == null) {
 			instance = new Builddit();
 		}
 		return instance;
 	}
 
 	public ChunkGenerator getDefaultWorldGenerator(String worldname, String id) {
 		return (new PlotGenerator());
 	}
 
 	private void _saveConfig() {
 		File configFile = new File(getDataFolder(), "config.yml");
 		try {
 			this.getConfig().save(configFile);
 		} catch (IOException e) {
 			getLogger().log(Level.SEVERE, "Failed to write configuration data back to config.yml!");
 			getLogger().log(Level.SEVERE, "Your preferences will not be saved for next time.");
 		}
 	}
 }
 
