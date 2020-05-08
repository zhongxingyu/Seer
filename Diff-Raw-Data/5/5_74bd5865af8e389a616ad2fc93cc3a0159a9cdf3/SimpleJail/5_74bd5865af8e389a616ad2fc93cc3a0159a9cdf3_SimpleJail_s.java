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
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 import ru.tehkode.permissions.PermissionGroup;
 import ru.tehkode.permissions.PermissionManager;
 import ru.tehkode.permissions.bukkit.PermissionsEx;
 
 public class SimpleJail extends JavaPlugin {
     
     private static final Logger log = Logger.getLogger("Minecraft");
     public ConsoleCommandSender console;
     private PermissionsPlugin bukkitPermissions;
     private PermissionManager pexPermissions;
     private Location jailLoc;
     private Location unjailLoc;
     private String jailGroup;
     private YamlConfiguration jailed;
     private SimpleJailPlayerListener listener;
     
     @Override
     public void onDisable() {
         log.info("[SimpleJail] " + this.getDescription().getName() + " v" + this.getDescription().getVersion() +  " disabled.");
     }
 
     @Override
     public void onEnable() {
         
         // Get console:
         console = this.getServer().getConsoleSender();
         
         // Load configuration:
         this.loadConfig();
         
         // Get permissions plugin:
         this.setupPermissions();
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
         } else if(commandLabel.equalsIgnoreCase("setjail") && (args.length == 0 || args.length == 4)) {
             if(!hasPermission(sender, "SimpleJail.setjail")) return true;
             this.setJail(sender, args);
             return true;
         } else if(commandLabel.equalsIgnoreCase("setunjail") && (args.length == 0 || args.length == 4)) {
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
         
         args[0] = (player == null) ? args[0].toLowerCase() : player.getName().toLowerCase();
         
         // Check if player is slready jailed:
         if(jailed.get(args[0]) != null) {
             sender.sendMessage(ChatColor.RED + "That player is already in jail!");
             return;
         }
         
         List<String> groupName = this.getGroups(args[0]);
         jailed.set(args[0] + ".groups", groupName);
         this.setGroup(args[0], jailGroup);
         
         int minutes = 0;
         
         if(args.length == 2) {
             minutes = this.parseTimeString(args[1]);
             if(minutes != -1) {
                 double tempTime = System.currentTimeMillis() + (minutes * 60000);
                 jailed.set(args[0] + ".tempTime", tempTime);
             }
         }
         
         // Move player into jail:
         if (player != null)
             player.teleport(jailLoc);
         else
             jailed.set(args[0] + ".status", "pending");
         
         this.saveJail();
         
         if (player != null) {
             if(args.length == 1 || minutes == -1) player.sendMessage(ChatColor.AQUA + "You have been jailed!");
             else player.sendMessage(ChatColor.AQUA + "You have been jailed for " + this.prettifyMinutes(minutes) + "!");
         }
         sender.sendMessage(ChatColor.AQUA + "Player sent to jail.");
     }
     
     public void unjailPlayer(CommandSender sender, String[] args, boolean fromTempJail) {
         Player player = this.getServer().getPlayer(args[0]);
         
         args[0] = (player == null) ? args[0].toLowerCase() : player.getName().toLowerCase();
         
         // Check if player is in jail:
         if(jailed.get(args[0]) == null) {
             sender.sendMessage(ChatColor.RED + "That player is not in jail!");
             return;
         }
         
         // Check if player is offline:
         if (player == null) {
             jailed.set(args[0] + ".status", "freed");
             sender.sendMessage(ChatColor.AQUA + "Player removed from jail.");
             return;
         }
         
         // Move player out of jail:
         player.teleport(unjailLoc);
         
         this.setGroup(args[0], jailed.getStringList(args[0] + ".groups"));
         
         jailed.set(args[0], null);
         
         this.saveJail();
         
         player.sendMessage(ChatColor.AQUA + "You have been removed from jail!");
         if (fromTempJail) sender.sendMessage(ChatColor.AQUA + player.getName() + " auto-unjailed.");
         else sender.sendMessage(ChatColor.AQUA + "Player removed from jail.");
     }
     
     public void unjailPlayer(CommandSender sender, String[] args) {
         this.unjailPlayer(sender, args, false);
     }
     
     public void setJail(CommandSender sender, String[] args) {
         if(!(sender instanceof Player) && args.length != 4) {
             sender.sendMessage(ChatColor.RED + "Only players can use that.");
             return;
         }
         if(args.length == 0) {
             Player player = (Player)sender;
             jailLoc = player.getLocation();
         } else {
             if(!(new Scanner(args[0]).hasNextInt()) || !(new Scanner(args[1]).hasNextInt()) || !(new Scanner(args[2]).hasNextInt())) {
                 sender.sendMessage(ChatColor.RED + "Invalid coordinate.");
                 return;
             }
             jailLoc = new Location(
                     this.getServer().getWorld(args[3]),
                     Integer.parseInt(args[0]),
                     Integer.parseInt(args[1]),
                     Integer.parseInt(args[2]));
         }
         
         YamlConfiguration config = (YamlConfiguration) this.getConfig();
         config.set("jail.x", (int) jailLoc.getX());
         config.set("jail.y", (int) jailLoc.getY());
         config.set("jail.z", (int) jailLoc.getZ());
         config.set("jail.world", jailLoc.getWorld().getName());
         
         this.saveConfig();
         
         sender.sendMessage(ChatColor.AQUA + "Jail point saved.");
     }
     
     public void setUnjail(CommandSender sender, String[] args) {
         if(!(sender instanceof Player) && args.length != 4) {
             sender.sendMessage(ChatColor.RED + "Only players can use that.");
             return;
         }
         if(args.length == 0) {
             Player player = (Player)sender;
             unjailLoc = player.getLocation();
         } else {
             if(!(new Scanner(args[0]).hasNextInt()) || !(new Scanner(args[1]).hasNextInt()) || !(new Scanner(args[2]).hasNextInt())) {
                 sender.sendMessage(ChatColor.RED + "Invalid coordinate.");
                 return;
             }
             unjailLoc = new Location(
                     this.getServer().getWorld(args[3]),
                     Integer.parseInt(args[0]),
                     Integer.parseInt(args[1]),
                     Integer.parseInt(args[2]));
         }
         
         YamlConfiguration config = (YamlConfiguration) this.getConfig();
         config.set("unjail.x", (int) unjailLoc.getX());
         config.set("unjail.y", (int) unjailLoc.getY());
         config.set("unjail.z", (int) unjailLoc.getZ());
         config.set("unjail.world", unjailLoc.getWorld().getName());
         
         this.saveConfig();
         
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
         // Init config files:
         YamlConfiguration config = (YamlConfiguration) this.getConfig();
         config.options().copyDefaults(true);
         config.addDefault("jailgroup", "Jailed");
         config.addDefault("jail.world", this.getServer().getWorlds().get(0).getName());
         config.addDefault("jail.x", 0);
         config.addDefault("jail.y", 0);
         config.addDefault("jail.z", 0);
         config.addDefault("unjail.world", this.getServer().getWorlds().get(0).getName());
         config.addDefault("unjail.x", 0);
         config.addDefault("unjail.y", 0);
         config.addDefault("unjail.z", 0);
         
         jailed = new YamlConfiguration();
         File f = new File(this.getDataFolder().getPath() + File.separator + "jailed.yml");
         
         try {
             if(!f.exists()) {
                 f.getParentFile().mkdirs();
                 f.createNewFile();
             }
         } catch (IOException ex) {
             ex.printStackTrace();
         }
         
         try {
             jailed.load(f);
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         
         jailLoc = new Location(
                 this.getServer().getWorld(config.getString("jail.world", this.getServer().getWorlds().get(0).getName())),
                 config.getInt("jail.x", 0),
                 config.getInt("jail.y", 0),
                 config.getInt("jail.z", 0));
         unjailLoc = new Location(
                 this.getServer().getWorld(config.getString("unjail.world", this.getServer().getWorlds().get(0).getName())),
                 config.getInt("unjail.x", 0),
                 config.getInt("unjail.y", 0),
                 config.getInt("unjail.z", 0));
         jailGroup = config.getString("jailgroup", "Jailed");
         
         this.saveConfig();
         
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
     
     public Location getJailLocation() {
         return jailLoc;
     }
     
     public Location getUnjailLocation() {
         return unjailLoc;
     }
     
     public boolean playerIsJailed(Player player) {
         if (jailed.get(player.getName().toLowerCase()) != null)
             return true;
         return false;
     }
     
     public boolean playerIsTempJailed(Player player) {
         if (jailed.get(player.getName().toLowerCase() + ".tempTime") != null)
             return true;
         return false;
     }
     
     public double getTempJailTime(Player player) {
         return jailed.getDouble(player.getName().toLowerCase() + ".tempTime", -1);
     }
     
     public JailStatus getPlayerStatus(Player player) {
         return JailStatus.valueOf(jailed.getString(player.getName().toLowerCase() + ".status", "jailed").toUpperCase());
     }
     
     public boolean hasPermission(CommandSender sender, String permission) {
         if (sender instanceof Player)
             return sender.hasPermission(permission);
         else return true;
     }
     
     public List<String> getGroups(String player) {
         if(bukkitPermissions != null) {
             List<Group> groups = bukkitPermissions.getGroups(player);
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
     
     public void setGroup(String player, String group) {
         if (bukkitPermissions != null)
             this.getServer().dispatchCommand(console, "permissions player setgroup " + player + " " + group);
         else if(pexPermissions != null)
             pexPermissions.getUser(player).setGroups(new String[] { group });
     }
     
     public void setGroup(String player, List<String> group) {
         if (bukkitPermissions != null) {
             String params = new String();
             for (String grp : group) {
                params += " " + grp;
             }
            this.getServer().dispatchCommand(console, "permissions player setgroup " + player + params);
         } else if(pexPermissions != null) {
             pexPermissions.getUser(player).setGroups(group.toArray(new String[0]));
         }
     }
     
     public String prettifyMinutes(int minutes) {
         if (minutes == 1) return "one minute";
         if (minutes < 60) return minutes + " minutes";
         if (minutes % 60 == 0) {
             if(minutes / 60 == 1) return "one hour";
             else return (minutes / 60) + " hours";
         }
         int m = minutes % 60;
         int h = (minutes - m) / 60;
         return h + "h" + m + "m";
     }
     
     public int parseTimeString(String time) {
         if(!time.matches("[0-9]*h?[0-9]*m?")) return -1;
         if(time.matches("[0-9]+")) return Integer.parseInt(time);
         if(time.matches("[0-9]+m")) return Integer.parseInt(time.split("m")[0]);
         if(time.matches("[0-9]+h")) return Integer.parseInt(time.split("h")[0]) * 60;
         if(time.matches("[0-9]+h[0-9]+m")) {
             String[] split = time.split("[mh]");
             return (Integer.parseInt(split[0]) * 60) + Integer.parseInt(split[1]);
         }
         return -1;
     }
     
     public void saveJail() {
         try {
             jailed.save(new File(this.getDataFolder().getPath() + File.separator + "jailed.yml"));
         } catch (IOException ex) {
             ex.printStackTrace();
         }
     }
     
     public enum JailStatus {
         JAILED,
         PENDING,
         FREED;
     }
 }
