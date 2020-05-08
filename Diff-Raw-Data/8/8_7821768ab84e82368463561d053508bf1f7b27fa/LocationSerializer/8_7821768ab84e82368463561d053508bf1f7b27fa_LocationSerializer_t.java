 package com.tyzoid.jailr.serialization;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 
 public class LocationSerializer {
     Location location = null;
     String string = null;
 
     public LocationSerializer(Location location, String string) {
         this.location = location;
         this.string = string;
     }
 
     public LocationSerializer(Location location) {
         this.location = location;
     }
 
     public LocationSerializer(String string) {
         this.string = string;
     }
 
     public String getString() {
         double x = this.location.getX();
         double y = this.location.getY();
         double z = this.location.getZ();
         String world = this.location.getWorld().getName();
         float yaw = this.location.getYaw();
         float pitch = this.location.getPitch();
 
         return world + "," + x + "," + y + "," + z + "," + yaw + "," + pitch;
     }
 
     public Location getLocation() {
         String[] splitLocation = this.string.split(",");
        String world = splitLocation[0];
        double x = Double.parseDouble(splitLocation[1]);
        double y = Double.parseDouble(splitLocation[2]);
        double z = Double.parseDouble(splitLocation[3]);
         float yaw = Float.parseFloat(splitLocation[4]);
         float pitch = Float.parseFloat(splitLocation[5]);
 
         return new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
     }
 }
