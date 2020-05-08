 package com.imjake9.simplejail;
 
 import com.platymuus.bukkit.permissions.Group;
 import com.platymuus.bukkit.permissions.PermissionsPlugin;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
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
 import ru.tehkode.permissions.PermissionGroup;
 import ru.tehkode.permissions.PermissionManager;
 import ru.tehkode.permissions.bukkit.PermissionsEx;
 
 public class SimpleJail extends JavaPlugin {
     
     private static final Logger log = Logger.getLogger("Minecraft");
     public ColouredConsoleSender console;
     private PermissionsPlugin bukkitPermissions;
     private PermissionManager pexPermissions;
     private int[] jailCoords = new int[3];
     private int[] unjailCoords = new int[3];
     private String jailGroup;
     private Configuration jailed;
     private SimpleJailPlayerListener listener;
     
     @Override
     public void onDisable() {
         log.info("[SimpleJail] " + this.getDescription().getName() + " v" + this.getDescription().getVersion() +  " disabled.");
     }
 
     @Override
     public void onEnable() {
         
         // Get console:
         console = ((CraftServer)this.getServer()).getServer().console;
         
         // Load configuration:
         this.loadConfig();
         
         // Get permissions plugin:
         bukkitPermissions = (PermissionsPlugin)this.getServer().getPluginManager().getPlugin("PermissionsBukkit");
         if(!this.isEnabled()) return;
         
         listener = new SimpleJailPlayerListener(this);
         this.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_RESPAWN, listener, Priority.High, this);
         this.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_JOIN, listener, Priority.Normal, this);
         
         this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
 
             @Override
             public void run() {
                 
                 long currentTime = System.currentTimeMillis();
                 
                 for (Player p : getServer().getOnlinePlayers()) {
                     
                     if (!playerIsJailed(p) || !playerIsTempJailed(p)) continue;
 
                     double tempTime = getTempJailTime(p);
 
                     if (tempTime <= currentTime) {
                         unjailPlayer(console, new String[]{p.getName()}, true);
                     }
                     
                 }
                 
             }
             
         }, 600, 600);
         
         log.info("[SimpleJail] " + this.getDescription().getName() + " v" + this.getDescription().getVersion() + " enabled.");
     }
     
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
         
         if(commandLabel.equalsIgnoreCase("jail") && (args.length == 1 || args.length == 2)) {
             if(!hasPermission(sender, "SimpleJail.jail")) return true;
             this.jailPlayer(sender, args);
             return true;
         } else if(commandLabel.equalsIgnoreCase("unjail") && args.length == 1) {
             if(!hasPermission(sender, "SimpleJail.unjail")) return true;
             this.unjailPlayer(sender, args);
             return true;
         } else if(commandLabel.equalsIgnoreCase("setjail") && (args.length == 0 || args.length == 3)) {
             if(!hasPermission(sender, "SimpleJail.setjail")) return true;
             this.setJail(sender, args);
             return true;
         } else if(commandLabel.equalsIgnoreCase("setunjail") && (args.length == 0 || args.length == 3)) {
             if(!hasPermission(sender, "SimpleJail.setjail")) return true;
             this.setUnjail(sender, args);
             return true;
         } else if(commandLabel.equalsIgnoreCase("jailtime") && args.length <= 1) {
             if(!hasPermission(sender, "SimpleJail.jailtime")) return true;
             this.jailTime(sender, args);
             return true;
         } else {
             if(!hasPermission(sender, "SimpleJail.jail")) return true;
             if(!hasPermission(sender, "SimpleJail.unjail")) return true;
             if(!hasPermission(sender, "SimpleJail.setjail")) return true;
             if(!hasPermission(sender, "SimpleJail.jailtime")) return true;
             return false;
         }
         
     }
     
     public void jailPlayer(CommandSender sender, String[] args) {
         Player player = this.getServer().getPlayer(args[0]);
         
         if(player == null) {
             sender.sendMessage(ChatColor.RED + "Couldn't find player \"" + args[0] + ".");
             return;
         }
         
         args[0] = player.getName().toLowerCase();
         
         // Check if player is slready jailed:
         if(jailed.getProperty(args[0]) != null) {
             sender.sendMessage(ChatColor.RED + "That player is already in jail!");
             return;
         }
         
         // Move player into jail:
         player.teleport(new Location(player.getWorld(), jailCoords[0], jailCoords[1], jailCoords[2]));
         
         // if (bukkitPermissions != null) {
         List<String> groupName = this.getGroups(player);
         jailed.setProperty(args[0] + ".groups", groupName);
         this.setGroup(player, jailGroup);
         
         int minutes = 0;
         
         if(args.length == 2) {
             minutes = this.parseTimeString(args[1]);
             if(minutes != -1) {
                 double tempTime = System.currentTimeMillis() + (minutes * 60000);
                 jailed.setProperty(args[0] + ".tempTime", tempTime);
             }
         }
         
         jailed.save();
         if(args.length == 1 || minutes == -1) player.sendMessage(ChatColor.AQUA + "You have been jailed!");
         else player.sendMessage(ChatColor.AQUA + "You have been jailed for " + this.prettifyMinutes(minutes) + "!");
         sender.sendMessage(ChatColor.AQUA + "Player sent to jail.");
     }
     
     public void unjailPlayer(CommandSender sender, String[] args, boolean fromTempJail) {
         Player player = this.getServer().getPlayer(args[0]);
         
         if(player == null) {
             sender.sendMessage(ChatColor.RED + "Couldn't find player \"" + args[0] + ".");
             return;
         }
         
         // Convert old jail entries:
         if (jailed.getProperty(args[0] + ".groups") == null) {
             List<String> groups = jailed.getStringList(args[0], new ArrayList<String>());
             jailed.removeProperty(args[0]);
             jailed.setProperty(args[0] + ".groups", groups);
         }
         
         args[0] = player.getName().toLowerCase();
         
         // Check if player is in jail:
         if(jailed.getProperty(args[0]) == null) {
             sender.sendMessage(ChatColor.RED + "That player is not in jail!");
             return;
         }
         
         // Move player out of jail:
         player.teleport(new Location(player.getWorld(), unjailCoords[0], unjailCoords[1], unjailCoords[2]));
         
         // if (bukkitPermissions != null) {
         this.setGroup(player, jailed.getStringList(args[0] + ".groups", new ArrayList()));
         
         jailed.removeProperty(args[0]);
         jailed.save();
         
         player.sendMessage(ChatColor.AQUA + "You have been removed from jail!");
         if (fromTempJail) sender.sendMessage(ChatColor.AQUA + player.getName() + " auto-unjailed.");
         else sender.sendMessage(ChatColor.AQUA + "Player removed from jail.");
     }
     
     public void unjailPlayer(CommandSender sender, String[] args) {
         this.unjailPlayer(sender, args, false);
     }
     
     public void setJail(CommandSender sender, String[] args) {
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
     
     public void setUnjail(CommandSender sender, String[] args) {
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
     
     public void jailTime(CommandSender sender, String[] args) {
         if(!(sender instanceof Player) && args.length == 0) {
             sender.sendMessage(ChatColor.RED + "Must specify a player.");
             return;
         }
         Player player = (args.length == 0) ? (Player)sender : this.getServer().getPlayer(args[0]);
         if(player == null) {
             sender.sendMessage(ChatColor.RED + "Couldn't find player '" + args[0] + "'.");
             return;
         }
         if(!this.playerIsTempJailed(player)) {
             if(args.length == 0) sender.sendMessage(ChatColor.RED + "You are not tempjailed.");
             else sender.sendMessage(ChatColor.RED + "That player is not tempjailed.");
             return;
         }
         int minutes = (int)((this.getTempJailTime(player) - System.currentTimeMillis()) / 60000);
         sender.sendMessage(ChatColor.AQUA + "Remaining jail time: " + this.prettifyMinutes(minutes));
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
         config.save();
         
         File f = new File(this.getDataFolder().getPath() + File.separator + "jailed.yml");
         try {
             if(!f.exists()) f.createNewFile();
         } catch (IOException ex) {}
         jailed = new Configuration(f);
         jailed.load();
     }
     
     public void setupPermissions() {
         
         Plugin bukkit = this.getServer().getPluginManager().getPlugin("PermissionsBukkit");
         Plugin pex = this.getServer().getPluginManager().getPlugin("PermissionsEx");
         
         boolean permissionsLoaded = false;
         
         if(bukkitPermissions == null){
            if(bukkit != null){
                bukkitPermissions = (PermissionsPlugin)bukkit;
                permissionsLoaded = true;
            }
         }
         if (pexPermissions == null) {
             if (pex != null) {
                 pexPermissions = PermissionsEx.getPermissionManager();
                 permissionsLoaded = true;
             }
         }
         
         if(!permissionsLoaded) {
             log.info("[SimpleJail] ERROR: Permissions plugin not detected.");
             this.getServer().getPluginManager().disablePlugin(this);
             return;
         }
     }
     
     public Location getJailLocation(Player player) {
         return new Location(player.getWorld(), jailCoords[0], jailCoords[1], jailCoords[2]);
     }
     
     public boolean playerIsJailed(Player player) {
         if (jailed.getProperty(player.getName().toLowerCase()) != null)
             return true;
         return false;
     }
     
     public boolean playerIsTempJailed(Player player) {
         if (jailed.getProperty(player.getName().toLowerCase() + ".tempTime") != null)
             return true;
         return false;
     }
     
     public double getTempJailTime(Player player) {
         return jailed.getDouble(player.getName().toLowerCase() + ".tempTime", -1);
     }
     
     public boolean hasPermission(CommandSender sender, String permission) {
         if (sender instanceof Player)
             return sender.hasPermission(permission);
         else return true;
     }
     
     public List<String> getGroups(Player player) {
         if(bukkitPermissions != null) {
             List<Group> groups = bukkitPermissions.getGroups(player.getName());
             List<String> stringGroups = new ArrayList<String>();
             for (Group g : groups) {
                 stringGroups.add(g.getName());
             }
             return stringGroups;
         } else if (pexPermissions != null) {
             PermissionGroup[] groups = pexPermissions.getUser(player).getGroups();
             List<String> stringGroups = new ArrayList<String>();
             for (PermissionGroup g : groups) {
                 stringGroups.add(g.getName());
             }
             return stringGroups;
         }
         
         return null;
     }
     
     public void setGroup(Player player, String group) {
         if (bukkitPermissions != null)
             this.getServer().dispatchCommand(console, "permissions player setgroup " + player.getName() + " " + group);
         else if(pexPermissions != null)
             pexPermissions.getUser(player).setGroups(new String[] { group });
     }
     
     public void setGroup(Player player, List<String> group) {
         if (bukkitPermissions != null) {
             String params = new String();
             for (String grp : group) {
                 params += " " + grp;
             }
             this.getServer().dispatchCommand(console, "permissions player setgroup " + player.getName() + params);
         } else if(pexPermissions != null) {
             pexPermissions.getUser(player).setGroups(group.toArray(new String[0]));
         }
     }
     
     public String prettifyMinutes(int minutes) {
         if (minutes == 1) return "one minute";
        if (minutes < 60) return minutes + "minutes";
         if (minutes % 60 == 0) {
             if(minutes / 60 == 1) return "one hour";
            else return (minutes / 60) + "hours";
         }
         int m = minutes % 60;
         int h = (minutes - m) / 60;
         return h + "h" + m + "m";
     }
     
     public int parseTimeString(String time) {
        if(!time.matches("[0-9]*h?[0-9]+m?")) return -1;
         if(time.matches("[0-9]+")) return Integer.parseInt(time);
         if(time.matches("[0-9]+m")) return Integer.parseInt(time.split("m")[0]);
         if(time.matches("[0-9]+h")) return Integer.parseInt(time.split("h")[0]) * 60;
         if(time.matches("[0-9]+h[0-9]+m")) {
             String[] split = time.split("[mh]");
             return (Integer.parseInt(split[0]) * 60) + Integer.parseInt(split[1]);
         }
         return -1;
     }
 }
