 package com.imjake9.simplejail;
 
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 import com.platymuus.bukkit.permissions.PermissionsPlugin;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Scanner;
 import java.util.logging.Logger;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.craftbukkit.CraftServer;
 import org.bukkit.craftbukkit.command.ColouredConsoleSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.config.Configuration;
 
 public class SimpleJail extends JavaPlugin {
     
     private static final Logger log = Logger.getLogger("Minecraft");
    private final ColouredConsoleSender console = ((CraftServer)this.getServer()).getServer().console;
     public static PermissionHandler permissions;
     public static PermissionsPlugin bukkitPermissions;
     private int[] jailCoords = new int[3];
     private int[] unjailCoords = new int[3];
     private String jailGroup;
     private Configuration perms;
     private Configuration jailed;
     private boolean newPerms = false;
     private SimpleJailPlayerListener listener;
     public boolean useBukkitPermissions = true;
     
     @Override
     @SuppressWarnings("LoggerStringConcat")
     public void onDisable() {
         log.info("[SimpleJail] " + this.getDescription().getName() + " v" + this.getDescription().getVersion() +  " disabled.");
     }
 
     @Override
     @SuppressWarnings("LoggerStringConcat")
     public void onEnable() {
         this.loadConfig();
         if(!useBukkitPermissions) this.setupPermissions();
         else bukkitPermissions = (PermissionsPlugin)this.getServer().getPluginManager().getPlugin("Permissions");
         
         listener = new SimpleJailPlayerListener(this);
         this.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_RESPAWN, listener, Priority.High, this);
         
         log.info("[SimpleJail] " + this.getDescription().getName() + " v" + this.getDescription().getVersion() + " enabled.");
     }
     
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
         
         if(commandLabel.equalsIgnoreCase("jail") && args.length == 1) {
             if(sender instanceof Player) {
                 if(!permissions.has((Player)sender, "SimpleJail.jail")) return true;
             }
             this.jailPlayer(sender, args);
             return true;
         } else if(commandLabel.equalsIgnoreCase("unjail") && args.length == 1) {
             if(sender instanceof Player) {
                 if(!permissions.has((Player)sender, "SimpleJail.unjail")) return true;
             }
             this.unjailPlayer(sender, args);
             return true;
         } else if(commandLabel.equalsIgnoreCase("setjail") && (args.length == 0 || args.length == 3)) {
             if(sender instanceof Player) {
                 if(!permissions.has((Player)sender, "SimpleJail.setjail")) return true;
             }
             this.setJail(sender, args);
             return true;
         } else if(commandLabel.equalsIgnoreCase("setunjail") && (args.length == 0 || args.length == 3)) {
             if(sender instanceof Player) {
                 if(!permissions.has((Player)sender, "SimpleJail.setjail")) return true;
             }
             this.setUnjail(sender, args);
             return true;
         } else {
             if(sender instanceof Player) {
                 if(!permissions.has((Player)sender, "SimpleJail.jail")) return true;
                 if(!permissions.has((Player)sender, "SimpleJail.unjail")) return true;
                 if(!permissions.has((Player)sender, "SimpleJail.setjail")) return true;
             }
             return false;
         }
     }
     
     public void jailPlayer(CommandSender sender, String[] args) {
         Player player = this.getServer().getPlayer(args[0]);
         if(player == null) {
             sender.sendMessage(ChatColor.RED + "Couldn't find player \"" + args[0] + ".");
             return;
         }
         args[0] = player.getName();
         
         //Compatibility code:
         if(!(args[0].equals(args[0].toLowerCase())) && jailed.getProperty(args[0].toLowerCase()) != null) {
             jailed.setProperty(args[0], args[0].toLowerCase());
             jailed.removeProperty(args[0].toLowerCase());
         }
         
         if(jailed.getProperty(args[0]) != null) {
             sender.sendMessage(ChatColor.RED + "That player is already in jail!");
             return;
         }
         player.teleport(new Location(player.getWorld(), jailCoords[0], jailCoords[1], jailCoords[2]));
         
         if (useBukkitPermissions || !newPerms) {
             Object groupName;
             if(useBukkitPermissions) {
                 groupName = bukkitPermissions.getGroups(player.getName());
             } else {
                 groupName = permissions.getGroup(this.getServer().getWorlds().get(0).getName(), args[0]);
             }
             jailed.setProperty(args[0], groupName);
             this.setGroup(player, jailGroup);
         } else {
             String[] groupName = permissions.getGroups(this.getServer().getWorlds().get(0).getName(), args[0]);
             List groupList = Arrays.asList(groupName);
             if (groupList == null) {
                 groupList = new ArrayList();
             }
             jailed.setProperty(args[0], groupList);
             List jailList = new ArrayList();
             jailList.add(jailGroup);
             this.setGroup(player, jailList);
         }
         
         jailed.save();
         perms.save();
         this.getServer().dispatchCommand(((CraftServer)getServer()).getServer().console, "permissions -reload all");
         sender.sendMessage(ChatColor.AQUA + "Player sent to jail.");
     }
     
     public void unjailPlayer(CommandSender sender, String[] args) {
         Player player = this.getServer().getPlayer(args[0]);
         if(player == null) {
             sender.sendMessage(ChatColor.RED + "Couldn't find player \"" + args[0] + ".");
             return;
         }
         args[0] = player.getName();
         
         //Compatibility code:
         if(!(args[0].equals(args[0].toLowerCase())) && jailed.getProperty(args[0].toLowerCase()) != null) {
             jailed.setProperty(args[0], args[0].toLowerCase());
             jailed.removeProperty(args[0].toLowerCase());
         }
         
         if(jailed.getProperty(args[0]) == null) {
             sender.sendMessage(ChatColor.RED + "That player is not in jail!");
             return;
         }
         player.teleport(new Location(player.getWorld(), unjailCoords[0], unjailCoords[1], unjailCoords[2]));
         
         if (useBukkitPermissions || !newPerms) {
             this.setGroup(player, jailed.getString(args[0]));
         } else {
             if (jailed.getProperty(args[0]) instanceof String) {
                 this.convertPermission(args[0]);
             }
             List groupList = jailed.getList(args[0]);
             if (groupList == null) {
                 groupList = new ArrayList();
             }
             this.setGroup(player, groupList);
         }
         
         jailed.removeProperty(args[0]);
         
         jailed.save();
         perms.save();
         this.getServer().dispatchCommand(((CraftServer)getServer()).getServer().console, "permissions -reload all");
         sender.sendMessage(ChatColor.AQUA + "Player removed from jail.");
     }
     
     public void setJail(CommandSender sender, String args[]) {
         if(!(sender instanceof Player)) {
             sender.sendMessage(ChatColor.RED + "Only players can use that.");
             return;
         }
         if(args.length == 0) {
             Player player = (Player)sender;
             Location loc = player.getLocation();
             jailCoords[0] = loc.getBlockX();
             jailCoords[1] = loc.getBlockY();
             jailCoords[2] = loc.getBlockZ();
         } else {
             if(!(new Scanner(args[0]).hasNextInt()) || !(new Scanner(args[1]).hasNextInt()) || !(new Scanner(args[2]).hasNextInt())) {
                 sender.sendMessage(ChatColor.RED + "Invalid coordinate.");
                 return;
             }
             jailCoords[0] = Integer.parseInt(args[0]);
             jailCoords[1] = Integer.parseInt(args[1]);
             jailCoords[2] = Integer.parseInt(args[2]);
         }
         
         Configuration config = this.getConfiguration();
         config.setProperty("jail.x", jailCoords[0]);
         config.setProperty("jail.y", jailCoords[1]);
         config.setProperty("jail.z", jailCoords[2]);
         config.save();
         sender.sendMessage(ChatColor.AQUA + "Jail point saved.");
     }
     
     public void setUnjail(CommandSender sender, String args[]) {
         if(!(sender instanceof Player)) {
             sender.sendMessage(ChatColor.RED + "Only players can use that.");
             return;
         }
         if(args.length == 0) {
             Player player = (Player)sender;
             Location loc = player.getLocation();
             unjailCoords[0] = loc.getBlockX();
             unjailCoords[1] = loc.getBlockY();
             unjailCoords[2] = loc.getBlockZ();
         } else {
             if(!(new Scanner(args[0]).hasNextInt()) || !(new Scanner(args[1]).hasNextInt()) || !(new Scanner(args[2]).hasNextInt())) {
                 sender.sendMessage(ChatColor.RED + "Invalid coordinate.");
                 return;
             }
             unjailCoords[0] = Integer.parseInt(args[0]);
             unjailCoords[1] = Integer.parseInt(args[1]);
             unjailCoords[2] = Integer.parseInt(args[2]);
         }
         
         Configuration config = this.getConfiguration();
         config.setProperty("unjail.x", unjailCoords[0]);
         config.setProperty("unjail.y", unjailCoords[1]);
         config.setProperty("unjail.z", unjailCoords[2]);
         config.save();
         sender.sendMessage(ChatColor.AQUA + "Unjail point saved.");
     }
     
     public void loadConfig() {
         Configuration config = this.getConfiguration();
         jailCoords[0] = config.getInt("jail.x", 0);
         jailCoords[1] = config.getInt("jail.y", 0);
         jailCoords[2] = config.getInt("jail.z", 0);
         unjailCoords[0] = config.getInt("unjail.x", 0);
         unjailCoords[1] = config.getInt("unjail.y", 0);
         unjailCoords[2] = config.getInt("unjail.z", 0);
         jailGroup = config.getString("jailgroup", "Jailed");
         useBukkitPermissions = config.getBoolean("usebukkitpermissions", config.getProperty("jailGroup") == null);
         config.save();
         
         File f = new File(this.getDataFolder().getPath() + File.separator + "jailed.yml");
         try {
             if(!f.exists()) f.createNewFile();
         } catch (IOException ex) {}
         jailed = new Configuration(f);
         jailed.load();
     }
     
     public void setupPermissions() {
         Plugin plugin = this.getServer().getPluginManager().getPlugin("Permissions");
         
         if(permissions == null){
            if(plugin != null){
                permissions = ((Permissions)plugin).getHandler();
            } else {
                log.info("[SimpleJail] ERROR: Permissions not detected.");
                this.getServer().getPluginManager().disablePlugin(this);
                return;
            }
         }
         
         File f = new File(this.getFile().getParent() + File.separator + "Permissions" + File.separator + this.getServer().getWorlds().get(0).getName() + ".yml");
         if(!f.exists()) {
             f = new File(this.getFile().getParent() + File.separator + "Permissions" + File.separator + this.getServer().getWorlds().get(0).getName() + File.separator + "users.yml");
             this.newPerms = true;
         }
         if(!f.exists()) {
             log.info("[SimpleJail] ERROR: Permissions file not found.");
             this.getServer().getPluginManager().disablePlugin(this);
             return;
         }
         
         perms = new Configuration(f);
         perms.load();
     }
     
     public void convertPermission(String key) {
         List groupList = new ArrayList();
         groupList.add(key);
         jailed.removeProperty(key);
         jailed.setProperty(key, groupList);
     }
     
     public Location getJailLocation(Player player) {
         return new Location(player.getWorld(), jailCoords[0], jailCoords[1], jailCoords[2]);
     }
     
     public boolean playerIsJailed(Player player) {
         if (jailed.getProperty(player.getName().toLowerCase()) != null)
             return true;
         return false;
     }
     
     public boolean hasPermission(Player player, String permission) {
         if(useBukkitPermissions)
             return player.hasPermission(permission);
         else
             return permissions.has(player, permission);
     }
     
     public void setGroup(Player player, String group) {
         if (useBukkitPermissions) {
             this.getServer().dispatchCommand(console, "permissions player setgroup " + player.getName() + " " + group);
         } else {
             perms.setProperty("users." + player.getName() + ((newPerms) ? ".groups" : ".group"), group);
         }
     }
     
     public void setGroup(Player player, List<String> group) {
         if (useBukkitPermissions) {
             String params = new String();
             for (String grp : group) {
                 params += " " + grp;
             }
             this.getServer().dispatchCommand(console, "permissions player setgroup " + player.getName() + params);
         } else {
             perms.setProperty("users." + player.getName() + ((newPerms) ? ".groups" : ".group"), group);
         }
     }
     
 }
