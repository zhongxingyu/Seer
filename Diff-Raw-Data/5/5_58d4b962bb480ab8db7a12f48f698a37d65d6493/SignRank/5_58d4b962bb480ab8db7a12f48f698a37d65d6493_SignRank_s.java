 package me.coolblinger.signrank;
 
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 import com.platymuus.bukkit.permissions.PermissionsPlugin;
 import me.coolblinger.signrank.listeners.SignRankBlockListener;
 import me.coolblinger.signrank.listeners.SignRankPlayerListener;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.scheduler.BukkitScheduler;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.logging.Logger;
 
 public class SignRank extends JavaPlugin{
 	public Logger log = Logger.getLogger("Minecraft");
 	public boolean permissions3;
 	public PermissionHandler permissions;
 	private SignRankPlayerListener playerListener = new SignRankPlayerListener(this);
 	private SignRankBlockListener blockListener = new SignRankBlockListener(this);
 	public SignRankPermissionsBukkit permissionsBukkit = new SignRankPermissionsBukkit();
 	public SignRankConfig signRankConfig = new SignRankConfig();
 	public SignRankPermissionsBukkitYML signRankPermissionsBukkitYML = new SignRankPermissionsBukkitYML(this);
 
 	public void onDisable() {
 		
 	}
 
 	public void onEnable() {
 		PluginDescriptionFile pdFile = this.getDescription();
 		PluginManager pm = this.getServer().getPluginManager();
 		Plugin permissionsBukkitPlugin = pm.getPlugin("PermissionsBukkit");
 		if (permissionsBukkitPlugin == null) {
 			Plugin permissions3Plugin = pm.getPlugin("Permissions");
 			if (permissions3Plugin == null) {
 				log.severe("PermissionsBukkit nor Permissions3 could be found. SignRank will disable itself.");
 				this.setEnabled(false);
 				return;
 			} else {
 				if (!permissions3Plugin.getDescription().getVersion().contains("3.")) {
 					log.severe("PermissionsBukkit nor Permissions3 could be found. SignRank will disable itself.");
 					log.warning(permissions3Plugin.getDescription().getVersion());
 					this.setEnabled(false);
 					return;
 				} else {
 					permissions3 = true;
 					permissions = ((Permissions)permissions3Plugin).getHandler();
 				}
 			}
 		} else {
 			permissions3 = false;
 			permissionsBukkit.plugin = (PermissionsPlugin)permissionsBukkitPlugin;
 		}
 		pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.SIGN_CHANGE, blockListener, Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Event.Priority.Normal, this);
 		log.info(pdFile.getName() + " version " + pdFile.getVersion() + " loaded!");
 	}
 
 	public void updateCheck() throws IOException {
 		URL url = new URL("http://dl.dropbox.com/u/677732/uploads/SignRank.jar");
 		int urlSize = url.openConnection().getContentLength();
 		File pluginFile = new File("plugins" + File.separator + "SignRank.jar");
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
 
 	public boolean hasPermission(Player player, String permission) {
 		if (permissions3) {
 			return permissions.has(player, permission);
 		} else {
 			return player.hasPermission(permission);
 		}
 	}
 }
