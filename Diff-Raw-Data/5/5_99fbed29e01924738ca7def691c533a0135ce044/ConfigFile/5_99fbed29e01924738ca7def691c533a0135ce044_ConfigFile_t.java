 package net.crystalyx.bukkit.simplyperms.io;
 
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import net.crystalyx.bukkit.simplyperms.SimplyPlugin;
 
 public class ConfigFile implements PermsConfig {
 
 	private SimplyPlugin plugin;
 
 	public ConfigFile(SimplyPlugin plugin) {
 		this.plugin = plugin;
 	}
 
 	@Override
 	public void removePlayer(String player) {
 		player = player.toLowerCase();
 		plugin.getConfig().set("users/" + player, null);
 	}
 
 	@Override
 	public void removePlayerGroups(String player) {
 		player = player.toLowerCase();
 		plugin.getConfig().set("users/" + player + "/groups", null);
 	}
 
 	@Override
 	public void removePlayerGroup(String player, String group) {
 		player = player.toLowerCase();
 		List<String> groups = getPlayerGroups(player);
 		groups.remove(group);
 		plugin.getConfig().set("users/" + player + "/groups", (groups.isEmpty()) ? null : groups);
 	}
 
 	@Override
 	public void setPlayerGroup(String player, String group) {
 		removePlayerGroups(player);
 		addPlayerGroup(player, group);
 	}
 
 	@Override
 	public void addPlayerGroup(String player, String group) {
 		player = player.toLowerCase();
 		if (group.isEmpty()) return;
 		List<String> groups = getPlayerGroups(player);
 		if (!groups.contains(group)) groups.add(group);
 		plugin.getConfig().set("users/" + player + "/groups", groups);
 	}
 
 	@Override
 	public void addPlayerPermission(String player, String permission, boolean value) {
 		addPlayerPermission(player, "", permission, value);
 	}
 
 	@Override
 	public void addPlayerPermission(String player, String world, String permission, boolean value) {
 		player = player.toLowerCase();
 		Map<String, Boolean> permissions = getPlayerPermissions(player, world);
 		if (permissions.containsKey(permission)) permissions.remove(permission);
 		permissions.put(permission, value);
 		if (!world.isEmpty()) {
 			plugin.getConfig().set("users/" + player + "/worlds/" + world, permissions);
 		}
 		else {
 			plugin.getConfig().set("users/" + player + "/permissions", permissions);
 		}
 	}
 
 	@Override
 	public void removePlayerPermissions(String player) {
 		player = player.toLowerCase();
 		plugin.getConfig().set("users/" + player + "/permissions", null);
 	}
 
 	@Override
 	public void removePlayerPermission(String player, String permission) {
 		removePlayerPermission(player, "", permission);
 	}
 
 	@Override
 	public void removePlayerPermission(String player, String world, String permission) {
 		player = player.toLowerCase();
 		Map<String, Boolean> permissions = getPlayerPermissions(player, world);
 		permissions.remove(permission);
 		if (!world.isEmpty()) {
 			plugin.getConfig().set("users/" + player + "/worlds/" + world, (permissions.isEmpty()) ? null : permissions);
 		}
 		else {
 			plugin.getConfig().set("users/" + player + "/permissions", (permissions.isEmpty()) ? null : permissions);
 		}
 	}
 
 	@Override
 	public List<String> getPlayers(String group) {
 		List<String> players = new ArrayList<String>();
 		for (String player : getAllPlayers()) {
 			for (String groupName : getPlayerGroups(player)) {
 				if (groupName.equals(group)) {
 					players.add(player);
 				}
 			}
 		}
 		return players;
 	}
 
 	@Override
 	public List<String> getPlayerGroups(String player) {
 		player = player.toLowerCase();
 		return plugin.getConfig().getStringList("users/" + player + "/groups");
 	}
 
 	@Override
 	public Map<String, Boolean> getPlayerPermissions(String player) {
 		return getPlayerPermissions(player, "");
 	}
 
 	@Override
 	public Map<String, Boolean> getPlayerPermissions(String player, String world) {
 		player = player.toLowerCase();
 		Map<String, Boolean> finalPerms = new LinkedHashMap<String, Boolean>();
 		String permNode = (!world.isEmpty()) ? "users/" + player + "/worlds/" + world : "users/" + player + "/permissions";
 		if (plugin.getNode(permNode) != null) {
 			for (Entry<String, Object> permPlayer : plugin.getNode(permNode).getValues(false).entrySet()) {
				finalPerms.put(permPlayer.getKey(), Boolean.valueOf(permPlayer.getValue().toString()));
 			}
 		}
 		return finalPerms;
 	}
 
 	@Override
 	public boolean isPlayerInDB(String player) {
 		player = player.toLowerCase();
 		return plugin.getNode("users/" + player) != null;
 	}
 
 	@Override
 	public List<String> getPlayerWorlds(String player) {
 		player = player.toLowerCase();
 		if (plugin.getNode("users/" + player + "/worlds") != null) {
 			return new ArrayList<String>(plugin.getNode("users/" + player + "/worlds").getKeys(false));
 		}
 		else {
 			return new ArrayList<String>();
 		}
 	}
 
 	@Override
 	public List<String> getAllPlayers() {
 		if (plugin.getNode("users") != null) {
 			return new ArrayList<String>(plugin.getNode("users").getKeys(false));
 		}
 		else {
 			return new ArrayList<String>();
 		}
 	}
 
 	@Override
 	public List<String> getAllGroups() {
 		if (plugin.getNode("groups") != null) {
 			return new ArrayList<String>(plugin.getNode("groups").getKeys(false));
 		}
 		else {
 			return new ArrayList<String>();
 		}
 	}
 
 	@Override
 	public List<String> getGroupWorlds(String group) {
 		if (group.isEmpty()) group = getDefaultGroup();
 		if (plugin.getNode("groups/" + group + "/worlds") != null) {
 			return new ArrayList<String>(plugin.getNode("groups/" + group + "/worlds").getKeys(false));
 		}
 		else {
 			return new ArrayList<String>();
 		}
 	}
 
 	@Override
 	public List<String> getGroupInheritance(String group) {
 		if (group.isEmpty()) group = getDefaultGroup();
 		return plugin.getConfig().getStringList("groups/" + group + "/inheritance");
 	}
 
 	@Override
 	public void addGroupInheritance(String group, String inherit) {
 		if (group.isEmpty()) group = getDefaultGroup();
 		List<String> inheritances = getGroupInheritance(group);
 		if (!inheritances.contains(inherit)) inheritances.add(inherit);
 		plugin.getConfig().set("groups/" + group + "/inheritance", inheritances);
 	}
 
 	@Override
 	public void removeGroupInheritance(String group, String inherit) {
 		if (group.isEmpty()) group = getDefaultGroup();
 		List<String> inheritances = getGroupInheritance(group);
 		inheritances.remove(inherit);
 		plugin.getConfig().set("groups/" + group + "/inheritance", inheritances);
 	}
 
 	@Override
 	public void removeGroupInheritances(String group) {
 		if (group.isEmpty()) group = getDefaultGroup();
 		plugin.getConfig().set("groups/" + group + "/inheritance", null);
 	}
 
 	@Override
 	public Map<String, Boolean> getGroupPermissions(String group, String world) {
 		if (group.isEmpty()) group = getDefaultGroup();
 		Map<String, Boolean> finalPerms = new LinkedHashMap<String, Boolean>();
 		String permNode = (!world.isEmpty()) ? "groups/" + group + "/worlds/" + world : "groups/" + group + "/permissions";
 		if (plugin.getNode(permNode) != null) {
 			for (Entry<String, Object> permGroup : plugin.getNode(permNode).getValues(false).entrySet()) {
				finalPerms.put(permGroup.getKey(), Boolean.valueOf(permGroup.getValue().toString()));
 			}
 		}
 		return finalPerms;
 	}
 
 	@Override
 	public Map<String, Boolean> getGroupPermissions(String group) {
 		return getGroupPermissions(group, "");
 	}
 
 	@Override
 	public void addGroupPermission(String group, String world, String permission, boolean value) {
 		if (group.isEmpty()) group = getDefaultGroup();
 		Map<String, Boolean> permissions = getGroupPermissions(group, world);
 		if (permissions.containsKey(permission)) permissions.remove(permission);
 		permissions.put(permission, value);
 		if (!world.isEmpty()) {
 			plugin.getConfig().set("groups/" + group + "/worlds/" + world, permissions);
 		}
 		else {
 			plugin.getConfig().set("groups/" + group + "/permissions", permissions);
 		}
 	}
 
 	@Override
 	public void addGroupPermission(String group, String permission, boolean value) {
 		addGroupPermission(group, "", permission, value);
 	}
 
 	@Override
 	public void removeGroupPermission(String group, String world, String permission) {
 		if (group.isEmpty()) group = getDefaultGroup();
 		Map<String, Boolean> permissions = getGroupPermissions(group, world);
 		permissions.remove(permission);
 		if (!world.isEmpty()) {
 			plugin.getConfig().set("groups/" + group + "/worlds/" + world, (permissions.isEmpty()) ? null : permissions);
 		}
 		else {
 			plugin.getConfig().set("groups/" + group + "/permissions", (permissions.isEmpty()) ? null : permissions);
 		}
 	}
 
 	@Override
 	public void removeGroupPermission(String group, String permission) {
 		removeGroupPermission(group, "", permission);
 	}
 
 	@Override
 	public void removeGroupPermissions(String group) {
 		if (group.isEmpty()) group = getDefaultGroup();
 		plugin.getConfig().set("groups/" + group + "/permissions", null);
 	}
 
 	@Override
 	public void removeGroup(String group) {
 		plugin.getConfig().set("groups/" + group, null);
 	}
 
 	@Override
 	public Map<String, Object> getMessages() {
 		if (plugin.getNode("messages") != null) {
 			return plugin.getNode("messages").getValues(false);
 		}
 		else {
 			return new LinkedHashMap<String, Object>();
 		}
 	}
 
 	@Override
 	public String getMessage(String key) {
 		return plugin.getConfig().getString("messages/" + key, plugin.getConfig().getString("messages/all", "")).replace('&', '\u00A7');
 	}
 
 	@Override
 	public void addMessage(String key, String message) {
 		Map<String, Object> messages = getMessages();
 		if (!messages.containsKey(key)) messages.put(key, message);
 		plugin.getConfig().set("messages", messages);
 	}
 
 	@Override
 	public void removeMessage(String key) {
 		Map<String, Object> messages = getMessages();
 		messages.remove(key);
 		plugin.getConfig().set("messages", messages);
 	}
 
 	@Override
 	public String getDefaultGroup() {
 		return plugin.getConfig().getString("default", "default");
 	}
 
 	@Override
 	public void setDefaultGroup(String group) {
 		if (group.isEmpty()) group = "default";
 		plugin.getConfig().set("default", group);
 	}
 
 	@Override
 	public boolean getDebug() {
 		return plugin.getConfig().getBoolean("debug", true);
 	}
 
 	@Override
 	public void setDebug(boolean debug) {
 		plugin.getConfig().set("debug", debug);
 	}
 
 }
