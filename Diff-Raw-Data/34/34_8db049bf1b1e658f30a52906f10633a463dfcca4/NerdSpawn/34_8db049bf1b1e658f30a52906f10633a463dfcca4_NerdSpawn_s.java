 package nu.nerd.nerdspawn;
 
 import java.io.File;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 
 public class NerdSpawn extends JavaPlugin
 {
     private final NerdSpawnListener listener = new NerdSpawnListener(this);
    public static final Logger log = Logger.getLogger("Minecraft");
    public String worldName;
 
     public Location getSpawnLocation()
     {
         // just in case someone edited the file
         reloadConfig();
 
        return new Location(Bukkit.getWorld(worldName),
                 getConfig().getDouble("spawn-location.x"),
                 getConfig().getDouble("spawn-location.y"),
                 getConfig().getDouble("spawn-location.z"),
                 (float)getConfig().getDouble("spawn-location.yaw"),
                 (float)getConfig().getDouble("spawn-location.pitch"));
     }
 
     @Override
     public void onEnable()
     {
         getServer().getPluginManager().registerEvents(listener, this);
 
         File config = new File(getDataFolder() + File.separator + "config.yml");
         if (!config.exists()) {
             getConfig().options().copyDefaults(true);
             saveConfig();
         }
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args)
     {
         if (!(sender instanceof Player)) {
             sender.sendMessage("Sorry, this plugin cannot be used from console");
             return true;
         }
 
         if (command.getName().equalsIgnoreCase("spawn")) {
             if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                if (Permissions.hasPermission((Player)sender, Permissions.SETSPAWN)) {
                     reloadConfig();
                     sender.sendMessage(ChatColor.GRAY + "Spawn configuration reloaded");
                     return true;
                 }
             }
             ((Player) sender).teleport(getSpawnLocation());
             return true;
         }
 
         if (command.getName().equalsIgnoreCase("setspawn")) {
            setSpawn((Player)sender);
             return true;
         }
 
         return false;
     }
 
     private void setSpawn(Player player)
     {
         Location loc = player.getLocation();
         getConfig().set("spawn-location.x", loc.getX());
         getConfig().set("spawn-location.y", loc.getY());
         getConfig().set("spawn-location.z", loc.getZ());
         getConfig().set("spawn-location.yaw", loc.getYaw());
         getConfig().set("spawn-location.pitch", loc.getPitch());
         saveConfig();
 
        Bukkit.getServer().getWorld(worldName).setSpawnLocation(
                 loc.getBlockX(),
                 loc.getBlockY(),
                 loc.getBlockZ());
 
         player.sendMessage(ChatColor.GRAY + "Spawn set at " +
                 loc.getBlockX() + ", " +
                 loc.getBlockY() + ", " +
                 loc.getBlockZ());
     }
 }
