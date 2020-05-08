 /*
 * Copyright (C) 2013 daboross
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package net.daboross.bukkitdev.skywars.api.location;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Level;
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.serialization.ConfigurationSerializable;
 import org.bukkit.configuration.serialization.SerializableAs;
 import org.bukkit.entity.Entity;
 
 /**
  *
  * @author daboross
  */
 @SerializableAs("SkyLocation")
 public class SkyBlockLocation implements ConfigurationSerializable {
 
     public final int x;
     public final int y;
     public final int z;
     public final String world;
 
     public SkyBlockLocation(int x, int y, int z, String world) {
         this.x = x;
         this.y = y;
         this.z = z;
         this.world = world;
     }
 
     public SkyBlockLocation(Block block) {
         this(block.getX(), block.getY(), block.getZ(), block.getWorld().getName());
     }
 
     public SkyBlockLocation(Location location) {
         this(location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getWorld().getName());
     }
 
     public SkyBlockLocation(Entity entity) {
         this(entity.getLocation());
     }
 
     public SkyBlockLocation add(int modX, int modY, int modZ) {
         return new SkyBlockLocation(x + modX, y + modY, z + modZ, world);
     }
 
     public SkyBlockLocation add(SkyBlockLocation location) {
         return new SkyBlockLocation(this.x + location.x, this.y + location.y, this.z + location.z, world);
     }
 
     public SkyPlayerLocation add(SkyPlayerLocation location) {
         return new SkyPlayerLocation(this.x + location.x, this.y + location.y, this.z + location.z, world);
     }
 
     public SkyBlockLocation changeWorld(String newWorld) {
         return new SkyBlockLocation(x, y, z, newWorld);
     }
 
     public boolean isNear(Location loc) {
         return world.equals(loc.getWorld().getName())
                 && x <= loc.getX() + 1 && x >= loc.getX() - 1
                 && y <= loc.getY() + 1 && y >= loc.getY() - 1
                 && z <= loc.getZ() + 1 && z >= loc.getZ() - 1;
     }
 
     public Location toLocation() {
         World bukkitWorld = null;
         if (world != null) {
             bukkitWorld = Bukkit.getWorld(world);
         }
         if (bukkitWorld == null) {
             Bukkit.getLogger().log(Level.WARNING, "[SkyWars] [SkyBlockLocation] World ''{0}'' not found when {1}.toLocation() called", new Object[]{world, this});
         }
         return new Location(bukkitWorld, x, y, z);
     }
 
     @Override
     public Map<String, Object> serialize() {
         Map<String, Object> map = new HashMap<String, Object>();
         map.put("xpos", x);
         map.put("ypos", y);
         map.put("zpos", z);
         if (world != null) {
             map.put("world", world);
         }
         return map;
     }
 
     public static SkyBlockLocation deserialize(Map<String, Object> map) {
         Object xObject = map.get("xpos"),
                 yObject = map.get("ypos"),
                 zObject = map.get("zpos"),
                 worldObject = map.get("world");
         if (!(xObject instanceof Integer && yObject instanceof Integer && zObject instanceof Integer)) {
            Bukkit.getLogger().log(Level.WARNING, "[SkyWars] [SkyBlockLocation] Silently failing deserialization due to x, y or z not existing on map or not being integers.");
             return null;
         }
         Integer x = (Integer) xObject, y = (Integer) yObject, z = (Integer) zObject;
         String worldString = worldObject == null ? null : worldObject.toString();
         return new SkyBlockLocation(x, y, z, worldString);
     }
 
     public static SkyBlockLocation deserialize(ConfigurationSection configurationSection) {
         Object xObject = configurationSection.get("xpos"),
                 yObject = configurationSection.get("ypos"),
                 zObject = configurationSection.get("zpos"),
                 worldObject = configurationSection.get("world");
         if (!(xObject instanceof Integer
                 && yObject instanceof Integer
                 && zObject instanceof Integer)) {
            Bukkit.getLogger().log(Level.WARNING, "[SkyWars] [SkyBlockLocation] Silently failing deserialization from configurationSection due to x, y or z not existing on map or not being integers.");
             return null;
         }
         Integer x = (Integer) xObject, y = (Integer) yObject, z = (Integer) zObject;
         String worldString = worldObject instanceof String ? (String) worldObject : worldObject == null ? null : worldObject.toString();
         return new SkyBlockLocation(x, y, z, worldString);
     }
 
     @Override
     public boolean equals(Object obj) {
         if (!(obj instanceof SkyBlockLocation)) {
             return false;
         }
         SkyBlockLocation l = (SkyBlockLocation) obj;
         return l.x == x && l.y == y && l.z == z && l.world.equals(world);
     }
 
     @Override
     public int hashCode() {
         int hash = 7;
         hash = 79 * hash + this.x;
         hash = 79 * hash + this.y;
         hash = 79 * hash + this.z;
         hash = 79 * hash + (this.world != null ? this.world.hashCode() : 0);
         return hash;
     }
 
     @Override
     public String toString() {
         return "SkyBlockLocation{x=" + x + ",y=" + y + ",z=" + z + ",world=" + world + "]";
     }
 }
