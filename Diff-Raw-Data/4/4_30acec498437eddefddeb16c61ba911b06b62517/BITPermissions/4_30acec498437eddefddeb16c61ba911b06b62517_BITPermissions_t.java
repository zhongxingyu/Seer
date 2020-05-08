 package dk.gabriel333.Library;
 
 import org.anjocaido.groupmanager.GroupManager;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.plugin.Plugin;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 //import Permissions 3 classes
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 //import bPermissions
 import de.bananaco.permissions.worlds.WorldPermissionsManager;
 
 //import PermissionsEx classes
 import ru.tehkode.permissions.PermissionManager;
 import ru.tehkode.permissions.bukkit.PermissionsEx;
 
 public class BITPermissions {
 
 	public static String PERMISSION_NODE;
 	public final static Boolean QUIET = false;
 	public final static Boolean NOT_QUIET = true;
 
 	// Hook into Permissions 3.xxx
 	private static Plugin permissions3Plugin;
 	private static PermissionHandler permission3Handler;
 	public static Boolean permissions3 = false;
 
 	// Hook into PermissionsBukkit
 	private static Plugin permissionsBukkitPlugin;
 	public static Boolean permissionsBukkit = false;
 
 	// Hook into PermissionsEx
 	private static PermissionManager permissionsexManager;
 	public static Boolean permissionsex = false;
 
 	// Hook into bPermissions
 	public static WorldPermissionsManager wpm = null;
 	public static Boolean bPermissions = false;
 
 	// Hook into Essentials GroupManager
 	private static Plugin groupManagerPlugin;
 	public static GroupManager groupManager;
 	public static Boolean essentialsGroupManager = false;
 
 	// Initialize all permissionsplugins
 	public static void setupPermissions(Plugin plugin) {
 		PERMISSION_NODE = plugin.getDescription().getName() + ".";
 		if (permissions3 || permissionsBukkit || permissionsex || bPermissions) {
 			BITMessages
 					.showWarning("Your permission system is allready detected!");
 			return;
 		} else {
 			// PermissionsBukkit
 			permissionsBukkitPlugin = plugin.getServer().getPluginManager()
 					.getPlugin("PermissionsBukkit");
 			if (permissionsBukkitPlugin != null) {
 				permissionsBukkit = true;
 				BITMessages.showInfo("PermissionsBukkit is detected.");
 			} else
 
 			// PermissionEx
 			if (Bukkit.getServer().getPluginManager()
 					.isPluginEnabled("PermissionsEx")) {
 				permissionsexManager = PermissionsEx.getPermissionManager();
 				BITMessages.showInfo("PermissionsEx is detected.");
 				permissionsex = true;
 			} else
 
 			// bPermissions
 			if (Bukkit.getServer().getPluginManager()
 					.isPluginEnabled("bPermissions")) {
 
 				try {
 					wpm = de.bananaco.permissions.Permissions
 							.getWorldPermissionsManager();
 					bPermissions = true;
 					BITMessages.showInfo("bPermissions is detected.");
 				} catch (Exception e) {
 
 				}
 			} else
 			// Essentials GroupManager
 			if (Bukkit.getServer().getPluginManager()
 					.isPluginEnabled("GroupManager")) {
 				groupManagerPlugin = Bukkit.getServer().getPluginManager()
 						.getPlugin("GroupManager");
 				//if (groupManagerPlugin != null) {
 					groupManager = (GroupManager) groupManagerPlugin;
 					BITMessages.showInfo("Essentials GroupManager is detected.");
 					essentialsGroupManager = true;
 				//}
 
 			} else
 			// Permission3
 			if (permissions3Plugin == null) {
 				permissions3Plugin = plugin.getServer().getPluginManager()
 						.getPlugin("Permissions");
 				if (permissions3Plugin != null) {
 					permission3Handler = ((Permissions) permissions3Plugin)
 							.getHandler();
 					permissions3 = true;
 					BITMessages
 							.showInfo("Permissions3/SuperpermBridge is detected. "
 									+ ((Permissions) permissions3Plugin)
 											.getDescription().getFullName());
 				}
 			}
 
 			// No permission systems found
 			if (!(permissions3 || permissionsBukkit || permissionsex || bPermissions || essentialsGroupManager)) {  // Added lookup for EssentialGroupManager 2/6/12
 				BITMessages.showInfo("No permissions system found, Defaulting to build-in permissions.");
 				return;
 			}
 		}
 	}
 
 	// Test if the player has permissions to do the action
 	public static boolean hasPerm(CommandSender sender, String label,
 			Boolean not_quiet) {
 		if (BITConfig.DEBUG_PERMISSIONS) {
 			sender.sendMessage("Testing permission: "
 					+ (PERMISSION_NODE + label).toLowerCase());
 		}
 
 		// How to hook into PermissionsBukkit
 		// Basic Permission Check
 		// In this example (MyPlugin) is meant to represent the name of your
 		// plugin,
 		// for example... iConomy would look like:
 		// Player player = (Player) sender;
 		// if (player.hasPermission("a.custom.node") {
 		// return true;
 		// }
 
 		// How to hook into Permissions 3.1.6
 		// Basic Permission Check
 		// In this example (MyPlugin) is meant to represent the name of your
 		// plugin,
 		// for example... iConomy would look like:
 		// if (!(MyPlugin).permissionHandler.has(player, "a.custom.node")) {
 		// return;
 		// }
 		// Checking if a user belongs to a group
 		// if (!(MyPlugin).permissionHandler.inGroup(world, name, groupName)) {
 		// return;
 		// }
 
 		// Permission check
 		// if(permissions.has(player, "yourplugin.permission")){
 		// yay!
 		// } else {
 		// houston, we have a problems :)
 		// }
 		SpoutPlayer sPlayer = (SpoutPlayer) sender;
 		Boolean hasPermission = false;
 
 		// PermissionsBukkit system (with fallback builtin Permission system)
 		if (permissionsBukkit) {
 			hasPermission = (sPlayer.hasPermission((PERMISSION_NODE + label)
 					.toLowerCase()) || sPlayer
 					.hasPermission((PERMISSION_NODE + "*").toLowerCase()));
 		} else if (permissionsex) {
 			hasPermission = permissionsexManager.has(sPlayer,
 					(PERMISSION_NODE + label).toLowerCase());
 		} else if (bPermissions) {
 			//TODO: fix bPermissions.
			hasPermission = wpm.getPermissionSet(sPlayer.getWorld()).has(
					sPlayer, (PERMISSION_NODE + label).toLowerCase());  // Fixed by Dockter
 		} else if (essentialsGroupManager) {
 			// Essentials GroupManager
 			hasPermission = groupManager.getWorldsHolder()
 					.getWorldPermissions(sPlayer)
 					.has(sPlayer, (PERMISSION_NODE + label).toLowerCase());
 		} else if (permissions3) {
 			// or SuperpermBridge
 			hasPermission = permission3Handler.has(sPlayer,
 					(PERMISSION_NODE + label).toLowerCase());
 		} else {
 			// fallback builtin Permission system
 			hasPermission = sPlayer.hasPermission((PERMISSION_NODE + label)
 					.toLowerCase());
 		}
 
 		// return permission
 		if (BITConfig.DEBUG_PERMISSIONS)
 			sPlayer.sendMessage(ChatColor.RED + "The result is:"
 					+ hasPermission);
 		if (hasPermission) {
 			if (BITConfig.DEBUG_PERMISSIONS)
 				sPlayer.sendMessage(ChatColor.GREEN
 						+ "G333Permissions: You have permission to: "
 						+ (PERMISSION_NODE + label).toLowerCase());
 			return true;
 		} else if (not_quiet) {
 			if (BITConfig.DEBUG_PERMISSIONS) {
 				sPlayer.sendMessage(ChatColor.RED
 						+ "You to dont have permission to do this." + " ("
 						+ (BITPlugin.PLUGIN_NAME + "." + label).toLowerCase()
 						+ ")");
 			} else {
 				sPlayer.sendMessage(ChatColor.RED
 						+ "You to dont have permission to do this.");
 			}
 		}
 
 		return false;
 	}
 }
