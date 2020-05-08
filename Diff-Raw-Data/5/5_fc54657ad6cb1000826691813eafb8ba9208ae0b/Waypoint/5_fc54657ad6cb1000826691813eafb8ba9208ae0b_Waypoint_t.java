 package me.limebyte.battlenight.api.battle;
 
 import me.limebyte.battlenight.core.util.config.ConfigManager;
 import me.limebyte.battlenight.core.util.config.ConfigManager.Config;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.configuration.file.FileConfiguration;
 
 public class Waypoint {
 
     private String name;
     private String arenaName;
     private static final String LOC_SEP = ", ";
 
     private static Waypoint lounge = new Waypoint("lounge");
     private static Waypoint exit = new Waypoint("exit");
 
     public Waypoint(String name) {
         this.name = name;
         arenaName = "default";
     }
 
     public Waypoint(String name, Arena arena) {
         this.name = name;
         arenaName = arena.getName();
     }
 
     public String getName() {
         return name;
     }
 
     public Location getLocation() {
        if (!isSet()) return null;
         FileConfiguration config = ConfigManager.get(Config.ARENAS);
         return parseLocation(config.getString(arenaName + "." + name));
     }
 
     public void setLocation(Location location) {
         ConfigManager.reload(Config.ARENAS);
         FileConfiguration config = ConfigManager.get(Config.ARENAS);
         config.set(arenaName + "." + name, parseLocation(location));
         ConfigManager.save(Config.ARENAS);
     }
 
     public boolean isSet() {
        FileConfiguration config = ConfigManager.get(Config.ARENAS);
        return config.getString(arenaName + "." + name) != null;
     }
 
     public String getParsedLocation() {
         return parseLocation(getLocation());
     }
 
     public static final String parseLocation(Location loc) {
         String w = loc.getWorld().getName();
         double x = loc.getBlockX() + 0.5;
         double y = loc.getBlockY();
         double z = loc.getBlockZ() + 0.5;
         float yaw = loc.getYaw();
         float pitch = loc.getPitch();
         return w + "(" + x + LOC_SEP + y + LOC_SEP + z + LOC_SEP + yaw + LOC_SEP + pitch + ")";
     }
 
     public static final Location parseLocation(String string) {
         String[] parts = string.split("\\(");
         World w = Bukkit.getServer().getWorld(parts[0]);
 
         String[] coords = parts[1].substring(0, parts[1].length() - 1).split(LOC_SEP);
         double x = Double.parseDouble(coords[0]);
         double y = Double.parseDouble(coords[1]);
         double z = Double.parseDouble(coords[2]);
         float yaw = Float.parseFloat(coords[3]);
         float pitch = Float.parseFloat(coords[4]);
 
         return new Location(w, x, y, z, yaw, pitch);
     }
 
     /**
      * @return the lounge
      */
     public static Waypoint getLounge() {
         return lounge;
     }
 
     /**
      * @param lounge
      */
     public static void setLounge(Waypoint lounge) {
         Waypoint.lounge = lounge;
     }
 
     /**
      * @return the exit
      */
     public static Waypoint getExit() {
         return exit;
     }
 
     /**
      * @param exit
      */
     public static void setExit(Waypoint exit) {
         Waypoint.exit = exit;
     }
 
 }
