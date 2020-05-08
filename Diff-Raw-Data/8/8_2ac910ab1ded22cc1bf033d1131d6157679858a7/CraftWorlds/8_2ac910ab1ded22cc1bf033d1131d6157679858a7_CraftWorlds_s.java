 package me.furt.craftworlds;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import javax.persistence.PersistenceException;
 
 import me.furt.craftworlds.commands.WorldCommand;
 import me.furt.craftworlds.listeners.CWPlayerListener;
 import me.furt.craftworlds.sql.WorldTable;
 
 import org.bukkit.World;
 import org.bukkit.World.Environment;
 import org.bukkit.WorldCreator;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class CraftWorlds extends JavaPlugin {
 	public CWPlayerListener PlayerListener = new CWPlayerListener(this);
 	public boolean permEnabled;
 
 	@Override
 	public void onDisable() {
 		PluginDescriptionFile pdfFile = this.getDescription();
 		this.logger(Level.INFO, "v" + pdfFile.getVersion() + " Disabled");
 	}
 
 	@Override
 	public void onEnable() {
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvents(PlayerListener, this);
 		this.setupDatabase();
 		this.loadWorlds();
		getCommand("world").setExecutor(new WorldCommand(this));
 		PluginDescriptionFile pdfFile = this.getDescription();
 		this.logger(Level.INFO, "v" + pdfFile.getVersion() + " Enabled");
 
 	}
 
 	private void loadWorlds() {
 		List<WorldTable> getWorld = getDatabase().find(WorldTable.class)
 				.where().eq("worldEnabled", true).findList();
 		int count = getWorld.size();
 		for (int i = 0; i < count; i++) {
 			String wName = getWorld.get(i).getWorldName();
 			String env = getWorld.get(i).getEnvironment();
 			long seed = getWorld.get(i).getSeed();
 			boolean pvp = getWorld.get(i).isPvpEnabled();
 			WorldCreator wc = WorldCreator.name(wName);
 			wc.environment(Environment.valueOf(env));
 			if (seed != 0) {
 				wc.seed(seed);
 			}
 			this.getServer().createWorld(wc);
 			World world = this.getServer().getWorld(wName);
 			world.setPVP(pvp);
 			this.getServer().broadcastMessage(
 					"[CraftWorlds] Map: " + wName + " loaded.");
 		}
 
 	}
 
 	private void setupDatabase() {
 		try {
 			File ebeans = new File("ebean.properties");
 			if (!ebeans.exists()) {
 				try {
 					ebeans.createNewFile();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 			getDatabase().find(WorldTable.class).findRowCount();
 		} catch (PersistenceException ex) {
 			this.logger(Level.INFO, "Installing database.");
 			installDDL();
 		}
 	}
 
 	@Override
 	public List<Class<?>> getDatabaseClasses() {
 		List<Class<?>> list = new ArrayList<Class<?>>();
 		list.add(WorldTable.class);
 		return list;
 	}
 
 	public boolean isPlayer(CommandSender sender) {
 		if (!(sender instanceof Player)) {
 			return false;
 		} else {
 			return true;
 		}
 	}
 
 	public boolean hasPerm(CommandSender sender, String cmd,
 			boolean consoleUse) {
 		boolean perm = sender.hasPermission("craftworld." + cmd);
 
 		if (this.console(sender)) {
 			if (consoleUse)
 				return true;
 
 			this.logger(Level.INFO, "This command cannot be used in console.");
 			return false;
 		} else {
 			if (sender.isOp())
 				return true;
 
 			return perm;
 		}
 	}
 
 	public boolean console(CommandSender sender) {
 		if (sender instanceof Player) {
 			return false;
 		}
 		// Needs more checks
 		return true;
 	}
 	
 	public void logger(Level l, String s) {
 		this.getLogger().log(l, "[CraftWorlds] " + s);
 	}
 
 }
