 package com.Baummann.SetRank;
 
 
 import java.io.File;
 import java.util.LinkedHashSet;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.config.Configuration;
 
 import com.nijiko.permissions.Entry;
 import com.nijiko.permissions.Group;
 import com.nijiko.permissions.PermissionHandler;
 import com.nijiko.permissions.User;
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 public class SetRank extends JavaPlugin {
 	public static PermissionHandler permissionHandler;
 	public static boolean broadcastMessage;
 	public static boolean broadcastRankOnLogin;
 	private final spl playerListener = new spl(this);
 	public static String ownerName;
 	
     public void println(String str) {
     	System.out.println("[SetRank] " + str);
     }
     
     public void message(Player player, String str) {
     	player.sendMessage(ChatColor.DARK_RED + str);
     }
     
     public void broadcast(String str) {
     	getServer().broadcastMessage(ChatColor.DARK_RED + str);
     }
     
     public void setupPermissions() {
     	Plugin perm = getServer().getPluginManager().getPlugin("Permissions");
     	
     	if (permissionHandler == null) {
     		if (perm != null) {
     			println("Permissions system detected!");
     			SetRank.permissionHandler = ((Permissions) perm).getHandler();
     		} else {
     			println("Permissions system not detected!");
     			println("The plugin will be deleted as soon as the server goes offline!");
     			new File("plugins/SetRank.jar").deleteOnExit();
     			new File("plugins/SetRank").deleteOnExit();
     		}
     	} else {
     		println("The permission handler is already intialized!");
     	}
     }
     
     public boolean canUseCommand(Player player, String node) {
     	if (SetRank.permissionHandler.has(player, node) || player.isOp()) {
     		return true;
     	} else {
     		return false;
     	}
     }
     
     public boolean canUseOnPlayer(Player player, Player target, String node) {
     	if ((canUseCommand(player, node) && canUseCommand(player, "setrank.rank." + getGroup(target))) || player.isOp()) {
     		return true;
     	} else {
     		return false;
     	}
     }
     
     public boolean canAddOrRemoveParent(Player player, String g) {
     	User user = SetRank.permissionHandler.getUserObject(player.getWorld().getName(), player.getName());
     	if (user == null)
     		return false;
     	Group group = SetRank.permissionHandler.getGroupObject(player.getWorld().getName(), g);
     	if (group == null) 
     		return false;
     	return true;
     }
     
     public void addParent(Player player, String g) {
     	User user = SetRank.permissionHandler.getUserObject(player.getWorld().getName(), player.getName());
     	Group group = SetRank.permissionHandler.getGroupObject(player.getWorld().getName(), g);
     	user.addParent(group);
     }
     
     public void removeParent(Player player, String g) {
     	User user = permissionHandler.getUserObject(player.getWorld().getName(), player.getName());
     	Group group = permissionHandler.getGroupObject(player.getWorld().getName(), g);
     	user.removeParent(group);
     }
     
     public void loadConfig() {
     	Configuration config = getConfiguration();
     	config.load();
     	SetRank.broadcastMessage = config.getBoolean("broadcast-message-on-rank-change", true);
     	SetRank.broadcastRankOnLogin = config.getBoolean("broadcast-rank-on-login", true);
     	SetRank.ownerName = config.getString("owner-name", "");
     	config.setProperty("broadcast-message-on-rank-change", Boolean.valueOf(SetRank.broadcastMessage));
     	config.setProperty("broadcast-rank-on-login", Boolean.valueOf(SetRank.broadcastRankOnLogin));
     	config.save();
     }
     
     public String getGroup(Player player) {
     	User user = permissionHandler.getUserObject(player.getWorld().getName(), player.getName());
         LinkedHashSet<Entry> group = user.getParents();
         String str = "";
         for (Entry g : group) {
         	str = g.getName();
         }
     	return str;
     }
 
     public void onEnable() {
     	println("Booting...");
     	setupPermissions();
     	loadConfig();
     	PluginManager pm = getServer().getPluginManager();
     	pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Event.Priority.Normal, this);
     	println("Done!");
     }
     
     public void onDisable() {
     	println("Shutting down...");
     	println("Done!");
     }
     
     public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] split) {
     	try {
     	if (cmd.getName().equalsIgnoreCase("rank") || cmd.getName().equalsIgnoreCase("setrank")) {
     		if (sender instanceof Player) {
     			Player player = (Player) sender;
 				Player t = getServer().matchPlayer(split[0]).get(0);
     			if (canUseOnPlayer(player, t, "setrank.rank." + split[1]) || canUseCommand(player, "setrank.rankall")) {
                     if (!canAddOrRemoveParent(t, split[1]))	{
                     	player.sendMessage(ChatColor.RED + "No such group!");
                     	return true;
                     } else if (canAddOrRemoveParent(t, split[1])) {
                             removeParent(t, getGroup(t));
                     	    addParent(t, split[1]);
                     	    message(player, "Changed " + t.getDisplayName() + ChatColor.DARK_RED + "'s rank to " + ChatColor.YELLOW + split[1] + ChatColor.DARK_RED + "!");
                     	    String a = "";
                     	    if (split[1].startsWith("a") || split[1].startsWith("e") || split[1].startsWith("i") || split[1].startsWith("o") || split[1].startsWith("u") || split[1].startsWith("A") || split[1].startsWith("E") || split[1].startsWith("I") || split[1].startsWith("O") || split[1].startsWith("U"))
                     		    a = "an";
                     	    else
                     		    a = "a";
                     	    message(t, "You are now " + a + " " + ChatColor.YELLOW + split[1] + "!");
                    	    println(ChatColor.DARK_AQUA + player.getName() + ChatColor.DARK_RED + " changed " + ChatColor.DARK_AQUA + t.getName() + ChatColor.DARK_RED + " to " + a + " " + ChatColor.YELLOW + split[1] + ChatColor.DARK_RED + "!");
                     	    if (broadcastMessage) 
                    		    broadcast(ChatColor.DARK_AQUA + player.getName() + ChatColor.DARK_RED + " changed " + ChatColor.DARK_AQUA + t.getName() + ChatColor.DARK_RED + " to " + a + " " + ChatColor.YELLOW + split[1] + ChatColor.DARK_RED + "!");
                     	    return true;
                     }
     			} else if (canUseOnPlayer(player, t, "setrank.rank" + split[1])) {
     				player.sendMessage(ChatColor.RED + "You don't have permission to rank to " + ChatColor.YELLOW + split[1] + ChatColor.RED + "!");
     				return true;
     			} else if (!canUseCommand(player, "setrank.rankall")) {
     				player.sendMessage(ChatColor.RED + "You don't have permission to use this!");
     				return true;
     			}
     		} else {
 				Player t = getServer().matchPlayer(split[0]).get(0);
                 if (!canAddOrRemoveParent(t, split[1]))	{
                 	sender.sendMessage(ChatColor.RED + "No such group!");
                 	return true;
                 } else if (canAddOrRemoveParent(t, split[1])) {
                 	    removeParent(t, getGroup(t));
                 	    addParent(t, split[1]);
                         sender.sendMessage(ChatColor.DARK_RED + "Changed " + t.getDisplayName() + ChatColor.DARK_RED + "'s rank to " + ChatColor.YELLOW + split[1] + ChatColor.DARK_RED + "!");
                 	    String a = "";
                 	    if (split[1].startsWith("a") || split[1].startsWith("e") || split[1].startsWith("i") || split[1].startsWith("o") || split[1].startsWith("u") || split[1].startsWith("A") || split[1].startsWith("E") || split[1].startsWith("I") || split[1].startsWith("O") || split[1].startsWith("U"))
                 		    a = "an";
                 	    else
                 		    a = "a";
                 	    message(t, "You are now " + a + " " + ChatColor.YELLOW + split[1] + ChatColor.DARK_RED + "!");
                 	    println(t.getName() + "'s rank has been changed to " + split[1]);
                 	    if (broadcastMessage) 
                 		    broadcast(t.getName() + " is now " + a + " " + ChatColor.YELLOW + split[1] + ChatColor.DARK_RED + "!");
                 	    return true;
                 }
     		}
     	}
     	} catch (ArrayIndexOutOfBoundsException e) {
     		sender.sendMessage(ChatColor.RED + "Wrong syntax! Usage: /rank [Player] [Rank]");
     		return true;
     	} catch (IndexOutOfBoundsException e) {
     		sender.sendMessage(ChatColor.RED + "No such player!");
     		return true;
     	}
     	return false;
     }
 }
