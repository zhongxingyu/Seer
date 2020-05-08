 package me.heldplayer.ModeratorGui;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.PersistenceException;
 
 import me.heldplayer.ModeratorGui.tables.Bans;
 import me.heldplayer.ModeratorGui.tables.Demotions;
 import me.heldplayer.ModeratorGui.tables.Issues;
 import me.heldplayer.ModeratorGui.tables.Promotions;
 import me.heldplayer.ModeratorGui.tables.Unbans;
 
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class ModeratorGui extends JavaPlugin {
 
 	public static boolean isRunning = false;
 	public PluginDescriptionFile pdf;
 
 	@Override
 	public void onDisable() {
 		isRunning = false;
 
 		getLogger().info("Disabled!");
 	}
 
 	@Override
 	public void onEnable() {
 		setupDatabase();
 
 		pdf = getDescription();
 
 		getCommand("report").setExecutor(new ReportCommand(this));
 
 		isRunning = true;
 
 		getLogger().info("Enabled!");
 	}
 
 	private void setupDatabase() {
 		try {
 			getDatabase().find(Issues.class).findRowCount();
 		} catch (PersistenceException ex) {
 			getLogger().info("Installing database due to first time usage");
 			installDDL();
 		}
 	}
 
 	@Override
 	public List<Class<?>> getDatabaseClasses() {
 		List<Class<?>> list = new ArrayList<Class<?>>();
 		list.add(Issues.class);
 		list.add(Bans.class);
 		list.add(Unbans.class);
 		list.add(Promotions.class);
 		list.add(Demotions.class);
 		return list;
 	}
 }
