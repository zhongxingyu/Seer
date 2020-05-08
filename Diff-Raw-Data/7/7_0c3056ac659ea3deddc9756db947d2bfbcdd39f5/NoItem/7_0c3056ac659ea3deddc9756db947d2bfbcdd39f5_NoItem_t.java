 package net.worldoftomorrow.nala.ni;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import net.worldoftomorrow.nala.ni.CustomItems.CustomBlockLoader;
 import net.worldoftomorrow.nala.ni.listeners.CommandListener;
 import net.worldoftomorrow.nala.ni.listeners.DebugListeners;
 import net.worldoftomorrow.nala.ni.listeners.EventListener;
 
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class NoItem extends JavaPlugin {
 
 	private Config config;
 	protected Vault vault;
 	private static NoItem plugin;
 	private Metrics metrics;
 	public DebugListeners debugListener;
 
 	private Map<String, List<ItemStack>> itemList = new HashMap<String, List<ItemStack>>();
 
 	@Override
 	public void onEnable() {
		setupStatic(this);
 		this.config = new Config(this);
 		new CustomBlockLoader(this).load();
 		this.vault = new Vault(this);
 		CommandListener cl = new CommandListener(this);
 		this.getCommand("noitem").setExecutor(cl);
 
 		if(Config.getBoolean("CheckForUpdates")) {
 			String slug = "NoItem";
 			if(Config.getBoolean("Auto-Download-Updates")) {
 				new AutoUpdater(this, slug, this.getFile(), AutoUpdater.UpdateType.DEFAULT, false);
 			} else {
 				new AutoUpdater(this, slug, this.getFile(), AutoUpdater.UpdateType.NO_DOWNLOAD, false);
 			}
 		}
 
 		PluginManager pm = getServer().getPluginManager();
 
 		pm.registerEvents(new EventListener(this), this);
 		if (Config.getBoolean("Debugging")) {
 			this.debugListener = new DebugListeners();
 			pm.registerEvents(this.debugListener, this);
 			Log.info("This plugin is in debugging mode!");
 		}
 
 		try {
 			metrics = new Metrics(this);
 			metrics.start();
 		} catch (IOException ex) {
 			Log.severe("Failed to start metrics!");
 		}
 	}
	
	private static void setupStatic(NoItem plugin) {
		NoItem.plugin = plugin;
	}
 
 	public static NoItem getPlugin() {
 		return plugin;
 	}
 
 	public Vault getVault() {
 		return vault;
 	}
 
 	public Config getConfigClass() {
 		return this.config;
 	}
 
 	public Map<String, List<ItemStack>> getItemList() {
 		return this.itemList;
 	}
 }
