 package net.sradonia.bukkit.minecartmania.teleport;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.logging.Logger;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.server.PluginDisableEvent;
 import org.bukkit.event.server.PluginEnableEvent;
 import org.bukkit.event.server.ServerListener;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.afforess.minecartmaniacore.MinecartManiaCore;
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 public class MinecartManiaTeleport extends JavaPlugin {
 	private static final Logger log = Logger.getLogger("Minecraft");
 
 	private final static String TELEPORTERS_FILE = "Teleporters.dat";
 	private TeleporterList teleporters;
 
 	private PermissionHandler permissionHandler = null;
 
 	public void onEnable() {
 		final PluginDescriptionFile pdf = getDescription();
 		PluginManager pluginManager = getServer().getPluginManager();
 
 		// Get Minecart Mania Core plugin - should already be laoded by Bukkit
 		Plugin minecartMania = pluginManager.getPlugin("MinecartManiaCore");
 		if (minecartMania == null) {
 			log.severe(pdf.getName() + " requires MinecartManiaCore to function! Disabled.");
 			setEnabled(false);
 			return;
 		}
 
 		// Try to enable Permissions support
 		Plugin permissionsPlugin = pluginManager.getPlugin("Permissions");
 		if (permissionsPlugin != null && permissionsPlugin.isEnabled())
 			setPermissionsPlugin((Permissions) permissionsPlugin);
 
 		// Load teleporters
 		File teleporterFile = new File(MinecartManiaCore.dataDirectory, TELEPORTERS_FILE);
 		teleporters = new TeleporterList(teleporterFile);
 
 		if (teleporterFile.exists())
 			try {
 				int signCount = teleporters.load();
 				log.info("[" + pdf.getName() + "] Successfully loaded " + signCount + " teleporter signs");
 			} catch (IOException e) {
 				log.severe("[" + pdf.getName() + "] Error loading existing teleporters: " + e.getMessage());
 			}
 
 		// Register listeners
 		final ServerListener serverListener = new ServerListener() {
 			@Override
 			public void onPluginEnable(PluginEnableEvent event) {
				if (event.getPlugin().getDescription().getName().equals("Permissions"))
 					setPermissionsPlugin((Permissions) event.getPlugin());
 			}
 			@Override
 			public void onPluginDisable(PluginDisableEvent event) {
				if (event.getPlugin().getDescription().getName().equals("Permissions"))
 					setPermissionsPlugin(null);
 			}
 		};
 		pluginManager.registerEvent(Event.Type.PLUGIN_ENABLE, serverListener, Priority.Monitor, this);
 		pluginManager.registerEvent(Event.Type.PLUGIN_DISABLE, serverListener, Priority.Monitor, this);
 
 		final SignBlockListener blockListener = new SignBlockListener(this);
 		pluginManager.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Priority.Monitor, this);
 		pluginManager.registerEvent(Event.Type.BLOCK_PHYSICS, blockListener, Priority.High, this);
 		final SignPlayerListener playerListener = new SignPlayerListener(this);
 		pluginManager.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Priority.Normal, this);
 
 		final MinecartActionListener actionListener = new MinecartActionListener(this);
 		pluginManager.registerEvent(Event.Type.CUSTOM_EVENT, actionListener, Priority.Low, this);
 
 		log.info("[" + pdf.getName() + "] version " + pdf.getVersion() + " enabled!");
 	}
 
 	public void onDisable() {
 		// nothing to do
 	}
 
 	public TeleporterList getTeleporters() {
 		return teleporters;
 	}
 
 	private void setPermissionsPlugin(Permissions plugin) {
 		final PluginDescriptionFile pdf = getDescription();
 		if (plugin != null) {
 			permissionHandler = plugin.getHandler();
 			log.info("[" + pdf.getName() + "] Permissions support enabled! Remember to set the appropriate permissions!");
 		} else {
 			permissionHandler = null;
 			log.info("[" + pdf.getName() + "] Permissions support disabled!");
 		}
 	}
 
 	public boolean hasPermission(Player player, String permission) {
 		if (permissionHandler != null) {
 			return permissionHandler.has(player, permission);
 		} else {
 			// legacy: without Permissions everyone is allowed to do everything
 			return true;
 		}
 	}
 }
