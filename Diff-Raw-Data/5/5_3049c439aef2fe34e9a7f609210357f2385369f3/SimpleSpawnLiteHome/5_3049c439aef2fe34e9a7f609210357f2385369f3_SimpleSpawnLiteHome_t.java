 package me.ellbristow.simplespawnlitehome;
 
 import java.util.HashMap;
 import me.ellbristow.simplespawnlitecore.LocationType;
 import me.ellbristow.simplespawnlitecore.SimpleSpawnLiteCore;
 import me.ellbristow.simplespawnlitecore.events.SimpleSpawnChangeLocationEvent;
 import me.ellbristow.simplespawnlitecore.events.SimpleSpawnRemoveLocationEvent;
 import me.ellbristow.simplespawnlitehome.listeners.PlayerListener;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class SimpleSpawnLiteHome extends JavaPlugin {
 
     private SimpleSpawnLiteHome plugin = this;
     private boolean setHomeWithBeds = false;
     private FileConfiguration config;
     private SimpleSpawnLiteCore ss;
     private String[] homeColumns = {"player", "world", "x", "y", "z", "yaw", "pitch"};
     private String[] homeDims = {"TEXT NOT NULL PRIMARY KEY", "TEXT NOT NULL", "DOUBLE NOT NULL DEFAULT 0", "DOUBLE NOT NULL DEFAULT 0", "DOUBLE NOT NULL DEFAULT 0", "FLOAT NOT NULL DEFAULT 0", "FLOAT NOT NULL DEFAULT 0"};
     
     @Override
     public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
         if (!(sender instanceof Player)) {
             sender.sendMessage("Sorry! Command " + commandLabel + " cannot be run from the console!");
             return false;
         }
         
         Player player = (Player)sender;
         
         if (commandLabel.equalsIgnoreCase("sethome") || commandLabel.equalsIgnoreCase("ssethome")) {
             if (args.length == 0) {
                 if (!player.hasPermission("simplespawn.home.set")) {
                     player.sendMessage(ChatColor.RED + "You do not have permission to use that command!");
                     return false;
                 }
 
                 getLogger().finer("Player " + player.getName() + " sets his home location.");
                 setHomeLoc(player);
                 player.sendMessage(ChatColor.GOLD + "Your home has been set to this location!");
                 return true;
             } else if (args.length == 1) {
                 if (!player.hasPermission("simplespawn.home.set.others")) {
                     player.sendMessage(ChatColor.RED + "You do not have permission to set other peoples home!");
                     return false;
                 }
 
                 OfflinePlayer target = getServer().getOfflinePlayer(args[0]);
                 if (!target.hasPlayedBefore() && !target.isOnline()) {
                     player.sendMessage(ChatColor.RED + "Player '" + ChatColor.WHITE + args[0] + ChatColor.RED + "' not found!");
                     return true;
                 }
                 setOtherHomeLoc(target, player);
                 player.sendMessage(ChatColor.WHITE + target.getName() + ChatColor.GOLD + "'s home has been set to this location!");
                 return true;
             } else {
                 player.sendMessage(ChatColor.RED + "Command not recognised!");
                 player.sendMessage(ChatColor.RED + "Try: /sethome OR /sethome {playerName}");
                 return false;
             }
 
         } else if (commandLabel.equalsIgnoreCase("home") || commandLabel.equalsIgnoreCase("shome")) {
             if (args.length == 0) {
                 if (!player.hasPermission("simplespawn.home.use")) {
                     player.sendMessage(ChatColor.RED + "You do not have permission to use that command!");
                     return false;
                 }
                 Location homeLoc = getHomeLoc(player);
                ss.simpleTeleport(player, homeLoc, LocationType.HOME);
                 return true;
             } else if (args.length == 1) {
                 if (!player.hasPermission("simplespawn.home.use.others")) {
                     player.sendMessage(ChatColor.RED + "You do not have permission to use that command to spawn to others home!");
                     return false;
                 }
                 OfflinePlayer target = getServer().getOfflinePlayer(args[0]);
                 if (!target.hasPlayedBefore() && !target.isOnline()) {
                     player.sendMessage(ChatColor.RED + "Player '" + ChatColor.WHITE + args[0] + ChatColor.RED + "' not found!");
                     return false;
                 }
                 if (!target.isOnline() && !player.hasPermission("simplespawn.home.use.offline")) {
                     player.sendMessage(ChatColor.RED + "You do not have permission to use that command to spawn to offline players home!");
                     return false;
                 }
                 Location homeLoc = getHomeLoc(target.getName());
                 if (homeLoc == null) {
                     player.sendMessage(ChatColor.RED + "Can't find " + ChatColor.WHITE + args[0] + ChatColor.RED + "'s home or bed!");
                     return false;
                 }
                ss.simpleTeleport(player, homeLoc, LocationType.HOME);
                 return true;
             }
         } else if (
                 commandLabel.equalsIgnoreCase("removehome")
                 || commandLabel.equalsIgnoreCase("sremovehome")
                 || commandLabel.equalsIgnoreCase("remhome")
                 || commandLabel.equalsIgnoreCase("sremhome")
                 || commandLabel.equalsIgnoreCase("delhome")
                 || commandLabel.equalsIgnoreCase("sdelhome") ) {
             if (args.length == 0) {
                 if (!player.hasPermission("simplespawn.home.remove")) {
                     player.sendMessage(ChatColor.RED + "You do not have permission to use that command!");
                     return false;
                 }
                 removeHome(player.getName());
                 player.sendMessage(ChatColor.GOLD + "Your home location has been removed!");
                 return true;
             } else if (args.length == 1) {
                 if (!player.hasPermission("simplespawn.home.remove.others")) {
                     player.sendMessage(ChatColor.RED + "You do not have permission to remove other peoples work!");
                     return false;
                 }
                 OfflinePlayer target = getServer().getOfflinePlayer(args[0]);
                 if (!target.hasPlayedBefore() && !target.isOnline()) {
                     player.sendMessage(ChatColor.RED + "Player '" + ChatColor.WHITE + args[0] + ChatColor.RED + "' not found!");
                     return false;
                 }
                 removeHome(target.getName());
                 player.sendMessage(ChatColor.WHITE + target.getName() + ChatColor.GOLD + "'s home location has been removed!");
                 return true;
             } else {
                 player.sendMessage(ChatColor.RED + "Command not recognised!");
                 player.sendMessage(ChatColor.RED + "Try: /removehome OR /removehome {playerName}");
                 return false;
             }
 
         }
         return true;
     }
     
     public void setBedLoc(Player player) {
         SimpleSpawnChangeLocationEvent e = new SimpleSpawnChangeLocationEvent(player.getName(), LocationType.HOME, player.getLocation());
         getServer().getPluginManager().callEvent(e);
         Location homeLoc = player.getLocation();
         setHomeLoc(player);
         player.setBedSpawnLocation(homeLoc);
     }
     
     private void setHomeLoc(Player player) {
         SimpleSpawnChangeLocationEvent e = new SimpleSpawnChangeLocationEvent(player.getName(), LocationType.HOME, player.getLocation());
         getServer().getPluginManager().callEvent(e);
         Location homeLoc = player.getLocation();
         String world = homeLoc.getWorld().getName();
         double x = homeLoc.getX();
         double y = homeLoc.getY();
         double z = homeLoc.getZ();
         float yaw = homeLoc.getYaw();
         float pitch = homeLoc.getPitch();
         ss.sql.query("INSERT OR REPLACE INTO PlayerHomes (player, world, x, y, z, yaw, pitch) VALUES ('" + player.getName() + "', '" + world + "', " + x + ", " + y + ", " + z + ", " + yaw + ", " + pitch + ")");
     }
 
     private void setOtherHomeLoc(OfflinePlayer target, Player player) {
         SimpleSpawnChangeLocationEvent e = new SimpleSpawnChangeLocationEvent(target.getName(), LocationType.HOME, player.getLocation());
         getServer().getPluginManager().callEvent(e);
         Location homeLoc = player.getLocation();
         String world = homeLoc.getWorld().getName();
         double x = homeLoc.getX();
         double y = homeLoc.getY();
         double z = homeLoc.getZ();
         float yaw = homeLoc.getYaw();
         float pitch = homeLoc.getPitch();
         ss.sql.query("INSERT OR REPLACE INTO PlayerHomes (player, world, x, y, z, yaw, pitch) VALUES ('" + target.getName() + "', '" + world + "', " + x + ", " + y + ", " + z + ", " + yaw + ", " + pitch + ")");
     }
     
     public Location getHomeLoc(String playerName) {
         HashMap<Integer, HashMap<String, Object>> result = ss.sql.select("world, x, y, z, yaw, pitch", "PlayerHomes", "player = '" + playerName + "'", null, null);
         Location location = null;
         if (result == null || result.isEmpty()) {
             // if you haven't used /sethome - first home is your bed
             getLogger().finest("No home found for " + playerName + ", trying to retrieve bedspawn.");
             OfflinePlayer player = getServer().getOfflinePlayer(playerName);
             if (player.hasPlayedBefore() || player.isOnline()) {
                 location = player.getBedSpawnLocation();
                 if (location == null) {
                     location = ss.getDefaultSpawn();
                 }
             }
         } else {
             String world = (String) result.get(0).get("world");
             double x = (Double) result.get(0).get("x");
             double y = (Double) result.get(0).get("y");
             double z = (Double) result.get(0).get("z");
             float yaw = Float.parseFloat(result.get(0).get("yaw").toString());
             float pitch = Float.parseFloat(result.get(0).get("pitch").toString());
             location = new Location(getServer().getWorld(world), x, y, z, yaw, pitch);
         }
         return location;
     }
 
     public Location getHomeLoc(Player player) {
         Location homeLoc = getHomeLoc(player.getName());
 
         // Default Spawn Location in this world if no Home/Bed set
         if (homeLoc == null) {
             getLogger().finest("No home/bed found for " + player.getName() + ", trying to retrieve spawn location.");
             homeLoc = ss.getWorldSpawn(player.getWorld().getName());
         }
 
         return homeLoc;
     }
     
     private void removeHome(String playerName) {
         SimpleSpawnRemoveLocationEvent e = new SimpleSpawnRemoveLocationEvent(playerName, LocationType.HOME);
         getServer().getPluginManager().callEvent(e);
         ss.sql.query("DELETE FROM PlayerHomes WHERE player='" + playerName + "' ");
     }
     
     public boolean getSetting(String setting) {
         if (setting.equalsIgnoreCase("SetHomeWithBeds")) return setHomeWithBeds;
         return false;
     }
     
     @Override
     public void saveConfig() {
         ss.saveConfig();
     }
 
     @Override
     public void onDisable() {
         ss.sql.close();
     }
 
     @Override
     public void onEnable() {
         
         ss = SimpleSpawnLiteCore.getPluginLink();
         
         config = ss.getConfig();
         
         setHomeWithBeds = config.getBoolean("set_home_with_beds", true);
         config.set("set_home_with_beds", setHomeWithBeds);
         
         saveConfig();
         
         getServer().getPluginManager().registerEvents(new PlayerListener(plugin), plugin);
         
         if (!ss.sql.checkTable("PlayerHomes")) {
             ss.sql.createTable("PlayerHomes", homeColumns, homeDims);
         }
         
     }
 
 }
