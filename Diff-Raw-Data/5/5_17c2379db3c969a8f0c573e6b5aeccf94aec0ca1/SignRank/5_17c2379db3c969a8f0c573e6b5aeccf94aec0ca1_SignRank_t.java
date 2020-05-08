 package me.coolblinger.signrank;
 
 import com.nijiko.permissions.PermissionHandler;
 import com.platymuus.bukkit.permissions.PermissionsPlugin;
 import de.bananaco.permissions.Permissions;
 import de.bananaco.permissions.worlds.WorldPermissionsManager;
 import me.coolblinger.signrank.listeners.SignRankBlockListener;
 import me.coolblinger.signrank.listeners.SignRankPlayerListener;
 import org.anjocaido.groupmanager.GroupManager;
 import org.bukkit.configuration.Configuration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.scheduler.BukkitScheduler;
 import ru.tehkode.permissions.PermissionManager;
 import ru.tehkode.permissions.bukkit.PermissionsEx;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.logging.Logger;
 
 public class SignRank extends JavaPlugin{
 	private final Logger log = Logger.getLogger("Minecraft");
 	public String pluginName;
 	public PermissionHandler permissions;
 	public GroupManager gm;
 	public PermissionManager pex;
 	public WorldPermissionsManager bp;
 	private final SignRankPlayerListener playerListener = new SignRankPlayerListener(this);
 	private final SignRankBlockListener blockListener = new SignRankBlockListener(this);
 	public final SignRankPermissionsBukkit permissionsBukkit = new SignRankPermissionsBukkit();
 	public final SignRankPermissionsBukkitYML signRankPermissionsBukkitYML = new SignRankPermissionsBukkitYML(this);
 
 	public void onDisable() {
 		log.info("SignRank has been disabled.");
 	}
 
 	public void onEnable() {
 		PluginDescriptionFile pdFile = this.getDescription();
 		PluginManager pm = this.getServer().getPluginManager();
 		Plugin permissionsBukkitPlugin = pm.getPlugin("PermissionsBukkit");
 		Plugin groupManagerPlugin = pm.getPlugin("GroupManager");
 		Plugin permissions3Plugin = pm.getPlugin("Permissions");
 		Plugin permissionsExPlugin = pm.getPlugin("PermissionsEx");
 		Plugin bPermissionsPlugin = pm.getPlugin("bPermissions");
 		if (permissionsBukkitPlugin != null) {
 			pluginName = "PermissionsBukkit";
 			permissionsBukkit.plugin = (PermissionsPlugin) permissionsBukkitPlugin;
 		} else if (groupManagerPlugin != null) {
 			pluginName = "GroupManager";
 			gm = (GroupManager) groupManagerPlugin;
 		} else if (permissionsExPlugin != null) {
 			pluginName = "PermissionsEx";
 			pex = PermissionsEx.getPermissionManager();
 		} else if (bPermissionsPlugin != null) {
 			pluginName = "bPermissions";
 			bp = Permissions.getWorldPermissionsManager();
 		} else if (permissions3Plugin != null) {
 			com.nijikokun.bukkit.Permissions.Permissions permissionsPlugin = (com.nijikokun.bukkit.Permissions.Permissions) permissions3Plugin;
 			if (permissions3Plugin.getDescription().getVersion().startsWith("3.") && permissionsPlugin.getHandler() instanceof PermissionHandler) {
 				pluginName = "Permissions3";
 				permissions = permissionsPlugin.getHandler();
 			} else {
				log.severe("No support Permissions plugin has been found, SignRank will disable itself.");
 				setEnabled(false);
 				return;
 			}
 		} else {
			log.severe("No supported Permissions plugin has been found, SignRank will disable itself.");
 			setEnabled(false);
 			return;
 		}
 		pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.SIGN_CHANGE, blockListener, Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Event.Priority.Normal, this);
 		log.info(pdFile.getName() + " version " + pdFile.getVersion() + " loaded!");
 		try {
 			updateCheck();
 		} catch (Exception e) {
 			log.severe("SignRank could not check for updates.");
 		}
 		initConfig();
 	}
 
 	void updateCheck() throws IOException {
 		URL url = new URL("http://dl.dropbox.com/u/677732/uploads/SignRank.jar");
 		int urlSize = url.openConnection().getContentLength();
 		File pluginFile = getFile();
 		if (!pluginFile.exists()) {
 			log.severe("SignRank has not been installed correctly");
 			return;
 		}
 		long pluginFileSize = pluginFile.length();
 		if (urlSize != pluginFileSize) {
 			BukkitScheduler bScheduler = this.getServer().getScheduler();
 			bScheduler.scheduleSyncDelayedTask(this, new Runnable() {
 				public void run() {
 					log.warning("There has been an update for SignRank.");
 				}
 			}, 600);
 		}
 	}
 
 	public void initConfig() {
 		YamlConfiguration config = (YamlConfiguration) getConfig();
 		config.options().header("'signText' is the text that has to be on the first line of the sign to in order for it to be a SignRankSign.\n" +
 				"You have to manually set the groups for every world when using either Permissions3, Groupmanager or bPermissions.\n" +
 				"Those groups are ignored when 'bypassGroupCheck' is true, players will then be promoted to the group specified on the second line of the sign.");
 		if (config.get("PermissionsBukkit.toGroup") == null) {
 			config.set("PermissionsBukkit.toGroup", "user");
 			config.set("MultiWorld.worldName", "groupName");
 		}
 		if (config.get("signText") == null) {
 			config.set("signText", "[SignRank]");
 		}
 		if (config.get("bypassGroupCheck") == null) {
 			config.set("bypassGroupCheck", false);
 		}
 		if (config.get("messages.rankUp") == null) {
 			config.set("messages.rankUp", "You've been promoted to '%group%'.");
 		}
 		if (config.get("messages.deny") == null) {
 			config.set("messages.deny", "You're already in the '%group%' group.");
 		}
 		saveConfig();
 	}
 
 	public String readString(String path) {
 		Configuration config = getConfig();
 		return config.getString(path);
 	}
 
 	public boolean readBoolean(String path) {
 		Configuration config = getConfig();
 		return config.getBoolean(path, false);
 	}
 
 	public boolean hasPermission(Player player) {
 		if (pluginName.equals("Permissions3")) {
 			return permissions.has(player, "signrank.build");
 		} else if (pluginName.equals("PermissionsBukkit")) {
 			return player.hasPermission("signrank.build");
 		} else if (pluginName.equals("GroupManager")) {
 			return gm.getWorldsHolder().getWorldPermissions(player).has(player, "signrank.build");
 		} else if (pluginName.equals("PermissionsEx")) {
 			return pex.has(player, "signrank.build");
 		} else if (pluginName.equals("bPermissions")) {
 			return bp.getPermissionSet(player.getWorld()).has(player, "signrank.build");
 		} else {
 			return false;
 		}
 	}
 }
