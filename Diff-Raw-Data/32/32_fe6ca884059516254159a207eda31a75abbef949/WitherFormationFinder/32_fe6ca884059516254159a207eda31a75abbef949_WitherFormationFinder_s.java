 package me.limebyte.endercraftessentials;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 
 /**
  * Utility class for searching for the natural bedrock formation used to assist
  * in killing a Wither.
  * @author LimeByte
 * @version 4
  */
 public class WitherFormationFinder {
 
     public static Set<Location> searchArea(Location location, int radius) {
         Set<Location> locations = new HashSet<Location>();
         final int x = location.getBlockX();
         final int z = location.getBlockZ();
 
         for (int xOffset = -radius; xOffset < radius; xOffset++) {
            location.setX(x + xOffset);
             for (int zOffset = -radius; zOffset < radius; zOffset++) {
                location.setZ(z + zOffset);
                if (check(location)) {
                    locations.add(location);
                 }
             }
         }
 
         if (radius == 0) {
             if (check(location)) {
                 locations.add(location);
             }
         }
 
         return locations;
     }
 
     private static boolean checkCentre(Location location) {
         location.setY(1);
         if (location.getBlock().getType() == Material.BEDROCK) return false;
 
         location.setY(2);
         if (location.getBlock().getType() == Material.BEDROCK) return false;
 
         location.setY(3);
         if (location.getBlock().getType() == Material.BEDROCK) return false;
 
         location.setY(4);
         if (location.getBlock().getType() != Material.BEDROCK) return false;
 
         return true;
     }
 
     private static boolean checkSide(Location location) {
         location.setY(2);
         if (location.getBlock().getType() == Material.BEDROCK) return false;
 
         location.setY(3);
         if (location.getBlock().getType() == Material.BEDROCK) return false;
 
         return true;
     }
 
     private static boolean check(Location location) {
         if (!checkCentre(location)) return false;
 
         final int x = location.getBlockX();
         final int z = location.getBlockZ();
 
         location.setX(x - 1);
         if (checkSide(location)) {
             location.setX(x + 1);
             if (checkSide(location)) return true;
         }
 
         location.setX(x);
         location.setZ(z - 1);
         if (checkSide(location)) {
             location.setZ(z + 1);
             if (checkSide(location)) return true;
         }
 
         return false;
     }
 
 }
