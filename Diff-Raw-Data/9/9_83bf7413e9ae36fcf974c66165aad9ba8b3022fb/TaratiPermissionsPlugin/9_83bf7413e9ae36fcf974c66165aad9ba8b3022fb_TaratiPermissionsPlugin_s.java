 package com.schneenet.tarati;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.config.Configuration;
 
 import ru.tehkode.permissions.PermissionGroup;
 import ru.tehkode.permissions.PermissionManager;
 import ru.tehkode.permissions.bukkit.PermissionsEx;
 
 import com.schneenet.tarati.database.DatabaseException;
 import com.schneenet.tarati.database.DatabaseHandler;
 import com.schneenet.tarati.database.ForumUser;
 import com.schneenet.tarati.database.TaratiMySQLHandler;
 
 public class TaratiPermissionsPlugin extends JavaPlugin {
 
 	private PermissionManager permissionManager;
 	private HashMap<Integer, TaratiPermissionGroup> groupMapping = new HashMap<Integer, TaratiPermissionGroup>();
 	private DatabaseHandler databaseHandler;
 	private TaratiPermissionsCommandHandler commandHandler;
 
 	private String notRegisteredKickMessage;
 	private String groupNotAllowedKickMessage;
 	private boolean useWhitelist;
 
 	private String pluginName;
 	private String pluginVersion;
 
 	@Override
 	public void onDisable() {
 		TaratiPermissionsLogger.info("Disabling " + pluginName + " v" + pluginVersion);
 		TaratiPermissionsLogger.info("Disabled " + pluginName + " v" + pluginVersion);
 	}
 
 	@Override
 	public void onEnable() {
 
 		// Basic Plugin Setup
 		pluginName = this.getDescription().getName();
 		pluginVersion = this.getDescription().getVersion();
 		TaratiPermissionsLogger.info("Enabling " + pluginName + " v" + pluginVersion);
 
 		// Hook into PermissionsEx
 		Plugin permissionsEx = this.getServer().getPluginManager().getPlugin("PermissionsEx");
 		if (permissionsEx != null) {
 			permissionManager = PermissionsEx.getPermissionManager();
 			TaratiPermissionsLogger.info("Hooked into " + permissionsEx.getDescription().getName() + " (v" + permissionsEx.getDescription().getVersion() + ").");
 		} else {
 			TaratiPermissionsLogger.warn("PermissionsEx plugin was not found or enabled.");
 		}
 
 		// Load Configuration
 		Configuration config = this.getConfiguration();
 
 		// Database configs
 		databaseHandler = new TaratiMySQLHandler();
 
 		try {
 			databaseHandler.initialize(config.getString("bridge.database.uri"), config.getString("bridge.database.user"), config.getString("bridge.database.password"), config.getString("bridge.database.userTable"), config.getString("bridge.database.userIdCol"), config.getString("bridge.database.userNameCol"), config.getString("bridge.database.ignCol"), config.getString("bridge.database.groupIdCol"));
 			String customLookupQuery = config.getString("bridge.database.customlookupquery", null);
 			if (customLookupQuery != null) {
 				databaseHandler.setLookupQuery(customLookupQuery);
 			}
 		} catch (DatabaseException e) {
 			TaratiPermissionsLogger.severe(e.getMessage(), e);
 			// The plugin is now useless, shut it down
 			this.getPluginLoader().disablePlugin(this);
 		}
 
 		// Group mapping
 		// Iterate over all specified groups from PermissionsEx looking for the configuration node:
 		// bridge.groups.<group>.gid
 		PermissionGroup[] groups = permissionManager.getGroups();
 		int size = groups.length;
 		for (int i = 0; i < size; i++) {
 			int gid = config.getInt("bridge.groups." + groups[i].getName() + ".gid", -1);
 			boolean whitelisted = config.getBoolean("bridge.groups." + groups[i].getName() + ".whitelist", false);
 			if (gid < 0) {
 				TaratiPermissionsLogger.warn("No forum mapping for group '" + groups[i].getName() + "'!");
 			} else {
 				this.groupMapping.put(gid, new TaratiPermissionGroup(gid, groups[i].getName(), whitelisted));
 			}
 		}
 
 		// Kick messages
 		this.notRegisteredKickMessage = config.getString("bridge.kickmessage.notregistered", "Please register on our forum to play on our server!");
 		this.groupNotAllowedKickMessage = config.getString("bridge.kickmessage.groupnotallowed", "You are not allowed on our server at this time!");
 
 		// Whitelisting
 		this.useWhitelist = config.getBoolean("bridge.whitelist.enable", false);
 
 		// TODO Other configuration options
 
 		// Register commandHandler
 		this.commandHandler = new TaratiPermissionsCommandHandler(this);
 
 		// Register playerListener
 		this.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_JOIN, new TaratiPermissionsPlayerListener(this), Priority.Lowest, this);

 		TaratiPermissionsLogger.info("Enabled " + pluginName + " v" + pluginVersion);
 
 	}
 
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 		return this.commandHandler.onCommand(sender, command, label, args);
 	}
 
 	public void processPlayer(Player p) {
 		try {
 			// Perform that actual database lookup ASAP
 			ForumUser forumUser = this.databaseHandler.lookupForumUser(p.getName());
 			if (forumUser != null) {
 				// The user was registered on the forum!
 				if (groupMapping.containsKey(forumUser.getGroupId())) {
 					// The user is in a forum group that is specified in the
 					// configuration
 					// Get that group:
 					TaratiPermissionGroup tGroup = groupMapping.get(forumUser.getGroupId());
 					// Tell PEX they should be in that group:
 					String[] newGroups = { tGroup.getPexName() };
 					permissionManager.getUser(p).setGroups(newGroups);
 					// If we are using a white list, and the group is not white
 					// listed, kick them
 					if (this.useWhitelist && !tGroup.isWhitelisted()) {
 						p.kickPlayer(this.groupNotAllowedKickMessage);
 					}
 				} else if (this.useWhitelist) {
 					// The user is in a forum group that is not specified in the
 					// configuration
 					// If we are using a white list, kick them
 					p.kickPlayer(this.groupNotAllowedKickMessage);
 				}
 			} else if (this.useWhitelist) {
 				// The user is not registered, if we are using a white list,
 				// kick them.
 				p.kickPlayer(this.notRegisteredKickMessage);
 			}
 		} catch (DatabaseException e1) {
 			// Should the database error out, they will be set to the default
 			// group by PEX. (Hopefully, that group has no permissions)
 			TaratiPermissionsLogger.severe(e1.getMessage(), e1);
 		}
 	}
 
 	public PermissionManager getPermissionManager() {
 		return this.permissionManager;
 	}
 
 	public Map<Integer, TaratiPermissionGroup> getGroupMapping() {
 		return this.groupMapping;
 	}
 
 	public String getNotRegisteredKickMessage() {
 		return this.notRegisteredKickMessage;
 	}
 
 	public String getGroupNotAllowedKickMessage() {
 		return this.groupNotAllowedKickMessage;
 	}
 
 	public DatabaseHandler getDatabaseHandler() {
 		return this.databaseHandler;
 	}
 
 	public boolean getUseWhitelist() {
 		return this.useWhitelist;
 	}
 
 }
